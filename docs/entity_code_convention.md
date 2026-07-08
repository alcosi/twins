# Entity Code Convention

**Status:** Accepted
**Date:** 2026-06-17
**Supersedes:** `entity_many_to_one_relations.md`, `transient_vs_many_to_one.md`

This document defines how JPA entities in Twins represent:

1. Foreign-key relationships to other aggregates
2. Specification-only relations (query construction)
3. Runtime-loaded data (business logic, mappers, serializers)
4. I18n translation fields (the `i18n_translation` direct-join pattern)

---

## 1. Core Principle

> An ORM relationship inside an entity is **NOT** a runtime navigation API.

A relationship exists for **query construction**. Runtime navigation must happen only through separately loaded transient fields (see load_method_pattern.md for details).

The existence of an FK in the database does **not** imply the need for `@ManyToOne` in the entity. The domain model is built around business logic, aggregates, and performance — not around mirroring the DB schema.

---

## 2. Three responsibilities, three field kinds

For every FK column the entity must distinguish:

| Responsibility         | Field shape                                   | Loaded by                     |
| ---------------------- | --------------------------------------------- | ----------------------------- |
| Raw FK id              | `private UUID someId`                         | JPA automatically             |
| Specification relation | `private ... someSpecOnly`                    | Not loaded — query only       |
| Runtime entity         | `@Transient private ... some`                 | Service bulk loading          |
| I18n translation       | `private List<I18nTranslationEntity> someI18nTranslationsSpecOnly` | Query only (LEFT JOIN by FK)  |

A single FK may have any subset of these, depending on usage.

---

## 3. When to use `@ManyToOne`

### 3.1 Allowed

`@ManyToOne` is allowed only when **at least one** is true:

- The related entity participates in core business logic
- The relationship is conceptually part of the aggregate
- The query genuinely needs the JOIN

Even then, prefer the SpecOnly + `@Transient` runtime split (see §5).

### 3.2 Forbidden

- Adding `@ManyToOne` only because an FK exists
- `@ManyToOne` without explicit `fetch = FetchType.LAZY`
- `EAGER` outside truly aggregate-defining relationships (rare; document each case)
- Using the relation field for runtime navigation in mappers, serializers, business logic, validation, DTO conversion
- **`@ManyToOne` (or any JPA relation) to `org.cambium.featurer.dao.FeaturerEntity`** — see §3.4

### 3.4 Featurer relations — not reconmended on Twins entities

Not recommended:

```java
// ❌ useless
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "multiplier_featurer_id", insertable = false, updatable = false)
private FeaturerEntity multiplierFeaturerSpecOnly;
```

This applies regardless of the FK column type — `UUID`, `Integer`, anything. The column itself (`multiplierFeaturerId: Integer`) is fine; the relation is not.

**Implications:**
- **No JOIN-based sort** by `featurer.name` in `createSortSpecification`. Drop the enum value (`multiplierFeaturerName`, `fillerFeaturerName`, `conditionerFeaturerName`, etc.) from `*SortField`.
- **Enrichment still possible** via `FeaturerRestDTOMapper.postpone(Integer featurerId, MapperContext)` — it calls `featurerService.getFeaturerEntity(featurerId)` internally and resolves the entity by ID (no JPA relation on the Twins entity). Use this in count/search mappers when you need the featurer DTO in related objects.
- Group/count by `featurerId` itself still works — `GROUP BY` operates on the column, not on a relation.

If a true JOIN to `FeaturerEntity` is ever required (very rare), it must be done with a manual Criteria API `Join<>` constructed in the Specification — never by declaring a persistent relation on the entity.

### 3.3 Risk

`@ManyToOne` is required for `root.join(...)` in Criteria API, but even with `LAZY`:

```java
entity.getUser().getName()   // triggers lazy load → N+1
```

`@OneToMany` is worse — one accidental call loads an entire collection:

```java
twinEntity.getTagsSpecOnly().size()  // SELECT * FROM twin_tag WHERE twin_id = ?
```

These calls are especially dangerous in mappers, loops, serialization, logging, and debugger evaluation.

---

## 4. Naming Convention

### 4.1 Raw FK id

Plain field named after the FK column's logical role:

```
createdByUserId, twinClassId, srcTwinClassId, nameI18nId
```

### 4.2 Specification-only relation

Append `SpecOnly`:

