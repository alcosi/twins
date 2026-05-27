# Load Method Pattern

A pattern for batch loading `@Transient` fields in JPA entities. It solves the N+1 query problem when working with related data that is not loaded through standard JPA mechanisms (lazy relations, JOIN FETCH, etc.).

## Problem Overview

An entity has an `@Transient` field that is populated manually from a separate query. If a separate query is executed for each entity in a collection, this results in N+1. Load methods solve this with a single batch query for the entire collection.

## Pattern Structure

Each `load*` method consists of three phases:

```
1. Filtering (needLoad)   →  determine which entities need data
2. Batch query            →  fetch all data with a single SQL query
3. Assignment             →  distribute results across entities
```

### Mandatory Rules

* The method accepts `Collection<E>` (single-element overloads exist via `Collections.singletonList`)
* Filtering: an entity is skipped if the `@Transient` field is already not `null`
* Early return when the `needLoad` collection is empty
* Results are grouped by owner entity ID using `KitGrouped`

## Implementation Variants

### Variant A: Delegation to base `EntitySecureFindServiceImpl.load()`

Used for loading a single related entity by FK (ManyToOne). The base class already contains the template.

```java
public void loadTwinflowSchema(Collection<DomainBusinessAccountEntity> srcCollection) throws ServiceException {
    twinflowSchemaService.load(
        srcCollection,
        DomainBusinessAccountEntity::getId,
        DomainBusinessAccountEntity::getTwinflowSchemaId,
        DomainBusinessAccountEntity::getTwinflowSchema,
        DomainBusinessAccountEntity::setTwinflowSchema
    );
}
```

**What the base `load()` does** (`EntitySecureFindServiceImpl`):

1. Filters the collection using `KitUtils.createNeedLoadGrouped()` — keeps only entities where the `@Transient` field is `null`
2. Calls `findEntitiesSafe(needLoad.getGroupedKeySet())` — a single query by UUID set
3. Distributes results using `BiConsumer`

### Variant B: Manual load with aggregation (count, sum, etc.)

Used for calculated values that do not exist as a separate entity.

```java
public void loadUserCountForDomainBusinessAccounts(Collection<DomainBusinessAccountEntity> srcCollection) throws ServiceException {
    var needLoad = domainBusinessAccountService.getNeedLoad(srcCollection, DomainBusinessAccountEntity::getUsersCount);
    if (MapUtils.isEmpty(needLoad))
        return;
    List<EntryCount> entryCounts = userRepository.countUsersInBusinessAccounts(needLoad.keySet(), domainId);
    for (EntryCount entryCount : entryCounts) {
        needLoad.get(entryCount.id()).setUsersCount(entryCount.count());
    }
}
```

* Instead of `findEntitiesSafe()` — a custom aggregating query (GROUP BY + COUNT)
* Result is a list of `EntryCount(id, count)`, not entities
* `getNeedLoad()` — helper that filters by `null` value of the loaded field with domain validation

### Variant D: Load with KitGrouped (one-to-many)

Used for loading collections of related entities (one-to-many). Delegates to `EntitySecureFindServiceImpl.loadKitGrouped()`.

```java
// AttachmentService.loadAttachments()
public void loadAttachments(Collection<TwinEntity> twinEntityList) {
    loadKitGrouped(
        twinEntityList,
        TwinEntity::getId,                          // source entity ID getter
        TwinEntity::getAttachmentKit,               // @Transient field getter (null = not loaded)
        twinAttachmentRepository::findByTwinIdIn,   // batch query (Set<UUID> → Collection<R>)
        TwinAttachmentEntity::getId,                // result entity ID getter
        TwinAttachmentEntity::getTwinId,            // result entity FK to source
        TwinEntity::setAttachmentKit);              // setter
}
```

**What `loadKitGrouped()` does** (`EntitySecureFindServiceImpl`):

1. Builds `Kit<S, UUID> needLoad` — filters entities where @Transient field is null
2. Calls `queryFunction.apply(needLoad.getIdSet())` — single batch query
3. Wraps results into `KitGrouped<R, UUID, UUID>` — groups by FK to source
4. For each source: if data exists → `new Kit<>(groupedList, resultGetId)`, else → `Kit.emptyKit()`

