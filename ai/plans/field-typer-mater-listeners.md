# Архитектура listener-системы для Mater-полей

**Дата:** 2026-07-06
**Задача:** Спроектировать архитектуру поддержания консистентности материализованных калькулируемых полей (`FieldTyperCalc*Mater`) через listener-систему в Twins (Spring Boot 3.5 / Java 21 / PostgreSQL)
**Тип:** Технический дизайн / Архитектурное решение (ADR)
**Статус:** Полная замена `field-typer-mater-listeners.md` (v1) — упрощённая схема метаданных + адаптированный перенос критичных тем из v1

---

## 0. TL;DR для разработчика

**Проблема.** Mater-поля (`FieldTyperCalcSumMater`, `FieldTyperCalcSubtractionMater`, ...) хранят вычисленный результат в `twin_field_decimal` через `serializeValue()`. Но `serializeValue` вызывается только если поле пришло в request payload (`TwinService.convertTwinFields:841-853` итерирует по `fields` map из запроса). Когда пользователь меняет operand-поле (например `firstFieldId`), Mater-поле остаётся со старым значением. Когда C/U/D происходит с дочкой — сумма у родителя не обновляется. Mater-поля в проде сейчас **фактически неработоспособны** — нет гарантии консистентности.

**Решение.** Две listener-таблицы + одна таблица validator-rule:

- `field_listener_on_field` — на изменение конкретного `twin_class_field` (intra-twin и cross-twin через TwinPointer)
- `field_listener_on_action` — на CREATE/EDIT/DELETE над твинами указанного класса (для `FieldTyperCalcChildrenFieldV1`-кейсов)
- `field_listener_on_action_validator_rule` — необязательные validator_set-ы для сложных предикатов (например, «учитывать ребёнка в сумме только если status=ACTIVE»)

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
| **OnField** | `field_listener_on_field` | Изменилось значение поля `publisher_twin_class_field_id` у одного или нескольких твинов. Подписчик — twin, на который указывает `subscriber_twin_pointer_id` от твина-publisher'а | `FieldTyperCalcSumMater` слушает `firstFieldId`/`secondFieldId` через pointer SELF. Или parent-class Mater слушает поле child-class через pointer PARENT |
| **OnAction** | `field_listener_on_action` + `field_listener_on_action_validator_rule` | Выполнен TwinAction (CREATE/EDIT/DELETE) над твином класса `publisher_twin_class_id`. Подписчик — twin, на который указывает `subscriber_twin_pointer_id` от publisher-а | `FieldTyperCalcChildrenFieldV1` analogue в материализованном виде: child-class CREATE/EDIT/DELETE → пересчёт суммы у parent-class Mater |
| _OnLinkChange_ (future) | _projected_ | Изменение TwinLink между твинами | Когда TwinLink «task→assignee» удаляется, пересчитать нагрузку assignee |
| _OnMarkerChange_ (future) | _projected_ | TwinMarker установлен/снят | При marker BLOCKED → исключить twin из суммы родителя |
| _OnTagChange_ (future) | _projected_ | TwinTag добавлен/удалён | При tag PRIORITY=HIGH → коэффициент умножения в parent-агрегате |

**Почему не одна таблица с enum.** Разные trigger-source-ы требуют разных схем: OnField хранит publisher_field_id, OnAction — publisher_twin_action_id + опциональный validator_set. Запихнуть в одну таблицу → sparse anti-pattern с nullable-колонками. Раздельные таблицы — схемная чистота + easy extendability (новый trigger-source = новая таблица, а не nullable-каша).

**Почему не пять таблиц сразу.** OnField и OnAction исчерпывают текущие Mater-сценарии (`FieldTyperCalcBinaryMater` для intra/cross-field, `FieldTyperCalcChildrenFieldV1` для children-CRUD). Остальные источники — это *точки расширения*, их заводить под гипотетические Mater-варианты (которых в иерархии FieldTyper сейчас нет) — premature.

### 1.5. Структура таблиц

```sql
-- ============================================================
-- field_listener_on_field: срабатывает на изменение поля publisher_twin_class_field_id
-- ============================================================
create table if not exists field_listener_on_field
(
    id                             uuid not null
        constraint field_listener_on_field_pk primary key,
    domain_id                      uuid not null
        constraint field_listener_on_action_domain_id_fk
            references domain on update cascade on delete cascade,
    subscriber_twin_pointer_id     uuid not null
        constraint field_listener_on_field_sub_twin_pointer_id_fk
            references twin_pointer on update cascade on delete cascade,
    subscriber_twin_class_field_id uuid not null
        constraint field_listener_on_field_sub_twin_class_field_id_fk
            references twin_class_field on update cascade  on delete cascade,
    publisher_twin_class_field_id  uuid not null
        constraint field_listener_on_field_pub_twin_class_field_id_fk
            references twin_class_field on update cascade on delete cascade,
    async                          boolean not null default false
);

create index if not exists field_listener_on_field_twin_class_field_id_index
    on field_listener_on_field (subscriber_twin_class_field_id);
create index if not exists field_listener_on_field_pub_twin_class_field_id_idx
    on field_listener_on_field (publisher_twin_class_field_id);
create index if not exists field_listener_on_field_sub_twin_pointer_id_idx
    on field_listener_on_field (subscriber_twin_pointer_id);

-- ============================================================
-- field_listener_on_action: срабатывает на TwinAction над твином класса publisher_twin_class_id
-- ============================================================
create table if not exists field_listener_on_action
(
    id                             uuid not null
        constraint field_listener_on_action_pk primary key,
    domain_id                      uuid not null
        constraint field_listener_on_action_domain_id_fk
            references domain on update cascade on delete cascade,
    subscriber_twin_pointer_id     uuid not null
        constraint field_listener_on_action_sub_twin_pointer_id_fk
            references twin_pointer on update cascade on delete cascade,
    subscriber_twin_class_field_id uuid not null
        constraint field_listener_on_action_sub_twin_class_field_id_fk
            references twin_class_field on update cascade  on delete cascade,
    publisher_twin_class_id        uuid not null
        constraint field_listener_on_action_pub_twin_class_id_fk
            references twin_class on update cascade  on delete cascade,
    publisher_twin_action_id       varchar not null
        constraint field_listener_on_action_pub_twin_action_id_fk
            references twin_action on update cascade on delete cascade,
    async                          boolean not null default false
);

create index if not exists field_listener_on_action_sub_twin_class_field_idx
    on field_listener_on_action (subscriber_twin_class_field_id);
create index if not exists field_listener_on_action_pub_class_action_idx
    on field_listener_on_action (publisher_twin_class_id, publisher_twin_action_id);
create index if not exists field_listener_on_action_sub_twin_pointer_idx
    on field_listener_on_action (subscriber_twin_pointer_id);

-- ============================================================
-- field_listener_on_action_validator_rule: необязательные validator_set-ы,
-- которые проверяются ДО Mater recompute при срабатывании field_listener_on_action.
-- Например: «учитывать ребёнка только если status=ACTIVE».
-- Паттерн переиспользует существующую twin_action_validator_rule (см. TwinActionValidatorRuleEntity).
-- ============================================================
create table if not exists field_listener_on_action_validator_rule
(
    id                              uuid                 not null
        constraint field_listener_on_action_validator_rule_pk primary key,
    field_listener_on_action_id           uuid                 not null
        constraint field_listener_on_action_id_fk
            references field_listener_on_action on update cascade on delete cascade,
    "order"                         integer default 1,
    active                          boolean default true not null,
    twin_validator_set_id           uuid
        constraint field_listener_on_action_id_twin_validator_set_id_fk
            references twin_validator_set on update cascade on delete cascade
);

create unique index if not exists field_listener_on_action_validator_rule_order_uniq
    on field_listener_on_action_validator_rule (field_listener_on_action_id, "order");
create index if not exists field_listener_on_action_validator_rule_twin_validator_set_idx
    on field_listener_on_action_validator_rule (twin_validator_set_id);
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

`TwinPointerEntity` (`core/.../dao/twin/TwinPointerEntity.java`) — метаданные на уровне `twin_class`: связывает `twin_class_id`, `pointer_featurer_id` (FK на `featurer` в cambium) и `pointer_params` (HStore). Базовый класс всех pointer-featurer-ов — `Pointer` (`core/.../featurer/pointer/Pointer.java`) с контрактом:

```java
public TwinEntity point(HashMap<String, String> linkerParams, TwinEntity srcTwinEntity) throws ServiceException
```

**Важно про контракт.** `srcTwinEntity` — это publisher (твин, чьё изменение запустило listener). Возвращаемое значение — **ровно один** subscriber-twin. Если pointer находит несколько кандидатов → `ServiceException(POINTER_NON_SINGLE)`. Если ни одного → `null` (listener пропускается молча, с metric-tag `no_subscriber`). Никаких коллекций — если нужно «подписчики = все assigner-ы», то это либо N listener-ов с разными pointer-ами, либо отдельный pointer-featurer с другими семантиками.

**Реализованные pointer-featurer-ы** (все живут в `core/.../featurer/pointer/`):

| Featurer ID | Класс | Name | Что возвращает от publisher-а | params |
|-------------|-------|------|--------------------------------|--------|
| `ID_3101` | `PointerOnSelf` | Self pointed | Того же twin-а (publisher = subscriber). Для intra-twin Mater | — |
| `ID_3102` | `PointerOnHead` | Head twin pointed | **Непосредственного родителя** (через `twinService.loadHead` → `TwinEntity.headTwin` по FK `head_twin_id`). Это НЕ корень иерархии — корень доступен через `hierarchy_tree` (ltree) отдельно. Если `head_twin_id = null` (twin верхнего уровня) → `null` | — |
| `ID_3103` | `PointerOnLinkedTwin` | Some linked twin pointed (by link id) | dst TwinLink-и по конкретному `linkId`. Если несколько forward-links одного типа → `POINTER_NON_SINGLE` | `linkId: UUID` |
| `ID_3104` | `PointerOnSingleChild` | Direct single child | Единственного ребёнка класса `childTwinClassId` (поиск через `headTwinId=publisher`). >1 ребёнок → `POINTER_NON_SINGLE` | `childTwinClassId: UUID` |
| `ID_3105` | `PointerOnSingleGrandChild` | Direct single grandchild | Единственного внука класса `grandChildTwinClassId` (поиск через `hierarchyTreeContainsId=publisher`). >1 → `POINTER_NON_SINGLE` | `grandChildTwinClassId: UUID` |
| `ID_3106` | `PointerOnGivenTwin` | Given twin pointed | Захардкоженный twin по `twinId`. Ad-hoc сценарии, не для массовой регистрации | `twinId: UUID` |

**Семантика направления для listener-системы.** `srcTwinEntity = publisher` (твин, чьё изменение запустило listener), return = subscriber (твин, чьё Mater-поле надо пересчитать). То есть pointer описывает отношение **publisher → subscriber**. Примеры:

| Сценарий                                                                                    | Кто publisher | Кто subscriber | Какой pointer |
|---------------------------------------------------------------------------------------------|---------------|----------------|---------------|
| Intra-twin Mater: A.field обновился → пересчитать B.field того же твина                     | twin X | twin X | `PointerOnSelf` |
| `FieldTyperCalcChildrenFieldV1`-кейс: сумма детей на parent-классе, обновилось поле ребёнка | Child | Parent | `PointerOnHead` |
| Mater на parent слушает единственного ребёнка (если ребёнок один)                           | Parent | Child | `PointerOnSingleChild(childTwinClassId=Child)` |
| Mater на grandparent слушает поле внука                                                     | Grandchild | Grandparent | `PointerOnHead` дважды (транзит через parent) — или отдельный pointer при появлении |
| lTwinLink «Task→Assignee»: listener на Task → пересчёт Assignee-поля                        | Task | Assignee | `PointerOnLinkedTwin(linkId=ASSIGNED_TO)` |
| Mater на корне иерархии слушает любое изменение в поддереве                                 | descendant | root | через `hierarchy_tree` lookup — пока без отдельного pointer-featurer, см. §7 |
| Mater на конкретном twin-адресе (один глобальный listener)                                  | любой | заданный twin | `PointerOnGivenTwin(twinId=...)` |

**Runtime-флоу при срабатывании listener-а:**

1. Грузим `TwinPointerEntity` по `subscriber_twin_pointer_id` (TwinPointerService)
2. `Pointer.point(pointer, srcTwinEntity=publisherTwin)`:
   - Сначала проверяет `srcTwinEntity.pointers.get(pointer.id)` — если уже резолвили в этой tx, возвращает кэшированный результат (включая null — важно не звать pointer повторно)
   - Иначе выполняет реальную работу (SQL/TwinLink lookup/etc.), кладёт результат в `srcTwinEntity.pointers` и возвращает
3. Если результат null → listener пропускается, метрика `mater_listener_no_subscriber_total`
4. Если не null → пересчитываем `subscriber_twin_class_field_id` у найденного подписчика

**Кэш pointer-результатов** живёт на `TwinEntity` как `@Transient Map<UUID, TwinEntity> pointers`. Это даёт несколько важных свойств (см. §4.3 для деталей): автоматическая инвалидация по tx, один SQL на publisher-а на pointer в рамках tx, защита от повторных дорогих lookups когда несколько listener-ов используют тот же pointer от одного publisher-а.

### 2.2. Точка вызова в TwinService

Вызовы listeners — в `TwinService.createTwin` / `TwinService.updateTwin` **между** `validateAndCollect` и `applyChanges`:

```java
// TwinService.java — упрощённо
public void createTwin(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
    createTwins(TwinCreateStage.of(twinCreate), twinChangesCollector);   // существующая логика
    // === НОВЫЙ HOOK ===
    fieldListenerService.triggerAffected(twinChangesCollector);   // добавляет Mater recompute команды в тот же collector
    // === КОНЕЦ HOOK ===
}

