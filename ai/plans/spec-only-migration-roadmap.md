# SpecOnly + load-паттерн: дорожная карта миграции

**Сгенерировано:** 2026-06-23 (автоматический аудит)  
**Последний переаудит:** 2026-09-22 (grep-аудит кодовой базы)  
**Конвенции:** `docs/entity_code_convention.md`, `docs/load_method_pattern.md`  
**Всего сущностей с legacy `@ManyToOne` (на момент первого аудита):** 97  
**Уже мигрировано (полный цикл):** 57  

## Factory batch (2026-07-08)

Мигрированы 9 Factory-сущностей. 20 полей стали SpecOnly + `@Transient` runtime. Load-методы добавлены в parent services. Все 18 mappers (9 RestDTOMapper + 9 CountRestDTOMapper) обновлены с `beforeCollectionConversion` batch-load + single-load fallback в `map()`.

### Полностью мигрированы

| Entity | Поля → SpecOnly | Load-методы в parent service |
|---|---|---|
| `TwinFactoryBranchEntity` | `factory`, `nextFactory`, `conditionSet` | `FactoryBranchService.loadFactory / loadNextFactory / loadConditionSet` |
| `TwinFactoryConditionEntity` | `conditionSet` (только, `conditionerFeaturerSpecOnly` уже был) | `FactoryConditionService.loadConditionSet` |
| `TwinFactoryEraserEntity` | `twinFactory`, `conditionSet`, `inputTwinClass` | `FactoryEraserService.loadTwinFactory / loadConditionSet / loadInputTwinClass` |
| `TwinFactoryMultiplierEntity` | `twinFactory`, `inputTwinClass` (только, `multiplierFeaturerSpecOnly` уже был) | `FactoryMultiplierService.loadTwinFactory / loadInputTwinClass` |
| `TwinFactoryMultiplierFilterEntity` | `multiplier`, `conditionSet`, `inputTwinClass` | `FactoryMultiplierFilterService.loadMultiplier / loadConditionSet / loadInputTwinClass` |
| `TwinFactoryPipelineEntity` | `twinFactory`, `nextTwinFactory`, `inputTwinClass`, `conditionSet`, `outputTwinStatus`, `templateTwin` | `FactoryPipelineService.loadTwinFactory / loadNextTwinFactory / loadInputTwinClass / loadConditionSet / loadOutputTwinStatus / loadTemplateTwin` |
| `TwinFactoryPipelineStepEntity` | `twinFactoryPipeline`, `twinFactoryConditionSet` (только, `fillerFeaturerSpecOnly` уже был) | `FactoryPipelineStepService.loadPipeline / loadConditionSet` |

### Блокирующие поля (isEntityReadDenied) — временно отключены

5 полей использовались в `isEntityReadDenied()` для domain check. По решению пользователя — закомментированы с `// TODO TWINS-840`, пользователь сам перепишет domain check через Specification/service lookup:

| Entity | Поле | Закомментированная логика |
|---|---|---|
| `TwinFactoryBranchEntity` | `factory` | `entity.getFactory().getDomainId()` |
| `TwinFactoryEraserEntity` | `twinFactory` | `entity.getTwinFactory().getDomainId()` |
| `TwinFactoryMultiplierEntity` | `twinFactory` | `entity.getTwinFactory().getDomainId()` |
| `TwinFactoryPipelineEntity` | `twinFactory` | `entity.getTwinFactory().getDomainId()` |
| `TwinFactoryPipelineStepEntity` | `twinFactoryPipeline` | `entity.getTwinFactoryPipeline().getTwinFactory().getDomainId()` (двойная навигация) |

### validateEntity — упрощён (remove eager load)

В `*Service.validateEntity()` удалён блок `beforeSave` eager-loading related entities (`twinFactoryService.findEntitySafe(...)`, `twinClassService.findEntitySafe(...)`, etc). Бизнес-логике не нужны eager-loaded relations для валидации (проверяется только наличие FK id).

### Specifications — updated to use `*SpecOnly`

В `dao/specifications/factory/*Specification.java` обновлены JOIN paths:
- `Fields.twinFactory` → `Fields.twinFactorySpecOnly`
- `Fields.factory` → `Fields.factorySpecOnly`
- `Fields.twinFactoryPipeline` → `Fields.twinFactoryPipelineSpecOnly`
- и т.д.

### SearchService.createSortSpecification — updated

Все 7 Factory `*SearchService.createSortSpecification` используют `*SpecOnly` для JOIN-сортировки.

### Бизнес-логика — minor fixes

`TwinFactoryService.runPipelineSteps`:
- `factoryPipelineEntity.getTemplateTwin()` → `twinService.findEntitySafe(factoryPipelineEntity.getTemplateTwinId())`
- `factoryPipelineEntity.getOutputTwinStatus()` → `twinStatusService.findEntitySafe(factoryPipelineEntity.getOutputTwinStatusId())`

