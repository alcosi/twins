# TwinFaces — Core Architecture

> Backend architecture of the Twins Faces UI framework: how face components are registered, configured and
> served to the frontend. For per-component config reference see the
> [Components repository](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418508058/Components+repository) Confluence page.

## 1. Concept: SMIC

TwinFaces follows the **SMIC** principle — *Soft Management of Isolated Components*:

- **Isolated components** — the whole frontend is split into atomic, reusable components (*faces*) of a fixed set of types.
  Each component type is hardcoded on the FE once (layout, design, behaviour) and reused across all domains.
- **Soft management** — the backend (Twins framework) controls only the *content* of a component and the small part of
  the design that must be flexible per domain (labels, columns, buttons, style classes). Roughly: 95% of a component is
  classic FE-managed UI, 5% is config delivered by the backend.

Because domains differ only in configuration (not in code), the same FE build serves every domain: the FE asks the
backend "which face should render this twin/page/navbar?" and then "give me the config of face X in the context of twin Y".

## 2. Registry: component types, components, faces

Three registry tables form the backbone (`core/src/main/java/org/twins/core/dao/face/`):

| Table | Entity | Purpose |
|---|---|---|
| `face_component_type` | `FaceComponentTypeEntity` | Coarse families: `NAVBAR`, `PAGE`, `WIDGET`, `BREADCRUMBS`, `TWIN_CREATE` (plus deprecated `TWIDGET`, merged into `WIDGET` by TWINS-943). |
| `face_component` | `FaceComponentEntity` | Concrete FE component id (varchar(5)), e.g. `NB001`, `PG001`, `WT001`. Each id maps to a hardcoded FE component. This is the contract between BE config and FE code. |
| `face` | `FaceEntity` | A **face instance**: `id`, `domain_id`, `face_component_id`, `name`, `description`, audit fields. All component configs reference a face. |

A *face* is therefore just a named, domain-owned configuration handle of a given component type. The FE never
hardcodes face ids (except deliberate hardcoding escapes, see SMIC) — it receives them from the backend
(see §5).

## 3. Config tables: variants and pointed configs

Each component stores its config in dedicated tables under `org.twins.face.dao.<family>.<component>/`
(e.g. `face_wt001`, `face_pg002_tab`, `face_navbar_nb001_menu_item`). Two interfaces define the semantics
(`core/src/main/java/org/twins/core/dao/face/`):

```java
public interface FaceVariantEntity {                       // "config variant"
    UUID getTwinPointerValidatorRuleId();                  // nullable rule: when this variant applies
}

public interface FacePointedEntity extends FaceVariantEntity {  // "config pointed at a twin"
    UUID getTargetTwinPointerId();                         // nullable pointer: which twin the config is about
    UUID getFaceId();
    FaceEntity getFace();
}
```

- **Variants** — a single face may have several config rows (variants). At request time exactly one variant is
  selected using `twin_pointer_validator_rule_id` (a `TwinPointerValidatorRuleEntity` wrapping a twin validator set,
  evaluated against the *current* twin). A variant with a `null` rule always matches.
- **Pointed configs** — components that display twin data (all WT* widgets) additionally have
  `target_twin_pointer_id`: a `TwinPointerEntity` (featurer-driven pointer, see `org.twins.core.featurer.pointer.Pointer`)
  that resolves *the twin the widget should show data for*. `null` means "the twin the user is currently viewing".
  This is what lets a widget on a PROJECT page show data of its head twin, parent, linked twin, etc.

Child collections of a config (WT001 columns, WT002 buttons, WT007 buttons, PG002 tabs…) are themselves
`FaceVariantEntity` rows, so individual columns/buttons can be shown/hidden per twin via validator rules.

## 4. Config resolution flow

```
FE request: GET /private/face/wt001/{faceId}/v1?twinId=<currentTwinId>
│
├─ FaceVariantsService.findSingleVariant(faceId, twinId)
│   ├─ RequestTwinPointers.setCurrentTwinId(twinId)        // request-scoped bean, caches the twin
│   ├─ getVariants(faceId)                                 // all config rows of the face
│   └─ pick variants where rule == null || rule.isValid(currentTwin)
│       ├─ 0 matches  → FACE_NO_CONFIG_IS_SUITABLE
│       └─ >1 matches → FACE_CONFIG_IS_NOT_UNIQ
│
└─ FacePointedService.findPointedFace(faceId, twinId)      // for pointed components
    ├─ findSingleVariant(...)                              // as above
    └─ resolve target twin:
        ├─ targetTwinPointerId == null → current twin
        └─ else FaceTwinPointerService.getPointer(pointerId) // featurer-resolved, request-cached
            → null → POINTER_ON_NULL
    → PointedFace { config, targetTwinId, targetTwin }
```