public void updateTwin(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector, ...) throws ServiceException {
    // ... существующая логика ...
    fieldListenerService.triggerAffected(twinChangesCollector);
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
INSERT INTO field_listener_on_field (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
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
INSERT INTO field_listener_on_field (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
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

### 3.2. Cross-twin Mater: parent подписан на поле ребёнка (FieldTyperCalcChildrenFieldV1-analogue)

**Сценарий:** `Project` имеет Mater-поле `totalTaskEstimate` = SUM `estimate` всех `Task`-детей. При UPDATE `estimate` у Task должен пересчитаться `Project.totalTaskEstimate`. Publisher = Task (ребёнок), subscriber = Project (родитель). Pointer = `PointerOnHead` (ID_3102), настроенный на классе `Task` — он возвращает непосредственного родителя через FK `head_twin_id`.

```sql
-- Шаг 1. Завести TwinPointer на классе Task, указывающий на родителя (PointerOnHead = featurer ID_3102)
INSERT INTO twin_pointer (id, twin_class_id, pointer_featurer_id, name)
VALUES (
    '<task_head_pointer_uuid>',
    '<Task_twin_class_id>',
    3102,                                            -- PointerOnHead
    'PARENT'
)
ON CONFLICT DO NOTHING;

-- Шаг 2. Зарегистрировать listener: при изменении Task.estimate → пересчёт Project.totalTaskEstimate
INSERT INTO field_listener_on_field (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
                               publisher_twin_class_field_id, async, domain_id)
VALUES (
    gen_random_uuid(),
    '<task_head_pointer_uuid>',                     -- PointerOnHead на классе Task
    '<twin_class_field_id для Project.totalTaskEstimate>',
    '<twin_class_field_id для Task.estimate>',
    false,                                           -- sync для single child update; рассмотреть true для deep cascade
    '<domain_id>'
);
```

### 3.3. OnAction listener (CRUD над твином-ребёнком)

**Сценарий:** `Project.budgetUsed` = SUM всех `Task.cost` где Task.status IN (IN_PROGRESS, DONE). При CREATE новой Task — пересчитать `budgetUsed` с учётом новой задачи (если она подходит по статусу). При DELETE Task — вычесть. Publisher = Task, subscriber = Project через тот же `PointerOnHead` (см. §3.2 шаг 1).

```sql
INSERT INTO field_listener_on_action (id, subscriber_twin_pointer_id, subscriber_twin_class_field_id,
                                publisher_twin_class_id, publisher_twin_action_id, async, domain_id)
VALUES (
    gen_random_uuid(),
    '<task_head_pointer_uuid>',                     -- PointerOnHead на классе Task
    '<twin_class_field_id для Project.budgetUsed>',
    '<Task_twin_class_id>',
    'CREATE',                                        -- TwinAction.CREATE
    true,                                            -- async (CRUD может быть bulk)
    '<domain_id>'
);

-- То же для EDIT и DELETE
INSERT INTO field_listener_on_action (...) VALUES (..., 'EDIT',  true, ...);
INSERT INTO field_listener_on_action (...) VALUES (..., 'DELETE', true, ...);
```

### 3.4. Validator rule для сложных предикатов

**Сценарий:** Аналогично §3.3, но переcчёт `budgetUsed` только если `Task.status IN (IN_PROGRESS, DONE)` (т.е. DRAFT не учитывается). Используем существующий `twin_validator_set` с предикатом `status IN (...)`.

```sql
-- Предполагаем, что validator_set уже создан (например, 'Task in active status')
INSERT INTO field_listener_on_action_validator_rule (id, field_listener_on_action_id, "order", active, twin_validator_set_id)
VALUES (
    gen_random_uuid(),
    '<field_listener_on_action_id для CREATE из §3.3>',
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
   - Строим граф: `subscriber_twin_class_field_id → publisher_twin_class_field_id` (по `field_listener_on_field`)
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

### 4.3. Кэширование: два уровня

Hot path listener-системы делает несколько lookups, и каждый должен быть кэширован. Используем **два независимых уровня** с разной семантикой.

#### 4.3.1. Pointer-result cache — на `TwinEntity` через `@Transient Map`

Результат `Pointer.point(pointer, publisherTwin)` для конкретного publisher-а в рамках tx. Зачем нужен: в одном `triggerAffected` от одного publisher-а может срабатывать несколько listener-ов с одним и тем же `subscriber_twin_pointer_id` (например, parent-class Mater слушает одновременно `fieldA` и `fieldB` ребёнка — оба listener-а резолвят одного и того же parent-а через тот же pointer). Без кэша — двойной SQL/lookup.

**Реализация:**

```java
// TwinEntity.java
@Transient
@EqualsAndHashCode.Exclude
@ToString.Exclude
private Map<UUID, Optional<TwinEntity>> pointers;

public TwinEntity getPointer(UUID pointerId) {
    return pointers == null ? null : pointers.getOrDefault(pointerId, Optional.empty()).orElse(null);
}

/** true если pointer уже резолвили (включая null-результат) — чтобы не звать point() повторно. */
public boolean hasPointer(UUID pointerId) {
    return pointers != null && pointers.containsKey(pointerId);
}

public void addPointer(UUID pointerId, TwinEntity pointedTwin) {
    if (pointers == null) pointers = new HashMap<>();
    pointers.put(pointerId, Optional.ofNullable(pointedTwin));
}
```

`Optional<TwinEntity>` as value — чтобы отличить «ещё не вычислено» (key отсутствует) от «вычислено, результат null» (key присутствует с empty Optional). Это критично для pointer-ов типа `PointerOnHead` у корневого twin-а: первый вызов вернёт null, и без sentinel мы бы делали реальную работу на каждом последующем listener-е.

**Заполнение — в `Pointer` (базовый класс), два уровня API:**

```java
// Pointer.java
public abstract class Pointer extends FeaturerTwins {

    /**
     * Single-twin API. Читает кэш на srcTwinEntity; при cache miss дергает load([srcTwinEntity]).
     * Сохраняет семантику ровно одного subscriber-а — если load нашёл несколько, бросает POINTER_NON_SINGLE.
     */
    public TwinEntity point(TwinPointerEntity pointer, TwinEntity srcTwinEntity) throws ServiceException {
        if (!srcTwinEntity.hasPointer(pointer.getId())) {
            load(pointer, List.of(srcTwinEntity));
        }
        return srcTwinEntity.getPointer(pointer.getId());
    }

    /**
     * Batch API. Главный инструмент устранения N+1: для N publisher-ов с одним pointer-ом
     * делает ОДИН запрос/lookup, не N. Сам занимается кэшем:
     *   1. Фильтрует srcTwins, оставляя только cache-miss (hasPointer == false)
     *   2. Если miss-ов 0 — сразу return того, что уже в кэше
     *   3. Иначе вызывает abstract load(properties, missTwins) — реализация в subclass
     *   4. Кэширует результат (включая null) на каждом src twin-е
     */
    public void load(TwinPointerEntity pointer, Collection<TwinEntity> srcTwins) throws ServiceException {
        if (srcTwins.isEmpty()) return Map.of();
        UUID pointerId = pointer.getId();

        List<TwinEntity> misses = new ArrayList<>();
        for (TwinEntity src : srcTwins) {
            if (src.hasPointer(pointerId)) {
                TwinEntity cached = src.getPointer(pointerId);
                if (cached != null) result.put(src.getId(), cached);
            } else {
                misses.add(src);
            }
        }
        if (misses.isEmpty()) return result;

        Properties properties = featurerService.extractProperties(this, pointer.getPointerParams());
        Map<UUID, TwinEntity> loaded = load(properties, misses);  // subclass impl: один batch SQL/lookup

        for (TwinEntity src : misses) {
            TwinEntity target = loaded.get(src.getId());  // null — валидный результат
            src.addPointer(pointerId, target);
        }
    }

    /**
     * Subclass реализует. Должен быть batch — один SQL/lookup для всей коллекции,
     * не N. Возвращает map {srcTwinId -> subscriber}, где отсутствие ключа = null subscriber
     * (subscriber не найден). POINTER_NON_SINGLE — exception если для какого-то src
     * pointer резолвится в несколько кандидатов.
     */
    protected abstract Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException;
}
```

**Примеры `load` в subclass-ах (один SQL на batch):**

```java
// PointerOnSelf
@Override
protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) {
    return srcTwins.stream().collect(Collectors.toMap(TwinEntity::getId, Function.identity()));
}

// PointerOnHead — один SELECT ... WHERE id IN (:srcIds) JOIN для head
@Override
protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
    twinService.loadHead(srcTwins);
    return srcTwins.stream().map(TwinEntity::getId, TwinEntity::getHeadTwin).toMap();
}

// PointerOnLinkedTwin — один SELECT twinlink WHERE src_id IN (:srcIds) AND link_id = :linkId
@Override
protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
    UUID linkIdValue = linkId.extract(properties);
    return twinLinkService.loadSingleDstTwinsBySrcIdsAndLinkId(
            srcTwins.stream().map(TwinEntity::getId).toList(), linkIdValue);
    // внутри: SELECT tl.src_id, tl.dst_id FROM twin_link tl WHERE tl.src_id IN (?) AND tl.link_id = ?
    //         + POINTER_NON_SINGLE если для src_id > 1 строка
}

