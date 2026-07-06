# Архитектура listener-системы для Mater-полей

**Дата:** 2026-07-06
**Задача:** Спроектировать архитектуру поддержания консистентности материализованных калькулируемых полей (`FieldTyperCalc*Mater`) через listener-систему в Twins (Spring Boot 3.5 / Java 21 / PostgreSQL)
**Тип:** Технический дизайн / Архитектурное решение (ADR)
**Статус:** Полная замена `field-typer-mater-listeners.md` (v1) — упрощённая схема метаданных + адаптированный перенос критичных тем из v1

---

## 0. TL;DR для разработчика

**Проблема.** Mater-поля (`FieldTyperCalcSumMater`, `FieldTyperCalcSubtractionMater`, ...) хранят вычисленный результат в `twin_field_decimal` через `serializeValue()`. Но `serializeValue` вызывается только если поле пришло в request payload (`TwinService.convertTwinFields:841-853` итерирует по `fields` map из запроса). Когда пользователь меняет operand-поле (например `firstFieldId`), Mater-поле остаётся со старым значением. Когда C/U/D происходит с дочкой — сумма у родителя не обновляется. Mater-поля в проде сейчас **фактически неработоспособны** — нет гарантии консистентности.

**Решение.** Две listener-таблицы + одна таблица validator-rule:

- `listener_on_field` — на изменение конкретного `twin_class_field` (intra-twin и cross-twin через TwinPointer)
- `listener_on_action` — на CREATE/EDIT/DELETE над твинами указанного класса (для `FieldTyperCalcChildrenFieldV1`-кейсов)
- `listener_on_action_validator_rule` — необязательные validator_set-ы для сложных предикатов (например, «учитывать ребёнка в сумме только если status=ACTIVE»)

Поиск подписчика (твина, чьё Mater-поле надо пересчитать) идёт через `TwinPointer` — настраиваемый для класса твинов указатель (родитель, родитель родителя, dst TwinLink, родитель dst TwinLink, SELF). Sync/async регулируется per-listener boolean-флагом. Вызовы listeners — в `TwinService.createTwin` / `updateTwin` между `validateAndCollect` и `applyChanges` (команды попадают в тот же collector → atomicity).

**MVP-scope.** Заполнение listener-таблиц — ручное, SQL INSERT'ами напрямую. Будет smoke-test механики. Будущая итерация — авто-регистрация из FieldTyper в `TwinClassFieldService.saveField()`.

**Что НЕ делаем.** Не Hibernate `PostUpdateEventListener` (слеп к `@ManyToMany` на TwinLink). Не Spring `ApplicationEventPublisher` по 10 сервисам (churn). Не `@Async + @TransactionalEventListener` (ломает `TransactionSynchronization`). Не PostgreSQL triggers (нарушают multi-tenant isolation — `ApiUser` не виден в PL/pgSQL, плюс дублирование бизнес-логики FieldTyper). Не Materialized Views (REFRESH слишком дорог на high-traffic — до 25× медленнее concurrent).

---

## 1. Контекст

### 1.1. Иерархия FieldTyper (упрощённо)

```
FieldTyper<D,T,S,A>  (abstract, FeaturerTwins)
  ├─ FieldTyperSimple, FieldTyperText, FieldTyperBoolean, ...   (обычные хранимые поля)
  ├─ FieldTyperImmutable<D,T,S,A>                                (immutable поля)
  │    ├─ FieldTyperCalcBinaryBase  implements FieldTyperCalcBinary    ← ON-FLY калькулятор
  │    │    └─ FieldTyperCalcSum, FieldTyperCalcSubtraction,
  │    │       FieldTyperCalcMultiplication, FieldTyperCalcDivision
  │    ├─ FieldTyperCalcChildrenFieldV1  implements FieldTyperCalcChildrenField  ← ON-FLY
  │    │    └─ (суммирует поле детей с фильтром статуса)
  │    └─ FieldTyperDecimalBase<Numeric, Text, Numeric>
  │         └─ FieldTyperCalcBinaryMater  implements FieldTyperCalcBinary, FieldTyperCalcMater
  │              ├─ FieldTyperCalcSumMater           ← MATERIALIZED
  │              ├─ FieldTyperCalcSubtractionMater   ← MATERIALIZED
  │              ├─ FieldTyperCalcMultiplicationMater ← MATERIALIZED
  │              └─ FieldTyperCalcDivisionMater      ← MATERIALIZED
```

### 1.2. Как работает on-the-fly калькулятор (`FieldTyperCalcChildrenFieldV1`)

```java
// FieldTyperCalcChildrenFieldV1.java:27
@Override
protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
    return new FieldValueText(twinField.getTwinClassField())
            .setValue(String.valueOf(
                twinField.getTwin().getTwinFieldCalculated()
                    .get(twinField.getTwinClassFieldId())));
}
```

При каждом read ходит в `twin.getTwinFieldCalculated()` — кэш, который заполняется одним SQL `sumChildrenTwinFieldValuesWithStatusIn/NotIn(twinId, fieldId, statusIds)`. Этот SQL делает JOIN `twin_field_decimal ⊕ twin ⊕ WHERE twin.status_id IN (...)`. На твине со 100 полями и горячим buffer-pool это **5–15 ms per read**. На большом классе с тысячами детей — экспоненциальный рост.

