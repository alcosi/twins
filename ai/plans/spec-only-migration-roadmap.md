# SpecOnly + load-паттерн: дорожная карта миграции

**Сгенерировано:** 2026-06-23 (автоматический аудит)  
**Конвенции:** `docs/entity_code_convention.md`, `docs/load_method_pattern.md`  
**Всего сущностей с legacy `@ManyToOne`:** 97  
**Уже мигрировано (полный цикл):** 18  

## Легенда

| Статус | Описание |
|---|---|
| done | Полный цикл выполнен |
| partial | Часть полей мигрирована, часть пропущена из-за isEntityReadDenied/business |
| ready | Поля «simple» — можно мигрировать |
| audit | Есть «business»-поля — нужен ручной аудит |
| blocked | Использование в `isEntityReadDenied()` блокирует все оставшиеся поля |

## Классификация полей (эвристическая)

Для каждого legacy-поля прогнан grep по `entity.getField()` и классифицирован по зоне использования.  
**Эвристика может ошибаться** — финальное решение по каждой сущности принимается после ручного grep-аудита в момент миграции.  
Поля с множественным business-использованием часто используют setter в `validateEntity` (что безопасно) — такие случаи отмечены в `validate`, а не в `business`.  

## Дорожная карта (алфавитный порядок)

