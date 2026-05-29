# Rule for Working with Relation Fields in JPA Entities

# Goal

Separate:

1. relation fields required only for `Specification` / `Criteria API`
2. runtime-loaded entities used in business logic

in order to:

* avoid hidden `N+1`
* eliminate accidental lazy loading
* preserve convenient `Criteria API` usage
* preserve the bulk-load pattern
* make intent explicit at the model level

---

# Problem

## `@ManyToOne`

`@ManyToOne` in JPA:

* is required for `root.join(...)`
* but creates the risk of accidental relation traversal

Even with:

```java 
@ManyToOne(fetch = FetchType.LAZY)
```

the following is still dangerous:

```java 
entity.getUser().getName()
```

because it may:

* trigger lazy loading
* cause N+1 queries
* generate hundreds of SQL statements
* work invisibly when `open-in-view=true`

## `@OneToMany`

`@OneToMany` is even more dangerous than `@ManyToOne`:

* loads a **collection** of entities instead of one
* a single accidental `getTags().size()` can trigger loading **thousands** of records
* iterating over the collection in a mapper, logger, or serializer instantly creates N+1

```java 
// One call — a full SELECT per owning entity
twinEntity.getTags().size()       // SELECT * FROM twin_tag WHERE twin_id = ?
twinEntity.getLinksBySrcTwinId()  // SELECT * FROM twin_link WHERE src_twin_id = ?
```

## Common risks

The problem is especially critical in:

* mappers
* loops
* serialization
* logging
* debugger evaluation

---

# Core Principle

## Specification relations and runtime entities are different responsibilities

Therefore, an entity must contain:

### `@ManyToOne`

| Field             | Purpose                                                                    |
| ----------------- |----------------------------------------------------------------------------|
| `<field>SpecOnly` | Only for `Specification` / `Criteria API`                                  |
| `<field>`         | Runtime entity after bulk loading (see load_method_pattern.md for details) |

### `@OneToMany`

| Field                    | Purpose                                                                    |
| ------------------------ |----------------------------------------------------------------------------|
| `<collection>SpecOnly`   | Only for `Specification` / `Criteria API`                                  |
| `<collection>`           | Runtime collection after bulk loading (see load_method_pattern.md)         |

---

# Mandatory Pattern

## Example — `@ManyToOne`

```java 
@Column(name = "created_by_user_id")
private UUID createdByUserId;

@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(
    name = "created_by_user_id",
    insertable = false,
    updatable = false
)
@EqualsAndHashCode.Exclude
@ToString.Exclude
private UserEntity createdByUserSpecOnly;

@Transient
@EqualsAndHashCode.Exclude
private UserEntity createdByUser;
```

## Example — `@OneToMany`

```java 
@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(
    name = "twin_id",
    insertable = false,
    updatable = false
)
@EqualsAndHashCode.Exclude
@ToString.Exclude
private Collection<TwinTagEntity> tagsSpecOnly;

@Transient
@EqualsAndHashCode.Exclude
private Collection<TwinTagEntity> tags;
```

Or with `mappedBy`:

```java 
@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
@EqualsAndHashCode.Exclude
@ToString.Exclude
private Set<UserGroupMapEntity> userGroupMapsSpecOnly;
```

---

# Naming Convention

## Specification-only relation

Required naming pattern:

```text 
<field>SpecOnly
```

`@ManyToOne` examples:

* `createdByUserSpecOnly`
* `twinSpecOnly`
* `parentSpecOnly`
* `statusSpecOnly`

`@OneToMany` examples:

* `tagsSpecOnly`
* `markersSpecOnly`
* `linksBySrcTwinIdSpecOnly`
* `linksByDstTwinIdSpecOnly`
* `userGroupMapsSpecOnly`
* `spaceRoleUsersSpecOnly`

---

## Runtime loaded entity (see load_method_pattern.md for details)

Regular field name:

```text 
<field>
```

`@ManyToOne` examples:

* `createdByUser`
* `twin`
* `parent`
* `status`

`@OneToMany` examples:

* `tags`
* `markers`
* `linksBySrcTwinId`
* `userGroupMaps`

---

# Usage Rules

---

## 1. `<field>SpecOnly` may ONLY be used in:

* `Specification`
* `Criteria API`
* repository query construction

