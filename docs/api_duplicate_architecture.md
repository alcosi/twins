# Architecture: Duplicate API

**Date:** 2026-06-16
**Status:** Implemented
**Context:** TWINS-798 — extract duplicate logic into dedicated `<Entity>DuplicateService` classes extending common `EntityDuplicateService`. Extended in TWINS-835 with `duplicateFor(Map<P, P>)` for bulk parent-to-parent cascade and the `P` (parent entity) type parameter.

---

## Context

The system needs to duplicate domain entities — create a deep copy of an entity with a new key and ID, optionally cascading to its child entities (fields, statuses, triggers, rules). Duplication is used for cloning twin class definitions, copying statuses between classes, and replicating field configurations.

---

## Decision

### Principle

Duplicate logic lives in a **dedicated `{Entity}DuplicateService extends EntityDuplicateService<D, E, P>`** — one per entity type. The base `EntityDuplicateService<D, E, P>` provides two flows:

* `duplicate(Collection<D>)` — top-level flow: clone entities by ID + new key.
* `duplicateFor(Map<P, P>)` — cascade flow: clone every child of each source parent into the matching destination parent (e.g. clone all fields of class A into the freshly-duplicated class B).

`P` is the parent entity type. For top-level services (those driven by `duplicate()` rather than `duplicateFor()`), use `Void` — the parent-related abstract methods are never invoked in that case.

### Why dedicated DuplicateService instead of adding duplicate() to existing Service

* Separation of concerns: the entity service already handles CRUD, hierarchy, and queries. Duplicate is a distinct operation with its own dependencies.
* The base class eliminates boilerplate across duplicate implementations (key uniqueness, load originals, save, hooks).
* Controllers inject only the duplicate service — cleaner dependency graph.
* Adding new duplicate APIs requires only a new `{Entity}DuplicateService` subclass, no modification to existing entity services.

### Why domain Duplicate object instead of passing DTOs to service

* The service layer must not depend on DTOs — domain objects (`TwinClassDuplicate`, `TwinStatusDuplicate`) carry both the request data and service-resolved entity references (`originalEntity`, `newEntity`, `duplicateParentEntityId`).
* Domain objects are enriched during service execution: original entities are loaded, new entities created, IDs generated, defaults applied.
* The reverse mapper only converts DTO -> domain; the service fills in entity references.

### Why batch `Collection<EntityDuplicate>` instead of single entity

* The controller accepts a list, allowing batch duplication in one request.
* Key uniqueness is validated across the batch (no duplicate keys in one request).
* Entities are saved in bulk via `saveSafe()`.

---

## Components

### 1. EntityDuplicate<E> — base domain object

```java
// org.twins.core.domain.EntityDuplicate
@Data
@Accessors(chain = true)
public class EntityDuplicate<E> {
    private UUID originalEntityId;
    private UUID duplicateParentEntityId; // optional — target parent when copying to a different parent
    private String newKey;                // optional
    private E originalEntity;
    private E newEntity;
}
```

All domain duplicate objects extend `EntityDuplicate<E>`. Subclasses add entity-specific fields (e.g., `duplicateFields`, `duplicateStatuses`, `duplicateSteps`).

`duplicateParentEntityId` is used by `duplicate()` — it provides possibility to link duplicate to the new parent. For the top-level `duplicate()` flow it is set only when callers want to relocate the entity under a different parent (Pattern B).

### 2. EntityDuplicateService<D extends EntityDuplicate<E>, E, P> — abstract base service

