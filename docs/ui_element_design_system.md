 нем# TwinFaces UI Element Design System (JIRA-based)

> **TL;DR** — TWINFACES is a *meta-UI design system* modelled in JIRA instead of Figma.
> Every atomic piece of the admin UI is a **`UI element`** issue in project `TWINFACES`.
> Elements are composed into screens through two custom issue links:
> **`UI element`** (composition) and **`UI pointer`** (navigation).
> This document is the convention for adding new UI elements consistently and for
> tracing where any element is used on the front end.

---

## 1. How the design system is modelled

| Layer | JIRA object | Example |
|---|---|---|
| UI **type** (the catalog of primitives) | `Epic` with summary `UI type: <name>` | `TWINFACES-136` *UI type: page* |
| Concrete UI **element** (instance) | issue of type **`UI element`** | `TWINFACES-1052` *`[modal] factory trigger duplicate`* |
| Composition / nesting | link **`UI element`** (`uses` / `is used by`) | *page* `uses UI element` *table* |
| Navigation / reference | link **`UI pointer`** (`points to` / `pointed from`) | *menu item* `points to` *page* |
| Backend binding | fields in description (`API endpoint`, permission) | `/private/factory_trigger/duplicate/v1` |

* Project key: **`TWINFACES`**
* Issue type name: **`UI element`** (id `11063`)
* Every `UI element` MUST have a **parent Epic** = its `UI type: <name>`.

---

## 2. UI element type registry

These are the canonical types (each is an Epic `UI type: <name>`). When you create a
new element, pick **exactly one** as the parent.

### Layout primitives (`face*` — page chrome / shell)

| Type | Epic | Notes |
|---|---|---|
| `[face] LayoutFaces` (a.k.a `<PGXXX />`) | `TWINFACES-516` | Top-level layout-face contract |
| LayoutFace | `TWINFACES-562` | |
| face navbar | `TWINFACES-515` | |
| face breadcrumbs | `TWINFACES-616` | |
| face widget | `TWINFACES-523` | |
| face twiget | `TWINFACES-501` | |

### Screens & containers

| Type | Epic | Notes |
|---|---|---|
| page | `TWINFACES-136` | A full admin page |
| tab | `TWINFACES-138` | A tab inside a page (often encoded in the page summary) |
| forms (a.k.a modals) | `TWINFACES-212` | Dialogs / modals |
| table | `TWINFACES-137` | Data table |
| filter | `TWINFACES-139` | Generic filter; **`table filter`** is the common concrete form |
| card | `TWINFACES-245` | |
| cards list | `TWINFACES-248` | |
| tree view | `TWINFACES-619` | Includes the **`chain`** visual variant (e.g. hierarchy chains) |

### Navigation & actions

| Type | Epic | Notes |
|---|---|---|
| menu | `TWINFACES-361` | A menu container |
| menu item | `TWINFACES-340` | One entry in a menu; usually `points to` a page |
| action menu | `TWINFACES-909` | Row/object actions (Duplicate / Export / Delete …) |

### Atoms (the most reused building blocks)

| Type | Epic | Notes |
|---|---|---|
| input field | `TWINFACES-140` | Text, checkbox, multi-select, uuids, etc. — reused dozens of times |
| smart link | `TWINFACES-57` | Inline link to another entity with a tooltip |

> **Variants observed in summaries** (no separate Epic — they are concrete shapes):
> `[chain]` → child of **tree view**; `[table filter]` → concrete **filter**; `[modals]` → typo of `[modal]`, avoid.

---

## 3. Naming convention

Summary **always** starts with the type tag in square brackets, then the domain
entity, optionally an operation and/or a nested `[tab]`.

```
[<type>] <domain entity> [tab] <section> [(single|multi)] | <operation>
```

