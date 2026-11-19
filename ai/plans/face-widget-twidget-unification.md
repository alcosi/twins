# Face Widget/Twidget Unification — Plan

**Status:** Phase 1 ✅ DONE, Phase 2 ✅ DONE (2026-11-13), Phase 3 ✅ DONE (2026-11-17). Phase 4 dissolved into Phase 3 (decided 2026-11-17: the client ships in the same release, so no legacy alias period is needed).
**Date:** 2026-11-13
**Scope:** Eliminate the historical WT (widget) / TW (twidget) split in the Face subsystem. Target model: a single **widget** concept where `twinId` is mandatory for every face component except the navbar, and every widget is *pointed* (resolves a target twin, defaulting to the current twin).

---

## 1. Background

History confirmed from git/migrations:

| Ticket | Change |
|---|---|
| TWINS-338 (`V1.3.191.01`) | Initial face model. Component types: `NAVBAR`, `PAGE`, `WIDGET`. Everything was a widget (even the image gallery was `WT003`). |
| TWINS-353 (`V1.3.204.01`) | `TWIDGET` component type introduced; twin-bound widgets split off (`WT003→TW001`, `WT004→TW002`). |
| TWINS-429 (`V1.3.297–316`, commit `2edbe338e`) | Mechanics unified: face variants + `twin_pointer` + `twin_pointer_validator_rule` for **both** families; umbrella table `face_twidget` dropped; tables renamed `face_twidget_twXXX→face_twXXX`, `face_widget_wtXXX→face_wtXXX`; `FaceTwidget→FacePointedEntity`; `FaceVariantsService` / `FacePointedService` introduced. |
| TWINS-475 (`V1.3.355.01`, breadcrumbs) | System twin class **`FACE_PAGE`** created (`00000000-0000-0000-0001-000000000005`); `twin.page_face_id` / `twin.bread_crumbs_face_id` added; navbar menu items migrated from `target_page_face_id` (now `@Deprecated`, `FaceNB001MenuItemEntity.java:49`) to `target_twin_id` → a FACE_PAGE twin. |

**Conclusion:** "general pages" already *are* twins (class FACE_PAGE). The frontend always knows the current twin (`FaceNB001MenuItemDTOv1.targetTwinId`). The WT/TW distinction no longer carries semantic weight:

- `FacePointedEntity` = `FaceVariantEntity` + `target_twin_pointer_id` + `faceId` (`FacePointedEntity.java`)
- `FacePointedService.findPointedFace(faceId, twinId)` = `findSingleVariant` + target twin resolution; if `target_twin_pointer_id == null` → target = current twin (`FacePointedService.java:28`)
- WT001 already hard-requires a current twin when `context_id` is configured: `POINTER_ON_NULL` in `FaceWT001Service.loadSearchParams` (`FaceWT001Service.java:78`)

So a TW widget is just a widget with a non-null target pointer; a WT widget is a widget whose target is the current twin.

## 2. Goals / Non-goals

### Goals

- Single term **widget** everywhere — code, API, DB, docs. "Twidget" is eliminated entirely.
- `twinId` is a **required** request parameter for every face component endpoint except NB001.
- Every widget endpoint resolves and returns `pointedTwinId` (target twin; equals `twinId` when no target pointer is configured).
- Uniform service base: all widget services extend `FacePointedService`.
- Continuous widget numbering WT001–WT010 (no gaps).

### Non-goals

- No changes to navbar (NB001) semantics — it stays domain-level and twinless.
- No changes to variant mechanics (`twin_pointer_validator_rule`) — they already work uniformly.
- No frontend implementation — the contract change is coordinated, the client ships in the same release.

## 3. Current state inventory (pre-unification)

| Component | Type | twinId | Entity iface | Service base | Response |
|---|---|---|---|---|---|
| NB001 navbar | NAVBAR | — (absent) | — | `EntitySecureFindServiceImpl` | `FaceNB001DTOv1` |
| PG001/PG002 pages | PAGE | optional | `FaceVariantEntity` | `FaceVariantsService` | `FacePGxxxDTOv1 extends FaceDTOv1` |
| TC001 twin create | TC | optional | `FaceVariantEntity` | `FaceVariantsService` | `FaceTC001DTOv1 extends FaceDTOv1` (has `head_twin_pointer_id`) |
| BC001 breadcrumbs | BC | **required** | `FaceVariantEntity` | `FaceVariantsService` | `FaceBC001DTOv1 extends FaceDTOv1` |
| WT001 table, WT002 create buttons, WT003 messages, WT008 chart | WIDGET | optional | `FaceVariantEntity` | `FaceVariantsService` | `FaceWTxxxDTOv1 extends FaceDTOv1` |
| TW001 gallery, TW002 accordion, TW004 fields, TW005 transitions, TW006 actions, TW007 class change | TWIDGET | **required** | `FacePointedEntity` | `FacePointedService` | `FaceTWxxxDTOv1 extends FaceTwidgetDTOv1` (has `pointedTwinId`) |