```java
// org.twins.core.service.EntityDuplicateService
public abstract class EntityDuplicateService<D extends EntityDuplicate<E>, E, P> {

    // === Top-level flow hooks ===
    protected abstract EntitySecureFindServiceImpl<E> entityService();
    protected abstract E createNewEntity(D duplicate) throws ServiceException;
    protected abstract ErrorCode getKeyDuplicatedErrorCode();
    protected abstract void duplicateI18nFields(E src, E dst) throws ServiceException;
    protected abstract void setNewParentEntityId(E newEntity, UUID duplicateParentEntityId);

    // Optional hook
    protected void afterSave(Collection<D> duplicates, Collection<E> saved) throws ServiceException {}

    // === Cascade flow hooks (only relevant when P != Void) ===
    protected abstract D createNewDuplicate();                       // factory for fresh D instances
    protected abstract void loadFor(Collection<P> parents);          // load children into source parents
    protected abstract Kit<E, UUID> extractorChildren(P parent);     // read children's kit from a source parent
    protected abstract UUID extractParentId(P parent);               // read UUID from a destination parent

    // === Top-level entry point ===
    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) return Collections.emptyList();
        validateKeyUniqueness(duplicates);
        loadOriginalEntities(duplicates);
        var entitiesToSave = new ArrayList<E>();
        for (var duplicate : duplicates) {
            var original = duplicate.getOriginalEntity();
            var newEntity = createNewEntity(duplicate);
            if (duplicate.getDuplicateParentEntityId() != null) {
                setNewParentEntityId(newEntity, duplicate.getDuplicateParentEntityId());
            }
            duplicateI18nFields(original, newEntity);
            duplicate.setNewEntity(newEntity);
            entitiesToSave.add(newEntity);
        }
        var saved = StreamSupport.stream(entityService().saveSafe(entitiesToSave).spliterator(), false).toList();
        afterSave(duplicates, saved);
        return saved;
    }

    // === Cascade entry point: clone every child of each src parent into matching dst parent ===
    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicateFor(Map<P, P> parentMap) throws ServiceException {
        if (parentMap == null || parentMap.isEmpty()) return Collections.emptyList();
        loadFor(parentMap.keySet());
        Function<E, UUID> childIdExtractor = entityService().entityGetIdFunction();
        List<D> duplicates = new ArrayList<>();
        for (var entry : parentMap.entrySet()) {
            UUID destinationParentId = extractParentId(entry.getValue());
            Kit<E, UUID> children = extractorChildren(entry.getKey());
            if (KitUtils.isEmpty(children)) continue;
            for (E child : children) {
                // setters on EntityDuplicate<E> return EntityDuplicate<E>, not D — assign field-by-field
                D newDuplicate = createNewDuplicate();
                newDuplicate.setOriginalEntity(child);
                newDuplicate.setOriginalEntityId(childIdExtractor.apply(child));
                newDuplicate.setDuplicateParentEntityId(destinationParentId);
                duplicates.add(newDuplicate);
            }
        }
        return duplicate(duplicates);
    }

    // === Built-in helpers ===
    protected void validateKeyUniqueness(Collection<D> duplicates) throws ServiceException { /* skips null keys */ }
    protected void loadOriginalEntities(Collection<D> duplicates) throws ServiceException { /* entityService().load(...) */ }
}
```

**Type parameters:**
* `D` — domain duplicate object (e.g., `TwinClassDuplicate`)
* `E` — JPA entity type (e.g., `TwinClassEntity`)
* `P` — parent entity type (e.g., `TwinClassEntity` for fields, `TwinFactoryEntity` for branches). Use `Void` for top-level entities (e.g., `TwinClassEntity`, `TwinFactoryEntity`) that are never the target of `duplicateFor()`.

**Delegates to entity service for:**
* `entityService().load()` — resolving IDs to entities
* `entityService().saveSafe()` — persisting with validation
* `entityService().entityGetIdFunction()` — extracting UUID from a child entity

**`duplicateFor()` vs `duplicate()`:**
* `duplicate(Collection<D>)` is the entry point for top-level requests: caller supplies original IDs and new keys.
* `duplicateFor(Map<P, P>)` is the entry point for cascading duplication: caller supplies a `source -> destination` map; the service loads every child of each source, builds `D` instances pointing at the matching destination, and delegates back to `duplicate()` for the actual save.

**Top-level services (`P = Void`)** — implement `loadFor`/`extractorChildren`/`extractParentId` as no-ops or `return null`; these are never invoked because nothing calls `duplicateFor()` on a top-level service.