| Summary | Decodes as |
|---|---|
| `[modal] factory trigger duplicate` | modal · domain `factory` · entity `trigger` · op `duplicate` |
| `[action menu] factory trigger` | action menu for the `factory trigger` entity |
| `[page] domain business account [tab] twins` | page `domain business account`, tab `twins` |
| `[table] domain business account users` | table listing `domain business account users` |
| `[table filter] domain business account user` | filter for that table |
| `[input field] uuids (multi)` | multi-value uuid input |
| `[input field] class field rule (single)` | single-value select |
| `[smart link] domain business account` | smart link to a DBA |
| `[menu item] factory triggers` | one menu entry |
| `[chain] class head hierarchy` | hierarchy chain (tree view variant) |
| `[page] twin pointer list` | **list** page — a table of all twin pointers |
| `[page] twin pointer` | **view / edit** page of one twin pointer (holds tabs) |
| `[page] twin pointer [tab] general` | a **tab** of that view page (parent `TWINFACES-138`) |

Rules:
* One leading `[type]` only — matching the parent Epic.
* A **tab is its own UI element** (parent Epic `UI type: tab` = `TWINFACES-138`),
  even though its summary begins with `[page]`. The `[page]` prefix names the page
  that owns the tab — e.g. `[page] class [tab] general` (`TWINFACES-163`, parent
  `TWINFACES-138`). **Create the tab before its page** (bottom-up, §7).
* A **list** page is `[page] <entity> list`; a **view / edit** page of one object
  is `[page] <entity>` (no `list`). The view page holds tabs; editable fields live
  on the tabs, not on the page.
* CRUD families are symmetric: for a given entity create the whole set together
  (see §7).
* Lowercase type tag; keep the same wording for the entity across the family
  (`factory trigger`, `factory pipeline`, …).

---

## 4. Link types — the reuse graph (most important)

Two **custom** link types form the graph. Everything else (`Relates`, `Blocks`,
`Cloners`, `Duplicate`) is standard JIRA and is used incidentally.

### 4.1 `UI element` — composition / containment

> **`A` `uses UI element` `B`** ⟺ `A` is a **part that is rendered inside** `B`.
> The link is created **from the part outward to its container.**
> Reverse reading on `B` (the container): **`is used by UI element`** `A`.

Direction rule: **the PART / ATOM links outward to its CONTAINER.**
Open the **part** (e.g. an `[input field]`) and read its **outward** `uses UI element`
links — each one is a container (modal / table / filter / …) that renders it. That
is the “where is this input used?” answer.

> The verb reads as “part *uses* container”, which feels backwards, but this is the
> established direction in the project. Verified: `[input field] text field`
> (`TWINFACES-276`) carries 41 outward `uses` links into its containers (modals,
> tables, …). Keep this direction — it is what makes every atom self-describe all
> of its usages.

| Part (outward `uses UI element`) | Container (inward `is used by`) | Example |
|---|---|---|
| table, card, chain | page / tab | `[chain] class head hierarchy` uses `[page] class [tab] general` |
| input field / smart link (per column), table filter | table | `[smart link] domain business account` uses `[table] … users`; `[table filter] …` uses `[table] …` |
| modal (one per action) | action menu | `[modal] factory trigger duplicate` uses `[action menu] factory trigger` |
| menu item | menu | `[menu item] factory triggers` uses `[menu] triggers` |
| input field (one per field) | modal | the `twin factory` input uses `[modal] factory trigger duplicate` |
| card | cards list | |

### 4.2 `UI pointer` — navigation / reference

> **`A` `points to` `B`** ⟺ `A` is the **destination** that `B` (a clickable
> source) navigates to. The link is created **from the destination outward to its
> source.**
> Reverse reading on `B` (the source): **`pointed from`** `A`.

Direction rule: **the DESTINATION (page / entity view) links outward to its
SOURCE** (menu item / smart link / table row). Open a page and read its outward
`points to` links to see every element that navigates into it.