Key services (`core/src/main/java/org/twins/core/service/face/`):

| Class | Role |
|---|---|
| `FaceService` | CRUD/security for `face`; `loadFaces(twins)` bulk-resolves page/breadcrumbs faces; `loadTwin(pointedFaces)` bulk-loads target twins. |
| `FaceVariantsService<T>` | `findSingleVariant` / `filterVariants` — validator-rule driven variant selection. |
| `FacePointedService<T>` | Extends variants service with target-twin resolution → `PointedFace`. |
| `FaceTwinPointerService` | Resolves `TwinPointerEntity` against the current twin; caches per request. |
| `FaceTwinPointerValidatorRuleService` | Evaluates validator rules against the current twin; caches per request. |
| `RequestTwinPointers` | `@RequestScope` bean holding the current twin + resolved pointers + rule results for the whole request. |

Every component service extends `FaceVariantsService` or `FacePointedService` and implements only
`getVariants(faceId)` plus bulk child loaders (`loadColumns`, `loadButtons`, … — Kit-based, `findBy...IdIn`,
called from mapper `beforeCollectionConversion` to avoid N+1).

## 5. How the FE learns face ids

| Surface | Source |
|---|---|
| Domain navbar | `domain.navbar_face_id` → exposed as `DomainViewDTOv1.navbarFaceId` (related object `navbarFace`). |
| Twin view page | `twin.page_face_id` → fallback `twin_class.page_face_id` → fallback `twin_class.inherited_page_face_id` (from the extends hierarchy). Exposed as `TwinBaseDTOv1.pageFaceId`. |
| Twin breadcrumbs | same chain via `bread_crumbs_face_id` / `inherited_bread_crumbs_face_id` → `TwinBaseDTOv1.breadCrumbsFaceId`. |
| Page widgets / tabs | page config (PG001/PG002) lists `widget_face_id` per widget slot — each slot points to another face (usually a WT* widget face). |
| Modal forms | WT001/WT002 configs carry `modal_face_id` (e.g. a TC001 twin-create face opened in a modal). |

Resolution logic lives in `FaceService.resolvePageFaceId / resolveBreadCrumbsFaceId` (twin → class → inherited).

## 6. REST API conventions

All face config endpoints live under `/private/face/{component}/{faceId}/v{n}` and require auth
(`@ParametersApiUserHeaders`); read security is domain-scoped (`FaceService.isEntityReadDenied`).

```
GET /private/face/pg001/{faceId}/v1?twinId=...   → FacePG001ViewRsDTOv1   { page,    relatedObjects }
GET /private/face/pg002/{faceId}/v1?twinId=...   → FacePG002ViewRsDTOv1   { page,    relatedObjects }
GET /private/face/nb001/{faceId}/v1              → FaceNB001ViewRsDTOv1   { navbar,  relatedObjects }
GET /private/face/bc001/{faceId}/v1?twinId=...   → FaceBC001ViewRsDTOv1   { breadCrumbs, relatedObjects }
GET /private/face/tc001/{faceId}/v1?twinId=...   → FaceTC001ViewRsDTOv1   { faceTwinCreate, relatedObjects }
GET /private/face/wtNNN/{faceId}/v1?twinId=...   → FaceWTNNNViewRsDTOv1   { widget,  relatedObjects }
```

- `twinId` (query param) = the *current* twin, required by every endpoint that resolves variants/pointers
  (pages, widgets, breadcrumbs, twin-create). Navbar is domain-level and does not need it.
- Widget DTOs extend `FaceWidgetDTOv1` (abstract) → `FaceDTOv1`:
  `id` (the face id), `component` (component id, e.g. `WT001`), `name`, `description`, `createdAt`,
  `createdByUserId`, plus `pointedTwinId` — the resolved target twin (see §3).
- All responses extend `ResponseRelatedObjectsDTOv1`: heavy objects (target twin, twin class fields, modal faces,
  transitions, i18n translations, attachment restrictions) are postponed into `relatedObjects` by mappers.
  Granularity is controlled by mapper modes (`FaceMode` HIDE/SHORT/DETAILED, `TwinMode.FaceWidget2TwinMode`,
  per-component `*2TwinClassFieldMode`, etc.) requested via `MapperContextBinding`.
- i18n: labels are stored as `*_i18n_id` FKs; mappers register them in `I18nCacheHolder` so translations ship in
  `relatedObjects` (WT003 is the exception — it translates directly because it applies `field.value` substitutions).