| # | Entity | Пакет | legacy | simple | business | blocked | bidir | Статус |
|---|---|---|---|---|---|---|---|---|
| 1 | `AttachmentDeleteTaskEntity` | attachment | 1 | 1 | 0 | 0 | 0 | ready |
| 2 | `BusinessAccountEntity` | businessaccount | 1 | 1 | 0 | 0 | 0 | ready |
| 3 | `BusinessAccountUserEntity` | businessaccount | 0 | 0 | 0 | 0 | 0 | done |
| 4 | `DataListOptionEntity` | datalist | 0 | 0 | 0 | 0 | 0 | done |
| 5 | `DataListOptionProjectionEntity` | datalist | 4 | 4 | 0 | 0 | 0 | ready |
| 6 | `DataListOptionSearchPredicateEntity` | datalist | 1 | 1 | 0 | 0 | 0 | ready |
| 7 | `DataListSubsetOptionEntity` | datalist | 2 | 2 | 0 | 0 | 0 | ready |
| 8 | `DomainBusinessAccountEntity` | domain | 0 | 0 | 0 | 0 | 0 | done |
| 9 | `DomainBusinessAccountUserEntity` | domain | 0 | 0 | 0 | 0 | 0 | done |
| 10 | `DomainEntity` | domain | 12 | 12 | 0 | 0 | 0 | ready |
| 11 | `DomainLocaleEntity` | domain | 1 | 1 | 0 | 0 | 0 | ready |
| 12 | `DomainTypeEntity` | domain | 2 | 2 | 0 | 0 | 0 | ready |
| 13 | `DomainTypeTwinClassOwnerTypeEntity` | domain | 1 | 1 | 0 | 0 | 0 | ready |
| 14 | `DomainUserEntity` | domain | 0 | 0 | 0 | 0 | 0 | done |
| 15 | `DraftHistoryEntity` | draft | 2 | 2 | 0 | 0 | 0 | ready |
| 16 | `DraftTwinAttachmentEntity` | draft | 2 | 1 | 1 | 0 | 0 | partial |
| 17 | `DraftTwinEraseEntity` | draft | 2 | 2 | 0 | 0 | 0 | ready |
| 18 | `DraftTwinFieldBooleanEntity` | draft | 1 | 0 | 1 | 0 | 0 | audit |
| 19 | `DraftTwinFieldDataListEntity` | draft | 1 | 0 | 1 | 0 | 0 | audit |
| 20 | `DraftTwinFieldSimpleEntity` | draft | 1 | 0 | 1 | 0 | 0 | audit |
| 21 | `DraftTwinFieldSimpleNonIndexedEntity` | draft | 1 | 0 | 1 | 0 | 0 | audit |
| 22 | `DraftTwinFieldTwinClassEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 23 | `DraftTwinLinkEntity` | draft | 2 | 2 | 0 | 0 | 0 | ready |
| 24 | `DraftTwinMarkerEntity` | draft | 1 | 0 | 1 | 0 | 0 | audit |
| 25 | `DraftTwinPersistEntity` | draft | 2 | 2 | 0 | 0 | 0 | ready |
| 26 | `DraftTwinTagEntity` | draft | 1 | 0 | 1 | 0 | 0 | audit |
| 27 | `EraseflowEntity` | eraseflow | 0 | 0 | 0 | 0 | 0 | done |
| 28 | `EraseflowLinkCascadeEntity` | eraseflow | 0 | 0 | 0 | 0 | 0 | done |
| 29 | `FaceEntity` | face | 0 | 0 | 0 | 0 | 0 | done |
| 30 | `HistoryEntity` | history | 1 | 1 | 0 | 0 | 0 | ready |
| 31 | `HistoryNotificationEntity` | notification | 1 | 1 | 0 | 0 | 0 | ready |
| 32 | `HistoryNotificationTaskEntity` | notification | 1 | 1 | 0 | 0 | 0 | ready |
| 33 | `HistoryTypeDomainTemplateEntity` | history | 0 | 0 | 0 | 0 | 0 | done |
| 34 | `I18nEntity` | i18n | 1 | 0 | 1 | 0 | 0 | audit |
| 35 | `I18nTranslationEntity` | i18n | 1 | 1 | 0 | 0 | 0 | ready |
| 36 | `IdentityProviderInternalUserEntity` | idp | 1 | 0 | 1 | 0 | 0 | audit |
| 37 | `NotificationEmailEntity` | notification | 4 | 4 | 0 | 0 | 0 | ready |
| 38 | `PermissionEntity` | permission | 1 | 1 | 0 | 0 | 0 | ready |
| 39 | `PermissionGrantGlobalEntity` | permission | 0 | 0 | 0 | 0 | 0 | done |
| 40 | `PermissionGrantSpaceRoleEntity` | permission | 1 | 1 | 0 | 0 | 0 | partial |
| 41 | `PermissionGrantTwinRoleEntity` | permission | 4 | 3 | 0 | 1 | 1 | partial |
| 42 | `PermissionGrantUserEntity` | permission | 1 | 1 | 0 | 0 | 0 | partial |
| 43 | `PermissionGrantUserGroupEntity` | permission | 1 | 1 | 0 | 0 | 0 | partial |
| 44 | `PermissionGroupEntity` | permission | 2 | 1 | 1 | 0 | 0 | partial |
| 45 | `PermissionMaterGlobalEntity` | permission | 1 | 0 | 0 | 1 | 1 | blocked |
| 46 | `PermissionMaterSpaceUserEntity` | permission | 1 | 0 | 0 | 1 | 1 | blocked |
| 47 | `PermissionMaterSpaceUserGroupEntity` | permission | 1 | 0 | 0 | 1 | 1 | blocked |
| 48 | `PermissionMaterUserGroupEntity` | permission | 1 | 0 | 0 | 1 | 1 | blocked |
| 49 | `PermissionSchemaEntity` | permission | 2 | 1 | 1 | 0 | 0 | partial |
| 50 | `ProjectionTypeEntity` | projection | 2 | 2 | 0 | 0 | 0 | ready |
| 51 | `ResourceEntity` | resource | 3 | 2 | 1 | 0 | 0 | partial |
| 52 | `SpaceEntity` | space | 4 | 4 | 0 | 0 | 0 | ready |
| 53 | `SpaceRoleEntity` | space | 2 | 2 | 0 | 0 | 0 | ready |
| 54 | `SpaceRoleUserEntity` | space | 0 | 0 | 0 | 0 | 0 | done |
| 55 | `SpaceRoleUserGroupEntity` | space | 0 | 0 | 0 | 0 | 0 | done |
| 56 | `StorageEntity` | resource | 1 | 0 | 1 | 0 | 0 | audit |
| 57 | `TierEntity` | domain | 4 | 4 | 0 | 0 | 0 | ready |
| 58 | `TwinBusinessAccountAliasCounterEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 59 | `TwinChangeTaskEntity` | twin | 1 | 1 | 0 | 0 | 0 | ready |
| 60 | `TwinClassDynamicMarkerEntity` | twinclass | 2 | 2 | 0 | 0 | 0 | ready |
| 61 | `TwinClassEntity` | twinclass | 2 | 1 | 1 | 0 | 0 | partial |
| 62 | `TwinClassFieldConditionEntity` | twinclass | 1 | 1 | 0 | 0 | 0 | ready |
| 63 | `TwinClassFieldEntity` | twinclass | 1 | 1 | 0 | 0 | 0 | ready |
| 64 | `TwinClassFieldRuleMapEntity` | twinclass | 2 | 2 | 0 | 0 | 0 | ready |
| 65 | `TwinClassFieldSearchPredicateEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 66 | `TwinClassFreezeEntity` | twinclass | 1 | 1 | 0 | 0 | 0 | ready |
| 67 | `TwinClassSchemaEntity` | twinclass | 2 | 1 | 1 | 0 | 0 | partial |
| 68 | `TwinClassSchemaMapEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 69 | `TwinClassSearchPredicateEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 70 | `TwinEntity` | twin | 7 | 7 | 0 | 0 | 0 | ready |
| 71 | `TwinFactoryBranchEntity` | factory | 3 | 3 | 0 | 0 | 0 | ready |
| 72 | `TwinFactoryConditionEntity` | factory | 1 | 1 | 0 | 0 | 0 | ready |
| 73 | `TwinFactoryEraserEntity` | factory | 3 | 3 | 0 | 0 | 0 | ready |
| 74 | `TwinFactoryMultiplierEntity` | factory | 2 | 2 | 0 | 0 | 0 | ready |
| 75 | `TwinFactoryMultiplierFilterEntity` | factory | 3 | 3 | 0 | 0 | 0 | ready |
| 76 | `TwinFactoryPipelineEntity` | factory | 6 | 6 | 0 | 0 | 0 | ready |
| 77 | `TwinFactoryPipelineStepEntity` | factory | 2 | 2 | 0 | 0 | 0 | ready |
| 78 | `TwinFactoryTriggerEntity` | factory | 1 | 1 | 0 | 0 | 0 | ready |
| 79 | `TwinFieldAttributeEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 80 | `TwinFieldBooleanEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 81 | `TwinFieldDataListEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 82 | `TwinFieldDecimalEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 83 | `TwinFieldI18nEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 84 | `TwinFieldSimpleEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 85 | `TwinFieldSimpleNonIndexedEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 86 | `TwinFieldTimestampEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 87 | `TwinFieldTwinClassEntity` | twin | 3 | 3 | 0 | 0 | 0 | ready |
| 88 | `TwinFieldUserEntity` | twin | 2 | 1 | 1 | 0 | 0 | partial |
| 89 | `TwinLinkEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 90 | `TwinMarkerEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 91 | `TwinPointerEntity` | twin | 1 | 1 | 0 | 0 | 0 | ready |
| 92 | `TwinPointerValidatorRuleEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 93 | `TwinSearchPredicateEntity` | search | 0 | 0 | 0 | 0 | 0 | done |
| 94 | `TwinStatusEntity` | twin | 3 | 3 | 0 | 0 | 0 | ready |
| 95 | `TwinStatusGroupEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 96 | `TwinTagEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 97 | `TwinTouchEntity` | twin | 2 | 1 | 1 | 0 | 0 | partial |
| 98 | `TwinTriggerEntity` | trigger | 1 | 1 | 0 | 0 | 0 | ready |
| 99 | `TwinTriggerTaskEntity` | trigger | 3 | 3 | 0 | 0 | 0 | ready |
| 100 | `TwinWorkEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 101 | `TwinflowEntity` | twinflow | 3 | 3 | 0 | 0 | 0 | ready |
| 102 | `TwinflowFactoryEntity` | twinflow | 2 | 2 | 0 | 0 | 0 | ready |
| 103 | `TwinflowSchemaEntity` | twinflow | 2 | 1 | 1 | 0 | 0 | partial |
| 104 | `TwinflowSchemaMapEntity` | twinflow | 3 | 3 | 0 | 0 | 0 | ready |
| 105 | `TwinflowTransitionEntity` | twinflow | 6 | 6 | 0 | 0 | 0 | ready |
| 106 | `TwinflowTransitionTriggerEntity` | twinflow | 2 | 2 | 0 | 0 | 0 | ready |
| 107 | `TwinflowTransitionValidatorRuleEntity` | validator | 1 | 1 | 0 | 0 | 0 | ready |
| 108 | `UserEmailVerificationEntity` | user | 2 | 1 | 1 | 0 | 0 | partial |
| 109 | `UserGroupEntity` | user | 0 | 0 | 0 | 0 | 0 | done |
| 110 | `UserGroupInvolveAssigneeEntity` | usergroup | 3 | 3 | 0 | 0 | 0 | ready |
| 111 | `UserGroupMapEntity` | usergroup | 0 | 0 | 0 | 0 | 0 | done |
| 112 | `UserSearchPredicateEntity` | user | 1 | 1 | 0 | 0 | 0 | ready |

## Сводка по статусам

| Статус | Кол-во сущностей |
|---|---|
| done | 22 |
| ready | 61 |
| partial | 15 |
| audit | 9 |
| blocked | 4 |

**Итого полей:** legacy=187, simple=165, business=17, blocked=5

**История обновлений:**
- 2026-07-01: `TwinLinkEntity` → `done` (3 поля: `srcTwin`, `dstTwin`, `link`). `srcTwin`/`dstTwin`/`link` переименованы в `*SpecOnly` (`@Getter(AccessLevel.NONE)`, LAZY) с добавлением `@Transient` runtime-полей. В `TwinLinkService` добавлены `loadSrcTwin`/`loadDstTwin`/`loadLink` (collection + single) по образцу `loadCreatedByUser`. Load-вызовы добавлены в: `loadTwinLinks` (srcTwin+dstTwin+link), `filterDenied` (то же), `isLinkDstTwinStatusIn` (dstTwin), `updateTwinLinks` (dbTwinLinkEntity: srcTwin+dstTwin+link), `deleteTwinLinks` (srcTwin+dstTwin+link). `TwinLinkSpecification.checkStrength` — `Fields.link` → `Fields.linkSpecOnly`. Мапперы `TwinLinkForwardRestDTOMapper`/`TwinLinkBackwardRestDTOMapper` — `beforeCollectionConversion` bulk-load + load в `map()`; `TwinFieldValueRestDTOMapperV2` — bulk-load внутри `convert()` для `FieldValueLink`. Featurer'ы: в `FillerLinks.addLinks(FactoryItem, Collection)` добавлен throws + bulk-load (покрывает все subclasses); load'ы в `MultiplierIsolatedCopyWithDepth`, `FillerForwardLinkFromOutputTwinLinkDstTwinHead`, `FillerForwardLinkFromContextTwinLinkDstTwinHead`; null-safe fallback через `getDstTwinId()`+`findEntitySafe` в `FillerFieldAsContextFieldHead` и `FillerBasicsAssigneeFromOutputTwinFieldLink`. Остальные featurer'ы либо берут TwinLinkEntity через `twin.getTwinLinks()` (покрыто `loadTwinLinks`), либо уже имели null-safe fallback.
- 2026-06-25: `DataListOptionEntity` → `done` (2 поля: `dataList`, `businessAccount`). При ручном аудите нашлись бизнес-использования, пропущенные эвристикой (переменные не с именем `entity.`): `isEntityReadDenied` (`entity.getDataList().getDomainId()`), `updateDataListOptions` (`dbOption.getDataList()`), `reloadOptionsOnDataListAbsent` (null-check — упрощён до `getDataListId()`), маппер `DataListOptionRestDTOMapper.getAttributes` (`src.getDataList().getAttributeXkey()`). В `isEntityReadDenied` добавлен `loadDataList(entity)` при `null`. В `updateDataListOptions` добавлен `loadDataList(dbOption)`. Маппер переписан: добавлен `beforeCollectionConversion` (bulk load) + null-safe `getAttributes`. JPQL в `DataListOptionRepository.findAllByBusinessAccountIdAndDomainId` — `dlo.dataList` → `dlo.dataListSpecOnly`. Спецификации (`DataListOptionSearchService`, `DataListOptionSpecification`) — `Fields.dataList` → `Fields.dataListSpecOnly` (4 места). Load-методы `loadDataList`/`loadBusinessAccount` уже существовали в `DataListOptionService`. Статус в таблице детализации исправлен: 2 поля simple → migrated; эвристика не нашла business-использований из-за имён переменных вне `{Entity}Service`.
- 2026-06-24: `UserGroupEntity` → `done` (3 поля: `domain`, `businessAccount`, `userGroupType`). `loadUserGroupType` написан вручную через repository (UserGroupTypeEntity.id это String, не UUID — `EntitySecureFindServiceImpl.load()` не подходит). `UserGroupTypeRepository.findValidTypes` JPQL обновлён: 5 ссылок `ug.userGroupType` → `ug.userGroupTypeSpecOnly`. `getUserGroupType()` getter-uses в featurer-ах (Slugger, UserGroupManager — 5 мест) — пользователь взял аудит на себя.
- 2026-06-24: `DomainUserEntity` → `done` (2 поля: `domain`, `user`). `DomainUserSpecification` updated (`Fields.user` → `Fields.userSpecOnly`, 3 места), `DomainUserSearchService` updated (`Fields.domain` → `Fields.domainSpecOnly`). Маппер с `beforeCollectionConversion`. По ходу пофикшен missing-return в `DomainBusinessAccountUserRepository.findByDomainIdAndBusinessAccountIdAndUserId`.
- 2026-06-23: `DomainBusinessAccountEntity` → `done` (4 поля: `domain`, `businessAccount`, `permissionSchema`, `tier`). Существуют getter-uses в бизнес-логике (`PermissionService`, `BusinessAccountInitiator`, `DomainService`) — пользователь взял аудит на себя. В сервис добавлены `loadDomain`/`loadBusinessAccount` (loadPermissionSchema/loadTier уже были), маппер обновлён для вызова всех load-методов в `map()` и `beforeCollectionConversion()`.
- 2026-06-23: `BusinessAccountUserEntity`, `DomainBusinessAccountUserEntity` → `done`. `DomainBusinessAccountEntity`: статус `partial`→`audit` (при ручном аудите все 4 legacy-поля оказались в бизнес-логике сервисов/featurer — эвристика пропустила из-за того, что getter-использования вне `{Entity}Service` ищутся через переменные с произвольным именом).

## Детализация по сущностям (для приоритизации)

Только сущности со статусом `partial` / `audit` / `blocked` — то есть где эвристика нашла не-simple использование.  

### `BusinessAccountUserEntity` (businessaccount)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DomainBusinessAccountEntity` (domain)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `tier` | TierEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `DomainBusinessAccountUserEntity` (domain)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domainUser` | DomainUserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `domainBusinessAccount` | DomainBusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `businessAccountUser` | BusinessAccountUserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `DomainUserEntity` (domain)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DraftTwinAttachmentEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |
| `draft` | DraftEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `DraftTwinFieldBooleanEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DraftTwinFieldDataListEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DraftTwinFieldSimpleEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DraftTwinFieldSimpleNonIndexedEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DraftTwinMarkerEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

