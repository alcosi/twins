# План: Добавление сортировки во все search API (кроме twin/search)

## Контекст

Пилот реализован на `DomainBusinessAccountUserSearchController` (TWINS-831). Паттерн основан на JPA Specification вместо Pageable-сортировки (причина: [Spring Data JPA #2253](https://github.com/spring-projects/spring-data-jpa/issues/2253) — дублирующий JOIN при Specification + Pageable.getSort()). Подробная архитектура: `docs/api_sorting_architecture.md`.

## Связанные документы

* `docs/api_sorting_architecture.md` — полная архитектура сортировки
* `docs/api_counting_architecture.md` — полная архитектура count API с группировкой

## Прогресс реализации

> Статус сверен с кодом (наличие `SearchService extends EntitySearchService` + `*CountController` + `sortField` в `SearchRqDTO`).

**✅ DONE (18 API):** attachment, comment, twin_class, twin_class_fields, twin_status, link, data_list_option, domain_business_account, domain_business_account_user (пилот), factory, factory_branch, factory_condition, factory_condition_set, factory_eraser, factory_multiplier, factory_multiplier_filter, factory_pipeline, factory_pipeline_step.
*(Вне плана, но тоже готовы: twin_pointer TWINS-880, twin.)*

**⬜ Осталось (~40 API):** i18n_translation; permission (7); projection, projection_type, scheduler, scheduler_log, tier, transition_trigger (6); twin_class_field_rule/condition/schema/dynamic_marker/freeze (5); twinflow_schema, twinflow/factory, twin_factory/trigger, twin_status/trigger, twin_trigger, twin_trigger_task (6); notification_schema, history_notification, history_notification_recipient, history_notification_recipient_collector (4); space_role, user_group, user_group/involve_assignee, user_group/involve_act_as_user, user, domain/user (6); featurer, data_list, data_list_option_projection, twin_validator_set, action_restriction_reason (5).

**Следующий по порядку:** `i18n_translation`.

**Замечание по версиям DTO:** при миграции часть сущностей получила `SearchRqDTOv2` (новая структура: `search` (SearchDTO-обёртка) + inline `sortField`/`sortDirection`, v1 оставлен для обратной совместимости) — attachment, comment, twin_status, link, data_list_option, twin_class, twin_class_fields. Остальные мигрированы в рамках существующего `SearchRqDTOv1` (factory-группа, domain_business_account).

## Паттерн (на примере DomainBusinessAccountUser)

Для каждой сущности нужно выполнить **8 изменений** (search + count делаются вместе):

1. **SortField enum** (новый файл) → `enums.sort/{Entity}SortField.java` — простой enum без fieldPath, чистый список имён. Swagger автоматически показывает dropdown.
2. **GroupField enum** (новый файл) → `enums.sort/{Entity}GroupField.java` — enum с **прямыми** полями Entity (без JOIN). Только низкокардинальные: UUID FK, enum, boolean, Integer FK к Featurer. **НЕ добавлять** Timestamp/Date/String поля — высокая кардинальность, GROUP BY бессмысленен (см. правила ниже).
3. **SearchRqDTO** (изменить) → добавить `sortField` + `sortDirection` inline на уровне RqDTO (НЕ внутри SearchDTO). Jackson десериализует enum из JSON, невалидное значение → 400. SearchDTO остаётся чистым (критерии поиска без sort) — переиспользуется в count API.
4. **Domain Search Object** (изменить) → extends `EntitySearch<E>` (маркерный базовый класс). Sort-поля в него **НЕ добавляются**.
5. **SearchService** (изменить) → **ОБЯЗАТЕЛЬНО** extends `EntitySearchService<S, E, SF, GF>`. Реализовать ВСЕ абстрактные методы: `createFilterSpecification()`, `createSortSpecification()`, `convertToEntityField()`, `mapGroupedField()`, `jpaSpecificationExecutor()`, `emptySearch()`, `entityClass()`, `newEntity()`. Sort-поля передаются как параметры в `search()`, а не хранятся в search object. Service **НЕ** может наследовать другой класс (Java не поддерживает множественное наследование) — если текущий сервис наследует EntitySecureFindServiceImpl, CRUD нужно вынести в отдельный ConfigService.
6. **Count DTOs** (новые файлы):
    * `{Entity}CountRqDTOv1` — `search` (тот же SearchDTO, что и в search) + `Set<{Entity}GroupField> groupFields`
    * `{Entity}CountDTOv1 extends CountDTOv1` — явно объявленные groupable-поля + унаследованный `count`
    * `{Entity}CountRsDTOv1 extends ResponseCountDTOv1` — `List<{Entity}CountDTOv1> counts` (+ унаследованные `pagination`, `relatedObjects`)
7. **Count Mapper** (новый файл) → `{Entity}CountRestDTOMapper extends RestSimpleDTOMapper<CountResult<E, GF>, {Entity}CountDTOv1>`. Conditional loading через `needLoad(mapperContext, mode, src, groupField)` — related object грузится только если соответствующее поле запрошено в `groupFields`. Batch-load в `beforeCollectionConversion()` только для тех group fields, которые реально присутствуют в результате.
8. **Контроллер** (изменить):
    * **search endpoint**: `service.search(search, pagination, sortField, sortDirection)`
    * **count endpoint** (новый): `@SimplePaginationParams SimplePagination pagination` + `service.countByGroupFields(search, groupFields, pagination)` → `PaginationResult<CountResult<E, GF>>` → mapper → response. Pagination тут — пагинация групп (total = число уникальных групп), а не записей.

### Сигнатуры из api_counting_architecture.md (кратко)

```java
// EntitySearchService — что даёт базовый класс
public PaginationResult<E> search(S search, SimplePagination pagination, SF sortField, SortDirection sortDirection);
public List<CountResult<E, GF>> countByGroupFields(S search, Set<GF> groupFields);                                  // без пагинации
public PaginationResult<CountResult<E, GF>> countByGroupFields(S search, Set<GF> groupFields, SimplePagination pagination); // с пагинацией

// CountResult — typed-обёртка над Object[]
public class CountResult<E, GF> {
    private E entity;            // частично заполнен — только group-поля
    private Long count;
    private Set<GF> groupFields; // какие поля реально запрошены — для conditional loading в mapper
}

// CountQueryExecutor — группировка через Criteria API (обходит ограничения Specification + GROUP BY)
public <E> List<Object[]> executeGroupedCount(Class<E> entityClass, Specification<E> filterSpec, List<String> groupFieldNames);
public <E> Page<Object[]> executeGroupedCountPaginated(Class<E> entityClass, Specification<E> filterSpec, List<String> groupFieldNames, SimplePagination pagination);
```

### Типовой контроллер count endpoint

```java
@PostMapping(value = "/private/{entity}/count/v1")
public ResponseEntity<?> entityCountV1(
        @MapperContextBinding(...) MapperContext mapperContext,
        @SimplePaginationParams SimplePagination pagination,
        @RequestBody {Entity}CountRqDTOv1 request) {
    {Entity}CountRsDTOv1 rs = new {Entity}CountRsDTOv1();
    try {
        var results = searchService.countByGroupFields(
                searchDTOReverseMapper.convert(request.getSearch(), mapperContext),
                request.getGroupFields(), pagination);
        rs.setCounts(countRestDTOMapper.convertCollection(results.getList(), mapperContext))
          .setPagination(paginationMapper.convert(results))
          .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
    } catch (ServiceException se) { return createErrorRs(se, rs); }
      catch (Exception e)        { return createErrorRs(e, rs); }
    return new ResponseEntity<>(rs, HttpStatus.OK);
}
```

**КРИТИЧЕСКИ ВАЖНО:**
- **SearchService ОБЯЗАТЕЛЬНО наследует EntitySearchService** — это обеспечивает единый `search()` и `countByGroupFields()`. Никаких самодельных методов `findXxx()`.
- **Domain Search Object ОБЯЗАТЕЛЬНО наследует EntitySearch<EntityType>** — требование generic-параметра `S extends EntitySearch<E>` в EntitySearchService.
- **Если текущий сервис наследует EntitySecureFindServiceImpl** — вынести его CRUD в отдельный ConfigService, а search-сервис переписать на EntitySearchService.
- **SearchDTO общий для search и count** — sort поля остаются на уровне RqDTO, поэтому SearchDTO чистый и подходит для count без изменений.
- **CountResult<E, GF>** (не `CountResult<E>`) — generic-параметр `GF` нужен для conditional loading в mapper.
- **Pagination в count** — по умолчанию используем paginated overload (он же отдаёт total = число групп). Unpaginated overload — только для внутренних вызовов (валидаторы и т.п.).
- **NULL группируется в отдельную группу** — стандартное поведение PostgreSQL `GROUP BY`.

**Что НЕ нужно делать:**
- Sort-поля **НЕ добавляются** в Domain Search Object — они передаются напрямую из `SearchRqDTO` как параметры `sortField`/`sortDirection` в `EntitySearchService.search()`
- `SortDTOReverseMapper` **НЕ нужен** — Jackson уже десериализует enum из JSON
- GroupField enum **НЕ содержит fieldPath** — маппинг имени → колонка делается в `convertToEntityField()` switch в SearchService (как и для SortField)
- Count DTO **НЕ наследует** Domain Entity DTOv1 — он наследует `CountDTOv1` и явно объявляет только groupable-поля (никаких лишних null-ов от unrelated entity полей)

### `POST /private/twin_class/search/v2` — DONE (search + sort + count)

* **DTO**: `TwinClassDTOv1`
* **SearchRqDTO**: `TwinClassSearchRqDTOv2`
    * Поля фильтрации: `twinClassIdList`, `twinClassKeyLikeList`, `nameI18nLikeList`, `descriptionI18nLikeList`, `externalIdLikeList`, `ownerTypeList`, `markerDatalistIdList`, `tagDatalistIdList`, `freezeIdList`, `abstractt`, `segment`, `hasSegments`, `uniqueName`, `twinflowSchemaSpace`, `twinClassSchemaSpace`, `permissionSchemaSpace`, `aliasSpace`, `assigneeRequired`, `viewPermissionIdList`, `createPermissionIdList`, `editPermissionIdList`, `deletePermissionIdList`, `twinCounterRange`
* **Entity**: `TwinClassEntity`
* **Service**: `TwinClassSearchService extends EntitySearchService`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `createdAt`
    * `externalId`
    * `ownerType`
    * `twinCounter`
    * `abstractt`
    * `segment`
    * `uniqueName`
    * `headTwinClassId`
    * `extendsTwinClassId`
    * `markerDataListId`
    * `tagDataListId`
    * `twinflowSchemaSpace`
    * `twinClassSchemaSpace`
    * `aliasSpace`
    * `viewPermissionId`
    * `headHunterFeaturerId`
    * `editPermissionId`
    * `deletePermissionId`
    * `assigneeRequired`
    * `hasDynamicMarkers`
    * `breadCrumbsFaceId`
    * `pageFaceId`

* **GroupField values** (count API):
    * `ownerType`
    * `abstractt`
    * `segment`
    * `twinClassFreezeId`
    * `headTwinClassId`
    * `extendsTwinClassId`
    * `markerDataListId`
    * `tagDataListId`
    * `twinflowSchemaSpace`
    * `twinClassSchemaSpace`
    * `aliasSpace`
    * `viewPermissionId`
    * `headHunterFeaturerId`
    * `editPermissionId`
    * `deletePermissionId`
    * `assigneeRequired`
    * `uniqueName`
    * `hasDynamicMarkers`
    * `breadCrumbsFaceId`
    * `pageFaceId`

* **New filter fields** (added to TwinClassSearchDTOv1 + TwinClassSearch):
    * `headTwinClassIdList` / `headTwinClassIdExcludeList`
    * `extendsTwinClassIdList` / `extendsTwinClassIdExcludeList`
    * `headHunterFeaturerIdList`
    * `hasDynamicMarkers`
    * `breadCrumbsFaceIdList` / `breadCrumbsFaceIdExcludeList`
    * `pageFaceIdList` / `pageFaceIdExcludeList`

* **Примечание:** `twinClassFreezeName` (i18n-join) не реализован — в TwinClassFreezeEntity отсутствует @ManyToOne связь nameI18n к I18nEntity.

---

## Реестр API и полей сортировки/группировки

Формат: **API endpoint** → **DTO ответа** → **Entity** → `SortField values` + `GroupField values`.

Поля **сортировки** определяются по **колонкам Entity** и разбиты на категории:

1. **Прямые поля** — скалярные колонки Entity (Timestamp, String, Integer/Long, Boolean, Enum).
   Исключаются: `id`, UUID FK, Integer FK к Featurer, Map/hstore, Set/List, URL, длинный текст.

2. **I18n-поля** `(i18n)` — `name`/`description` для Entity, у которых нет прямой колонки,
   а есть только `nameI18nId`/`descriptionI18nId` (UUID FK → I18nEntity).
   Сортировка через JOIN к `I18nTranslationEntity` по локали пользователя → `translation`.

3. **Join-поля** `(join)` — FK (UUID @ManyToOne или Integer FK к Featurer) к сущности с `name`.
   Если целевая сущность использует I18n — помечаются `(i18n-join)` — трёхуровневый JOIN через I18nTranslation.

Первый элемент в списке полей сортировки — default.

### Правила вывода GroupField values

GroupField — это список полей, по которым имеет смысл делать **GROUP BY** (count API). Главное правило: **только низкокардинальные прямые поля Entity** (без JOIN).

**✅ Включать:**
- **UUID FK** (`userId`, `businessAccountId`, `twinClassId`, `permissionSchemaId`, ...) — типичный use case: «посчитать записи по бизнес-аккаунту/классу/пользователю»
- **Integer FK к Featurer** (`fieldTyperFeaturerId`, `triggerFeaturerId`, ...) — featurer-ов ограниченное количество
- **Enum** (`type`, `status`, `key` если это enum-колонка, `logicOperator`, `nextFactoryLimitScope`, ...)
- **Boolean** (`active`, `required`, `inheritable`, `system`, `custom`, `optional`, `cachable`, `invert`, `async`, `logEnabled`, `exclude`, ...)

**❌ Исключать:**
- **Timestamp/Date** (`createdAt`, `updatedAt`, `lastActivityAt`, `addedAt`, `grantedAt`, `doneAt`, `changedAt`, `executionTime`) — высокая кардинальность, каждый GROUP = отдельная запись
- **String** (`name`, `key`, `description`, `title`, `externalId`, `cron`, `backgroundColor`, `fontColor`, ...) — слишком много уникальных значений; плюс для name/key/description обычно есть i18n/JOIN-вариант, который для GROUP BY не подходит
- **Числовые scalar** (`order`, `size`, `twinCounter`, `fixedRate`, `attachmentsStorageQuotaCount`, ...) — обычно business-value от группировки по ним низкое
- **JOIN-поля и i18n-поля** — `xxxName`, `xxxName(i18n)` — GROUP BY через JOIN не поддерживается архитектурой (только прямые поля Entity)

**Практический алгоритм для каждой сущности:**
1. Взять SortField values.
2. Убрать все скалярные строки/timestamp/имена через JOIN.
3. Убрать числовые scalar (order, size и т.п.) — кроме случаев, где это явный enum.
4. Если в SortField не оказалось UUID FK (потому что они там не нужны для сортировки) — вывести их из **полей фильтрации** (например, фильтр `userIdList` → group field `userId`).
5. Проверить, что для каждого group field в Entity есть прямая колонка (не SpecOnly collection, не Map).

**Дополнительно:** если в фильтре есть `xxxIdList`, это сильный сигнал, что `xxxId` — нужный group field (клиенты хотят знать распределение по этому измерению).

**Composite indexes для частых combo.** Если есть частый бизнес-сценарий «count по паре полей» (например, `userId + businessAccountId`), добавить composite-индекс под этот случай. Для каждого такого комбо — отдельная строка в секции индексов у соответствующего API.

---

### `POST /private/attachment/search/v1` — DONE (search + sort + count)

* **DTO**: `AttachmentDTOv1`
* **SearchRqDTO**: `AttachmentSearchRqDTOv1`
    * Поля фильтрации:  `twinIdList`, `twinflowTransitionIdList`, `commentIdList`, `twinClassFieldIdList`, `storageLinkLikeList`, `viewPermissionIdList`, `createdByUserIdList`, `externalIdLikeList`, `titleLikeList`, `descriptionLikeList`, `createdAt`, `order`
* **Entity**: `TwinAttachmentEntity`
* **SortField values**:

    * `createdAt`
    * `title`
    * `description`
    * `externalId`
    * `size`
    * `order`
    * `twinName`(join: twin → Twin.name)
    * `twinClassFieldName`(i18n-join: twinClassField → TwinClassField.nameI18n → I18nTranslation.translation)
    * `authorUserName`(join: createdByUser → User.name)
    * `twinflowTransitionName`(i18n-join: twinflowTransition → TwinflowTransition.nameI18n → I18nTranslation.translation)
    * `viewPermissionName`(i18n-join: viewPermission → Permission.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `twinId`
    * `twinflowTransitionId`
    * `commentId`
    * `twinClassFieldId`
    * `viewPermissionId`
    * `createdByUserId`

* **Пропущенные поля фильтрации:**
    * `commentIdList` → пропущено: у TwinCommentEntity нет осмысленного поля `name` для сортировки
    * `storageLinkLikeList` → пропущено: сортировка по URL не имеет практического смысла

### `POST /private/comment/search/v1` — DONE (search + sort + count)

* **DTO**: `CommentDTOv1`
* **SearchRqDTO**: `CommentSearchRqDTOv1`
    * Поля фильтрации:  `twinIdList`, `createdByUserIdList`, `textLikeList`, `createdAt`, `updatedAt`
* **Entity**: `TwinCommentEntity`
* **SortField values**:

    * `createdAt`
    * `changedAt`
    * `authorUserName`(join: createdByUser → User.name)
    * `twinName`(join: twin → Twin.name)

* **GroupField values**:
    * `twinId`
    * `createdByUserId`

* **Пропущенные поля фильтрации:**
    * `textLikeList` → пропущено: сортировка по тексту комментария не имеет практического смысла

### `POST /private/i18n_translation/search/v1` ✅ reviewed

* **DTO**: `I18nTranslationDTOv1`
* **SearchRqDTO**: `I18nTranslationSearchRqDTOv1`
    * Поля фильтрации: `search` → `i18nIdList`, `translationLikeList`, `localeLikeList`, `usageCounter`
* **Entity**: `I18nTranslationEntity`
* **SortField values**:

    * `locale`
    * `translation`
    * `usageCounter`

* **GroupField values**:
    * `locale` (низкая кардинальность — ограниченный набор языков)
    * `i18nId`

### `POST /private/permission_group/search/v1` ✅ reviewed

* **DTO**: `PermissionGroupDTOv1`
* **SearchRqDTO**: `PermissionGroupSearchRqDTOv1`
    * Поля фильтрации:  `twinClassIdList`, `keyLikeList`, `nameLikeList`, `descriptionLikeList`, `showSystemGroups`
* **Entity**: `PermissionGroupEntity`
* **SortField values**:

    * `key`
    * `name`
    * `description`
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `twinClassId`

### `POST /private/permission/search/v1` ✅ reviewed

* **DTO**: `PermissionDTOv1`
* **SearchRqDTO**: `PermissionSearchRqDTOv1`
    * Поля фильтрации:  `keyLikeList`, `nameLikeList`, `descriptionLikeList`, `groupIdList`
* **Entity**: `PermissionEntity`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `groupName`(join: permissionGroup → PermissionGroup.name)

* **GroupField values**:
    * `groupId`

### `POST /private/permission_schema/search/v1` ✅ reviewed

* **DTO**: `PermissionSchemaDTOv1`
* **SearchRqDTO**: `PermissionSchemaSearchRqDTOv1`
    * Поля фильтрации:  `nameLikeList`, `descriptionLikeList`, `businessAccountIdList`, `createdByUserIdList`
* **Entity**: `PermissionSchemaEntity`
* **SortField values**:

    * `name`
    * `createdAt`
    * `description`
    * `createdByUserName`(join: createdByUser → User.name)
    * `businessAccountName`(join: businessAccount → BusinessAccount.name)

* **GroupField values**:
    * `businessAccountId`
    * `createdByUserId`

### `POST /private/permission_grant/user/search/v1` ✅ reviewed

* **DTO**: `PermissionGrantUserDTOv1`
* **SearchRqDTO**: `PermissionGrantUserSearchRqDTOv1`
    * Поля фильтрации:  `permissionIdList`, `permissionSchemaIdList`, `userIdList`, `grantedByUserIdList`
* **Entity**: `PermissionGrantUserEntity`
* **SortField values**:

    * `grantedAt`
    * `permissionName`(i18n-join: permission → Permission.nameI18n → I18nTranslation.translation)
    * `permissionSchemaName`(join: permissionSchema → PermissionSchema.name)
    * `userName`(join: user → User.name)
    * `grantedByUserName`(join: grantedByUser → User.name)

* **GroupField values**:
    * `permissionId`
    * `permissionSchemaId`
    * `userId`
    * `grantedByUserId`
* **Composite index:** `(permissionSchemaId, userId)` — частый кейс «сколько грантов у user в схеме»

### `POST /private/permission_grant/user_group/search/v1` ✅ reviewed

* **DTO**: `PermissionGrantUserGroupDTOv1`
* **SearchRqDTO**: `PermissionGrantUserGroupSearchRqDTOv1`
    * Поля фильтрации:  `permissionSchemaIdList`, `permissionIdList`, `userGroupIdList`, `grantedByUserIdList`
* **Entity**: `PermissionGrantUserGroupEntity`
* **SortField values**:

    * `grantedAt`
    * `permissionName`(i18n-join: permission → Permission.nameI18n → I18nTranslation.translation)
    * `permissionSchemaName`(join: permissionSchema → PermissionSchema.name)
    * `userGroupName`(i18n-join: userGroup → UserGroup.nameI18n → I18nTranslation.translation)
    * `grantedByUserName`(join: grantedByUser → User.name)

* **GroupField values**:
    * `permissionId`
    * `permissionSchemaId`
    * `userGroupId`
    * `grantedByUserId`

### `POST /private/permission_grant/twin_role/search/v1` ✅ reviewed

* **DTO**: `PermissionGrantTwinRoleDTOv1`
* **SearchRqDTO**: `PermissionGrantTwinRoleSearchRqDTOv1`
    * Поля фильтрации:  `permissionSchemaIdList`, `permissionIdList`, `twinClassIdList`, `isAssignee`, `isSpaceAssignee`, `isCreator`, `isSpaceCreator`, `grantedByUserIdList`
* **Entity**: `PermissionGrantTwinRoleEntity`
* **SortField values**:

    * `grantedAt`
    * `permissionName`(i18n-join: permission → Permission.nameI18n → I18nTranslation.translation)
    * `permissionSchemaName`(join: permissionSchema → PermissionSchema.name)
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `grantedToAssignee`
    * `grantedToSpaceAssignee`
    * `grantedToCreator`
    * `grantedToSpaceCreator`
    * `grantedByUserName`(join: grantedByUser → User.name)

* **GroupField values**:
    * `permissionId`
    * `permissionSchemaId`
    * `twinClassId`
    * `grantedToAssignee`
    * `grantedToSpaceAssignee`
    * `grantedToCreator`
    * `grantedToSpaceCreator`
    * `grantedByUserId`

### `POST /private/permission_grant/space_role/search/v1` ✅ reviewed

* **DTO**: `PermissionGrantSpaceRoleDTOv1`
* **SearchRqDTO**: `PermissionGrantSpaceRoleSearchRqDTOv1`
    * Поля фильтрации:  `permissionSchemaIdList`, `permissionIdList`, `spaceRoleIdList`, `grantedByUserIdList`
* **Entity**: `PermissionGrantSpaceRoleEntity`
* **SortField values**:

    * `grantedAt`
    * `permissionName`(i18n-join: permission → Permission.nameI18n → I18nTranslation.translation)
    * `permissionSchemaName`(join: permissionSchema → PermissionSchema.name)
    * `grantedByUserName`(join: grantedByUser → User.name)
    * `spaceRoleName`(i18n-join: spaceRole → SpaceRole.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `permissionId`
    * `permissionSchemaId`
    * `spaceRoleId`
    * `grantedByUserId`

### `POST /private/projection/search/v1` ✅ reviewed

* **DTO**: `ProjectionDTOv1`
* **SearchRqDTO**: `ProjectionSearchRqDTOv1`
    * Поля фильтрации: `srcTwinPointerIdList`, `srcTwinClassFieldIdList`, `dstTwinClassIdList`, `dstTwinClassFieldIdList`, `projectionTypeIdList`, `fieldProjectorIdList`, `active`
* **Entity**: `ProjectionEntity`
* **SortField values**:

    * `active`
    * `srcTwinClassFieldName`(i18n-join: srcTwinClassField → TwinClassField.nameI18n → I18nTranslation.translation)
    * `dstTwinClassName`(i18n-join: dstTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `dstTwinClassFieldName`(i18n-join: dstTwinClassField → TwinClassField.nameI18n → I18nTranslation.translation)
    * `projectionTypeName`(join: projectionType → ProjectionType.name)
    * `fieldProjectorFeaturerName`(join: fieldProjectorFeaturer → Featurer.name)

* **GroupField values**:
    * `srcTwinClassFieldId`
    * `dstTwinClassId`
    * `dstTwinClassFieldId`
    * `projectionTypeId`
    * `fieldProjectorFeaturerId`
    * `active`

* **Пропущенные поля фильтрации:**
    * `srcTwinPointerIdList` → пропущено: TwinPointer не имеет осмысленного name для сортировки

### `POST /private/projection_type/search/v1` ✅ reviewed

* **DTO**: `ProjectionTypeDTOv1`
* **SearchRqDTO**: `ProjectionTypeSearchRqDTOv1`
    * Поля фильтрации: `keyLikeList`, `nameLikeList`, `projectionTypeGroupIdList`, `membershipTwinClassIdList`
* **Entity**: `ProjectionTypeEntity`
* **SortField values**:

    * `key`
    * `name`
    * `projectionTypeGroupName`(join: projectionTypeGroup → ProjectionTypeGroup.key)
    * `membershipTwinClassName`(i18n-join: membershipTwinClass → TwinClass.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `projectionTypeGroupId`
    * `membershipTwinClassId`

### `POST /private/scheduler/search/v1` ✅ reviewed

* **TODO:** добавить поле `name` в `SchedulerEntity`, `SchedulerDTOv1`, `SchedulerSearchDTOv1`
* **DTO**: `SchedulerDTOv1`
* **SearchRqDTO**: `SchedulerSearchRqDTOv1`
    * Поля фильтрации: `domainIdSet`, `featurerIdSet`, `active`, `logEnabled`, `cronLikeSet`, `fixedRateRange`, `descriptionLikeSet`, `createdAtRange`, `updatedAtRange`
* **Entity**: `SchedulerEntity`
* **SortField values**:

    * `createdAt`
    * `updatedAt`
    * `name`
    * `active`
    * `logEnabled`
    * `description`
    * `cron`
    * `fixedRate`
    * `schedulerFeaturerName`(join: schedulerFeaturer → Featurer.name)

* **GroupField values**:
    * `schedulerFeaturerId`
    * `domainId`
    * `active`
    * `logEnabled`

### `POST /private/scheduler_log/search/v1` ✅ reviewed

* **TODO:** зависит от добавления `name` в `SchedulerEntity` (см. scheduler/search)
* **DTO**: `SchedulerLogDTOv1`
* **SearchRqDTO**: `SchedulerLogSearchRqDTOv1`
    * Поля фильтрации: `schedulerIdSet`, `createdAt`, `resultLikeSet`, `executionTimeRange`
* **Entity**: `SchedulerLogEntity`
* **SortField values**:

    * `createdAt`
    * `executionTime`
    * `result`
    * `schedulerName`(join: scheduler → Scheduler.name)

* **GroupField values**:
    * `schedulerId`
    * `result` (если enum-like: success/failure/timeout — низкая кардинальность)

### `POST /private/tier/search/v1` ✅ reviewed

* **DTO**: `TierDTOv1`
* **SearchRqDTO**: `TierSearchRqDTOv1`
    * Поля фильтрации:  `permissionSchemaIdList`, `twinflowSchemaIdList`, `twinclassSchemaIdList`, `nameLikeList`, `descriptionLikeList`, `attachmentsStorageQuotaCountRange`, `attachmentsStorageQuotaSizeRange`, `userCountQuotaRange`, `custom`
* **Entity**: `TierEntity`
* **SortField values**:

    * `name`
    * `createdAt`
    * `updatedAt`
    * `description`
    * `custom`
    * `attachmentsStorageQuotaCount`
    * `attachmentsStorageQuotaSize`
    * `userCountQuota`
    * `permissionSchemaName`(join: permissionSchema → PermissionSchema.name)
    * `twinflowSchemaName`(join: twinflowSchema → TwinflowSchema.name)
    * `twinClassSchemaName`(join: twinClassSchema → TwinClassSchema.name)

* **GroupField values**:
    * `permissionSchemaId`
    * `twinflowSchemaId`
    * `twinClassSchemaId`
    * `custom`

### `POST /private/transition_trigger/search/v1` ✅ reviewed

* **DTO**: `TransitionTriggerDTOv1`
* **SearchRqDTO**: `TransitionTriggerSearchRqDTOv1`
    * Поля фильтрации: `twinflowTransitionIdList`, `twinTriggerIdList`, `active`, `async`
* **Entity**: `TwinflowTransitionTriggerEntity`
* **SortField values**:

    * `order`
    * `active`
    * `async`
    * `twinflowTransitionName`(i18n-join: twinflowTransition → TwinflowTransition.nameI18n → I18nTranslation.translation)
    * `twinTriggerName`(join: twinTrigger → TwinTrigger.name)

* **GroupField values**:
    * `twinflowTransitionId`
    * `twinTriggerId`
    * `active`
    * `async`

### `POST /private/twin_class_fields/search/v1` — DONE (search + sort + count)

* **DTO**: `TwinClassFieldDTOv1`
* **SearchRqDTO**: `TwinClassFieldSearchRqDTOv1`
    * Поля фильтрации:  `twinClassIdMap`, `keyLikeList`, `nameI18nLikeList`, `descriptionI18nLikeList`, `externalIdLikeList`, `fieldTyperIdList`, `fieldInitiatorIdList`, `twinSorterIdList`, `viewPermissionIdList`, `editPermissionIdList`, `required`, `inheritable`, `system`, `dependentField`, `hasDependentFields`, `projectionField`, `hasProjectionFields`, `orderRange`
* **Entity**: `TwinClassFieldEntity`
* **SortField values**:

    * `order`
    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `externalId`
    * `required`
    * `inheritable`
    * `system`
    * `dependentField`
    * `hasDependentFields`
    * `projectionField`
    * `hasProjectedFields`
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `fieldTyperFeaturerName`(join: fieldTyperFeaturer → Featurer.name)
    * `fieldInitializerFeaturerName`(join: fieldInitializerFeaturerId → Featurer.name)
    * `twinSorterFeaturerName`(join: twinSorterFeaturerId → Featurer.name)
    * `viewPermissionName`(i18n-join: viewPermission → Permission.nameI18n → I18nTranslation.translation)
    * `editPermissionName`(i18n-join: editPermission → Permission.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `twinClassId`
    * `fieldTyperFeaturerId`
    * `fieldInitializerFeaturerId`
    * `twinSorterFeaturerId`
    * `viewPermissionId`
    * `editPermissionId`
    * `required`
    * `inheritable`
    * `system`
    * `dependentField`
    * `hasDependentFields`
    * `projectionField`
    * `hasProjectedFields`

* **Пропущенные поля фильтрации:**
    * `feValidationErrorI18nLikeList` → пропущено: текст ошибки валидации, не имеет смысла для сортировки
    * `beValidationErrorI18nLikeList` → пропущено: текст ошибки валидации, не имеет смысла для сортировки

### `POST /private/twin_class_field_rule/search/v1` ✅ reviewed

* **DTO**: `TwinClassFieldRuleDTOv1`
* **SearchRqDTO**: `TwinClassFieldRuleSearchRqDTOv1`
    * Поля фильтрации: `twinClassFieldIdList`, `fieldOverwriterFeaturerIdList`
* **Entity**: `TwinClassFieldRuleEntity`
* **SortField values**:

    * `rulePriority`
    * `overwrittenRequired`
    * `twinClassFieldName`(i18n-join: twinClassField → TwinClassField.nameI18n → I18nTranslation.translation)
    * `fieldOverwriterFeaturerName`(join: fieldOverwriterFeaturer → Featurer.name)

* **GroupField values**:
    * `twinClassFieldId`
    * `fieldOverwriterFeaturerId`
    * `overwrittenRequired`

### `POST /private/twin_class_field_condition/search/v1` ✅ reviewed

* **DTO**: `TwinClassFieldConditionDTOv1`
* **SearchRqDTO**: `TwinClassFieldConditionSearchRqDTOv1`
    * Поля фильтрации: `twinClassFieldRuleIdList`, `baseTwinClassFieldIdList`, `parentTwinClassFieldConditionIdList`, `logicOperatorIdList`, `conditionEvaluatorFeaturerIdList`
* **Entity**: `TwinClassFieldConditionEntity`
* **SortField values**:

    * `conditionOrder`
    * `baseTwinClassFieldName`(i18n-join: baseTwinClassField → TwinClassField.nameI18n → I18nTranslation.translation)
    * `conditionEvaluatorFeaturerName`(join: conditionEvaluatorFeaturer → Featurer.name)
    * `logicOperator`

* **GroupField values**:
    * `twinClassFieldRuleId`
    * `baseTwinClassFieldId`
    * `parentTwinClassFieldConditionId`
    * `logicOperatorId`
    * `conditionEvaluatorFeaturerId`

* **Пропущенные поля фильтрации:**
    * `twinClassFieldRuleIdList` → пропущено: у TwinClassFieldRuleEntity нет осмысленного name для сортировки
    * `parentTwinClassFieldConditionIdList` → пропущено: self-reference, сортировка не имеет смысла

### `POST /private/twin_class_schema/search/v1` ✅ reviewed

* **DTO**: `TwinClassSchemaDTOv1`
* **SearchRqDTO**: `TwinClassSchemaSearchRqDTOv1`
    * Поля фильтрации: `nameLikeList`, `descriptionLikeList`, `createdByUserIdList`, `createdAt`
* **Entity**: `TwinClassSchemaEntity`
* **SortField values**:

    * `name`
    * `createdAt`
    * `description`
    * `createdByUserName`(join: createdByUser → User.name)

* **GroupField values**:
    * `createdByUserId`

### `POST /private/twin_class_dynamic_marker/search/v1` ✅ reviewed

* **DTO**: `TwinClassDynamicMarkerDTOv1`
* **SearchRqDTO**: `TwinClassDynamicMarkerSearchRqDTOv1`
    * Поля фильтрации: `twinClassIdMap`, `inheritable`, `twinValidatorSetIdList`, `markerDataListOptionIdList`
* **Entity**: `TwinClassDynamicMarkerEntity`
* **SortField values**:

    * `inheritable`
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `twinValidatorSetName`(join: twinValidatorSet → TwinValidatorSet.name)
    * `markerName`(join: markerDataListOption → DataListOption.option)

* **GroupField values**:
    * `twinClassId`
    * `inheritable`
    * `twinValidatorSetId`
    * `markerDataListOptionId`

### `POST /private/twin_class_freeze/search/v1` ✅ reviewed

* **DTO**: `TwinClassFreezeDTOv1`
* **SearchRqDTO**: `TwinClassFreezeSearchRqDTOv1`
    * Поля фильтрации: `keyLikeList`, `statusIdList`, `nameLikeList`, `descriptionLikeList`
* **Entity**: `TwinClassFreezeEntity`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `statusName`(i18n-join: twinStatus → TwinStatus.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `statusId`

### `POST /private/twinflow_schema/search/v1` ✅ reviewed

* **DTO**: `TwinflowSchemaDTOv1`
* **SearchRqDTO**: `TwinflowSchemaSearchRqDTOv1`
    * Поля фильтрации:  `nameLikeList`, `descriptionLikeList`, `businessAccountIdList`, `createdByUserIdList`
* **Entity**: `TwinflowSchemaEntity`
* **SortField values**:

    * `name`
    * `createdAt`
    * `description`
    * `createdByUserName`(join: createdByUser → User.name)
    * `businessAccountName`(join: businessAccount → BusinessAccount.name)

* **GroupField values**:
    * `businessAccountId`
    * `createdByUserId`

### `POST /private/twinflow/factory/search/v1` ✅ reviewed

* **DTO**: `TwinflowFactoryDTOv1`
* **SearchRqDTO**: `TwinflowFactorySearchRqDTOv1`
    * Поля фильтрации: `twinflowIdSet`, `factoryIdSet`, `factoryLauncherSet`
* **Entity**: `TwinflowFactoryEntity`
* **SortField values**:

    * `twinFactoryLauncherId` (enum)
    * `twinflowName`(i18n-join: twinflow → Twinflow.nameI18n → I18nTranslation.translation)
    * `factoryName`(i18n-join: factory → TwinFactory.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `twinflowId`
    * `factoryId`
    * `twinFactoryLauncherId`

### `POST /private/twin_factory/trigger/search/v1` ✅ reviewed

* **DTO**: `TwinFactoryTriggerDTOv1`
* **SearchRqDTO**: `TwinFactoryTriggerSearchRqDTOv1`
    * Поля фильтрации: `twinFactoryIdList`, `inputTwinClassIdList`, `twinTriggerIdList`, `active`, `async`
* **Entity**: `TwinFactoryTriggerEntity`
* **SortField values**:

    * `active`
    * `description`
    * `async`
    * `twinFactoryConditionInvert`
    * `inputTwinClassName`(i18n-join: inputTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `twinFactoryName`(i18n-join: twinFactory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `twinFactoryConditionSetName`(join: twinFactoryConditionSet → FactoryConditionSet.name)
    * `twinTriggerName`(join: twinTrigger → TwinTrigger.name)

* **GroupField values**:
    * `twinFactoryId`
    * `inputTwinClassId`
    * `twinTriggerId`
    * `active`
    * `async`
    * `twinFactoryConditionInvert`

### `POST /private/twin_status/search/v1` — DONE (search + sort + count)

* **DTO**: `TwinStatusDTOv1`
* **SearchRqDTO**: `TwinStatusSearchRqDTOv1`
    * Поля фильтрации:  `twinClassIdMap`, `inheritable`, `keyLikeList`, `nameI18nLikeList`, `descriptionI18nLikeList`
* **Entity**: `TwinStatusEntity`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `inheritable`
    * `backgroundColor`
    * `fontColor`
    * `type`
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `twinClassId`
    * `inheritable`
    * `type`

### `POST /private/twin_status/trigger/search/v1` ✅ reviewed

* **DTO**: `TwinStatusTriggerDTOv1`
* **SearchRqDTO**: `TwinStatusTriggerSearchRqDTOv1`
    * Поля фильтрации: `twinStatusIdList`, `incomingElseOutgoing`, `twinTriggerIdList`, `active`, `async`
* **Entity**: `TwinStatusTriggerEntity`
* **SortField values**:

    * `order`
    * `active`
    * `async`
    * `incomingElseOutgoing`
    * `twinStatusName`(i18n-join: twinStatus → TwinStatus.nameI18n → I18nTranslation.translation)
    * `twinTriggerName`(join: twinTrigger → TwinTrigger.name)

* **GroupField values**:
    * `twinStatusId`
    * `twinTriggerId`
    * `active`
    * `async`
    * `incomingElseOutgoing`

### `POST /private/twin_trigger/search/v1` ✅ reviewed

* **DTO**: `TwinTriggerDTOv1`
* **SearchRqDTO**: `TwinTriggerSearchRqDTOv1`
    * Поля фильтрации: `triggerFeaturerIdList`, `active`, `nameLikeList`, `jobTwinClassIdList`
* **Entity**: `TwinTriggerEntity`
* **SortField values**:

    * `name`
    * `description`
    * `active`
    * `jobTwinClassName`(i18n-join: jobTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `triggerFeaturerName`(join: triggerFeaturer → Featurer.name)

* **GroupField values**:
    * `triggerFeaturerId`
    * `active`
    * `jobTwinClassId`

### `POST /private/twin_trigger_task/search/v1` ✅ reviewed

* **DTO**: `TwinTriggerTaskDTOv1`
* **SearchRqDTO**: `TwinTriggerTaskSearchRqDTOv1`
    * Поля фильтрации: `twinIdList`, `twinTriggerIdList`, `previousTwinStatusIdList`, `createdByUserIdList`, `businessAccountIdList`, `statusIdList`
* **Entity**: `TwinTriggerTaskEntity`
* **SortField values**:

    * `createdAt`
    * `doneAt`
    * `statusId`
    * `statusDetails`
    * `twinName`(join: twin → Twin.name)
    * `createdByUserName`(join: createdByUser → User.name)
    * `twinTriggerName`(join: twinTrigger → TwinTrigger.name)
    * `previousTwinStatusName`(i18n-join: previousTwinStatus → TwinStatus.nameI18n → I18nTranslation.translation)
    * `businessAccountName`(join: businessAccount → BusinessAccount.name)

* **GroupField values**:
    * `twinId`
    * `twinTriggerId`
    * `previousTwinStatusId`
    * `createdByUserId`
    * `businessAccountId`
    * `statusId`

### `POST /private/factory/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryDTOv1`
* **SearchRqDTO**: `FactorySearchRqDTOv1`
    * Поля фильтрации:  `keyLikeList`, `nameLikeList`, `descriptionLikeList`
* **Entity**: `TwinFactoryEntity`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `createdAt`
    * `description`(i18n)
    * `createdByUserName`(join: createdByUser → User.name)

* **GroupField values**:
    * `createdByUserId`

### `POST /private/factory_branch/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryBranchDTOv1`
* **SearchRqDTO**: `FactoryBranchSearchRqDTOv1`
    * Поля фильтрации:  `factoryIdList`, `factoryConditionSetIdList`, `nextFactoryIdList`, `descriptionLikeList`, `conditionInvert`, `active`
* **Entity**: `TwinFactoryBranchEntity`
* **SortField values**:

    * `active`
    * `description`
    * `factoryConditionSetInvert`
    * `factoryName`(i18n-join: factory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `nextFactoryName`(i18n-join: nextFactory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `factoryConditionName`(join: factoryCondition → FactoryConditionSet.name)

* **GroupField values**:
    * `factoryId`
    * `factoryConditionSetId`
    * `nextFactoryId`
    * `active`
    * `factoryConditionSetInvert`

### `POST /private/factory_condition/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryConditionDTOv1`
* **SearchRqDTO**: `FactoryConditionSearchRqDTOv1`
    * Поля фильтрации:  `factoryConditionSetIdList`, `conditionerFeaturerIdList`, `descriptionLikeList`, `invert`, `active`
* **Entity**: `TwinFactoryConditionEntity`
* **SortField values**:

    * `active`
    * `description`
    * `invert`
    * `factoryConditionSetName`(join: factoryConditionSet → FactoryConditionSet.name)
    * `conditionerFeaturerName`(join: conditionerFeaturer → Featurer.name)

* **GroupField values**:
    * `factoryConditionSetId`
    * `conditionerFeaturerId`
    * `invert`
    * `active`

### `POST /private/factory_condition_set/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryConditionSetDTOv1`
* **SearchRqDTO**: `FactoryConditionSetSearchRqDTOv1`
    * Поля фильтрации:  `twinFactoryIdList`, `nameLikeList`, `descriptionLikeList`, `cachable`
* **Entity**: `TwinFactoryConditionSetEntity`
* **SortField values**:

    * `name`
    * `description`
    * `createdAt`
    * `updatedAt`
    * `cachable`
    * `createdByUserName`(join: createdByUser → User.name)
    * `twinFactoryName`(i18n-join: twinFactory → TwinFactory.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `twinFactoryId`
    * `cachable`
    * `createdByUserId`

### `POST /private/factory_eraser/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryEraserDTOv1`
* **SearchRqDTO**: `FactoryEraserSearchRqDTOv1`
    * Поля фильтрации:  `factoryIdList`, `inputTwinClassIdList`, `factoryConditionSetIdList`, `conditionInvert`, `descriptionLikeList`, `eraseActionLikeList`, `active`
* **Entity**: `TwinFactoryEraserEntity`
* **SortField values**:

    * `active`
    * `description`
    * `factoryConditionSetInvert`
    * `action`
    * `inputTwinClassName`(i18n-join: inputTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `factoryName`(i18n-join: factory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `factoryConditionSetName`(join: factoryConditionSet → FactoryConditionSet.name)

* **GroupField values**:
    * `factoryId`
    * `inputTwinClassId`
    * `factoryConditionSetId`
    * `factoryConditionSetInvert`
    * `active`
    * `action`

### `POST /private/factory_multiplier/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryMultiplierDTOv1`
* **SearchRqDTO**: `FactoryMultiplierSearchRqDTOv1`
    * Поля фильтрации:  `factoryIdList`, `inputTwinClassIdList`, `multiplierFeaturerIdList`, `descriptionLikeList`, `active`
* **Entity**: `TwinFactoryMultiplierEntity`
* **SortField values**:

    * `active`
    * `description`
    * `inputTwinClassName`(i18n-join: inputTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `factoryName`(i18n-join: factory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `multiplierFeaturerName`(join: multiplierFeaturer → Featurer.name)

* **GroupField values**:
    * `factoryId`
    * `inputTwinClassId`
    * `multiplierFeaturerId`
    * `active`

### `POST /private/factory_multiplier_filter/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryMultiplierFilterDTOv1`
* **SearchRqDTO**: `FactoryMultiplierFilterSearchRqDTOv1`
    * Поля фильтрации:  `factoryIdList`, `factoryMultiplierIdList`, `inputTwinClassIdList`, `factoryConditionSetIdList`, `descriptionLikeList`, `active`, `factoryConditionInvert`
* **Entity**: `TwinFactoryMultiplierFilterEntity`
* **SortField values**:

    * `active`
    * `description`
    * `factoryConditionSetInvert`
    * `inputTwinClassName`(i18n-join: inputTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `factoryConditionSetName`(join: factoryConditionSet → FactoryConditionSet.name)

* **GroupField values**:
    * `factoryMultiplierId`
    * `inputTwinClassId`
    * `factoryConditionSetId`
    * `active`
    * `factoryConditionSetInvert`

* **Пропущенные поля фильтрации:**
    * `factoryIdList` → пропущено: косвенная связь через multiplier, нет прямой FK в Entity
    * `factoryMultiplierIdList` → пропущено: у TwinFactoryMultiplierEntity нет поля `name`

### `POST /private/factory_pipeline/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryPipelineDTOv1`
* **SearchRqDTO**: `FactoryPipelineSearchRqDTOv1`
    * Поля фильтрации:  `factoryIdList`, `inputTwinClassIdList`, `factoryConditionSetIdList`, `outputTwinStatusIdList`, `nextFactoryIdList`, `descriptionLikeList`, `active`, `nextFactoryLimitScope`
* **Entity**: `TwinFactoryPipelineEntity`
* **SortField values**:

    * `active`
    * `description`
    * `factoryConditionSetInvert`
    * `nextFactoryLimitScope`
    * `inputTwinClassName`(i18n-join: inputTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `outputTwinStatusName`(i18n-join: outputTwinStatus → TwinStatus.nameI18n → I18nTranslation.translation)
    * `factoryName`(i18n-join: factory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `nextFactoryName`(i18n-join: nextFactory → TwinFactory.nameI18n → I18nTranslation.translation)
    * `factoryConditionSetName`(join: factoryConditionSet → FactoryConditionSet.name)

* **GroupField values**:
    * `factoryId`
    * `inputTwinClassId`
    * `factoryConditionSetId`
    * `outputTwinStatusId`
    * `nextFactoryId`
    * `active`
    * `nextFactoryLimitScope`
    * `factoryConditionSetInvert`

### `POST /private/factory_pipeline_step/search/v1` — DONE (search + sort + count)

* **DTO**: `FactoryPipelineStepDTOv1`
* **SearchRqDTO**: `FactoryPipelineStepSearchRqDTOv1`
    * Поля фильтрации:  `factoryIdList`, `factoryPipelineIdList`, `factoryConditionSetIdList`, `descriptionLikeList`, `fillerFeaturerIdList`, `conditionInvert`, `active`, `optional`
* **Entity**: `TwinFactoryPipelineStepEntity`
* **SortField values**:

    * `order`
    * `active`
    * `description`
    * `optional`
    * `factoryConditionInvert`
    * `factoryConditionSetName`(join: factoryConditionSet → FactoryConditionSet.name)
    * `fillerFeaturerName`(join: fillerFeaturer → Featurer.name)

* **GroupField values**:
    * `factoryPipelineId`
    * `factoryConditionSetId`
    * `fillerFeaturerId`
    * `active`
    * `optional`
    * `factoryConditionInvert`

* **Пропущенные поля фильтрации:**
    * `factoryIdList` → пропущено: косвенная связь через pipeline, нет прямой FK в Entity
    * `factoryPipelineIdList` → пропущено: у TwinFactoryPipelineEntity нет поля `name`

### `POST /private/notification_schema/search/v1` ✅ reviewed

* **DTO**: `NotificationSchemaDTOv1`
* **SearchRqDTO**: `NotificationSchemaSearchRqDTOv1`
    * Поля фильтрации: `nameLikeList`, `createdByUserIdList`
* **Entity**: `NotificationSchemaEntity`
* **SortField values**:

    * `name`(i18n)
    * `createdAt`
    * `description`(i18n)
    * `createdByUserName`(join: createdByUser → User.name)

* **GroupField values**:
    * `createdByUserId`

### `POST /private/history_notification/search/v1` ✅ reviewed

* **DTO**: `HistoryNotificationDTOv1`
* **SearchRqDTO**: `HistoryNotificationSearchRqDTOv1`
    * Поля фильтрации:  `historyTypeIdList`, `twinClassIdMap`, `twinClassFieldIdList`, `twinValidatorSetIdList`, `twinValidatorSetInvert`, `notificationSchemaIdList`, `historyNotificationRecipientIdList`, `notificationChannelEventIdList`
* **Entity**: `HistoryNotificationEntity`
* **SortField values**:

    * `createdAt`
    * `twinValidatorSetInvert`
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `twinClassFieldName`(i18n-join: twinClassField → TwinClassField.nameI18n → I18nTranslation.translation)
    * `twinValidatorSetName`(join: twinValidatorSet → TwinValidatorSet.name)
    * `notificationSchemaName`(i18n-join: notificationSchema → NotificationSchema.nameI18n → I18nTranslation.translation)
    * `historyNotificationRecipientName`(i18n-join: historyNotificationRecipient → HistoryNotificationRecipient.nameI18n → I18nTranslation.translation)
    * `createdByUserName`(join: createdByUser → User.name)

* **GroupField values**:
    * `historyTypeId`
    * `twinClassId`
    * `twinClassFieldId`
    * `twinValidatorSetId`
    * `twinValidatorSetInvert`
    * `notificationSchemaId`
    * `historyNotificationRecipientId`
    * `notificationChannelEventId`
    * `createdByUserId`

* **Пропущенные поля фильтрации:**
    * `historyTypeIdList` → пропущено: у HistoryTypeEntity нет осмысленного name для сортировки
    * `notificationChannelEventIdList` → пропущено: у NotificationChannelEventEntity нет поля `name`

### `POST /private/history_notification_recipient/search/v1` ✅ reviewed

* **DTO**: `HistoryNotificationRecipientDTOv1`
* **SearchRqDTO**: `HistoryNotificationRecipientSearchRqDTOv1`
    * Поля фильтрации: `nameLikeList`, `descriptionLikeList`
* **Entity**: `HistoryNotificationRecipientEntity`
* **SortField values**:

    * `name`(i18n)
    * `createdAt`
    * `description`(i18n)
    * `createdByUserName`(join: createdByUser → User.name)

* **GroupField values**:
    * `createdByUserId`

### `POST /private/history_notification_recipient_collector/search/v1` ✅ reviewed

* **DTO**: `HistoryNotificationRecipientCollectorDTOv1`
* **SearchRqDTO**: `HistoryNotificationRecipientCollectorSearchRqDTOv1`
    * Поля фильтрации: `recipientIdList`, `recipientResolverFeaturerIdList`, `exclude`
* **Entity**: `HistoryNotificationRecipientCollectorEntity`
* **SortField values**:

    * `exclude`
    * `recipientName`(i18n-join: recipient → HistoryNotificationRecipient.nameI18n → I18nTranslation.translation)
    * `recipientResolverFeaturerName`(join: recipientResolverFeaturer → Featurer.name)

* **GroupField values**:
    * `recipientId`
    * `recipientResolverFeaturerId`
    * `exclude`

### `POST /private/space_role/search/v1` ✅ reviewed

* **DTO**: `SpaceRoleDTOv1`
* **SearchRqDTO**: `SpaceRoleSearchRqDTOv1`
    * Поля фильтрации:  `twinClassIdList`, `businessAccountIdList`, `keyLikeList`, `nameI18nLikeList`, `descriptionI18nLikeList`
* **Entity**: `SpaceRoleEntity`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `twinClassName`(i18n-join: twinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `businessAccountName`(join: businessAccount → BusinessAccount.name)

* **GroupField values**:
    * `twinClassId`
    * `businessAccountId`

### `POST /private/user_group/search/v1` ✅ reviewed

* **DTO**: `UserGroupDTOv2`
* **SearchRqDTO**: `UserGroupSearchRqDTOv1`
    * Поля фильтрации:  `nameI18NLikeList`, `descriptionI18NLikeList`, `typeList`
* **Entity**: `UserGroupEntity`
* **SortField values**:

    * `name`(i18n)
    * `description`(i18n)
    * `type`

* **GroupField values**:
    * `type`

### `POST /private/user_group/involve_assignee/search/v1` ✅ reviewed

* **DTO**: `UserGroupInvolveAssigneeDTOv1`
* **SearchRqDTO**: `UserGroupInvolveAssigneeSearchRqDTOv1`
    * Поля фильтрации:  `userGroupIdList`, `propagationTwinClassIdList`, `propagationTwinStatusIdList`, `createdByUserIdList`
* **Entity**: `UserGroupInvolveAssigneeEntity`
* **SortField values**:

    * `addedAt`
    * `machineUserName`(join: machineUser → User.name)
    * `addedByUserName`(join: addedByUser → User.name)
    * `userGroupName`(i18n-join: userGroup → UserGroup.nameI18n → I18nTranslation.translation)
    * `propagationTwinClassName`(i18n-join: propagationTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `propagationTwinStatusName`(i18n-join: propagationTwinStatus → TwinStatus.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `userGroupId`
    * `propagationTwinClassId`
    * `propagationTwinStatusId`

### `POST /private/user_group/involve_act_as_user/search/v1` ✅ reviewed

* **DTO**: `UserGroupInvolveActAsUserDTOv1`
* **SearchRqDTO**: `UserGroupInvolveActAsUserSearchRqDTOv1`
    * Поля фильтрации: `machineUserIdList`, `userGroupIdList`
* **Entity**: `UserGroupInvolveActAsUserEntity`
* **SortField values**:

    * `addedAt`
    * `machineUserName`(join: machineUser → User.name)
    * `addedByUserName`(join: addedByUser → User.name)
    * `userGroupName`(i18n-join: userGroup → UserGroup.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `machineUserId`
    * `userGroupId`

### `POST /private/user/search/v1` ✅ reviewed

* **DTO**: `UserDTOv1`
* **SearchRqDTO**: `UserSearchRqDTOv1`
    * Поля фильтрации: `userNameLikeList`, `userEmailLikeList`, `userGroupIdList`, `statusIdList`
* **Entity**: `UserEntity`
* **SortField values**:

    * `fullName`
    * `email`
    * `createdAt` (уже есть sortField в @SimplePaginationParams — мигрировать)
    * `status`
    * `userGroupName`(i18n-join: userGroup → UserGroup.nameI18n → I18nTranslation.translation)

* **GroupField values**:
    * `statusId`
    * `userGroupId`

### `POST /private/domain/user/search/v1` ✅ reviewed

* **DTO**: `DomainUserDTOv1`
* **SearchRqDTO**: `DomainUserSearchRqDTOv1`
    * Поля фильтрации: `userIdList`, `nameLikeList`, `emailLikeList`, `statusIdList`, `businessAccountIdList`
* **Entity**: `DomainUserEntity`
* **SortField values**:

    * `createdAt`
    * `lastActivityAt`
    * `userName`(join: user → User.name)
    * `businessAccountName`(join: businessAccount → BusinessAccount.name)

* **GroupField values**:
    * `userId`
    * `statusId`
    * `businessAccountId`
* **Composite index:** `(userId, businessAccountId)` — частый кейс «пользователь в нескольких BA»

### `POST /private/domain/business_account/search/v1` — DONE (search + sort + count)

* **DTO**: `DomainBusinessAccountDTOv1`
* **SearchRqDTO**: `DomainBusinessAccountSearchRqDTOv1`
    * Поля фильтрации: `idList`, `businessAccountIdList`, `businessAccountNameLikeList`, `permissionSchemaIdList`, `notificationSchemaIdList`, `twinflowSchemaIdList`, `twinClassSchemaIdList`, `tierIdList`, `storageUsedSizeRange`, `storageUsedCountRange`, `createdAt`
* **Entity**: `DomainBusinessAccountEntity`
* **SortField values**:

    * `createdAt`
    * `businessAccountName`(join: businessAccount → BusinessAccount.name)
    * `permissionSchemaName`(join: permissionSchema → PermissionSchema.name)
    * `twinClassSchemaName`(join: twinClassSchema → TwinClassSchema.name)
    * `twinflowSchemaName`(join: twinflowSchema → TwinflowSchema.name)
    * `notificationSchemaName`(join: notificationSchema → NotificationSchema.name)
    * `tierName`(join: tier → Tier.name)
    * `attachmentsStorageUsedCount`
    * `attachmentsStorageUsedSize`

* **GroupField values**:
    * `permissionSchemaId`
    * `twinClassSchemaId`
    * `twinflowSchemaId`
    * `notificationSchemaId`
    * `tierId`

### `POST /private/domain/business_account_user/search/v1` — УЖЕ РЕАЛИЗОВАНО (пропустить)

### `POST /private/featurer/search/v1` ✅ reviewed

* **DTO**: `FeaturerDTOv1`
* **SearchRqDTO**: `FeaturerSearchRqDTOv1`
    * Поля фильтрации:  `typeIdList`, `nameLikeList`
* **Entity**: `FeaturerEntity`
* **SortField values**:

    * `name`
    * `description`
    * `deprecated` (уже есть sortField в @SimplePaginationParams — мигрировать)

* **GroupField values**:
    * `typeId`
    * `deprecated`

### `POST /private/data_list/search/v1` ✅ reviewed

* **DTO**: `DataListDTOv1`
* **SearchRqDTO**: `DataListSearchRqDTOv1`
    * Поля фильтрации:  `nameLikeList`, `descriptionLikeList`, `keyLikeList`, `optionSearch`, `externalIdLikeList`, `defaultOptionIdList`
* **Entity**: `DataListEntity`
* **SortField values**:

    * `key`
    * `name`(i18n)
    * `description`(i18n)
    * `createdAt`
    * `updatedAt`
    * `externalId`

* **GroupField values**:
    * `defaultOptionId`

### `POST /private/data_list_option/search/v1` — DONE (search + sort + count)

* **DTO**: `DataListOptionDTOv1`
* **SearchRqDTO**: `DataListOptionSearchRqDTOv1` (наследует `DataListOptionSearchDTOv1`)
    * Поля фильтрации:  `dataListIdList`, `dataListKeyList`, `optionLikeList`, `optionI18nLikeList`, `businessAccountIdList`, `dataListSubsetIdList`, `dataListSubsetKeyList`, `statusIdList`, `externalIdLikeList`, `externalIdList`, `validForTwinClassFieldIdList`, `custom`
* **Entity**: `DataListOptionEntity`
* **SortField values**:

    * `name`
    * `createdAt`
    * `externalId`
    * `icon`
    * `status`
    * `backgroundColor`
    * `fontColor`
    * `custom`
    * `dataListName`(i18n-join: dataList → DataList.nameI18n → I18nTranslation.translation)
    * `businessAccountName`(join: businessAccount → BusinessAccount.name) (уже есть sortField в @SimplePaginationParams — мигрировать)

* **GroupField values**:
    * `dataListId`
    * `businessAccountId`
    * `statusId`
    * `custom`

### `POST /private/data_list_option_projection/search/v1` ✅ reviewed

* **DTO**: `DataListOptionProjectionDTOv1`
* **SearchRqDTO**: `DataListOptionProjectionSearchRqDTOv1`
    * Поля фильтрации: `projectionTypeIdList`, `srcDataListOptionIdList`, `dstDataListOptionIdList`, `savedByUserIdList`, `changedAt`
* **Entity**: `DataListOptionProjectionEntity`
* **SortField values**:

    * `changedAt`
    * `savedByUserName`(join: savedByUser → User.name)
    * `projectionTypeName`(join: projectionType → ProjectionType.name)
    * `srcDataListOptionName`(join: srcDataListOption → DataListOption.option)
    * `dstDataListOptionName`(join: dstDataListOption → DataListOption.option)

* **GroupField values**:
    * `projectionTypeId`
    * `srcDataListOptionId`
    * `dstDataListOptionId`
    * `savedByUserId`

### `POST /private/twin_validator_set/search/v1` ✅ reviewed

* **DTO**: `TwinValidatorSetDTOv1`
* **SearchRqDTO**: `TwinValidatorSetSearchRqDTOv1`
    * Поля фильтрации:  `nameLikeList`, `descriptionLikeList`, `invert`
* **Entity**: `TwinValidatorSetEntity`
* **SortField values**:

    * `name`
    * `description`
    * `invert`

* **GroupField values**:
    * `invert`

### `POST /private/link/search/v1` — DONE (search + sort + count)

* **DTO**: `LinkDTOv2`
* **SearchRqDTO**: `LinkSearchRqDTOv1`
    * Поля фильтрации:  `srcTwinClassIdList`, `srcTwinClassInheritable`, `dstTwinClassIdList`, `dstTwinClassInheritable`, `srcOrDstTwinClassIdList`, `forwardNameLikeList`, `backwardNameLikeList`, `typeLikeList`, `strengthLikeList`
* **Entity**: `LinkEntity`
* **SortField values**:

    * `createdAt`
    * `forwardName`
    * `backwardName`
    * `srcTwinClassInheritable`
    * `dstTwinClassInheritable`
    * `type`
    * `linkStrength`
    * `srcTwinClassName`(i18n-join: srcTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `dstTwinClassName`(i18n-join: dstTwinClass → TwinClass.nameI18n → I18nTranslation.translation)
    * `createdByUserName`(join: createdByUser → User.name)

* **GroupField values**:
    * `srcTwinClassId`
    * `dstTwinClassId`
    * `srcTwinClassInheritable`
    * `dstTwinClassInheritable`
    * `type`
    * `createdByUserId`

### `POST /private/action_restriction_reason/search/v1` ✅ reviewed

* **DTO**: `ActionRestrictionReasonDTOv1`
* **SearchRqDTO**: `ActionRestrictionReasonSearchRqDTOv1`
    * Поля фильтрации: `typeLikeList`, `descriptionLikeList`
* **Entity**: `ActionRestrictionReasonEntity`
* **SortField values**:

    * `type`
    * `description`

* **GroupField values**:
    * `type`

---

## Порядок реализации

По пакетам, один за другим. Для каждого API делаем **search + count вместе** (общие SortField/GroupField enum, общий SearchService):

1. ✅ attachment → ✅ comment → ⬜ i18n
2. ⬜ permission (7 сущностей)
3. ⬜ projection → scheduler → tier → transition (6)
4. twinclass: ✅ twin_class + ✅ twin_class_fields → ⬜ rule/condition/schema/dynamic_marker/freeze (5)
5. ⬜ twinflow → ✅ twinstatus → ⬜ trigger (twin_status/trigger, twin_trigger, twin_trigger_task, twin_factory/trigger, twinflow/factory)
6. ✅ factory (9 сущностей)
7. ⬜ notification (4 сущности)
8. ⬜ space → usergroup → user → domain (✅ domain_business_account + ✅ domain_business_account_user; ⬜ остальное)
9. ⬜ system → datalist (✅ data_list_option) → validator → ✅ link → action

**Шаги на каждый API:**
1. Создать `{Entity}SortField` enum
2. Создать `{Entity}GroupField` enum
3. Подготовить Domain Search Object: extends `EntitySearch<EntityType>`
4. Переписать SearchService на `EntitySearchService<S, E, SF, GF>` (если наследовал `EntitySecureFindServiceImpl` — вынести CRUD в `{Entity}ConfigService`)
5. Добавить `sortField`/`sortDirection` в `{Entity}SearchRqDTOv1`
6. Создать `{Entity}CountRqDTOv1` / `{Entity}CountDTOv1` / `{Entity}CountRsDTOv1`
7. Создать `{Entity}CountRestDTOMapper` с conditional loading
8. Изменить search контроллер + добавить count контроллер (`POST /private/{entity}/count/v1` с `@SimplePaginationParams`)
9. Проверить индексы под SortField + GroupField (см. ниже)

## Важные замечания

* **Контроллеры с существующим sortField** — DataListOptionSearchController ✅ мигрирован. Остались: FeaturerSearchController, UserSearchController — нужно мигрировать inline `sortField`/`sortDirection` на новый enum-паттерн
* **Сущности без createdAt** — используют первое поле из списка как default
* **SearchDTOReverseMapper** — маппит только критерии поиска (SearchDTO). Sort-поля (`sortField`/`sortDirection`) передаются напрямую из `SearchRqDTO` в `EntitySearchService.search()` — отдельный mapper не нужен
* **Entity.Fields** — используем константы из Lombok `@FieldNameConstants` для имён полей в switch
* **LEFT JOIN** — `CommonSpecification.toSortSpecification()` использует LEFT, не INNER — чтобы не отсекать записи с NULL-связями
* **getResultType() guard** — обязателен в `toSortSpecification()`, count-запрос не должен содержать ORDER BY
* **Не трогать TwinSorter** — featurer-based механизм сортировки Twin решает другую задачу
* **EntitySearchService** — все новые search services должны extends `EntitySearchService<S, E, SF, GF>` и реализовать ВСЕ абстрактные методы: `createSortSpecification()`, `convertToEntityField()`, `mapGroupedField()`, и т.д.
* **SearchDTO общий для search и count** — не дублируем критерии. Count берёт тот же SearchDTO через Composition в `{Entity}CountRqDTOv1`.
* **CountResult<E, GF>** — generic-параметр `GF` обязателен (для conditional loading в mapper). НЕ `CountResult<E>`.
* **Conditional loading в Count Mapper** — related object грузится только если соответствующее поле есть в `src.getGroupFields()`. Иначе генерируются лишние запросы к БД.
* **Pagination в count** — `total` = число уникальных групп (не записей). Это часто неожиданное поведение для клиентов — задокументировать в Swagger description.

## Индексы БД

### Для сортировки (SortField)

Сортировка по неиндексированному столбцу вызывает full table scan + filesort, что неприемлемо на больших таблицах (O(N log N) деградация, см. `docs/api_sorting_architecture.md` — performance оценка).

1. Проверить наличие индексов через `\d+ <table_name>` или `pg_indexes`
2. Если индекс отсутствует — создать миграцию `V1.xxx.y__TWINS-sort_index_<entity>.sql` с `CREATE INDEX IF NOT EXISTS`
3. Для полей, которые сортируют через JOIN (например `userName` → join к `UserEntity.name`), индекс нужен на стороне присоединяемой таблицы
4. Для JOIN lookup (опционально) — составной индекс по FK+domain_id ускоряет Nested Loop

### Для группировки (GroupField) — МЕНЕЕ КРИТИЧНО, но желательно

GROUP BY без индекса тоже вызывает Sort. Но GROUP BY обычно на низкокардинальных полях (UUID FK / enum / boolean), и PostgreSQL может использовать Hash Aggregate без sort — это приемлемо.

1. Проверить, есть ли уже индекс по FK-колонке (обычно есть — миграции создания FK автоматически создают индекс)
2. Для boolean/enum полей индекс не нужен — низкая селективность, planner всё равно сделает seq scan
3. **Composite indexes** — только для частых combo (обозначены в плане как `**Composite index:**` для конкретных API). Пример:
    ```sql
    CREATE INDEX IF NOT EXISTS idx_dba_user_ba
        ON domain_business_account_user(user_id, business_account_id);
    ```

### Шаблон миграции

```sql
-- V1.xxx.y__TWINS-sort_count_index_<entity>.sql
CREATE INDEX IF NOT EXISTS idx_<entity>_<sort_field> ON <entity_table>(<sort_field>);
-- Для часто используемых combo (если указано в плане):
CREATE INDEX IF NOT EXISTS idx_<entity>_<field1>_<field2> ON <entity_table>(<field1>, <field2>);
```

## Верификация

### Сборка
1. `./gradlew build` — сборка без ошибок
2. `./gradlew test` — тесты проходят

### Search API
3. Все `{Entity}SearchRqDTOv1` содержат inline `sortField` (enum) + `sortDirection`
4. Все search services `extends EntitySearchService<S, E, SF, GF>` и реализуют `createSortSpecification()` со switch по enum
5. Sort-поля НЕ хранятся в Domain Search Object, передаются как параметры в `EntitySearchService.search()`
6. Для каждого SortField есть индекс в БД
7. Swagger показывает dropdown для `sortField`

### Count API
8. Все `{Entity}CountRqDTOv1` содержат `search` (тот же SearchDTO) + `Set<{Entity}GroupField> groupFields`
9. Все `{Entity}CountDTOv1` наследуют `CountDTOv1` и явно объявляют groupable-поля с `@RelatedObject` где уместно
10. Все `{Entity}CountRsDTOv1` наследуют `ResponseCountDTOv1` (включает `pagination` + `relatedObjects`)
11. Все `{Entity}CountRestDTOMapper` реализуют conditional loading через `needLoad(mapperContext, mode, src, groupField)` + batch-load в `beforeCollectionConversion()`
12. Каждый count endpoint принимает `@SimplePaginationParams` и возвращает `pagination.total` = число уникальных групп
13. Для каждого GroupField есть индекс в БД (для FK — обычно уже есть; composite — для помеченных combo)
14. Swagger показывает dropdown для `groupFields` (Set<enum>)

### Sanity checks (запросы)
15. Отправить `groupFields: []` → ожидаем 1 строку с общим `count`
16. Отправить `groupFields: ["xxxId"]` → ожидаем по строке на каждое уникальное значение `xxxId`, `pagination.total` = число уникальных значений
17. Отправить невалидное имя в `groupFields` → ожидаем 400 от Jackson
