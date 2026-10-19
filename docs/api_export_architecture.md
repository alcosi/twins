# Architecture: SQL Export API

**Date:** 2026-06-10
**Status:** Implemented
**Context:** TWINS-415 — export entities as SQL INSERT statements for data migration and backup

---

## Context

The system needs to export domain entities (twin classes, statuses, fields, twinflows, etc.) as SQL `INSERT` statements. This is used for data migration between environments, backup, and debugging. The export produces a downloadable `.sql` file containing idempotent `INSERT ... ON CONFLICT DO NOTHING` statements with all dependent entities (i18n, translations, child entities).

---

## Decision

### Principle

Export is implemented through **dedicated `ExportService` per entity type**, each with a `String exportToSql(Collection<Entity> entities)` method. The controller returns `ResponseEntity<byte[]>` with `Content-Disposition: attachment` and `text/sql` content type. The `SqlBuilder` component uses reflection on JPA annotations to generate INSERT statements, and `I18nExportService` handles i18n + translation SQL.

### Why dedicated ExportService per entity type

* Each entity type has different dependencies (i18n, child entities, schema maps) — a single generic service would require complex configuration
* The `exportToSql` method composes SQL from multiple parts (i18n → entity → children) in a specific order that respects FK constraints
* Composable: a parent export service (e.g., `TwinClassExportService`) delegates to child export services (`TwinClassFieldExportService`, `TwinStatusExportService`, `TwinflowExportService`)

### Why `SqlBuilder` with reflection instead of hand-written SQL

* Single source of truth — `@Table` and `@Column` annotations define both JPA mapping and SQL export
* No maintenance burden when entity schema changes — `SqlBuilder` picks up new fields automatically
* Caffeine cache for reflection metadata — no performance penalty on repeated calls
* `ON CONFLICT DO NOTHING` — idempotent re-import without errors

### Why `ResponseEntity<byte[]>` instead of JSON response

* The result is a file download, not a JSON API response
* `Content-Disposition: attachment` triggers browser file download
* `text/sql` content type for proper MIME handling
* No need for response DTO wrapping (`Response`, `relatedObjects`, etc.)

---

## Components

### 1. Request DTO — entity IDs + export options

```java
// TwinClassExportSqlRqDTOv1.java — complex export with options
@Schema(name = "TwinClassExportSqlRqV1")
public class TwinClassExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin class ids to export SQL for")
    public Set<UUID> twinClassIds;

    @Schema(description = "include twin class fields in export")
    public boolean includeFields = false;

    @Schema(description = "include twin class statuses in export")
    public boolean includeStatuses = false;

    @Schema(description = "include twinflow in export")
    public boolean includeTwinflow = false;
}

// TwinStatusExportSqlRqDTOv1.java — simple export without options
@Schema(name = "TwinStatusExportSqlRqV1")
public class TwinStatusExportSqlRqDTOv1 extends Request {
    @Schema(description = "twin status ids to export SQL for")
    public Set<UUID> statusIds;
}
```

Extends `Request`. Contains `Set<UUID>` of entity IDs to export. Optional boolean flags control what dependent entities to include. No response DTO — the controller returns raw bytes.

### 2. Controller — file download response

```java
@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE})
public class TwinClassExportSqlController extends ApiController {
    private final TwinClassExportService twinClassExportService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassExportSqlV1", summary = "Exports twin class as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> twinClassExportSqlV1(
            @RequestBody TwinClassExportSqlRqDTOv1 request) throws ServiceException {
        String sql = twinClassExportService.exportToSql(
                request.getTwinClassIds(),
                request.isIncludeFields(),
                request.isIncludeStatuses(),
                request.isIncludeTwinflow()
        );

        String filename = "twin_classes_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
```

Key points:
* `produces = "text/sql;charset=UTF-8"` — correct MIME type
* Returns `ResponseEntity<byte[]>` — raw bytes, no JSON wrapping
* `Content-Disposition: attachment` — triggers browser download
* Filename includes timestamp for uniqueness
* No `Response` DTO wrapping, no `relatedObjects`