Duplicate services (`*DuplicateService`): `loadRequiredRelations` уже корректно вызывает load methods → runtime поля загружаются перед processing.

### Mappper'ы — batch-load pattern

Все 18 mapper'ов (9 RestDTOMapper + 9 CountRestDTOMapper) приведены к единому паттерну:

```java
@Override
public void map(...) {
    if (mapperContext.hasModeButNot(SomeMode.HIDE)) {
        if (src.getXxx() == null) parentService.loadXxx(src);  // single-load fallback
        xxxRestDTOMapper.convertOrPostpone(src.getXxx(), ...);
    }
}

@Override
public void beforeCollectionConversion(...) {
    super.beforeCollectionConversion(srcCollection, mapperContext);
    if (srcCollection.isEmpty()) return;
    if (mapperContext.hasModeButNot(SomeMode.HIDE)) {
        parentService.loadXxx(srcCollection);  // batch-load
    }
}
```

В Count mapper'ах conditional loading сохранён через `needLoad(mapperContext, mode, src, groupField)` — грузится только если соответствующее groupField запрошено в `request.getGroupFields()`.

### Предыдущие миграции

(см. ниже — список остальных мигрированных сущностей)

---

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
| 2 | `BusinessAccountEntity` | businessaccount | 0 | 0 | 0 | 0 | 0 | done |
| 3 | `BusinessAccountUserEntity` | businessaccount | 0 | 0 | 0 | 0 | 0 | done |
| 4 | `DataListOptionEntity` | datalist | 0 | 0 | 0 | 0 | 0 | done |
| 5 | `DataListOptionProjectionEntity` | datalist | 0 | 0 | 0 | 0 | 0 | done |
| 6 | `DataListOptionSearchPredicateEntity` | datalist | 1 | 1 | 0 | 0 | 0 | ready |
| 7 | `DataListSubsetOptionEntity` | datalist | 2 | 2 | 0 | 0 | 0 | ready |
| 8 | `DomainBusinessAccountEntity` | domain | 0 | 0 | 0 | 0 | 0 | done |
| 9 | `DomainBusinessAccountUserEntity` | domain | 0 | 0 | 0 | 0 | 0 | done |
| 10 | `DomainEntity` | domain | 12 | 12 | 0 | 0 | 0 | ready |
| 11 | `DomainLocaleEntity` | domain | 1 | 1 | 0 | 0 | 0 | ready |
| 12 | `DomainTypeEntity` | domain | 2 | 2 | 0 | 0 | 0 | ready |
| 13 | `DomainTypeTwinClassOwnerTypeEntity` | domain | 1 | 1 | 0 | 0 | 0 | ready |
| 14 | `DomainUserEntity` | domain | 0 | 0 | 0 | 0 | 0 | done |
| 15 | `DraftHistoryEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 16 | `DraftTwinAttachmentEntity` | draft | 1 | 1 | 0 | 0 | 0 | partial |
| 17 | `DraftTwinEraseEntity` | draft | 1 | 1 | 0 | 0 | 0 | partial |
| 18 | `DraftTwinFieldBooleanEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 19 | `DraftTwinFieldDataListEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 20 | `DraftTwinFieldSimpleEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 21 | `DraftTwinFieldSimpleNonIndexedEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 22 | `DraftTwinFieldTwinClassEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 23 | `DraftTwinLinkEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 24 | `DraftTwinMarkerEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 25 | `DraftTwinPersistEntity` | draft | 1 | 1 | 0 | 0 | 0 | partial |
| 26 | `DraftTwinTagEntity` | draft | 0 | 0 | 0 | 0 | 0 | done |
| 27 | `EraseflowEntity` | eraseflow | 0 | 0 | 0 | 0 | 0 | done |
| 28 | `EraseflowLinkCascadeEntity` | eraseflow | 0 | 0 | 0 | 0 | 0 | done |
| 29 | `FaceEntity` | face | 0 | 0 | 0 | 0 | 0 | done |
| 30 | `HistoryEntity` | history | 0 | 0 | 0 | 0 | 0 | done |
| 31 | `HistoryNotificationEntity` | notification | 0 | 0 | 0 | 0 | 0 | done |
| 32 | `HistoryNotificationTaskEntity` | notification | 0 | 0 | 0 | 0 | 0 | done |
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
| 60 | `TwinClassDynamicMarkerEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 61 | `TwinClassEntity` | twinclass | 1 | 1 | 0 | 0 | 0 | partial |
| 62 | `TwinClassFieldConditionEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 63 | `TwinClassFieldEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 64 | `TwinClassFieldRuleMapEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 65 | `TwinClassFieldSearchPredicateEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 66 | `TwinClassFreezeEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 67 | `TwinClassSchemaEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 68 | `TwinClassSchemaMapEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 69 | `TwinClassSearchPredicateEntity` | twinclass | 0 | 0 | 0 | 0 | 0 | done |
| 70 | `TwinEntity` | twin | 5 | 5 | 0 | 0 | 0 | partial |
| 71 | `TwinFactoryBranchEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 72 | `TwinFactoryConditionEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 73 | `TwinFactoryEraserEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 74 | `TwinFactoryMultiplierEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 75 | `TwinFactoryMultiplierFilterEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 76 | `TwinFactoryPipelineEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 77 | `TwinFactoryPipelineStepEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 78 | `TwinFactoryTriggerEntity` | factory | 0 | 0 | 0 | 0 | 0 | done |
| 79 | `TwinFieldAttributeEntity` | twin | 1 | 1 | 0 | 0 | 0 | partial |
| 80 | `TwinFieldBooleanEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 81 | `TwinFieldDataListEntity` | twin | 1 | 1 | 0 | 0 | 0 | partial |
| 82 | `TwinFieldDecimalEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 83 | `TwinFieldI18nEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 84 | `TwinFieldSimpleEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 85 | `TwinFieldSimpleNonIndexedEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 86 | `TwinFieldTimestampEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 87 | `TwinFieldTwinClassEntity` | twin | 1 | 1 | 0 | 0 | 0 | partial |
| 88 | `TwinFieldUserEntity` | twin | 1 | 0 | 1 | 0 | 0 | partial |
| 89 | `TwinLinkEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 90 | `TwinMarkerEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 91 | `TwinPointerEntity` | twin | 1 | 1 | 0 | 0 | 0 | ready |
| 92 | `TwinPointerValidatorRuleEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 93 | `TwinSearchPredicateEntity` | search | 0 | 0 | 0 | 0 | 0 | done |
| 94 | `TwinStatusEntity` | twin | 3 | 3 | 0 | 0 | 0 | ready |
| 95 | `TwinStatusGroupEntity` | twin | 0 | 0 | 0 | 0 | 0 | done |
| 96 | `TwinTagEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 97 | `TwinTouchEntity` | twin | 2 | 1 | 1 | 0 | 0 | partial |
| 98 | `TwinTriggerEntity` | trigger | 0 | 0 | 0 | 0 | 0 | done |
| 99 | `TwinTriggerTaskEntity` | trigger | 0 | 0 | 0 | 0 | 0 | done |
| 100 | `TwinWorkEntity` | twin | 2 | 2 | 0 | 0 | 0 | ready |
| 101 | `TwinflowEntity` | twinflow | 0 | 0 | 0 | 0 | 0 | done |
| 102 | `TwinflowFactoryEntity` | twinflow | 0 | 0 | 0 | 0 | 0 | done |
| 103 | `TwinflowSchemaEntity` | twinflow | 2 | 1 | 1 | 0 | 0 | partial |
| 104 | `TwinflowSchemaMapEntity` | twinflow | 0 | 0 | 0 | 0 | 0 | done |
| 105 | `TwinflowTransitionEntity` | twinflow | 0 | 0 | 0 | 0 | 0 | done |
| 106 | `TwinflowTransitionTriggerEntity` | twinflow | 0 | 0 | 0 | 0 | 0 | done |
| 107 | `TwinflowTransitionValidatorRuleEntity` | validator | 1 | 1 | 0 | 0 | 0 | ready |
| 108 | `UserEmailVerificationEntity` | user | 2 | 1 | 1 | 0 | 0 | partial |
| 109 | `UserGroupEntity` | user | 0 | 0 | 0 | 0 | 0 | done |
| 110 | `UserGroupInvolveAssigneeEntity` | usergroup | 3 | 3 | 0 | 0 | 0 | ready |
| 111 | `UserGroupMapEntity` | usergroup | 0 | 0 | 0 | 0 | 0 | done |
| 112 | `UserSearchPredicateEntity` | user | 1 | 1 | 0 | 0 | 0 | ready |

## Сводка по статусам

| Статус | Кол-во сущностей |
|---|---|
| done | 57 |
| ready | 29 |
| partial | 19 |
| audit | 3 |
| blocked | 4 |

**Итого полей:** legacy=96, simple=75, business=16, blocked=5

**Примечание к переаудиту 2026-09-22:** сущности, созданные после внедрения конвенции (`TwinCommentEntity`, `LinkEntity`, `TwinAttachmentEntity`, `TwinActionEntity`, `SchedulerLogEntity`, `TwinAliasEntity`, `ProjectionEntity`, `TwinFactoryEntity`, `TwinFactoryConditionSetEntity`, `DataListEntity`, `DataListSubsetEntity`, `NotificationSchemaEntity`, `NotificationContext*Entity`, `NotificationChannelEventEntity`, `HistoryNotificationRecipient*Entity`, `I18nTranslationBin/StyleEntity`, `TwinStatusTriggerEntity`, `TwinValidatorEntity`, `TwinClassFieldValidatorEntity`, `DraftEntity`, `TwinRecompute*Entity` и др.), пишутся сразу по SpecOnly-паттерну и в дорожную карту не входят.

**История обновлений:**
- 2026-09-22 (4): **Twinclass batch** — мигрированы 5 сущностей / 7 полей (все ready из кластера twinclass): `TwinClassDynamicMarkerEntity` (`markerDataListOption`, `twinClass` — оба EAGER), `TwinClassFieldConditionEntity` (`twinClassFieldRule`), `TwinClassFieldEntity` (`twinClass` — EAGER), `TwinClassFieldRuleMapEntity` (`twinClassFieldRule`, `twinClassField` — оба EAGER), `TwinClassFreezeEntity` (`twinStatus` — EAGER). Load-методы: `TwinClassDynamicMarkerService.loadTwinClass/loadMarkerDataListOption` (single+collection, target — `twinClassService`/`dataListOptionService`), `TwinClassFreezeService.loadTwinStatus`, `TwinClassFieldRuleMapService.loadTwinClassFieldRule` (bulk — внутри `loadRules` сразу после `findByTwinClassFieldIdIn`); `TwinClassFieldService.loadTwinClass` уже существовал — сигнатура расширена `List` → `Collection` для beforeCollectionConversion. `TwinClassFieldConditionEntity.twinClassFieldRule` — новых load-методов не потребовалось: население уже делает существующий `loadConditions` (loadKit). `TwinClassFieldRuleMapEntity.twinClassField` — геттер не читается, load не нужен (прецедент SchemaMap). Спеки (по чек-листу «grep `Fields.<oldName>`»): `TwinClassDynamicMarkerSearchService:40`, `TwinClassFieldSearchService:90` (фильтр) и `:146` (сортировка twinClassName), `TwinClassFieldValidatorSearchService:65` — `Fields.twinClass` → `Fields.twinClassSpecOnly`. Бизнес-логика: `TwinClassDynamicMarkerService.isEntityReadDenied` + `processValidatorSet`, `TwinClassFieldService.isEntityReadDenied` — load-if-null; `FieldTyperSpaceRoleUsers.serializeValue` и `FieldTyperLink.allowMultiply` — load перед `getTwinClassField().getTwinClass()` (пропуск allowMultiply найден при повторном аудите без head-усечения grep'а; FieldTyperLinkTest получил @Mock TwinClassFieldService). Мапперы: `TwinClassDynamicMarkerDTOMapper`, `TwinClassFieldRestDTOMapper`, `TwinClassFreezeDTOMapper` — fallback в `map()` + `beforeCollectionConversion`; `TwinClassFieldCountRestDTOMapper` уже имел load. Новый @Lazy-цикл: `TwinClassFieldRuleMapService → TwinClassFieldRuleService` (обратное ребро существовало). compileJava + compileTestJava чистые; grep устаревших `Fields.<oldName>` — пусто.
- 2026-09-22 (3): **Twinflow batch — merge-note по итогам expert-panel ревью** (полный протокол: `ai/review/PR-TWINS-836-twinflow-load.md`). (а) **Контракт load (теперь официальный):** чтение runtime-поля (`@Transient`-дубля) требует предшествующего load-вызова; компилятор это не проверяет — сигнатуры геттеров не изменились, забытый load = тихий null/NPE. Related-objects drain (`RelatedObjectsRestDTOConverter.drain()`) конвертирует postponed-объекты **поштучно**, минуя `beforeCollectionConversion` → per-entity load в `map()` обязателен всегда, bulk в `beforeCollectionConversion` — оптимизация, не замена. (б) **Новые @Lazy-циклы этим батчем:** `TwinflowTransitionService ↔ TwinflowTransitionTriggerService`, `TwinflowTransitionService → FactoryService` (обратное ребро существовало) — новые рёбра сервисов домена без нужды не добавлять, при росте клубка — интерфейсный срез Loader. (в) **Чек-лист миграции поля (расширенный):** после раскола поля grep'ать не только вызовы геттеров, но и СТРОЧНЫЕ ссылки на старое имя — `Fields.<oldName>` в specifications/JPQL и `root.join("<oldName>")`: `git grep -n "Fields.<oldName>" -- "*.java"`. Кейс: `TransitionSpecification.checkAliasLikeIn` делал `root.join(Fields.twinflowTransitionAlias)` по @Transient-полю → `IllegalArgumentException` на каждом transition search (P1 ревью; компилятор и lombok `@FieldNameConstants` это не ловят). Фикс — оба метода `TransitionSpecification` заменены на generic-хелперы `CommonSpecification` (`checkFieldLikeIn`/`checkFieldIn`, путь через `twinflowTransitionAliasSpecOnly`), файл удалён: меньше специализированного spec-кода — меньше мест для «забытого SpecOnly». Остаточные условия merge: integration-тест transition search (alias непустой/пустой + DETAILED-маппер) и смоук остальных twinflow search-endpoint'ов. (г) likeList-конвенция: `checkFieldLikeContainsIn` оборачивает термы в `%…%` (сырые подстроки: name/key/alias), `checkFieldLikeIn` — паттерны как есть (regexp/type-поля).
- 2026-09-22 (2): **Twinflow batch** — мигрированы 5 сущностей / 16 полей: `TwinflowEntity` (`twinClass`, `initialTwinStatus`, `initialSketchTwinStatus`), `TwinflowTransitionEntity` (`twinflowTransitionAlias`, `twinflow`, `srcTwinStatus`, `dstTwinStatus`, `inbuiltFactory`, `draftingFactory`), `TwinflowFactoryEntity` (`twinflow`, `twinFactory`), `TwinflowSchemaMapEntity` (`twinflowSchema`, `twinClass`, `twinflow`), `TwinflowTransitionTriggerEntity` (`twinflowTransition`, `twinTrigger` — был EAGER). Load-методы: `TwinflowService.loadTwinClass/loadInitialTwinStatus/loadInitialSketchTwinStatus`, `TwinflowTransitionService.loadTwinflow/loadSrcTwinStatus/loadDstTwinStatus/loadInbuiltFactory/loadDraftingFactory/loadTwinflowTransitionAlias` (alias — вручную через репозиторий, своего сервиса у alias-энтити нет), `TwinflowTransitionTriggerService.loadTwinflowTransition(s)`; в `TwinflowFactoryService` loadTwinflow/loadTwinFactory уже были. `TwinflowSchemaMapEntity` — геттеры нигде не читаются, load-методы не нужны. Мапперы: `TwinflowBaseV1`, `TransitionBaseV1/V2`, `TwinflowFactoryRestDTOMapperV1`, `TransitionTrigger` — fallback в `map()` + `beforeCollectionConversion` (в `TransitionBaseV1` alias грузится без mode-гварда — используется во всех режимах, после миграции null-safe). Спеки: 4 `*SearchService` + JPQL `TwinflowRepository.findAllByBusinessAccountIdAndDomainId` (`t.twinClassSpecOnly.domainId`) + `TwinflowTransitionRepository.findTransitionByAlias` (`tt.twinflowTransitionAliasSpecOnly.alias`). Бизнес-логика: `validateEntity` в Twinflow/TwinflowTransition сервисах — load-if-null в default-ветке (иначе NPE в afterRead); `loadTwinflow(Collection<TwinEntity>)` батч-предзагружает twinClass+initialTwinStatus перед `validateEntityAndThrow`; `updateTransitionSrc/DstStatus` + `updateTwinflowTransitions` cacheEvict — load-if-null (хелпер `loadTwinflowWithTwinClass`); `performTransitions`/`draftTransitions` — батч `loadDstTwinStatus`; `runTriggers` — батч `loadSrcTwinStatus`/`loadDstTwinStatus` + батч `loadTriggers` для sync-триггеров; `TwinService.setInitStatus` — load initialTwinStatus; `LogSupportService.generateSubstitutionsConfig` — батч-загрузки twinClass/statuses/twinflow перед лог-циклами; `isEntityReadDenied`: TwinflowService — load-if-null twinClass, TwinflowFactoryService — load-if-null twinflow+twinFactory (паттерн FactoryTriggerService, без TODO-комментирования). Цикл зависимостей TwinflowTransitionService ↔ TwinflowTransitionTriggerService разорван `@Lazy`.
- 2026-09-22: полный переаудит кодовой базы (grep `@ManyToOne` без `SpecOnly` по `dao/`). Статусная таблица приведена к фактическому состоянию: done 22→52, ready 61→34, partial 15→19, audit 9→3, blocked 4. Новые done: `BusinessAccountEntity` (`ownerUserGroup` закомментирован), `DataListOptionProjectionEntity` (4 поля), `DraftHistoryEntity` (2), `HistoryEntity`, `HistoryNotificationEntity`, `HistoryNotificationTaskEntity`, `TwinClassSchemaEntity` (`domain`, `createdByUser` закомментированы), `TwinFactoryTriggerEntity`, `TwinTriggerEntity`, `TwinTriggerTaskEntity`, batch `DraftTwinFieldBoolean/DataList/Simple/SimpleNonIndexed/Link/Marker/Tag` (`draft` → `draftSpecOnly` + `@Transient`; `twinId` остаётся raw UUID намеренно — «we can not create @ManyToOne relation, because it can be new twin here»), batch `TwinFieldBoolean/Decimal/I18n/Simple/SimpleNonIndexed/Timestamp` — миграция выполнена один раз в базовом `TwinFieldBaseEntity` (`twinSpecOnly`, `twinClassFieldSpecOnly` + `@Transient` runtime). Также в таблицу наконец внесён Factory batch 2026-07-08 (7 сущностей → done). Новые partial: `TwinEntity` (из 7 полей мигрированы `headTwin`, `ownerBusinessAccount` — закомментированы с `@Transient` runtime; остались `viewPermission`, `twinClass` (EAGER!), `twinStatus`, `assignerUser`, `ownerUser`), `DraftTwinEraseEntity` (`reasonTwin` закомментирован, остался `twin`), `DraftTwinPersistEntity` (остался `twinStatus`; `draft`/`twinClass`/`assignerUser`/`createdByUser` уже SpecOnly), `TwinFieldAttribute` (остался `twinClassFieldAttribute`), `TwinFieldDataList` (остался `dataListOption`), `TwinFieldTwinClass` (остался `twinClass`), `TwinClassEntity` (`domain` закомментирован, остался `twinClassFreeze`). Псевдо-audit-поля `twinId` (UUID) у draft-сущностей исключены из счётчика legacy — это целевое состояние по конвенции, а не долг.
- 2026-07-01: `TwinLinkEntity` → `done` (3 поля: `srcTwin`, `dstTwin`, `link`). `srcTwin`/`dstTwin`/`link` переименованы в `*SpecOnly` (`@Getter(AccessLevel.NONE)`, LAZY) с добавлением `@Transient` runtime-полей. В `TwinLinkService` добавлены `loadSrcTwin`/`loadDstTwin`/`loadLink` (collection + single) по образцу `loadCreatedByUser`. Load-вызовы добавлены в: `loadTwinLinks` (srcTwin+dstTwin+link), `filterDenied` (то же), `isLinkDstTwinStatusIn` (dstTwin), `updateTwinLinks` (dbTwinLinkEntity: srcTwin+dstTwin+link), `deleteTwinLinks` (srcTwin+dstTwin+link). `TwinLinkSpecification.checkStrength` — `Fields.link` → `Fields.linkSpecOnly`. Мапперы `TwinLinkForwardRestDTOMapper`/`TwinLinkBackwardRestDTOMapper` — `beforeCollectionConversion` bulk-load + load в `map()`; `TwinFieldValueRestDTOMapperV2` — bulk-load внутри `convert()` для `FieldValueLink`. Featurer'ы: в `FillerLinks.addLinks(FactoryItem, Collection)` добавлен throws + bulk-load (покрывает все subclasses); load'ы в `MultiplierIsolatedCopyWithDepth`, `FillerForwardLinkFromOutputTwinLinkDstTwinHead`, `FillerForwardLinkFromContextTwinLinkDstTwinHead`; null-safe fallback через `getDstTwinId()`+`findEntitySafe` в `FillerFieldAsContextFieldHead` и `FillerBasicsAssigneeFromOutputTwinFieldLink`. Остальные featurer'ы либо берут TwinLinkEntity через `twin.getTwinLinks()` (покрыто `loadTwinLinks`), либо уже имели null-safe fallback.
- 2026-06-25: `DataListOptionEntity` → `done` (2 поля: `dataList`, `businessAccount`). При ручном аудите нашлись бизнес-использования, пропущенные эвристикой (переменные не с именем `entity.`): `isEntityReadDenied` (`entity.getDataList().getDomainId()`), `updateDataListOptions` (`dbOption.getDataList()`), `reloadOptionsOnDataListAbsent` (null-check — упрощён до `getDataListId()`), маппер `DataListOptionRestDTOMapper.getAttributes` (`src.getDataList().getAttributeXkey()`). В `isEntityReadDenied` добавлен `loadDataList(entity)` при `null`. В `updateDataListOptions` добавлен `loadDataList(dbOption)`. Маппер переписан: добавлен `beforeCollectionConversion` (bulk load) + null-safe `getAttributes`. JPQL в `DataListOptionRepository.findAllByBusinessAccountIdAndDomainId` — `dlo.dataList` → `dlo.dataListSpecOnly`. Спецификации (`DataListOptionSearchService`, `DataListOptionSpecification`) — `Fields.dataList` → `Fields.dataListSpecOnly` (4 места). Load-методы `loadDataList`/`loadBusinessAccount` уже существовали в `DataListOptionService`. Статус в таблице детализации исправлен: 2 поля simple → migrated; эвристика не нашла business-использований из-за имён переменных вне `{Entity}Service`.
- 2026-06-24: `UserGroupEntity` → `done` (3 поля: `domain`, `businessAccount`, `userGroupType`). `loadUserGroupType` написан вручную через repository (UserGroupTypeEntity.id это String, не UUID — `EntitySecureFindServiceImpl.load()` не подходит). `UserGroupTypeRepository.findValidTypes` JPQL обновлён: 5 ссылок `ug.userGroupType` → `ug.userGroupTypeSpecOnly`. `getUserGroupType()` getter-uses в featurer-ах (Slugger, UserGroupManager — 5 мест) — пользователь взял аудит на себя.
- 2026-06-24: `DomainUserEntity` → `done` (2 поля: `domain`, `user`). `DomainUserSpecification` updated (`Fields.user` → `Fields.userSpecOnly`, 3 места), `DomainUserSearchService` updated (`Fields.domain` → `Fields.domainSpecOnly`). Маппер с `beforeCollectionConversion`. По ходу пофикшен missing-return в `DomainBusinessAccountUserRepository.findByDomainIdAndBusinessAccountIdAndUserId`.
- 2026-06-23: `DomainBusinessAccountEntity` → `done` (4 поля: `domain`, `businessAccount`, `permissionSchema`, `tier`). Существуют getter-uses в бизнес-логике (`PermissionService`, `BusinessAccountInitiator`, `DomainService`) — пользователь взял аудит на себя. В сервис добавлены `loadDomain`/`loadBusinessAccount` (loadPermissionSchema/loadTier уже были), маппер обновлён для вызова всех load-методов в `map()` и `beforeCollectionConversion()`.
- 2026-06-23: `BusinessAccountUserEntity`, `DomainBusinessAccountUserEntity` → `done`. `DomainBusinessAccountEntity`: статус `partial`→`audit` (при ручном аудите все 4 legacy-поля оказались в бизнес-логике сервисов/featurer — эвристика пропустила из-за того, что getter-использования вне `{Entity}Service` ищутся через переменные с произвольным именом).

## Детализация по сущностям (для приоритизации)

Только сущности со статусом `partial` / `audit` / `blocked` — то есть где остались непромигрированные поля.

### `DraftTwinAttachmentEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design (FK может указывать на ещё не сохранённый twin) |
| `draft` | DraftEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne(EAGER)` |

### `DraftTwinEraseEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne(EAGER)` |
| `reasonTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован) |

