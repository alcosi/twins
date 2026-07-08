# API Starter: Instructions for Creating Settings CRUD API

This document describes the full chain for creating a REST API to manage settings entities — from controllers to the service layer. All new settings APIs **must** follow this template.

## Referenced Documents

| Document | Purpose |
|---|---|
| [dto_code_convention.md](dto_code_convention.md) | DTO naming, inheritance hierarchy, field conventions |
| [api_counting_architecture.md](api_counting_architecture.md) | Count API with grouping, `EntitySearchService`, `CountQueryExecutor` |
| [api_sorting_architecture.md](api_sorting_architecture.md) | Dynamic sorting via enum, `CommonSpecification`, I18n sorting |
| [load_method_pattern.md](load_method_pattern.md) | Batch loading of `@Transient` fields (N+1 prevention) |
| [api_export_architecture.md](api_export_architecture.md) | SQL Export API — `SqlBuilder`, `I18nExportService`, file download |

---

## 1. API Endpoints

Each settings entity exposes the following endpoints. All batch operations accept collections.

| Operation | Method | URL | Description |
|---|---|---|---|
| Create (batch) | `POST` | `/private/{entity}/v1` | Create one or more entities |
| Update (batch) | `PUT` | `/private/{entity}/v1` | Update one or more entities |
| Delete (batch) | `POST` | `/private/{entity}/delete/v1` | Delete one or more entities by ID list |
| View (single) | `GET` | `/private/{entity}/{entityId}/v1` | Get entity by ID |
| Search | `POST` | `/private/{entity}/search/v1` | Search with pagination + sorting |
| Count | `POST` | `/private/{entity}/search/count/v1` | Count with grouping |
| Export SQL | `POST` | `/private/{entity}/export/sql/v1` | Export entities as SQL INSERT file download |

> **Batch-first**: create and update always accept `List<...DTO>` even for a single item. The client sends a list with one element.

---

## 2. Files to Create

For an entity named `{Entity}`, the following files are required:

### 2.1 DTOs (`dto/rest/{domain}/`)

| File | Extends | Purpose |
|---|---|---|
| `{Entity}DTOv1` | — | Entity representation (response) |
| `{Entity}SaveDTOv1` | — | Abstract base for create/update common fields |
| `{Entity}CreateDTOv1` | `{Entity}SaveDTOv1` | Create-specific fields |
| `{Entity}UpdateDTOv1` | `{Entity}SaveDTOv1` | Update-specific fields + `UUID id` |
| `{Entity}SearchDTOv1` | — | Search parameters (no sort fields here) |
| `{Entity}CountDTOv1` | `CountDTOv1` | Count result with explicit groupable fields |

### 2.2 Request DTOs (`dto/rest/{domain}/`)

| File | Extends | Key Field |
|---|---|---|
| `{Entity}CreateRqDTOv1` | `Request` | `List<{Entity}CreateDTOv1> {entities}` |
| `{Entity}UpdateRqDTOv1` | `Request` | `List<{Entity}UpdateDTOv1> {entities}` |
| `{Entity}DeleteRqDTOv1` | `Request` | `Set<UUID> {entity}IdList` |
| `{Entity}SearchRqDTOv1` | `Request` | `{Entity}SearchDTOv1 search` + `sortField` + `sortDirection` |
| `{Entity}CountRqDTOv1` | `Request` | `{Entity}SearchDTOv1 search` + `Set<{Entity}GroupField> groupFields` |
| `{Entity}ExportSqlRqDTOv1` | `Request` | `Set<UUID> {entity}Ids` + optional boolean flags |

### 2.3 Response DTOs (`dto/rest/{domain}/`)

| File | Extends | Key Field |
|---|---|---|
| `{Entity}ListRsDTOv1` | `Response` | `List<{Entity}DTOv1> {entities}` |
| `{Entity}SearchRsDTOv1` | `{Entity}ListRsDTOv1` | `+ PaginationDTOv1 pagination` |
| `{Entity}ViewRsDTOv1` | `Response` | `{Entity}DTOv1 {entity}` |
| `{Entity}CountRsDTOv1` | `ResponseCountDTOv1` | `List<{Entity}CountDTOv1> counts` |

