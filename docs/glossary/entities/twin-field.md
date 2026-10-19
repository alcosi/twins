---
slug: twin-field
title: TwinField (typed value tables)
category: fields
class: TwinFieldSimpleEntity (and siblings)
table: twin_field_simple (and siblings — see below)
is_system: true
actualized_at: 2026-06-17
see_also:
  - twin
  - twin-class
  - twin-class-field
---

# TwinField (typed value tables)

## Summary

Per-type storage tables for Twin field values. Each TwinClassField declaration has a type (string, boolean, decimal, timestamp, user, twin-class, data-list option, i18n); values for that field live in the matching `twin_field_*` table.

## Purpose

Twins need to carry arbitrary typed data per their class — a PROJECT twin might have a `budget` decimal field, an ISSUE twin might have an `assignee` user-reference field, a DOCUMENT twin might have a `body` i18n-text field. Rather than jamming every value into a single JSON blob or EAV table, the platform splits storage by type: each type gets its own table with a typed `value` column, allowing PostgreSQL to index and validate values natively.

This is the "vertical partitioning by type" pattern. A [TwinClassField](twin-class-field.md) declaration determines which table its values live in (via its featurer-driven `fieldType`). When a Twin's field is written, the service routes the value to the correct `twin_field_*` table; when read back, the value comes back already typed.

The split keeps indexes meaningful: a btree index on `twin_field_decimal.value` is a numeric index, not a string-cast index. Queries for "all Twins where budget > 1000" hit a real numeric range scan.

## Fields

Listed for `TwinFieldSimpleEntity`; all sibling tables share the same shape (`id` + `twinId` + `twinClassFieldId`) — only the `value` column type differs (see Dev notes for the full list).

- `id` — primary key (UUID v7)
- `twinId` — FK to [Twin](twin.md), the owning twin
- `twinClassFieldId` — FK to [TwinClassField](twin-class-field.md), the field definition
- `value` — the typed value (column type varies per sibling table — see below)

All sibling tables share the same `id` + `twinId` + `twinClassFieldId` shape; only the `value` column type differs.

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [Twin](twin.md) | many-to-one | owning | Owning twin |
| [TwinClassField](twin-class-field.md) | many-to-one | owning | Field definition (determines which sibling table is used) |

## API

TwinField values are not exposed via a separate top-level resource. Read and write goes through the [Twin](twin.md) field endpoints:

- `GET    /private/twin/{twinId}/field/{fieldKey}/v1` — read single field value
- `POST   /private/twin/{twinId}/field/{fieldKey}/v2` — write single field value
- `POST   /private/twin/{twinId}/field_list/v1` — write multiple field values

The `fieldKey` path parameter is the [TwinClassField](twin-class-field.md) `key`; the platform resolves it to the correct TwinField table.

## Examples

Writing a budget field on a PROJECT twin (decimal):

```json
POST /private/twin/{twinId}/field/budget/v2
{ "value": 125000.50 }
```

The service routes the value to `twin_field_decimal` because the `budget` field is declared as `decimal` on the PROJECT TwinClass.

Writing an assignee field (user reference):

```json
POST /private/twin/{twinId}/field/assignee/v2
{ "value": "0192a7c4-..." }
```

Routed to `twin_field_user`.

## Dev notes

- **Never write to `twin_field_*` tables directly** — always go through the field-write API so the type-routing, validation, and indexing are applied correctly.
- **Sibling tables (one per value type)** — all share the same shape (`id` + `twinId` + `twinClassFieldId`); only the `value` column type differs. Routing is decided by `fieldTyperFeaturerId` on [TwinClassField](twin-class-field.md).

  | Table | Entity | Value column type | Used for |
  |---|---|---|---|
  | `twin_field_simple` | `TwinFieldSimpleEntity` | VARCHAR | Short strings (names, slugs, codes) |
  | `twin_field_simple_non_indexed` | `TwinFieldSimpleNonIndexedEntity` | VARCHAR | Long strings where index not needed |
  | `twin_field_boolean` | `TwinFieldBooleanEntity` | BOOLEAN | True/false flags |
  | `twin_field_decimal` | `TwinFieldDecimalEntity` | NUMERIC | Money, quantities, measurements |
  | `twin_field_timestamp` | `TwinFieldTimestampEntity` | TIMESTAMP | Dates and date-times |
  | `twin_field_user` | `TwinFieldUserEntity` | UUID (FK to user) | User references |
  | `twin_field_twin_class` | `TwinFieldTwinClassEntity` | UUID (FK to twin_class) | TwinClass references |
  | `twin_field_data_list` | `TwinFieldDataListEntity` | UUID (FK to data_list_option) | DataList option selections |
  | `twin_field_i18n` | `TwinFieldI18nEntity` | UUID (FK to i18n) | Translatable text |

  All entities live in `org.twins.core.dao.twin`.
- **The `twinClassField` field on each TwinField entity is loaded eagerly** (no `fetch = LAZY` annotation, defaults to EAGER for `@ManyToOne`). This is intentional — the field definition is almost always needed alongside the value.
- **Each TwinField entity has a `cloneFor(TwinEntity)` method** used during Twin duplication. If you add a new TwinField type, you must implement `cloneFor` for it.
- **`twin_field_attribute` is special** — it stores per-field metadata (e.g., "this field was set by system, not user"), not a typed value. Modeled with `KitGrouped` for multi-attribute storage on the same twin+field pair.