**With element transformation** (when result entities need mapping before storing):

```java
// TwinTagService.loadTags()
public void loadTags(Collection<TwinEntity> twinEntityList) {
    loadKitGrouped(
        twinEntityList,
        TwinEntity::getId,
        TwinEntity::getTwinTagKit,
        twinTagRepository::findByTwinIdIn,
        TwinTagEntity::getId,
        TwinTagEntity::getTwinId,
        (twin, kit) -> twin.setTwinTagKit(kit.isEmpty()
            ? Kit.emptyKit()
            : new Kit<>(kit.getList().stream().map(TwinTagEntity::getTagDataListOption).toList(), DataListOptionEntity::getId)));
}
```

**With KitGrouped result** (when result needs secondary grouping — cannot use `loadKitGrouped`, write manually):

```java
// TwinFieldAttributeService.loadAttributes()
public void loadAttributes(Collection<TwinEntity> twinEntityList) {
    Kit<TwinEntity, UUID> needLoad = new Kit<>(TwinEntity::getId);
    for (TwinEntity twinEntity : twinEntityList) {
        if (twinEntity.getTwinFieldAttributeKit() == null)
            needLoad.add(twinEntity);
    }
    if (needLoad.isEmpty())
        return;
    KitGrouped<TwinFieldAttributeEntity, UUID, UUID> attributes = new KitGrouped<>(
        twinFieldAttributeRepository.findByTwinIdIn(needLoad.getIdSet()),
        TwinFieldAttributeEntity::getId,
        TwinFieldAttributeEntity::getTwinId);
    for (TwinEntity twinEntity : needLoad) {
        if (attributes.containsGroupedKey(twinEntity.getId()))
            twinEntity.setTwinFieldAttributeKit(new KitGrouped<>(
                attributes.getGrouped(twinEntity.getId()),
                TwinFieldAttributeEntity::getId,
                TwinFieldAttributeEntity::getTwinClassFieldId));
        else
            twinEntity.setTwinFieldAttributeKit(KitGrouped.EMPTY);
    }
}
```

**Key points:**

* `loadKitGrouped()` handles the full cycle: filter → query → group → assign
* No extra allocations — Kit is created only when data exists, otherwise `Kit.emptyKit()` singleton is used
* Use custom setter lambda when transformation is needed (e.g., extracting nested objects)
* When result needs `KitGrouped` (not `Kit`), write the pattern manually

## Load State Management (LoadState)

For complex load operations that may be called recursively or may fail, `LoadState` is used:

```java
public enum LoadState {
    NOT_LOADED,   // data has not been requested yet
    LOADING,      // loading in progress (recursion protection)
    LOADED,       // data loaded
    LOAD_ERROR    // loading failed
}
```

**Lifecycle:**

```java
public void loadMarkers(Collection<TwinEntity> twinEntityList) throws ServiceException {
    List<TwinEntity> twinsToLoad = loadStart(twinEntityList, TwinEntity::getMarkersLoadState, TwinEntity::setMarkersLoadState);
    if (twinsToLoad.isEmpty()) return;
    try {
        loadStaticMarkers(twinsToLoad);
        twinClassDynamicMarkerService.loadDynamicMarkers(twinsToLoad);
        loadFinish(twinsToLoad, TwinEntity::setMarkersLoadState);
    } catch (ServiceException e) {
        loadError(twinsToLoad, TwinEntity::setMarkersLoadState);
        throw e;
    }
}
```

**Methods from `EntitySecureFindServiceImpl`:**

* `loadStart(collection, stateGetter, stateSetter)` — filters `NOT_LOADED`, sets `LOADING`, throws `RECURSIVE_LOAD_DETECTED` if already `LOADING`
* `loadFinish(collection, stateSetter)` — sets `LOADED`
* `loadError(collection, stateSetter)` — sets `LOAD_ERROR`

## When to Use LoadState