Example:

```java Поля фильтрации:  
root.join(TwinCommentEntity.Fields.createdByUserSpecOnly)
```

---

## 2. `<field>SpecOnly` is forbidden in:

* service layer
* mappers
* controllers
* serializers
* business logic
* validation
* DTO conversion

---

## 3. Runtime access must use only `<field>`

Example:

```java Поля фильтрации:  
comment.getCreatedByUser()
```

where the field:

* is loaded via bulk loading

---

# Protection Against Accidental Lazy Loading

## Mandatory protection

Specification-only relations MUST use:

```java Поля фильтрации:  
@Getter(AccessLevel.NONE)
```

This prevents:

* accidental getter calls
* accidental traversal
* relation usage outside the intended scope

---

## Forbidden

```java:  
comment.getCreatedByUserSpecOnly()
```

Such a getter must not exist.

---

# Fetch Policy

## All `@ManyToOne` and `@OneToMany` relations MUST explicitly define:

```java 
fetch = FetchType.LAZY
```

---

## Forbidden

```java 
@ManyToOne
```

or

```java 
@OneToMany
```

without explicit `LAZY`. It can by EAGER or empty in rare cases see for details  transient_vs_many_to_one.md

---

# Bulk Loading Pattern

Runtime entities must be loaded separately see load_method_pattern.md for details

---

# Why Not Use Only LAZY Relations

Because:

```java 
comment.getCreatedByUserSpecOnly()  // @ManyToOne
twinEntity.getTagsSpecOnly().size() // @OneToMany — loads entire collection!
```

is still:

* valid
* compilable
* capable of triggering SQL
* capable of creating N+1 issues

For `@OneToMany`, the damage is multiplied by the collection size.

`LAZY` reduces the cost of a relation,
but does NOT solve accidental navigation.

---

# Why Not Use Only `@Transient`

Because:

```java Поля фильтрации:  
root.join("createdByUser")
```

requires a relation in the JPA metamodel.

Without `@ManyToOne`:

* type-safe joins are lost
* Criteria API becomes more complex
* manual join conditions appear

---

# Why a Separate Runtime Field Is Required

A runtime field:

* never triggers SQL
* does not depend on the persistence context
* is fully controlled by the application layer
* is safe for mappers and serializers

---

# ArchUnit Rule

A mandatory rule must be added:

> access to `*SpecOnly` fields is forbidden outside:

* repositories
* specifications
* persistence packages

---

# Lombok Rules

## Forbidden

```java Поля фильтрации:  
@Data
```

for Hibernate entities.

---

## Allowed

```java Поля фильтрации:  
@Getter
@Setter
```

with manual control over:

* equals
* hashCode
* toString

---

# Additional Requirements

## Specification-only fields must include:

```java Поля фильтрации:  
@EqualsAndHashCode.Exclude
@ToString.Exclude
```

---

## Runtime transient fields are also recommended to be excluded from:

* equals
* hashCode
* toString

---

# Example of a Final Entity

```java 
@Entity
@Getter
@Setter
@Table(name = "twin_comment")
@Accessors(chain = true)
@FieldNameConstants
public class TwinCommentEntity {

    @Id
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    // --- @ManyToOne: Specification-only relation ---
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by_user_id",
        insertable = false,
        updatable = false
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUserSpecOnly;

    // --- @ManyToOne: Runtime loaded entity ---
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    // --- @OneToMany: Specification-only collection ---
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "twin_id",
        insertable = false,
        updatable = false
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Collection<TwinTagEntity> tagsSpecOnly;

    // --- @OneToMany: Runtime loaded collection ---
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Collection<TwinTagEntity> tags;
}
```

---

# Expected Outcome

## Architectural Benefits

* Explicit separation between query-model and runtime-model
* Elimination of accidental lazy loading
* Reduced N+1 risk
* Safer mappers
* Controlled bulk loading
* Preserved Criteria API convenience

---

## Performance Benefits

* Predictable SQL
* Controlled query count
* No hidden lazy loads
* Reduced latency
* Reduced database load

---

# Final Rule

## A relation inside an entity is NOT a runtime navigation API

A relation:

* exists for query construction
* is not intended for business traversal

Runtime navigation must happen only through separately loaded transient fields.
