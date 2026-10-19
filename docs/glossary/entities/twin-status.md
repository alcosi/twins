---
slug: twin-status
title: TwinStatus
category: core
class: TwinStatusEntity
table: twin_status
is_system: true
actualized_at: 2026-06-17
see_also:
  - twin
  - twin-class
  - twinflow
  - twinflow-transition
---

# TwinStatus

## Summary

A status a Twin can be in within its Twinflow (e.g., a class might define DRAFT, IN_REVIEW, PUBLISHED). One TwinClass declares many statuses; one Twin holds a single current status at any time.

## Purpose

Statuses are the vertices of a [Twinflow](twinflow.md) state machine. Without them, Twins have no notion of state — they exist, but they cannot express "this is still a draft", "this is waiting for approval", or "this is archived". Each TwinClass owns a set of statuses relevant to its domain (an Issue class might define OPEN / IN_PROGRESS / DONE / REJECTED; a Document class might define DRAFT / IN_REVIEW / PUBLISHED).

A Twin's current status (`Twin.twinStatusId`) drives permission checks, UI presentation (icon, color, badge), available [TwinflowTransitions](twinflow-transition.md) (the outgoing edges from this status), and lifecycle policies. Initial status for new Twins is declared on the [Twinflow](twinflow.md) via `initialTwinStatusId`.

The `type` enum (`StatusType`: `BASIC` or `SKETCH`) classifies whether the status is a regular status or a sketch/draft status — used by the platform to drive sketch-mode transitions and UI presentation.

## Fields

- `id` — primary key (UUID v7)
- `twinClassId` — FK to [TwinClass](twin-class.md), owner class
- `key` — unique within class (e.g., `OPEN`, `DRAFT`, `PUBLISHED`)
- `inheritable` — boolean; child TwinClasses inherit this status
- `nameI18nId` — FK to [I18n](i18n.md), display name translations
- `descriptionI18nId` — FK to [I18n](i18n.md), description translations
- `iconLightResourceId` / `iconDarkResourceId` — FK to Resource, UI icons per theme
- `backgroundColor` / `fontColor` — CSS color strings for UI badges
- `type` — enum `StatusType` (`BASIC` / `SKETCH`)

## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [TwinClass](twin-class.md) | many-to-one | owning | Owner class |
| [Twin](twin.md) | one-to-many | link | Twins currently in this status (`twinStatusId`) |
| [Twinflow](twinflow.md) | one-to-many | link | Flows referencing this as initial status (`initialTwinStatusId`) |
| [Twinflow](twinflow.md) | one-to-many | link | Flows referencing this as initial sketch status (`initialSketchTwinStatusId`) |
| [TwinflowTransition](twinflow-transition.md) | one-to-many | link | Transitions where this is the source (`srcTwinStatusId`) |
| [TwinflowTransition](twinflow-transition.md) | one-to-many | link | Transitions where this is the destination (`dstTwinStatusId`) |
| [I18n](i18n.md) | many-to-one | owning | Display name translations |
| [I18n](i18n.md) | many-to-one | owning | Description translations |
| Resource | many-to-one | owning | UI icons (light + dark theme) |

## API

- `POST   /private/twin_class/{twinClassId}/twin_status/v1` — create status attached to a TwinClass (JSON)
- `POST   /private/twin_class/{twinClassId}/twin_status/v2` — create (multipart with uploads)
- `PUT    /private/twin_status/{twinStatusId}/v1` — update status (JSON)
- `PUT    /private/twin_status/{twinStatusId}/v2` — update (multipart)
- `GET    /private/twin_status/{twinStatusId}/v1` — view single status
- `POST   /private/twin_status/search/v1` — search statuses
- `POST   /private/twin_status/search/v2` — search (extended)
- `POST   /private/twin_status/count/v1` — count
- `POST   /private/twin_status/export/sql/v1` — export status as SQL INSERT file
- `POST   /private/twin_status/duplicate/v1` — duplicate a status
- `POST   /private/twin_status/trigger/v1` — create status-level trigger
- `PUT    /private/twin_status/trigger/v1` — update status trigger
- `POST   /private/twin_status/trigger/search/v1` — search status triggers

## Examples

Typical status set for an Issue class:

- `OPEN` (type=BASIC) — newly created issue, awaiting triage
- `IN_PROGRESS` (type=BASIC) — assigned and being worked on
- `DONE` (type=BASIC) — completed
- `DRAFT` (type=SKETCH) — sketch/draft state, separate from the BASIC lifecycle

## Dev notes

- **`nameI18nSpecOnly` / `descriptionI18nSpecOnly`** — deprecated spec-only fields with `@Getter(AccessLevel.NONE)`. Do not call from business code; use the runtime-resolved I18n values via service.
- **Deleting a status** referenced by `Twinflow.initialTwinStatusId` or any `TwinflowTransition.src/dstTwinStatusId` leaves the flow broken. Always check references first.
- **`type` drives sketch-mode behavior** — `SKETCH` statuses are used by `Twinflow.initialSketchTwinStatusId` for draft Twins; routing, UI presentation, and lifecycle policies may differ between BASIC and SKETCH statuses.