### `DraftTwinTagEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

### `I18nEntity` (i18n)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `IdentityProviderInternalUserEntity` (idp)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `PermissionGrantTwinRoleEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |
| `grantedByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `PermissionGroupEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `PermissionMaterGlobalEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

### `PermissionMaterSpaceUserEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

### `PermissionMaterSpaceUserGroupEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

### `PermissionMaterUserGroupEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

### `PermissionSchemaEntity` (permission)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `ResourceEntity` (resource)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `storage` | StorageEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `uploadedByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `StorageEntity` (resource)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `TwinClassEntity` (twinclass)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twinClassFreeze` | TwinClassFreezeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `TwinClassSchemaEntity` (twinclass)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `createdByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `TwinFieldUserEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `TwinTouchEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `TwinflowSchemaEntity` (twinflow)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

### `UserEmailVerificationEntity` (user)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `identityProvider` | IdentityProviderEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

### `UserGroupEntity` (user)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `userGroupType` | UserGroupTypeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

---

## Приложение: полная детализация по всем legacy-полям

<details><summary>Раскрыть всё</summary>

#### `AttachmentDeleteTaskEntity` (attachment) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `storage` | StorageEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `BusinessAccountEntity` (businessaccount) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `ownerUserGroup` | UserGroupEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `BusinessAccountUserEntity` (businessaccount) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DataListOptionEntity` (datalist) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `dataList` | DataListEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DataListOptionProjectionEntity` (datalist) — 4 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `ProjectionType` | ProjectionTypeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `srcDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `dstDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `savedByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DataListOptionSearchPredicateEntity` (datalist) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `dataListOptionSearch` | DataListOptionSearchEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DataListSubsetOptionEntity` (datalist) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `dataListSubset` | DataListSubsetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `dataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainBusinessAccountEntity` (domain) — 4 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `tier` | TierEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainBusinessAccountUserEntity` (domain) — 6 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domainUser` | DomainUserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `domainBusinessAccount` | DomainBusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `businessAccountUser` | BusinessAccountUserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainEntity` (domain) — 12 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `iconLightResource` | ResourceEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `iconDarkResource` | ResourceEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `attachmentsStorage` | StorageEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `resourcesStorage` | StorageEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `navbarFace` | FaceEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `identityProvider` | IdentityProviderEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassSchema` | TwinClassSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `businessAccountTemplateTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `defaultTier` | TierEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `domainUserTemplateTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `notificationSchema` | NotificationSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainLocaleEntity` (domain) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `i18nLocale` | I18nLocaleEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainTypeEntity` (domain) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domainInitiatorFeaturer` | FeaturerEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `defaultIdentityProvider` | IdentityProviderEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainTypeTwinClassOwnerTypeEntity` (domain) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassOwnerType` | TwinClassOwnerTypeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DomainUserEntity` (domain) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DraftHistoryEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `actorUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DraftTwinAttachmentEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |
| `draft` | DraftEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DraftTwinEraseEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `reasonTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DraftTwinFieldBooleanEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DraftTwinFieldDataListEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DraftTwinFieldSimpleEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DraftTwinFieldSimpleNonIndexedEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DraftTwinLinkEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `srcTwinId` | UUID | 0 | 0 | 0 | 0 | 0 | - | simple |
| `dstTwinId` | UUID | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DraftTwinMarkerEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `DraftTwinPersistEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `viewPermissionId` | UUID | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `DraftTwinTagEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `HistoryEntity` (history) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `HistoryNotificationEntity` (notification) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `historyType` | HistoryTypeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `HistoryNotificationTaskEntity` (notification) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `history` | HistoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `I18nEntity` (i18n) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `I18nTranslationEntity` (i18n) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `i18n` | I18nEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `IdentityProviderInternalUserEntity` (idp) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `NotificationEmailEntity` (notification) — 4 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `event` | EventEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `emailSender` | EmailSenderEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `subjectTemplateGenerator` | TemplateGeneratorEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `bodyTemplateGenerator` | TemplateGeneratorEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `PermissionEntity` (permission) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permissionGroup` | PermissionGroupEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `PermissionGrantTwinRoleEntity` (permission) — 4 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |
| `grantedByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `PermissionGroupEntity` (permission) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `PermissionMaterGlobalEntity` (permission) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

#### `PermissionMaterSpaceUserEntity` (permission) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

#### `PermissionMaterSpaceUserGroupEntity` (permission) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

#### `PermissionMaterUserGroupEntity` (permission) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | yes | BLOCKED-BIDIR |

#### `PermissionSchemaEntity` (permission) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `ProjectionTypeEntity` (projection) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `projectionTypeGroup` | ProjectionTypeGroupEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `membershipTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `ResourceEntity` (resource) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `storage` | StorageEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `uploadedByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `SpaceEntity` (space) — 4 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinflowSchema` | TwinflowSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassSchema` | TwinClassSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `SpaceRoleEntity` (space) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `StorageEntity` (resource) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `TierEntity` (domain) — 4 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `permissionSchema` | PermissionSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinflowSchema` | TwinflowSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassSchema` | TwinClassSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `notificationSchema` | NotificationSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinChangeTaskEntity` (twin) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassDynamicMarkerEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `markerDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twinClassFreeze` | TwinClassFreezeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassFieldConditionEntity` (twinclass) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldRule` | TwinClassFieldRuleEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassFieldEntity` (twinclass) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassFieldRuleMapEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldRule` | TwinClassFieldRuleEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassFreezeEntity` (twinclass) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinClassSchemaEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `createdByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinEntity` (twin) — 7 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `viewPermission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `headTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `ownerBusinessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `assignerUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `ownerUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryBranchEntity` (factory) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `factory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `nextFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryConditionEntity` (factory) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryEraserEntity` (factory) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryMultiplierEntity` (factory) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryMultiplierFilterEntity` (factory) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `multiplier` | TwinFactoryMultiplierEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryPipelineEntity` (factory) — 6 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `nextTwinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `outputTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `templateTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryPipelineStepEntity` (factory) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactoryPipeline` | TwinFactoryPipelineEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinFactoryConditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFactoryTriggerEntity` (factory) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinTrigger` | TwinTriggerEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldAttributeEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldAttribute` | TwinClassFieldAttributeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldBooleanEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldDataListEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `dataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldDecimalEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldI18nEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldSimpleEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldSimpleNonIndexedEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldTimestampEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldTwinClassEntity` (twin) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinFieldUserEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `TwinLinkEntity` (twin) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `srcTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `dstTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `link` | LinkEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinMarkerEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `markerDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinPointerEntity` (twin) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinPointerValidatorRuleEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinPointer` | TwinPointerEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinValidatorSet` | TwinValidatorSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinStatusEntity` (twin) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `iconLightResource` | ResourceEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `iconDarkResource` | ResourceEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinTagEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `tagDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinTouchEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinTriggerEntity` (trigger) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `jobTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinTriggerTaskEntity` (trigger) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinTrigger` | TwinTriggerEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `previousTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinWorkEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinByTwinId` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `authorUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowEntity` (twinflow) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `initialTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `initialSketchTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowFactoryEntity` (twinflow) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflow` | TwinflowEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowSchemaEntity` (twinflow) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowSchemaMapEntity` (twinflow) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflowSchema` | TwinflowSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinflow` | TwinflowEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowTransitionEntity` (twinflow) — 6 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflowTransitionAlias` | TwinflowTransitionAliasEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinflow` | TwinflowEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `srcTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `dstTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `inbuiltFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `draftingFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowTransitionTriggerEntity` (twinflow) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflowTransition` | TwinflowTransitionEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinTrigger` | TwinTriggerEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowTransitionValidatorRuleEntity` (validator) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinValidatorSet` | TwinValidatorSetEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `UserEmailVerificationEntity` (user) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `identityProvider` | IdentityProviderEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |

#### `UserGroupEntity` (user) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `userGroupType` | UserGroupTypeEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `UserGroupInvolveAssigneeEntity` (usergroup) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `userGroup` | UserGroupEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `UserSearchPredicateEntity` (user) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `userSearch` | UserSearchEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

</details>