### 3. Request DTO — list of duplicate operations

```java
// TwinClassDuplicateRqDTOv1.java
@Schema(name = "TwinClassDuplicateRqV1")
public class TwinClassDuplicateRqDTOv1 extends Request {
    @Schema(description = "duplicates list")
    @Size(min = 1, max = 50)
    public List<TwinClassDuplicateDTOv1> duplicates;
}
```

```java
// TwinClassDuplicateDTOv1.java — individual duplicate operation
@Schema(name = "TwinClassDuplicateV1")
public class TwinClassDuplicateDTOv1 {
    @Schema(description = "original twin class id")
    public UUID originalTwinClassId;

    @Schema(description = "new class key", example = "PROJECT")
    public String newKey;

    @Schema(description = "[optional] duplicate all class fields")
    public boolean duplicateFields = false;

    @Schema(description = "[optional] duplicate all class statuses")
    public boolean duplicateStatuses = false;
}
```

Extends `Request`. Contains a `List<EntityDuplicateDTO>` of individual duplicate operations. Each item specifies the original entity ID, a new key, and boolean flags for optional child duplication. Reuses the standard entity response DTO (`EntityListRsDTOv1`) for the response.

### 4. Domain Duplicate Object — enriched by service

Each duplicate object extends `EntityDuplicate<E>` which provides `originalEntityId`, `duplicateParentEntityId`, `newKey`, `originalEntity`, `newEntity`. Subclasses add entity-specific boolean flags and (rarely) extra ID fields.

```java
// domain/twinclass/TwinClassDuplicate.java
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassDuplicate extends EntityDuplicate<TwinClassEntity> {
    // Entity-specific flags
    private boolean duplicateFields = false;
    private boolean duplicateStatuses = false;
}
```

```java
// domain/twinstatus/TwinStatusDuplicate.java
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinStatusDuplicate extends EntityDuplicate<TwinStatusEntity> {
    private boolean duplicateTriggers = false;
}
```

Field groups:
* **From EntityDuplicate base** — `originalEntityId` and `newKey` set by the reverse mapper; `originalEntity` and `newEntity` resolved by the service; `duplicateParentEntityId` set by the cascade flow or by Pattern B callers.
* **Subclass-specific** — additional request flags (e.g., `duplicateFields`, `duplicateStatuses`, `duplicateSteps`).

### 5. Reverse Mapper — DTO -> Domain object

```java
// TwinClassDuplicateRestDTOReverseMapper.java
@Component
@RequiredArgsConstructor
public class TwinClassDuplicateRestDTOReverseMapper
        extends RestSimpleDTOMapper<TwinClassDuplicateDTOv1, TwinClassDuplicate> {

    @Override
    public void map(TwinClassDuplicateDTOv1 src, TwinClassDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalTwinClassId())
                .setNewKey(src.getNewKey())
                .setDuplicateFields(src.isDuplicateFields())
                .setDuplicateStatuses(src.isDuplicateStatuses());
    }
}
```

Extends `RestSimpleDTOMapper<DTO, DomainObject>`. Maps only the DTO fields — entity references are left for the service. Named `{Entity}DuplicateRestDTOReverseMapper`.

### 6. Controller