```
createdByUserSpecOnly, twinSpecOnly, parentSpecOnly, statusSpecOnly
```

For collections:

```
tagsSpecOnly, markersSpecOnly, linksBySrcTwinIdSpecOnly, userGroupMapsSpecOnly
```

### 4.3 Runtime entity

Plain field name, loaded via service bulk loading:

```
createdByUser, twin, parent, status, tags, markers
```

### 4.4 I18n translation collection

Append `TranslationsSpecOnly` (note the plural — it points at `I18nTranslationEntity`, not `I18nEntity`):

```
nameI18nTranslationsSpecOnly, descriptionI18nTranslationsSpecOnly,
forwardNameI18nTranslationsSpecOnly, optionI18nTranslationsSpecOnly
```

---

## 5. Mandatory Pattern — plain relation

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
@ToString.Exclude
private UserEntity createdByUser;
```

`@Getter(AccessLevel.NONE)` is mandatory on SpecOnly fields — it makes `getCreatedByUserSpecOnly()` not compile, preventing accidental traversal.

For `@OneToMany` SpecOnly, the same pattern applies — either with `@JoinColumn` (unidirectional) or `mappedBy` (bidirectional):

```java
@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(name = "twin_id", insertable = false, updatable = false)
@EqualsAndHashCode.Exclude
@ToString.Exclude
private Collection<TwinTagEntity> tagsSpecOnly;

@Transient
@EqualsAndHashCode.Exclude
private Collection<TwinTagEntity> tags;
```

---

## 6. I18n Pattern — direct join to `i18n_translation`

### 6.1 Problem

Naive JPA navigation goes through `I18nEntity.translations`:

```
TwinClassEntity.nameI18n  →  I18nEntity  →  I18nTranslationEntity
```

This produces two LEFT JOINs — one to `i18n` (intermediate) and one to `i18n_translation`. The intermediate join reads no columns; it just bridges the FK.

### 6.2 Solution — `@OneToMany` with `referencedColumnName`

Map `i18n_translation` directly by raw FK column, skipping the intermediate table. **Always apply the `@Access(PROPERTY)` + NOOP getter/setter workaround (see §6.6)** — without it, Hibernate throws `Found shared references to a collection` on flush whenever multiple entity rows share the same FK target:

```java
@Column(name = "name_i18n_id")
private UUID nameI18nId;

// Direct join to i18n_translation by raw FK — skips intermediate i18n table.
// HACK: @Access(PROPERTY) + NOOP getter/setter — see §6.6 for the full explanation.
@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@Setter(AccessLevel.NONE)
@EqualsAndHashCode.Exclude
@ToString.Exclude
@Access(AccessType.PROPERTY)
@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(
    name = "i18n_id",
    referencedColumnName = "name_i18n_id",
    insertable = false,
    updatable = false
)
private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

public List<I18nTranslationEntity> getNameI18nTranslationsSpecOnly() {
    return null;
}

public void setNameI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
    // NOOP — never store PersistentBag, so Hibernate flush visitor sees null
}
```

The `referencedColumnName` points at the FK column on **this** entity; `name` points at the join column on `i18n_translation`.

Resulting SQL — one join instead of two:

```sql
LEFT JOIN i18n_translation t
       ON t.i18n_id = twin_class.name_i18n_id
      AND t.locale = ?
```

NULL FK rows are preserved (LEFT OUTER JOIN semantics).

### 6.3 Where the field lives

Add the `*TranslationsSpecOnly` field on the **holder** entity — the one that owns the FK column. Examples:

- `TwinClassEntity.nameI18nTranslationsSpecOnly` (FK: `name_i18n_id`)
- `LinkEntity.forwardNameI18nTranslationsSpecOnly` (FK: `forward_name_i18n_id`)
- `DataListOptionEntity.optionI18nTranslationsSpecOnly` (FK: `option_i18n_id`)

### 6.4 Usage in specifications

Use the `*Direct` variants in `I18nSpecification`:

```java
I18nSpecification.toSortSpecificationDirect(ascending, locale,
    TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);

I18nSpecification.toSortSpecificationDirect(ascending, locale,
    LinkEntity.Fields.srcTwinClass,
    TwinClassEntity.Fields.nameI18nTranslationsSpecOnly);

I18nSpecification.joinAndSearchByI18NFieldDirect(
    TwinClassEntity.Fields.nameI18nTranslationsSpecOnly,
    search.getNameI18nLikeList(), locale, false, false);