## 4. Phase 1 — Mandatory `twinId` (contract) ✅ DONE 2026-11-13

Controllers changed (`@RequestParam(required = false) UUID twinId` → `@RequestParam UUID twinId`):

- `FacePG001Controller.java`, `FacePG002Controller.java`
- `FaceTC001Controller.java`
- `FaceWT001Controller.java`, `FaceWT002Controller.java`, `FaceWT003Controller.java`, `FaceWT008Controller.java`

Already compliant: BC001, TW001–TW007. Untouched: NB001.

Behavioral notes:

- Missing `twinId` now yields HTTP 400 (Spring) instead of silently running twinless.
- `RequestTwinPointers.setCurrentTwinId(null)` path (`NULLIFY_MARKER`) becomes unreachable from widget endpoints; keep it for internal reuse.
- Verify no request sets the current twin twice — `RequestTwinPointers.setCurrentTwinId` throws `UNEXPECTED_SERVER_EXCEPTION` on a second call (`RequestTwinPointers.java:33`). Today each controller calls it exactly once; keep this invariant.

## 5. Phase 2 — Model unification (all widgets are pointed) ✅ DONE 2026-11-13

### 5.1 DB migration (in `db/migration_backlog/` per convention)

Added nullable target pointer to former WT tables (`V1.4.394.06__TWINS-943_face_widget_target_twin_pointer.sql`):

```sql
alter table face_wt001 add column if not exists target_twin_pointer_id uuid
    references twin_pointer on update cascade on delete cascade;
create index if not exists face_wt001_target_twin_pointer_id_idx on face_wt001 (target_twin_pointer_id);
-- same for face_wt002, face_wt003 (released tables)
```

WT008 is still backlog-only, so it does **not** get an ALTER — its creation migration (`V1.4.394.07__TWINS-943_face_wt008_comments_widget.sql`) declares `target_twin_pointer_id` inline in `CREATE TABLE`.

Nullable ⇒ zero data migration; existing rows mean "target = current twin".

### 5.2 Entities

`FaceWT001/2/3/8Entity`: implement `FacePointedEntity` instead of `FaceVariantEntity` (added `targetTwinPointerId` column mapping; `getFaceId()/getFace()` already existed).

### 5.3 Services

`FaceWT001/2/3/8Service`: extend `FacePointedService<T>` instead of `FaceVariantsService<T>`.

WT001 keeps its `loadSearchParams` context logic; with mandatory twinId the `POINTER_ON_NULL` branch is defensive-only.

### 5.4 Controllers

WT001/2/3/8 controllers: `findSingleVariant(faceId, twinId)` → `findPointedFace(faceId, twinId)`, response built from `PointedFace<T>`.

### 5.5 DTOs / mappers

- Renamed `FaceTwidgetDTOv1` → **`FaceWidgetDTOv1`** (keeps `pointedTwinId`; moved to `dto/rest/widget/`).
- `FaceWT001/2/3/8DTOv1`: parent changed `FaceDTOv1` → `FaceWidgetDTOv1`.
- Renamed `FaceTwidgetRestDTOMapper` → `FaceWidgetRestDTOMapper` (`core/mappers/rest/face/`), parameterized as `FaceWidgetRestDTOMapper<E extends FacePointedEntity>` so delegating mappers can pass typed `PointedFace<FaceWTxxxEntity>` collections to `beforeCollectionConversion` (Java generics invariance); `FaceService.loadTwin(Collection)` widened to `Collection<? extends PointedFace<? extends FacePointedEntity>>` accordingly.
- WT mappers accept `PointedFace<FaceWTxxxEntity>` and delegate face+pointedTwinId mapping to the base mapper.
- Deleted dead code: duplicate `dto/rest/twidget/FaceTW007DTOv1.java` + `FaceTW007ViewRsDTOv1.java` (unreferenced).

### 5.6 Pages / TC001 / BC001 — NOT widgets (decided, Q2)

Usage-pattern test — a **widget** is a component placed into page widget slots (`face_pg001_widget.widget_face_id` / `face_pg002_widget.widget_face_id`). Only the WT+TW families qualify:

- **TC001** — modal create-face, referenced from `face_wt001.modal_face_id` / `face_wt002_button.modal_face_id`. Rendered as a dialog, not embedded in a page layout.
- **BC001** — page chrome, referenced from `twin.bread_crumbs_face_id`.
- **PG001/PG002** — page layouts, referenced from `twin.page_face_id`.
- **NB001** — domain navbar (`domain.navbar_face_id`).

