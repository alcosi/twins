---
slug: twin-class-field
title: TwinClassField
category: core
class: TwinClassFieldEntity
table: twin_class_field
is_system: true
actualized_at: 2026-06-18
see_also:
  - twin-class
  - twin-field
  - permission
  - featurer
---

# TwinClassField

## Summary

A single field definition on a [TwinClass](twin-class.md) — its key, type, validation, storage backend, and featurer-driven behaviour. Defines what data instances of the class carry; values live in the per-type [TwinField](twin-field.md) tables.

## Purpose

Every Twin of a class carries the same set of fields, and `TwinClassField` is the metadata row that declares each one. Without it, Twins would be empty shells — the field definition is what gives a Twin its shape: an ISSUE class has fields like `summary`, `priority`, `assignee`; a PROJECT class has `budget`, `deadline`, `sponsor`. Each of those is a TwinClassField row.

What makes this entity complex is that field behaviour is not hardcoded. Three featurer hooks drive runtime semantics:

1. **`fieldTyperFeaturerId` + `fieldTyperParams`** — determines which [TwinField](twin-field.md) sibling table stores values (`twin_field_simple`, `twin_field_decimal`, `twin_field_user`, …) and how they're serialised. The featurer is the strategy; the params are its configuration.
2. **`fieldInitializerFeaturerId` + `fieldInitializerParams`** — produces the initial value when a Twin is created (e.g., default value, computed value, value copied from template).
3. **`twinSorterFeaturerId` + `twinSorterParams`** — drives sort ordering when Twins are listed with this field as the sort key (e.g., natural sort for strings, custom ordering for enums).

Beyond featurers, a TwinClassField carries: a `required` flag (validation), a `system` flag (platform-managed, not user-editable), optional `viewPermissionId` / `editPermissionId` for field-level permission gating, frontend / backend validation error message templates (`feValidationErrorI18nId` / `beValidationErrorI18nId`), and projection metadata (`projectionField`, `hasProjectedFields`) for derived/computed fields.

The `dependentField` / `hasDependentFields` flags encode field dependency graphs — when one field's value affects another's visibility, validation, or default, the platform uses these flags to know which fields to re-evaluate on each update.

## Fields

- `id` — deterministic; derived from `key + twinClassId` via `UUID.nameUUIDFromBytes(...)`
- `twinClassId` — FK to [TwinClass](twin-class.md), owner class
- `key` — unique within class (e.g., `summary`, `budget`, `assignee`)
- `inheritable` — boolean; child TwinClasses inherit this field
- `nameI18nId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18nId` — FK to [I18n](i18n.md), description translations
- `fieldTyperFeaturerId` + `fieldTyperParams` — featurer selecting the value storage type + hstore params
- `fieldInitializerFeaturerId` + `fieldInitializerParams` — featurer producing initial values + hstore params
- `twinSorterFeaturerId` + `twinSorterParams` — featurer driving sort order + hstore params
- `viewPermissionId` — FK to [Permission](permission.md), permission required to read this field
- `editPermissionId` — FK to [Permission](permission.md), permission required to write this field
- `required` — Boolean (not primitive — update logic relies on three-state semantics)
- `system` — Boolean; system-managed field, not user-editable
- `dependentField` / `hasDependentFields` — Boolean flags for field-dependency graph
- `projectionField` / `hasProjectedFields` — Boolean flags for derived/computed fields
- `order` — Integer; UI display order (column name escaped as `` `order` `` — SQL reserved word)
- `externalId` — identifier from external system
- `externalProperties` — hstore map for external-system metadata
- `feValidationErrorI18nId` — FK to [I18n](i18n.md), frontend validation error template
- `beValidationErrorI18nId` — FK to [I18n](i18n.md), backend validation error template

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [TwinClass](twin-class.md) | many-to-one | owning | Owner class |
| [TwinField](twin-field.md) | one-to-many | link | Field values across all Twins of this class (routed to typed sibling table by `fieldTyperFeaturerId`) |
| Featurer | many-to-one | owning | Field-typer featurer (`fieldTyperFeaturerId`) |
| Featurer | many-to-one | owning | Field-initializer featurer (`fieldInitializerFeaturerId`) |
| Featurer | many-to-one | owning | Twin-sorter featurer (`twinSorterFeaturerId`) |
| [Permission](permission.md) | many-to-one | owning | View permission |
| [Permission](permission.md) | many-to-one | owning | Edit permission |
| [I18n](i18n.md) | many-to-one | owning | Name translations |
| [I18n](i18n.md) | many-to-one | owning | Description translations |
| [I18n](i18n.md) | many-to-one | owning | Frontend validation error template |
| [I18n](i18n.md) | many-to-one | owning | Backend validation error template |
| ProjectionEntity | one-to-many | link | Projections where this field is the source (`srcTwinClassFieldId`) |
| ProjectionEntity | one-to-many | link | Projections where this field is the destination (`dstTwinClassFieldId`) |

