# Export: перевод на `ON CONFLICT DO UPDATE` (upsert) + `clearElements` для Factory

> План одобрен 2026-07-20. Сохранён для последующей реализации. Связанная уже реализованная фича: `cascadeFactory` (branch2, застейджена, компиляция не верифицирована — нет Java 21 в окружении).

## Context

Все SQL-export-эндпойнты сейчас генерируют `INSERT ... ON CONFLICT DO NOTHING`. При повторной заливке на другую БД это оставляет stale-строки: если id совпал, но definition изменился — старая строка не обновляется. Два согласованных решения:
1. **Перевести весь export на upsert** (`ON CONFLICT (id) DO UPDATE SET ...`) — актуализация definition существующих строк без удаления. Применяется ко **всем** export-сервисам.
2. **`clearElements` для Factory** — дополнительно удалить orphan-элементы фабрики (которых нет в выгрузке) перед upsert, чтобы целевая БД пришла в состояние «ровно то, что в выгрузке».

Предполагается (в другой ветке, но считаем что есть): FK `twin_factory_condition_set.twin_factory_id → twin_factory(id)` — полноценная связь. Саму `twin_factory` не удаляем (внешние FK из `twinflow_transition` — `V1.3.98.01:8485,8493`).

## Часть A — Upsert во всём export-стеке

### A.1 `SqlBuilder` (`core/.../common/sql/SqlBuilder.java`)
Добавить upsert-методы + определение PK в metadata:
- `EntityMetadata`: добавить поле `idColumnName` (определяется по аннотации `@Id` в `extractMetadata`; fallback — колонка `"id"`). Существующие `columns/extractors` не менять.
- `public String buildUpsert(Object entity)` — как `buildInsert`, но хвост: `ON CONFLICT (<id>) DO UPDATE SET <col>=EXCLUDED.<col>, ...` для всех insertable-колонок кроме id.
- `public String buildUpserts(Collection<?> entities)` — `joining("\n")` как `buildInserts`.
- `buildInsert`/`buildInserts` (DO NOTHING) оставить — нужны для случаев, где перезапись нежелательна.

### A.2 Массовая замена `buildInserts` → `buildUpserts` во всех export-сервисах
Одинаковый паттерн: в `exportCollectionToSql` / `exportToSql` каждого сервиса заменить `sqlBuilder.buildInserts(list)` на `sqlBuilder.buildUpserts(list)`.

Factory-семейство (`core/.../service/factory/`):
- `FactoryExportService` (фабрика + оркестрация)
- `FactoryBranchExportService`, `FactoryPipelineExportService`, `FactoryPipelineStepExportService`
- `FactoryMultiplierExportService`, `FactoryMultiplierFilterExportService`
- `FactoryEraserExportService`, `FactoryTriggerExportService`
- `FactoryConditionSetExportService`, `FactoryConditionExportService`

Соседние export-сервисы:
- `service/twinclass/TwinClassExportService`, `TwinClassFieldExportService`
- `service/twin/TwinStatusExportService`, `TwinPointerExportService`
- `service/twinflow/TwinflowExportService`
- `service/i18n/I18nExportService` + `service/sql/I18nSqlBuilder` (тут используется `i18nSqlBuilder.buildI18nInsert` — добавить `buildI18nUpsert` для `i18n` и `i18n_translation`; переводит 2 таблицы).

### A.3 Риск
Все export-сущности должны иметь `@Id` на single-column PK (проверено для `TwinFactoryEntity` и типичных сущностей — поле `id`). Если встретится сущность без `@Id`/с composite key — `buildUpsert` бросит ошибку PK-detection; обрабатывать по факту.

## Часть B — `clearElements` для Factory (orphan-cleanup)

### B.1 Миграция `core/src/main/resources/db/migration/V1.4.324.01__multiplier_filter_cascade_delete.sql` (тикет TBD)
```sql
ALTER TABLE public.twin_factory_multiplier_filter
    DROP CONSTRAINT IF EXISTS twin_factory_multiplier_filter_twin_factory_multiplier_id_fk;
ALTER TABLE public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_twin_factory_multiplier_id_fk
        FOREIGN KEY (twin_factory_multiplier_id) REFERENCES public.twin_factory_multiplier(id)
        ON DELETE CASCADE;
```
→ `multiplier_filter` удаляется каскадом от `multiplier`, как `pipeline_step` (от pipeline) и `condition` (от condition_set). Отдельные subquery-DELETE для них не нужны.

