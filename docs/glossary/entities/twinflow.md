---
slug: twinflow
title: Twinflow
category: workflow
class: TwinflowEntity
table: twinflow
is_system: true
actualized_at: 2026-06-17
see_also:
  - twin-class
  - twin-status
  - twinflow-transition
  - twinflow-factory
  - eraseflow
---

# Twinflow

## Summary

A state machine attached to a TwinClass — defines which statuses a Twin can be in and the legal transitions between them. One TwinClass has at most one active Twinflow.

## Purpose

A Twinflow is the lifecycle definition for instances of a TwinClass. Without it, Twins of a class exist but have no notion of progress — they cannot move from "draft" to "in review" to "published", cannot gate destructive operations behind approval, and cannot trigger side effects when their state changes. The flow is what turns a static record into a stateful business object.

Concretely, a flow declares the initial status that newly created Twins receive (`initialTwinStatusId`), the full set of statuses available (defined as separate TwinStatus rows on the same TwinClass), and the directed edges between them (TwinflowTransition — each carrying its own permission gate, screen, validators, and optional triggers). When a Twin moves from one status to another, the platform resolves the matching transition, checks permissions, runs validators, executes the state change, and fires any attached TwinflowFactory recipes for side effects (creating related Twins, sending notifications, etc.).

A flow also binds an Eraseflow policy (`eraseflowId`) that defines what happens to related Twins, links, and attachments when a Twin under this flow is deleted — for example, cascading deletion to children, archiving instead of hard-deleting, or blocking deletion if the Twin is in a "protected" status.

## Fields

- `id` — primary key (UUID v7)
- `twinClassId` — FK to [TwinClass](twin-class.md), owner class (one active flow per class)
- `inheritable` — boolean; child TwinClasses inherit transitions and may override individual ones
- `nameI18NId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18NId` — FK to [I18n](i18n.md), description translations
- `createdByUserId` — FK to [User](user.md), author
- `initialTwinStatusId` — FK to [TwinStatus](twin-status.md), assigned to newly created Twins under this flow
- `initialSketchTwinStatusId` — FK to [TwinStatus](twin-status.md), assigned to draft/sketch Twins
- `eraseflowId` — FK to [Eraseflow](eraseflow.md), cascade-delete policy
- `createdAt` — creation timestamp

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [TwinClass](twin-class.md) | many-to-one | owning | Owner TwinClass (one active flow per class) |
| [TwinStatus](twin-status.md) | many-to-one | owning | Initial status for new Twins (`initialTwinStatusId`) |
| [TwinStatus](twin-status.md) | many-to-one | owning | Initial sketch status for drafts (`initialSketchTwinStatusId`) |
| [TwinflowTransition](twinflow-transition.md) | one-to-many | own_collection | Directed edges between statuses |
| [TwinflowFactory](twinflow-factory.md) | one-to-many | own_collection | Factories triggered by lifecycle events |
| [Eraseflow](eraseflow.md) | many-to-one | owning | Cascade-delete policy applied on Twin deletion |
| [I18n](i18n.md) | many-to-one | owning | Name translations |
| [I18n](i18n.md) | many-to-one | owning | Description translations |
| [User](user.md) | many-to-one | owning | Author (`createdByUserId`) |

## API

- `POST   /private/twin_class/{twinClassId}/twinflow/v1` — create flow attached to a TwinClass
- `PUT    /private/twinflow/{twinflowId}/v1` — update flow
- `GET    /private/twinflow/{twinflowId}/v1` — view single flow
- `POST   /private/twinflow/search/v1` — search flows
- `POST   /private/twinflow_schema/search/v1` — search flow schemas
- `POST   /private/twinflow/export/sql/v1` — export flow (with transitions + factories) as SQL INSERT file
- `POST   /private/twinflow/factory/v1` — create factory attached to a flow
- `PUT    /private/twinflow/factory/v1` — update factory
- `GET    /private/twinflow/factory/{twinflowFactoryId}/v1` — view factory
- `POST   /private/twinflow/factory/search/v1` — search factories

## Examples

Typical flow patterns used in production:

- **Issue tracking** — Open → In Progress → Done, with Reopened as a back-edge. Each forward transition requires an `assignerUserId`; the back-edge requires manager permission.
- **Document approval** — Draft → In Review → Approved / Rejected. Approved triggers a factory that publishes the document; Rejected triggers a factory that notifies the author.
- **Lifecycle of a glossary entry** — Draft → Published, where Published is terminal and the eraseflow blocks deletion.

## Dev notes

- **Deleting a status** referenced by `initialTwinStatusId` or by any `TwinflowTransition.srcTwinStatusId` / `dstTwinStatusId` leaves the flow in an inconsistent state. Always check references before removing a status.
- **Switching a class from one Twinflow to another** does not migrate existing Twins — they keep their current status, which may not exist in the new flow. Plan a data migration.
- **Factories attached via TwinflowFactory** fire on transition events. A circular factory (factory A triggers a transition that fires factory A again) will loop forever — validate at design time.
- **`transitionsKit` and `factoriesKit`** are `@Transient` runtime fields populated by `load*` methods. Do not access them without a preceding load call.