### 1.3. Как работает materialized (`FieldTyperCalcBinaryMater`)

```java
// FieldTyperCalcBinaryMater.java:27
@Override
protected void serializeValue(Properties properties, TwinEntity twin,
                              TwinFieldDecimalEntity twinFieldEntity,
                              FieldValueText value,
                              TwinChangesCollector twinChangesCollector) throws ServiceException {
    if (skipIfEmpty(twin, properties, twinClassFieldService,
            List.of(firstFieldId.extract(properties), secondFieldId.extract(properties)),
            value.getTwinClassField())) {
        return;
    }
    if (twinFieldEntity == null) {
        twinFieldEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), null);
        twinChangesCollector.add(twinFieldEntity);  // ← ПОПАДАЕТ В tx ЧЕРЕЗ COLLECTOR
    }
    var firstValue  = twinClassFieldService.getDecimalValue(twin, firstFieldId.extract(properties), BigDecimal.ZERO);
    var secondValue = twinClassFieldService.getDecimalValue(twin, secondFieldId.extract(properties), BigDecimal.ZERO);
    detectValueChange(twinFieldEntity, twinChangesCollector, calculate(firstValue, secondValue, properties));
}
```

**Сценарий 1 — работает:** Пользователь через UI создаёт twin, в payload приходит Mater-поле со значением. `serializeValue` вызывается, читает operands, считает, пишет результат в `twin_field_decimal`. ✓

**Сценарий 2 — НЕ работает:** Пользователь обновляет `firstFieldId` (operand). Request содержит только operand-поле. В `TwinService.convertTwinFields:843-851` итерация идёт по `twinClassFieldKit`, но `serializeFieldValue:855-861` вызывает `serializeValue` только если поле есть в `fields` map из request. Mater-поля нет в request → его `serializeValue` не вызывается → значение не пересчитывается. ✗

**Сценарий 3 — НЕ работает:** Дочерний twin обновил своё поле. У родителя Mater-поле (`FieldTyperCalcChildrenFieldV1` analogue в materialized виде) должно пересчитаться, но родителя вообще нет в транзакции. ✗

### 1.4. Типы listeners

Таксономия по **trigger-source** — источнику события, на которое подписан listener. Каждому типу соответствует отдельная таблица метаданных; новые источники событий = новые таблицы (контролируемое расширение).

| Trigger-source | Таблица | Описание | Пример |
|----------------|---------|----------|--------|
| **OnField** | `listener_on_field` | Изменилось значение поля `publisher_twin_class_field_id` у одного или нескольких твинов. Подписчик — twin, на который указывает `subscriber_twin_pointer_id` от твина-publisher'а | `FieldTyperCalcSumMater` слушает `firstFieldId`/`secondFieldId` через pointer SELF. Или parent-class Mater слушает поле child-class через pointer PARENT |
| **OnAction** | `listener_on_action` + `listener_on_action_validator_rule` | Выполнен TwinAction (CREATE/EDIT/DELETE) над твином класса `publisher_twin_class_id`. Подписчик — twin, на который указывает `subscriber_twin_pointer_id` от publisher-а | `FieldTyperCalcChildrenFieldV1` analogue в материализованном виде: child-class CREATE/EDIT/DELETE → пересчёт суммы у parent-class Mater |
| _OnLinkChange_ (future) | _projected_ | Изменение TwinLink между твинами | Когда TwinLink «task→assignee» удаляется, пересчитать нагрузку assignee |
| _OnMarkerChange_ (future) | _projected_ | TwinMarker установлен/снят | При marker BLOCKED → исключить twin из суммы родителя |
| _OnTagChange_ (future) | _projected_ | TwinTag добавлен/удалён | При tag PRIORITY=HIGH → коэффициент умножения в parent-агрегате |

**Почему не одна таблица с enum.** Разные trigger-source-ы требуют разных схем: OnField хранит publisher_field_id, OnAction — publisher_twin_action_id + опциональный validator_set. Запихнуть в одну таблицу → sparse anti-pattern с nullable-колонками. Раздельные таблицы — схемная чистота + easy extendability (новый trigger-source = новая таблица, а не nullable-каша).

**Почему не пять таблиц сразу.** OnField и OnAction исчерпывают текущие Mater-сценарии (`FieldTyperCalcBinaryMater` для intra/cross-field, `FieldTyperCalcChildrenFieldV1` для children-CRUD). Остальные источники — это *точки расширения*, их заводить под гипотетические Mater-варианты (которых в иерархии FieldTyper сейчас нет) — premature.

### 1.5. Структура таблиц