| Situation                                  | LoadState Required?             |
| ------------------------------------------ | ------------------------------- |
| Simple FK loading (Variant A)              | No — `null` check is sufficient |
| Aggregation (Variant B)                    | No — `null` check is sufficient |
| Collection loading (Variant D)             | No — `null` check is sufficient |
| Recursive loading (dependent load methods) | Yes — recursion protection      |
| Composite loading from multiple sources    | Yes — state atomicity           |

## Summary Table of Variants

| Variant | Scenario                | Key Tools                            | Example                                     |
| ------- | ----------------------- | ------------------------------------ | ------------------------------------------- |
| A       | ManyToOne by FK         | `EntitySecureFindServiceImpl.load()` | `loadTwinflowSchema`, `loadTwinClassSchema` |
| B       | Aggregation (count/sum) | `getNeedLoad()`, aggregating SQL     | `loadUserCountForDomainBusinessAccounts`    |
| D       | One-to-many             | `Kit`, `KitGrouped`                  | `loadAttributes`, `loadTags`, `loadMarkers` |

## Naming Conventions

* `load{FieldName}` — load a single field (for example, `loadTags`, `loadTwinflowSchema`)
* `load{FieldName}(E entity)` — overload for a single element, delegates to `load{FieldName}(Collection<E>)`
* Always provide an overload for `Collection` — single-element version should use `Collections.singletonList()`

## Load Method Placement

Load methods are placed in the service that owns the corresponding repository (i.e., the service managing the child entity), not in the parent entity's service. A coordinating method (e.g., `loadFactoryElements`) may reside in the parent service and delegate to sub-services.

**Why:** the service managing an entity has access to its repository and understands its loading specifics. This reduces coupling — the parent service doesn't bloat with load methods for every child type.

**Example:**
```
TwinFactoryEntity
  ├── twinFactoryMultiplierKit       → FactoryMultiplierService.loadFactoryMultipliers()
  ├── twinFactoryPipelineKit         → FactoryPipelineService.loadFactoryPipelines()
  ├── twinFactoryBranchKit           → FactoryBranchService.loadFactoryBranches()
  ├── twinFactoryEraserKit           → FactoryEraserService.loadFactoryErasers()
  └── twinFactoryTriggerKit          → FactoryTriggerService.loadFactoryTriggers()

TwinFactoryPipelineEntity
  └── twinFactoryPipelineStepKit     → FactoryPipelineStepService.loadFactoryPipelineSteps()

TwinFactoryMultiplierEntity
  └── twinFactoryMultiplierFilterKit → FactoryMultiplierService.loadFactoryMultiplierFilters()

Coordination:
  TwinFactoryService.loadFactoryElements() — delegates to all sub-services
```

## Examples in the Codebase

| Service                               | Method                                                                | Variant       |
| ------------------------------------- | --------------------------------------------------------------------- | ------------- |
| `DomainBusinessAccountService`        | `loadTwinflowSchema`, `loadTwinClassSchema`, `loadNotificationSchema` | A             |
| `UserService`                         | `loadUserCountForDomainBusinessAccounts`                              | B             |
| `TwinService`                         | `loadTwinCountForDomainBusinessAccounts`                              | B             |
| `TwinTagService`                      | `loadTags`                                                            | D             |
| `AttachmentService`                   | `loadAttachments`                                                     | D             |
| `CommentService`                      | `loadAttachments`                                                     | D             |
| `TwinFieldAttributeService`           | `loadAttributes`, `loadFieldAttributes`                               | D             |
| `TwinMarkerService`                   | `loadMarkers`                                                         | D + LoadState |
| `CommentActionService`                | `loadCommentActions`, `loadClassCommentActions*`                      | D + cascading |
| `HistoryNotificationRecipientService` | `loadCreatedByUser`                                                   | A             |
| `FactoryMultiplierService`            | `loadFactoryMultipliers`, `loadFactoryMultiplierFilters`              | D             |
| `FactoryPipelineService`              | `loadFactoryPipelines`                                                | D             |
| `FactoryPipelineStepService`          | `loadFactoryPipelineSteps`                                            | D             |
| `FactoryBranchService`                | `loadFactoryBranches`                                                 | D             |
| `FactoryEraserService`                | `loadFactoryErasers`                                                  | D             |
| `FactoryTriggerService`               | `loadFactoryTriggers`                                                 | D             |