I18nSpecification.doubleJoinAndSearchByI18NFieldDirect(
    DataListEntity.Fields.dataListOptions,
    DataListOptionEntity.Fields.optionI18nTranslationsSpecOnly,
    search.getOptionI18nLikeList(), locale, false, false);
```

### 6.5 Create / update flows — use a domain envelope, not a `@Transient` carrier

When a REST request needs to create / update an entity with i18n data, **do not** declare `@Transient I18nEntity xxxI18n` on the entity to carry the request payload between mapper and service. That mixes persistence state with request-scoped data and blurs the entity boundary.

Instead, use a **domain save envelope** (`XxxSave` + `XxxCreate` / `XxxUpdate`). The envelope carries the entity + auxiliary `I18nEntity` instances; the service resolves them into FK ids. The entity itself stays clean — only raw columns + JPA-mapped relations.

See [`docs/domain_save_envelope.md`](domain_save_envelope.md) for the full pattern with a `NotificationSchemaSave` reference implementation.

The `@ManyToOne xxxI18n` pattern (mapping directly to `I18nEntity`) is **forbidden** — it forces an extra JOIN in every specification query that touches the same FK, and conflates "spec relation" with "runtime carrier". Use `*TranslationsSpecOnly` `@OneToMany` + domain envelope instead.

### 6.6 Required workaround — `@Access(PROPERTY)` + NOOP getter/setter

**Symptom.** Without this workaround, any entity holding a `*I18nTranslationsSpecOnly` collection will throw at flush time:

```
org.hibernate.HibernateException: Found shared references to a collection:
    org.twins.core.dao.twinclass.TwinClassFieldEntity.nameI18nTranslationsSpecOnly
```

The error fires from `Collections.processReachableCollection` whenever multiple entity instances in the persistence context share the same non-unique FK target (very common for i18n — multiple fields/classes reuse the same `i18n_id`).

**Root cause.** `referencedColumnName = "name_i18n_id"` points at a **non-unique** column. Hibernate creates a `PersistentBag` wrapper per `@OneToMany` mapping; during auto-flush it traverses every entity's collections and verifies in `PersistenceContext.collectionEntries` that each wrapper is uniquely owned. With non-unique `referencedColumnName`, wrappers get shared/lost and the check fails.

This is a known limitation of unidirectional `@OneToMany` + `@JoinColumn(referencedColumnName = ...)` in Hibernate 6.x — Vlad Mihalcea's recommendation is to make the relationship bidirectional or point at a unique column, both of which defeat the purpose of the direct-join optimization (§6.2).

**Workaround.** Switch the field from default `FIELD` access to `PROPERTY` access, and provide a NOOP getter/setter pair. The getter returns `null`; the setter discards any value Hibernate tries to assign:

```java
@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@Setter(AccessLevel.NONE)
@EqualsAndHashCode.Exclude
@ToString.Exclude
@Access(AccessType.PROPERTY)
@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(
    name = "i18n_id",
    referencedColumnName = "name_i18n_id",
    insertable = false,
    updatable = false
)
private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

public List<I18nTranslationEntity> getNameI18nTranslationsSpecOnly() {
    return null;
}

