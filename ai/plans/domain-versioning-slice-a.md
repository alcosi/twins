# План: Версионирование настроек домена — срез A (пилот, модель v2)

## 0. Контекст

Реализация пилота по модели v2 — `ai/plans/domain-versioning-v2.md` (дизайн согласован полностью:
схема DDL, импорт FULL/INCREMENTAL, обратные дельты, rollback двух типов, LOCK, защита колонок,
краевые кейсы, механика reconcile, стиль миграций). Документ v2 — единственный источник решений;
здесь только реализация. Предыдущая версия этого плана (модель «указатель на рабочую версию»)
заменена целиком.

Пилот — 7 таблиц: `twin_class` (+ `twin_class_field`, `twin_status`) и `data_list`
(+ `data_list_option`, `data_list_subset`, `data_list_subset_option`).

Ключевое из v2 одним абзацем: штамп `domain_version_id` = «версия последнего CUD», ставит только
триггер; `row_status` ACTIVE/DISABLED/DELETED, soft-delete до релиза, физическая чистка при
релизе; ленивое открытие окна — релиз НЕ открывает следующую версию, её создаёт первая правка,
домен рождается с V1; дельта = итоговое состояние строк со штампом релизящейся версии; аудит
`domain_version_audit` пишут все пути с тегом этапа; правки — разделяемый advisory-лок, первая
правка окна и операции — исключающий; защита колонок — GUC `twins.version_op`/`twins.import_mode`
+ `pg_trigger_depth()`.

## 1. Миграции (`db/migration_backlog/`, `V1.4.381.01+`, тикет заменить; все идемпотентны)

### 01_schema

- `domain_version`, `domain_version_audit`, колонки `domain` — DDL дословно из раздела «Схема
  (DDL)» v2-документа. Статусы — varchar + CHECK, словарных таблиц нет. FK
  `domain_version.domain_id` — `DEFERRABLE INITIALLY DEFERRED` (рождение домена), FK штампов —
  `ON UPDATE CASCADE ON DELETE RESTRICT`.
- Служебные колонки 7 таблиц — helper + плоский список (стиль TWINS-719 kk1):

```sql
CREATE OR REPLACE FUNCTION add_domain_version_columns(tbl text) RETURNS void AS $$
BEGIN
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS domain_version_id uuid', tbl);
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS row_status varchar
                        NOT NULL DEFAULT ''ACTIVE''
                        CHECK (row_status IN (''ACTIVE'',''DISABLED'',''DELETED''))', tbl);
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I (domain_id, domain_version_id)',
                   tbl || '_domain_version_idx', tbl);
    EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (domain_version_id)
                        REFERENCES domain_version(id) ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID',
                   tbl, tbl || '_domain_version_id_fk');
END;
$$ LANGUAGE plpgsql;

SELECT add_domain_version_columns('twin_class');
SELECT add_domain_version_columns('twin_class_field');
SELECT add_domain_version_columns('twin_status');
SELECT add_domain_version_columns('data_list');
SELECT add_domain_version_columns('data_list_option');
SELECT add_domain_version_columns('data_list_subset');
SELECT add_domain_version_columns('data_list_subset_option');
DROP FUNCTION IF EXISTS add_domain_version_columns(text);
```

- `domain_id` добавить таблицам без него (`twin_class_field`, `twin_status`, `data_list_option`,
  `data_list_subset`, `data_list_subset_option`) — без FK, заполняется бэкфиллом из родителя
  (шаг 03), FK добавляем после.