```sql
-- ============================================================
-- listener_on_field: срабатывает на изменение поля publisher_twin_class_field_id
-- ============================================================
create table if not exists listener_on_field
(
    id                             uuid not null
        constraint listener_on_field_pk primary key,
    domain_id                      uuid not null
        constraint listener_on_action_domain_id_fk
            references domain on update cascade on delete cascade,
    subscriber_twin_pointer_id     uuid not null
        constraint listener_on_field_sub_twin_pointer_id_fk
            references twin_pointer on update cascade on delete cascade,
    subscriber_twin_class_field_id uuid not null
        constraint listener_on_field_sub_twin_class_field_id_fk
            references twin_class_field on update cascade  on delete cascade,
    publisher_twin_class_field_id  uuid not null
        constraint listener_on_field_pub_twin_class_field_id_fk
            references twin_class_field on update cascade on delete cascade,
    async                          boolean not null default false
);

create index if not exists listener_on_field_twin_class_field_id_index
    on listener_on_field (subscriber_twin_class_field_id);
create index if not exists listener_on_field_pub_twin_class_field_id_idx
    on listener_on_field (publisher_twin_class_field_id);
create index if not exists listener_on_field_sub_twin_pointer_id_idx
    on listener_on_field (subscriber_twin_pointer_id);

-- ============================================================
-- listener_on_action: срабатывает на TwinAction над твином класса publisher_twin_class_id
-- ============================================================
create table if not exists listener_on_action
(
    id                             uuid not null
        constraint listener_on_action_pk primary key,
    domain_id                      uuid not null
        constraint listener_on_action_domain_id_fk
            references domain on update cascade on delete cascade,
    subscriber_twin_pointer_id     uuid not null
        constraint listener_on_action_sub_twin_pointer_id_fk
            references twin_pointer on update cascade on delete cascade,
    subscriber_twin_class_field_id uuid not null
        constraint listener_on_action_sub_twin_class_field_id_fk
            references twin_class_field on update cascade  on delete cascade,
    publisher_twin_class_id        uuid not null
        constraint listener_on_action_pub_twin_class_id_fk
            references twin_class on update cascade  on delete cascade,
    publisher_twin_action_id       varchar not null
        constraint listener_on_action_pub_twin_action_id_fk
            references twin_action on update cascade on delete cascade,
    async                          boolean not null default false
);

create index if not exists listener_on_action_sub_twin_class_field_idx
    on listener_on_action (subscriber_twin_class_field_id);
create index if not exists listener_on_action_pub_class_action_idx
    on listener_on_action (publisher_twin_class_id, publisher_twin_action_id);
create index if not exists listener_on_action_sub_twin_pointer_idx
    on listener_on_action (subscriber_twin_pointer_id);

-- ============================================================
-- listener_on_action_validator_rule: необязательные validator_set-ы,
-- которые проверяются ДО Mater recompute при срабатывании listener_on_action.
-- Например: «учитывать ребёнка только если status=ACTIVE».
-- Паттерн переиспользует существующую twin_action_validator_rule (см. TwinActionValidatorRuleEntity).
-- ============================================================
create table if not exists listener_on_action_validator_rule
(
    id                              uuid                 not null
        constraint listener_on_action_validator_rule_pk primary key,
    listener_on_action_id           uuid                 not null
        constraint listener_on_action_id_fk
            references listener_on_action on update cascade on delete cascade,
    "order"                         integer default 1,
    active                          boolean default true not null,
    twin_validator_set_id           uuid
        constraint listener_on_action_id_twin_validator_set_id_fk
            references twin_validator_set on update cascade on delete cascade
);

create unique index if not exists listener_on_action_validator_rule_order_uniq
    on listener_on_action_validator_rule (listener_on_action_id, "order");
create index if not exists listener_on_action_validator_rule_twin_validator_set_idx
    on listener_on_action_validator_rule (twin_validator_set_id);
```

**Обоснование колонок.**

| Колонка | Зачем |
|---------|-------|
| `subscriber_twin_pointer_id` | TwinPointer — это настраиваемый для twin_class указатель (через PointerFeaturer). Указывает, **какой twin-подписчик** соответствует publisher-у: SELF, PARENT, GRANDPARENT, dst TwinLink, parent-of-dst-TwinLink и т.д. NOT NULL — для каждого listener-а способ нахождения подписчика должен быть задан явно |
| `subscriber_twin_class_field_id` | Mater-поле подписчика, которое надо пересчитать |
| `publisher_twin_class_field_id` (OnField) | Operand-поле publisher-а, изменение которого триггерит listener |
| `publisher_twin_class_id` (OnAction) | Класс publisher-твина, над которым выполняется action |
| `publisher_twin_action_id` (OnAction) | ID из TwinAction enum (CREATE/EDIT/DELETE/...). varchar + FK на `twin_action` для ссылочной целостности |
| `async` | Per-listener флаг sync/async. `false` → recompute в той же tx. `true` → постановка в `TwinChangeTaskEntity(NEED_START)` (outbox). По умолчанию `false` |
| `domain_id` | Multi-tenant isolation; lookup listeners только в текущем домене ApiUser |

---

## 2. Механика срабатывания

### 2.1. TwinPointer как ключ к нахождению подписчика

`TwinPointerEntity` — это метаданные на уровне `twin_class`: связывает `twin_class_id` и `pointer_featurer_id` + `pointer_params`. Pointer-featurer — это реализация логики «для данного твина верни target-twin» (например, «верни parent через TwinLink типа X», «верни grandparent через 2 hop», «верни самого себя»).

**Примеры pointer-конфигураций:**

