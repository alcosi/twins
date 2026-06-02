# Architecture: Count API with Grouping for Search

**Date:** 2026-06-01
**Status:** In Progress
**Context:** TWINS-831 — count API alongside every search API

---

## Context

Each search API must have a paired count API that calculates the number of records grouped by one or more grouping attributes. For example: how many users are in each business account, how many records exist per creation date, etc. Search criteria (`SearchDTO`) are reused from the search API — sort fields remain in `SearchRqDTO` and are not included in count.

---

## Decision

### Principle

The count API accepts the **same SearchDTO** (search criteria) + **Set<enum> groupFields** (grouping fields). The result is an array of `CountDTOv1[]`, where each element extends the base `CountDTOv1` (provides `count`) and explicitly declares only the groupable fields for the domain.

### Why enum for groupFields

* Similar to sortField — compile-time whitelist, Swagger displays a dropdown
* Enum in `enums.sort` — reuse the same package
* Only direct entity fields (without JOIN) — simplifies `GROUP BY`

### Why dedicated CountDTO with explicit fields (not inheritance from DTOv1)

* Base `CountDTOv1` provides only the `count` field — clean and minimal
* Each domain count DTO extends `CountDTOv1` and **explicitly declares** groupable fields as `UUID` / enum / etc.
* No partial population: only the requested group fields are present in the response, everything else simply doesn't exist in the DTO
* Schema is explicit and stable — client sees exactly what fields are possible, no surprise `null`s from unrelated entity fields
* `@RelatedObject` annotations on fields enable related-object enrichment (e.g., resolve `userId` → user details)

### Why SearchDTO is reused

* Search criteria are identical for search and count
* Sort fields remain in `SearchRqDTO` — `SearchDTO` is clean and suitable for count
* Backward compatibility: changes to search criteria are automatically applied to count

---

## Components

### 1. Enum for Grouping

```java id="u1ojmz"
package org.twins.core.enums.sort;

public enum DomainBusinessAccountUserGroupField {
    userId,
    businessAccountId
}
```

Simple enum without fieldPath — only direct entity fields (without JOIN).

### 2. Count Request DTO

```java id="zab3mv"
// DomainBusinessAccountUserCountRqDTOv1.java
@Schema(name = "DomainBusinessAccountUserCountRqV1")
public class DomainBusinessAccountUserCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainBusinessAccountUserSearchDTOv1 search;

    @Schema(description = "Group by fields")
    public Set<DomainBusinessAccountUserGroupField> groupFields;
}
```

* Reuses `DomainBusinessAccountUserSearchDTOv1` (search criteria without sort)
* `groupFields` — Set enum, Swagger displays a dropdown

### 3. Base Count DTO

```java id="cdt1"
// org.twins.core.dto.rest.CountDTOv1
@Data
@Accessors(chain = true)
@Schema(name = "CountDTOv1")
public class CountDTOv1 {
    @Schema(description = "count of records in this group")
    public Long count;
}
```

Minimal base class with only the `count` field. Each domain count DTO extends it and declares its own groupable fields.

### 4. Domain Count DTO — Explicit Groupable Fields

```java id="9lyx2t"
// DomainBusinessAccountUserCountDTOv1.java
@Schema(name = "DomainBusinessAccountUserCountV1")
public class DomainBusinessAccountUserCountDTOv1 extends CountDTOv1 {
    @Schema(description = "user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "business account id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;
}
```

Extends `CountDTOv1` (not `DomainBusinessAccountUserDTOv1`). Each groupable field is declared explicitly — no inheritance from entity DTO. Only the fields requested in `groupFields` are populated; absent group fields remain `null`. `@RelatedObject` annotations allow the controller to enrich group values with related-object details.

### 5. Count Response DTO

```java id="priq62"
// DomainBusinessAccountUserCountRsDTOv1.java
@Schema(name = "DomainBusinessAccountUserCountRsV1")
public class DomainBusinessAccountUserCountRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "count results grouped by requested fields")
    public List<DomainBusinessAccountUserCountDTOv1> counts;
}
```

Extends `ResponseRelatedObjectsDTOv1` — enables related-object enrichment for group field values (e.g., user details for `userId`).

### 6. Generic Result Wrapper