### 3. SqlBuilder — reflection-based INSERT generation

```java
@Component
public class SqlBuilder {
    public String buildInsert(Object entity) {
        // 1. Read @Table → table name
        // 2. Read @Column fields → column names + extract values via getters
        // 3. Skip @ManyToOne, @OneToOne, @Transient, static, collection fields
        // 4. Format values (UUID → 'string', Enum → 'name', Boolean → TRUE/FALSE, Map → jsonb, etc.)
        // 5. Build: INSERT INTO "table" ("col1", "col2") VALUES ('val1', 'val2') ON CONFLICT DO NOTHING;
    }

    public String buildInserts(Collection<?> entities) {
        return entities.stream().map(this::buildInsert)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));
    }
}
```

Generates `INSERT INTO ... ON CONFLICT DO NOTHING;` from JPA entities using reflection:
* Reads `@Table(name = "...")` for table name
* Reads `@Column(name = "...")` for column names
* Skips relationship fields (`@ManyToOne`, `@OneToOne`), collections, `@Transient`, static fields
* Handles special types: `UUID`, `Enum`, `Boolean`, `Timestamp`, `HashMap` (hstore), `Map` (jsonb)
* Caches reflection metadata via Caffeine (5 min TTL)
* Unwraps Hibernate proxy classes

### 4. I18nExportService — i18n + translations SQL

```java
@Service
public class I18nExportService {
    private final I18nService i18nService;
    private final I18nSqlBuilder i18nSqlBuilder;

    public String exportToSql(Set<UUID> i18nIds) throws ServiceException {
        List<I18nEntity> i18nEntities = i18nService.findEntitiesSafe(i18nIds).getList();
        i18nService.loadTranslations(i18nEntities);

        StringBuilder result = new StringBuilder();
        for (I18nEntity i18n : i18nEntities) {
            String i18nSql = i18nSqlBuilder.buildI18nInsert(i18n, i18n.getTranslationsKit().getList());
            // ...
        }
        return result.toString();
    }
}
```

For each i18n ID: loads the `I18nEntity` + its translations, then generates INSERT SQL for both. Uses `I18nSqlBuilder` which delegates to `SqlBuilder.buildInsert()` for each entity.

### 5. Entity ExportService — composable per-entity export

```java
// Simple entity export (single entity type + i18n)
@Service
public class TwinStatusExportService extends EntityExportService {
    public String exportToSql(Collection<TwinStatusEntity> statuses) throws ServiceException {
        if (statuses.isEmpty()) return "";

        Set<UUID> i18nIds = i18nService.collectI18nIds(statuses,
                TwinStatusEntity::getNameI18nId,
                TwinStatusEntity::getDescriptionI18nId);

        List<String> sqlParts = new ArrayList<>();

        // 1. i18n first (FK constraint order)
        if (!i18nIds.isEmpty()) {
            sqlParts.add(i18nExportService.exportToSql(i18nIds));
        }

        // 2. Entity itself
        sqlParts.add(sqlBuilder.buildInserts(statuses));

        return String.join("\n", sqlParts);
    }
}
```

```java
// Composite entity export (entity + optional children)
@Service
public class TwinClassExportService extends EntityExportService{
    private final TwinClassService twinClassService;
    private final TwinClassFieldExportService twinClassFieldExportService;
    private final TwinStatusExportService twinStatusExportService;
    private final TwinflowExportService twinflowExportService;

    public String exportToSql(Set<UUID> twinClassIds,
                              boolean includeFields, boolean includeStatuses, boolean includeTwinflow)
            throws ServiceException {

        Kit<TwinClassEntity, UUID> twinClassesKit = twinClassService.findEntitiesSafe(twinClassIds);
        List<TwinClassEntity> twinClasses = twinClassesKit.getList();

        List<String> sqlParts = new ArrayList<>();

        // 1. i18n for twin classes
        Set<UUID> i18nIds = collectI18nIds(twinClasses);
        if (!i18nIds.isEmpty()) sqlParts.add(i18nExportService.exportToSql(i18nIds));

        // 2. Twin classes themselves
        sqlParts.add(sqlBuilder.buildInserts(twinClasses));

        // 3. Optional children (controlled by boolean flags)
        if (includeFields)   sqlParts.add(twinClassFieldExportService.exportToSql(...));
        if (includeStatuses) sqlParts.add(twinStatusExportService.exportToSql(...));
        if (includeTwinflow) sqlParts.add(twinflowExportService.exportToSql(...));

        return String.join("\n", sqlParts);
    }
}
```

