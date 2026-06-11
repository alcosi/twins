# Architecture: Duplicate API

**Date:** 2026-06-10
**Status:** Implemented
**Context:** TWINS-840 — duplicate domain entities (twin classes, statuses, fields) with optional child entities

---

## Context

The system needs to duplicate domain entities — create a deep copy of an entity with a new key and ID, optionally cascading to its child entities (fields, statuses, triggers, rules). Duplication is used for cloning twin class definitions, copying statuses between classes, and replicating field configurations.

---

## Decision

### Principle

Duplicate is implemented through the **existing entity `Service`** with a `duplicate(Collection<EntityDuplicate>)` method. The controller uses a **dedicated reverse mapper** (`DuplicateRestDTOReverseMapper`) to convert DTOs into domain `Duplicate` objects, and the standard forward mapper (`RestDTOMapper`) to convert the saved entities back to response DTOs.

### Why domain Duplicate object instead of passing DTOs to service

* The service layer must not depend on DTOs — domain objects (`TwinClassDuplicate`, `TwinStatusDuplicate`) carry both the request data and service-resolved entity references (`originalTwinClass`, `newTwinClass`).
* Domain objects are enriched during service execution: original entities are loaded, new IDs generated, defaults applied.
* The reverse mapper only converts DTO → domain; the service fills in entity references.

### Why batch `Collection<EntityDuplicate>` instead of single entity

* The controller accepts a list, allowing batch duplication in one request.
* Key uniqueness is validated across the batch (no duplicate keys in one request).
* Entities are saved in bulk via `saveSafe()`.

### Why existing Service instead of dedicated DuplicateService

* Duplicate is tightly coupled to the entity's creation logic (same validation, hierarchy refresh, counters reset).
* The service already has access to all required dependencies (`i18nService`, `twinClassService`, etc.).
* Child-entity duplication (`duplicateFieldsForClass`, `duplicateStatusesForClass`) is called from the parent service.

---

## Components

### 1. Request DTO — list of duplicate operations

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

### 2. Domain Duplicate Object — enriched by service

```java
// domain/twinclass/TwinClassDuplicate.java
@Data
@Accessors(chain = true)
public class TwinClassDuplicate {
    // From DTO (set by reverse mapper)
    private UUID originalTwinClassId;
    private String newKey;
    private boolean duplicateFields = false;
    private boolean duplicateStatuses = false;

    // Resolved by service
    private UUID newTwinClassId;
    private TwinClassEntity originalTwinClass;
    private TwinClassEntity newTwinClass;
}
```

```java
// domain/twinstatus/TwinStatusDuplicate.java
@Data
@Accessors(chain = true)
public class TwinStatusDuplicate {
    // From DTO
    private UUID originalTwinStatusId;
    private UUID newTwinClassId;      // optional — defaults to same class
    private String newKey;
    private boolean duplicateTriggers = false;

    // Resolved by service
    private UUID newTwinStatusId;
    private TwinStatusEntity originalTwinStatus;
    private TwinClassEntity newTwinClass;
    private TwinStatusEntity newTwinStatus;
}
```

Two groups of fields:
* **From DTO** — set by the reverse mapper. Raw input data.
* **Resolved by service** — populated during service execution via `load()` helpers and entity creation.

### 3. Reverse Mapper — DTO → Domain object

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

### 4. Controller