- **Инвентаризация уникальных бизнес-индексов** 7 таблиц до написания миграции: существующие
  unique по бизнес-ключам пересоздать partial (`WHERE row_status <> 'DELETED'`) в этой же
  миграции, отдельным осознанным списком (helper'ом не автоматизируется).

### 02_functions

GUC (оба транзакционные): `twins.version_op` — внутренние записи наших функций и обёрток;
`twins.import_mode` — применение внешнего содержимого (импорт/restore/rollback) с явными
штампами и статусами.

| Функция | Что делает |
|---|---|
| `domain_version_gate(domain_id) → uuid` | Гейт правок. NULL-домен и `import_mode` пропускает. Проверяет `operation_status = 'IDLE' AND NOT locked` (иначе ошибка 55006-стиля «идёт операция/домен заморожен»). Если текущая версия RELEASED — исключающий advisory-лок, повторная проверка, создание OPEN-окна (uuid v7, метка `draft`), `current_domain_version_id →` оно; если уже OPEN — разделяемый лок. Возвращает id окна (штамп) |
| `domain_version_init(domain_id)` | Рождение: создаёт V1 (OPEN, `draft`); вызывается before-insert-триггером `domain` (проставляет `NEW.current_domain_version_id`, FK — deferrable) и бэкфиллом. Идемпотентна |
| `domain_version_audit_write(table, row_id, operation, before, after)` | Запись аудита: `domain_operation_status` — из `domain.operation_status`, actor — из GUC `app.current_user_id`; внутри ставит `version_op`. Вызывают AFTER-обёртки |
| `domain_operation_release_start(domain_id)` | Искл. лок; IDLE; текущая версия должна быть OPEN (окно с правками существует). RELEASING |
| `domain_operation_release_end(domain_id, version, json, hash, delta, delta_hash, rollback_delta, rollback_delta_hash, duration?, name?, description?)` | Искл. лок; непустые version/json/hash. UPDATE OPEN-строки → RELEASED: финальный лейбл (проверка занятости — индексом), артефакты, `previous_domain_version_id` = последний релиз (NULL у первого), `"order"` = max+1 по домену, released_at/duration. Физическая чистка DELETED-строк домена (под `version_op`). IDLE |
| `domain_operation_import_start(domain_id, mode, expected_base_hash?)` | Искл. лок; IDLE. INCREMENTAL: текущая версия RELEASED и её `hash = expected_base_hash`, иначе ошибка «база дельты не совпадает / у цели есть черновик». FULL: без предусловий. IMPORTING |
| `domain_operation_import_end(domain_id, hash, label?)` | Искл. лок; создаёт импортную версию (RELEASED, `hash`, `label` напр. `'(import) 1.2'`, `order`, `previous` = прежний current); сворачивает пустое OPEN-окно (RESTRICT-FK подстрахует, если строки ещё ссылаются); `current →` импортная версия. IDLE |
| `domain_operation_rollback_start(domain_id)` | Искл. лок; IDLE + NOT locked + текущая OPEN. ROLLBACK |
| `domain_operation_rollback_end(domain_id)` | Искл. лок (Java применила before-образы между start и end); если у окна не осталось штампованных строк — окно удаляется, `current →` предыдущая RELEASED-версия. IDLE |
| `domain_operation_abort(domain_id)` | Сброс застрявшего статуса в IDLE |
| `domain_version_reconcile(domain_id, dry_run)` | Отчёт/физическое удаление строк со штампами вне цепи current (сироты после багов/ручных правок) |

Артефакты (снапшот, прямая/обратная дельты, хэши) строит Java между start и end — функции их
только фиксируют и валидируют. Прямая и обратная дельты — один diff пары снапшотов в двух
направлениях.

### 03_backfill

1. `domain_version_init` каждому существующему домену → V1 OPEN, `current → V1`.
2. `domain_id` дочерних таблиц — из родителей (один bulk-UPDATE на таблицу:
   `twin_class_field`/`twin_status` ← `twin_class`; `data_list_option`/`data_list_subset` ←
   `data_list`; `data_list_subset_option` ← `data_list_subset`), затем FK.
3. Штамп: `UPDATE <table> SET domain_version_id = :v1 WHERE domain_id IS NOT NULL` (батчами).
   Существующие строки становятся «изменёнными в V1» → первый релиз домена = бейзлайн с полной
   прямой дельтой (rollback-дельта NULL).
4. `VALIDATE CONSTRAINT` всех FK штампов (после бэкфилла, кратковременно).
5. Инвариант после бэкфилла: `CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)`.

### 04_triggers (строго по `docs/db_trigger_functions_convention.md`)

7 таблиц × (before insert/update/delete + after insert/update/delete), `domain`,
`domain_version`, `domain_version_audit`:

- **before-insert** (настроечные): домен — вывод из родителя или проверка совпадения (инвариант
  консистентности); входящий `domain_version_id` → ошибка; входящий `row_status` — только
  ACTIVE/DISABLED; `NEW.domain_version_id := domain_version_gate(NEW.domain_id)`.
- **before-update**: защита колонок — изменение `domain_version_id` → ошибка; установка
  `row_status = 'DELETED'` → ошибка; прямые ACTIVE↔DISABLED разрешены. Исключения:
  `twins.import_mode`, `twins.version_op`, `pg_trigger_depth() > 1`. No-op фильтр (сравнение
  образов без `updated_at`, счётчиков, штампа) — не штампуем и не пишем аудит; иначе
  рестамп гейтом.
- **before-delete**: born-in-window — первая запись аудита строки в текущем окне INSERT →
  физический DELETE (под `version_op`) + аудит DELETE; иначе конверсия в soft-delete: UPDATE
  `row_status = 'DELETED'` + штамп окна (под `version_op`, чтобы не поймать собственную защиту
  before-update) + аудит DELETE.
- **after-insert/update/delete**: `domain_version_audit_write(...)` — единственная обязанность.
- **domain** before-insert: `domain_version_init(NEW.id)` + `NEW.current_domain_version_id`.
- **domain_version**: любые записи только под `version_op`; UPDATE/DELETE строк RELEASED —
  ошибка всегда.
- **domain_version_audit**: INSERT только из `domain_version_audit_write`; UPDATE/DELETE —
  ошибка (история неизменяема; будущий архив-шедулер — под `version_op`).
- `twin_class` — тяжёлые обёртки существуют, дополняем их по чек-листу конвенции
  (DROP TRIGGER IF EXISTS → CREATE OR REPLACE → CREATE TRIGGER); остальные 6 таблиц — новые
  обёртки.

## 2. Java — не меняется

Колонки не маплены: INSERT получает `row_status` DEFAULT ACTIVE, штамп и аудит ставят триггеры,
`delete()` конвертируется в soft-delete. REST релиза/импорта/rollback/lock, маппинг ошибки
гейта (55006-стиль), построитель артефактов, UI истории по аудиту — срез B.

## 3. Тесты — `integration/service/domainversion/DomainVersionPilotIntegrationTest`

`BaseIntegrationTest`, без классового @Transactional; бэклог-миграции применяются в @BeforeEach
(паттерн `DataListSubsetMigrationAndCrudIntegrationTest`); конкурентность — два потока
REQUIRES_NEW + барьер, зависание ловим `future.get(2s)`.

1. Рождение домена через `createDomain`: создана V1 (OPEN, `draft`), `current → V1` —
   deferrable-FK работает.
2. Жизненный цикл: правки штампуют V1; релиз 1.0 — V1 → RELEASED (снапшот/хэш/прямая дельта =
   полный состав, rollback-дельта NULL, `order`=1, previous NULL), **current остаётся на V1,
   новой версии нет**; первая правка открывает V2 (uuid v7); повторные правки — та же V2.
3. Два релиза подряд: цепочка previous, `order` растёт, у второго релиза обе дельты; чистка
   DELETED при релизе удаляет строки физически.
4. Удаления: soft-delete до релиза (DELETED + штамп окна); born-in-window (вставка+удаление в
   одном окне) — физическое удаление сразу, в дельту не попадает; воскрешение по PK — с
   изменением штамп окна, без изменения — возвращён старый штамп (строка вне дельты).
5. no-op UPDATE — ни штампа, ни аудита.
6. Гейты: правки запрещены при RELEASING/IMPORTING/ROLLBACK и при `locked`; релиз требует
   OPEN-окно; INCREMENTAL требует RELEASED + совпадения хэша.
7. Гонки: правка, начатая до release_start, коммитится и попадает в релиз; правка после старта —
   отклонена; две одновременные первые правки окна сериализуются, дублей окон нет.
8. Rollback: полный откат окна восстанавливает before-образы (включая старые штампы), пустое
   окно сворачивается, `current →` RELEASED; частичный откат — по выбранным строкам, окно
   остаётся OPEN.
9. Аудит: пишут все пути (правка/импорт/чистка/rollback) с корректным
   `domain_operation_status`; UPDATE/DELETE аудита — ошибка.
10. Защита: ручное изменение штампа и установка DELETED — ошибка; `import_mode`/`version_op`
    работают; запись в `domain_version` без `version_op` — ошибка; RELEASED неизменяем даже
    с GUC.
11. Импорт: FULL в домен с черновиком (откат окна → reconcile temp-таблицей → импортная версия,
    пустое окно свёрнуто); INCREMENTAL при совпавшем хэше применяется; при OPEN-окне — сначала
    откат черновика.
12. `domain_version_reconcile` (dry_run и удаление) находит/удаляет строку с осиротевшим
    штампом.
13. Существующие домены: бэкфилл создал V1 и заштамповал строки; инвариант
    `domain_id IS NULL OR domain_version_id IS NOT NULL` держится.

Верификация: пилотный IT зелёный + полный `:twins-core:test` (каждый IT создаёт домены →
рождение домена исполняется везде — регрессионная сеть).

## 4. Дорожная карта

- **Срез B (Java)**: entity/repository версий; построитель артефактов (детерминированный
  ObjectMapper из `SqlBuilder`): снапшот, прямая/обратная дельты, SHA-256; REST релиза, импорта,
  rollback, lock/unlock, список версий, скачивание артефактов; маппинг ошибки гейта; UI истории
  правок по аудиту (фильтр `domain_operation_status = 'IDLE'`).
- **Срез C**: импорт/restore по контрактам v2 — FULL (reconcile temp-таблицей), INCREMENTAL
  (цепочки дельт по хэшу базы), обратные цепочки, rollback API.
- **Срез D**: дублирование домена = `createDomain` + FULL-импорт снапшота исходника с
  перегенерацией id и перемапом ссылок (`resolveTwinReferences`).
- **Волны 1–4**: тот же контракт на остальные ~180 настроечных таблиц семействами
  (колонки+бэкфиллом → триггеры); до волны i18n решить, едут ли переводы в версиях.

## 5. Риски

- Инвентаризация уникальных бизнес-индексов 7 таблиц и их partial-пересоздание — сделать до
  написания 01 (упустить = воскрешение/повторная вставка упрётся в unique).
- Конверсия DELETE→UPDATE в before-delete — единственное нестандартное место против конвенции
  обёрток: оформить отдельным helper'ом, покрыть тестом 4.
- Бэкфилл больших доменов — батчевые UPDATE; VALIDATE FK кратковременен.
- Вывод домена из родителя при вставке дочерней строки — один PK-lookup, приемлемо.
- `pg_trigger_depth()` в защите: обёртки аудита и конверсии вызывают другие обёртки — учесть
  глубину в тестах (тест 10).