Two patterns:
* **Simple export** — single entity type + its i18n: `TwinStatusExportService`, `TwinClassFieldExportService`
* **Composite export** — parent entity + optional children via boolean flags: `TwinClassExportService`, `TwinflowExportService`

SQL parts are ordered to respect FK constraints: i18n → parent entity → child entities.

### 6. i18n ID collection helper

```java
// I18nService.collectI18nIds — extracts non-null i18n IDs from entities
Set<UUID> i18nIds = i18nService.collectI18nIds(entities,
        TwinStatusEntity::getNameI18nId,
        TwinStatusEntity::getDescriptionI18nId);
```

Takes a collection of entities + getter references. Collects all non-null i18n IDs into a `Set<UUID>`. Used by every export service to gather i18n dependencies before calling `I18nExportService`.

---

## Data Flow

```
Controller
  │
  ├─ Request DTO (Set<UUID> ids + boolean flags)
  │
  ├─ Entity lookup: findEntitiesSafe(ids)          ← EntitySecureFindServiceImpl
  │
  ├─ ExportService.exportToSql(entities, flags)     ← per-entity export logic
  │     │
  │     ├─ collect i18n IDs from entities           ← I18nService.collectI18nIds
  │     ├─ export i18n SQL                          ← I18nExportService.exportToSql
  │     │     └─ load i18n + translations           ← I18nService
  │     │     └─ build INSERT for i18n + translations ← SqlBuilder
  │     │
  │     ├─ export entity SQL                        ← SqlBuilder.buildInserts
  │     │
  │     └─ (optional) export child entities         ← child ExportService.exportToSql
  │
  └─ ResponseEntity<byte[]> with Content-Disposition: attachment
```

---

## SQL Output Format

```sql
-- i18n
INSERT INTO "i18n" ("id", "type") VALUES ('uuid-1', 'TWIN_STATUS_NAME') ON CONFLICT DO NOTHING;
INSERT INTO "i18n_translation" ("id", "i18n_id", "locale", "translation") VALUES ('uuid-2', 'uuid-1', 'en', 'Active') ON CONFLICT DO NOTHING;
INSERT INTO "i18n_translation" ("id", "i18n_id", "locale", "translation") VALUES ('uuid-3', 'uuid-1', 'ru', 'Активный') ON CONFLICT DO NOTHING;

-- entity
INSERT INTO "twin_status" ("id", "twin_class_id", "key", "name_i18n_id", "background_color") VALUES ('uuid-4', 'uuid-5', 'active', 'uuid-1', '#00FF00') ON CONFLICT DO NOTHING;
```

* `ON CONFLICT DO NOTHING` — idempotent re-import
* Parts ordered by FK dependency: i18n → i18n_translation → entity → child entities
* UTF-8 encoding

---

## API Contract

**Endpoint:** `POST /private/{entity}/export/sql/v1`
**Content-Type:** `application/json`
**Produces:** `text/sql;charset=UTF-8`

**Request (complex — with export options):**

```json
{
  "twinClassIds": ["uuid-1", "uuid-2"],
  "includeFields": true,
  "includeStatuses": true,
  "includeTwinflow": false
}
```

**Request (simple — IDs only):**

```json
{
  "statusIds": ["uuid-1", "uuid-2"]
}
```

**Response:** File download (`Content-Disposition: attachment; filename="entity_type_1718000000000.sql"`)

---

## How to Add a New Export API

### Step 1: Create Request DTO