| Pointer-featurer | params | Возвращает |
|------------------|--------|------------|
| `TwinPointerSelf` | — | тот же twin (для intra-twin Mater) |
| `TwinPointerByLink` | `linkType=PARENT_CHILD, direction=SRC→DST` | parent-твин |
| `TwinPointerByLink` | `linkType=PARENT_CHILD, direction=SRC→DST, hops=2` | grandparent |
| `TwinPointerByLink` | `linkType=ASSIGNED_TO, direction=DST→SRC` | все assigner-ы |
| `TwinPointerDstTwinLink` | `linkType=...` | dst twin конкретной TwinLink |
| `TwinPointerParentOfDstTwinLink` | `linkType=...` | parent dst twin-а |

При срабатывании listener-а рантайм:
1. Грузит `TwinPointerEntity` по `subscriber_twin_pointer_id`
2. Через `Pointer.point(publisherTwin)` получает подписчика (может быть 0 или 1)
3. Для каждого подписчика — пересчитывает `subscriber_twin_class_field_id`

### 2.2. Точка вызова в TwinService

Вызовы listeners — в `TwinService.createTwin` / `TwinService.updateTwin` **между** `validateAndCollect` и `applyChanges`:

```java
// TwinService.java — упрощённо
public void createTwin(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
    createTwins(TwinCreateStage.of(twinCreate), twinChangesCollector);   // существующая логика
    // === НОВЫЙ HOOK ===
    materListenerTriggerService.triggerAffected(twinChangesCollector);   // добавляет Mater recompute команды в тот же collector
    // === КОНЕЦ HOOK ===
}

public void updateTwin(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector, ...) throws ServiceException {
    // ... существующая логика ...
    materListenerTriggerService.triggerAffected(twinChangesCollector);
}
```

**Почему здесь, а не внутри `applyChanges` (как в v1).** В этой схеме listener-ы — про бизнес-события CREATE/EDIT/DELETE над твином, а не про низкоуровневые C/U/D сущностей. Срабатывание должно происходить в терминах бизнес-операции, а не в terms JPA entity lifecycle. `applyChanges` слишком низкоуровневый — он не различает « CREATED new twin» от «UPDATED fields of existing twin».

**Atomicity.** Команды Mater recompute кладутся в тот же `TwinChangesCollector` → попадают в ту же tx через `applyChanges`. Sync listeners обновляются атомарно с publisher-изменением. Async listeners добавляют запись в `TwinChangeTaskEntity` (через `collector.addPostponed`) — тоже атомарно с publisher-изменением (outbox pattern).

### 2.3. Sync vs async

Per-listener boolean `async` определяет контракт:

| `async` | Поведение | 适用 scenarios |
|---------|-----------|---------------|
| `false` (default) | Recompute в той же tx. Если Mater-поле не может быть пересчитано (например, operand missing) → вся tx откатывается | Intra-twin Mater (FieldTyperCalcBinaryMater с SELF-pointer). Бизнес-консистентность как FK constraint |
| `true` | Добавляем `TwinChangeTaskEntity` в collector; worker-pool асинхронно забирает и пересчитывает. SLA: per-event <1s lag, bulk p95 ≤30s, p99 ≤60s | Cross-twin Mater с deep cascade. Bulk operations (>50 twins). Mater с fan-out на тысячи подписчиков |

**Принцип выбора.** Mater-поле помечается как STRONG (sync) только если: (а) указатель SELF, (б) нет каскада (Mater-поле не является operand-ом для другого Mater). Иначе — EVENTUAL (async). При ручной регистрации (MVP) — отвечает автор INSERT-а. При будущей авто-регистрации из FieldTyper — вычисляется автоматически по графу зависимостей.

---

## 3. MVP: ручное заполнение таблиц

До реализации авто-регистрации из FieldTyper, listeners регистрируются **SQL INSERT'ами напрямую** в БД. Это для smoke-test механики.

### 3.0. Предварительные требования

1. Расширить `TwinAction` enum — добавить `CREATE`:

```java
// org.twins.core.enums.action.TwinAction
public enum TwinAction {
    CREATE,   // ← НОВЫЙ
    EDIT,
    DELETE,
    // ... существующие
}
```

2. В БД добавить:

```sql
INSERT INTO twin_action (id) VALUES ('CREATE') ON CONFLICT DO NOTHING;
```

3. Завести системные TwinPointer-ы для базовых сценариев (если ещё нет):

```sql
-- Pointer "на самого себя" — для intra-twin Mater
INSERT INTO twin_pointer (id, twin_class_id, pointer_featurer_id, name)
VALUES ('00000000-0000-0000-0000-self000000001', '<class_id>', <TwinPointerSelf_featurer_id>, 'SELF')
ON CONFLICT DO NOTHING;

-- Pointer "на parent через TwinLink типа PARENT_CHILD"
INSERT INTO twin_pointer (id, twin_class_id, pointer_featurer_id, pointer_params, name)
VALUES ('00000000-0000-0000-0000-parent00000001', '<child_class_id>', <TwinPointerByLink_featurer_id>,
        'linkType=PARENT_CHILD'::hstore, 'PARENT')
ON CONFLICT DO NOTHING;
```

### 3.1. Intra-twin Mater (FieldTyperCalcSumMater)

**Сценарий:** В классе `Order` есть поля `price` (operand) и `total` (Mater: sum price + tax). При обновлении `price` надо пересчитать `total` в том же твине.