### B.2 DTO `FactoryExportSqlRqDTOv1.java`
```java
@Schema(description = "before upsert, DELETE all factory elements of include-* categories so orphan rows (present in target DB but not in this export) are removed. Factory row itself is NOT deleted (external FKs). Steps/conditions/multiplier_filters are removed via DB cascade. twin_factory_condition_set is cleared only when all its RESTRICT referencers are in clear scope, otherwise skipped with a SQL comment (its definition is still refreshed by the upsert).")
public boolean clearElements = false;
```

### B.3 Controller `FactoryExportSqlController.java`
`request.isClearElements()` последним аргументом в `exportToSql(...)`.

### B.4 FactoryExportService
- `@Slf4j`.
- Параметр `boolean clearElements` в перегрузках (делегаты с `false`).
- После `expandFactoryCascade`, до i18n — `appendClearElementsSql(...)`.
- INSERT-блок теперь upsert (`buildUpserts` вместо `buildInserts`) — из части A.

Порядок DELETE (FK-safe), для категорий с `includeXxx=true`:

| # | DELETE | Триггер |
|---|---|---|
| 1 | `twin_factory_pipeline` WHERE `twin_factory_id` IN (...) — steps cascade | `includePipelines` |
| 2 | `twin_factory_branch` | `includeBranches` |
| 3 | `twin_factory_eraser` | `includeErasers` |
| 4 | `twin_factory_multiplier` — filters cascade (после миграции) | `includeMultipliers` |
| 5 | `twin_factory_trigger` | `includeTriggers` |
| 6 | `twin_factory_condition_set` — **только если `canClearConditionSets`** | `includeConditionSets` + предикат |

Предикат: `canClearConditionSets = includeConditionSets && includePipelines && includeBranches && includeErasers && includeMultipliers`.
Если `includeConditionSets && !canClearConditionSets` → skip DELETE CS + SQL-комментарий + `log.warn` (определение всё равно обновится через upsert — orphan маловероятен, т.к. CS принадлежит фабрике).

Helper:
```java
private void appendClearElementsSql(StringList sqlParts, Collection<TwinFactoryEntity> factories, /*8 include flags*/) {
    if (CollectionUtils.isEmpty(factories)) return;
    Set<UUID> factoryIds = factories.stream().map(TwinFactoryEntity::getId).collect(Collectors.toSet());
    StringList deletes = new StringList();
    if (includePipelines)   deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryPipelineEntity.class,  "twin_factory_id", factoryIds));
    if (includeBranches)    deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryBranchEntity.class,   "twin_factory_id", factoryIds));
    if (includeErasers)     deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryEraserEntity.class,    "twin_factory_id", factoryIds));
    if (includeMultipliers) deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryMultiplierEntity.class,"twin_factory_id", factoryIds));
    if (includeTriggers)    deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryTriggerEntity.class,   "twin_factory_id", factoryIds));
    boolean canClearConditionSets = includeConditionSets && includePipelines && includeBranches && includeErasers && includeMultipliers;
    if (includeConditionSets && !canClearConditionSets) {
        deletes.add("-- clearElements: twin_factory_condition_set cleanup SKIPPED (RESTRICT referencers outside clear scope: "
                  + joinMissing(includePipelines, includeBranches, includeErasers, includeMultipliers) + "); definition refreshed via upsert");
    } else if (canClearConditionSets) {
        deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryConditionSetEntity.class, "twin_factory_id", factoryIds));
    }
    if (!deletes.isEmpty()) { sqlParts.add("-- clearElements: factories = " + factoryIds); sqlParts.addAll(deletes); }
}
```

## SqlBuilder — `buildDeleteByColumn` (для части B)
```java
public String buildDeleteByColumn(Class<?> entityClass, String columnName, Collection<UUID> ids) {
    if (ids == null || ids.isEmpty()) return "";
    return "DELETE FROM " + resolveTableName(entityClass) + " WHERE " + quoteIdentifier(columnName)
         + " IN " + formatUuidInClause(ids) + ";";
}
private String quoteIdentifier(String name) { return "\"" + name + "\""; }
private String formatUuidInClause(Collection<UUID> ids) {
    return ids.stream().map(id -> "'" + id + "'").collect(Collectors.joining(", ", "(", ")"));
}
```
`buildDeleteByParentSubquery` не нужен (все зависимые каскадом).