## 7. Component inventory

Current registry (`face_component` rows + code under `org.twins.face`):

| Component | Type | Purpose | Config table(s) |
|---|---|---|---|
| `NB001` | NAVBAR | Domain navigation bar (user/admin area menus, hierarchical items) | `face_navbar_nb001`, `face_navbar_nb001_menu_item` |
| `PG001` | PAGE | Single-column page: widget slots in declaration order | `face_pg001`, `face_pg001_widget` |
| `PG002` | PAGE | Multi-tab page: tabs → widget slots | `face_pg002`, `face_pg002_tab`, `face_pg002_widget` |
| `BC001` | BREADCRUMBS | Breadcrumb trail for a twin | `face_bc001`, `face_bc001_item` |
| `TC001` | TWIN_CREATE | Twin create form/modal | `face_tc001`, `face_tc001_option` |
| `WT001` | WIDGET | Table of twins of a given class (columns from twin class fields). Rows are fetched by the FE via the configured twin search: `POST /private/twin/search/{searchId}/v1` with `params.twinId = pointedTwinId` plus the config's `searchParams` merged into `params` | `face_wt001`, `face_wt001_column` |
| `WT002` | WIDGET | Create-buttons panel (each button opens a modal face) | `face_wt002`, `face_wt002_button` |
| `WT003` | WIDGET | Static message/banner (i18n title+message with `field.value` substitution) | `face_wt003` |
| `WT004` | WIDGET | Image gallery of a twin (attachments field) | `face_wt004` |
| `WT005` | WIDGET | i18n-field accordion (translations of one field per locale) | `face_wt005`, `face_wt005_accordion_item` |
| `WT006` | WIDGET | Twin field(s) view/edit (search-driven field set, per-field editable flag) — v1 deprecated, v2 multi-field | `face_wt006` |
| `WT007` | WIDGET | Transition buttons (twinflow transitions of the pointed twin) | `face_wt007`, `face_wt007_button` |
| `WT008` | WIDGET | Comments widget (delegates to a configured comment search). Feed: `POST /private/comment/search/{twinCommentSearchId}/v1` with `params.twinId = pointedTwinId`. New-comment input is shown only when the pointed twin's `actions` contain `COMMENT`; add via `POST /private/comment/twin/{pointedTwinId}/v1` | `face_wt008` |
| `WT009` | WIDGET | Twin action buttons (generic actions: edit/delete/comment/…) | `face_wt009`, `face_wt009_action` |
| `WT010` | WIDGET | Twin class change widget (class search + labels) | `face_wt010` |

### TW → WT merge (TWINS-943)

The former TWIDGET family (`TW001` gallery, `TW002` accordion, `TW004` fields, `TW005` transitions, `TW006` actions,
`TW007` class change) was merged into the WIDGET family with data-preserving renames
(`V1.4.394.08__TWINS-943_face_twidget_to_widget_rename.sql`):

| Old id | New id | Old table → new table |
|---|---|---|
| TW001 | WT004 | `face_tw001` → `face_wt004` |
| TW002 | WT005 | `face_tw002` → `face_wt005` (+ `..._accordion_item`) |
| TW004 | WT006 | `face_tw004` → `face_wt006` |
| TW005 | WT007 | `face_tw005` → `face_wt007` (+ `..._button`) |
| TW006 | WT009 | `face_tw006` → `face_wt009` (+ `..._action`) |
| TW007 | WT010 | `face_tw007` → `face_wt010` |

`TWIDGET` component type is kept in DB (marked deprecated) for dump compatibility only. Note the historical quirk:
the original `WT003` gallery row was renamed to `TW001` early on, so the current `WT003` (messages widget) was
(re-)registered only in TWINS-943.

## 8. Adding a new face component (backend checklist)

1. **Register the component**: migration inserting into `face_component` (id pattern `{FAMILY}{NNN}`, e.g. `WT011`)
   with the correct `face_component_type_id`.
2. **Config tables**: `face_{component}` main table (own `id` PK + `face_id` FK → `face`) plus child tables if needed.
   Decide per table:
   - implements `FaceVariantEntity` (`twin_pointer_validator_rule_id`) — if several config variants per face;
   - implements `FacePointedEntity` (+ `target_twin_pointer_id`) — if the config shows data of a (possibly pointed) twin.
   Follow the SQL conventions: inline FKs, index per FK column, `IF NOT EXISTS`, no schema qualifier.