```java
@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_CREATE})
public class TwinClassDuplicateController extends ApiController {
    private final TwinClassService twinClassService;
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
            var duplicatedClasses = twinClassService.duplicate(duplicates);
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
* Four injected dependencies: `{Entity}Service`, `{Entity}DuplicateRestDTOReverseMapper`, `{Entity}RestDTOMapper`, `RelatedObjectsRestDTOConverter`
* `@MapperContextBinding` with `roots = EntityRestDTOMapper.class` and `response = EntityListRsDTOv1.class`
* `@ProtectedBy` with `_CREATE` permission (creating new entities)
* Endpoint: `POST /private/{entity_snake_case}/duplicate/v1`
* Reuses existing `{Entity}ListRsDTOv1` for response — no dedicated duplicate response DTO

### 5. Service — duplicate method

```java
// In existing {Entity}Service.java
@Transactional
public Collection<Entity> duplicate(Collection<EntityDuplicate> duplicates) throws ServiceException {
    // 1. Validate key uniqueness in batch
    var newKeys = new HashSet<String>();
    for (var duplicate : duplicates) {
        if (newKeys.contains(duplicate.getNewKey()))
            throw new ServiceException(ErrorCode.KEY_ALREADY_IN_USE, "...");
        newKeys.add(duplicate.getNewKey());
    }

    // 2. Load original entities
    loadOriginalEntities(duplicates);

    // 3. Resolve defaults (e.g., newTwinClassId = original.twinClassId)
    // 4. Load target parent entities (if applicable)

    // 5. Create new entities by copying properties from originals
    var entitiesToSave = new ArrayList<Entity>();
    for (var duplicate : duplicates) {
        var original = duplicate.getOriginalEntity();
        var newEntity = new Entity()
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCode.KEY_INCORRECT))
                // ... copy all business fields from original
                // ... reset counters, set createdBy, set domainId
                ;
        // 6. Duplicate i18n
        setI18nForDuplicate(original, newEntity);

        duplicate.setNewEntity(newEntity);
        entitiesToSave.add(newEntity);
    }

    // 7. Save in bulk
    var saved = StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();

    // 8. Post-save operations (hierarchy refresh, child duplication)
    for (var duplicate : duplicates) {
        if (duplicate.isDuplicateChildren()) {
            childService.duplicateChildrenForParent(duplicate.getOriginalEntity(), duplicate.getNewEntity());
        }
    }

    return saved;
}
```

### 6. Service helpers

```java
// Load original entities from DB
private void loadOriginalEntities(Collection<EntityDuplicate> duplicates) throws ServiceException {
    load(duplicates,
            EntityDuplicate::getOriginalEntityId,
            EntityDuplicate::getOriginalEntity,
            EntityDuplicate::setOriginalEntity);
}

// Copy i18n strings (creates new I18nEntity for each non-null i18n field)
private void setI18nForDuplicate(Entity src, Entity dst) {
    if (src.getNameI18nId() != null) {
        dst.setNameI18nId(i18nService.duplicateI18n(src.getNameI18nId()).getId());
    }
    if (src.getDescriptionI18nId() != null) {
        dst.setDescriptionI18nId(i18nService.duplicateI18n(src.getDescriptionI18nId()).getId());
    }
}
```

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
  +-- Service.duplicate(Collection<EntityDuplicate>)
  |     |
  |     +-- Validate: key uniqueness in batch
  |     +-- load(): resolve originalEntityId -> originalEntity (from DB)
  |     +-- (optional) load(): resolve newTwinClassId -> newTwinClass
  |     +-- Generate new entity ID (UUID.nameUUIDFromBytes or let DB generate)
  |     +-- Create new entity: copy fields from original, reset counters
  |     +-- i18nService.duplicateI18n(): copy i18n + translations
  |     +-- saveSafe(): persist all new entities
  |     +-- Post-save: refresh hierarchies, cascade to child entities
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

The entity is a top-level domain object. Child duplication (fields, statuses) is controlled by boolean flags and delegated to child services.

```
TwinClassDuplicateController
  -> TwinClassService.duplicate()
       -> i18nService.duplicateI18n()
       -> saveSafe(twinClasses)
       -> twinClassFieldService.duplicateFieldsForClass()   (if duplicateFields)
       -> twinStatusService.duplicateStatusesForClass()     (if duplicateStatuses)
```

### Pattern B: Entity with optional target parent (TwinStatus, TwinClassField)

The duplicated entity can optionally be moved to a different parent. `newTwinClassId` defaults to the original entity's parent if not provided.

```
TwinStatusDuplicateController
  -> TwinStatusService.duplicate()
       -> load original status
       -> default newTwinClassId = original.twinClassId  (if not provided)
       -> load target twinClass
       -> create new status, copy fields
       -> i18nService.duplicateI18n()
       -> saveSafe(statuses)
