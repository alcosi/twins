# План: Добавление сортировки во все search API (кроме twin/search)

## Контекст

Пилот реализован на `DomainBusinessAccountUserSearchController` (TWINS-831). Паттерн основан на JPA Specification вместо Pageable-сортировки (причина: [Spring Data JPA #2253](https://github.com/spring-projects/spring-data-jpa/issues/2253) — дублирующий JOIN при Specification + Pageable.getSort()). Подробная архитектура: `docs/api_sorting_architecture.md`.

## Паттерн (на примере DomainBusinessAccountUser)

Для каждой сущности нужно выполнить 5 изменений:

1. **SortField enum** (новый файл) → `enums.sort/{Entity}SortField.java` — простой enum без fieldPath, чистый список имён. Swagger автоматически показывает dropdown.
2. **SearchRqDTO** (изменить) → добавить `sortField` + `sortDirection` inline на уровне RqDTO (НЕ внутри SearchDTO). Jackson десериализует enum из JSON, невалидное значение → 400.
3. **Domain Search Object** (изменить) → добавить прямые поля `sortField` (enum) + `sortDirection` с default. БЕЗ `SortOption<S>`.
4. **SearchRqDTOReverseMapper** (изменить) → тривиальный маппинг sort-полей. `SortDTOReverseMapper` НЕ нужен.
5. **SearchService** (изменить) → добавить `createSortSpecification()` со switch по enum → `CommonSpecification.toSortSpecification(fieldPath, ascending)` + `pagination.setSort(null)`

Контроллер **не меняется** — `@SimplePaginationParams` остаётся как есть, sort приходит из request body.

## Реестр API и полей сортировки

Формат: **API endpoint** → **DTO ответа** → **Entity** → поля сортировки.

Поля сортировки определяются по **колонкам Entity** и разбиты на категории:

1. **Прямые поля** — скалярные колонки Entity (Timestamp, String, Integer/Long, Boolean, Enum).
   Исключаются: `id`, UUID FK, Integer FK к Featurer, Map/hstore, Set/List, URL, длинный текст.

2. **I18n-поля** `(i18n)` — `name`/`description` для Entity, у которых нет прямой колонки,
   а есть только `nameI18nId`/`descriptionI18nId` (UUID FK → I18nEntity).
   Сортировка через JOIN к `I18nTranslationEntity` по локали пользователя → `translation`.

3. **Join-поля** `(join)` — FK (UUID @ManyToOne или Integer FK к Featurer) к сущности с `name`.
   Если целевая сущность использует I18n — помечаются `(i18n-join)` — трёхуровневый JOIN через I18nTranslation.

Первый элемент в списке полей — default.

---

### `POST /private/attachment/search/v1` ✅ reviewed

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

* **Пропущенные поля фильтрации:**
    * `commentIdList` → пропущено: у TwinCommentEntity нет осмысленного поля `name` для сортировки
    * `storageLinkLikeList` → пропущено: сортировка по URL не имеет практического смысла

### `POST /private/comment/search/v1` ✅ reviewed

* **DTO**: `CommentDTOv1`
* **SearchRqDTO**: `CommentSearchRqDTOv1`
    * Поля фильтрации:  `twinIdList`, `createdByUserIdList`, `textLikeList`, `createdAt`, `updatedAt`
* **Entity**: `TwinCommentEntity`
* **SortField values**:

    * `createdAt`
    * `changedAt`
    * `authorUserName`(join: createdByUser → User.name)
    * `twinName`(join: twin → Twin.name)

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

### `POST /private/twin_class_fields/search/v1` ✅ reviewed

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

### `POST /private/twinflow/factory/search/v1` ✅ reviewed

* **DTO**: `TwinflowFactoryDTOv1`
* **SearchRqDTO**: `TwinflowFactorySearchRqDTOv1`
    * Поля фильтрации: `twinflowIdSet`, `factoryIdSet`, `factoryLauncherSet`
* **Entity**: `TwinflowFactoryEntity`
* **SortField values**:

    * `twinFactoryLauncherId` (enum)
    * `twinflowName`(i18n-join: twinflow → Twinflow.nameI18n → I18nTranslation.translation)
    * `factoryName`(i18n-join: factory → TwinFactory.nameI18n → I18nTranslation.translation)

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

### `POST /private/twin_status/search/v1` ✅ reviewed

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

### `POST /private/factory/search/v1` ✅ reviewed

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

### `POST /private/factory_branch/search/v1` ✅ reviewed

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

### `POST /private/factory_condition/search/v1` ✅ reviewed

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

### `POST /private/factory_condition_set/search/v1` ✅ reviewed

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

### `POST /private/factory_eraser/search/v1` ✅ reviewed

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

### `POST /private/factory_multiplier/search/v1` ✅ reviewed

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

### `POST /private/factory_multiplier_filter/search/v1` ✅ reviewed

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

* **Пропущенные поля фильтрации:**
    * `factoryIdList` → пропущено: косвенная связь через multiplier, нет прямой FK в Entity
    * `factoryMultiplierIdList` → пропущено: у TwinFactoryMultiplierEntity нет поля `name`

### `POST /private/factory_pipeline/search/v1` ✅ reviewed

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

### `POST /private/factory_pipeline_step/search/v1` ✅ reviewed

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

### `POST /private/history_notification_recipient_collector/search/v1` ✅ reviewed

* **DTO**: `HistoryNotificationRecipientCollectorDTOv1`
* **SearchRqDTO**: `HistoryNotificationRecipientCollectorSearchRqDTOv1`
    * Поля фильтрации: `recipientIdList`, `recipientResolverFeaturerIdList`, `exclude`
* **Entity**: `HistoryNotificationRecipientCollectorEntity`
* **SortField values**:

    * `exclude`
    * `recipientName`(i18n-join: recipient → HistoryNotificationRecipient.nameI18n → I18nTranslation.translation)
    * `recipientResolverFeaturerName`(join: recipientResolverFeaturer → Featurer.name)

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

### `POST /private/user_group/search/v1` ✅ reviewed

* **DTO**: `UserGroupDTOv2`
* **SearchRqDTO**: `UserGroupSearchRqDTOv1`
    * Поля фильтрации:  `nameI18NLikeList`, `descriptionI18NLikeList`, `typeList`
* **Entity**: `UserGroupEntity`
* **SortField values**:

    * `name`(i18n)
    * `description`(i18n)
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

### `POST /private/data_list_option/search/v1` ✅ reviewed

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

### `POST /private/twin_validator_set/search/v1` ✅ reviewed

* **DTO**: `TwinValidatorSetDTOv1`
* **SearchRqDTO**: `TwinValidatorSetSearchRqDTOv1`
    * Поля фильтрации:  `nameLikeList`, `descriptionLikeList`, `invert`
* **Entity**: `TwinValidatorSetEntity`
* **SortField values**:

    * `name`
    * `description`
    * `invert`

### `POST /private/link/search/v1` ✅ reviewed

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

### `POST /private/action_restriction_reason/search/v1` ✅ reviewed

* **DTO**: `ActionRestrictionReasonDTOv1`
* **SearchRqDTO**: `ActionRestrictionReasonSearchRqDTOv1`
    * Поля фильтрации: `typeLikeList`, `descriptionLikeList`
* **Entity**: `ActionRestrictionReasonEntity`
* **SortField values**:

    * `type`
    * `description`

---

## Порядок реализации

По пакетам, один за другим:

1. attachment → comment → i18n
2. permission (7 сущностей)
3. projection → scheduler → tier → transition
4. twinclass (7 сущностей)
5. twinflow → twinstatus → trigger
6. factory (9 сущностей)
7. notification (4 сущности)
8. space → usergroup → user → domain
9. system → datalist → validator → link → action

## Важные замечания

* **Контроллеры с существующим sortField** (FeaturerSearchController, DataListOptionSearchController, UserSearchController) — нужно мигрировать inline `sortField`/`sortDirection` на новый enum-паттерн
* **Сущности без createdAt** — используют первое поле из списка как default
* **SearchDTOReverseMapper** — маппит только критерии поиска. Sort-поля маппятся тривиально на уровне RqDTO mapper. `SortDTOReverseMapper` НЕ нужен.
* **Entity.Fields** — используем константы из Lombok `@FieldNameConstants` для имён полей в switch
* **LEFT JOIN** — `CommonSpecification.toSortSpecification()` использует LEFT, не INNER — чтобы не отсекать записи с NULL-связями
* **getResultType() guard** — обязателен в `toSortSpecification()`, count-запрос не должен содержать ORDER BY
* **Не трогать TwinSorter** — featurer-based механизм сортировки Twin решает другую задачу

## Индексы БД

Для каждого SortField нужно убедиться, что в PostgreSQL есть индекс по соответствующему столбцу. Сортировка по неиндексированному столбцу вызывает full table scan + filesort, что неприемлемо на больших таблицах (O(N log N) деградация, см. `docs/api_sorting_architecture.md` — performance оценка).

**Порядок действий для каждой сущности:**

1. Проверить наличие индексов через `\d+ <table_name>` или `pg_indexes`
2. Если индекс отсутствует — создать миграцию `V1.xxx.y__TWINS-sort_index_<entity>.sql` с `CREATE INDEX IF NOT EXISTS`
3. Для полей, которые сортируют через JOIN (например `userName` → join к `UserEntity.name`), индекс нужен на стороне присоединяемой таблицы
4. Для JOIN lookup (опционально) — составной индекс по FK+domain_id ускоряет Nested Loop

## Верификация

1. `./gradlew build` — сборка без ошибок
2. Проверить что все SearchRqDTO содержат inline `sortField` (enum) + `sortDirection`
3. Проверить что все Search domain objects содержат прямые поля `sortField` + `sortDirection` с default
4. Проверить что все search services используют `createSortSpecification()` со switch + `pagination.setSort(null)`
5. Проверить что для каждого SortField есть соответствующий индекс в БД
6. Проверить что Swagger показывает dropdown для sortField (enum-тип)
