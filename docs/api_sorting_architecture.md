# Architecture: Dynamic Sorting for Search API

**Date:** 2026-06-01 (updated)
**Status:** Partially Implemented (pilot on DomainBusinessAccountUserSearch)
**Context:** TWINS-831 — sorting is missing on 74+ search APIs

---

## Context

The project contains many search controllers with pagination (`@SimplePaginationParams`) but without dynamic sorting. `sortField` is hardcoded in the annotation at compile time. The client cannot change the sorting field. This is especially critical because sorting by RelatedObject fields (for example, `user.name` instead of `userId`) requires JOINs to related tables.

---

## Solution

### Principle

Sorting is implemented through a **simple enum per entity** in the `enums.sort` package. The enum contains only field names — no JPA logic and no fieldPath. The JPA Specification is built in **SearchService** through a switch on the enum, which calls the static helper `CommonSpecification.toSortSpecification(ascending, fieldPath...)`. Sort fields are inlined directly into **SearchRqDTO** (not **SearchDTO**) — Swagger automatically displays a dropdown with available values.

### Why Specification-only instead of Pageable.getSort()

Spring Data JPA bug [#2253](https://github.com/spring-projects/spring-data-jpa/issues/2253): when combining Specification + `Pageable.getSort()` on a nested path (`user.name`), Spring Data creates a **duplicate JOIN** on top of the one already added by the Specification. This results in incorrect result sets and breaks count queries.

### Why enum instead of annotation

* Enum = compile-time type safety + natural whitelist
* Enum = Swagger automatically displays a dropdown (Jackson deserializes from JSON)
* Enum = easy to test
* Annotation `@SortableFields` on the controller = responsibility mixing (controller knows about the entity graph)

### Why enum without fieldPath, and switch in SearchService

* Enum in `enums.sort` does not depend on the DAO layer — it is a pure list of names
* SearchService owns knowledge about the JPA entity graph — fieldPath logically belongs there
* Switch provides full flexibility: custom logic can be added for a specific field (subquery, different JOIN type, etc.)
* When adding a new sort field: add a value to the enum + add a case in the switch

### Why sort is inline in SearchRqDTO instead of a separate SortDTO

* `SortDTOv1` with `String field` — Swagger displays a text field, the client does not see allowed values
* Enum type directly in SearchRqDTO — Swagger automatically generates a dropdown
* Jackson automatically deserializes a JSON string into an enum; invalid value → 400 Bad Request
* Sort fields are located in `SearchRqDTO` next to `search`, not inside `SearchDTO` — this allows reusing `SearchDTO` in other APIs (for example, grouping) where sorting is not needed

---

## Components

### 1. Enum in `enums.sort` — simple list of names

```java
package org.twins.core.enums.sort;

public enum DomainBusinessAccountUserSortField {
    createdAt,
    lastActivityAt,
    userName,
    businessAccountName
}
```

No constructor, no fieldPath, no SortField<T>. Pure data carrier.

### 2. Static helpers in `CommonSpecification` and `I18nSpecification`

```java
// CommonSpecification.java — for regular fields
public static <T> Specification<T> toSortSpecification(boolean ascending, String... fieldPath) {
    if (fieldPath == null)
        return (root, query, cb) -> cb.conjunction();
    return (root, query, cb) -> {
        if (query.getResultType().equals(Long.class))
            return cb.conjunction();
        Path<?> sortPath = getFieldPath(root, JoinType.LEFT, fieldPath);
        List<Order> orders = new ArrayList<>(query.getOrderList());
        orders.add(ascending ? cb.asc(sortPath) : cb.desc(sortPath));
        query.orderBy(orders);
        return cb.conjunction();
    };
}

// I18nSpecification.java — for i18n fields (sorting by translation)
public static <T> Specification<T> toSortSpecification(boolean ascending, Locale locale, String... fieldPath) {
    // Navigate through fieldPath → LEFT JOIN I18nTranslationEntity with locale in ON → ORDER BY translation
}
```

Signature `(boolean ascending, String... fieldPath)` — varargs instead of `String[]`, making calls shorter: `toSortSpecification(ascending, "businessAccount", "name")`.

Logic: count-query guard → `AbstractSpecification.getFieldPath(root, LEFT, fieldPath)` → `orderBy`.

### 3. SearchRqDTO — sort fields next to search

```java
// DomainBusinessAccountUserSearchRqDTOv1.java
public class DomainBusinessAccountUserSearchRqDTOv1 extends Request {
    public DomainBusinessAccountUserSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public DomainBusinessAccountUserSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
```

Sort fields are located at the `SearchRqDTO` level, not inside `SearchDTO`. This allows reusing `SearchDTO` in grouping APIs and other endpoints where sorting is not needed.

Swagger automatically displays a dropdown for `sortField`: `createdAt`, `lastActivityAt`, `userName`, `businessAccountName`.

### 4. SearchDTO — pure search parameters

```java
// DomainBusinessAccountUserSearchDTOv1.java — search parameters only, without sort
public class DomainBusinessAccountUserSearchDTOv1 {
    public Set<UUID> userIdList;
    public Set<UUID> businessAccountIdList;
    public DataTimeRangeDTOv1 lastActivityAt;
    // ... search criteria only
}
```

### 5. EntitySearch — base class for search objects

```java
// domain/search/EntitySearch.java
public abstract class EntitySearch<E> {
}
```

All search objects extend `EntitySearch<EntityType>`. Sort fields are **not stored** in the search object — they are passed as parameters into `EntitySearchService.search()`.

### 6. Domain Search Object — pure search criteria

```java
// DomainBusinessAccountUserSearch.java
public class DomainBusinessAccountUserSearch extends EntitySearch<DomainBusinessAccountUserEntity> {
    private Set<UUID> userIdList;
    private Set<UUID> userIdExcludeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<UUID> userGroupIdList;
    private Set<UUID> userGroupIdExcludeList;
    private DataTimeRange lastActivityAtRange;
    private DataTimeRange createdAtRange;
}
```

### 7. EntitySearchService — base search service

```java
// EntitySearchService.java
public abstract class EntitySearchService<
        S extends EntitySearch<E>,  // search object
        E,                          // JPA entity
        SF,                         // sort field enum
        GF> {                       // group field enum

    // Common methods (implemented in the base class):
    public PaginationResult<E> search(S search, SimplePagination pagination,
                                       SF sortField, SortDirection sortDirection);
    public List<CountResult<E>> countByGroupFields(S search, Set<GF> groupFields);

    // Abstract methods (implemented by a concrete service):
    abstract JpaSpecificationExecutor<E> jpaSpecificationExecutor();
    abstract S emptySearch();
    abstract Class<E> entityClass();
    abstract Specification<E> createFilterSpecification(S search, UUID domainId);
    abstract Specification<E> createSortSpecification(SF sortField, SortDirection sortDirection);
    abstract String convertToEntityField(GF groupField);
    abstract void mapGroupedField(E entity, GF field, Object value);
    abstract E newEntity();
}
```

The base class encapsulates common logic: obtaining `domainId` from `AuthService`, combining filter + sort specifications, executing paginated queries through `JpaSpecificationExecutor`, and grouped counting through `CountQueryExecutor`.

The concrete service implements only entity-specific methods: filters, switch by sort enum, and group field mapping.

### 8. SearchRqDTOReverseMapper — mapping search + sort

```java
// Controller calls:
PaginationResult<DomainBusinessAccountUserEntity> result =
    searchService.search(search, pagination, src.getSortField(), src.getSortDirection());
```

Sort fields (`sortField`, `sortDirection`) are passed directly from `SearchRqDTO` into `EntitySearchService.search()` — they do not become part of the search object. Jackson has already deserialized the enum from JSON. `SortDTOReverseMapper` is not needed.

### 9. SearchService — switch by enum

```java
// DomainBusinessAccountUserSearchService.java — extends EntitySearchService
@Override
public Specification<DomainBusinessAccountUserEntity> createSortSpecification(
        DomainBusinessAccountUserSortField sortField, SortDirection sortDirection) {
    if (sortField == null)
        sortField = DomainBusinessAccountUserSortField.createdAt;
    boolean ascending = sortDirection != SortDirection.DESC;
    return switch (sortField) {
        case createdAt -> toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.createdAt);
        case lastActivityAt -> toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.lastActivityAt);
        case userName -> toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.user, UserEntity.Fields.name);
        case businessAccountName -> toSortSpecification(ascending, DomainBusinessAccountUserEntity.Fields.businessAccount, BusinessAccountEntity.Fields.name);
    };
}
```

**How it works:**

* `createdAt` → `toSortSpecification(ascending, "createdAt")` → `getFieldPath` uses `root.get("createdAt")` — no JOIN
* `userName` → `toSortSpecification(ascending, "user", "name")` → `getFieldPath` performs `root.join("user", LEFT)` + `.get("name")`
* `AbstractSpecification.getFieldPath` + `getOrCreateJoin` guarantee that if a JOIN already exists (from filtering), it will be reused instead of duplicated

### 10. Controller — unchanged

```java
@SimplePaginationParams SimplePagination pagination,
@RequestBody DomainBusinessAccountUserSearchRqDTOv1 request
```

`pagination.setSort(null)` in the service removes Sort from Pageable. The sort field comes from the request body through the search DTO.

---

## Indexes (mandatory)

```sql
-- For sorting by user.name
CREATE INDEX IF NOT EXISTS idx_user_name ON "user"(name);

-- For sorting by business_account.name
CREATE INDEX IF NOT EXISTS idx_business_account_name ON business_account(name);

-- For JOIN lookup (optional, speeds up Nested Loop)
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_user_domain
    ON domain_business_account_user(user_id, domain_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_ba_domain
    ON domain_business_account_user(business_account_id, domain_id);
```

---

## Performance estimation

| Records in domain | Sort by created_at (indexed) | Sort by user.name (WITHOUT index) | Sort by user.name (With index) |
| ----------------- | ---------------------------- | --------------------------------- | ------------------------------ |
| 1,000             | ~2ms                         | ~5ms                              | ~2ms                           |
| 10,000            | ~3ms                         | ~30ms                             | ~3ms                           |
| 100,000           | ~5ms                         | **~350ms**                        | ~5ms                           |
| 1,000,000         | ~10ms                        | **~4000ms**                       | ~8ms                           |

**Conclusion:** without indexes, sorting by JOIN fields degrades as O(N log N). With proper indexes, execution time is almost constant due to early termination with LIMIT.

### 11. I18n fields — sorting through translation

Entities whose name/description are stored through I18n (for example `NotificationSchemaEntity.nameI18n`) require special handling: instead of sorting by a direct field, sorting is performed through a LEFT JOIN to `I18nEntity` → `I18nTranslationEntity` filtered by locale.

#### Problem

```java
// INCORRECT — fieldPath ends at I18nEntity instead of a string
case notificationSchemaName -> new String[]{"notificationSchemaSpecOnly", "nameI18n"};
// CommonSpecification.toSortSpecification will try to sort by I18nEntity — makes no sense
```

An I18n field stores not a string but a `UUID` → `I18nEntity` → `List<I18nTranslationEntity>` (one record per locale). An additional JOIN to `I18nTranslationEntity` is required with filtering by the current user's locale.

#### Solution: `I18nSpecification.toSortSpecification`

```java
// I18nSpecification.java
public static <T> Specification<T> toSortSpecification(
    boolean ascending,
    Locale locale,
    String... fieldPath   // path TO I18nEntity (e.g. "notificationSchemaSpecOnly", "nameI18n")
)
```

**How it works:**

1. Navigates through `fieldPath` to I18nEntity (using `findOrCreateJoin` to reuse existing JOINs from filtering)
2. LEFT JOINs `I18nTranslationEntity` with locale in the **ON clause** (not WHERE — to avoid filtering out records without translation)
3. Sorts by `I18nTranslationEntity.translation`
4. Contains a `getResultType()` guard for count queries

**Resulting SQL:**

```sql
LEFT JOIN i18n_translation translation_join
    ON translation_join.i18n_id = i18n.id
    AND translation_join.locale = 'en'
ORDER BY translation_join.translation ASC
```

#### Integration into SearchService switch

```java
// SomeSearchService.createSortSpecification(sortField, sortDirection)
return switch (sortField) {
    case createdAt -> toSortSpecification(ascending, "createdAt");
    // ... regular fields via CommonSpecification.toSortSpecification

    case notificationSchemaName -> I18nSpecification.toSortSpecification(
        ascending, locale, "notificationSchemaSpecOnly", "nameI18n");
};
```

When i18n sorting is required, `createSortSpecification` may accept `Locale locale` (via `authService.getApiUser().getLocale()`), or locale may be obtained directly inside the method from `authService`.

#### Which entities use i18n fields

According to SORTING-ALL-SEARCH-API.md, i18n sorting will be required for:

| Entity                     | I18n fields                   |
| -------------------------- | ----------------------------- |
| `NotificationSchemaEntity` | `nameI18n`, `descriptionI18n` |
| `TwinClassFieldEntity`     | `nameI18n`, `descriptionI18n` |
| `TwinStatusEntity`         | `nameI18n`, `descriptionI18n` |
| `SpaceRoleEntity`          | `nameI18n`, `descriptionI18n` |
| `UserGroupEntity`          | `nameI18n`, `descriptionI18n` |
| `DataListOptionEntity`     | `nameI18n`, `descriptionI18n` |

For each such field, the switch calls `I18nSpecification.toSortSpecification` instead of `CommonSpecification.toSortSpecification`.

#### Indexes for i18n sorting

```sql
-- Index on i18n_translation for fast locale lookup
CREATE INDEX IF NOT EXISTS idx_i18n_translation_i18n_locale
    ON i18n_translation(i18n_id, locale);

-- Covering index for sorting (includes translation)
CREATE INDEX IF NOT EXISTS idx_i18n_translation_locale_translation
    ON i18n_translation(locale, translation);
```

---

## Critical rules

1. **`getResultType()` guard** — MANDATORY in `CommonSpecification.toSortSpecification()`. Count queries must not contain ORDER BY.
2. **LEFT JOIN, not INNER JOIN** — to avoid filtering out records with NULL relationships.
3. **Whitelist** — enum values are the only allowed sort fields. Invalid value → 400 from Jackson.
4. **Pageable without Sort** — when using Specification-based sorting, Sort in Pageable must be unsorted (`pagination.setSort(null)`).
5. **Do not touch TwinSorter** — the existing feature-based Twin sorting mechanism solves a different problem (dynamic fields).

---

## API contract

**Request:**

```json
{
  "search": {
    "userIdList": ["..."]
  },
  "sortField": "userName",
  "sortDirection": "DESC"
}
```

Sort fields are located at the top level of `SearchRqDTO`, not inside `search`.

Jackson automatically deserializes `sortField` from a JSON string into an enum. Invalid value → 400 Bad Request with a clear error message.

---

## Phased rollout

| Phase | What                                                                                                                                             | Status                 |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------- |
| 1     | Enum in `enums.sort` + inline sort in SearchDTO + `EntitySearchService` / `EntitySearch` base classes + pilot on DomainBusinessAccountUserSearch | **Done**               |
| 2     | Generalization to 3–5 priority APIs (children of `EntitySearchService`)                                                                          | After pilot validation |
| 3     | Template for new search APIs (enum mandatory)                                                                                                    | Ongoing                |
| 4     | Cursor-based pagination for tables > 500K (if needed)                                                                                            | As data volume grows   |

---

## Deprecated components (kept for backward compatibility)

The following components are no longer used by the new pattern but remain in the codebase:

* `SortDTOv1` — replaced by inline sort fields in SearchDTO (enum type)
* `SortDTOReverseMapper` — replaced by direct mapping in SearchDTOReverseMapper
* `SortField<T>` interface — replaced by a static helper in `CommonSpecification`
* `SortOption<S>` — replaced by direct `sortField` + `sortDirection` fields

These components will be removed when the solution is rolled out to all APIs.

---

## Sources

* Spring Data JPA #2253: Specifications with Sort create additional join
* Spring Data JPA #4178: Sort field whitelist validation
* CVE-2016-6652: Blind SQL/JPQL Injection via Sort Parameters
* PGAnalyze: Postgres Planner Quirks with ORDER BY + LIMIT
* Milan Jovanovic: Cursor Pagination Deep Dive
* Baeldung: Joining Tables With Specifications
* Vlad Mihalcea: JOIN FETCH and Pagination