```java
@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_CREATE})
public class TwinClassDuplicateController extends ApiController {
    private final TwinClassDuplicateService twinClassDuplicateService;
    private final TwinClassDuplicateRestDTOReverseMapper twinClassDuplicateRestDTOReverseMapper;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassDuplicateV1", summary = "Duplicates twin classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin classes copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/duplicate/v1")
    public ResponseEntity<?> twinClassDuplicateV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody TwinClassDuplicateRqDTOv1 request) {
        var rs = new TwinClassListRsDTOv1();
        try {
            var duplicates = twinClassDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicatedClasses = twinClassDuplicateService.duplicate(duplicates);
            rs
                    .setTwinClassList(twinClassRestDTOMapper.convertCollection(duplicatedClasses, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

Key points:
* Injects `{Entity}DuplicateService` (not the entity service) plus mapper dependencies
* `@MapperContextBinding` with `roots = EntityRestDTOMapper.class` and `response = EntityListRsDTOv1.class`
* `@ProtectedBy` with `_CREATE` permission (creating new entities)
* Endpoint: `POST /private/{entity_snake_case}/duplicate/v1`
* Reuses existing `{Entity}ListRsDTOv1` for response — no dedicated duplicate response DTO

### 7. Duplicate Service — concrete implementation

Each `{Entity}DuplicateService` extends `EntityDuplicateService<D extends EntityDuplicate<E>, E, P>` and implements:

| Method | Required | Purpose |
|---|---|---|
| `entityService()` | yes | Returns the entity's service for `load()`, `saveSafe()`, `entityGetIdFunction()` |
| `createNewEntity(D duplicate)` | yes | Entity-specific field copying; reads original via `duplicate.getOriginalEntity()` |
| `duplicateI18nFields(E src, E dst)` | yes | Entity-specific i18n duplication |
| `getKeyDuplicatedErrorCode()` | yes | ErrorCode for duplicate key in batch |
| `setNewParentEntityId(E newEntity, UUID)` | yes | Wire FK to destination parent (Pattern B / cascade). No-op for top-level entities without parent |
| `createNewDuplicate()` | yes | Factory for fresh `D` instances used by `duplicateFor()` |
| `loadFor(Collection<P> parents)` | yes | Bulk-load children into source parents. No-op when loading is performed externally (e.g., `FactoryDuplicateService` pre-loads) |
| `extractorChildren(P parent)` | yes | Read the children `Kit<E, UUID>` from a source parent |
| `extractParentId(P parent)` | yes | Read the UUID from a destination parent |
| `afterSave(duplicates, saved)` | optional | Post-save: refresh hierarchies, build per-flag maps, delegate to children via `duplicateFor()` |

`prepareDuplicates()` no longer exists — defaults and target-parent resolution are done inline in `createNewEntity()` / controller / reverse mapper as appropriate.

Common logic (key validation, loading originals, saving) is handled by the base class using `EntityDuplicate` fields directly — no accessor boilerplate needed.

---

## Data Flow

### Top-level request

```
Controller
  |
  +-- Request DTO (List<EntityDuplicateDTO>)
  |
  +-- Reverse Mapper: DTO -> Domain Object (EntityDuplicate)
  |     |
  |     +-- Sets: originalEntityId, newKey, duplicateXxx flags
  |
  +-- EntityDuplicateService.duplicate(Collection<D>)
  |     |
  |     +-- validateKeyUniqueness(): check for duplicate keys in batch (null keys skipped)
  |     +-- loadOriginalEntities(): resolve originalEntityId -> originalEntity (via entityService.load)
  |     +-- for each duplicate:
  |          +-- createNewEntity(): copy fields from original, reset counters
  |          +-- setNewParentEntityId(): applied only when duplicateParentEntityId != null
  |          +-- duplicateI18nFields(): copy i18n + translations
  |     +-- entityService.saveSafe(): persist all new entities
  |     +-- afterSave(): hook — refresh hierarchies, build per-flag maps, delegate each
  |                      to child services via duplicateFor(Map<srcParent, dstParent>)
  |
  +-- Forward Mapper: saved entities -> response DTOs
  +-- RelatedObjectsRestDTOConverter: enrich related objects
  |
  +-- Response: EntityListRsDTO (reuses existing response DTO)
```

### Cascade (invoked from a parent's `afterSave()`)

```
ParentService.afterSave()
  -> childDuplicateService.duplicateFor(parentMap)
       [child subclass] loadFor(parentMap.keySet())              — bulk load children into src parents
       [base]            for each (src -> dst):
                           extractParentId(dst)                  — destination UUID
                           extractorChildren(src)                — Kit<E, UUID>
                           for each child:
                             createNewDuplicate()                — fresh D
                             set originalEntity / originalEntityId / duplicateParentEntityId
       [base]            duplicate(childDuplicates)              — re-enters top-level flow with
                                                                 duplicateParentEntityId populated
                           -> createNewEntity() + setNewParentEntityId(newEntity, dstParentId)
                           -> entityService.saveSafe()
                           -> afterSave() (recurse for grandchildren)