### `DraftTwinPersistEntity` (draft)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `viewPermissionId` | UUID | 0 | 0 | 0 | 0 | 0 | - | raw by design («can have nullify marker here») |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne(EAGER)` |

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
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | migrated (закомментирован, runtime `@Transient`) |
| `twinClassFreeze` | TwinClassFreezeEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` (LAZY, но без SpecOnly-суффикса и `@Getter(NONE)`) |

### `TwinEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `viewPermission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne(LAZY)` без SpecOnly-суффикса, без `@Transient` runtime |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne(EAGER)` — приоритет, жёсткое нарушение §8 конвенции |
| `headTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован, runtime `@Transient`) |
| `ownerBusinessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован) |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne` без fetch (дефолт EAGER) |
| `assignerUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne` без fetch (дефолт EAGER) |
| `ownerUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne` без fetch (дефолт EAGER) |

### `TwinFieldAttributeEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldAttribute` | TwinClassFieldAttributeEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

### `TwinFieldDataListEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `dataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

### `TwinFieldTwinClassEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

### `TwinFieldUserEntity` (twin)

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `user` | UserEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

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
| `ownerUserGroup` | UserGroupEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован) |

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
| `ProjectionType` | ProjectionTypeEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |
| `srcDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |
| `dstDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |
| `savedByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

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
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментированы) |
| `actorUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментированы) |

