---
slug: twin-class
title: TwinClass
category: core
class: TwinClassEntity
table: twin_class
is_system: true
actualized_at: 2026-06-17
see_also:
  - twin
  - twin-class-field
  - twinflow
  - domain
---

# TwinClass

## Summary

The type (schema) of a Twin — analogous to a class in OOP. Defines which fields, statuses, twinflows, links, factories, and permission schema apply to its instances.

## Purpose

A TwinClass is the metadata entity that gives Twins their structure. Without it, Twins are just rows — the class is what makes them meaningful. Each Twin references exactly one TwinClass via `twinClassId`, and the class in turn owns the full definition of what that Twin can be: which fields it has (via TwinClassField), which statuses it can hold (via TwinStatus), how it can transition between them (via Twinflow), how it can link to other Twins (via Link types), and how new Twins of this class can be produced (via TwinFactory recipes).

TwinClasses can be created through the admin UI or via direct API calls. They live in a Domain and have a `key` that is unique within that domain. The id is deterministic — derived from `key + domainId` — so the same class definition produces the same id across environments, which makes cross-environment alignment painless.

Classes can extend each other via `extendsTwinClassId` (inheritance): a child class inherits field definitions, statuses, and twinflow from the parent, with the option to override individual elements. Abstract classes (`abstractt=true`) cannot be instantiated directly — they serve only as parents.

## Fields

- `id` — deterministic; derived from `key + domainId` via `UUID.nameUUIDFromBytes(...)`
- `domainId` — FK to [Domain](domain.md), owner domain
- `key` — unique within domain (e.g., `TWINS_GLOSSARY`, `PROJECT`, `ISSUE`)
- `twinClassFreezeId` — optional frozen schema version
- `permissionSchemaSpace` — boolean; instances use space-scoped permission schema when true
- `twinflowSchemaSpace` — boolean; instances use space-scoped twinflow schema
- `twinClassSchemaSpace` — boolean; instances use space-scoped twinclass schema
- `aliasSpace` — boolean; aliases are unique per-space instead of per-domain
- `viewPermissionId` — FK to [Permission](permission.md), default view permission for instances
- `createPermissionId` — FK to [Permission](permission.md), permission required to create instances
- `abstractt` — boolean; abstract classes cannot be instantiated directly (intentional double `t` — Java reserved word)
- `nameI18NId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18NId` — FK to [I18n](i18n.md), description translations
- `createdByUserId` — FK to [User](user.md), author
- `createdAt` — creation timestamp
- `iconLightResourceId` / `iconDarkResourceId` — FK to Resource, UI icons
- `headTwinClassId` — FK to TwinClass, parent in class hierarchy
- `extendsTwinClassId` — FK to TwinClass, class being extended (inheritance)
- `headHierarchyTree` / `extendsHierarchyTree` — materialized ltree paths
- `segment` / `hasSegment` — booleans, segment-support flags
- `domainAliasCounter` — int, sequence for alias generation
- `markerDataListId` — FK to [DataList](data-list.md), allowed markers
- `tagDataListId` — FK to [DataList](data-list.md), allowed tags
- `ownerType` — enum `OwnerType` (domain, business_account, user, …)
- `headHunterFeaturerId` / `headHunterParams` — featurer driving head-twin resolution + params (hstore)
- `assigneeRequired` — boolean; instances must have `assignerUserId` set
- `hasDynamicMarkers` — boolean; class uses dynamic markers
- `pageFaceId` / `breadCrumbsFaceId` — FK to Face, UI layouts
- `generalAttachmentRestrictionId` / `commentAttachmentRestrictionId` — FK to restriction entities
- `externalId` — identifier from external system
- `headHierarchyCounterDirectChildren` / `extendsHierarchyCounterDirectChildren` — materialized counts
- `twinCounter` — materialized count of instances of this class

## Relations

Belongs to a [Domain](domain.md). Owns collections of [TwinClassField](twin-class-field.md), [TwinStatus](twin-status.md), [Twinflow](twinflow.md), [Link](link.md), and [TwinFactory](twin-factory.md). References a default view [Permission](permission.md) and create [Permission](permission.md). Optionally extends another TwinClass (inheritance). UI icons resolve through Resource entity; display strings resolve through [I18n](i18n.md).

## API

- `POST   /private/twin_class/v1` — create one or more classes (JSON)
- `POST   /private/twin_class/v2` — create (JSON, extended) or multipart with uploads
- `PUT    /private/twin_class/{twinClassId}/v1` — update single class
- `PUT    /private/twin_class/v2` — batch update (JSON or multipart)
- `GET    /private/twin_class/{twinClassId}/v1` — view single class
- `GET    /private/twin_class_by_key/{twinClassKey}/v1` — lookup by key
- `GET    /private/twin_class/list/v1` — lightweight list (no pagination)
- `POST   /private/twin_class/search/v1` — search classes
- `POST   /private/twin_class/search/v2` — search (extended)
- `POST   /private/twin_class/search/{searchId}/v1` — execute saved search
- `POST   /private/twin_class/count/v1` — count
- `POST   /private/twin_class/export/sql/v1` — export class as SQL INSERT file
- `POST   /private/twin_class/duplicate/v1` — duplicate a class

## Examples

In production, typical TwinClass examples include:

- `PROJECT` — top-level organisational projects owned by a BusinessAccount
- `ISSUE` — work items inside a project, with workflow statuses (Open → In Progress → Done)
- `TWINS_GLOSSARY` — this very glossary, where each Twin documents a domain entity

## Dev notes

- **Deterministic id** — `UUID.nameUUIDFromBytes((key + domainId).getBytes())` in `@PrePersist`. Same key in same domain always produces the same id, so migrations and admin-UI creation are interchangeable.
- **Renaming `key` is breaking** — the deterministic id changes, so existing Twins reference a non-existent class. To rename, create a new class with the new key and migrate Twins in batches.
- **`abstractt`** is intentionally misspelled (Java reserved word) — do not "fix" it; the field stays `abstractt` in the entity, `abstract` in the DB column.