// PointerOnSingleChild — один SELECT twin WHERE head_twin_id IN (:srcIds) AND twin_class_id = :classId
@Override
protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
    UUID childClassId = twinClassId.extract(properties);
    return twinService.loadSingleChildrenByHeadIdsAndClass(
            srcTwins.stream().map(TwinEntity::getId).toList(), childClassId);
    // внутри: GROUP BY head_twin_id HAVING count = 1; если count > 1 для какого-то head → POINTER_NON_SINGLE
}
```

**API меняется:** `point` теперь принимает `TwinPointerEntity` (а не `HashMap<String,String>`), чтобы знать `pointerId` как cache-key. Старый `point(linkerParams, srcTwinEntity)` — делегирует в новый с wrapping-ом (для обратной совместимости external callers), но без кэш-семантики (caller должен сам передать TwinPointerEntity если хочет кэш).

**Инвалидация:** автоматически — `TwinEntity.resetTransientState()` (строка ~677 в `TwinEntity.java`) уже чистит `headTwin`, `twinFieldCalculated` и подобные @Transient-поля; туда же добавить `pointers = null`. Это вызывается на entity detach / clear persistence context — то есть кэш умирает вместе с tx. Никакого TTL, никакой явной инвалидации.

**Batch-resolve в FieldListenerService.** `collectOnFieldGroups` / `collectOnActionGroups` (см. §5.2.2) собирают все `(pointerId, [publisherTwinIds])` пары, группируют по `pointerId`, и для каждой группы вызывают `pointer.load(pointer, twinCollection)` — один SQL на группу, не N. Это устраняет N+1 в bulk scenarios (100 publisher-ов с одним pointer-ом → 1 SQL, не 100).

#### 4.3.2. Listener-config + pointer metadata — Spring `@Cacheable` (Caffeine backend)

Сюда идёт то, что **между tx** и **между publisher-ами**: metadata-таблицы, которые меняются редко (только при config change). Кэшированные методы живут **на secure-find сервисах** (`FieldListenerOnFieldService`, `FieldListenerOnActionService`, `TwinPointerService`) через Spring `@Cacheable` — backing implementation Caffeine. Никаких отдельных «cache-классов» — кэш рядом с сервисом-владельцем entity (SRP), имена cache как константы на сервисе для `@CacheEvict`.

| Cache name (константа на сервисе) | На методе | Key (SpEL) | TTL | Evict |
|-------|-----|-------|-----|------|
| `FieldListenerOnField.byPublisherField` | `FieldListenerOnFieldService.findOnFieldListeners(Set<UUID>)` | `CollectionUtils.generateUniqueKey(#publisherFieldIds)` | 5 min | `@CacheEvict(allEntries=true)` на `save()` / `delete()` того же сервиса |
| `FieldListenerOnAction.byPublisherClassAction` | `FieldListenerOnActionService.findOnActionListeners(UUID, TwinAction)` | `CollectionUtils.generateUniqueKey(#twinClassId, #action)` | 5 min | `@CacheEvict(allEntries=true)` на `save()` / `delete()` |
| `TwinPointer.byId` (на `TwinPointerService`) | `findById(UUID)` | `#pointerId` (default) | 30 min | По `TwinPointer` save/delete |

Конфигурация TTL — в `application-*.properties` (через `spring.cache.cache-names` + Caffeine spec per cache).

**Почему не @Transient для listener-config:** listener-config — это глобальная metadata, не привязанная к конкретному publisher-twin. Её читают для всех publisher-ов; `@Cacheable` на secure-find сервисе — естественное место.

**Почему не `@Cacheable` для pointer-result:** результат `point()` привязан к конкретному publisher-twin в конкретный момент; `@Cacheable` потребовал бы ключ `(pointerId, publisherTwinId, publisherTxVersion)` — слишком сложный и risk stale data. @Transient-поле на publisher-entity (см. §4.3.1) — ровно правильный scope.

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

Все три entity пишутся по `docs/entity_code_convention.md`: `@Getter @Setter` (НЕ `@Data`), `@Accessors(chain = true)`, `@FieldNameConstants`, `@PrePersist` для авто-генерации UUID, `implements EasyLoggable, Identifiable`. Для каждого FK — тройка: raw UUID id + `SpecOnly` `@ManyToOne(LAZY)` для Specifications + `@Transient` runtime поле для bulk-loaded entity. SpecOnly-поля имеют `@Getter(AccessLevel.NONE)` чтобы компилятор запрещал случайную навигацию.

```java
package org.twins.core.dao.fieldlistener;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "field_listener_on_field")
@FieldNameConstants
public class FieldListenerOnFieldEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "subscriber_twin_pointer_id", nullable = false)
    private UUID subscriberTwinPointerId;

    @Column(name = "subscriber_twin_class_field_id", nullable = false)
    private UUID subscriberTwinClassFieldId;

    @Column(name = "publisher_twin_class_field_id", nullable = false)
    private UUID publisherTwinClassFieldId;

    @Column(name = "async", nullable = false)
    private boolean async;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    // --- subscriberTwinPointer: SpecOnly + runtime ---
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_twin_pointer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity subscriberTwinPointerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity subscriberTwinPointer;

    // --- subscriberTwinClassField: SpecOnly + runtime ---
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity subscriberTwinClassFieldSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity subscriberTwinClassField;

    // --- publisherTwinClassField: SpecOnly + runtime ---
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity publisherTwinClassFieldSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity publisherTwinClassField;

    // --- domain: SpecOnly only (runtime не нужен — домен извлекается из ApiUser) ---
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private org.twins.core.dao.domain.DomainEntity domainSpecOnly;

    @Override
    public String logShort() {
        return "fieldListenerOnField[" + id + "]";
    }

    @Override
    public String logNormal() {
        return "fieldListenerOnField[id:" + id + ", subscriberField:" + subscriberTwinClassFieldId
                + ", publisherField:" + publisherTwinClassFieldId + "]";
    }

    @Override
    public UUID getId() {
        return id;
    }
}
```

```java
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "field_listener_on_action")
@FieldNameConstants
public class FieldListenerOnActionEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "subscriber_twin_pointer_id", nullable = false)
    private UUID subscriberTwinPointerId;

    @Column(name = "subscriber_twin_class_field_id", nullable = false)
    private UUID subscriberTwinClassFieldId;

    @Column(name = "publisher_twin_class_id", nullable = false)
    private UUID publisherTwinClassId;

    @Column(name = "publisher_twin_action_id", nullable = false)
    @Enumerated(EnumType.STRING)
    private TwinAction publisherTwinAction;

    @Column(name = "async", nullable = false)
    private boolean async;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    // SpecOnly + runtime для subscriber_twin_pointer / subscriber_twin_class_field / publisher_twin_class / domain
    // — аналогично FieldListenerOnFieldEntity выше (опущено для краткости, см. полную реализацию)

    @Deprecated @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_twin_pointer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinPointerEntity subscriberTwinPointerSpecOnly;
    @Transient @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinPointerEntity subscriberTwinPointer;

    @Deprecated @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinClassFieldEntity subscriberTwinClassFieldSpecOnly;
    @Transient @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinClassFieldEntity subscriberTwinClassField;

    @Deprecated @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_twin_class_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinClassEntity publisherTwinClassSpecOnly;
    @Transient @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinClassEntity publisherTwinClass;

    @Deprecated @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private org.twins.core.dao.domain.DomainEntity domainSpecOnly;

    @Override public String logShort() { return "fieldListenerOnAction[" + id + "]"; }
    @Override public String logNormal() {
        return "fieldListenerOnAction[id:" + id + ", publisherClass:" + publisherTwinClassId
                + ", action:" + publisherTwinAction + "]";
    }
    @Override public UUID getId() { return id; }
}
```

```java
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "field_listener_on_action_validator_rule")
@FieldNameConstants
public class FieldListenerOnActionValidatorRuleEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "field_listener_on_action_id", nullable = false)
    private UUID fieldListenerOnActionId;

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Deprecated @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_listener_on_action_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private FieldListenerOnActionEntity fieldListenerOnActionSpecOnly;
    @Transient @EqualsAndHashCode.Exclude @ToString.Exclude
    private FieldListenerOnActionEntity fieldListenerOnAction;

    @Deprecated @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinValidatorSetEntity twinValidatorSetSpecOnly;
    @Transient @EqualsAndHashCode.Exclude @ToString.Exclude
    private TwinValidatorSetEntity twinValidatorSet;

    @Override public String logShort() { return "validatorRule[" + id + "]"; }
    @Override public String logNormal() {
        return "validatorRule[id:" + id + ", listener:" + fieldListenerOnActionId + ", order:" + order + "]";
    }
    @Override public UUID getId() { return id; }
}
```

**Bulk-loading runtime-полей** (по `load_method_pattern.md`, см. `core/.../service/loader/`): `FieldListenerOnFieldService`/`FieldListenerOnActionService` предоставляют методы `loadSubscriberTwinPointer(Collection<...Entity>)`, `loadSubscriberTwinClassField(...)`, и т.п., которые одним batch-SQL заполняют `@Transient` runtime-поля для всей коллекции. Это устраняет N+1 при чтении listener-конфигов в mappers/admin UI. FieldListenerService (orchestrator) использует только raw UUID id-ы и сам контролирует batch-loading — ему runtime-entity не нужны, кроме `subscriberTwinClassField` (для извлечения `fieldTyperFeaturerId`) и `subscriberTwinPointer` (для извлечения `pointerFeaturerId` + `pointerParams`); эти два грузятся в `batchResolveSubscribers` одним batch-ем.

### 5.2. Сервис trigger-а

#### 5.2.1. Репозитории и secure-find сервисы (обязательно для каждой entity)

Каждая из трёх entity получает свой репозиторий (Spring Data `CrudRepository` + `JpaSpecificationExecutor`) и сервис — наследник `TwinsEntitySecureFindService<T>` (см. `core/.../service/TwinsEntitySecureFindService.java`). По конвенции проекта (CLAUDE.md «Services»): override `entityRepository()`, `entityGetIdFunction()`, `isEntityReadDenied()`, `validateEntity()`; write-методы под `@Transactional(rollbackFor = Throwable.class)`. `TwinsEntitySecureFindService` добавляет domain-aware `findByKey` через `AuthService.getApiUser().getDomainId()` — это автоматически даёт multi-tenant isolation для административных REST-эндпоинтов.

```java
package org.twins.core.dao.fieldlistener;

public interface FieldListenerOnFieldRepository
        extends CrudRepository<FieldListenerOnFieldEntity, UUID>,
                JpaSpecificationExecutor<FieldListenerOnFieldEntity> {
    List<FieldListenerOnFieldEntity> findByPublisherTwinClassFieldIdIn(Collection<UUID> ids);
    List<FieldListenerOnFieldEntity> findByDomainId(UUID domainId);
    void deleteBySubscriberTwinClassFieldId(UUID subscriberFieldId);
}

public interface FieldListenerOnActionRepository
        extends CrudRepository<FieldListenerOnActionEntity, UUID>,
                JpaSpecificationExecutor<FieldListenerOnActionEntity> {
    List<FieldListenerOnActionEntity> findByPublisherTwinClassIdAndPublisherTwinAction(
            UUID twinClassId, TwinAction action);
    List<FieldListenerOnActionEntity> findByDomainId(UUID domainId);
}

public interface FieldListenerOnActionValidatorRuleRepository
        extends CrudRepository<FieldListenerOnActionValidatorRuleEntity, UUID>,
                JpaSpecificationExecutor<FieldListenerOnActionValidatorRuleEntity> {
    List<FieldListenerOnActionValidatorRuleEntity>
            findByFieldListenerOnActionIdOrderByOrder(UUID fieldListenerOnActionId);
}
```

```java
package org.twins.core.service.fieldlistener;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.cambium.common.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class FieldListenerOnFieldService
        extends TwinsEntitySecureFindService<FieldListenerOnFieldEntity> {

    public static final String CACHE_BY_PUBLISHER_FIELD = "FieldListenerOnField.byPublisherField";
    public static final String CACHE_BY_DOMAIN          = "FieldListenerOnField.byDomain";

    private final FieldListenerOnFieldRepository repository;

    @Override public CrudRepository<FieldListenerOnFieldEntity, UUID> entityRepository() { return repository; }
    @Override public Function<FieldListenerOnFieldEntity, UUID> entityGetIdFunction() { return FieldListenerOnFieldEntity::getId; }
    @Override public boolean isEntityReadDenied(FieldListenerOnFieldEntity e) { return false; }
    @Override public void validateEntity(FieldListenerOnFieldEntity e) { /* checks */ }

    /** Hot path: FieldListenerService.triggerAffected вызывает на каждый изменённый publisher-field. */
    @Cacheable(value = CACHE_BY_PUBLISHER_FIELD,
               key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#publisherFieldIds)")
    public List<FieldListenerOnFieldEntity> findOnFieldListeners(Set<UUID> publisherFieldIds) {
        return repository.findByPublisherTwinClassFieldIdIn(publisherFieldIds);
    }

    /** Инвалидация при config change (create/update/delete listener). Делегируется в CRUD-методы сервиса. */
    @CacheEvict(value = {CACHE_BY_PUBLISHER_FIELD, CACHE_BY_DOMAIN}, allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    public FieldListenerOnFieldEntity save(FieldListenerOnFieldEntity e) throws ServiceException {
        return super.save(e);
    }

    @CacheEvict(value = {CACHE_BY_PUBLISHER_FIELD, CACHE_BY_DOMAIN}, allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    public void delete(UUID id) throws ServiceException { super.delete(id); }
}

@Service
@RequiredArgsConstructor
public class FieldListenerOnActionService
        extends TwinsEntitySecureFindService<FieldListenerOnActionEntity> {

    public static final String CACHE_BY_PUBLISHER_CLASS_ACTION = "FieldListenerOnAction.byPublisherClassAction";

    private final FieldListenerOnActionRepository repository;
    /* overrides аналогично */

    @Cacheable(value = CACHE_BY_PUBLISHER_CLASS_ACTION,
               key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassId, #action)")
    public List<FieldListenerOnActionEntity> findOnActionListeners(UUID twinClassId, TwinAction action) {
        return repository.findByPublisherTwinClassIdAndPublisherTwinAction(twinClassId, action);
    }

    @CacheEvict(value = CACHE_BY_PUBLISHER_CLASS_ACTION, allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    public FieldListenerOnActionEntity save(FieldListenerOnActionEntity e) throws ServiceException {
        return super.save(e);
    }

    @CacheEvict(value = CACHE_BY_PUBLISHER_CLASS_ACTION, allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    public void delete(UUID id) throws ServiceException { super.delete(id); }
}

@Service
@RequiredArgsConstructor
public class FieldListenerOnActionValidatorRuleService
        extends TwinsEntitySecureFindService<FieldListenerOnActionValidatorRuleEntity> {
    private final FieldListenerOnActionValidatorRuleRepository repository;
    /* overrides + save/delete с @CacheEvict для валидатор-рулесов, если они станут hot */
}
```

Эти три сервиса отвечают за административный CRUD (будущий UI) **и** предоставляют кэшированные lookup-методы для hot path. Имена cache (константы `CACHE_*`) используются в `@CacheEvict` для инвалидации при config change. Backing implementation — Caffeine (через Spring Cache abstraction), конфигурация TTL в `application-*.properties` для каждого cache-имени. Это устраняет отдельный класс `CaffeineListenerCache` — кэш живёт рядом с сервисом, который владеет entity (SRP).

Сам trigger/recompute — отдельный сервис `FieldListenerService` ниже, он работает поверх `FieldListenerOnFieldService` / `FieldListenerOnActionService` (не поверх репозиториев напрямую) и не наследует `TwinsEntitySecureFindService`, т.к. не является CRUD-сервисом entity.

#### 5.2.2. Оркестратор trigger-а и recompute

**Жизненный цикл события (sync flow):**

1. `createTwin` / `updateTwin` заполнили `TwinChangesCollector` изменениями publisher-ов
2. `FieldListenerService.triggerAffected(collector)`:
   - Извлекает `AffectedSnapshot` (какие поля у каких твинов изменились, какие actions выполнялись)
   - Bulk detection: если >50 twins → consolidated async task, return
   - Lookup listeners (через Caffeine cache) — отдельно для OnField и OnAction
   - Для каждого сработавшего listener-а:
     - `pointer.point(publisher)` → subscriber twin (0 или 1, см. §2.1)
     - Validator check (для OnAction, опционально)
     - **Группировка**: кладёт `PublisherRef` (sealed: `PublisherByField` или `PublisherByAction`) в `Group` с уже загруженными Entity — `Map<SubscriberKey, Group>`, где `SubscriberKey = (subscriberTwinId, subscriberFieldId)` (UUID-pair для HashMap key; TwinEntity нельзя в key из-за JPA hashCode). Внутри Group — `subscriberTwin: TwinEntity`, `subscriberField: TwinClassFieldEntity`, `publishers: List<PublisherRef>`
3. После сбора всех групп — для каждой группы один вызов `subscriber.notify(event, collector)`:
   - `FieldListenerService` находит `FieldTyper` для `group.subscriberField()` (по `fieldTyperFeaturerId`), кастует к `FieldListenerSubscriber`
   - FieldTyper внутри `notify` сам решает: full recompute через `serializeValue` (MVP) / delta-increment (future) / skip
   - Event содержит уже загруженные Entity — никаких `findById` внутри `notify`
4. Каскад: FieldTyper через `serializeValue` положил изменение subscriber-поля в тот же collector → FieldListenerService recurses (visited-set + max-depth cap)

**Ключевой момент про publisher-state в sync flow.** На момент вызова `notify` publishers **уже изменены в collector, но ещё не в БД** (publisher-tx ещё не закоммичена). Это значит:
- `serializeValue` работает — он читает pending values через `twinClassFieldService.getDecimalValue()`, который ходит в `TwinChangesCollector` поверх БД
- SQL aggregate на БД (`SELECT SUM(value) FROM twin_field WHERE twin_id IN (publishers)`) **НЕ работает** — данных там ещё нет
- Delta-increment требует либо (а) чтения pending oldValue из collector, либо (б) откладывания delta до async worker-а (после commit). См. §7 open questions

```java
package org.twins.core.service.fieldlistener;

@Service
@RequiredArgsConstructor
@Slf4j
public class FieldListenerService {

    // Lookup-методы с @Cacheable живут на этих сервисах — FieldListenerService дёргает их напрямую.
    private final FieldListenerOnFieldService listenerOnFieldService;
    private final FieldListenerOnActionService listenerOnActionService;
    private final FieldListenerOnActionValidatorRuleService validatorRuleService;
    private final PointerFeaturerService pointerFeaturerService;
    private final TwinPointerService twinPointerService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinService twinService;
    private final FeaturerService featurerService;
    private final TwinChangeTaskService twinChangeTaskService;
    private final ValidatorService validatorService;
    private final MeterRegistry meterRegistry;

    @Value("${twins.mater.max-depth:5}")          private int maxDepth;
    @Value("${twins.mater.bulk-threshold:50}")    private int bulkThreshold;

    /** Главный hook — вызывается из TwinService.createTwin / updateTwin между validateAndCollect и applyChanges. */
    public void triggerAffected(TwinChangesCollector collector) throws ServiceException {
        // Каскадная рекурсия использует тот же visited-set по (subscriber twin, field),
        // чтобы не запускать notify дважды для одной пары и защититься от циклов.
        triggerAffected(collector, new HashSet<>(), 0);
    }

    private void triggerAffected(TwinChangesCollector collector, Set<SubscriberKey> visited, int depth) throws ServiceException {
        if (depth > maxDepth) {
            log.warn("FieldListener cascade depth {} exceeded max {}, skipping remaining", depth, maxDepth);
            return;
        }
        AffectedSnapshot snapshot = extractAffected(collector);
        if (snapshot.isEmpty()) return;

        if (snapshot.touchedTwinCount() > bulkThreshold) {
            scheduleConsolidatedBulkTask(snapshot, collector);
            return;
        }

        // Группировка publisher-ов по (subscriberTwin, subscriberField) — duplicates merged.
        // Если у двух listener-ов одинаковый SubscriberKey (например, Mater = A + B, A и B оба изменились,
        // оба listener-а резолвятся в один и тот же (subscriber twin, subscriber field)) —
        // в groups попадёт ОДНА запись с двумя PublisherRef, notify вызовется один раз.
        Map<SubscriberKey, Group> groups = new LinkedHashMap<>();
        collectOnFieldGroups(snapshot, groups, visited);
        collectOnActionGroups(snapshot, groups, visited);

        for (Group group : groups.values()) {
            if (!visited.add(group.key())) continue;  // cycle protection
            dispatchNotify(group, collector, visited, depth);
        }
    }

    private void collectOnFieldGroups(AffectedSnapshot snapshot,
                                      Map<SubscriberKey, Group> groups, Set<SubscriberKey> visited) throws ServiceException {
        // @Cacheable на FieldListenerOnFieldService.findOnFieldListeners(...) — Caffeine под капотом.
        // Cache key — composite через CollectionUtils.generateUniqueKey(#publisherFieldIds).
        List<FieldListenerOnFieldEntity> listeners = listenerOnFieldService
                .findOnFieldListeners(snapshot.changedFieldClassIds());

        // Собираем все (pointerId, publisherTwinId, listener) тройки, потом batch-resolve по pointerId.
        Map<UUID, List<PendingResolve>> pendingByPointer = new HashMap<>();
        for (FieldListenerOnFieldEntity listener : listeners) {
            UUID changedFieldId = listener.getPublisherTwinClassFieldId();
            if (!snapshot.changedFieldClassIds().contains(changedFieldId)) continue;
            for (UUID publisherTwinId : snapshot.twinsByChangedField(changedFieldId)) {
                pendingByPointer
                        .computeIfAbsent(listener.getSubscriberTwinPointerId(), k -> new ArrayList<>())
                        .add(PendingResolve.forField(publisherTwinId, changedFieldId, listener));
            }
        }
        resolveAndDistribute(pendingByPointer, groups, visited, false);
    }

    private void collectOnActionGroups(AffectedSnapshot snapshot,
                                       Map<SubscriberKey, Group> groups, Set<SubscriberKey> visited) throws ServiceException {
        Map<UUID, List<PendingResolve>> pendingByPointer = new HashMap<>();
        for (ActionKey actionKey : snapshot.twinActionsByKey()) {
            // @Cacheable lookup per (publisherClassId, action) — Caffeine под капотом.
            List<FieldListenerOnActionEntity> listeners = listenerOnActionService
                    .findOnActionListeners(actionKey.twinClassId(), actionKey.action());
            for (UUID publisherTwinId : snapshot.twinIdsByAction(actionKey)) {
                for (FieldListenerOnActionEntity listener : listeners) {
                    if (!shouldFireByValidators(listener, publisherTwinId)) continue;
                    pendingByPointer
                            .computeIfAbsent(listener.getSubscriberTwinPointerId(), k -> new ArrayList<>())
                            .add(PendingResolve.forAction(publisherTwinId, actionKey.action(), listener));
                }
            }
        }
        resolveAndDistribute(pendingByPointer, groups, visited, true);
    }

    /**
     * Для каждого pointerId: один batch-load (см. §4.3.1) всех publisher-ов → Map<publisherTwinId, subscriberTwin>.
     * Batch-load TwinClassFieldEntity для subscriber-полей и changed-полей (для OnField).
     * Затем распределяет результаты по Group-ам. Один SQL на pointer + один SQL на field-batch — не N.
     */
    private void resolveAndDistribute(Map<UUID, List<PendingResolve>> pendingByPointer,
                                      Map<SubscriberKey, Group> groups,
                                      Set<SubscriberKey> visited, boolean isAction) throws ServiceException {
        for (var e : pendingByPointer.entrySet()) {
            UUID pointerId = e.getKey();
            List<PendingResolve> pendings = e.getValue();

            ResolvedBatch resolved;
            try {
                resolved = batchResolve(pointerId, pendings, isAction);
            } catch (ServiceException ex) {
                if (ex.getErrorCode() == ErrorCodeTwins.POINTER_NON_SINGLE) {
                    log.warn("Pointer {} batch returned NON_SINGLE for {} publishers, skipping batch",
                            pointerId, pendings.size());
                    meterRegistry.counter("field_listener_pointer_non_single.total",
                            Tags.of("batch", "true")).increment();
                    continue;  // skip весь batch — partial-retry см. §7
                }
                throw ex;
            }

            for (PendingResolve p : pendings) {
                TwinEntity subscriber = resolved.subscriberByPublisher.get(p.publisherTwinId());
                if (subscriber == null) {
                    meterRegistry.counter("field_listener_no_subscriber.total").increment();
                    continue;
                }
                UUID subscriberFieldId = p.listenerSubscriberFieldId();
                SubscriberKey key = new SubscriberKey(subscriber.getId(), subscriberFieldId);
                if (visited.contains(key)) continue;

                TwinEntity publisherTwin = resolved.publisherTwinById.get(p.publisherTwinId());
                TwinClassFieldEntity subscriberField = resolved.subscriberFieldById.get(subscriberFieldId);

                PublisherRef ref = isAction
                        ? new PublisherByAction(publisherTwin, p.action(), p.listenerAsync())
                        : new PublisherByField(publisherTwin,
                            resolved.changedFieldById.get(p.changedFieldId()), p.listenerAsync());

                Group group = groups.computeIfAbsent(key, k ->
                        new Group(key, subscriber, subscriberField, new ArrayList<>()));
                group.publishers().add(ref);
            }
        }
    }

    /** Один pointer → один Pointer.load() + один batch-load TwinClassFieldEntity для всех нужных полей. */
    private ResolvedBatch batchResolve(UUID pointerId, List<PendingResolve> pendings, boolean isAction) throws ServiceException {
        TwinPointerEntity pointer = twinPointerService.findById(pointerId);  // Caffeine L1 (см. §4.3.2)
        List<UUID> publisherIds = pendings.stream().map(PendingResolve::publisherTwinId).distinct().toList();
        Collection<TwinEntity> publisherTwins = twinService.findByIds(publisherIds);
        Pointer pointerFeaturer = featurerService.getFeaturer(pointer.getPointerFeaturerId(), Pointer.class);
        Map<UUID, TwinEntity> subscriberByPublisher = pointerFeaturer.load(pointer, publisherTwins);  // см. §4.3.1

        Map<UUID, TwinEntity> publisherTwinById = publisherTwins.stream()
                .collect(Collectors.toMap(TwinEntity::getId, Function.identity()));

        // Batch-load TwinClassFieldEntity: subscriber-поля для всех pendings, changed-поля для OnField
        Set<UUID> fieldIds = new HashSet<>();
        for (PendingResolve p : pendings) {
            fieldIds.add(p.listenerSubscriberFieldId());
            if (!isAction && p.changedFieldId() != null) fieldIds.add(p.changedFieldId());
        }
        Map<UUID, TwinClassFieldEntity> fieldById = twinClassFieldService.findByIds(fieldIds).stream()
                .collect(Collectors.toMap(TwinClassFieldEntity::getId, Function.identity()));

        return new ResolvedBatch(subscriberByPublisher, publisherTwinById, fieldById);
    }

    private void dispatchNotify(Group group, TwinChangesCollector collector,
                                Set<SubscriberKey> visited, int depth) throws ServiceException {
        // Если хотя бы один publisher помечен async → вся группа идёт async (atomicity по группе).
        boolean anyAsync = group.publishers().stream().anyMatch(PublisherRef::async);
        FieldListenerEvent event = new FieldListenerEvent(
                group.subscriberTwin(), group.subscriberField(), List.copyOf(group.publishers()));
        if (anyAsync) {
            scheduleAsyncNotify(event, collector);
            return;
        }

        FieldTyper fieldTyper = featurerService.getFeaturer(
                group.subscriberField().getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldListenerSubscriber subscriber)) {
            log.warn("FieldTyper {} for field {} is not a FieldListenerSubscriber, skipping",
                    fieldTyper.getClass().getSimpleName(), group.key().subscriberFieldId());
            meterRegistry.counter("field_listener_not_subscriber.total").increment();
            return;
        }

        // Один вызов notify на группу. event уже содержит Entity — без дополнительных findById в FieldTyper.
        // sync flow: serializeValue внутри notify читает operands из collector-pending-state (publishers ещё не в БД).
        subscriber.notify(event, collector);

        // Каскад: serializeValue положил изменение subscriber-поля в тот же collector.
        // Recurses с тем же visited-set + depth+1 — новый snapshot обнаружит изменение subscriber-поля
        // (если оно operand для другого Mater) и trigger-ит новые группы.
        triggerAffected(collector, visited, depth + 1);
    }

    private boolean shouldFireByValidators(FieldListenerOnActionEntity listener, UUID publisherTwinId) throws ServiceException {
        List<FieldListenerOnActionValidatorRuleEntity> rules = validatorRuleRepository
                .findByFieldListenerOnActionIdOrderByOrder(listener.getId());
        if (rules.isEmpty()) return true;
        for (var rule : rules) {
            if (!rule.isActive()) continue;
            ValidationResult r = validatorService.validate(publisherTwinId, rule.getTwinValidatorSetId());
            if (!r.isSuccess()) {
                meterRegistry.counter("field_listener_validation_skipped.total",
                        Tags.of("reason", r.getReason())).increment();
                return false;
            }
        }
        return true;
    }

    private void scheduleAsyncNotify(FieldListenerEvent event, TwinChangesCollector collector) {
        // Сериализовать event в JSON, положить в TwinChangeTaskEntity (NEED_START) через collector — outbox.
        // Worker-pool после commit publisher-tx прочитает, десериализует, вызовет subscriber.notify в новой tx.
        // Здесь publishers уже в БД — поэтому serializeValue / SQL aggregate работают.
        // ВНИМАНИЕ: TwinEntity / TwinClassFieldEntity в event не сериализуются напрямую; worker
        // должен по UUID из event'а загрузить их заново в своей tx (см. §7 open question про async event payload).
        collector.addPostponedFieldListenerNotify(event);
    }

    private void scheduleConsolidatedBulkTask(AffectedSnapshot snapshot, TwinChangesCollector collector) {
        collector.addPostponedFieldListenerBulk(snapshot.touchedTwinIds(), snapshot.changedFieldClassIds());
    }

    // === Group/event DTOs ===

    /** Ключ дедупликации групп: UUID-pair (TwinEntity не подходит для HashMap key — hashCode/toString конфликтуют с JPA). */
    record SubscriberKey(UUID subscriberTwinId, UUID subscriberFieldId) {}

    /** Группа с уже загруженными Entity для dispatchNotify. */
    record Group(
            SubscriberKey key,
            TwinEntity subscriberTwin,
            TwinClassFieldEntity subscriberField,
            List<PublisherRef> publishers
    ) {}

    /** Внутренний batch-result: всё, что.loaded для одной группы pendings. */
    record ResolvedBatch(
            Map<UUID, TwinEntity> subscriberByPublisher,        // from Pointer.load
            Map<UUID, TwinEntity> publisherTwinById,            // publisher TwinEntity по id
            Map<UUID, TwinClassFieldEntity> subscriberFieldById // для subscriber-полей И changed-полей
    ) {}

    /** Вспомогательная запись: один pending resolve subscriber-а в batch-режиме. */
    record PendingResolve(
            UUID publisherTwinId,
            UUID changedFieldId,        // для OnField
            TwinAction action,           // для OnAction
            boolean listenerAsync,
            UUID listenerSubscriberFieldId
    ) {
        static PendingResolve forField(UUID publisherTwinId, UUID changedFieldId, FieldListenerOnFieldEntity l) {
            return new PendingResolve(publisherTwinId, changedFieldId, null, l.isAsync(), l.getSubscriberTwinClassFieldId());
        }
        static PendingResolve forAction(UUID publisherTwinId, TwinAction action, FieldListenerOnActionEntity l) {
            return new PendingResolve(publisherTwinId, null, action, l.isAsync(), l.getSubscriberTwinClassFieldId());
        }
    }
}
```

**Удалённый метод `resolveSubscriberTwin(UUID, UUID)`**: заменён на `batchResolveSubscribers(pointerId, pendings)`. Single-twin API `Pointer.point()` остаётся доступным для external callers (например, business logic, не FieldListener), но FieldListenerService использует только batch API `Pointer.load()`.

**Почему batch важен.** Bulk update 100 Task → без batch было бы 100 SQL в `PointerOnHead.load` (по одному на parent lookup). С batch — один SQL `SELECT id, head_twin_id FROM twin WHERE id IN (100 UUIDs)`. На 10k bulk — экономия 10k → 1 SQL на pointer group. Это критично для performance SLA p95 ≤30s.

#### 5.2.3. Интерфейс FieldListenerSubscriber

Реализуется на тех `FieldTyper`, которые могут быть Mater-подписчиками (в первую очередь — `FieldTyperCalcBinaryMater` и его наследники). Конкретные наследники могут override-ить `notify` для оптимизаций (delta-increment для Sum-вариантов и т.п.).

```java
package org.twins.core.service.fieldlistener;

/**
 * Контракт: FieldTyper как получатель уведомления от FieldListenerService.
 *
 * Реализация сама решает, как именно обновить Mater-поле:
 *  - full recompute через serializeValue (default в FieldTyperCalcBinaryMater)
 *  - delta-increment через collector (override в FieldTyperCalcSumMater — future)
 *  - skip (например, operand missing и skipIfEmpty=true)
 *
 * Изменения кладёт в collector для atomicity с publisher-tx (sync flow) или
 * с worker-tx (async flow). Возвращает void — FieldTyper обновляет ровно одно
 * поле (subscriber), списка изменений нет по архитектуре FieldTyper.
 */
public interface FieldListenerSubscriber {

    /**
     * @param event     full context: subscriber TwinEntity + TwinClassFieldEntity (уже загружены),
     *                  publishers с готовыми Entity (без дополнительных lookup-ов в FieldTyper)
     * @param collector accumulate-точка для изменений Mater-поля; та же tx что и у publisher (sync) или worker (async)
     */
    void notify(FieldListenerEvent event, TwinChangesCollector collector) throws ServiceException;
}

/**
 * Событие «обнови Mater-поле subscriber.subscriberField учитывая publishers».
 * Один event = одна группа (один subscriber twin + одно subscriber field), publishers ≥1.
 *
 * Все Entity уже загружены FieldListenerService.batchResolveSubscribers + единый findById
 * для subscriberField — FieldTyper не должен делать дополнительных lookup-ов в БД.
 */
record FieldListenerEvent(
        TwinEntity subscriberTwin,
        TwinClassFieldEntity subscriberField,
        List<PublisherRef> publishers
) {}

/**
 * Marker для двух вариантов publisher-а. Sealed interface → exhaustive switch в FieldTyper.
 * Все publishers в одном event-е всегда одного типа (группировка идёт отдельно для OnField
 * и OnAction listener-ов, mixed не смешиваются).
 */
sealed interface PublisherRef permits PublisherByField, PublisherByAction {
    TwinEntity twin();
    boolean async();
}

/**
 * Publisher для OnField listener-а: у twin-а изменилось поле twinClassField (operand).
 * Обе Entity уже загружены, никаких дополнительных lookup-ов не требуется.
 *
 * ВАЖНО: на момент notify в sync flow publisher-twin уже изменён в TwinChangesCollector,
 * но ещё не закоммичен в БД. serializeValue внутри FieldTyper читает operands через
 * twinClassFieldService.getDecimalValue() — он умеет ходить в TwinChangesCollector поверх БД.
 */
record PublisherByField(
        TwinEntity twin,
        TwinClassFieldEntity twinClassField,
        boolean async
) implements PublisherRef {}

/**
 * Publisher для OnAction listener-а: над twin-ом выполнена TwinAction (CREATE/EDIT/DELETE).
 * TwinAction — enum, не Entity; twin Entity уже загружено.
 */
record PublisherByAction(
        TwinEntity twin,
        TwinAction action,
        boolean async
) implements PublisherRef {}
```

**Default-реализация на FieldTyperCalcBinaryMater:**

```java
package org.twins.core.featurer.fieldtyper.mater;

public abstract class FieldTyperCalcBinaryMater
        extends FieldTyperDecimalBase<Numeric, Text, Numeric>
        implements FieldTyperCalcBinary, FieldTyperCalcMater, FieldListenerSubscriber {

    /**
     * MVP default: full recompute через serializeValue.
     * subscriberTwin и subscriberField уже приходят в event — никаких лишних findById.
     * serializeValue читает operands через twinClassFieldService.getDecimalValue(),
     * который умеет ходить в TwinChangesCollector (pending state) — поэтому работает
     * даже если publishers ещё не в БД (sync flow).
     */
    @Override
    public void notify(FieldListenerEvent event, TwinChangesCollector collector) throws ServiceException {
        FieldValue fieldValue = tryToInitializeValue(event.subscriberTwin(), event.subscriberField());
        serializeValue(event.subscriberTwin(), fieldValue, collector);  // переиспользуем существующую логику
    }
}
```

**Override для FieldTyperCalcSumMater (future, delta-optimization):**

```java
public class FieldTyperCalcSumMater extends FieldTyperCalcBinaryMater {

    /**
     * Future: для SUM возможен delta-increment вместо full recompute.
     * Ограничения sync flow: publishers ещё не в БД → SQL aggregate невозможен.
     * Нужен другой механизм: читать pending oldValue из collector, сравнивать с pending newValue,
     * delta класть в collector как increment (как applyDecimalIncrements).
     * В async flow: publishers уже в БД, но oldValue нужно из twin_change_history.
     *
     * MVP: просто fallback на super.notify (full recompute).
     * Optimization — отдельная итерация, см. §7.
     */
    @Override
    public void notify(FieldListenerEvent event, TwinChangesCollector collector) throws ServiceException {
        super.notify(event, collector);
    }
}
```

**Почему `FieldTyper` реализует интерфейс напрямую (а не отдельный класс-подписчик).**

- **Тесная связь с params и serializeValue.** `notify` нужен доступ к `firstFieldId`, `secondFieldId` из `featurerParams`, и к существующему `serializeValue` — всё это уже на FieldTyper. Отдельный subscriber-класс потребовал бы дублирования или делегирования.
- **Будущая авто-регистрация listeners из FieldTyper.** На втором этапе (см. §5.4) при `TwinClassFieldService.saveField()` нужно извлекать operand-fields из params и регистрировать listeners. Наличие `FieldListenerSubscriber` на самом FieldTyper даёт единый self-describing контракт: FieldTyper сам знает, на какие поля ему подписываться — это пригодится для авто-регистрации.
- **Проверка «может ли FieldTyper быть Mater-подписчиком» = `instanceof FieldListenerSubscriber`.** Простой, без реестра featurer_id → subscriber-class.

### 5.3. Hook в TwinService

```java
// TwinService.java
public void createTwin(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
    createTwins(TwinCreateStage.of(twinCreate), twinChangesCollector);
    fieldListenerService.triggerAffected(twinChangesCollector);  // ← НОВЫЙ HOOK
}

public void updateTwin(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector,
                       ChangesRecorder<TwinEntity, ?> recorder) throws ServiceException {
    // ... существующая логика updateTwin ...
    fieldListenerService.triggerAffected(twinChangesCollector);  // ← НОВЫЙ HOOK
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

2. **`field_listener_on_action.publisher_twin_action_id` — varchar vs enum.** Сейчас varchar + FK на `twin_action` — OK для расширяемости. Альтернатива: `@Enumerated(EnumType.STRING)` в JPA — но тогда нельзя добавить кастомный action без деплоя. Решение: оставить varchar + FK.

3. **FK ON DELETE для subscriber_twin_class_field_id.** Сейчас `ON DELETE NO ACTION` (по умолчанию). При удалении Mater-поля операция упадёт, пока listener не удалён. Альтернативы: (а) `ON DELETE CASCADE` для listener-ов + `mater_stale=true` на зависимых полях; (б) `ON DELETE RESTRICT` с явным precondition-check. Решение: NO ACTION сейчас, добавить `dropListenerAndMarkStale(fieldId)` в `TwinClassFieldService.deleteField()`.

4. **Bulk-detection forced-async для sync listener-ов.** Если listener.async=false, но >50 twins → форсить async через consolidated task. Иначе deadlock risk. Подтвердить поведение.

5. **Каскад Mater→Mater через разные pointer-ы.** Сценарий: A-class Mater по SELF-pointer зависит от B, B-class Mater по PARENT-pointer зависит от C (ребёнок). При UPDATE C → recompute B → A-class Mater должен тоже пересчитаться. Это работает только если A-class listener тоже срабатывает на «child updated». Нужно ли для OnField listener-ов автоматически добавлять transitive listeners? Или только явная регистрация?

6. **Validator-rule semantics.** Сейчас — «если validator fails, listener пропускается». Альтернатива: «если validator fails, listener всё равно срабатывает, но возвращает специальный флаг для FieldTyper, чтобы тот исключил twin из расчёта». Решение зависит от семантики `FieldTyperCalcChildrenFieldV1`-аналога — нужно ли «sum where validator passes» или «sum, и listener решает, обновлять ли».

7. **TwinPointer discovery для ручной регистрации.** MVP требует ручного SQL — но пользователю надо знать UUID-ы pointer-ов. Стоит ли добавить admin-REST endpoint `/twin-pointers?classId=X` для lookup? Или документировать `SELECT id, name FROM twin_pointer WHERE twin_class_id = ...` в README?

8. **Performance baseline.** До реализации снять `pg_stat_statements` + `@Timed` на on-the-fly калькуляторах (см. v1 §15.2). Без baseline нельзя доказать ROI и выбрать SLA.

9. **Pointer на корень иерархии (root) и на TwinLink-родителя dst.** `PointerOnHead` покрывает непосредственного родителя через FK `head_twin_id`. Но если понадобится: (а) listener на «любом descendant → recompute Mater у root-а» — нужен pointer через `hierarchy_tree` (ltree) с lookup корня; (б) listener «от dst TwinLink к parent dst twin-а» — нужен составной pointer. Сейчас это не реализовано; заводить как только появится конкретный use-case, premature сейчас.

10. **Многодетный кейс для `FieldTyperCalcChildrenFieldV1`.** `PointerOnHead` решает «single-parent от ребёнка», но не «multi-child → parent aggregation». Сама агрегация делается не pointer-ом, а serializeValue внутри FieldTyper — pointer только находит subscriber-а (parent). Если parent обновляется из-за одного ребёнка, а детей 1000 — `serializeValue` parent-а сделает SUM заново по всем детям. Это может быть узким местом; оптимизация через delta-increment (SUM только разница) — отдельная тема, см. §4.2 bulk и v1 про `applyDecimalIncrements`.

11. 🔴 **Delta-optimization для SUM Mater — требует проработки отдельно под sync/async flow.** MVP делаем full recompute через `serializeValue` для всех Mater-типов. Delta-increment (`UPDATE twin_field SET value = value + Δ`) привлекательнее для bulk, но есть два принципиально разных flow-а:
    - **Sync flow (publisher-tx ещё не закоммичена)**: publishers находятся в `TwinChangesCollector`, в БД их нет. SQL aggregate (`SELECT SUM(value) WHERE twin_id IN (publishers)`) бесполезен. Чтобы посчитать Δ нужно: (а) прочитать **persisted oldValue** из БД ДО изменения, (б) прочитать **pending newValue** из collector, (в) Δ = newValue − oldValue. Это требует расширения `TwinChangesCollector` API: метод «дай мне pending newValue и persisted oldValue для этого поля твина».
    - **Async flow (worker-tx после commit)**: publishers уже в БД. Но oldValue уже утерян — нужно читать из `twin_change_history` (последний snapshot до изменения). Это медленнее и требует индексов по history.
    
    **Рекомендация:** не реализовывать delta в MVP. Сначала baseline-замеры (`pg_stat_statements` + `@Timed`) — если SUM Mater на bulk-CUD не упрётся в latency, full recompute достаточно. Если упрётся — приоритизировать sync flow (вариант а) как более дешёвый.

12. **Каскадная рекурсия через `triggerAffected(collector, visited, depth+1)` после `subscriber.notify`.** Сейчас FieldListenerService recursing-ет после каждого notify, рассчитывая, что новый snapshot из collector поймает изменения subscriber-поля (если оно operand для другого Mater). Возможные проблемы:
    - Один и тот же subscriber-field может быть operand для нескольких других Mater-полей — все они должны быть в одной cascade-волне. Текущая рекурсия ловит это через новый snapshot.
    - Cycle protection через visited-set: если A → B → A, второе срабатывание A пропустится через visited. Но深度 cap (default 5) — единственная защита от очень длинных цепочек. Если в domain будет 6+ уровней Mater-цепочек — поднять cap через конфиг.
    - Performance: каждая cascade-волна = новый full snapshot extract. Для глубоких цепочек (depth=5) это 5 snapshots. На больших collector-ах может быть дорого. Альтернатива: incremental snapshot (только новые изменения с прошлого snapshot). Не в MVP.

13. **Partial failure в `Pointer.load` batch-е.** Если для одного из publisher-ов в batch-е pointer резолвится в несколько кандидатов (`POINTER_NON_SINGLE`), сейчас весь batch для этого pointer-а skip-ается (см. `resolveAndDistribute` try-catch). Это потеря валидных notify для других publisher-ов того же batch-а. Возможные варианты:
    - (а) **Оставить как в MVP** (skip весь batch). Просто, но теряет часть notify на bulk CUD если один publisher сконфигурирован криво (например, у одного TwinLink два dst, остальные норм). Reconciliation cron (§4.6) подстрахует.
    - (б) **Partial retry**: при POINTER_NON_SINGLE — сплит batch пополам, retry каждой половины; где падает — ещё сплит, пока не останется один problematic publisher, который skip-аем individually. Правильно, но усложняет код.
    - (в) **Изменить контракт load**: вернуть `Map<UUID srcId, Either<TwinEntity, ServiceException>>` — partial success на уровне API. Самый гибкий, но усложняет все реализации subclass-ов.
    
    **Рекомендация:** MVP — вариант (а), цикл `reconciliation cron` покрывает loss. Если метрика `field_listener_pointer_non_single.total{batch=true}` окажется значимой на проде — переходить на (б).

14. **`Pointer.load` контракт на null subscribers.** Если для какого-то src twin подписчик не найден (например, `PointerOnHead` для root twin-а), `load` возвращает map без записи для этого srcId. Базовый класс `Pointer.load` после `load` обходит `misses`, и для отсутствующих в loaded map берёт `null`. Это работает, но есть альтернатива: явный marker-key `Map.put(srcId, null)`. Решение: возвращать map без ключа (как в коде) — проще; null-handling делается в `load`. Зафиксировано в §4.3.1.

15. 🔴 **Async event payload serialization.** `FieldListenerEvent` теперь содержит `TwinEntity subscriberTwin` и `TwinClassFieldEntity subscriberField`, плюс `List<PublisherRef>` с `TwinEntity twin`. При `anyAsync=true` event попадает в `TwinChangeTaskEntity` (через `collector.addPostponedFieldListenerNotify`) и должен сериализоваться в JSON для worker-pool. JPA Entity напрямую не сериализуется (proxy, lazy-load, cyclic refs). Варианты:
    - (а) **DTO-projection**: при `addPostponedFieldListenerNotify` конвертировать event в serializable DTO (`SubscriberTwinId`, `SubscriberFieldId`, `List<PublisherTwinId>`); worker в своей tx делает batch-load entity и reconstructs event. Самый чистый, но дублирование DTO.
    - (б) **Только UUID в event**: вернуться к UUID-based event (откатить §5.2.3), всё entity-loading переложить на FieldTyper внутри notify. Потеря convenience для sync flow.
    - (в) **Dual event**: sync event содержит Entity, async event — UUID-only. FieldListenerService сам выбирает по `anyAsync`. Поддержка двух record-ов с одним интерфейсом.
    
    **Рекомендация:** вариант (а). Sync flow — Entity (быстро, без lookup-ов в FieldTyper). Async flow — DTO projection → worker reconstruct. Код прост: один метод `event.toAsyncPayload()` в FieldListenerEvent, и метод `fromAsyncPayload(payload, tx)` в worker.

16. **Cascading reload Entity на каждой cascade wave.** После `subscriber.notify()` FieldListenerService recurses (`triggerAffected(collector, visited, depth+1)`). Новый snapshot из collector → новый batch → новая batch-load TwinEntity/TwinClassFieldEntity для subscriber-полей. На глубоких cascade (depth=5) это 5 batch-load циклов. Caffeine на TwinClassFieldEntity metadata (см. §4.3.2) смягчает — но TwinEntity subscriber каждый раз грузится заново. Возможные оптимизации: (i) кэш TwinEntity по id в tx scope (аналог pointerResultCache, но на fieldListener level), (ii) инкрементальный snapshot. Не в MVP.

---

## 8. Источники

(Полный список из v1 сохраняет актуальность — см. `field-typer-mater-listeners.md` §19. Ключевые для этого документа: Transactional Outbox (AWS), Spring Transaction-bound Events, Hibernate discourse on ManyToMany, Tiger Data on transition tables, Cybertec on trigger recursion, Bart Slota on @TransactionalEventListener pitfalls.)