```

---

## API Contract

**Endpoint:** `POST /private/{entity}/duplicate/v1`
**Content-Type:** `application/json`

**Request:**

```json
{
  "duplicates": [
    {
      "originalTwinClassId": "uuid-1",
      "newKey": "PROJECT_COPY",
      "duplicateFields": true,
      "duplicateStatuses": true
    },
    {
      "originalTwinClassId": "uuid-2",
      "newKey": "TASK_COPY",
      "duplicateFields": false,
      "duplicateStatuses": false
    }
  ]
}
```

**Response:** Reuses existing `{Entity}ListRsDTOv1` with created entities.

---

## Duplicate Patterns

### Pattern A: Top-level entity with child cascade via `duplicateFor()` (TwinClass, TwinFactory)

The entity is a top-level domain object. Child duplication is controlled by boolean flags and handled in `afterSave()` — but instead of writing bespoke cascade code, the parent service groups duplicates by flag into per-category maps and delegates each to the child service's `duplicateFor(Map<P, P>)`.

```
TwinClassDuplicateController
  -> TwinClassDuplicateService.duplicate()
       [base]    validateKeyUniqueness()
       [base]    loadOriginalEntities()
       [subclass] createNewEntity() — copy fields, reset counters
       [subclass] duplicateI18nFields() — name, description
       [base]    save via entityService.saveSafe()
       [subclass] afterSave() — refresh hierarchies, build per-category maps by flag,
                  then delegate each:
                   -> twinClassFieldDuplicateService.duplicateFor(copyFieldsFor)
                      [subclass] loadFor(srcClasses)   — twinClassFieldService::loadTwinClassFields
                      [subclass] extractorChildren(src) — src.getTwinClassFieldKit()
                      [subclass] extractParentId(dst)   — dst.getId()
                      [base]    duplicate(children)     — validates, copies, saves
                   -> twinStatusDuplicateService.duplicateFor(copyStatusesFor)  (same shape)
```

The parent's `afterSave()` is the only place that knows about the flags; everything below it is generic.

### Pattern B: Entity with optional target parent (TwinStatus, TwinClassField at top level)

The duplicated entity can optionally be moved to a different parent. Caller sets `duplicateParentEntityId` on the `EntityDuplicate`; the base `duplicate()` flow invokes `setNewParentEntityId(newEntity, duplicateParentEntityId)` only when that field is non-null. If absent, `createNewEntity()` keeps the original parent (the common case for batch class-to-class copies).

```
TwinStatusDuplicateController
  -> TwinStatusDuplicateService.duplicate()
       [base]    validateKeyUniqueness()
       [base]    loadOriginalEntities()
       [subclass] createNewEntity() — copy fields
       [base]    setNewParentEntityId() — applied only if duplicateParentEntityId != null
       [subclass] duplicateI18nFields() — name, description
       [base]    save via entityService.saveSafe()
```

### Pattern C: Child entity driven only by `duplicateFor()` (FactoryBranch, FactoryPipeline, …)

The entity never has its own top-level controller — it is only duplicated as part of cascading from its parent. Its service still extends `EntityDuplicateService<D, E, P>` and implements every abstract method, but `duplicate(Collection<D>)` is only ever reached transitively via the parent's `afterSave()` → `duplicateFor()` → `duplicate()` chain.

```
FactoryDuplicateService.afterSave()
  -> factoryBranchDuplicateService.duplicateFor(branchesMap)
       [subclass] loadFor(srcFactories)         — factoryBranchService::loadFactoryBranches
       [subclass] extractorChildren(srcFactory) — srcFactory.getTwinFactoryBranchKit()
       [subclass] extractParentId(dstFactory)   — dstFactory.getId()
       [base]    duplicate(branchDuplicates)
                   -> createNewEntity() — copy fields, setTwinFactoryId(parentId)
                   -> save via factoryBranchService.saveSafe()