```sql
INSERT INTO listener_on_field (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
                               publisher_twin_class_field_id, async, domain_id)
VALUES (
    gen_random_uuid(),
    '<SELF_pointer_id_for_Order_class>',           -- указатель "на того же twin-а"
    '<twin_class_field_id для total>',             -- Mater-поле подписчика
    '<twin_class_field_id для price>',             -- operand publisher-а
    false,                                          -- sync
    '<domain_id>'
);

-- Аналогично для tax
INSERT INTO listener_on_field (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
                               publisher_twin_class_field_id, async, domain_id)
VALUES (
    gen_random_uuid(),
    '<SELF_pointer_id_for_Order_class>',
    '<twin_class_field_id для total>',
    '<twin_class_field_id для tax>',
    false,
    '<domain_id>'
);
```

### 3.2. Cross-twin Mater через TwinLink (FieldTyperCalcChildrenFieldV1-analogue)

**Сценарий:** `Project` имеет Mater-поле `totalTaskEstimate` = SUM `estimate` всех `Task`-детей. При UPDATE `estimate` у Task должен пересчитаться `Project.totalTaskEstimate`.

```sql
-- pointer "на Project через TwinLink PARENT_CHILD" — настраивается на классе Task
INSERT INTO listener_on_field (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
                               publisher_twin_class_field_id, async, domain_id)
VALUES (
    gen_random_uuid(),
    '<PARENT_pointer_id_for_Task_class>',          -- указатель "на Project родитель"
    '<twin_class_field_id для Project.totalTaskEstimate>',
    '<twin_class_field_id для Task.estimate>',
    false,                                          -- sync для single child update; рассмотреть true для deep cascade
    '<domain_id>'
);
```

### 3.3. OnAction listener (CRUD над твином-ребёнком)

**Сценарий:** `Project.budgetUsed` = SUM всех `Task.cost` где Task.status IN (IN_PROGRESS, DONE). При CREATE новой Task — пересчитать `budgetUsed` с учётом новой задачи (если она подходит по статусу). При DELETE Task — вычесть.

```sql
INSERT INTO listener_on_action (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
                                publisher_twin_class_id, publisher_twin_action_id, async, domain_id)
VALUES (
    gen_random_uuid(),
    '<PARENT_pointer_id_for_Task_class>',          -- указатель на Project родитель
    '<twin_class_field_id для Project.budgetUsed>',
    '<Task_twin_class_id>',
    'CREATE',                                       -- TwinAction.CREATE
    true,                                           -- async (CRUD может быть bulk)
    '<domain_id>'
);

-- То же для EDIT и DELETE
INSERT INTO listener_on_action (...) VALUES (..., 'EDIT',  true, ...);
INSERT INTO listener_on_action (...) VALUES (..., 'DELETE', true, ...);
```

### 3.4. Validator rule для сложных предикатов

**Сценарий:** Аналогично §3.3, но переcчёт `budgetUsed` только если `Task.status IN (IN_PROGRESS, DONE)` (т.е. DRAFT не учитывается). Используем существующий `twin_validator_set` с предикатом `status IN (...)`.

```sql
-- Предполагаем, что validator_set уже создан (например, 'Task in active status')
INSERT INTO listener_on_action_validator_rule (id, listener_on_action_id, "order", active, twin_validator_set_id)
VALUES (
    gen_random_uuid(),
    '<listener_on_action_id для CREATE из §3.3>',
    1,
    true,
    '<validator_set_id для "active status">'
);
-- Аналогично для EDIT и DELETE listeners
```

При срабатывании listener-а:
1. Если validator_rules есть → `ValidatorService.validate(publisherTwin, validatorSet)` для каждого rule по `order`
2. Если все правила проходят → Mater recompute выполняется
3. Если хотя бы одно fails → recompute пропускается (для DRAFT Task сумма не пересчитывается), логируется `mater_listener_validation_skipped_total`

---

## 4. Что унаследовано из v1 в адаптированном виде

v1 (см. `field-typer-mater-listeners.md`) содержал критичные темы, которые **обязательно** переносим. Ниже — адаптация под новую схему.

### 4.1. Cycle protection

**Цикл:** Mater-поле A зависит от B, B зависит от A → бесконечный recompute → DoS базы.

**Защита — 4 уровня:**

1. **Статический DAG при config-time** (после ручного INSERT-а или авто-регистрации):
   - Строим граф: `subscriber_twin_class_field_id → publisher_twin_class_field_id` (по `listener_on_field`)
   - DFS с цветами (WHITE/GRAY/BLACK); GRY-обращение = cycle
   - При обнаружении — `ServiceException(CONFIGURATION_IS_INVALID, "Cycle: A → B → A")`, listener не регистрируется

2. **Runtime `visitedSet<MaterFieldId>` per tx** — в одном `triggerAffected` проходе не запускать recompute одного и того же Mater-поля дважды (защита от дублей и скрытых циклов)

3. **Max-depth cap** — `twins.mater.max-depth: 5` (configurable). При превышении — warning + skip

4. **Logging при depth > 3** — info-лог для выявления глубоких конфигураций; > cap → warn

**Для OnAction listeners** цикл менее вероятен (subscriber-ы и publisher-ы — разных классов), но проверка всё равно нужна: A-class listener на CREATE B-class → B-class listener на UPDATE A-class.field → ... . Строим граф через TwinPointer resolution и проверяем аналогично.