3. **DAO**: entities + repositories (`findByFaceId`, `findBy{Parent}IdIn` for bulk) under `org.twins.face.dao.{family}.{component}`.
4. **Service**: extend `FacePointedService` (or `FaceVariantsService` when not pointed); implement `getVariants`;
   delegate read check to `FaceService.isEntityReadDenied(face)`; add bulk child loaders.
5. **DTO + mapper**: response DTO extending `FaceWidgetDTOv1` (widgets) or plain DTO + `ResponseRelatedObjectsDTOv1`
   envelope; mapper mapping the face wrapper via `FaceRestDTOMapper` / `FaceWidgetRestDTOMapper`; declare
   `*Modes.java` for related objects; register i18n ids via `I18nCacheHolder`.
6. **Controller**: `GET /private/face/{component}/{faceId}/v1` (+ `twinId` param when pointed), `ApiTag.FACE`,
   `MapperContextBinding` with the component mapper root.
7. **Confluence page**: create a child page for the component under the Components repository (see §11) and add
   it to the index table there and to §7 here.

## 9. Error codes

| Code | Meaning |
|---|---|
| `FACE_NO_CONFIG_IS_SUITABLE` | No config variant passed the validator rules for the current twin. |
| `FACE_CONFIG_IS_NOT_UNIQ` | More than one variant matched — ambiguous face config. |
| `POINTER_ON_NULL` | A `target_twin_pointer_id` / context could not be resolved to a twin. |

## 10. Known gaps (as of 2026-11)
- `WT009`: `style_classes` and `ui_type` columns exist but are not exposed in `FaceWT009DTOv1`.
- `WT007`: `FaceWT007DTOv1.searchId` is declared but never populated by the mapper (dead field); the button entity's
  `active` flag is not exposed in the DTO either.
- `WT010`: `FaceWT010DTOv1.twinClassSearchParams` is never populated.
- `TC001`: `FaceTC001OptionRestDTOMapper` does not populate `twinClassSearchParams`, `twinClassFieldsSearchParams`
  and `optionLabel`; `FaceTC001Modes` is declared but not referenced by any mapper; the domain holder
  `FaceTC001Twin` has no consumers.
- `PG002`: `FacePG002TabRestDTOMapper` never maps the tab `icon` (entity has `icon_resource_id`, DTO has the field).
- `PG001` vs `PG002` inconsistency: PG002 widget slots are filtered by `active=true` in SQL, PG001 slots are not
  (the `active` flag is only passed through to the DTO).
- `NB001`: `targetPageFaceId` (direct face pointer) is deprecated in favor of `targetTwinId`; items with status
  `HIDDEN` are loaded and filtered client-side (only `disabled` is derived server-side).
- `TC002` ("Create twin sketch modal widget") is registered in `face_component` but has no backend implementation yet.
- WT003 translations are done per-widget (direct `i18nService.translateToLocale`), not batched — marked
  `//todo think over batch`.

## 11. Component documentation in Confluence

Per-component config references for the FE team live in Confluence under
**[Components repository](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418508069)** (page id
`2418508069`, inside the space section [2418508058](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418508058)).
**Each component has its own child page** — when a component's config API, DTO or FE contract changes, update
its page (and the parent index when a component is added/renamed).

Editorial conventions for these pages (established with Nikita):

- page **content is in Russian**; page titles stay in English (`WT001 — twins table`, …);
- each page documents: component id + config table(s), the config endpoint, every DTO field (table), a response
  example, and FE integration notes (e.g. which search/comment API the widget calls and how `pointedTwinId` is
  passed via search `params`);
- DTO inheritance chains and mapper-mode internals are intentionally omitted — the FE reads swagger.

| Component | Confluence page id | Link |
|---|---|---|
| Index / shared concepts (parent) | `2418508069` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418508069) |
| NB001 | `2418802947` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418802947) |
| PG001 | `2417950956` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2417950956) |
| PG002 | `2418409786` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418409786) |
| BC001 | `2418934016` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418934016) |
| TC001 | `2419425376` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419425376) |
| WT001 | `2419359932` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419359932) |
| WT002 | `2419327092` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419327092) |
| WT003 | `2419228817` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419228817) |
| WT004 | `2417819939` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2417819939) |
| WT005 | `2419097727` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419097727) |
| WT006 | `2418639154` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2418639154) |
| WT007 | `2419196039` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419196039) |
| WT008 | `2419130523` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419130523) |
| WT009 | `2419458161` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419458161) |
| WT010 | `2419523659` | [link](https://alcosi.atlassian.net/wiki/spaces/Twins/pages/2419523659) |