```java id="q7k2n9"
// org.twins.core.domain.CountResult
@Data
@Accessors(chain = true)
public class CountResult<E> {
    private E entity;  // partially populated — only group fields filled
    private Long count;
}
```

Typed alternative to raw `Object[]`. The `EntitySearchService` populates only group fields on the entity via `mapGroupedField()`. The dedicated count mapper then converts `CountResult` → `CountDTOv1`.

### 7. Base Search Class — `EntitySearch<E>`

```java id="es1"
// org.twins.core.domain.search.EntitySearch
public abstract class EntitySearch<E> {
}
```

Marker base class for all search objects. Each domain search object extends it, binding the entity type. Example:

```java
public class DomainBusinessAccountUserSearch extends EntitySearch<DomainBusinessAccountUserEntity> {
    private List<UUID> userIdList;
    private List<UUID> businessAccountIdList;
    // ... other filter fields
}
```

### 8. Abstract Service — `EntitySearchService<S, E, SF, GF>`

```java id="es2"
// org.twins.core.service.EntitySearchService
public abstract class EntitySearchService<S extends EntitySearch<E>, E, SF, GF> {

    // Abstract methods — each domain implements these:

    JpaSpecificationExecutor<E> jpaSpecificationExecutor();   // repository
    S emptySearch();                                          // empty search instance
    Class<E> entityClass();                                   // entity class for CriteriaQuery
    Specification<E> createFilterSpecification(S search, UUID domainId);
    Specification<E> createSortSpecification(SF sortField, SortDirection sortDirection);
    String convertToEntityField(GF groupField);               // enum → entity field name
    void mapGroupedField(E entity, GF field, Object value);   // populate entity from row
    E newEntity();                                            // entity factory

    // Shared logic — provided by the base class:

    PaginationResult<E> search(S search, SimplePagination pagination, SF sortField, SortDirection sortDirection);
    List<CountResult<E>> countByGroupFields(S search, Set<GF> groupFields);
}
```

Generic parameters:
- `S` — search type (extends `EntitySearch<E>`)
- `E` — JPA entity type
- `SF` — sort field enum type
- `GF` — group field enum type

The base class implements `search()` and `countByGroupFields()` with all shared logic. Concrete services only provide domain-specific mappings via abstract methods. This eliminates duplication across all search/count APIs.

### 9. `CountQueryExecutor` — Grouped Count via Criteria API

```java id="ce1"
// org.twins.core.dao.specifications.CountQueryExecutor
@Component
public class CountQueryExecutor {
    @Autowired
    private EntityManager entityManager;

    public <E> List<Object[]> executeGroupedCount(
            Class<E> entityClass,
            Specification<E> filterSpec,
            List<String> groupFieldNames) {
        // Builds: SELECT field1, field2, ..., COUNT(*) FROM Entity GROUP BY field1, field2, ...
        // Returns List<Object[]> — each row: [field1Val, field2Val, ..., count]
    }
}
```

Uses `EntityManager` + Criteria API directly (instead of Spring Data `findAll` with `Specification`) to avoid `Specification` limitations with `GROUP BY` + `multiselect`. Accepts entity class, filter specification, and field names. Returns raw `Object[]` rows — the base `EntitySearchService` maps them via `mapGroupedField()`.

### 10. Concrete Service — `DomainBusinessAccountUserSearchService`

