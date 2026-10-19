---
slug: twin-link
title: TwinLink
category: core
class: TwinLinkEntity
table: twin_link
is_system: false
actualized_at: 2026-06-18
see_also:
  - twin
  - twin-class
  - link
---

# TwinLink

## Summary

An instance of a typed relationship between two Twins. Each TwinLink row points at a source Twin, a destination Twin, and a [Link](link.md) type that declares the semantics of the connection. One Twin has many outgoing links (via `srcTwinId`) and many incoming links (via `dstTwinId`).

## Purpose

Twins on their own are isolated nodes — they hold data but don't express how they relate to each other. TwinLink is the edge that connects them: an ISSUE twin might link to the PROJECT twin it belongs to, a DOCUMENT twin might link to its parent folder, a USER twin might link to the DEPARTMENT twin they work in.

Each link is typed via a [Link](link.md) definition (declared at the TwinClass level): `Issue.belongsToProject`, `Document.childOf`, `User.memberOf`. The Link type carries its own permission gates, triggers, and validators; the TwinLink instance just carries the specific src+dst pair and bookkeeping (creator, timestamp).

Links are directional — `srcTwinId` → `dstTwinId`. To model a bidirectional relationship, declare two Link types (forward + backward) or use a single symmetric Link type and create two TwinLink rows. Triggers and validators on the Link type fire on TwinLink creation / deletion, enabling cascading side-effects (e.g., "when an ISSUE is linked to a PROJECT, increment the PROJECT's `issueCount` field").

TwinLinks are the basis for graph queries: "find all Twins linked to this one via `memberOf`", "count outgoing `dependsOn` links", "traverse the link graph three hops out". The platform stores links in a flat table and relies on indexes (`src_twin_id`, `dst_twin_id`, `link_id`) to make these queries fast.

## Fields

- `id` — primary key (UUID v7)
- `srcTwinId` — FK to [Twin](twin.md), source twin
- `dstTwinId` — FK to [Twin](twin.md), destination twin
- `linkId` — FK to [Link](link.md), type definition (semantics, permission gates, triggers)
- `createdByUserId` — FK to [User](user.md), author
- `createdAt` — creation timestamp

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [Twin](twin.md) | many-to-one | owning | Source twin (`srcTwinId`) |
| [Twin](twin.md) | many-to-one | owning | Destination twin (`dstTwinId`) |
| [Link](link.md) | many-to-one | owning | Type definition (semantics, gates, triggers) |
| [User](user.md) | many-to-one | owning | Author (`createdByUserId`) |
| LinkTrigger | one-to-many | link | Side-effect triggers attached to the Link type |
| LinkValidator | one-to-many | link | Validators attached to the Link type |

## API

TwinLink endpoints (instance management):

- `POST   /private/twin/{twinId}/link/v1` — add a link from this Twin to another
- `POST   /private/twin/{twinId}/link/{linkId}/valid_twins/v1` — list valid destination Twins for a given link type

TwinLink instance lookup is currently performed via the Twin search endpoints with link filters (see [Twin](twin.md) API). There is no standalone `GET /private/twin_link/{id}` endpoint.

Link type management (the type definitions):

- `POST   /private/link/v1` — create link type
- `PUT    /private/link/{linkId}/v1` — update link type
- `GET    /private/link/{linkId}/v1` — view single link type
- `POST   /private/link/search/v1` — search link types
- `GET    /private/twin_class/{twinClassId}/link/v1` — list link types declared for a class

## Examples

Linking an ISSUE twin to its PROJECT twin:

```json
POST /private/twin/{issueTwinId}/link/v1
{
  "dstTwinId": "0192a7c0-...",   // PROJECT twin
  "linkId": "0192a700-..."        // Link type "Issue.belongsToProject"
}
```

After creation, the platform resolves the Link type, runs its validators (e.g., "dst Twin must be of class PROJECT"), checks permissions on the Link type, persists the TwinLink row, and fires any triggers (e.g., increment PROJECT.issueCount).

## Dev notes

- **Links are directional** — `srcTwinId` → `dstTwinId`. The reverse direction requires either a separate TwinLink row or a symmetric Link type. UI and API must be explicit about which direction is being queried.
- **Link type gates the operation** — creating a TwinLink runs the Link type's validators and triggers. Always go through the API; never insert into `twin_link` directly.
- **`@OneToMany` collections on TwinEntity** (`linksBySrcTwinIdSpecOnly`, `linksByDstTwinIdSpecOnly`) are `@Deprecated` spec-only — never access in business code. Use the link-service lookups instead.
- **Cascading deletes depend on Eraseflow** — when a Twin is deleted, the Eraseflow policy on its Twinflow decides whether incoming / outgoing TwinLinks are deleted, archived, or block the deletion.
- **Graph traversal is index-driven** — `src_twin_id`, `dst_twin_id`, and `link_id` columns are indexed. For deep graph queries (>3 hops), consider materialised projections (`ProjectionEntity`) rather than recursive SQL.
- **`PublicCloneable<TwinLinkEntity>`** — the entity implements `cloneFor(TwinEntity)` for Twin duplication. When duplicating a Twin, outgoing links are typically cloned to point at the new Twin's destinations.