| Destination (outward `points to`) | Source (inward `pointed from`) | Example |
|---|---|---|
| page | menu item | `[page] factory trigger list` `points to` `[menu item] factory triggers` |
| page / entity view | smart link | the DBA edit page `points to` `[smart link] domain business account` |
| edit page | table row (table) | (per-row navigation) |

> Verified: `[menu item] factory triggers` (`TWINFACES-863`) is the inward
> (`pointed from`) side of a `UI pointer` whose outward issue is `[page] factory
> trigger list` (`TWINFACES-873`).

> **Destination = the actual target of the click.** The outward side must be the
> page the user really lands on — not just any related page. Concretely:
> * a `[menu item]` that opens a list points *from* the **list page**;
> * a `[table]` row — clicking a row — points *from* the **view / edit page** of
>   that one object, **never** from the list page;
> * a `[smart link]` to a concrete entity points *from* that entity's **view / edit
>   page**, not from the list page.
>
> If a CRUD set has no separate view/edit page yet, **create it first**, then wire
> the `UI pointer`. Never point a table row or a smart link at the list page.

| Destination (outward `points to`) | Source (inward `pointed from`) | Correct? |
|---|---|---|
| `[page] twin pointer list` | `[smart link] twin pointer` | ✗ — smart link opens one pointer, not the list |
| `[page] twin pointer` (view/edit) | `[smart link] twin pointer` | ✓ |
| `[page] twin pointer list` | `[menu item] twin pointers` | ✓ — menu item opens the list |
| `[page] twin pointer list` | `[table] twin pointer` row | ✗ — a row opens one pointer's view, not the list |
| `[page] twin pointer` (view/edit) | `[table] twin pointer` row | ✓ — clicking a row opens that pointer's view page |

### 4.3 How to choose between the two

The question is about the **other** element `B`, relative to the one you’re linking **from**:

| Relationship | Link type | Direction (who is outward) |
|---|---|---|
| `B` is the **container** that renders the current element | `UI element` (`uses`) | part = outward, container = inward |
| `B` is the **destination** the current element navigates to | `UI pointer` (`points to`) | destination = outward, source = inward |
| `B` is a sibling referenced for context only | `Relates` | symmetric |

> **Tip — the “find all usages” use case.** To see every place an atom (e.g. an
> input field) appears on the front end, open that element and read its **outward**
> links of type **`UI element`** (`uses UI element`). Each outward link is a
> container (page / table / modal / …) that renders it. From each container, read
> its own outward `uses` links to climb to the top-level pages.

---

## 5. Description templates by type

Keep the description minimal and structural — the links carry the relationships,
the description carries the spec. Smartlinks inside tables are **allowed and
encouraged** as a human-readable mirror of the issue links.

> **Clickable smartlinks require ADF, not markdown.** When writing a description
> through the Atlassian MCP, a plain markdown URL
> (`https://alcosi.atlassian.net/browse/TWINFACES-NNNN`) is converted to a
> clickable `inlineCard` **non-deterministically** — in practice it often stays
> plain (non-clickable) text, especially inside table cells next to an empty cell.
> To guarantee a clickable issue card, send the description with
> `contentFormat: "adf"` and an explicit `inlineCard` node:
> ```json
> { "type": "inlineCard", "attrs": { "url": "https://alcosi.atlassian.net/browse/TWINFACES-NNNN" } }
> ```
> placed inside a `paragraph` inside the `tableCell` / `paragraph`. On save it
> renders as `<custom data-type="smartlink">…</custom>` — the same clickable card
> the JIRA UI produces. The templates below are shown in markdown for readability;
> emit them as ADF with `inlineCard`s when actually writing the issue.

### input field
```md
### Input type:
<Checkbox | Text | Select | Multi-select | Plate …>

<behavior bullets: validation, enter-key handling, plate “x” button, …>
```
Example — `TWINFACES-150` `[input field] uuids (multi)`.