> Export has no response DTO — the controller returns `ResponseEntity<byte[]>` (raw file download).

### 2.4 Enums (`enums/sort/`)

| File | Purpose |
|---|---|
| `{Entity}SortField` | Sort field whitelist for search |
| `{Entity}GroupField` | Group field whitelist for count |

### 2.5 Mappers (`mappers/rest/{domain}/`)

| File | Direction | Purpose |
|---|---|---|
| `{Entity}RestDTOMapper` | Entity → `{Entity}DTOv1` | Forward mapper (extends `RestSimpleDTOMapper`) |
| `{Entity}CreateRestDTOReverseMapper` | `{Entity}CreateDTOv1` → domain Create object | Reverse mapper for create |
| `{Entity}UpdateRestDTOReverseMapper` | `{Entity}UpdateDTOv1` → domain Update object | Reverse mapper for update |
| `{Entity}SearchDTOReverseMapper` | `{Entity}SearchDTOv1` → `{Entity}Search` | Reverse mapper for search |
| `{Entity}CountRestDTOMapper` | `CountResult<E, GF>` → `{Entity}CountDTOv1` | Count result mapper |

### 2.6 Domain (`domain/{domain}/`)

| File | Purpose |
|---|---|
| `{Entity}Search` extends `EntitySearch<E>` | Search criteria object |
| `{Entity}Create` | Create domain command |
| `{Entity}Update` | Update domain command |




### 2.7 Services (`service/{domain}/`)

| File | Extends                                                               | Purpose                                                            |
|---|-----------------------------------------------------------------------|--------------------------------------------------------------------|
| `{Entity}Service` | `EntitySecureFindServiceImpl<E>` (via `TwinsEntitySecureFindService`) | CRUD operations, load methods, validation                          |
| `{Entity}SearchService` | `EntitySearchService<S, E, SF, GF>`                                   | Search + count with sorting and grouping                           |
| `{Entity}ExportService` |  `EntityExportService`    |  Composes SQL export (i18n → entity → children), uses `SqlBuilder` |

### 2.8 Controllers (`controller/rest/priv/{domain}/`)

| File | Endpoints |
|---|---|
| `{Entity}CreateController` | `POST /private/{entity}/v1` |
| `{Entity}UpdateController` | `PUT /private/{entity}/v1` |
| `{Entity}DeleteController` | `POST /private/{entity}/delete/v1` |
| `{Entity}ViewController` | `GET /private/{entity}/{id}/v1` |
| `{Entity}SearchController` | `POST /private/{entity}/search/v1` |
| `{Entity}CountController` | `POST /private/{entity}/count/v1` |
| `{Entity}ExportSqlController` | `POST /private/{entity}/export/sql/v1` |

> Controllers are split by operation — each in its own class — for clean permission isolation via `@ProtectedBy`.

---

## 3. Controller Templates

### 3.1 Create Controller (batch)

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.{ENTITY}_CREATE)
@Tag(name = ApiTag.{ENTITY})
public class {Entity}CreateController extends ApiController {