## API

TwinClassField endpoints live under the `twinclass/` controller package:

- `POST   /private/twin_class/{twinClassId}/field/v1` — create field on a class
- `PUT    /private/twin_class/field/{fieldId}/v1` — update field
- `POST   /private/twin_class/field/search/v1` — search fields
- `GET    /private/twin_class/field/{fieldId}/v1` — view single field (if exposed; otherwise via class)
- `POST   /private/twin_class/field/duplicate/v1` — duplicate field
- `POST   /private/twin_class/field/rule/v1` — create cross-field rule on this field
- `POST   /private/twin_class/field/rule/search/v1` — search field rules
- `POST   /private/twin_class/field/rule/delete/v1` — delete field rule
- `POST   /private/twin_class/field/condition/v1` — create field condition
- `PUT    /private/twin_class/field/condition/v1` — update condition
- `POST   /private/twin_class/field/condition/search/v1` — search conditions

## Examples

A typical `budget` field declaration on a PROJECT class:

- `key`: `budget`
- `fieldTyperFeaturerId`: decimal typer featurer + params `{ "scale": 2 }`
- `required`: false
- `viewPermissionId`: PROJECT_VIEW
- `editPermissionId`: PROJECT_EDIT_BUDGET
- `feValidationErrorI18nId`: "Budget must be a non-negative number"

Values for this field on every PROJECT Twin live in `twin_field_decimal`.

## Dev notes

- **Three featurer hooks** drive runtime behaviour — `fieldTyper`, `fieldInitializer`, `twinSorter`. Each is a separate Featurer reference + hstore params. Changing the typer featurer after Twins exist requires data migration across `twin_field_*` tables.
- **All `*SpecOnly` fields are deprecated** — `nameI18nSpecOnly`, `descriptionI18nSpecOnly`, `viewPermissionSpecOnly`, `editPermissionSpecOnly`, `fieldTyperFeaturerSpecOnly`, `fieldInitializerFeaturerSpecOnly`, `twinSorterFeaturerSpecOnly`. All have `@Getter(AccessLevel.NONE)`. Never access from business code.
- **`required`, `system` are `Boolean` (not `boolean`)** — intentionally, because the update logic depends on three-state semantics (null / true / false). Do not change to primitive.
- **`order` column is escaped** in JPA as `` `order` `` because `ORDER` is a SQL reserved word. Keep this escaping.
- **`fieldStorage` is `@Transient`** — populated by the typer featurer at runtime. Do not persist it.
- **Hstore columns** (`fieldTyperParams`, `fieldInitializerParams`, `twinSorterParams`, `externalProperties`) use PostgreSQL `hstore` — not portable to H2 tests; mock or skip in unit tests.
- **Deterministic id** — `UUID.nameUUIDFromBytes((key + twinClassId).getBytes())` in `@PrePersist`. Same field key in same class always produces the same id.