```java id="p69h3u"
// DomainBusinessAccountUserSearchService.java
@Service
public class DomainBusinessAccountUserSearchService extends EntitySearchService
        <DomainBusinessAccountUserSearch, DomainBusinessAccountUserEntity,
         DomainBusinessAccountUserSortField, DomainBusinessAccountUserGroupField> {

    private final DomainBusinessAccountUserRepository domainBusinessAccountUserRepository;

    @Override public JpaSpecificationExecutor<DomainBusinessAccountUserEntity> jpaSpecificationExecutor() {
        return domainBusinessAccountUserRepository;
    }
    @Override public DomainBusinessAccountUserSearch emptySearch() {
        return new DomainBusinessAccountUserSearch();
    }
    @Override protected DomainBusinessAccountUserEntity newEntity() {
        return new DomainBusinessAccountUserEntity();
    }
    @Override protected Class<DomainBusinessAccountUserEntity> entityClass() {
        return DomainBusinessAccountUserEntity.class;
    }

    @Override public Specification<DomainBusinessAccountUserEntity> createFilterSpecification(...) {
        return Specification.allOf(
            checkFieldUuid(domainId, Fields.domainId),
            checkUuidIn(search.getUserIdList(), false, false, Fields.userId),
            checkUuidIn(search.getBusinessAccountIdList(), false, false, Fields.businessAccountId),
            // ... other filters
        );
    }

    @Override public Specification<DomainBusinessAccountUserEntity> createSortSpecification(...) {
        return switch (sortField) {
            case createdAt          -> toSortSpecification(ascending, Fields.createdAt);
            case lastActivityAt     -> toSortSpecification(ascending, Fields.lastActivityAt);
            case userName           -> toSortSpecification(ascending, Fields.user, UserEntity.Fields.name);
            case businessAccountName -> toSortSpecification(ascending, Fields.businessAccount, BusinessAccountEntity.Fields.name);
        };
    }

    @Override public String convertToEntityField(DomainBusinessAccountUserGroupField groupField) {
        return switch (groupField) {
            case userId            -> Fields.userId;
            case businessAccountId -> Fields.businessAccountId;
        };
    }

    @Override public void mapGroupedField(DomainBusinessAccountUserEntity entity,
                                           DomainBusinessAccountUserGroupField field, Object o) {
        switch (field) {
            case userId            -> entity.setUserId((UUID) o);
            case businessAccountId -> entity.setBusinessAccountId((UUID) o);
        }
    }
}
```

All shared logic (`search`, `countByGroupFields`, `mapCountResults`) is inherited from `EntitySearchService`. The concrete service only implements domain-specific mappings: filter specification, sort specification, group field → entity field conversion, and row → entity population.

### 11. Dedicated Count Mapper

```java id="cm1"
// DomainBusinessAccountUserCountRestDTOMapper.java
@Component
@MapperModeBinding(modes = DomainBusinessAccountUserMode.class)
public class DomainBusinessAccountUserCountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<DomainBusinessAccountUserEntity>,
                                    DomainBusinessAccountUserCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DomainBusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final DomainBusinessAccountUserService domainBusinessAccountUserService;

    @Override
    public void map(CountResult<DomainBusinessAccountUserEntity> src,
                    DomainBusinessAccountUserCountDTOv1 dst, MapperContext mapperContext) {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst.setUserId(entity.getUserId())
           .setBusinessAccountId(entity.getBusinessAccountId())
           .setCount(src.getCount());
        // Related-object enrichment via @MapperModePointerBinding
        if (mapperContext.hasModeButNot(UserMode.DomainBusinessAccountUser2UserMode.HIDE)) {
            domainBusinessAccountUserService.loadUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getUser(), mapperContext.forkOnPoint(...));
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE)) {
            domainBusinessAccountUserService.loadBusinessAccount(entity);
            businessAccountDTOMapper.convertOrPostpone(entity.getBusinessAccount(), mapperContext.forkOnPoint(...));
        }
    }

    @Override
    public void beforeCollectionConversion(
            Collection<CountResult<DomainBusinessAccountUserEntity>> srcCollection,
            MapperContext mapperContext) {
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).toList();
        // Batch-load related objects before individual mapping
        if (mapperContext.hasModeButNot(UserMode.DomainBusinessAccountUser2UserMode.HIDE))
            domainBusinessAccountUserService.loadUser(entityCollection);
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE))
            domainBusinessAccountUserService.loadBusinessAccount(entityCollection);
    }
}
```

Dedicated mapper for `CountResult<E>` → `CountDTOv1`. Maps group fields from the partially populated entity and the `count` value. Supports related-object enrichment via `@MapperModePointerBinding` — the client can request user/businessAccount details via mapper context modes. `beforeCollectionConversion` batch-loads related objects to avoid N+1.

### 12. Controller

```java id="3w9tgv"
@PostMapping(value = "/private/domain/business_account_user/count/v1")
public ResponseEntity<?> domainBusinessAccountUserCountV1(
        @MapperContextBinding(...) MapperContext mapperContext,
        @RequestBody DomainBusinessAccountUserCountRqDTOv1 request) {

    DomainBusinessAccountUserCountRsDTOv1 rs = new DomainBusinessAccountUserCountRsDTOv1();
    try {
        List<CountResult<DomainBusinessAccountUserEntity>> results =
                domainBusinessAccountUserSearchService.countByGroupFields(
                        domainBusinessAccountUserSearchDTOReverseMapper
                                .convert(request.getSearch(), mapperContext),
                        request.getGroupFields());

        rs.setCounts(domainBusinessAccountUserCountRestDTOMapper
                .convertCollection(results, mapperContext));
        rs.setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
    } catch (ServiceException se) {
        return createErrorRs(se, rs);
    } catch (Exception e) {
        return createErrorRs(e, rs);
    }
    return new ResponseEntity<>(rs, HttpStatus.OK);
}
```