public void setNameI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
    // NOOP — never store PersistentBag, so Hibernate flush visitor sees null
}
```

**Mechanism** (verified against Hibernate 6.6.53 bytecode):

1. On entity load, Hibernate calls the setter to inject the `PersistentBag`. NOOP setter discards it; the field stays `null`.
2. During flush, `FlushVisitor.processCollection` reads the value via the getter — gets `null`.
3. Bytecode branches on `value == null` → **early return** (lines 9-10 of `FlushVisitor.processCollection`). `Collections.processReachableCollection` is **never invoked** — no throw.
4. `@OneToMany` + `@JoinColumn` metadata is preserved, so Criteria API `root.join("nameI18nTranslationsSpecOnly")` continues to work normally.

**Cost.** ~6 lines of boilerplate per field (getter + setter). Considered acceptable — a compile-time codegen alternative (annotation processor) is not viable in pure JLS scope (it cannot modify existing classes), and Lombok-style AST patching is too fragile to maintain.

**Always apply** this workaround to every `*I18nTranslationsSpecOnly` field, even if the FK is currently unique — schema data changes over time and the error surfaces only at flush, which is hard to reproduce in tests.

---

## 7. Lombok Rules

### 7.1 Forbidden

```java
@Data
```

on Hibernate entities.

### 7.2 Allowed

```java
@Getter
@Setter
```

with manual control over `equals`, `hashCode`, `toString`. Use `@EqualsAndHashCode.Exclude` and `@ToString.Exclude` on every relation field (SpecOnly and runtime) to avoid:

- Lazy loading during `equals`/`hashCode`
- Recursive `toString` triggering collection loads
- `@Data` auto-generating unsafe methods

---

## 8. Fetch Policy

All `@ManyToOne` and `@OneToMany` relations MUST explicitly declare:

```java
fetch = FetchType.LAZY
```

`EAGER` is allowed only in rare, documented cases where the relationship is an unavoidable part of the aggregate.

---

## 9. Usage Rules

### 9.1 SpecOnly fields may ONLY be used in

- `Specification`
- Criteria API
- Repository query construction

```java
root.join(TwinCommentEntity.Fields.createdByUserSpecOnly)
```

### 9.2 SpecOnly fields are forbidden in

- Service layer
- Mappers
- Controllers
- Serializers
- Business logic
- Validation
- DTO conversion

### 9.3 Runtime access must use only the plain field

```java
comment.getCreatedByUser()
```

where the field is loaded via bulk loading (see `load_method_pattern.md`).

---

## 10. Anti-Patterns

- Adding `@ManyToOne` only because an FK exists
- Using `EAGER` by default
- Loading admin-area data through the domain model
- Relying on Hibernate to solve N+1
- Calling `entity.getXxxSpecOnly()` outside query construction
- Declaring `@ManyToOne xxxI18n` to `I18nEntity` for spec purposes — use `*I18nTranslationsSpecOnly` `@OneToMany` instead
- Declaring `*I18nTranslationsSpecOnly` without the `@Access(PROPERTY)` + NOOP getter/setter workaround (see §6.6) — throws `Found shared references to a collection` at flush time
- Calling the removed legacy methods (`joinAndSearchByI18NField`, `doubleJoinAndSearchByI18NField`, `toSortSpecification`) — only `*Direct` variants exist
- Using `@Data` on entities
- Omitting `fetch = LAZY` on a relation
- Declaring a JPA relation (`@ManyToOne`/`@OneToMany`) to `org.cambium.featurer.dao.FeaturerEntity` from a Twins entity — see §3.4

---

## 11. Final Rule

| Scenario                                                | Solution                                                    |
| ------------------------------------------------------- | ----------------------------------------------------------- |
| Relationship participates in core logic                 | `@ManyToOne + @JoinColumn + LAZY` (SpecOnly) + `@Transient` runtime |
| Relationship is needed only for display / admin         | FK id + `@Transient` runtime, bulk-loaded by services       |
| Mass display required                                   | Batch loading in the service                                |
| Sorting / filtering by an i18n field                    | `*I18nTranslationsSpecOnly` `@OneToMany` + `*Direct` spec   |
| Sorting / filtering by a non-i18n relation              | `*SpecOnly` `@ManyToOne`/`@OneToMany` + `Specification`     |

---

## 12. Example — full entity

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

I18n variant (note the `@Access(PROPERTY)` + NOOP getter/setter workaround — see §6.6):

```java
@Column(name = "name_i18n_id")
private UUID nameI18nId;

// Direct join to i18n_translation by raw FK — skips intermediate i18n table.
// HACK: @Access(PROPERTY) + NOOP getter/setter — see §6.6.
@Deprecated // for specification only
@Getter(AccessLevel.NONE)
@Setter(AccessLevel.NONE)
@EqualsAndHashCode.Exclude
@ToString.Exclude
@Access(AccessType.PROPERTY)
@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(
    name = "i18n_id",
    referencedColumnName = "name_i18n_id",
    insertable = false,
    updatable = false
)
private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

public List<I18nTranslationEntity> getNameI18nTranslationsSpecOnly() {
    return null;
}

public void setNameI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
    // NOOP
}
```

For create / update flows, the carrier `I18nEntity` lives in a domain `XxxSave` envelope, not on the entity — see [`docs/domain_save_envelope.md`](domain_save_envelope.md).

---

## 13. ArchUnit Rule (planned)

Access to `*SpecOnly` fields must be forbidden outside:

- repositories
- specifications
- persistence packages

This rule will be enforced via ArchUnit once the codebase is fully migrated.