#### `DraftTwinAttachmentEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design (FK может указывать на ещё не сохранённый twin) |
| `draft` | DraftEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne(EAGER)` |

#### `DraftTwinEraseEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne(EAGER)` |
| `reasonTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован) |

#### `DraftTwinFieldBooleanEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `DraftTwinFieldDataListEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `DraftTwinFieldSimpleEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `DraftTwinFieldSimpleNonIndexedEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `DraftTwinLinkEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `srcTwinId` | UUID | 0 | 0 | 0 | 0 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |
| `dstTwinId` | UUID | 0 | 0 | 0 | 0 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `DraftTwinMarkerEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `DraftTwinPersistEntity` (draft) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `viewPermissionId` | UUID | 0 | 0 | 0 | 0 | 0 | - | raw by design («can have nullify marker here») |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne(EAGER)`; `draft`/`twinClass`/`assignerUser`/`createdByUser` уже SpecOnly |

#### `DraftTwinTagEntity` (draft) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinId` | UUID | 0 | 0 | 0 | 1 | 0 | - | raw by design; `draft` → `draftSpecOnly` + `@Transient` — done |

#### `HistoryEntity` (history) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `HistoryNotificationEntity` (notification) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `historyType` | HistoryTypeEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `HistoryNotificationTaskEntity` (notification) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `history` | HistoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

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
| `markerDataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `TwinClassEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | migrated (закомментирован, runtime `@Transient`) |
| `twinClassFreeze` | TwinClassFreezeEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` (LAZY, но без SpecOnly-суффикса и `@Getter(NONE)`) |

#### `TwinClassFieldConditionEntity` (twinclass) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldRule` | TwinClassFieldRuleEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (население — существующий `loadConditions` через loadKit) |