The controller delegates all mapping to `DomainBusinessAccountUserCountRestDTOMapper`. Related objects are populated by the mapper and collected via `relatedObjectsRestDTOMapper.convert(mapperContext)`.

---

## API Contract

**Request:**

```json
{
  "search": {
    "businessAccountIdList": ["uuid-1", "uuid-2"]
  },
  "groupFields": ["userId", "businessAccountId"]
}
```

**Response:**

```json
{
  "counts": [
    {
      "userId": "user-uuid-1",
      "businessAccountId": "ba-uuid-1",
      "count": 42
    },
    {
      "userId": "user-uuid-2",
      "businessAccountId": "ba-uuid-1",
      "count": 7
    }
  ]
}
```

Fields not included in `groupFields` are `null`. `count` is the number of records in the group.

---

## Critical Rules

1. **Only direct fields** — `groupField` works only with direct entity fields. JOIN fields (`user.name`, etc.) are not supported.
2. **groupFields are mandatory** — if the Set is empty → return the total number of records matching the search criteria.
3. **NULL grouping** — PostgreSQL `GROUP BY` automatically groups `NULL` into a separate group. This is correct behavior.
4. **SearchDTO without sort** — count API does not support sorting, only filtering criteria.
5. **Type safety** — enum whitelist, invalid `groupField` → 400 from Jackson.

---

## Example: DomainBusinessAccountUser

### Available groupField Values

| Enum value          | Entity field        | Type          | Field in CountDTOv1 |
| ------------------- | ------------------- | ------------- | ------------------- |
| `userId`            | `userId`            | UUID          | `userId`            |
| `businessAccountId` | `businessAccountId` | UUID          | `businessAccountId` |

> **Do NOT add timestamp/date fields** (e.g., `createdAt`, `lastActivityAt`, `updatedAt`) as group fields. These have high cardinality — each value is almost unique — producing one group per row. GROUP BY on such fields is meaningless and returns as many groups as there are records. Only low-cardinality fields (UUID references to related entities, enums, booleans, status IDs) are valid grouping candidates.

### Request Examples

**Count users by business account:**

```json
{ "groupFields": ["businessAccountId"] }
```

**Count by user + business account pair:**

```json
{ "groupFields": ["userId", "businessAccountId"] }
```

**Total count (without grouping):**

```json
{ "groupFields": [] }
```

→ response: `{ "counts": [{ "count": 1234 }] }`

---

## Phased Rollout

| Phase | What                                                                                                              | Status   |
| ----- | ----------------------------------------------------------------------------------------------------------------- | -------- |
| 1     | GroupField enum + CountRq/Rs DTO + CountService for DomainBusinessAccountUser                                     | Done     |
| 2     | Abstract `EntitySearchService` + `EntitySearch` + `CountQueryExecutor` — eliminated code duplication              | Done     |
| 3     | Generalization to 3-5 priority APIs using EntitySearchService template                                            | Next     |
| 4     | Template for new count/search APIs (enum mandatory, extend EntitySearchService)                                   | Ongoing  |

---

## Indexes (Mandatory)

`GROUP BY` without an index causes Full Table Scan + Sort. Each `groupField` requires an index:

```sql
CREATE INDEX IF NOT EXISTS idx_dba_user_id ON domain_business_account_user(user_id);
CREATE INDEX IF NOT EXISTS idx_dba_ba_id ON domain_business_account_user(business_account_id);
CREATE INDEX IF NOT EXISTS idx_dba_last_activity ON domain_business_account_user(last_activity_at);
CREATE INDEX IF NOT EXISTS idx_dba_created_at ON domain_business_account_user(created_at);
```

For combined grouping (frequent query) — composite index:

```sql
CREATE INDEX IF NOT EXISTS idx_dba_user_ba ON domain_business_account_user(user_id, business_account_id);
```