```

Some cascading services themselves have children (e.g. `FactoryPipelineDuplicateService` cascades further to `FactoryStepDuplicateService` via its own `afterSave()`). The pattern recurses naturally — each level speaks the same `duplicateFor(Map<P, P>)` contract.

---

## How to Add a New Duplicate API

### Step 1: Create Domain Duplicate Object

```java
// domain/foo/FooDuplicate.java
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FooDuplicate extends EntityDuplicate<FooEntity> {
    // Entity-specific fields
    private boolean duplicateChildren = false;
}
```

### Step 2: Create Request DTO

```java
// dto/rest/foo/FooDuplicateDTOv1.java
@Schema(name = "FooDuplicateV1")
public class FooDuplicateDTOv1 {
    @Schema(description = "original foo id")
    public UUID originalFooId;

    @Schema(description = "new foo key", example = "FOO_COPY")
    public String newKey;

    @Schema(description = "[optional] duplicate child entities")
    public boolean duplicateChildren = false;
}
```

```java
// dto/rest/foo/FooDuplicateRqDTOv1.java
@Schema(name = "FooDuplicateRqV1")
public class FooDuplicateRqDTOv1 extends Request {
    @Schema(description = "duplicates list")
    @Size(min = 1, max = 50)
    public List<FooDuplicateDTOv1> duplicates;
}
```

### Step 3: Create Reverse Mapper

```java
// mappers/rest/foo/FooDuplicateRestDTOReverseMapper.java
@Component
@RequiredArgsConstructor
public class FooDuplicateRestDTOReverseMapper
        extends RestSimpleDTOMapper<FooDuplicateDTOv1, FooDuplicate> {

    @Override
    public void map(FooDuplicateDTOv1 src, FooDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFooId())
                .setNewKey(src.getNewKey());
        dst.setDuplicateChildren(src.isDuplicateChildren());
    }
}
```

### Step 4: Create Duplicate Service

```java
// service/foo/FooDuplicateService.java
@Slf4j
@Service
@RequiredArgsConstructor
// Use Void for P when Foo is top-level; replace with the parent entity type if Foo
// will be the target of duplicateFor() from a parent service.
public class FooDuplicateService extends EntityDuplicateService<FooDuplicate, FooEntity, Void> {

    private final FooService fooService;
    private final I18nService i18nService;

    @Override protected EntitySecureFindServiceImpl<FooEntity> entityService() { return fooService; }
    @Override protected ErrorCode getKeyDuplicatedErrorCode() { return ErrorCodeTwins.FOO_KEY_ALREADY_IN_USE; }