### 4.2. Bulk detection

**Проблема:** Bulk-update 10k Task → 10k Mater recompute → DB pool exhaustion (см. v1 §4.5: ~130s blocking tx).

**Решение:**
- В `triggerAffected()` после первого прохода по collector-у считаем `touchedTwinCount`
- Если `touchedTwinCount > twins.mater.bulk-threshold` (default 50) → создаём **одну consolidated `TwinChangeTaskEntity`** с payload=`{twinIds: [...], fieldIds: [...]}`, не 10k записей
- Worker-pool забирает consolidated task, чанкует по 100, выполняет batch SQL recompute одним `UPDATE`
- В payload храним `processedTwinIds` для retry с checkpoint при partial failure

**Для sync listeners bulk-detection форсирует async** — даже если listener.async=false, при bulk синхронно нельзя.

### 4.3. Caffeine cache

Hot path: lookup listeners не должен ходить в БД на каждый twin update. Cache по ключу:
- `OnField`: key=`(domain_id, publisher_twin_class_field_id)`, value=`List<ListenerOnField>`
- `OnAction`: key=`(domain_id, publisher_twin_class_id, publisher_twin_action_id)`, value=`List<ListenerOnAction>`

TTL 5 min + invalidation по `TwinClassField`/`TwinClass` update.

### 4.4. `is_derived` flag (MVP CQRS)

```sql
ALTER TABLE twin_field_decimal ADD COLUMN IF NOT EXISTS is_derived BOOLEAN NOT NULL DEFAULT false;
```

`true` → значение записано Mater-механизмом, `false` → user-written. Reconciliation знает, что сравнивать. Read-path при необходимости мёрджит.

### 4.5. STALE marker

```sql
ALTER TABLE twin_class_field ADD COLUMN IF NOT EXISTS mater_stale BOOLEAN NOT NULL DEFAULT false;
```

При удалении operand-field (`publisher_twin_class_field_id`) → FK `ON DELETE NO ACTION` блокирует удаление, пока listener не удалён. После удаления listener-а → Mater-поле помечается `mater_stale=true`. Read падает с `ServiceException(MATER_STALE)` — явно, не silent.

### 4.6. Reconciliation cron

```java
@Scheduled(cron = "0 0 */6 * * *")  // каждые 6 часов
public void reconciliationJob() {
    // Sample 100 случайных Mater-полей в каждом domain
    // Вычислить on-the-fly через исходный FieldTyper (не Mater)
    // Сравнить с stored value
    // Если drift > 0.1% → alert + trigger full backfill domain
}
```

Метрика: `mater_drift_ratio` per domain. SLO: <0.01%.

### 4.7. Backfill миграция

Для существующих Mater-полей в проде:
1. Создать listeners (аналогично §3, но через migration script `V1.x.y.z__TWINS-XXX_mater_listener_backfill.sql` по всем существующим Mater-полям)
2. Background job через `TwinChangeTaskEntity` — чанки по 1000 twinId + `pg_sleep(0.1)` между чанками
3. Checkpoint в payload для resume
4. После завершения — `VACUUM (ANALYZE) twin_field_decimal`
5. Через 1 час reconciliation должен показать 0% drift — валидация корректности

---

## 5. Java-интерфейсы и hook

### 5.1. Сущности (DAO)

```java
package org.twins.core.dao.listener;

@Data @Entity @Accessors(chain = true)
@Table(name = "listener_on_field")
public class ListenerOnFieldEntity {
    @Id private UUID id;
    @Column(name = "subscriber_twin_pointer_id")     private UUID subscriberTwinPointerId;
    @Column(name = "subscriber_twin_class_field_id") private UUID subscriberTwinClassFieldId;
    @Column(name = "publisher_twin_class_field_id")  private UUID publisherTwinClassFieldId;
    @Column(name = "async")                          private boolean async;
    @Column(name = "domain_id")                      private UUID domainId;
}

@Data @Entity @Accessors(chain = true)
@Table(name = "listener_on_action")
public class ListenerOnActionEntity {
    @Id private UUID id;
    @Column(name = "subscriber_twin_pointer_id")     private UUID subscriberTwinPointerId;
    @Column(name = "subscriber_twin_class_field_id") private UUID subscriberTwinClassFieldId;
    @Column(name = "publisher_twin_class_id")        private UUID publisherTwinClassId;
    @Column(name = "publisher_twin_action_id")       @Enumerated(EnumType.STRING) private TwinAction publisherTwinAction;
    @Column(name = "async")                          private boolean async;
    @Column(name = "domain_id")                      private UUID domainId;
}

@Data @Entity @Accessors(chain = true)
@Table(name = "listener_on_action_validator_rule")
public class ListenerOnActionValidatorRuleEntity {
    @Id private UUID id;
    @Column(name = "listener_on_action_id")          private UUID listenerOnActionId;
    @Column(name = "order")                          private Integer order;
    @Column(name = "active")                         private boolean active;
    @Column(name = "twin_validator_set_id")          private UUID twinValidatorSetId;
}
```

### 5.2. Сервис trigger-а

