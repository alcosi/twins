# Rules for Describing FK Relationships in JPA Entities

**Status:** Accepted
**Date:** 2026-02-18

## Context

The database contains tables with foreign keys (FK).

When generating or writing JPA entities, a decision must be made on how to represent the related entity:

* through a full ORM relationship (`@ManyToOne`)
* or without an ORM relationship, using only the FK id and auxiliary loading

An incorrect choice leads to:

* excessive JOINs in core queries
* performance degradation
* N+1 problems
* excessive coupling in the domain model
* uncontrolled EAGER loading

It is especially important to distinguish between:

* core logic (client flow)
* admin area (CRUD, filters, display)

---

## Problem

If a table contains an FK, the following question always arises:

> Should the entity contain an ORM relationship, or is it enough to store only the FK id?

JPA encourages the use of `@ManyToOne` by default, which results in automatic JOIN generation.

However, not all relationships participate in business logic.

---

## Options

### Option 1 — ORM Relationship via `@ManyToOne`

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "twin_id")
private Twin twin;
```

Use when:

* the related entity participates in core business logic
* fields of the related object are used during client request processing
* the logic is impossible without the related object
* the object is conceptually part of the aggregate

Consequences:

* Convenient domain logic navigation
* Consistent domain model
  − JOINs are generated even when not always needed
  − Risk of excessive data fetching
  − Harder performance control

---

### Option 2 — Store Only FK + `@Transient`

```java
@Column(name = "twin_id", nullable = false)
private UUID twinId;

@Transient
private Twin twin;
```

Use when:

* the relationship does not participate in business logic
* the data is needed only:

    * in the admin area
    * for display purposes
    * in mappers
* the object is not part of the aggregate
* SQL control is important

Relationship loading is performed:

* through dedicated load methods in services
* using batch queries by id collections
* through `IN (...)`
* without N+1

Consequences:

* No unnecessary JOINs in core queries
* Full SQL control
* Supports batch loading
* Solves the N+1 problem
  − Slightly more code
  − No automatic ORM navigation

---

## Decision

### 1️⃣ Use FK id + `@Transient` by default

Relationships must not be created automatically just because an FK exists in the table.

An ORM relationship should be added only if it truly participates in core business logic.

---

### 2️⃣ `@ManyToOne` + `EAGER` is allowed only if:

* the core logic cannot function without the related object
* the relationship is conceptually part of the aggregate
* the JOIN is logically mandatory

---

### 3️⃣ For the admin area:

* do not use ORM navigation
* use batch loading in services
* mappers must operate on already loaded data

---

## Architectural Principle

ORM is a tool, not a reflection of database structure.

The existence of an FK in a table does not imply the need for `@ManyToOne` in the entity.

The domain model should be built around:

* business logic
* aggregates
* performance

---

## Rationale

The project is focused on:

* SQL control
* minimizing unnecessary JOINs
* predictable queries
* avoiding hidden ORM magic

Experience shows that:

* automatic relationships more often hurt than help
* the admin area must not affect the core model
* batch loading through services scales better

---

## Anti-Patterns

❌ Adding `@ManyToOne` only because an FK exists
❌ Using `EAGER` by default
❌ Loading admin area data through the domain model
❌ Relying on Hibernate to solve N+1 problems

---

## Final Rule

| Scenario                                        | Solution                           |
| ----------------------------------------------- | ---------------------------------- |
| Relationship participates in core logic         | `@ManyToOne + @JoinColumn + EAGER` |
| Relationship is needed only for display / admin | FK id + `@Transient`               |
| Mass display is required                        | Batch loading in the service       |