    // === Top-level flow ===
    @Override
    protected FooEntity createNewEntity(FooDuplicate duplicate) throws ServiceException {
        FooEntity original = duplicate.getOriginalEntity();
        return new FooEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCodeTwins.FOO_KEY_INCORRECT))
                // ... copy fields from original
                ;
    }

    @Override
    protected void duplicateI18nFields(FooEntity src, FooEntity dst) {
        if (src.getNameI18nId() != null)
            dst.setNameI18nId(i18nService.duplicateI18n(src.getNameI18nId()).getId());
    }

    @Override
    protected void setNewParentEntityId(FooEntity newEntity, UUID duplicateParentEntityId) {
        // no-op if Foo has no parent; otherwise wire the FK, e.g. newEntity.setContainerId(duplicateParentEntityId)
    }

    @Override
    protected void afterSave(Collection<FooDuplicate> duplicates, Collection<FooEntity> saved) throws ServiceException {
        // Optional: build per-flag maps and delegate to child services via duplicateFor(Map<P, P>)
    }

    // === Cascade flow (only required when P != Void) ===
    // For P = Void, return null / no-op — these are never invoked.
    @Override protected FooDuplicate createNewDuplicate() { return new FooDuplicate(); }
    @Override protected void loadFor(Collection<Void> parents) { /* no-op */ }
    @Override protected Kit<FooEntity, UUID> extractorChildren(Void parent) { return null; }
    @Override protected UUID extractParentId(Void parent) { return null; }
}
```

If the new entity is itself a **child** (cascade target), pick the concrete `P` and implement the four cascade hooks meaningfully — see `TwinClassFieldDuplicateService` (P = `TwinClassEntity`) or `FactoryBranchDuplicateService` (P = `TwinFactoryEntity`) for templates.

### Step 5: Create Controller

```java
// controller/rest/priv/foo/FooDuplicateController.java
@Tag(name = ApiTag.FOO)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FOO_CREATE})
public class FooDuplicateController extends ApiController {
    private final FooDuplicateService fooDuplicateService;
    private final FooDuplicateRestDTOReverseMapper fooDuplicateRestDTOReverseMapper;
    private final FooRestDTOMapper fooRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "fooDuplicateV1", summary = "Duplicates foos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foo copy result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FooListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/foo/duplicate/v1")
    public ResponseEntity<?> fooDuplicateV1(
            @MapperContextBinding(roots = FooRestDTOMapper.class, response = FooListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @Valid @RequestBody FooDuplicateRqDTOv1 request) {
        var rs = new FooListRsDTOv1();
        try {
            var duplicates = fooDuplicateRestDTOReverseMapper.convertCollection(request.duplicates, mapperContext);
            var duplicated = fooDuplicateService.duplicate(duplicates);
            rs
                    .setFooList(fooRestDTOMapper.convertCollection(duplicated, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

---

## Critical Rules

1. **Dedicated DuplicateService** — each entity gets its own `{Entity}DuplicateService extends EntityDuplicateService<D, E, P>`. Never add `duplicate()` to the entity service.
2. **Three type parameters** — `D` (duplicate domain object), `E` (entity), `P` (parent entity type). Use `Void` for `P` when the service is never the target of `duplicateFor()`.
3. **Domain Duplicate object** — always create a domain object extending `EntityDuplicate<E>` (e.g., `TwinClassDuplicate extends EntityDuplicate<TwinClassEntity>`) in `domain/{domain}/`. Never pass DTOs to the service.
4. **Key uniqueness validation** — handled by base class `validateKeyUniqueness()`. Uses `duplicate.getNewKey()` from `EntityDuplicate` base. **Null keys are skipped** (important for `duplicateFor()` flow where every generated child duplicate has a null `newKey`).
5. **`loadOriginalEntities()`** — uses `entityService().load()` with `EntityDuplicate` getters/setters. The `load()` helper is a no-op for items where `originalEntity` is already set, so `duplicateFor()` (which pre-populates `originalEntity`) does not re-fetch.
6. **i18n duplication** — implemented in `duplicateI18nFields()`. Use `i18nService.duplicateI18n()` for each non-null i18n field. Never share i18n entities between original and duplicate.
7. **Reset counters** — duplicate entities must reset counters (e.g., `twinCounter = 0`, `directChildren = 0`) and flags (`hasSegment = false`).
8. **`@Transactional(rollbackFor = Throwable.class)`** — the base class `duplicate()` and `duplicateFor()` methods are `@Transactional(rollbackFor = Throwable.class)`. The `rollbackFor` is required because `ServiceException` is a checked exception and Spring only rolls back on unchecked exceptions by default.
9. **`@Valid` + `@Size(min = 1, max = 50)`** — the controller must use `@Valid` on `@RequestBody` and the request DTO's `duplicates` list must have `@Size(min = 1, max = 50)` to enforce batch size limits and prevent unrestricted resource consumption (OWASP API4:2023).
10. **`@ProtectedBy({Permissions.ENTITY_CREATE})`** — duplicate requires CREATE permission (it creates new entities).
11. **Reuse response DTO** — use the existing `{Entity}ListRsDTOv1` for the response. Do not create a dedicated duplicate response DTO.
12. **Boolean defaults are `false`** — child-entity duplication flags are opt-in.
13. **`KeyUtils`** — use `KeyUtils.upperCaseNullFriendly()` or `KeyUtils.lowerCaseNullSafe()` to normalize and validate the new key.
14. **`entityService()`** — returns the existing entity service for `load()`, `saveSafe()`, and `entityGetIdFunction()`. Never call `entityRepository()` directly from the duplicate service.
15. **Cascade via `duplicateFor(Map<P, P>)`** — when cascading to children, do not write bespoke iteration loops. Group duplicates by flag into per-category `Map<srcParent, dstParent>` and delegate each to the child service's `duplicateFor()`. The child service handles `loadFor()` → `extractorChildren()` → `extractParentId()` → `duplicate()`.
16. **`createNewEntity()` signature** — takes only `D duplicate`; the original entity is read from `duplicate.getOriginalEntity()` inside the method.
17. **Chain-setter gotcha** — `EntityDuplicate<E>` setters return `EntityDuplicate<E>`, not `D`. Inside `duplicateFor()` you cannot chain `.setOriginalEntity(...).setOriginalEntityId(...)` and assign to `D`; assign field-by-field via a local `D newDuplicate = createNewDuplicate();` variable. This is handled in the base class — subclasses are unaffected.

---

## File Naming Convention

| Layer | File | Package |
|---|---|---|
| Base Domain Object | `EntityDuplicate` | `org.twins.core.domain` |
| Base Service | `EntityDuplicateService` | `org.twins.core.service` |
| Domain Object | `FooDuplicate extends EntityDuplicate` | `domain.foo` |
| Duplicate Service | `FooDuplicateService extends EntityDuplicateService` | `service.foo` |
| Domain Object | `FooDuplicate` | `domain.foo` |
| Request DTO | `FooDuplicateRqDTOv1` | `dto.rest.foo` |
| Item DTO | `FooDuplicateDTOv1` | `dto.rest.foo` |
| Reverse Mapper | `FooDuplicateRestDTOReverseMapper` | `mappers.rest.foo` |
| Controller | `FooDuplicateController` | `controller.rest.priv.foo` |

---

## Existing Implementations

`P` column shows the parent-entity type parameter — `Void` marks a top-level service driven by `duplicate()`; non-`Void` marks a service that participates in `duplicateFor()` cascades.

| Entity | Controller | Duplicate Service | P | Child options |
|---|---|---|---|---|
| TwinClass | `TwinClassDuplicateController` | `TwinClassDuplicateService` | `Void` | `duplicateFields`, `duplicateStatuses` |
| TwinStatus | `TwinStatusDuplicateController` | `TwinStatusDuplicateService` | `TwinClassEntity` | `duplicateTriggers` (todo) |
| TwinClassField | `TwinClassFieldDuplicateController` | `TwinClassFieldDuplicateService` | `TwinClassEntity` | `duplicateRules` (todo) |
| Factory | `FactoryDuplicateController` | `FactoryDuplicateService` | `Void` | `duplicateBranches`, `duplicateMultipliers`, `duplicatePipelines`, `duplicateErasers`, `duplicateTriggers` |
| FactoryPipeline | `FactoryPipelineDuplicateController` | `FactoryPipelineDuplicateService` | `TwinFactoryEntity` | `duplicateSteps` |
| FactoryBranch | `FactoryBranchDuplicateController` | `FactoryBranchDuplicateService` | `TwinFactoryEntity` | — |
| FactoryEraser | `FactoryEraserDuplicateController` | `FactoryEraserDuplicateService` | `TwinFactoryEntity` | — |
| FactoryMultiplier | `FactoryMultiplierDuplicateController` | `FactoryMultiplierDuplicateService` | `TwinFactoryEntity` | — |
| FactoryTrigger | `FactoryTriggerDuplicateController` | `FactoryTriggerDuplicateService` | `TwinFactoryEntity` | — |