```java
package org.twins.core.service.listener;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterListenerTriggerService {

    private final ListenerOnFieldRepository listenerOnFieldRepository;
    private final ListenerOnActionRepository listenerOnActionRepository;
    private final ListenerOnActionValidatorRuleRepository validatorRuleRepository;
    private final PointerFeaturerService pointerFeaturerService;
    private final TwinClassFieldService twinClassFieldService;
    private final FeaturerService featurerService;
    private final TwinChangeTaskService twinChangeTaskService;
    private final CaffeineListenerCache listenerCache;

    @Value("${twins.mater.max-depth:5}")          private int maxDepth;
    @Value("${twins.mater.bulk-threshold:50}")    private int bulkThreshold;

    /**
     * Главный hook — вызывается из TwinService.createTwin / updateTwin
     * между validateAndCollect и applyChanges.
     */
    public void triggerAffected(TwinChangesCollector collector) throws ServiceException {
        AffectedSnapshot snapshot = extractAffected(collector);
        if (snapshot.isEmpty()) return;

        // 1. Bulk detection → consolidated async task
        if (snapshot.touchedTwinCount() > bulkThreshold) {
            scheduleConsolidatedBulkTask(snapshot, collector);
            return;
        }

        // 2. OnField listeners: один batch IN-query
        triggerOnFieldListeners(snapshot, collector);

        // 3. OnAction listeners: lookup по (twinClassId, action)
        triggerOnActionListeners(snapshot, collector);
    }

    private void triggerOnFieldListeners(AffectedSnapshot snapshot, TwinChangesCollector collector) throws ServiceException {
        Map<UUID, List<ListenerOnFieldEntity>> listenersByChangedField =
                listenerCache.findOnFieldListeners(snapshot.changedFieldClassIds(), snapshot.domainId());

        Set<UUID> visited = new HashSet<>();
        for (var entry : listenersByChangedField.entrySet()) {
            UUID changedFieldId = entry.getKey();
            for (var affectedTwinId : snapshot.twinsByChangedField(changedFieldId)) {
                for (var listener : entry.getValue()) {
                    triggerListener(listener, affectedTwinId, collector, visited, 0, null);
                }
            }
        }
    }

    private void triggerOnActionListeners(AffectedSnapshot snapshot, TwinChangesCollector collector) throws ServiceException {
        Map<ActionKey, List<ListenerOnActionEntity>> listenersByAction =
                listenerCache.findOnActionListeners(snapshot.twinActionsByKey(), snapshot.domainId());

        Set<UUID> visited = new HashSet<>();
        for (var entry : listenersByAction.entrySet()) {
            ActionKey key = entry.getKey();
            for (var publisherTwinId : snapshot.twinIdsByAction(key)) {
                for (var listener : entry.getValue()) {
                    if (!shouldFireByValidators(listener, publisherTwinId)) continue;
                    triggerListener(listener, publisherTwinId, collector, visited, 0, key.action());
                }
            }
        }
    }

    private boolean shouldFireByValidators(ListenerOnActionEntity listener, UUID publisherTwinId) throws ServiceException {
        List<ListenerOnActionValidatorRuleEntity> rules = validatorRuleRepository
                .findByListenerOnActionIdOrderByOrder(listener.getId());
        if (rules.isEmpty()) return true;
        for (var rule : rules) {
            if (!rule.isActive()) continue;
            ValidationResult r = validatorService.validate(publisherTwinId, rule.getTwinValidatorSetId());
            if (!r.isSuccess()) {
                meterRegistry.counter("mater_listener_validation_skipped.total",
                        Tags.of("reason", r.getReason())).increment();
                return false;
            }
        }
        return true;
    }

    private <L> void triggerListener(L listener, UUID publisherTwinId, TwinChangesCollector collector,
                                     Set<UUID> visited, int depth, TwinAction action) throws ServiceException {
        if (depth > maxDepth) {
            log.warn("Mater cascade depth {} exceeded max {} for publisher twin {}", depth, maxDepth, publisherTwinId);
            return;
        }

        UUID pointerId = getPointerId(listener);
        TwinPointerEntity pointer = twinPointerService.findById(pointerId);
        List<UUID> subscriberTwinIds = pointerFeaturerService.resolveSubscriberTwins(publisherTwinId, pointer);

        for (UUID subscriberTwinId : subscriberTwinIds) {
            UUID materFieldId = getSubscriberFieldId(listener);
            if (!visited.add(materFieldId)) continue;  // dedup + cycle protection

            if (isAsync(listener)) {
                scheduleAsyncRecompute(subscriberTwinId, materFieldId, collector);
            } else {
                recomputeSync(subscriberTwinId, materFieldId, collector);
                // Каскад: если этот Mater — operand для другого, trigger recursively
                cascadeIfNestedOperand(subscriberTwinId, materFieldId, collector, visited, depth + 1);
            }
        }
    }

    private void recomputeSync(UUID subscriberTwinId, UUID materFieldId, TwinChangesCollector collector) throws ServiceException {
        TwinClassFieldEntity field = twinClassFieldService.findById(materFieldId);
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        TwinEntity twin = twinService.findById(subscriberTwinId);
        FieldValue value = fieldTyper.tryToInitializeValue(twin, field);
        fieldTyper.serializeValue(twin, value, collector);  // → кладёт изменения в collector
    }

    private void scheduleAsyncRecompute(UUID subscriberTwinId, UUID materFieldId, TwinChangesCollector collector) {
        collector.addPostponedMaterRecompute(subscriberTwinId, materFieldId);
    }
}
```