```

---

## How to Add a New Duplicate API

### Step 1: Create Domain Duplicate Object

```java
// domain/foo/FooDuplicate.java
@Data
@Accessors(chain = true)
public class FooDuplicate {
    // From DTO
    private UUID originalFooId;
    private String newKey;
    private boolean duplicateChildren = false;

    // Resolved by service
    private UUID newFooId;
    private FooEntity originalFoo;
    private FooEntity newFoo;
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
                .setOriginalFooId(src.getOriginalFooId())
                .setNewKey(src.getNewKey())
                .setDuplicateChildren(src.isDuplicateChildren());
    }
}
```

### Step 4: Add `duplicate()` Method to Existing Service

```java
// In FooService.java
@Transactional
public Collection<FooEntity> duplicate(Collection<FooDuplicate> duplicates) throws ServiceException {
    var newKeys = new HashSet<String>();
    for (var duplicate : duplicates) {
        if (newKeys.contains(duplicate.getNewKey()))
            throw new ServiceException(ErrorCodeTwins.FOO_KEY_ALREADY_IN_USE, "...");
        newKeys.add(duplicate.getNewKey());
    }
    loadOriginalFoos(duplicates);
    var entitiesToSave = new ArrayList<FooEntity>();
    for (var duplicate : duplicates) {
        var original = duplicate.getOriginalFoo();
        var newEntity = new FooEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCodeTwins.FOO_KEY_INCORRECT))
                // copy fields from original
                ;
        setI18nForDuplicate(original, newEntity);
        duplicate.setNewFoo(newEntity);
        entitiesToSave.add(newEntity);
    }
    return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
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
    private final FooService fooService;
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
            var duplicated = fooService.duplicate(duplicates);
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

1. **Domain Duplicate object** — always create a domain object (e.g., `TwinClassDuplicate`) in `domain/{domain}/`, never pass DTOs to the service.
2. **Key uniqueness validation** — check for duplicate `newKey` values within the same request batch. Throw `ServiceException` with specific `ErrorCode`.
3. **`load()` helper** — use the generic `load()` method from `EntitySecureFindServiceImpl` to resolve IDs to entities.
4. **i18n duplication** — use `i18nService.duplicateI18n()` for each non-null i18n field. Never share i18n entities between original and duplicate.
5. **Reset counters** — duplicate entities must reset counters (e.g., `twinCounter = 0`, `directChildren = 0`) and flags (`hasSegment = false`).
6. **`@Transactional`** — the service `duplicate()` method must be annotated with `@Transactional`.
7. **`@ProtectedBy({Permissions.ENTITY_CREATE})`** — duplicate requires CREATE permission (it creates new entities).
8. **Reuse response DTO** — use the existing `{Entity}ListRsDTOv1` for the response. Do not create a dedicated duplicate response DTO.
9. **Boolean defaults are `false`** — child-entity duplication flags are opt-in.
10. **`KeyUtils`** — use `KeyUtils.upperCaseNullFriendly()` or `KeyUtils.lowerCaseNullSafe()` to normalize and validate the new key.

---

## File Naming Convention

| Layer | File | Package |
|---|---|---|
| Domain Object | `FooDuplicate` | `domain.foo` |
| Request DTO | `FooDuplicateRqDTOv1` | `dto.rest.foo` |
| Item DTO | `FooDuplicateDTOv1` | `dto.rest.foo` |
| Reverse Mapper | `FooDuplicateRestDTOReverseMapper` | `mappers.rest.foo` |
| Controller | `FooDuplicateController` | `controller.rest.priv.foo` |
| Service method | `FooService.duplicate()` | `service.foo` |

---

## Existing Implementations

| Entity | Controller | Service method | Child options |
|---|---|---|---|
| TwinClass | `TwinClassDuplicateController` | `TwinClassService.duplicate()` | `duplicateFields`, `duplicateStatuses` |
| TwinStatus | `TwinStatusDuplicateController` | `TwinStatusService.duplicate()` | `duplicateTriggers` (todo) |
| TwinClassField | `TwinClassFieldDuplicateController` | `TwinClassFieldService.duplicateFields()` | `duplicateRules` (todo) |