TC/BC/PG stay pure `FaceVariantEntity` faces — no `target_twin_pointer_id`, no `pointedTwinId` in DTOs (it would always equal current twin = noise). They share the *variants* mechanism with widgets — that is the correct common denominator. They still got mandatory `twinId` in Phase 1, since they always render in a twin context.

## 6. Phase 3 — Unified numbering & naming, full switch ✅ DONE 2026-11-17

Originally Phase 3 was backend-internal only, with legacy API aliases kept deprecated until a later "Phase 4". On 2026-11-17 decided: **the client is updated in the same release, so no alias/deprecation period is needed** — paths, component ids, schema names, operationIds and mode params all switch immediately. Phase 4 is dissolved.

### 6.1 What changes for the frontend (same-release contract change)

- API paths: `/private/face/tw001/{faceId}/v1` → `/private/face/wt004/{faceId}/v1`, etc. (7 endpoints; ex-TW004 has v1+v2).
- `FaceDTOv1.component`: `'TW001'` → `'WT004'`, etc. (`face_component.id` migrated with data, `face.face_component_id` cascades via `on update cascade`).
- TypeScript types (OpenAPI `@Schema` names): `FaceTW001v1` → `FaceWT004v1`, etc. Quirks fixed along the way: `FaceWT006v1` (was mistakenly on the TW006 actions DTO) → `FaceWT009v1`; `FaceTW0006ActionV1` (4-zero typo) → `FaceWT009ActionV1`.
- operationIds: `faceTW001ViewV1` → `faceWT004ViewV1`, etc.
- Mode query params (derived from enum class names, `MapperModesResolveService.java:111`): `showFaceTW0012TwinClassFieldMode` → `showFaceWT0042TwinClassFieldMode`, etc.; `showFaceTwidget2TwinMode` → `showFaceWidget2TwinMode`.
- `face_component_type` (`WIDGET`/`TWIDGET`) was never exposed — merged to `WIDGET`, no frontend impact.
- New: mandatory `twinId` on PG/TC/WT endpoints (Phase 1) and `pointedTwinId` in all widget DTOs (Phase 2).

### 6.2 Unified WT numbering

Ex-TW widgets filled the free WT004–WT007 gap (numbers dropped back in TWINS-353) and continued after WT008 ⇒ fully continuous WT001–WT010:

| Legacy | New | Widget |
|---|---|---|
| TW001 | **WT004** | image gallery |
| TW002 | **WT005** | i18n field accordion |
| TW004 | **WT006** | field view/edit |
| TW005 | **WT007** | transition buttons |
| TW006 | **WT009** | twin actions |
| TW007 | **WT010** | twin class change |

(WT008 comments was also renumberable — it is still backlog-only — but was left as-is.)

### 6.3 DB migration (`V1.4.394.08__TWINS-943_face_twidget_to_widget_rename.sql`, in backlog)

Data-preserving, idempotent (single `DO` block + updates):

1. `ALTER TABLE ... RENAME` for all 9 tables (`face_tw001`→`face_wt004`, `face_tw002`→`face_wt005`, `face_tw002_accordion_item`→`face_wt005_accordion_item`, `face_tw004`→`face_wt006`, `face_tw005`→`face_wt007`, `face_tw005_button`→`face_wt007_button`, `face_tw006`→`face_wt009`, `face_tw006_action`→`face_wt009_action`, `face_tw007`→`face_wt010`).
2. Child FK column renames (`face_tw002_id`→`face_wt005_id`, `face_tw005_id`→`face_wt007_id`, `face_tw006_id`→`face_wt009_id`).
3. Index + constraint renames via `pg_indexes`/`pg_constraint` loops with `starts_with` + `replace` (PostgreSQL does not rename them together with the table; child names share the parent prefix, so 6 parent mappings cover all).
4. `UPDATE face_component SET face_component_type_id = 'WIDGET' WHERE face_component_type_id = 'TWIDGET'`; the `TWIDGET` type row is kept, marked deprecated in its description (dump compatibility).
5. `UPDATE face_component SET id = 'WT004' WHERE id = 'TW001'` etc. — cascades to `face.face_component_id` (`face_face_component_id_fk ... on update cascade`).
6. Registry gap fix: `INSERT ... ON CONFLICT DO NOTHING` for `('WT003', 'WIDGET', 'Twin messages widget')` — the messages widget (`face_wt003`, since `V1.3.245.02`) never got a `face_component` row (the original WT003 gallery row was renamed to TW001 in `V1.3.204.01`), and the `face.face_component_id` FK makes the row mandatory.

### 6.4 Code-level renames

