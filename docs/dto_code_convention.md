# DTO Code Convention

## 1. General Rules

All DTO classes must follow consistent naming and structure conventions.

* All DTOs must be located in packages under `org/twins/core/dto/**`
* DTOs **must not** contain business logic
* DTOs **must not** depend on Entity classes
* DTOs are used exclusively for data transfer between layers and/or external interfaces

### 1.1 DTO Suffixes

The following mandatory suffixes are used:

* `RqDTO` — DTO for **requests**
* `RsDTO` — DTO for **responses**

Examples:

* `ResourceCreateRqDTO`
* `ResourceSearchRsDTO`

---

## 2. DTO Inheritance Hierarchy

The DTO hierarchy is built around an abstract business entity (for example, `Resource`).

### 2.1 Base DTOs

| DTO                 | Purpose                                         |
| ------------------- | ----------------------------------------------- |
| `ResourceDTO`       | Resource representation                         |
| `ResourceSaveDTO`   | Abstract class for create and update operations |
| `ResourceCreateDTO` | Creating a new resource                         |
| `ResourceUpdateDTO` | Updating an existing resource                   |
| `ResourceSearchDTO` | Search parameters                               |
| `ResourceCountDTO`  | Count result with groupable fields              |

---

### 2.2 DTOs for Create and Update Operations

All DTOs used for adding and updating data **must inherit** from `ResourceSaveDTO`.

`ResourceSaveDTO` contains fields common to create and update operations.

#### Create

```java
@Schema(name = "ResourceCreate")
public class ResourceCreateDTO extends ResourceSaveDTO {
}
```

#### Update

```java
@Schema(name = "ResourceUpdate")
public class ResourceUpdateDTO extends ResourceSaveDTO {

    @Schema
    private UUID id;
}
```

> ⚠️ The identifier (`id`) **must** be present only in update DTOs.

---

## 3. Request DTOs

### 3.1 General Rules

* All request DTOs **must inherit** from the base class `Request`
* Request DTOs use the `RqDTO` suffix

### 3.2 Examples

#### Resource Creation

```java
public class ResourceCreateRqDTO extends Request {
    public List<ResourceCreateDTO> resources;
}
```

#### Resource Update

```java
public class ResourceUpdateRqDTO extends Request {
    public List<ResourceUpdateDTO> resources;
}
```

#### Resource Search

```java
public class ResourceSearchRqDTO extends Request {
    public ResourceSearchDTO search;

    @Schema(description = "Sort field. Default: createdAt")
    public ResourceSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
```

**Sort fields** (`sortField`, `sortDirection`) are placed at the `SearchRqDTO` level, **not inside** `SearchDTO`. This allows reusing `SearchDTO` in other APIs (e.g., grouping) where sorting is not needed.

- `sortField` — enum from the `enums.sort` package (e.g., `ResourceSortField`). Swagger automatically renders a dropdown with values. Jackson deserializes the enum from JSON; invalid values result in a 400 error.
- `sortDirection` — enum `SortDirection` (`ASC` / `DESC`).
- Both fields are optional. Defaults are set in the domain Search Object (`sortField = ResourceSortField.createdAt`, `sortDirection = SortDirection.ASC`).

---

## 4. Response DTOs

### 4.1 General Rules

* All response DTOs use the `RsDTO` suffix
* All response DTOs **must inherit** from:

  * `Response`, or
  * `ResponseRelatedObjectsDTOv1`

---

### 4.2 Response for Create and Update Operations

Common response DTO for create and update operations:

```java
public class ResourceListRsDTO extends Response {
    public List<ResourceDTO> resources;
}
```

---

### 4.3 Search Response

Search response DTO **inherits** from `ResourceListRsDTO` and additionally contains pagination information.

```java
public class ResourceSearchRsDTO extends ResourceListRsDTO {
    public PaginationDTOv1 pagination;
}
```

---

## 5. Count API DTOs

Count APIs return the number of records grouped by one or more fields. The DTO pattern is separate from entity DTOs — count DTOs extend `CountDTOv1` and explicitly declare only groupable fields.

### 5.1 `CountDTOv1` — Base Class

```java
// org.twins.core.dto.rest.CountDTOv1
@Data
@Accessors(chain = true)
@Schema(name = "CountDTOv1")
public class CountDTOv1 {
    @Schema(description = "count of records in this group")
    public Long count;
}
```

Minimal base class with only `count`. Every domain count DTO inherits from it.

### 5.2 Domain Count DTO — Explicit Groupable Fields

```java
@Schema(name = "ResourceCountV1")
public class ResourceCountDTOv1 extends CountDTOv1 {
    @Schema(description = "user id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = UserDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "business account id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;
}
```

Rules:
* Extends `CountDTOv1` — **not** the entity DTO (e.g., not `ResourceDTOv1`)
* Each groupable field is declared explicitly as a simple type (`UUID`, enum, etc.)
* `@RelatedObject` annotations enable related-object enrichment (resolve `userId` → user details)
* Only the fields requested in `groupFields` are populated; absent fields remain `null`

### 5.3 Count Request DTO

```java
@Schema(name = "ResourceCountRqV1")
public class ResourceCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public ResourceSearchDTO search;

    @Schema(description = "Group by fields")
    public Set<ResourceGroupField> groupFields;
}
```

* Reuses the same `ResourceSearchDTO` from the search API (search criteria without sort)
* `groupFields` — `Set<enum>`, Swagger renders a dropdown. Enum is in `enums.sort` package

### 5.4 Count Response DTO

```java
@Schema(name = "ResourceCountRsV1")
public class ResourceCountRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "count results grouped by requested fields")
    public List<ResourceCountDTOv1> counts;
}
```

Extends `ResponseRelatedObjectsDTOv1` — enables related-object enrichment for group field values.

---

## 6. Additional Conventions

* DTOs should be as flat as possible
* Nested DTOs are allowed only when there is a clear business necessity
* Collections in DTOs are always initialized at the service level
* Nullable fields must be explicitly documented via `@Schema`