```java
// dto/rest/{domain}/FooExportSqlRqDTOv1.java
@Schema(name = "FooExportSqlRqV1")
public class FooExportSqlRqDTOv1 extends Request {
    @Schema(description = "foo ids to export SQL for")
    public Set<UUID> fooIds;

    // Optional: boolean flags for child entities
    @Schema(description = "include bars in export")
    public boolean includeBars = false;
}
```

### Step 2: Create ExportService

```java
// service/foo/FooExportService.java
@Service
@RequiredArgsConstructor
public class FooExportService {
    private final SqlBuilder sqlBuilder;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;
    private final FooService fooService;

    public String exportToSql(Collection<FooEntity> foos) throws ServiceException {
        if (foos.isEmpty()) return "";

        Set<UUID> i18nIds = i18nService.collectI18nIds(foos,
                FooEntity::getNameI18nId,
                FooEntity::getDescriptionI18nId);

        List<String> sqlParts = new ArrayList<>();

        if (!i18nIds.isEmpty()) {
            sqlParts.add(i18nExportService.exportToSql(i18nIds));
        }

        sqlParts.add(sqlBuilder.buildInserts(foos));

        return String.join("\n", sqlParts);
    }
}
```

### Step 3: Create Controller

```java
// controller/rest/priv/foo/FooExportSqlController.java
@Tag(name = ApiTag.FOO)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequiredArgsConstructor
@ProtectedBy({Permissions.FOO_MANAGE})
public class FooExportSqlController extends ApiController {
    private final FooExportService fooExportService;
    private final FooService fooService;

    @ParametersApiUserHeaders
    @Operation(operationId = "fooExportSqlV1", summary = "Exports foo as SQL INSERT statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SQL file"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/foo/export/sql/v1", produces = "text/sql;charset=UTF-8")
    public ResponseEntity<byte[]> fooExportSqlV1(
            @RequestBody FooExportSqlRqDTOv1 request) throws ServiceException {
        var foos = fooService.findEntitiesSafe(request.getFooIds());
        String sql = fooExportService.exportToSql(foos.getCollection());

        String filename = "foos_" + System.currentTimeMillis() + ".sql";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(sql.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }
}
```

---

## Critical Rules

1. **FK order in SQL parts** — i18n and translations must come before the entity that references them. Children come after the parent.
2. **`ON CONFLICT DO NOTHING`** — always use idempotent inserts to allow re-importing without errors.
3. **Entity lookup via `findEntitiesSafe`** — controller resolves entities through `EntitySecureFindServiceImpl`, ensuring permission checks and existence validation.
4. **No JSON response wrapping** — export endpoints return raw `byte[]` with file download headers, not `Response` DTOs.
5. **`produces = "text/sql;charset=UTF-8"`** — mandatory for correct content type.
6. **`@ProtectedBy`** — export requires manage-level permissions (e.g., `TWIN_CLASS_MANAGE`, `TWIN_STATUS_MANAGE`).
7. **Boolean defaults are `false`** — optional child entity exports are opt-in.
8. **Empty collection returns empty string** — if no entities found, return `""` (empty SQL file).
9. **i18n collection via `I18nService.collectI18nIds`** — never collect i18n IDs manually; use the helper that handles nulls.

---

## File Naming Convention

| Layer | File | Package |
|---|---|---|
| Request DTO | `FooExportSqlRqDTOv1` | `dto.rest.foo` |
| Export Service | `FooExportService` | `service.foo` |
| Controller | `FooExportSqlController` | `controller.rest.priv.foo` |

---

## Existing Implementations

| Entity | Controller | ExportService | Has child options |
|---|---|---|---|
| TwinClass | `TwinClassExportSqlController` | `TwinClassExportService` | Yes (fields, statuses, twinflow) |
| TwinStatus | `TwinStatusExportSqlController` | `TwinStatusExportService` | No |
| TwinClassField | — (used by `TwinClassExportService`) | `TwinClassFieldExportService` | No |
| Twinflow | — (used by `TwinClassExportService`) | `TwinflowExportService` | Yes (schema maps) |
| I18n | — (used by all export services) | `I18nExportService` | Yes (translations) |