### 5.3. Hook в TwinService

```java
// TwinService.java
public void createTwin(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
    createTwins(TwinCreateStage.of(twinCreate), twinChangesCollector);
    materListenerTriggerService.triggerAffected(twinChangesCollector);  // ← НОВЫЙ HOOK
}

public void updateTwin(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector,
                       ChangesRecorder<TwinEntity, ?> recorder) throws ServiceException {
    // ... существующая логика updateTwin ...
    materListenerTriggerService.triggerAffected(twinChangesCollector);  // ← НОВЫЙ HOOK
}
```

Внимание: точные имена методов и сигнатуры — на момент реализации уточняются по коду; главное — место вызова (после основного изменения, до `applyChanges`).

### 5.4. Регистрация listener-ов (future, из FieldTyper)

**Не в MVP.** Будущая итерация: при `TwinClassFieldService.saveField()` для Mater-полей — авто-регистрация listener-ов на основе `featurerParams` (extract operand-fields через `FieldTyperCalcBinary.firstFieldId.extract(...)`, etc.) + статический cycle detection в той же tx. Устраняет dual-write drift между конфигурацией FieldTyper и listener registration.

---

## 6. Что НЕ переносим из v1 — и почему

| Тема v1 | Что с ней |
|---------|-----------|
| Единый write-path audit | Не отдельная задача — naturally покрыто вызовом в `createTwin`/`updateTwin`. gRPC/GraphQL paths тоже идут через TwinService — audit встроенный |
| Delta-increment через JDBC для SUM | Сохраняется как optimization в `recomputeSync` для SUM-вариантов (reuse `applyDecimalIncrements` паттерн). Не как отдельная архитектурная тема |
| `consistency_class = STRONG \| EVENTUAL` | Заменено per-listener `async` boolean — гибче и точнее |
| Энум `EventSourceType` | Заменён отдельными таблицами (см. §1.4 rationale) |
| `Scope` enum (SELF/PARENT/GRANDPARENT/HEAD) | Заменён `TwinPointer` — более общее решение (pointer-featurer может быть любой сложности) |

---

## 7. Open questions / TODO

1. **`TwinAction.CREATE` добавить в enum + БД.** Это явный blocker для OnAction listener-ов на создание твинов. Просто добавить, но это всё-таки расширение публичной модели разрешений.

2. **`listener_on_action.publisher_twin_action_id` — varchar vs enum.** Сейчас varchar + FK на `twin_action` — OK для расширяемости. Альтернатива: `@Enumerated(EnumType.STRING)` в JPA — но тогда нельзя добавить кастомный action без деплоя. Решение: оставить varchar + FK.

3. **FK ON DELETE для subscriber_twin_class_field_id.** Сейчас `ON DELETE NO ACTION` (по умолчанию). При удалении Mater-поля операция упадёт, пока listener не удалён. Альтернативы: (а) `ON DELETE CASCADE` для listener-ов + `mater_stale=true` на зависимых полях; (б) `ON DELETE RESTRICT` с явным precondition-check. Решение: NO ACTION сейчас, добавить `dropListenerAndMarkStale(fieldId)` в `TwinClassFieldService.deleteField()`.

4. **Bulk-detection forced-async для sync listener-ов.** Если listener.async=false, но >50 twins → форсить async через consolidated task. Иначе deadlock risk. Подтвердить поведение.

5. **Каскад Mater→Mater через разные pointer-ы.** Сценарий: A-class Mater по SELF-pointer зависит от B, B-class Mater по PARENT-pointer зависит от C (ребёнок). При UPDATE C → recompute B → A-class Mater должен тоже пересчитаться. Это работает только если A-class listener тоже срабатывает на «child updated». Нужно ли для OnField listener-ов автоматически добавлять transitive listeners? Или только явная регистрация?

6. **Validator-rule semantics.** Сейчас — «если validator fails, listener пропускается». Альтернатива: «если validator fails, listener всё равно срабатывает, но возвращает специальный флаг для FieldTyper, чтобы тот исключил twin из расчёта». Решение зависит от семантики `FieldTyperCalcChildrenFieldV1`-аналога — нужно ли «sum where validator passes» или «sum, и listener решает, обновлять ли».

7. **TwinPointer discovery для ручной регистрации.** MVP требует ручного SQL — но пользователю надо знать UUID-ы pointer-ов. Стоит ли добавить admin-REST endpoint `/twin-pointers?classId=X` для lookup? Или документировать `SELECT id, name FROM twin_pointer WHERE twin_class_id = ...` в README?

8. **Performance baseline.** До реализации снять `pg_stat_statements` + `@Timed` на on-the-fly калькуляторах (см. v1 §15.2). Без baseline нельзя доказать ROI и выбрать SLA.

---

## 8. Источники

(Полный список из v1 сохраняет актуальность — см. `field-typer-mater-listeners.md` §19. Ключевые для этого документа: Transactional Outbox (AWS), Spring Transaction-bound Events, Hibernate discourse on ManyToMany, Tiger Data on transition tables, Cybertec on trigger recursion, Bart Slota on @TransactionalEventListener pitfalls.)