    private final {Entity}Service {entity}Service;
    private final {Entity}RestDTOMapper {entity}RestDTOMapper;
    private final {Entity}CreateRestDTOReverseMapper {entity}CreateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @PostMapping(value = "/private/{entity}/v1")
    public ResponseEntity<?> {entity}CreateV1(
            @MapperContextBinding(roots = {Entity}RestDTOMapper.class, response = {Entity}ListRsDTOv1.class)
            @RequestBody {Entity}CreateRqDTOv1 request) {
        {Entity}ListRsDTOv1 rs = new {Entity}ListRsDTOv1();
        try {
            List<{Entity}Create> createList = {entity}CreateRestDTOReverseMapper.convertCollection(request.get{Entities}());
            List<{Entity}Entity> created = {entity}Service.create{Entities}(createList);
            rs.set{Entities}({entity}RestDTOMapper.convertCollection(created, mapperContext))
              .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

### 3.2 Update Controller (batch)

Same structure as create, but:
- `@PutMapping`
- `@ProtectedBy(Permissions.{ENTITY}_UPDATE)`
- Uses `{Entity}UpdateRestDTOReverseMapper`
- Calls `{entity}Service.update{Entities}()`

### 3.3 Delete Controller (batch)

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.{ENTITY}_DELETE)
@Tag(name = ApiTag.{ENTITY})
public class {Entity}DeleteController extends ApiController {

    private final {Entity}Service {entity}Service;

    @PostMapping(value = "/private/{entity}/delete/v1")
    public ResponseEntity<?> {entity}DeleteV1(
            @RequestBody {Entity}DeleteRqDTOv1 request) {
        Response rs = new Response();
        try {
            {entity}Service.deleteSafe(request.get{Entity}IdList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

### 3.4 View Controller (single)

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.{ENTITY}_MANAGE, Permissions.{ENTITY}_VIEW})
@Tag(name = ApiTag.{ENTITY})
public class {Entity}ViewController extends ApiController {

    private final {Entity}Service {entity}Service;
    private final {Entity}RestDTOMapper {entity}RestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @GetMapping(value = "/private/{entity}/{entityId}/v1")
    public ResponseEntity<?> {entity}ViewV1(
            @MapperContextBinding(roots = {Entity}RestDTOMapper.class, response = {Entity}ViewRsDTOv1.class)
            @Parameter(example = DTOExamples.UUID_ID) @PathVariable UUID {entity}Id) {
        {Entity}ViewRsDTOv1 rs = new {Entity}ViewRsDTOv1();
        try {
            {Entity}Entity entity = {entity}Service.findEntitySafe({entity}Id);
            rs.set{Entity}({entity}RestDTOMapper.convert(entity, mapperContext))
              .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

### 3.5 Search Controller

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.{ENTITY}_MANAGE, Permissions.{ENTITY}_VIEW})
@Tag(name = ApiTag.{ENTITY})
public class {Entity}SearchController extends ApiController {

    private final {Entity}SearchService {entity}SearchService;
    private final {Entity}RestDTOMapper {entity}RestDTOMapper;
    private final {Entity}SearchDTOReverseMapper {entity}SearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @PostMapping(value = "/private/{entity}/search/v1")
    public ResponseEntity<?> {entity}SearchV1(
            @MapperContextBinding(roots = {Entity}RestDTOMapper.class, response = {Entity}SearchRsDTOv1.class)
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody {Entity}SearchRqDTOv1 request) {
        {Entity}SearchRsDTOv1 rs = new {Entity}SearchRsDTOv1();
        try {
            {Entity}Search search = {entity}SearchDTOReverseMapper.convert(request.getSearch(), mapperContext);
            PaginationResult<{Entity}Entity> result =
                    {entity}SearchService.search(search, pagination, request.getSortField(), request.getSortDirection());
            rs.set{Entities}({entity}RestDTOMapper.convertCollection(result.getList(), mapperContext))
              .setPagination(paginationMapper.convert(result))
              .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

### 3.6 Count Controller

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.{ENTITY}_MANAGE, Permissions.{ENTITY}_VIEW})
@Tag(name = ApiTag.{ENTITY})
public class {Entity}CountController extends ApiController {

    private final {Entity}SearchService {entity}SearchService;
    private final {Entity}CountRestDTOMapper {entity}CountRestDTOMapper;
    private final {Entity}SearchDTOReverseMapper {entity}SearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @PostMapping(value = "/private/{entity}/search/count/v1")
    public ResponseEntity<?> {entity}CountV1(
            @MapperContextBinding(roots = {Entity}CountRestDTOMapper.class, response = {Entity}CountRsDTOv1.class)
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody {Entity}CountRqDTOv1 request) {
        {Entity}CountRsDTOv1 rs = new {Entity}CountRsDTOv1();
        try {
            {Entity}Search search = {entity}SearchDTOReverseMapper.convert(request.getSearch(), mapperContext);
            var results = {entity}SearchService.countByGroupFields(search, request.getGroupFields(), pagination);
            rs.setCounts({entity}CountRestDTOMapper.convertCollection(results.getList(), mapperContext))
              .setPagination(paginationMapper.convert(results))
              .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
```

### 3.7 Export SQL Controller — see [api_export_architecture.md](api_export_architecture.md)

Unlike other endpoints, export returns a **file download** (`ResponseEntity<byte[]>`), not a JSON response.

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.{ENTITY}_MANAGE)
@Tag(name = ApiTag.{ENTITY})
public class {Entity}ExportSqlController extends ApiController {

    private final {Entity}Service {entity}Service;
    private final {Entity}ExportService {entity}ExportService;

    @ParametersApiUserHeaders
    @Operation(operationId = "{entity}ExportSqlV1", summary = "Exports {entity} as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/{entity}/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> {entity}ExportSqlV1(
            @RequestBody {Entity}ExportSqlRqDTOv1 request) throws ServiceException {
        var entities = {entity}Service.findEntitiesSafe(request.get{Entity}Ids());
        String sql = {entity}ExportService.exportToSql(entities.getCollection()
                // , request.isIncludeChildren()  // optional boolean flags
        );

        String filename = "{entity}_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
```

Key differences from other controllers:
- Returns `ResponseEntity<byte[]>`, not `ResponseEntity<?>`
- `produces = "text/sql;charset=UTF-8"`
- No `Response` DTO wrapping, no `relatedObjects`
- No `@MapperContextBinding` — export bypasses the mapper layer
- Requires manage-level permission (`{ENTITY}_MANAGE`)

---

## 4. Service Layer

### 4.1 CRUD Service — extends `EntitySecureFindServiceImpl`

This service handles create, update, delete, and entity lookup. It extends `TwinsEntitySecureFindService<E>` which extends `EntitySecureFindServiceImpl<E>`.

```java
@Service
@RequiredArgsConstructor
public class {Entity}Service extends TwinsEntitySecureFindService<{Entity}Entity> {

    @Getter
    private final {Entity}Repository repository;

    @Override
    public CrudRepository<{Entity}Entity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<{Entity}Entity, UUID> entityGetIdFunction() {
        return {Entity}Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied({Entity}Entity entity, ReadPermissionCheckMode mode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logShort(), mode);
    }

    @Override
    public boolean validateEntity({Entity}Entity entity, EntityValidateMode mode) throws ServiceException {
        // validate required fields, FK references, business rules
        return false; // return false if validation passes
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<{Entity}Entity> create{Entities}(List<{Entity}Create> createList) throws ServiceException {
        // batch optimized creation
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<{Entity}Entity> update{Entities}(List<{Entity}Update> updateList) throws ServiceException {
        // batch optimized update
    }

    // Load methods — see load_method_pattern.md
    public void load{RelatedEntity}(Collection<{Entity}Entity> srcCollection) throws ServiceException {
        // Variant A, B, or D — see load_method_pattern.md
    }
}
```

**Required abstract method implementations:**
- `entityRepository()` — return the Spring Data repository
- `entityGetIdFunction()` — return `Entity::getId`
- `isEntityReadDenied()` — domain access check
- `validateEntity()` — business validation

**Key inherited methods from `EntitySecureFindServiceImpl`:**
- `findEntitySafe(UUID id)` — lookup with caching + permission check
- `findEntitiesSafe(Collection<UUID> ids)` — batch lookup
- `saveSafe(E entity)` — save with validation
- `deleteSafe(E entity)` — delete with validation
- `load()` — batch load ManyToOne by FK (Variant A)
- `loadKit()` — batch load one-to-many (Variant D)

### 4.2 Search Service — extends `EntitySearchService`

This service handles search and count. It extends `EntitySearchService<S, E, SF, GF>` from the cambium framework.

```java
@Service
@RequiredArgsConstructor
public class {Entity}SearchService extends EntitySearchService<
        {Entity}Search,
        {Entity}Entity,
        {Entity}SortField,
        {Entity}GroupField> {

    private final {Entity}Repository {entity}Repository;

    @Override
    public JpaSpecificationExecutor<{Entity}Entity> jpaSpecificationExecutor() {
        return {entity}Repository;
    }

    @Override
    public {Entity}Search emptySearch() {
        return new {Entity}Search();
    }

    @Override
    protected {Entity}Entity newEntity() {
        return new {Entity}Entity();
    }

    @Override
    protected Class<{Entity}Entity> entityClass() {
        return {Entity}Entity.class;
    }

    @Override
    public Specification<{Entity}Entity> createFilterSpecification({Entity}Search search, UUID domainId) {
        return Specification.allOf(
            checkFieldUuid(domainId, {Entity}Entity.Fields.domainId),
            checkUuidIn(search.getNameList(), false, false, {Entity}Entity.Fields.name),
            // ... other filters
        );
    }

    @Override
    public Specification<{Entity}Entity> createSortSpecification({Entity}SortField sortField, SortDirection sortDirection) {
        if (sortField == null)
            sortField = {Entity}SortField.createdAt;
        boolean ascending = sortDirection != SortDirection.DESC;
        return switch (sortField) {
            case createdAt -> toSortSpecification(ascending, {Entity}Entity.Fields.createdAt);
            case name      -> toSortSpecification(ascending, {Entity}Entity.Fields.name);
            // For JOIN sort fields:
            // case userName -> toSortSpecification(ascending, {Entity}Entity.Fields.user, UserEntity.Fields.name);
            // For I18n sort fields:
            // case description -> I18nSpecification.toSortSpecification(ascending, locale, "descriptionI18n");
        };
    }

    @Override
    public String convertToEntityField({Entity}GroupField groupField) {
        return switch (groupField) {
            case statusId -> {Entity}Entity.Fields.statusId;
            case userId   -> {Entity}Entity.Fields.userId;
        };
    }

    @Override
    public void mapGroupedField({Entity}Entity entity, {Entity}GroupField field, Object value) {
        switch (field) {
            case statusId -> entity.setStatusId((UUID) value);
            case userId   -> entity.setUserId((UUID) value);
        }
    }
}
```

**Required abstract method implementations:**
- `jpaSpecificationExecutor()` — return the repository
- `emptySearch()` — create empty search object
- `newEntity()` — entity factory for count result population
- `entityClass()` — entity class for CriteriaQuery
- `createFilterSpecification()` — JPA Specification for WHERE clause
- `createSortSpecification()` — JPA Specification for ORDER BY (switch by enum)
- `convertToEntityField()` — group field enum → entity field name
- `mapGroupedField()` — populate entity field from CriteriaQuery row

**Key inherited methods from `EntitySearchService`:**
- `search(search, pagination, sortField, sortDirection)` — paginated search
- `countByGroupFields(search, groupFields)` — count without pagination
- `countByGroupFields(search, groupFields, pagination)` — count with pagination

### 4.3 Export Service — SQL file generation — see [api_export_architecture.md](api_export_architecture.md)

Generates SQL `INSERT ... ON CONFLICT DO NOTHING` statements from JPA entities. Uses `SqlBuilder` (reflection-based) and `I18nExportService` for i18n + translations.

```java
@Service
@RequiredArgsConstructor
public class {Entity}ExportService {

    private final SqlBuilder sqlBuilder;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;

    public String exportToSql(Collection<{Entity}Entity> entities) throws ServiceException {
        if (entities.isEmpty()) return "";

        // 1. Collect i18n IDs from entities
        Set<UUID> i18nIds = i18nService.collectI18nIds(entities,
                {Entity}Entity::getNameI18nId,
                {Entity}Entity::getDescriptionI18nId);

        List<String> sqlParts = new ArrayList<>();

        // 2. i18n first (FK constraint order)
        if (!i18nIds.isEmpty()) {
            sqlParts.add(i18nExportService.exportToSql(i18nIds));
        }

        // 3. Entity itself
        sqlParts.add(sqlBuilder.buildInserts(entities));

        // 4. Optional child entities (for composite exports)
        // if (includeChildren) sqlParts.add(childExportService.exportToSql(children));

        return String.join("\n", sqlParts);
    }
}
```

Two patterns:
- **Simple** — single entity type + i18n
- **Composite** — parent entity + optional children via boolean flags (e.g., `includeFields`, `includeStatuses`)

**SQL parts order** respects FK constraints: i18n → i18n_translation → entity → children.

---

## 5. DTO Conventions (summary)

For full rules see [dto_code_convention.md](dto_code_convention.md).

### Key Rules

1. **Request DTOs** extend `Request`, suffix `RqDTOv1`
2. **Response DTOs** extend `Response` or `ResponseCountDTOv1`, suffix `RsDTOv1`
3. **Entity DTOs** — flat structure, no business logic, no entity dependencies
4. **Create/Update DTOs** share a common `{Entity}SaveDTOv1` base
5. **`id` field** only in Update DTOs, never in Create DTOs
6. **Sort fields** (`sortField`, `sortDirection`) in `SearchRqDTOv1`, not in `SearchDTOv1`
7. **Count DTOs** extend `CountDTOv1` (not entity DTO), declare groupable fields explicitly
8. **Count response** extends `ResponseCountDTOv1` (includes `PaginationDTOv1`)
9. All DTOs annotated with `@Schema` for Swagger documentation

---

## 6. Mapper Conventions

### Forward Mapper (Entity → DTO)

```java
@Component
@MapperModeBinding(modes = {Entity}Mode.class)
public class {Entity}RestDTOMapper extends RestSimpleDTOMapper<{Entity}Entity, {Entity}DTOv1> {

    @Override
    public void map({Entity}Entity src, {Entity}DTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse({Entity}Mode.SHORT)) {
            case SHORT -> dst.setId(src.getId()).setName(src.getName());
            case DETAILED -> {
                dst.setId(src.getId()).setName(src.getName());
                // ... all fields
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<{Entity}Entity> srcCollection, MapperContext mapperContext) {
        // Batch load @Transient fields before mapping — see load_method_pattern.md
    }
}
```

### Reverse Mapper (DTO → Domain Object)

```java
@Component
public class {Entity}CreateRestDTOReverseMapper extends RestSimpleDTOMapper<{Entity}CreateDTOv1, {Entity}Create> {

    @Override
    public void map({Entity}CreateDTOv1 src, {Entity}Create dst, MapperContext mapperContext) {
        dst.setName(src.getName());
        // ... map fields from DTO to domain command
    }
}
```

### Count Mapper (conditional loading by groupFields)

```java
@Component
@MapperModeBinding(modes = {Entity}Mode.class)
public class {Entity}CountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<{Entity}Entity, {Entity}GroupField>, {Entity}CountDTOv1> {

    @Override
    public void map(CountResult<{Entity}Entity, {Entity}GroupField> src, {Entity}CountDTOv1 dst, MapperContext mapperContext) {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst.setCount(src.getCount());
        // Map only group fields from entity to DTO
        if (src.getGroupFields().contains({Entity}GroupField.statusId))
            dst.setStatusId(entity.getStatusId());
        // ...
    }

    @Override
    public void beforeCollectionConversion(
            Collection<CountResult<{Entity}Entity, {Entity}GroupField>> srcCollection,
            MapperContext mapperContext) {
        // Batch load related objects only for group fields present in results
        // See api_counting_architecture.md Section 11
    }
}
```

---

## 7. Sorting — see [api_sorting_architecture.md](api_sorting_architecture.md)

1. Create enum `{Entity}SortField` in `enums.sort` — pure names, no fieldPath
2. Add `sortField` and `sortDirection` fields to `SearchRqDTOv1` (not SearchDTO)
3. Implement `createSortSpecification()` in `{Entity}SearchService` with switch
4. Use `CommonSpecification.toSortSpecification(ascending, fieldPath...)` for regular fields
5. Use `I18nSpecification.toSortSpecification(ascending, locale, fieldPath...)` for I18n fields
6. Always include `getResultType()` guard (handled by `CommonSpecification`)
7. Use LEFT JOIN for relationship sort fields

---

## 8. Counting — see [api_counting_architecture.md](api_counting_architecture.md)

1. Create enum `{Entity}GroupField` in `enums.sort` — only direct entity fields (no JOIN fields)
2. `{Entity}CountDTOv1` extends `CountDTOv1` — explicit groupable fields with `@RelatedObject`
3. `{Entity}CountRsDTOv1` extends `ResponseCountDTOv1` — includes pagination
4. Implement `convertToEntityField()` and `mapGroupedField()` in `{Entity}SearchService`
5. Reuse same `{Entity}SearchDTOv1` from search (search criteria without sort)
6. `groupFields` is mandatory — empty set returns total count
7. Do not add timestamp/high-cardinality fields as group fields
8. `{Entity}CountRqDTOv1` has Set of groupFields annotated with `@Size(max = 2)`
   

---

## 9. Load Methods — see [load_method_pattern.md](load_method_pattern.md)

Load methods populate `@Transient` fields on entities in batch (prevent N+1).

| Variant | Scenario | Pattern |
|---|---|---|
| A | ManyToOne by FK | `EntitySecureFindServiceImpl.load()` |
| B | Aggregation (count, sum) | Custom query + `getNeedLoad()` |
| D | One-to-many collection | `EntitySecureFindServiceImpl.loadKit()` |

**Placement**: load methods go in the service that owns the repository for the child entity, not the parent.

**Naming**: `load{FieldName}(Collection<E>)` + single-element overload via `Collections.singletonList()`.

---

## 10. Export SQL — see [api_export_architecture.md](api_export_architecture.md)

Export generates a downloadable `.sql` file with `INSERT ... ON CONFLICT DO NOTHING` statements.

| Component | Purpose |
|---|---|
| `SqlBuilder` | Reflection-based INSERT generation from `@Table` / `@Column` annotations |
| `I18nExportService` | Handles i18n + translation SQL for entities with I18n fields |
| `{Entity}ExportService` | Per-entity composable service: i18n → entity → children |

**Key rules:**
1. SQL parts ordered by FK dependency: i18n → entity → children
2. `ON CONFLICT DO NOTHING` — idempotent re-import
3. Entity lookup via `findEntitiesSafe` (permission checks apply)
4. No JSON response wrapping — returns `ResponseEntity<byte[]>` with `Content-Disposition: attachment`
5. `produces = "text/sql;charset=UTF-8"` on the endpoint
6. Requires manage-level permission (`{ENTITY}_MANAGE`)
7. Collect i18n IDs via `I18nService.collectI18nIds()` — never manually

---

## 10. Checklist

When creating a new settings API, verify:

- [ ] Entity with `@Column(name = "snake_case")` on all fields, indexes on FK columns
- [ ] Repository extending `CrudRepository` + `JpaSpecificationExecutor`
- [ ] Sort field enum in `enums.sort` with all sortable fields
- [ ] Group field enum in `enums.sort` with low-cardinality fields only
- [ ] DTOs following [dto_code_convention.md](dto_code_convention.md)
- [ ] Forward mapper with `beforeCollectionConversion` for load methods
- [ ] Reverse mappers for create/update/search
- [ ] Count mapper with conditional loading by groupFields
- [ ] CRUD service extending `TwinsEntitySecureFindService` with all 4 abstract methods
- [ ] Search service extending `EntitySearchService` with all 8 abstract methods
- [ ] Controllers split by operation, each with `@ProtectedBy`
- [ ] Flyway migration with FK inline, `IF NOT EXISTS`, no `public.` schema qualifier
- [ ] Indexes for all FK columns and sort/group fields
- [ ] `@Transactional(rollbackFor = Throwable.class)` on all write methods
- [ ] Domain isolation check in `isEntityReadDenied()`
- [ ] Export service with `SqlBuilder` + `I18nExportService` for entities that need SQL export
- [ ] Export controller returns `ResponseEntity<byte[]>` with `produces = "text/sql;charset=UTF-8"`