### modal (form)
```md
Fields:

|   | **UI element** |
| --- | --- |
| <Field label> | <smartlink to input field> |
| … | … |

API endpoint:
`/private/<domain>/<entity>/<operation>/v1`
```
Example — `TWINFACES-1052` `[modal] factory trigger duplicate`.

> **Featurer params are not a UI field.** A featurer's parameters (e.g.
> `pointerParams` hstore) are configured by the featurer itself, not exposed as an
> input field in the modal — omit them from the `Fields` table.

### action menu
```md
Elements

| **Label** | **Modal** |
| --- | --- |
| Duplicate | <smartlink to [modal] … duplicate> |
| Export    | <smartlink to [modal] … export> |
| Delete    | not implemented |
```
Example — `TWINFACES-1050` `[action menu] factory trigger`.

### table
```md
<one-line purpose>

Columns:

| **Column** | **UI element** | **Sort** |
| --- | --- | --- |
| Id | <smartlink to [smart link] id> | |
| <Column> | <smartlink to input field / smart link> | `<sortField>` |
| … | | |

`API endpoint:` `/private/<domain>/<entity>/search/v1`
`Table filter`: <smartlink to [table filter] …>
```
Example — `TWINFACES-981` `[table] domain business account users`.

> **The first column is always `Id`** — it uses the `[smart link] id` atom
> (`TWINFACES-200`), the clickable object id. Never omit it, and **leave `Sort`
> empty** — the Id column is not sortable.

### table filter
```md
<one-line purpose>

Columns:

| **Filter** | **UI element** |
| --- | --- |
| <Filter> | <smartlink to input field> |
| … | … |

API endpoint: `/private/<domain>/<entity>/search/v1`
```
Example — `TWINFACES-983` `[table filter] domain business account user`.

> **Filter rows come from the backend search DTO.** Each filter must map to a real
> field of the entity's `*SearchDTOv1` (request DTO) / domain `*Search` — verify
> before adding. Do not invent filters the API doesn't accept; if one is desired
> but not yet supported, mark the row `planned`. Example: twin pointer supports
> `createdAt` (date range) but not `createdBy`; twin link supports both. When the
> backend gains a field, re-check the filter and add the row + its `uses` link.

### smart link
```md
<what is shown as a link and why>

`Text for link:` <field>
`Points to`: <page / entity>
Tooltip fields:
* Id
* Name
* Created at
```
Example — `TWINFACES-982` `[smart link] domain business account`.

### page (list)
```md
<one-line purpose>

| **Type** | **Label** | Element | **Comment** |
| --- | --- | --- | --- |
| Table |  | <smartlink to [table] …> | <limit/hide notes> |
| … | | | |
```
Example — `TWINFACES-1068` `[page] twin pointer list`.

### page (view / edit)
A view / edit page of one entity. It **holds tabs** (no `Fields` table here —
editable fields live on the tabs). Create the tabs first (bottom-up), then link
`tab → uses → page`. A `[smart link]` and a `[table]` row both `UI pointer`-point
**here** (not at the list page).
```md
View / edit page for a single <entity>.

`API endpoint:` `/private/<entity>/{id}/v1`
```
Example — `TWINFACES-1071` `[page] twin pointer`.

### tab
A tab of a view page. Parent Epic = `UI type: tab` (`TWINFACES-138`) — even though
the summary starts with `[page]`. Holds the editable fields of one section.
```md
<Section> tab of the <entity> view / edit page.

|   | **UI element** |
| --- | --- |
| <Field label> | <smartlink to input field> |
| … | … |
```
Example — `TWINFACES-1073` `[page] twin pointer [tab] general`; `TWINFACES-163` `[page] class [tab] general`.

### menu item / menu
```md
show only if <PERMISSION_NAME> permission present for user
```
(plus a `UI pointer` link to the destination page)
Example — `TWINFACES-863` `[menu item] factory triggers`.

### chain / tree view
```md
<what is visualised>
Data source: `<DTO/field>` (e.g. TwinClassV1.headClassIdSet)
* <ordering rules>
* <visibility rules>
```
Example — `TWINFACES-898` `[chain] class head hierarchy`.

