# Architecture: Count API with Grouping for Search

**Date:** 2026-05-28
**Status:** Proposed
**Context:** TWINS-831 — count API alongside every search API

---

## Context

Each search API must have a paired count API that calculates the number of records grouped by one or more grouping attributes. For example: how many users are in each business account, how many records exist per creation date, etc. Search criteria (`SearchDTO`) are reused from the search API — sort fields remain in `SearchRqDTO` and are not included in count.

---

## Decision

### Principle

The count API accepts the **same SearchDTO** (search criteria) + **Set<enum> groupFields** (grouping fields). The result is an array of `CountDTOv1[]`, where each element inherits from `DTOv1` (only group fields are populated) + the `count` field.

### Why enum for groupFields

* Similar to sortField — compile-time whitelist, Swagger displays a dropdown
* Enum in `enums.sort` — reuse the same package
* Only direct entity fields (without JOIN) — simplifies `GROUP BY`

### Why inheritance from DTOv1 with partial population

* `CountDTOv1` inherits from `DTOv1` — the client sees a familiar schema
* Mapper populates only group fields, the rest = `null`
* No need to create a separate DTO for every grouping combination

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

### 3. Count Item DTO — Inherits from DTOv1

```java id="9lyx2t"
// DomainBusinessAccountUserCountDTOv1.java
@Schema(name = "DomainBusinessAccountUserCountV1")
public class DomainBusinessAccountUserCountDTOv1 extends DomainBusinessAccountUserDTOv1 {
    @Schema(description = "count of records in this group")
    public Long count;
}
```

Inherits from `DomainBusinessAccountUserDTOv1`. Mapper populates only the fields used for grouping. All other fields = `null`. Additionally contains `count`.

### 4. Count Response DTO

```java id="priq62"
// DomainBusinessAccountUserCountRsDTOv1.java
@Schema(name = "DomainBusinessAccountUserCountRsV1")
public class DomainBusinessAccountUserCountRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "count results grouped by requested fields")
    public List<DomainBusinessAccountUserCountDTOv1> counts;
}
```

### 5. Base Helper in `CommonSpecification`

```java id="s9ls0m"
// CommonSpecification.java
public static <T> Specification<T> toCountSpecification(List<String> fields) {
    return (root, query, cb) -> {
        List<Path<?>> groupPaths = fields.stream()
            .map(root::get)
            .toList();

        query.groupBy(groupPaths);

        List<Selection<?>> selections = new ArrayList<>(groupPaths);
        selections.add(cb.count(root));
        query.multiselect(selections);

        return cb.conjunction();
    };
}
```

Accepts `List<String>` of direct entity field names (no JOINs — grouping is only over direct fields). Single specification with `GROUP BY` over all fields at once + `COUNT`. Reused by all count APIs.

### 6. Service — `createCountSpecification` with switch

```java id="p69h3u"
// DomainBusinessAccountUserSearchService.java
public List<Object[]> countByGroupFields(DomainBusinessAccountUserSearch search,
                                          Set<DomainBusinessAccountUserGroupField> groupFields) {
    UUID domainId = authService.getApiUser().getDomainId();
    Specification<DomainBusinessAccountUserEntity> filterSpec = createSearchSpecification(search, domainId);

    // If groupFields is empty — return total count
    if (CollectionUtils.isEmpty(groupFields)) {
        long total = domainBusinessAccountUserRepository.count(filterSpec);
        return List.of(new Object[]{total});
    }

    Specification<DomainBusinessAccountUserEntity> countSpec = createCountSpecification(groupFields);
    return domainBusinessAccountUserRepository.findAll(Specification.allOf(filterSpec, countSpec));
}

private Specification<DomainBusinessAccountUserEntity> createCountSpecification(
        Set<DomainBusinessAccountUserGroupField> groupFields) {
    List<String> fields = groupFields.stream()
        .map(field -> switch (field) {
            case userId -> DomainBusinessAccountUserEntity.Fields.userId;
            case businessAccountId -> DomainBusinessAccountUserEntity.Fields.businessAccountId;
        })
        .toList();
    return toCountSpecification(fields);
}
```

The pattern is analogous to `createSortSpecification`: switch by enum → field name → call base helper in `CommonSpecification`. The switch produces a `List<String>` of field names, passed as a single `toCountSpecification(fields)` call.

### 7. Result Mapping → CountDTOv1

The enum switch determines which `Object[]` array element should be written into which DTO field.

```java id="vjlwmz"
// DomainBusinessAccountUserCountMapper.java (or inside service)
public DomainBusinessAccountUserCountDTOv1 mapCountResult(
        Object[] row,
        Set<DomainBusinessAccountUserGroupField> groupFields,
        MapperContext mapperContext) {

    DomainBusinessAccountUserCountDTOv1 dto = new DomainBusinessAccountUserCountDTOv1();

    int i = 0;
    for (DomainBusinessAccountUserGroupField field : groupFields) {
        switch (field) {
            case userId -> dto.setUserId((UUID) row[i]);
            case businessAccountId -> dto.setBusinessAccountId((UUID) row[i]);
        }
        i++;
    }

    // Last element — COUNT(*)
    dto.setCount((Long) row[i]);

    return dto;
}
```

The enum switch determines which `Object[]` array element should be written into which DTO field.

### 8. Controller

```java id="3w9tgv"
@PostMapping(value = "/private/domain/business_account_user/count/v1")
public ResponseEntity<?> domainBusinessAccountUserCountV1(
        @MapperContextBinding(...) MapperContext mapperContext,
        @RequestBody DomainBusinessAccountUserCountRqDTOv1 request) {

    DomainBusinessAccountUserCountRsDTOv1 rs = new DomainBusinessAccountUserCountRsDTOv1();
    try {
        DomainBusinessAccountUserSearch search = searchRqDTOReverseMapper
            .convert(new DomainBusinessAccountUserSearchRqDTOv1().setSearch(request.getSearch()), mapperContext);
        Set<DomainBusinessAccountUserGroupField> groupFields = request.getGroupFields();

        List<Object[]> results = countService.countByGroupFields(search, groupFields);

        List<DomainBusinessAccountUserCountDTOv1> counts = results.stream()
            .map(row -> countMapper.mapCountResult(row, groupFields, mapperContext))
            .toList();

        rs.setCounts(counts);
    } catch (ServiceException se) {
        return createErrorRs(se, rs);
    } catch (Exception e) {
        return createErrorRs(e, rs);
    }
    return new ResponseEntity<>(rs, HttpStatus.OK);
}
```

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

| Phase | What                                                                          | Status           |
| ----- | ----------------------------------------------------------------------------- | ---------------- |
| 1     | GroupField enum + CountRq/Rs DTO + CountService for DomainBusinessAccountUser | Proposed         |
| 2     | Generalization to 3-5 priority APIs                                           | After validation |
| 3     | Template for new count APIs (enum mandatory)                                  | Ongoing          |

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
