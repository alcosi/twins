---
slug: data-list
title: DataList
category: content
class: DataListEntity
table: data_list
is_system: true
actualized_at: 2026-06-17
see_also:
  - data-list-option
  - twin
  - twin-class
---

# DataList

## Summary

A named, ordered list of options used as the source of valid values for tags, markers, and `twin_field_data_list` fields. Each DataList belongs to a Domain and contains a set of [DataListOption](data-list-option.md) entries.

## Purpose

Many places in the platform need a controlled vocabulary: the set of valid tags on a [TwinClass](twin-class.md), the set of markers, the dropdown options for a "priority" field on ISSUE twins, the categories a glossary entry can be tagged with. Hard-coding these in enums is too rigid — operators need to add, rename, and reorder options at runtime through the admin UI. DataLists provide that flexibility.

A DataList itself is just a named container. The actual entries are [DataListOptions](data-list-option.md) — each has a key, optional i18n-translated display name, and four free-form "attribute" slots (`attribute1key` … `attribute4key`) that options can use to carry extra structured data (sort order, color, group, anything). Subsets (`DataListSubset`) let a single DataList expose only a slice of its options to a specific consumer.

When a [TwinClassField](twin-class-field.md) is declared as `data-list` type, it references a DataList; field values on Twins then reference specific DataListOptions from that list. Tags and markers on TwinClass work the same way via `tagDataListId` / `markerDataListId`.

## Fields

- `id` — deterministic; derived from `key + domainId` via `UUID.nameUUIDFromBytes(...)`
- `key` — unique within domain (e.g., `GLOSSARY_CATEGORY`, `ISSUE_PRIORITY`)
- `domainId` — FK to [Domain](domain.md), owner domain
- `nameI18nId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18NId` — FK to [I18n](i18n.md), description translations
- `defaultDataListOptionId` — FK to [DataListOption](data-list-option.md), the default option for new entries
- `createdAt` / `updatedAt` — timestamps
- `attribute1key` / `attribute1nameI18nId` — slot 1: key + i18n name (semantic meaning is per-DataList)
- `attribute2key` / `attribute2nameI18nId` — slot 2
- `attribute3key` / `attribute3nameI18nId` — slot 3
- `attribute4key` / `attribute4nameI18nId` — slot 4
- `externalId` — identifier from external system

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [Domain](domain.md) | many-to-one | owning | Owner domain |
| [DataListOption](data-list-option.md) | one-to-many | own_collection | Options in this list |
| [DataListOption](data-list-option.md) | many-to-one | owning | Default option (`defaultDataListOptionId`) |
| [TwinClassField](twin-class-field.md) | one-to-many | link | Field declarations of type `data-list` that use this list |
| [TwinClass](twin-class.md) | one-to-many | link | Classes using this list for tags (`tagDataListId`) |
| [TwinClass](twin-class.md) | one-to-many | link | Classes using this list for markers (`markerDataListId`) |
| [I18n](i18n.md) | many-to-one | owning | Display name translations |
| [I18n](i18n.md) | many-to-one | owning | Description translations |
| [I18n](i18n.md) | many-to-one | owning | Attribute slot 1 name (×4 attribute slots) |

## API

DataList itself:

- `POST   /private/data_list/v1` — create
- `PUT    /private/data_list/{dataListId}/v1` — update
- `GET    /private/data_list/{dataListId}/v1` — view single
- `GET    /private/data_list_by_key/{dataListKey}/v1` — lookup by key
- `POST   /private/data_list/search/v1` — search

DataListOption (subresource):

- `POST   /private/data_list_option/v1` or `/v2` — create option
- `PUT    /private/data_list_option/{dataListOptionId}/v1` — update
- `PUT    /private/data_list_option/v2` — batch update
- `GET    /private/data_list_option/{dataListOptionId}/v1` — view single
- `POST   /private/data_list_option/search/v1` — search options
- `POST   /private/data_list_option/search/{searchId}/v1` — execute saved search

DataListOptionProjection (subresource, for space-scoped visibility filtering):

- `POST   /private/data_list_option_projection/v1` — create projection
- `PUT    /private/data_list_option_projection/v1` — update
- `POST   /private/data_list_option_projection/search/v1` — search projections

## Examples

A DataList backing the `GLOSSARY_CATEGORY` tag set:

```json
{
  "id": "...",
  "key": "GLOSSARY_CATEGORY",
  "domainId": "...",
  "options": [
    { "key": "core",          "name": "Core" },
    { "key": "workflow",      "name": "Workflow" },
    { "key": "permissions",   "name": "Permissions" }
  ]
}
```

When [TwinClass](twin-class.md) for TWINS_GLOSSARY references this list via `tagDataListId`, every glossary Twin can be tagged with one of these categories.

## Dev notes

- **Deterministic id** — `UUID.nameUUIDFromBytes((key + domainId).getBytes())` in `@PrePersist`. Same key + domain always produces the same id.
- **`options` is `@Transient`** — populated by a `load*` method on the service. Do not access without preceding load.
- **`dataListOptions` is `@Deprecated` spec-only** — used by JPA Criteria queries; never touch in business code.
- **Attribute slots (`attribute1key` … `attribute4key`)** are intentionally generic. Each DataList defines their meaning via the matching `*nameI18nId` (e.g., for ISSUE_PRIORITY, attribute1 might be "sort order"). The platform stores keys; the UI labels them per DataList.
- **`DataListOptionProjectionEntity`** controls which options are visible in which Space — relevant when a DataList is shared across spaces but each space should see only a subset.