---

## 6. Connection to the twins backend

TWINFACES elements are tightly bound to twins code through three explicit hooks:

1. **API endpoint** — every modal / table / filter that hits the backend lists the
   real twins REST endpoint (private, versioned). These are **real controllers**.
   * `[modal] factory trigger duplicate` → `/private/factory_trigger/duplicate/v1`
     → `core/.../controller/rest/priv/factory/FactoryTriggerDuplicateController.java`
2. **Permission gate** — menu items / menus declare the twins permission that
   controls their visibility, e.g. `TWIN_TRIGGER_MANAGE`
   (see `TWINS-768` `*_MANAGE` permission refactoring, `TWINFACES-851`).
3. **Entity reference** — an element may smartlink to the twins entity/feature
   task it operates on (the entity task then ties back to the TWINS backend work,
   e.g. `TWINS-593` describes which TwinFaces elements to create for a feature).

> When a backend feature task (TWINS-xxx) lands, the *same* ticket usually lists
> the UI elements to create/update (`[menu item] …`, `[page] …`, `[modal] …`).
> Treat that list as the work order and create the elements from this convention.

---

## 7. Recipe — add a new UI element

> **Golden rule — build bottom-up.** Create parts before containers. Atoms first
> (input fields, smart links — usually already exist), then `table filter`, then
> `table`, then the list `page`, then the **tabs**, then the **view `page`** (each
> tab links `uses → view page`), then modals, then `action menu`, then `menu item`.
> That way every `uses UI element` / `points to` link points at something that
> already exists and can be wired **at creation time**. Building top-down forces
> you to backfill links later (and they get forgotten).
>
> Get the link **direction right the first time** — the Atlassian MCP has no
> `deleteIssueLink`, so a wrong-direction link has to be removed by hand in the
> JIRA UI. (Direction rules: §4.)

Per element:

1. **Identify the type** → pick the parent Epic from §2.
2. **Name it** per §3: `[type] domain entity [tab] section | operation`.
3. **Create the issue** in `TWINFACES`, type `UI element`, set the parent Epic.
4. **Fill the description** from the matching template in §5.
5. **Add formal links — mandatory, not optional.** Issue links are the source of
   truth that powers §9 (“find all usages”); smartlinks in the description are
   only a human-readable mirror. Add **both**, always. **Mind the direction (§4):
   the PART is outward, the CONTAINER is inward** — so when you create a container
   you link *from each part to it*, not from it to the parts:
   * composition **`UI element`** — for each part the element renders, create
     `part → uses → this` (i.e. `createIssueLink(outwardIssue = part,
     inwardIssue = this, type = "UI element")`). Parts: input fields, smart links,
     nested tables, table filter, modals, menu items…
   * navigation **`UI pointer`** — the DESTINATION is outward, the source is inward.
     If this element is a destination (page), for each source leading into it
     create `this → points to → source` (`outwardIssue = this, inwardIssue =
     source, type = "UI pointer"`). If this element is a source (menu item / smart
     link), the link is created from its destination page instead.
6. **Mirror those links as smartlinks** in the description table (§5) for
   readability. Write the description as **ADF with `inlineCard` nodes** (§5) —
   plain markdown URLs render as non-clickable text unpredictably.
   **When you link a new part into an EXISTING container** — adding a tab to a
   page, a menu item to a menu, an action to an action menu, a column to a table —
   you must **also edit the container's description** to add the new smartlink
   row. The container's table is the human-readable index of its parts; a formal
   link alone leaves the description stale. (This was forgotten on `TWINFACES-362`
   and `TWINFACES-158` — always check the container side.)
7. **Bind to twins backend** — set `API endpoint` / permission / entity smartlink
   where applicable (§6).
8. **Set status** (§8): `Not implemented` until built, then `Has changes`, finally
   `Up to date`.

