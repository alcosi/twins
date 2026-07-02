---
slug: twinflow-transition
title: TwinflowTransition
category: workflow
class: TwinflowTransitionEntity
table: twinflow_transition
is_system: true
actualized_at: 2026-06-17
see_also:
  - twinflow
  - twin-status
  - twin-factory
  - permission
---

# TwinflowTransition

## Summary

A directed edge in a [Twinflow](twinflow.md) state machine — moves a [Twin](twin.md) from a source [TwinStatus](twin-status.md) to a destination status. Carries a permission gate, optional validators, optional triggers, and optional factories that fire on execution.

## Purpose

A transition is the only legal way to move a Twin between statuses. Without transitions, Twins would be stuck in whatever status they were created with. Transitions encode the business rules of state movement: "you can go from OPEN to IN_PROGRESS only if you have `ISSUE_ASSIGN` permission", "going from IN_PROGRESS to DONE triggers a notification factory", "going from DRAFT to PUBLISHED runs validators that check required fields".

When a transition is performed (via the `/transition/{id}/perform/v2` endpoint), the platform runs a fixed sequence: permission check → validator execution → status update on the Twin → trigger execution → factory invocation. Any failure in validators aborts the whole transition; triggers and factories are side effects that run after the state change is committed.

The transition's `twinflowTransitionTypeId` enum (`TwinflowTransitionType`: `OPERATION`, `OPERATION_DISABLE`, `STATUS_CHANGE`, `STATUS_CHANGE_MARKETING`, `MARKETING`) classifies what kind of operation it represents — used by the platform for default behavior such as audit log routing and lifecycle grouping.

## Fields

- `id` — primary key (UUID v7)
- `twinflowId` — FK to [Twinflow](twinflow.md), owner flow
- `srcTwinStatusId` — FK to [TwinStatus](twin-status.md), source status (null = "from any status")
- `dstTwinStatusId` — FK to [TwinStatus](twin-status.md), destination status (required)
- `nameI18nId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18nId` — FK to [I18n](i18n.md), description translations
- `screenId` — FK to Screen, optional UI screen to render for user-driven transitions
- `permissionId` — FK to [Permission](permission.md), required permission to execute
- `twinflowTransitionTypeId` — enum `TwinflowTransitionType` (`OPERATION` / `OPERATION_DISABLE` / `STATUS_CHANGE` / `STATUS_CHANGE_MARKETING` / `MARKETING`)
- `createdByUserId` — FK to [User](user.md), author
- `createdAt` — creation timestamp
- `inbuiltTwinFactoryId` — FK to [TwinFactory](twin-factory.md), factory executed as part of this transition
- `draftingTwinFactoryId` — FK to [TwinFactory](twin-factory.md), factory used when drafting (sketch mode)
- `twinflowTransitionAliasId` — FK to `TwinflowTransitionAliasEntity`, optional human-readable alias
- `allowComment` / `allowAttachment` / `allowLinks` — booleans, **marked `//todo delete it` in source** — legacy permission flags scheduled for removal

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [Twinflow](twinflow.md) | many-to-one | owning | Owner flow |
| [TwinStatus](twin-status.md) | many-to-one | owning | Source status — null means "from any" |
| [TwinStatus](twin-status.md) | many-to-one | owning | Destination status (required) |
| [Permission](permission.md) | many-to-one | owning | Required permission to execute |
| [TwinFactory](twin-factory.md) | many-to-one | owning | Inbuilt factory executed as part of this transition |
| [TwinFactory](twin-factory.md) | many-to-one | owning | Drafting factory (sketch mode) |
| TwinflowTransitionAlias | many-to-one | owning | Human-readable alias for client lookup |
| Screen | many-to-one | owning | Optional UI screen to render |
| [I18n](i18n.md) | many-to-one | owning | Display name translations |
| [I18n](i18n.md) | many-to-one | owning | Description translations |
| [User](user.md) | many-to-one | owning | Author |
| ValidatorRule | one-to-many | transient_runtime | Validation rules (loaded as kit at runtime) |
| Trigger | one-to-many | transient_runtime | Side-effect triggers (loaded as kit at runtime) |

## API

CRUD on transition definitions:

- `POST   /private/transition/v1` — create transition
- `PUT    /private/transition/v1` — update transition
- `GET    /private/transition/{transitionId}/v1` — view single transition
- `POST   /private/transition/search/v1` — search transitions
- `POST   /private/transition/export/sql/v1` — export as SQL INSERT file

Executing transitions (the "perform" endpoints):

- `POST   /private/transition/{transitionId}/perform/v2` — perform on a Twin (JSON or multipart)
- `POST   /private/transition/{transitionId}/draft/v1` — perform in draft/sketch mode
- `POST   /private/transition_by_alias/{transitionAlias}/perform/v2` — perform by alias (JSON or multipart)
- `POST   /private/transition_by_alias/{transitionAlias}/draft/v1` — draft by alias

Triggers (side effects fired on transition):

- `POST   /private/transition_trigger/v1` — create trigger
- `PUT    /private/transition_trigger/v1` — update trigger
- `POST   /private/transition_trigger/search/v1` — search triggers

Transition aliases:

- `POST   /private/transition_alias/search/v1` — search aliases

## Examples

A typical OPEN → IN_PROGRESS transition setup:

- `srcTwinStatusId`: OPEN
- `dstTwinStatusId`: IN_PROGRESS
- `permissionId`: ISSUE_ASSIGN
- `twinflowTransitionTypeId`: STATUS_CHANGE
- `inbuiltTwinFactoryId`: factory that sets `assignerUserId` to the current user

When a client calls `POST /private/transition/{id}/perform/v2` with the Twin ID, the platform checks ISSUE_ASSIGN permission, runs validators (e.g., "summary field is non-empty"), updates the Twin's `twinStatusId` to IN_PROGRESS, fires triggers (notifications), and runs the inbuilt factory (auto-assignment).

## Dev notes

- **`allowComment` / `allowAttachment` / `allowLinks`** are marked `//todo delete it` in source — legacy flags that pre-date the permission system. Do not use them in new code; route through `permissionId` and validators instead.
- **`validatorRulesKit` and `triggersKit`** are `@Transient` runtime fields populated by `load*` methods. Access them only after a successful load.
- **`srcTwinStatusId` is nullable** — null means "transition can be performed from any source status". Use this for universal transitions like "reset to DRAFT".
- **`dstTwinStatusId` is required (non-null)** — every transition must end somewhere. There is no "transition to nowhere".
- **Factory execution order** during perform: validators first (can abort), then status update, then triggers, then inbuilt factory. Drafting factory is used only in draft mode.
- **Circular transitions** (A → B → A → …) are not blocked by the platform — design factories carefully to avoid infinite loops.
