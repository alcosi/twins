# Architecture: Duplicate API

**Date:** 2026-06-11
**Status:** Implemented
**Context:** TWINS-798 — extract duplicate logic into dedicated `<Entity>DuplicateService` classes extending common `EntityDuplicateService`

---

## Context

The system needs to duplicate domain entities — create a deep copy of an entity with a new key and ID, optionally cascading to its child entities (fields, statuses, triggers, rules). Duplication is used for cloning twin class definitions, copying statuses between classes, and replicating field configurations.

---

## Decision

### Principle

Duplicate logic lives in a **dedicated `{Entity}DuplicateService extends EntityDuplicateService<D, E>`** — one per entity type. The base `EntityDuplicateService<D, E>` provides the common skeleton (key validation, loading originals, saving, template method hooks). Each subclass implements entity-specific logic (field copying, i18n, post-save cascade).

### Why dedicated DuplicateService instead of adding duplicate() to existing Service

* Separation of concerns: the entity service already handles CRUD, hierarchy, and queries. Duplicate is a distinct operation with its own dependencies.
* The base class eliminates boilerplate across duplicate implementations (key uniqueness, load originals, save, hooks).
* Controllers inject only the duplicate service — cleaner dependency graph.
* Adding new duplicate APIs requires only a new `{Entity}DuplicateService` subclass, no modification to existing entity services.

### Why domain Duplicate object instead of passing DTOs to service

* The service layer must not depend on DTOs — domain objects (`TwinClassDuplicate`, `TwinStatusDuplicate`) carry both the request data and service-resolved entity references (`originalTwinClass`, `newTwinClass`).
* Domain objects are enriched during service execution: original entities are loaded, new IDs generated, defaults applied.
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
    private String newKey;
    private E originalEntity;
    private E newEntity;
}
```

All domain duplicate objects extend `EntityDuplicate<E>`. Subclasses add entity-specific fields (e.g., `duplicateFields`, `newTwinClassId`).

### 2. EntityDuplicateService<D extends EntityDuplicate<E>, E> — abstract base service

```java
// org.twins.core.service.EntityDuplicateService
public abstract class EntityDuplicateService<D extends EntityDuplicate<E>, E> {

    // Must implement
    protected abstract EntitySecureFindServiceImpl<E> entityService();
    protected abstract E createNewEntity(D duplicate, E original) throws ServiceException;
    protected abstract ErrorCode getKeyDuplicatedErrorCode();
    protected abstract void duplicateI18nFields(E src, E dst) throws ServiceException;

    // Hooks — optional overrides
    protected void prepareDuplicates(Collection<D> duplicates) throws ServiceException {}
    protected void afterSave(Collection<D> duplicates, Collection<E> saved) throws ServiceException {}