> **Never guess a key.** Do not write a real `TWINFACES-NNNN` into a description
> before that issue exists — the number may land on an unrelated issue. If you
> must reference a not-yet-created sibling, leave a text placeholder and replace
> it with the smartlink **right after** that element is created.

### CRUD family shortcut
For a new manageable entity (e.g. `factory <x>`) create the **set** and link them
together. The list below is the **creation order (bottom-up)** — parts first,
containers last — so each `uses` / `points to` lands on an existing element:

Arrows read `source ──uses──▶ container` (part is outward, §4.1) and
`destination ──points to──▶ source` (§4.2):

```
1. atoms           [input field] <entity fields>          (often already exist)
                   [smart link] <entity>
2. table filter    input fields                             ──uses──▶ [table filter] <entity>
3. table           smart link + column atoms + table filter ──uses──▶ [table] <entity>
4. page (list)     [table] <entity>                         ──uses──▶ [page] <entity> list
5. tab(s)          input fields                             ──uses──▶ [page] <entity> [tab] general (+ other tabs)
6. page (view)     [tab] general (+ other tabs)             ──uses──▶ [page] <entity>   ← smart link & table row point here
7. modals          input fields                             ──uses──▶ [modal] <entity> create / delete
8. action menu     modals                                   ──uses──▶ [action menu] <entity>
9. menu item       [page] <entity> list                     ──points to──▶ [menu item] <entity>
```
> **No separate `[modal] <entity> update`.** Editing happens on the view page
> (step 6) via its tabs, not via a modal — so the modal set is `create` (+ optionally `delete`).

> **View page (step 6) is the `UI pointer` target for the `[smart link]` and the
> `[table]` row** — both navigate to one object's view, never to the list page (§4.2).

Concrete mirror: `factory trigger` (`TWINFACES-863`, `873`, `866`, `870`, `1050`,
`1052`, `1051`).

---

## 8. Status lifecycle

UI elements use a dedicated status flow (not the generic SDLC one):

| Status | Meaning |
|---|---|
| `Not implemented` | Designed in JIRA, not yet built on the front end |
| `IN DEVELOPMENT` | Being implemented |
| `In Review` | Implemented, under review |
| `Has changes` | Was `Up to date`, now the spec changed and needs a front-end update |
| `Up to date` | Front end matches the spec |

---

## 9. Recipe — “where is this element used?”

1. Open the element (e.g. an `[input field]`).
2. Read its **outward** links of type **`UI element`** (`uses UI element`). Each
   one is a direct container that renders it (§4.1: the part links outward to its
   container).
3. For each container, repeat step 2 (read its outward `uses` links) to climb to
   the top-level **pages**.
4. To find navigation in reverse — who opens a page — read the page's **outward**
   `UI pointer` (`points to`) links; each is a source (menu item / smart link)
   that navigates into it (§4.2: destination links outward to its source).
5. Cross-check the description tables (smartlinks) — they mirror the same graph
   for human reading.

---

## Appendix — reference keys

* Project: `TWINFACES` · UI element issue type id: `11063`
* Link type ids: `UI element` = `10440`, `UI pointer` = `10473`
* Canonical examples used above:
  * `[modal] factory trigger duplicate` — `TWINFACES-1052`
  * `[action menu] factory trigger` — `TWINFACES-1050`
  * `[table] domain business account users` — `TWINFACES-981`
  * `[table filter] domain business account user` — `TWINFACES-983`
  * `[smart link] domain business account` — `TWINFACES-982`
  * `[page] domain business account [tab] twins` — `TWINFACES-985`
  * `[input field] uuids (multi)` — `TWINFACES-150`
  * `[input field] toggle` — `TWINFACES-278` (reused 39× — good “find usages” sample)
  * `[chain] class head hierarchy` — `TWINFACES-898`
  * `[menu item] factory triggers` — `TWINFACES-863` (shows both link types at once)