- All classes/packages: `dao/twidget/` merged into `dao/widget/wtXXX/`; same for `service/`, `mappers/rest/`, `dto/rest/`, `domain/`, `controller/rest/` (66 files, `git mv` + scripted renames).
- DTO class ↔ `@Schema` name ↔ operationId ↔ path ↔ mode enum — all aligned to the new WT names (no legacy kept).
- `TwinMode.FaceTwidget2TwinMode` → `TwinMode.FaceWidget2TwinMode`.
- Glossary/docs: single "widget" definition; "twidget" survives only in this document as history.

## 7. Risks & edge cases

| Risk | Mitigation |
|---|---|
| Frontend breaks on mandatory `twinId`, new paths, new component ids, new schema/operation/mode names | Single coordinated release — client ships together with the backend (decided 2026-11-17). |
| WT001 without `context_id` previously worked twinless | By design now: pass the FACE_PAGE twin id. No code regression — contract change only. |
| Validator-rule variants evaluated without a twin used to silently filter out (`FACE_NO_CONFIG_IS_SUITABLE`) | Becomes impossible — twinId always present; rules evaluate deterministically. |
| Double `setCurrentTwinId` within one request | Asserted impossible by `RequestTwinPointers`; keep one-call-per-controller discipline; covered by tests. |
| `FaceWT001Service.loadSearchParams` `POINTER_ON_NULL` branch | Dead defense; keep the throw (invalid state) — it documents the invariant. |
| Modal faces (`modal_face_id` on WT001/WT002 buttons) opened from widgets | Frontend passes the current twin id to modal face endpoints too — it has it in context. |
| Old data dumps referencing `TWIDGET` type or `TWxxx` component ids | `face_component_type` row `TWIDGET` kept (marked deprecated). Component ids are migrated by `UPDATE`, so restored dumps get rewritten on next migration run. |

## 8. Open questions / decisions log

1. **Q1 — RESOLVED (updated 2026-11-17):** initially: paths stay, marked deprecated, final switch later. Updated: no deprecation period — full switch in one release (paths, component ids, schema names, operationIds, mode params).
2. **Q2 — RESOLVED:** TC/BC/PG are **not** widgets (widget = placed into page widget slots; §5.6). They stay pure `FaceVariantEntity` faces — no `target_twin_pointer_id`, no `pointedTwinId` — but got mandatory `twinId` in Phase 1.
3. **Q3 — RESOLVED:** migrating `face_component_type` rows TWIDGET→WIDGET does **not** break the frontend — the type is never exposed in the API.
4. **Q4 — RESOLVED:** TC001's `head_twin_pointer_id` is sufficient; no `target_twin_pointer_id` for TC001.
5. **Q5 — RESOLVED (2026-11-17):** WT numbering gap WT004–WT007 — filled by ex-TW widgets (continuous WT001–WT010); WT008 comments left as-is.

## 9. Testing

- Extend existing face tests (or add `FaceWidgetPointedTest`) covering: WT004 (ex-TW001) and WT001 with/without `target_twin_pointer_id`; mandatory-twinId 400 responses; variant selection with twin-bound validator rules; `pointedTwinId == twinId` default.
- Integration check on `localhost` profile: fetch navbar → page (FACE_PAGE twin) → WT001 table config end-to-end.

## 10. Execution order (actual)

1. **Phase 1** (contract) ✅ — mandatory `twinId` on PG/TC/WT controllers.
2. **Phase 2** (model) ✅ — backlog migration + entity/service/controller/DTO changes (all widgets pointed).
3. **Phase 3** (renumbering & full switch) ✅ — table/class renames, package merges, component type merge, component id + path + schema/operationId/mode-param switch. Single coordinated release with the client.

### Backlog migration files (run order pinned by the last version segment; user assigns `xxx` at release)

| # | File | Content |
|---|---|---|
| 01 | `featurer_twin_finders` | register featurers 2729–2731 (class/name/description from `@Featurer` annotations) |
| 02 | `face_navbar_nb001_menu_item_order` | navbar menu item sibling ordering (`"order"` column + index, `target_page_face_id` nullable) |
| 03 | `face_widget_target_twin_pointer` | `target_twin_pointer_id` ALTERs for the released `face_wt001/002/003` |
| 04 | `face_wt008_comments_widget` | WT008 component row + `face_wt008` table **with** `target_twin_pointer_id` inline and `twin_comment_search_id` (FK to TWINS-946 `twin_comment_search`, default = UNLIMITED system row; no `show_chart` — the widget renders comments via the configured comment search) |
| 05 | `face_twidget_to_widget_rename` | Phase 3 renames + type merge + component id switch + WT003 registry fix (final switch — runs last) |