    // Template method — uses EntityDuplicate base fields directly
    @Transactional
    public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) return Collections.emptyList();
        validateKeyUniqueness(duplicates);     // 1 — uses duplicate.getNewKey()
        loadOriginalEntities(duplicates);      // 2 — uses EntityDuplicate getters/setters
        prepareDuplicates(duplicates);         // 3 — hook
        // 4 — create new entities + i18n
        var entitiesToSave = new ArrayList<E>();
        for (var duplicate : duplicates) {
            var original = duplicate.getOriginalEntity();
            var newEntity = createNewEntity(duplicate, original);
            duplicateI18nFields(original, newEntity);
            duplicate.setNewEntity(newEntity);
            entitiesToSave.add(newEntity);
        }
        var saved = StreamSupport.stream(entityService().saveSafe(entitiesToSave).spliterator(), false).toList(); // 5
        afterSave(duplicates, saved);          // 6 — hook
        return saved;
    }

    // Built-in: key uniqueness check
    protected void validateKeyUniqueness(Collection<D> duplicates) throws ServiceException { ... }

    // Built-in: load originals using entity service's load()
    protected void loadOriginalEntities(Collection<D> duplicates) throws ServiceException { ... }
}
```

**Type parameters:**
* `D` — domain duplicate object (e.g., `TwinClassDuplicate`)
* `E` — JPA entity type (e.g., `TwinClassEntity`)

**Delegates to entity service for:**
* `entityService().load()` — resolving IDs to entities
* `entityService().saveSafe()` — persisting with validation

### 2. Request DTO — list of duplicate operations

```java
// TwinClassDuplicateRqDTOv1.java
@Schema(name = "TwinClassDuplicateRqV1")
public class TwinClassDuplicateRqDTOv1 extends Request {
    @Schema(description = "duplicates list")
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

### 3. Domain Duplicate Object — enriched by service

Each duplicate object extends `EntityDuplicate<E>` which provides `originalEntityId`, `newKey`, `originalEntity`, `newEntity`. Subclasses add entity-specific fields.

```java
// domain/twinclass/TwinClassDuplicate.java
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassDuplicate extends EntityDuplicate<TwinClassEntity> {
    // Entity-specific fields
    private UUID newTwinClassId;
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
    // Entity-specific fields
    private UUID newTwinClassId;
    private UUID newTwinStatusId;
    private boolean duplicateTriggers = false;
    private TwinClassEntity newTwinClass;
}
```

Field groups:
* **From EntityDuplicate base** — `originalEntityId`, `newKey` (set by reverse mapper), `originalEntity`, `newEntity` (resolved by service).
* **Subclass-specific** — additional request fields and resolved references.

### 4. Reverse Mapper — DTO -> Domain object

```java
// TwinClassDuplicateRestDTOReverseMapper.java
@Component
@RequiredArgsConstructor
public class TwinClassDuplicateRestDTOReverseMapper
        extends RestSimpleDTOMapper<TwinClassDuplicateDTOv1, TwinClassDuplicate> {

    @Override
    public void map(TwinClassDuplicateDTOv1 src, TwinClassDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalTwinClassId(src.getOriginalTwinClassId())
                .setNewKey(src.getNewKey())
                .setDuplicateFields(src.isDuplicateFields())
                .setDuplicateStatuses(src.isDuplicateStatuses());
    }
}
```

Extends `RestSimpleDTOMapper<DTO, DomainObject>`. Maps only the DTO fields — entity references are left for the service. Named `{Entity}DuplicateRestDTOReverseMapper`.

### 5. Controller

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
            @RequestBody TwinClassDuplicateRqDTOv1 request) {
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

### 6. Duplicate Service — concrete implementation

Each `{Entity}DuplicateService` extends `EntityDuplicateService<D extends EntityDuplicate<E>, E>` and implements:

| Method | Purpose |
|---|---|
| `entityService()` | Returns the entity's service for `load()` and `saveSafe()` |
| `createNewEntity()` | Entity-specific field copying from original to new entity |
| `duplicateI18nFields()` | Entity-specific i18n duplication |
| `getKeyDuplicatedErrorCode()` | ErrorCode for duplicate key in batch |
| `prepareDuplicates()` (optional) | Batch preparation: resolve defaults, load target parents |
| `afterSave()` (optional) | Post-save: refresh hierarchies, cascade child duplication |

Common logic (key validation, loading originals, saving) is handled by the base class using `EntityDuplicate` fields directly — no accessor boilerplate needed.

---

## Data Flow

```
Controller
  |
  +-- Request DTO (List<EntityDuplicateDTO>)
  |
  +-- Reverse Mapper: DTO -> Domain Object (EntityDuplicate)
  |     |
  |     +-- Sets: originalEntityId, newKey, duplicateFields/duplicateStatuses flags
  |
  +-- EntityDuplicateService.duplicate(Collection<EntityDuplicate>)
  |     |
  |     +-- validateKeyUniqueness(): check for duplicate keys in batch
  |     +-- loadOriginalEntities(): resolve originalEntityId -> originalEntity (via entityService.load)
  |     +-- prepareDuplicates(): hook — resolve defaults, load target parents
  |     +-- createNewEntity(): copy fields from original, reset counters
  |     +-- duplicateI18nFields(): copy i18n + translations
  |     +-- entityService.saveSafe(): persist all new entities
  |     +-- afterSave(): hook — refresh hierarchies, cascade to child entities
  |
  +-- Forward Mapper: saved entities -> response DTOs
  +-- RelatedObjectsRestDTOConverter: enrich related objects
  |
  +-- Response: EntityListRsDTO (reuses existing response DTO)
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

### Pattern A: Self-contained entity with child cascade (TwinClass)

The entity is a top-level domain object. Child duplication (fields, statuses) is controlled by boolean flags and handled in `afterSave()`.

```
TwinClassDuplicateController
  -> TwinClassDuplicateService.duplicate()
       [base] validateKeyUniqueness()
       [base] loadOriginalEntities()
       [subclass] prepareDuplicates() — generate newTwinClassId
       [subclass] createNewEntity() — copy fields, reset counters
       [subclass] duplicateI18nFields() — name, description
       [base] save via entityService.saveSafe()
       [subclass] afterSave() — refresh hierarchies, cascade:
           -> twinClassFieldService.duplicateFieldsForClass()   (if duplicateFields)
           -> twinStatusService.duplicateStatusesForClass()     (if duplicateStatuses)
```

### Pattern B: Entity with optional target parent (TwinStatus, TwinClassField)

The duplicated entity can optionally be moved to a different parent. `newTwinClassId` defaults to the original entity's parent if not provided.

```
TwinStatusDuplicateController
  -> TwinStatusDuplicateService.duplicate()
       [base] validateKeyUniqueness()
       [base] loadOriginalEntities()
       [subclass] prepareDuplicates() — default newTwinClassId, load target class
       [subclass] createNewEntity() — copy fields
       [subclass] duplicateI18nFields() — name, description
       [base] save via entityService.saveSafe()
```

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
public class FooDuplicateService extends EntityDuplicateService<FooDuplicate, FooEntity> {

    private final FooService fooService;
    private final I18nService i18nService;

    @Override protected EntitySecureFindServiceImpl<FooEntity> entityService() { return fooService; }
    @Override protected ErrorCode getKeyDuplicatedErrorCode() { return ErrorCodeTwins.FOO_KEY_ALREADY_IN_USE; }

    @Override
    protected FooEntity createNewEntity(FooDuplicate duplicate, FooEntity original) throws ServiceException {
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
    protected void afterSave(Collection<FooDuplicate> duplicates, Collection<FooEntity> saved) throws ServiceException {
        // Optional: cascade to child entities
    }
}
```

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
            @RequestBody FooDuplicateRqDTOv1 request) {
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

1. **Dedicated DuplicateService** — each entity gets its own `{Entity}DuplicateService extends EntityDuplicateService<D, E>`. Never add `duplicate()` to the entity service.
2. **Domain Duplicate object** — always create a domain object extending `EntityDuplicate<E>` (e.g., `TwinClassDuplicate extends EntityDuplicate<TwinClassEntity>`) in `domain/{domain}/`. Never pass DTOs to the service.
3. **Key uniqueness validation** — handled by base class `validateKeyUniqueness()`. Uses `duplicate.getNewKey()` from `EntityDuplicate` base. Throws `ServiceException` with entity-specific `ErrorCode`.
4. **`loadOriginalEntities()`** — uses `entityService().load()` with `EntityDuplicate` getters/setters. No manual load helpers or accessor methods needed.
5. **i18n duplication** — implemented in `duplicateI18nFields()`. Use `i18nService.duplicateI18n()` for each non-null i18n field. Never share i18n entities between original and duplicate.
6. **Reset counters** — duplicate entities must reset counters (e.g., `twinCounter = 0`, `directChildren = 0`) and flags (`hasSegment = false`).
7. **`@Transactional`** — the base class `duplicate()` method is `@Transactional`.
8. **`@ProtectedBy({Permissions.ENTITY_CREATE})`** — duplicate requires CREATE permission (it creates new entities).
9. **Reuse response DTO** — use the existing `{Entity}ListRsDTOv1` for the response. Do not create a dedicated duplicate response DTO.
10. **Boolean defaults are `false`** — child-entity duplication flags are opt-in.
11. **`KeyUtils`** — use `KeyUtils.upperCaseNullFriendly()` or `KeyUtils.lowerCaseNullSafe()` to normalize and validate the new key.
12. **`entityService()`** — returns the existing entity service for `load()` and `saveSafe()`. Never call `entityRepository()` directly from the duplicate service.

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

| Entity | Controller | Duplicate Service | Child options |
|---|---|---|---|
| TwinClass | `TwinClassDuplicateController` | `TwinClassDuplicateService` | `duplicateFields`, `duplicateStatuses` |
| TwinStatus | `TwinStatusDuplicateController` | `TwinStatusDuplicateService` | `duplicateTriggers` (todo) |
| TwinClassField | `TwinClassFieldDuplicateController` | `TwinClassFieldDuplicateService` | `duplicateRules` (todo) |