#### `TwinClassFieldEntity` (twinclass) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `TwinClassFieldRuleMapEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldRule` | TwinClassFieldRuleEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (bulk в `loadRules`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (геттер не читается, load не нужен — прецедент SchemaMap) |

#### `TwinClassFreezeEntity` (twinclass) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `TwinClassSchemaEntity` (twinclass) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | migrated (закомментирован) |
| `createdByUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован) |

#### `TwinEntity` (twin) — 7 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `viewPermission` | PermissionEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne(LAZY)`, но без SpecOnly-суффикса, `@Getter(NONE)` и `@Transient` runtime |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne(EAGER)` — приоритет, жёсткое нарушение §8 конвенции |
| `headTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован, runtime `@Transient` есть) |
| `ownerBusinessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (закомментирован) |
| `twinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne` без fetch (дефолт EAGER) |
| `assignerUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne` без fetch (дефолт EAGER) |
| `ownerUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple — legacy `@ManyToOne` без fetch (дефолт EAGER) |

Уже мигрированы в `TwinEntity` (вне исходного аудита): `flavorDataListOption`, `createdByUser` — SpecOnly + `@Transient` runtime.

#### `TwinFactoryBranchEntity` (factory) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `factory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `nextFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryConditionEntity` (factory) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryEraserEntity` (factory) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryMultiplierEntity` (factory) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryMultiplierFilterEntity` (factory) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `multiplier` | TwinFactoryMultiplierEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryPipelineEntity` (factory) — 6 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `nextTwinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `inputTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `conditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `outputTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `templateTwin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryPipelineStepEntity` (factory) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinFactoryPipeline` | TwinFactoryPipelineEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |
| `twinFactoryConditionSet` | TwinFactoryConditionSetEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (Factory batch 2026-07-08) |