## Результирующий SQL (Factory export, все include, clearElements=true, 2 фабрики)
```sql
-- clearElements: factories = [u1, u2]
DELETE FROM "twin_factory_pipeline" WHERE "twin_factory_id" IN ('u1','u2');
DELETE FROM "twin_factory_branch" WHERE "twin_factory_id" IN ('u1','u2');
DELETE FROM "twin_factory_eraser" WHERE "twin_factory_id" IN ('u1','u2');
DELETE FROM "twin_factory_multiplier" WHERE "twin_factory_id" IN ('u1','u2');
DELETE FROM "twin_factory_trigger" WHERE "twin_factory_id" IN ('u1','u2');
DELETE FROM "twin_factory_condition_set" WHERE "twin_factory_id" IN ('u1','u2');
-- i18n upserts
INSERT INTO "i18n" (...) VALUES (...) ON CONFLICT (id) DO UPDATE SET ...;
-- factory upserts
INSERT INTO "twin_factory" (...) VALUES (...) ON CONFLICT (id) DO UPDATE SET ...;
-- children upserts ...
```

## Порядок выполнения (compile-safe)
1. Миграция `V1.4.324.01`.
2. `SqlBuilder`: `idColumnName` в metadata + `buildUpsert(s)` + `buildDeleteByColumn` + приватные helpers.
3. `I18nSqlBuilder`: `buildI18nUpsert`.
4. Все export-сервисы: `buildInserts` → `buildUpserts` (Factory-семейство + twinclass/twin/twinflow/i18n).
5. `FactoryExportSqlRqDTOv1` + `FactoryExportSqlController` + `FactoryExportService.appendClearElementsSql`.

## Edge cases
- `clearElements=true && upsert`: DELETE orphans → upsert создаёт/обновляет. Комбо = точное состояние выгрузки для элементов фабрики.
- `clearElements=false`: только upsert (обновляет, orphan остаются). Поведение остальных export-сервисов (без clearElements) — только upsert.
- `includeConditionSets=true && не все ссылочники true`: CS-skip в clear (комментарий); definition обновится upsert'ом.
- `includePipelines=true && includePipelineSteps=false`: steps сносятся каскадом в clear и не перезаливаются (задокументировать).
- `cascadeFactory=true`: DELETE + upsert по всему замыканию фабрик.

## Верификация
1. Сборка под Java 21: `./gradlew :core:compileJava` (через `!` — нет Java 21 в окружении).
2. Flyway: `\d twin_factory_multiplier_filter` → `ON DELETE CASCADE`.
3. Юнит-тесты: `SqlBuilder.buildUpsert` (PK detection, SET clause); `appendClearElementsSql` (порядок DELETE, skip-CS).
4. Интеграционный тест `/private/factory/export/sql/v1` (`BaseIntegrationTest`): `clearElements=true` + все include → 6 DELETE + upsert INSERT; изменить definition фабрики в выгрузке, перезалить → updated (не stale).
5. Ручная заливка на дев-БД: нет FK violation; повторный импорт обновляет definition.

## Ссылки на код (для возврата)
- `core/src/main/java/org/cambium/common/sql/SqlBuilder.java` — `buildInsert`/`buildInserts` (L26-63), `extractMetadata` (L150-214), `EntityMetadata` record, `resolveTableName` (L223-232).
- `core/src/main/java/org/twins/core/service/factory/FactoryExportService.java` — оркестратор (главный метод L92+).
- `core/src/main/java/org/twins/core/service/EntityExportService.java` — базовый класс, `exportChildrenKit`.
- `core/src/main/java/org/twins/core/service/sql/I18nSqlBuilder.java` — `buildI18nInsert`.
- FK источники: `V1.3.135.03:22-23` (multiplier_filter→multiplier, RESTRICT), `V1.3.98.01:8037` (pipeline→CS, RESTRICT), `V1.4.138.01` (CS.twin_factory_id, логический FK).
