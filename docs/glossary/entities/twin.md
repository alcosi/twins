---
slug: twin
title: Twin
category: core
class: TwinEntity
table: twin
is_system: false
actualized_at: 2026-06-17
see_also:
  - twin-class
  - twinflow
  - twin-status
---

# Twin

## Summary

The central business object — a single instance of a TwinClass. Each Twin carries its own field values, status, tags, markers, links, attachments, and comments.

## Purpose

Twins are the atomic unit of business data in the platform. Whatever the system tracks — a project, a task, a customer record, a glossary entry — is stored as a Twin of an appropriate class. The class defines the schema (fields, statuses, twinflow); the Twin itself holds the instance data.

A Twin is created either through a TwinFactory (the recommended path for business workflows) or directly via the create API. Once created, it can move through its class's twinflow via TwinflowTransitions, accumulate tags and markers for classification, get linked to other Twins for relationship modeling, and host attachments and comments for collaboration.

Twins support hierarchies (parent-child via `headTwinId`) and multi-tenant ownership (organisation-owned via `ownerBusinessAccountId`, user-owned via `ownerUserId`, or both). This makes them flexible enough to represent everything from a top-level organisational unit down to a transient draft a single user is working on.

## Fields

- `id` — primary key (UUID v7)
- `twinClassId` — FK to [TwinClass](twin-class.md), defines this twin's schema
- `headTwinId` — parent twin in hierarchy (optional, null for root)
- `hierarchyTree` — materialized hierarchy path (PostgreSQL ltree)
- `externalId` — identifier from external system
- `ownerBusinessAccountId` — FK to [BusinessAccount](business-account.md), org-owner
- `ownerUserId` — FK to [User](user.md), user-owner
- `viewPermissionId` — FK to [Permission](permission.md), optional per-instance view permission override
- `viewPermissionCustom` — true when `viewPermissionId` overrides the class default
- `permissionSchemaId` — materialized; resolved permission schema for this twin (generated column)
- `permissionSchemaSpaceId` — materialized; space override for permission schema (generated column)
- `twinflowSchemaSpaceId` — materialized; space override for twinflow schema (generated column)
- `twinClassSchemaSpaceId` — materialized; space override for twinclass schema (generated column)
- `aliasSpaceId` — materialized; space override for aliases (generated column)
- `twinStatusId` — FK to [TwinStatus](twin-status.md), current status in twinflow
- `name` — display name
- `description` — long description
- `createdByUserId` — FK to [User](user.md), author
- `assignerUserId` — FK to [User](user.md), assigned operator (optional)
- `createdAt` — creation timestamp
- `pageFaceId` — FK to Face, page-rendering layout
- `breadCrumbsFaceId` — FK to Face, breadcrumbs layout
- `headHierarchyCounterDirectChildren` — materialized count of direct children (generated column)

## Relations

Belongs to a [TwinClass](twin-class.md) (many-to-one, EAGER — the schema is always needed). Holds a current [TwinStatus](twin-status.md) (many-to-one). Owns collections of [TwinLink](twin-link.md) (outgoing via `srcTwinId`, incoming via `dstTwinId`), [TwinTag](twin-tag.md), [TwinMarker](twin-marker.md), [TwinAttachment](twin-attachment.md), [TwinComment](twin-comment.md), [TwinAlias](twin-alias.md), and per-type TwinField values (`twin_field_simple`, `twin_field_boolean`, etc.). Optional parent Twin via `headTwinId` (hierarchy materialized in `hierarchyTree`).

## API

- `POST   /private/twin/v2` — create one or more twins (JSON or multipart with attachments)
- `POST   /private/twin/validate/v1` — validate before create
- `PUT    /private/twin/{twinId}/v1` — update single twin (JSON or multipart)
- `PUT    /private/twin/batch/v1` — batch update (JSON or multipart)
- `PUT    /private/twin/{twinId}/class_change/v1` — change twin's class
- `DELETE /private/twin/{twinId}/v1` — delete single twin
- `DELETE /private/twin/{twinId}/delete_drafted/v1` — delete drafted twin
- `POST   /private/twin/delete/v1` — batch delete by ID list
- `GET    /private/twin/{twinId}/v2` — view single twin
- `GET    /private/twin_by_alias/{twinAlias}/v2` — lookup by alias
- `POST   /private/twin/search/v4` — search with pagination + sorting + lazy relations + show modes
- `POST   /private/twin/search/count/v1` — count with grouping
- `POST   /private/twin/count/v1` — count (lightweight variant)
- `POST   /private/twin/search/{searchId}/v1` — execute saved search
- `POST   /private/twin/search_by_alias/{searchAlias}/v1` — search by alias
- `POST   /private/twin/search_by_alias/count/v1` — count by alias
- `POST   /private/twin/{twinId}/field/{fieldKey}/v2` — set single field value
- `GET    /private/twin/{twinId}/field/{fieldKey}/v1` — get single field value
- `POST   /private/twin/{twinId}/field_list/v1` — set multiple field values
- `POST   /private/twin/{twinId}/permisson_check_overview/v1` — check permissions for actions
- `POST   /private/twin/{twinId}/valid_heads/v1` — list valid head twins for hierarchy
- `POST   /private/twin/{twinId}/link/{linkId}/valid_twins/v1` — list valid twins for linking
- `POST   /private/twin/{twinId}/validate/v1` — validate before update

## Examples

Minimal twin:

```json
{
  "id": "0192a7c4-...",
  "twinClassId": "0192a7c0-...",
  "ownerBusinessAccountId": "0192a7b0-...",
  "createdAt": "2026-06-17T10:15:30Z"
}
```

Twin with hierarchy:

```json
{
  "id": "0192a7c4-...",
  "headTwinId": "0192a7c0-...",
  "hierarchyTree": "0192a7c0.0192a7c4"
}
```

## Dev notes

- **Do not call `viewPermissionSpecOnly`** in business code — it is `@Deprecated`, has `@Getter(AccessLevel.NONE)`, and triggers lazy loading. Use the runtime `viewPermission` field populated by the service.
- **Avoid `entity.getUser().getName()` in loops** — causes N+1. Always go through bulk-loaded `@Transient` runtime fields.
- **`hierarchyTree` is PostgreSQL-specific** (ltree) — H2-based tests cannot exercise this column.
- **Generated columns** (`permissionSchemaId`, `*SchemaSpaceId`, `headHierarchyCounterDirectChildren`) are populated by DB triggers; do not set them in application code. Marked `insertable=false, updatable=false` in JPA.