#### `TwinFactoryTriggerEntity` (factory) — 1 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinTrigger` | TwinTriggerEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `TwinFieldAttributeEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClassFieldAttribute` | TwinClassFieldAttributeEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` |
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldBooleanEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldDataListEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `dataListOption` | DataListOptionEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` |

#### `TwinFieldDecimalEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldI18nEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldSimpleEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldSimpleNonIndexedEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldTimestampEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |

#### `TwinFieldTwinClassEntity` (twin) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClassField` | TwinClassFieldEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | simple — осталось legacy `@ManyToOne` |

#### `TwinFieldUserEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (в `TwinFieldBaseEntity`) |
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
| `jobTwinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `TwinTriggerTaskEntity` (trigger) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twin` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |
| `twinTrigger` | TwinTriggerEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |
| `previousTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated |

#### `TwinWorkEntity` (twin) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinByTwinId` | TwinEntity | 0 | 0 | 0 | 0 | 0 | - | simple |
| `authorUser` | UserEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowEntity` (twinflow) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `initialTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `initialSketchTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |

#### `TwinflowFactoryEntity` (twinflow) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflow` | TwinflowEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `twinFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |

#### `TwinflowSchemaEntity` (twinflow) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `domain` | DomainEntity | 0 | 0 | 0 | 1 | 0 | - | audit |
| `businessAccount` | BusinessAccountEntity | 0 | 0 | 0 | 0 | 0 | - | simple |

#### `TwinflowSchemaMapEntity` (twinflow) — 3 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflowSchema` | TwinflowSchemaEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `twinClass` | TwinClassEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `twinflow` | TwinflowEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |

#### `TwinflowTransitionEntity` (twinflow) — 6 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflowTransitionAlias` | TwinflowTransitionAliasEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `twinflow` | TwinflowEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `srcTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `dstTwinStatus` | TwinStatusEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `inbuiltFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `draftingFactory` | TwinFactoryEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |

#### `TwinflowTransitionTriggerEntity` (twinflow) — 2 полей

| Поле | Тип | spec | mapper | validate | service/ctrl | denied | bidir | Вердикт |
|---|---|---|---|---|---|---|---|---|
| `twinflowTransition` | TwinflowTransitionEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |
| `twinTrigger` | TwinTriggerEntity | 0 | 0 | 0 | 0 | 0 | - | migrated (twinflow batch 2026-09-22) |

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