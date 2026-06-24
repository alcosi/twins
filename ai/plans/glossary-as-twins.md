# Glossary-as-Twins — Design Document

**Status:** Draft
**Date:** 2026-06-17
**Author:** Nikit (with Claude)
**Scope:** Convert `docs/glossary.md` into a structured set of per-entity markdown files, used as the source of truth for bootstrapping corresponding Twins at application startup. Enables MCP / admin frontend to query the glossary via the standard Twin search API.

---

## 1. Goals & Non-Goals

### Goals

- **Single source of truth** — markdown in `docs/glossary/entities/*.md` describes every documented domain entity. Source control = git history.
- **Bootstrap-on-startup** — application reads markdown files at boot and upserts corresponding Twins in DB. No manual data entry needed.
- **Standard API access** — MCP servers and admin frontends query glossary entries through the existing `/private/twin/search/v1` endpoint (filtered by TwinClass `TWINS_GLOSSARY`).
- **Cross-references via TwinLink** — entity relations (Twin → TwinClass, TwinClass → Twinflow) are stored as typed links between glossary Twins.
- **Categorization via TwinTag** — categories (`core`, `workflow`, `permissions`, …) become tags.

### Non-Goals

- ❌ Authoring glossary content via the admin UI (markdown is the only write path).
- ❌ Live sync of code changes into glossary (developer manually updates `.md` when entity changes).
- ❌ Full-text search across entity bodies (standard Twin search covers metadata + body).
- ❌ Generation of JPA entities from glossary (one-way: code → markdown).

---

## 2. File Layout

```
docs/glossary/
  README.md                          ← this design doc summary + how-to
  index.md                           ← auto-generated TOC by gradle task
  entities/
    twin.md                          ← one file per entity
    twin-class.md
    twin-class-field.md
    twin-status.md
    twin-alias.md
    twin-link.md
    twin-tag.md
    twin-marker.md
    twinflow.md
    twinflow-transition.md
    twinflow-transition-type.md
    eraseflow.md
    twin-factory.md
    twinflow-factory.md
    domain.md
    domain-type.md
    domain-business-account.md
    domain-user.md
    business-account.md
    business-account-user.md
    user.md
    user-group.md
    space.md
    space-role.md
    permission.md
    permission-group.md
    permission-schema.md
    twin-comment.md
    twin-attachment.md
    data-list.md
    data-list-option.md
    i18n.md
    featurer.md
    twin-class-schema.md
    twinflow-schema.md
    permission-schema.md
    twin-field-simple.md
    twin-field-boolean.md
    twin-field-decimal.md
    twin-field-timestamp.md
    twin-field-user.md
    twin-field-twin-class.md
    twin-field-data-list.md
    twin-field-i18n.md
    twin-field-attribute.md
    twin-class-field-rule.md
    validator.md                     ← covers all validator flavors
  schemas/
    V1.x.y.z__TWINS_GLOSSARY_create_glossary_class.sql   ← Flyway migration
core/src/main/java/org/twins/bootstrap/
    GlossaryBootstrapService.java     ← startup logic
    GlossaryMarkdownParser.java       ← frontmatter + body parser
    GlossaryEntityDto.java            ← in-memory representation
    GlossaryBootstrapRunner.java      ← ApplicationRunner wrapper
```

---

## 3. Markdown Format Specification

Each entity file = **minimal frontmatter** + **structured body sections**. Frontmatter carries identity + structured links. Body H2 sections become individual long-text fields on the glossary Twin.

### 3.1 Frontmatter — identity + curation

```yaml
---
slug: twin                  # required, kebab-case, matches filename
title: Twin                 # required, display name → Twin.name
category: core              # required, enum → DataList tag
class: TwinEntity           # optional, JPA simple name (developer info)
table: twin                 # optional, DB table name (developer info)
is_system: false            # optional, default false
actualized_at: 2026-06-17   # required, ISO date — when content was last verified by a human
see_also:                   # optional, list of slugs → TwinLink
  - twin-class
  - twinflow
---
```

### 3.2 Body sections — each H2 → TwinClass field

Standard section set:

```markdown
# Twin

## Summary
One to three sentences defining what the entity is. Used as card preview in admin UI.

## Purpose
2-3 short paragraphs covering: what business need it serves, when one is created,
how it fits into the broader system. The reader should understand "why does this
entity exist and when do I encounter it" after reading.

## Fields
Full column list (every persisted field). NO types — they live in the JPA source.
For FK columns, link to the related entity's markdown file (relative path).
Needed for admin UI creation modals — operator must see what each field does.

## Relations
Structured table — one row per relation. Columns: `Target`, `Cardinality`, `Kind`, `Description`.
Complements ## Fields by giving the architecture view (how this entity fits with others).

## API
Current REST endpoints (paths + brief description). Used by MCP servers and admin
frontend to discover entrypoints per entity. Only list non-deprecated versions here.

## API (deprecated)
Endpoints that are still available but slated for removal. Each entry should
note the replacement (e.g., "use /v2 instead") and reason if non-obvious.
Displayed with a "deprecated" badge in admin UI.

## Examples
Non-SQL snippets showing typical usage: JSON request/response payloads, real-world
use cases. SQL DDL belongs in Flyway migrations, not in the glossary.

## Dev notes
Developer-facing gotchas, anti-patterns, debugging tips, performance considerations.
NOT shown in default admin UI — only in "developer mode" toggle.
```

**Section-to-field mapping** (sections map either to a base Twin field or to a `TWINS_GLOSSARY` field — see §4):

| Body section | Storage                                                           | Required | Admin UI tier |
|---|-------------------------------------------------------------------|---|---|
| `## Summary` | `Twin.description` (base field, inherited from `GLOBAL_ANCESTOR`) | yes | always visible (card preview) |
| `## Purpose` | `purpose` TwinClass field                                         | no | default open |
| `## Fields` | `fields` TwinClass field                                          | yes | default open |
| `## Relations` | `relations_overview` TwinClass field                              | no | default open |
| `## API` | `api` TwinClass field                                             | no | default open |
| `## API (deprecated)` | `api_deprecated` TwinClass field                                  | no | collapsible (deprecated badge) |
| `## Examples` | `examples` TwinClass field                                        | no | collapsible |
| `## Dev notes` | `dev_notes`  TwinClass field                                                      | no | **hidden** (developer mode toggle) |

**Relations table format:**

```markdown
## Relations

| Target | Cardinality | Kind | Description |
|---|---|---|---|
| [TwinClass](twin-class.md) | many-to-one | owning | This twin's class — defines schema |
| [TwinStatus](twin-status.md) | many-to-one | transient_runtime | Current status (loaded via service) |
| [TwinLink](twin-link.md) | one-to-many | own_collection | Outgoing links via srcTwinId |
```

**Cardinality values:** `one-to-one` · `one-to-many` · `many-to-one` · `many-to-many`

**Kind values:**

| Kind | Meaning |
|---|---|
| `owning` | Source entity owns the FK column (e.g., `twin.twin_class_id`) |
| `own_collection` | `@OneToMany` collection owned by source |
| `link` | Cross-aggregate link entity (TwinLink, TwinTag, TwinMarker) |
| `semantic` | Conceptual relation — no direct FK |


### 3.3 Field entries — format and FK links

Inside `## Fields`, each entry is a bullet:

```markdown
## Fields

- `id` — primary key (UUID v7)
- `twinClassId` — FK to [TwinClass](twin-class.md)
- `headTwinId` — parent twin in hierarchy (optional)
- `ownerBusinessAccountId` — FK to [BusinessAccount](business-account.md)
- `viewPermissionId` — FK to [Permission](permission.md), overrides class default when `viewPermissionCustom=true`
- `createdAt` — creation timestamp
```

**FK link convention:**

- Use standard markdown link syntax: `[Display Name](<target-slug>.md)`
- Path is relative within `docs/glossary/entities/`
- Target slug MUST match an existing entity's `slug` (parser warns on dangling links)
- The admin UI intercepts these links and routes to the corresponding glossary Twin (queried by `externalId = "glossary:<slug>"`) instead of opening the `.md` file

**Why no types:** the JPA source is the canonical schema; reproducing types in markdown creates maintenance burden and drift risk. Operators get descriptions, developers get the JPA link.

### 3.4 Validation rules

- `slug` matches `^[a-z][a-z0-9-]*$` and equals filename stem
- `title` non-empty
- `category` in: `core`, `workflow`, `multi-tenancy`, `permissions`, `content`, `cross-cutting`, `fields`, `validation`, `other`
- `actualized_at` is required, ISO 8601 date (`YYYY-MM-DD`)
- Body MUST start with `# <title>` H1 (matches frontmatter `title`)
- Body MUST contain `## Summary` AND `## Fields`
- Any H2 not in the standard set → parser warning (skipped, not stored)
- Each `see_also[]` slug MUST exist in the parsed set, OR is logged as dangling (warn, not fail)
- Each `[X](x.md)` markdown link in `## Fields` MUST resolve to an existing slug, OR is logged as dangling (warn, not fail)
- `## API (deprecated)` SHOULD reference replacement endpoints where they exist (warning if not)

### 3.5 Why lean

- **Maintainability** — types and schema live in JPA / Flyway; glossary describes concepts and gives operators enough context to use admin UI creation modals
- **Admin UX** — sections become individual panels with explicit tiers (always visible / default open / collapsible / hidden dev-only); user sees what they need, expands on demand
- **Authoring cost** — a developer can write a new entity card in 15 minutes (full field list with descriptions is the bulk of the work)
- **Diff noise** — schema-affecting changes (rename, add column) require updating the matching `## Fields` bullet, which is one-line edit
- **Curation visible** — `actualized_at` field forces periodic review; admin UI flags stale entries (default threshold: 180 days) so outdated content is surfaced, not hidden

---

## 4. TWINS_GLOSSARY TwinClass Schema

The glossary Twins live in a single TwinClass with key `TWINS_GLOSSARY`. Each field in this class corresponds to a section of the source markdown — see §3.2 for the mapping. All UUIDs are registered as constants in `SystemEntityService` (see §15.6 for the constant block).

### Class identity

| Property | Value                                                                      |
|---|----------------------------------------------------------------------------|
| UUID | `00000000-0000-0000-0001-000000000006`                                     |
| Constant | `SystemEntityService.TWIN_CLASS_TWINS_GLOSSARY`                            |
| Key | `TWINS_GLOSSARY`                                                           |
| Owner type | `SYSTEM`                                                                   |
| Domain | null                                                                       |
| Created by | `SystemEntityService.USER_SYSTEM` (`00000000-0000-0000-0000-000000000000`) |

### Statuses

Two statuses are defined on the class — both migrated in §17, both registered as constants in §15.6. `ACTUAL` is the default for newly-bootstrapped Twins; `DELETED` is the soft-delete target for orphan cleanup (see §16 PHASE 2, MARK_DELETED action).

| Status | UUID | Constant |
|---|---|---|
| ACTUAL | `00000000-0000-0000-0003-000000001001` | `SystemEntityService.TWIN_STATUS_GLOSSARY_ACTUAL` |
| DELETED | `00000000-0000-0000-0003-000000001002` | `SystemEntityService.TWIN_STATUS_GLOSSARY_DELETED` |

i18n name/description UUIDs for each status (all in `0012-...` range, registered in `SystemEntityService` per §15.6):
- `I18N_GLOSSARY_STATUS_ACTUAL_NAME` = `00000000-0000-0000-0012-000000000039` (i18n_type `'twinStatusName'`, English: "Actual")
- `I18N_GLOSSARY_STATUS_ACTUAL_DESCRIPTION` = `00000000-0000-0000-0012-000000000040` (i18n_type `'twinStatusDescription'`, English: "Glossary entry is in sync with its markdown source file")
- `I18N_GLOSSARY_STATUS_DELETED_NAME` = `00000000-0000-0000-0012-000000000041` (English: "Deleted")
- `I18N_GLOSSARY_STATUS_DELETED_DESCRIPTION` = `00000000-0000-0000-0012-000000000042` (English: "Source markdown file removed; Twin retained for referential integrity")

### Fields

All field UUIDs share the prefix `00000000-0000-0000-0011-` and are registered in `SystemEntityService` as `TWIN_CLASS_FIELD_GLOSSARY_*` (see §15.6). Featurer IDs: 1336 = `FieldTyperTextNonIndexedField` (long markdown), 1301 = `FieldTyperTextField` (indexed short text), 1306 = `FieldTyperBooleanV1`, 1302 = `FieldTyperTimestamp`.

| Field UUID | Field key | Field type (featurer ID) | Required | Source | Admin UI tier | Constant suffix |
|---|---|---|---|---|---|---|
| `00000000-0000-0000-0011-000000001000` | `purpose` | long text (1336) | no | body `## Purpose` | default open | `_GLOSSARY_PURPOSE` |
| `00000000-0000-0000-0011-000000001001` | `fields` | long text markdown (1336) | yes | body `## Fields` | default open | `_GLOSSARY_FIELDS` |
| `00000000-0000-0000-0011-000000001002` | `relations_overview` | long text (1336) | no | body `## Relations` | default open | `_GLOSSARY_RELATIONS_OVERVIEW` |
| `00000000-0000-0000-0011-000000001003` | `api` | long text markdown (1336) | no | body `## API` | default open | `_GLOSSARY_API` |
| `00000000-0000-0000-0011-000000001004` | `api_deprecated` | long text markdown (1336) | no | body `## API (deprecated)` | collapsible (deprecated badge) | `_GLOSSARY_API_DEPRECATED` |
| `00000000-0000-0000-0011-000000001005` | `examples` | long text markdown (1336) | no | body `## Examples` | collapsible | `_GLOSSARY_EXAMPLES` |
| `00000000-0000-0000-0011-000000001006` | `dev_notes` | long text markdown (1336) | no | body `## Dev notes` | **hidden** (developer mode only) | `_GLOSSARY_DEV_NOTES` |
| `00000000-0000-0000-0011-000000001007` | `jpa_class` | simple string (1301) | no | frontmatter `class` | developer-only | `_GLOSSARY_JPA_CLASS` |
| `00000000-0000-0000-0011-000000001008` | `db_table` | simple string (1301) | no | frontmatter `table` | developer-only | `_GLOSSARY_DB_TABLE` |
| `00000000-0000-0000-0011-000000001009` | `markdown_source` | simple string (1301) | yes (auto) | computed at bootstrap | developer-only | `_GLOSSARY_MARKDOWN_SOURCE` |
| `00000000-0000-0000-0011-000000001010` | `markdown_hash` | simple string (1301) | yes (auto) | sha256 of source content | hidden (internal) | `_GLOSSARY_MARKDOWN_HASH` |
| `00000000-0000-0000-0011-000000001011` | `is_system` | boolean (1306) | yes | frontmatter `is_system` | developer-only (chip) | `_GLOSSARY_IS_SYSTEM` |
| `00000000-0000-0000-0011-000000001012` | `actualized_at` | date (1302) | yes | frontmatter `actualized_at` | always visible (meta line) | `_GLOSSARY_ACTUALIZED_AT` |
| *(not a TwinClassField)* | `category` | DataList option (`GLOSSARY_CATEGORY`) | yes | frontmatter `category` | always visible (chip) | n/a — stored as TwinTag |

**Note:** the `## Summary` body section is stored on the base `Twin.description` field (`SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION = 00000000-0000-0000-0011-000000000004`, inherited from `GLOBAL_ANCESTOR`), not as a separate TwinClass field. This avoids duplicating a long-text column that already exists on every Twin.

**Markdown rendering** — `fields`, `api`, `api_deprecated`, `examples`, `dev_notes` are stored as raw markdown. Admin UI renders them with a markdown renderer. Inline links like `[TwinClass](twin-class.md)` are intercepted and routed to the corresponding glossary Twin (lookup by `externalId`), not to the file system.

**Staleness flag** — admin UI computes `days_since(actualized_at)` and badges entries older than 180 days as "stale — needs review". Threshold configurable per environment.

### Twin built-ins used

- `Twin.name` ← frontmatter `title` (base field, no glossary-specific UUID)
- `Twin.externalId` ← `"glossary:" + slug` — used for idempotent upsert (base field)
- `Twin.description` ← body `## Summary` section (base field `SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION`)

### Tags

| Tag source | DataList UUID | Options UUID prefix |
|---|---|---|
| `GLOSSARY_CATEGORY` | `00000000-0000-0000-0020-000000000001` | `00000000-0000-0020-0001-00000000000X` (9 options: core, workflow, multi-tenancy, permissions, content, cross-cutting, fields, validation, other) |

`category` value as `TwinTag` via `GLOSSARY_CATEGORY` DataList — for filtering in admin UI and MCP queries. DataList UUIDs are NOT registered in SystemEntityService (the bootstrap service references options by slug, not UUID).

### Links

| Link UUID | Link key | Source → Target | Cardinality | Created from |
|---|---|---|---|---|
| `00000000-0000-0000-0019-000000000001` | `GLOSSARY_SEE_ALSO` | any glossary Twin → any glossary Twin | many_to_many | frontmatter `see_also[]` |

Link UUID is registered in `SystemEntityService` as `LINK_GLOSSARY_SEE_ALSO` (see §15.6) — useful for code-side lookups, debugging, and future direct queries.

Forward/backward i18n UUIDs (NOT registered in SystemEntityService — bootstrap service doesn't reference them):
- Forward name (`'See also'`): `00000000-0000-0000-0012-000000000043` (i18n_type `'linkForwardName'`)
- Backward name (`'Referenced by'`): `00000000-0000-0000-0012-000000000044` (i18n_type `'linkBackwardName'`)

Links are reconciled on every bootstrap pass: add new, remove stale. See §5.

---

## 5. Bootstrap Lifecycle

```
App start
  └─ Flyway migrations (creates TWINS_GLOSSARY TwinClass + GLOSSARY_CATEGORY DataList + GLOSSARY_SEE_ALSO Link)
  └─ Spring context refresh
  └─ ApplicationRunner: GlossaryBootstrapRunner
       └─ GlossaryBootstrapService.bootstrap()
            1. Scan classpath: docs/glossary/entities/*.md
            2. Parse each file:
                 - Split frontmatter / body
                 - Parse frontmatter as YAML → identity + see_also
                 - Split body by H2 headers → section map
                 - Compute sha256(raw bytes) → markdown_hash
            3. PASS 1 — Upsert all Twins by externalId ("glossary:<slug>"):
                 - Find Twin by externalId
                 - If missing → create with TWINS_GLOSSARY class
                 - If found AND markdown_hash != stored → update all section fields + metadata
                 - If found AND hash matches → skip
            4. PASS 2 — Reconcile TwinLink instances (GLOSSARY_SEE_ALSO):
                 - For each entity's see_also[]:
                   - Resolve target slug → glossary Twin (now guaranteed to exist)
                   - Upsert TwinLink
                 - Remove TwinLinks no longer present in frontmatter
            5. PASS 3 — Reconcile TwinTag (category):
                 - Set category tag from frontmatter
                 - Remove previous category tag if changed
            6. PASS 4 — Orphan cleanup:
                 - Find glossary Twins whose markdown_source file no longer exists
                 - Mark with `deprecated` tag — DO NOT auto-delete (manual review)
            7. Log summary: created / updated / skipped / links_added / links_removed / orphans
```

### Hash-based skip

The `markdown_hash` field stores SHA-256 of the source file content. On bootstrap:

- `stored_hash == new_hash` → no-op (fast path)
- `stored_hash != new_hash` → update all section fields + frontmatter-derived fields
- `stored_hash == null` (new Twin) → populate

This makes reboots cheap when nothing changed.

---

## 6. Sync Rules

| Markdown change | What happens on next startup |
|---|---|
| New file added | New glossary Twin created |
| File deleted | Twin status set to `DELETED` (soft delete — kept for referential integrity, still visible in standard search) |
| File renamed (slug changed) | Old Twin marked deprecated; new Twin created. **Breaking** — links from other entities pointing to old slug will dangle. To avoid: rename via "create new + add redirect alias in old file" pattern. |
| `title`, `class`, `table`, `category` changed | Twin fields updated |
| `key_fields` / `relations` / `see_also` changed | Twin fields updated; TwinLink set reconciled (add new, remove stale) |
| Body changed | `description` updated; `markdown_hash` rotated |

### Manual edits to glossary Twins

Per the chosen "markdown wins" model: edits made directly in DB (via admin UI) **will be overwritten** on next startup if `markdown_hash` indicates divergence. The admin UI should display glossary Twins as read-only with a banner:

> "This entry is managed by the glossary markdown files in `docs/glossary/entities/`. Edits here will be reverted on next app restart."

---

## 7. Permissions

```
TWINS_GLOSSARY_VIEW    — required to read glossary Twins (granted to authenticated users)
```

- Public MCP server reads with a service account holding `TWINS_GLOSSARY_VIEW`.
- Admin frontend uses user's normal permissions; glossary Twins inherit `viewPermissionId` from TWINS_GLOSSARY class.

---

## 8. API Usage Examples

### MCP server: find entity by JPA class name

```http
POST /private/twin/search/v1
{
  "search": {
    "twinClassIdList": ["<UUID of TWINS_GLOSSARY>"],
    "nameLikeList": "class"
  }
}
```

Returns the glossary Twin for `TwinClass`.

### Admin frontend: list all workflow entities

```http
POST /private/twin/search/v1
{
  "search": {
    "twinClassIdList": ["<UUID of TWINS_GLOSSARY>"],
    "twinTagDataListOptionIdList": ["<UUID of 'workflow' tag option>"]
  }
}
```

### MCP server: get related entities

```http
POST /private/twin_link/search/v1
{
  "search": {
    "srcTwinId": "<UUID of glossary:twin Twin>",
    "linkIdList": ["<UUID of GLOSSARY_RELATION>"]
  }
}
```

Returns Twins for `twin-class`, `twin-status`, `twin-link`, `twin-tag`, `twin-marker`.

---

## 9. Identity & Determinism

- `externalId = "glossary:" + slug` — stable across environments (dev/test/stage).
- The TWINS_GLOSSARY class itself is created by a Flyway migration with `key = 'TWINS_GLOSSARY'` and `domainId = null`, .
- Glossary Twins live in the **system domain** (not in any tenant domain) to avoid cross-tenant leakage.

---

## 10. Migration Plan

### Phase 1 (this iteration)

1. ✅ Design document (this file)
2. ✅ Markdown format spec (in §3)
3. ✅ 3 pilot entity files: `twin.md`, `twin-class.md`, `twinflow.md`
4. ⏭️ Review with team — confirm format, slugs, category values

### Phase 2 (next iteration)

1. Flyway migration: create `TWINS_GLOSSARY` class, `GLOSSARY_CATEGORY` DataList, `GLOSSARY_ALIAS` DataList, `GLOSSARY_RELATION` and `GLOSSARY_SEE_ALSO` Link types
2. Implement `GlossaryMarkdownParser`, `GlossaryBootstrapService`, `GlossaryBootstrapRunner`
3. Convert remaining ~37 entity sections from `docs/glossary.md` into individual files
4. Delete `docs/glossary.md` (or keep as auto-generated `index.md`)

### Phase 3

1. Wire `GlossaryBootstrapRunner` with `@ConditionalOnProperty` so it can be disabled in tests
2. Integration test: bootstrap on empty DB → expected Twins + Links created
3. Integration test: change markdown → next bootstrap updates fields, reconciles links
4. Documentation: `docs/glossary/README.md` for content authors

---

## 11. Risks & Open Questions

### Risks

| Risk | Mitigation |
|---|---|
| Markdown format drift (typos in frontmatter) | Strict parser — fail fast on startup if any file doesn't validate; print offending file + reason |
| Slug rename breaks incoming links | Detect dangling references in pass 2 — log warning, do not fail; the DELETED status on the old Twin flags the issue |
| Bootstrap slows startup | Hash-based skip means steady-state is fast. Measure with 40 files; expect <500ms |
| Glossary Twins pollute normal search | `externalId LIKE 'glossary:%'` filter convention; admin UI hides them by default unless "show glossary" toggle is on |
| TWINS_GLOSSARY class requires TWINS_GLOSSARY description (chicken/egg) | TWINS_GLOSSARY class itself is documented in markdown, but its own Twin is bootstrapped as part of phase 2; it's just another entity entry |

### Open Questions

1. Should glossary Twins be **searchable by default** in admin frontend, or hidden behind a toggle? (Recommendation: hidden by default, toggle in UI.)
2. Should `markdown_source` field contain repo-relative path or absolute? (Recommendation: repo-relative — portable across machines.)
3. Should we support **i18n bodies** (multiple markdown files per entity, one per locale)? (Recommendation: defer; current `I18n` field on Twin.description supports translation without per-locale markdown.)
4. What happens if the system domain is not yet bootstrapped when `GlossaryBootstrapRunner` fires? (Recommendation: order via `@DependsOn` on the system-domain seed migration.)

---

## 12. Appendix: Parser Strategy

`GlossaryMarkdownParser` is a thin component:

1. Read file as UTF-8 → raw bytes for hash
2. Split on first `---` (frontmatter delimiter) and second `---`
3. Parse frontmatter as YAML (use `com.fasterxml.jackson.dataformat.yaml.YAMLMapper` — already on classpath via Spring Boot)
4. Map frontmatter to `GlossaryIdentity` (validated via Bean Validation):
   - `slug` (required, regex `^[a-z][a-z0-9-]*$`)
   - `title` (required, non-empty)
   - `category` (required, enum)
   - `class` (optional)
   - `table` (optional)
   - `is_system` (optional, default `false`)
   - `see_also` (optional, list of slugs)
5. Parse body:
   - First H1 must match `title`
   - Walk H2 headers, collect each section's content into `Map<String, String>`
   - Standard section set: `Summary`, `Purpose`, `Fields`, `Relations`, `API`, `Examples`, `Dev notes`
   - Unknown H2s are logged as warnings (skipped, not stored)
6. **Required sections: `## Summary` AND `## Fields`** — parser fails fast if either is missing
7. Within `## Fields`, scan for inline markdown links `[X](x.md)`:
   - Collect target slugs for cross-reference validation
   - Store raw markdown in the `fields` TwinClass field (no transformation)
8. Compute `sha256(raw bytes)` → `markdown_hash`

---

## 13. Summary

- **Markdown = single source of truth.** Developers edit `.md`, commit, restart.
- **Bootstrap on startup** keeps DB in sync; hash-based skip makes it cheap.
- **Standard Twin API** unlocks MCP and admin frontend integration with zero custom endpoints.
- **TwinLink + TwinTag** model relations and categories using existing primitives.
- **Two-pass bootstrap** handles forward references safely.
- **Pilot of 3 entities** validates the format before migrating all 40.

---

## 14. Component Diagram (Phase 2)

The bootstrap pipeline consists of five Spring components. Dependencies flow top-down; the runner is the only externally-triggered entry point.

```
                  ┌──────────────────────────────────┐
                  │  ApplicationReadyEvent (Spring)  │
                  └────────────────┬─────────────────┘
                                   │
                                   ▼
          ┌────────────────────────────────────────────┐
          │   GlossaryBootstrapRunner                  │
          │   - @ConditionalOnProperty                 │
          │   - ApplicationListener<ApplicationReady>  │
          │   - thin wrapper, error boundary           │
          └────────────────┬───────────────────────────┘
                           │ delegates to
                           ▼
          ┌────────────────────────────────────────────┐
          │   GlossaryBootstrapService                 │
          │   - @Service, @Transactional (one tx)      │
          │   - orchestrates 2-phase flow:             │
          │     DISCOVERY (parse + classify) →         │
          │     EXECUTE (TwinService createTwin/       │
          │     updateTwin batched)                    │
          │   - status mgmt: ACTUAL on create/restore, │
          │     DELETED on orphan cleanup              │
          │   - returns GlossaryBootstrapResult        │
          └──┬────────────────────────┬────────────────┘
             │                        │
             ▼                        ▼
   ┌─────────────────┐  ┌──────────────────────────────┐
   │ MarkdownParser  │  │ TwinService                  │
   │ - @Component    │  │ - createTwin(TwinCreate)     │
   │ - reads .md     │  │   with fields + links + tags │
   │   from disk     │  │   in one call                │
   │ - parses YAML + │  │ - updateTwin(TwinUpdate)     │
   │   H2 sections   │  │   with TwinLinkCUD +         │
   └────────┬────────┘  │   TwinTagCUD + field changes │
            │           │ - permission bypass via      │
            │           │   checkCreate/EditPermission │
            │           │   =false (default)           │
            │           │ - handles TwinFields         │
            │           │   typecast + search index    │
            │           │   + GLOBAL_ANCESTOR          │
            │           │   inheritance internally     │
            │           └──────────────────────────────┘
            │ produces
            ▼
   ┌────────────────────────────────────────────┐
   │   GlossaryEntityDto (plain Java record)    │
   │   - identity (slug, title, category, ...)  │
   │   - sections map (Summary, Fields, ...)    │
   │   - markdownSource, markdownHash           │
   │   - validation: throws GlossaryParseExcn   │
   └────────────────────────────────────────────┘
            │ produces
            ▼
   ┌────────────────────────────────────────────┐
   │   GlossaryEntityDto (plain Java record)    │
   │   - identity (slug, title, category, ...)  │
   │   - sections map (Summary, Fields, ...)    │
   │   - markdownSource, markdownHash           │
   │   - validation: throws GlossaryParseExcn   │
   └────────────────────────────────────────────┘
```

**External dependencies added by Phase 2:**

| Dependency | Purpose | Added to |
|---|---|---|
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | Parse YAML frontmatter | `core/build.gradle` |
| `com.github.f4b6a3:uuid-creator:6.1.1` (already present) | Deterministic UUIDv5 | — |
| `commons-codec:commons-codec:1.17.1` (already present) | SHA-256 of markdown source | — |

**Existing services reused:**

- `EntitySmartService.saveAllAndLog(List, repository, SaveMode)` — batch upsert pattern, already used in `SystemEntityService.java:305-308`.
- `TwinClassRepository.findByDomainIdAndKey(domainId, key)` — locate `TWINS_GLOSSARY` class at boot.
- `TwinLinkService` / `TwinTagService` — existing services for cross-twin link/tag operations.

---

## 15. Class Signatures

### 15.1 GlossaryMarkdownParser

```java
package org.twins.bootstrap;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class GlossaryMarkdownParser {

    private static final String CLASSPATH_PATTERN = "classpath:/docs/glossary/entities/*.md";
    private static final YAMLMapper YAML = new YAMLMapper();

    /**
     * Scan classpath for all glossary markdown files, parse each.
     * Files that fail validation are logged at WARN level and excluded
     * from the returned list — they do NOT abort the bootstrap pass.
     *
     * @return list of valid DTOs (one per successfully-parsed file)
     */
    public List<GlossaryEntityDto> parseAll();

    /**
     * Parse a single markdown file. Throws GlossaryParseException on
     * schema violation (missing required section, malformed frontmatter).
     */
    public GlossaryEntityDto parse(Resource mdFile) throws GlossaryParseException;
}
```

### 15.2 GlossaryEntityDto

```java
package org.twins.bootstrap;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory representation of a parsed glossary entity file.
 * Identity + structured sections + bootstrap metadata.
 *
 * @param slug            kebab-case, matches filename stem (regex ^[a-z][a-z0-9-]*$)
 * @param title           display name, non-empty
 * @param category        one of: core|workflow|multi-tenancy|permissions|content|cross-cutting|fields|validation|other
 * @param jpaClass        optional JPA simple name (e.g. "TwinEntity")
 * @param dbTable         optional DB table name (e.g. "twin")
 * @param isSystem        optional, default false
 * @param actualizedAt    ISO date — last human curation review
 * @param seeAlso         set of slugs referenced from this entity
 * @param sections        map of H2 section name → raw markdown body
 *                        (key set: Summary, Purpose, Fields, Relations,
 *                        API, API (deprecated), Examples, Dev notes)
 * @param markdownSource  classpath-relative path (e.g. "docs/glossary/entities/twin.md")
 * @param markdownHash    SHA-256 hex of file bytes, computed at parse time
 * @param twinId          deterministic UUIDv5 from ("glossary:" + slug)
 */
public record GlossaryEntityDto(
        String slug,
        String title,
        String category,
        String jpaClass,
        String dbTable,
        boolean isSystem,
        LocalDate actualizedAt,
        Set<String> seeAlso,
        Map<String, String> sections,
        String markdownSource,
        String markdownHash,
        UUID twinId
) {
    public static UUID computeTwinId(String slug) {
        return UUID.nameUUIDFromBytes(("glossary:" + slug).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
```

### 15.3 GlossaryBootstrapService

```java
package org.twins.bootstrap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GlossaryBootstrapService {

    final GlossaryMarkdownParser parser;
    final TwinService twinService;                  // primary — handles TwinEntity + TwinFields + links + tags + search index
    final TwinRepository twinRepository;            // for hash-based lookup by ID
    final TwinClassRepository twinClassRepository;  // for classId resolution

    /**
     * Two-phase pipeline:
     *   DISCOVERY (in-memory, in tx) — parse all .md, classify each dto against DB state
     *   EXECUTE (in tx, batched)     — TwinService.createTwin / updateTwin with full payload
     *
     * Single @Transactional — all-or-nothing for the batch. Invalid .md files are
     * dropped DURING DISCOVERY (logged WARN), so they don't abort the whole pass.
     */
    @Transactional(rollbackFor = Throwable.class)
    public GlossaryBootstrapResult bootstrap() throws ServiceException;

    // ─── DISCOVERY ──────────────────────────────────────────────
    // parser.parseAll() → List<GlossaryEntityDto> (valid only)
    // load all existing glossary Twins by twinClassId = TWIN_CLASS_TWINS_GLOSSARY
    //   (single SELECT — returns Map<twinId, TwinEntity> for classification)
    // classify each dto + each existing Twin:
    //   - dto not in DB                              → action CREATE
    //   - dto in DB, hash matches, status = ACTUAL   → action SKIP
    //   - dto in DB, hash differs                    → action UPDATE
    //   - dto in DB, hash matches, status = DELETED  → action RESTORE (subset of UPDATE)
    //   - existing Twin, .md gone, status = ACTUAL   → action MARK_DELETED
    // returns BootstrapPlan { creates, updates, restores, markDeletes, skips }
    private BootstrapPlan discover(List<GlossaryEntityDto> dtos, UUID glossaryClassId);

    // ─── EXECUTE ────────────────────────────────────────────────
    // For each CREATE:
    //   build TwinCreate
    //     .setTwinEntity(new TwinEntity()
    //         .setId(dto.twinId())                                    // deterministic UUIDv5
    //         .setName(dto.title())
    //         .setTwinClassId(TWIN_CLASS_TWINS_GLOSSARY)
    //         .setTwinStatusId(TWIN_STATUS_GLOSSARY_ACTUAL)
    //         .setDescription(dto.sections().get("Summary"))          // base field, no TwinClassField
    //         .setExternalId("glossary:" + dto.slug()))
    //     .setFields(buildFieldMap(dto))                             // Map<fieldUUID, FieldValue> for 13 fields
    //     .setLinksEntityList(buildSeeAlsoLinks(dto))                // List<TwinLinkEntity> with dst = target UUIDv5
    //     .setTagsAddExisted(Set.of(categoryDataListOptionId))       // tag from GLOSSARY_CATEGORY
    //     .setCheckCreatePermission(false)                           // system bootstrap
    //   collect for batch
    // For each UPDATE / RESTORE:
    //   build TwinUpdate
    //     .setDbTwinEntity(existingTwin)                              // snapshot from DB
    //     .setTwinEntity(existingTwin.copy()                          // new values
    //         .setDescription(dto.sections().get("Summary"))
    //         .setTwinStatusId(TWIN_STATUS_GLOSSARY_ACTUAL))          // RESTORE: from DELETED → ACTUAL
    //     .setFields(buildFieldMap(dto))                              // full refresh — idempotent
    //     .setTwinLinkCUD(computeLinkCUD(existingLinks, dto.seeAlso)) // EntityCUD: add new, remove stale
    //     .setTagsAddExisted(...) / setTagsDelete(...)                // category change if any
    //     .setCheckEditPermission(false)
    // For each MARK_DELETED:
    //   build TwinUpdate
    //     .setDbTwinEntity(existingTwin)
    //     .setTwinEntity(existingTwin.copy().setTwinStatusId(TWIN_STATUS_GLOSSARY_DELETED))
    //     .setCheckEditPermission(false)
    //
    // Batched execution:
    //   twinService.createTwins(TwinCreateStage.of(creates))   // sync batch, internally splits on stages for see_also forward refs
    //   twinService.updateTwin(updates, false)                 // sync batch (List<TwinUpdate>, validateAll=false)
    private void execute(BootstrapPlan plan);
}
```

**Why TwinService instead of `entitySmartService.save()` directly:**

`TwinService.createTwin(TwinCreate)` and `updateTwin(TwinUpdate)` do work that we'd otherwise have to reimplement:

1. **TwinFields typecast** — each field type (`FieldTyperTextField`=1301, `FieldTyperTextNonIndexedField`=1336, `FieldTyperBooleanV1`=1306, `FieldTyperTimestamp`=1302) has its own JPA entity class and repository. TwinService routes via `initFields()` + `saveTwinFields()` through `TwinChangesCollector`.
2. **Search index update** — `twinChangesService.applyChanges()` updates the search index after TwinField writes, so glossary Twins are findable via `/private/twin/search/v1` predicates (`twinfield_jpa_class_text`, etc.) without manual reindexing.
3. **GLOBAL_ANCESTOR inheritance** — glossary Twins must inherit `name`/`description`/`externalId` from `GLOBAL_ANCESTOR`. TwinService ensures inheritance is respected; direct save can silently skip it.
4. **Validation** — `validateAndCollect()` enforces field-level constraints (required, format) before commit, surfacing markdown content bugs at bootstrap time rather than runtime.
5. **History** — `twinChangesService` records creation/update events. Free audit trail of glossary curation.

The "performance overhead" argument for bypass is weak: hash-skip means most startups do zero CREATE/UPDATE calls. The 1-2s overhead is real only on first boot or after markdown changes — acceptable cost for correctness.

`checkCreatePermission = false` / `checkEditPermission = false` are the default in `TwinCreate` / `TwinUpdate`, so the system-bootstrap context bypasses permission checks automatically (we're running as the system, not a user).

### 15.4 GlossaryBootstrapRunner

```java
package org.twins.bootstrap;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "twins.glossary.bootstrap",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class GlossaryBootstrapRunner implements ApplicationListener<ApplicationReadyEvent> {

    final GlossaryBootstrapService bootstrapService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            GlossaryBootstrapResult result = bootstrapService.bootstrap();
            log.info("Glossary bootstrap: created={}, updated={}, skipped={}, links_added={}, links_removed={}, orphans_marked_deleted={}, orphans_restored={}",
                    result.created(), result.updated(), result.skipped(),
                    result.linksAdded(), result.linksRemoved(),
                    result.orphansMarkedDeleted(), result.orphansRestored());
        } catch (Exception e) {
            log.error("Glossary bootstrap failed — glossary may be incomplete", e);
            // intentionally swallow — does not abort application startup
        }
    }
}
```

### 15.5 GlossaryBootstrapResult

```java
public record GlossaryBootstrapResult(
        int created,
        int updated,
        int skipped,
        int linksAdded,
        int linksRemoved,
        int orphansMarkedDeleted,  // Twins transitioned ACTUAL → DELETED (PHASE 2 MARK_DELETED action)
        int orphansRestored,       // Twins transitioned DELETED → ACTUAL (PHASE 2 RESTORE action, .md reappeared)
        List<String> invalidFiles  // filenames that were dropped during parse
) {}
```

### 15.6 SystemEntityService constants

All UUIDs referenced by the migration and by `GlossaryBootstrapService` are registered as `public static final UUID` constants in `core/src/main/java/org/twins/core/service/SystemEntityService.java`, following the existing pattern at lines 54-137. The migration INSERTs these literal UUIDs; the bootstrap service reads them via the constants. UUID ranges chosen to avoid collisions with existing system classes (USER=`0001-...-0001`, BUSINESS_ACCOUNT=`0001-...-0003`, GLOBAL_ANCESTOR=`0001-...-0004`, FACE_PAGE=`0001-...-0100`).

```java
// last type.id = 0015 → +1 for TWINS_GLOSSARY
public static final UUID TWIN_CLASS_TWINS_GLOSSARY = UUID.fromString("00000000-0000-0000-0001-000000000006");

// last field.id = 16 → +34 reserved for glossary fields (50..63, leaves gap for future system fields)
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_PURPOSE            = UUID.fromString("00000000-0000-0000-0011-000000001000");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_FIELDS             = UUID.fromString("00000000-0000-0000-0011-000000001001");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_RELATIONS_OVERVIEW = UUID.fromString("00000000-0000-0000-0011-000000001002");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_API                = UUID.fromString("00000000-0000-0000-0011-000000001003");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_API_DEPRECATED     = UUID.fromString("00000000-0000-0000-0011-000000001004");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_EXAMPLES           = UUID.fromString("00000000-0000-0000-0011-000000001005");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_DEV_NOTES          = UUID.fromString("00000000-0000-0000-0011-000000001006");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_JPA_CLASS          = UUID.fromString("00000000-0000-0000-0011-000000001007");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_DB_TABLE           = UUID.fromString("00000000-0000-0000-0011-000000001008");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_MARKDOWN_SOURCE    = UUID.fromString("00000000-0000-0000-0011-000000001009");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_MARKDOWN_HASH      = UUID.fromString("00000000-0000-0000-0011-000000001010");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_IS_SYSTEM          = UUID.fromString("00000000-0000-0000-0011-000000001011");
public static final UUID TWIN_CLASS_FIELD_GLOSSARY_ACTUALIZED_AT      = UUID.fromString("00000000-0000-0000-0011-000000001012");

// last i18.id = 56 → +4 reserved for glossary status name/description pairs (57..60)
public static final UUID I18N_GLOSSARY_STATUS_ACTUAL_NAME        = UUID.fromString("00000000-0000-0000-0012-000000000039");
public static final UUID I18N_GLOSSARY_STATUS_ACTUAL_DESCRIPTION = UUID.fromString("00000000-0000-0000-0012-000000000040");
public static final UUID I18N_GLOSSARY_STATUS_DELETED_NAME       = UUID.fromString("00000000-0000-0000-0012-000000000041");
public static final UUID I18N_GLOSSARY_STATUS_DELETED_DESCRIPTION= UUID.fromString("00000000-0000-0000-0012-000000000042");

// last status.id = 0015 → +2 for glossary (FACE_PAGE was the last system one)
public static final UUID TWIN_STATUS_GLOSSARY_ACTUAL  = UUID.fromString("00000000-0000-0000-0003-000000001001");
public static final UUID TWIN_STATUS_GLOSSARY_DELETED = UUID.fromString("00000000-0000-0000-0003-000000001002");

// Link type for cross-references between glossary Twins
public static final UUID LINK_GLOSSARY_SEE_ALSO = UUID.fromString("00000000-0000-0000-0019-000000000001");
```

**Naming convention notes:**

- TwinClass UUID range extended from `0001-...-0005` (FACE_PAGE) → `0001-...-0006` for TWINS_GLOSSARY.
- TwinClassField UUIDs use a new gap at `0011-...-1001..1012` (existing system fields stop at `...0016`) to leave room for future non-glossary system fields.
- TwinStatus UUIDs extend `0003-...-0004` (FACE_PAGE) → `0003-...-1001, ...-1002`.
- i18n UUIDs extend `0012-...-0038` → `0012-...-0039..0042` (statuses) and `0012-...-0043, ...-0044` (link forward/backward names — not registered as constants since the bootstrap service doesn't reference them).
- Link UUID `LINK_GLOSSARY_SEE_ALSO` is registered as a constant for code-side referencing (e.g. lookups, debugging, future direct queries).
- DataList and DataListOption UUIDs are **not** registered in SystemEntityService — the bootstrap service resolves options by slug (`core`, `workflow`, etc.) via the DataList name, not by UUID. They live only in the migration file.

---

## 16. Sequence Flow (2-phase: DISCOVERY → EXECUTE)

```
ApplicationReadyEvent fires
  │
  ▼
GlossaryBootstrapRunner.onApplicationEvent
  │  (no transaction yet — outside @Transactional)
  ▼
GlossaryBootstrapService.bootstrap()   ← @Transactional BEGINS
  │
  │  ─── PHASE 1: DISCOVERY (in-memory, no DB writes) ─────────
  │
  │  parser.parseAll()
  │    ├─ for each /docs/glossary/entities/*.md on classpath:
  │    │    - read bytes → sha256 → markdownHash
  │    │    - split frontmatter / body
  │    │    - parse YAML via jackson-dataformat-yaml → identity
  │    │    - split body by H2 headers → sections map
  │    │    - validate: slug regex, required sections, see_also format
  │    │    - on failure → log WARN with filename + reason, drop from list
  │    └─ returns List<GlossaryEntityDto> (valid only)
  │
  │  resolve glossaryClassId via twinClassRepository
  │    .findByDomainIdAndKey(null, "TWINS_GLOSSARY")  // null domainId — system class
  │
  │  load all existing glossary Twins:
  │    SELECT * FROM twin WHERE twin_class_id = glossaryClassId
  │    → Map<twinId, TwinEntity>  (used for hash + status comparison)
  │
  │  classify each DTO against DB state:
  │    ├─ dto.twinId NOT in DB                  → action CREATE
  │    ├─ in DB, hash matches, status = ACTUAL  → action SKIP
  │    ├─ in DB, hash differs                   → action UPDATE
  │    └─ in DB, hash matches, status = DELETED → action RESTORE (subset of UPDATE)
  │
  │  classify each existing Twin with no matching .md:
  │    └─ existing, no .md, status = ACTUAL     → action MARK_DELETED
  │  (existing Twins with status = DELETED and no .md → no-op, idempotent)
  │
  │  → BootstrapPlan { creates[], updates[], restores[], markDeletes[], skips }
  │
  │  ─── PHASE 2: EXECUTE (TwinService batched) ────────────────
  │
  │  For each CREATE — build TwinCreate:
  │    .setTwinEntity(TwinEntity with
  │       id = dto.twinId (deterministic UUIDv5),
  │       name = dto.title,
  │       twinClassId = TWIN_CLASS_TWINS_GLOSSARY,
  │       twinStatusId = TWIN_STATUS_GLOSSARY_ACTUAL,
  │       description = dto.sections["Summary"],
  │       externalId = "glossary:" + dto.slug)
  │    .setFields(buildFieldMap(dto))              // Map<UUID, FieldValue> for 13 fields
  │    .setLinksEntityList(buildSeeAlsoLinks(dto)) // TwinLinkEntity list with dst=UUIDv5 per see_also slug
  │    .setTagsAddExisted({categoryId})            // resolved from GLOSSARY_CATEGORY DataListOption
  │    .setCheckCreatePermission(false)            // system bootstrap bypass
  │
  │  For each UPDATE / RESTORE — build TwinUpdate:
  │    .setDbTwinEntity(existingTwin)              // snapshot loaded from DB
  │    .setTwinEntity(existingTwin.copy()
  │       .setDescription(dto.sections["Summary"])
  │       .setTwinStatusId(TWIN_STATUS_GLOSSARY_ACTUAL))   // RESTORE: DELETED → ACTUAL
  │    .setFields(buildFieldMap(dto))              // full refresh — idempotent
  │    .setTwinLinkCUD(computeCUD(                 // EntityCUD<TwinLinkEntity>
  │       existingLinks = loadLinks(existingTwin.id),
  │       desiredLinks = dto.seeAlso))
  │    .setTagsAddExisted(...) / setTagsDelete(...)  // only if category changed
  │    .setCheckEditPermission(false)
  │
  │  For each MARK_DELETED — build TwinUpdate:
  │    .setDbTwinEntity(orphanTwin)
  │    .setTwinEntity(orphanTwin.copy()
  │       .setTwinStatusId(TWIN_STATUS_GLOSSARY_DELETED))
  │    .setCheckEditPermission(false)
  │
  │  Execute batched:
  │    1. twinService.createTwins(TwinCreateStage.of(creates))
  │       (internally splits on stages to resolve see_also forward references)
  │    2. twinService.updateTwin(allUpdates, false)
  │       where allUpdates = updates + restores + markDeletes (each as TwinUpdate)
  │
  │  → counts: created.size, updated.size, restored.size,
  │            markDeletes.size, skips.size
  │
  │  return GlossaryBootstrapResult(...)
  │
  ▼
@Transactional COMMITS (or rolls back on DB error — entire batch)
  │
  ▼
Runner logs summary, returns. Application startup continues.
```

**Transaction boundary rules:**

1. **Single `@Transactional` on `bootstrap()`** — entire DISCOVERY + EXECUTE is one tx. DB error anywhere rolls back the whole batch.
2. **Invalid files excluded pre-execute** — parsing & validation happens in DISCOVERY (in-memory only). Broken `.md` is logged WARN and dropped before any DB write, so it doesn't abort the pass.
3. **TwinService handles per-twin transaction internally** — both `createTwin` and `updateTwin` are themselves `@Transactional`. Our outer transaction composes cleanly (Spring joins them via propagation REQUIRED).
4. **Batched execution** — single `createTwins(TwinCreateStage)` call for all CREATEs, single `updateTwin(List<TwinUpdate>)` for all UPDATE+RESTORE+MARK_DELETED. TwinService internally batches the SQL.
5. **Failure mode** — DB constraint violation aborts the tx. The runner catches, logs, and **does not crash the app**. Glossary may be in pre-bootstrap state until next restart with a fix.

**Why not 4 separate passes anymore:** `TwinCreate.setLinksEntityList` + `setTagsAddExisted` covers link/tag creation in the same call as Twin creation. `TwinUpdate.setTwinLinkCUD` + `setTagsAddExisted/Delete` covers link/tag reconciliation in the same call as Twin update. No need for separate Pass 2 (links) or Pass 3 (tags) — they fold into CREATE/UPDATE.

---

## 17. Flyway Migration Skeleton

**File:** `core/src/main/resources/db/migration/V1.4.100.01__TWINS-854_glossary_class.sql`

All UUIDs below are registered as constants in `SystemEntityService` (see §15.6). The migration INSERTs the literal UUID values; the bootstrap service references them via the constants. Glossary Twins themselves are **not** created by the migration — they are created at app startup by `GlossaryBootstrapService`.

Schema conventions (verified against existing migrations):
- `i18n` columns: `(id, name, key, i18n_type_id, domain_id)`; `i18n_type_id` is lowercase string FK (`'twinStatusName'`, `'linkForwardName'`, …).
- `i18n_translation` columns: `(i18n_id, locale, translation, usage_counter)`.
- `twin_status` columns: `(id, twins_class_id, name_i18n_id, description_i18n_id, logo, background_color, key, font_color)` — note `twins_class_id` (with `s`).
- `twin_class_owner_type_id` value: `'system'` (lowercase) — system class marker, `domain_id` is `null` for system classes.
- `link_type_id` value: `'ManyToMany'` (CamelCase as in `org.twins.core.enums.link.LinkType`).
- `data_list_option_status_id` value: `'active'` (lowercase) — enum `'active'|'disabled'|'hidden'`.
- All INSERTs use `on conflict (...) do nothing` for idempotency (re-runnable migrations).

```sql
-- TWINS-854: Glossary-as-Twins — bootstrap class schema.
-- Creates: TWINS_GLOSSARY TwinClass + 13 TwinClassFields + 2 TwinStatuses
--          (ACTUAL, DELETED) + 4 i18n rows (statuses) + 2 i18n rows (link names)
--          + 1 link type (GLOSSARY_SEE_ALSO) + 1 DataList (GLOSSARY_CATEGORY)
--          with 9 options.
-- Glossary Twins are bootstrapped at app startup by GlossaryBootstrapService.

-- 1. TwinClass: TWINS_GLOSSARY
--    UUID: SystemEntityService.TWIN_CLASS_TWINS_GLOSSARY
--    domain_id = null (system class — no tenant domain)
INSERT INTO public.twin_class (
    id, domain_id, key, permission_schema_space, abstract,
    head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, logo,
    created_by_user_id, created_at, twin_class_owner_type_id,
    domain_alias_counter, marker_data_list_id, tag_data_list_id,
    twinflow_schema_space, twin_class_schema_space, alias_space,
    view_permission_id, head_hierarchy_tree, extends_hierarchy_tree,
    head_hunter_featurer_id, head_hunter_featurer_params,
    create_permission_id, edit_permission_id, delete_permission_id, page_face_id
) VALUES (
    '00000000-0000-0000-0001-000000000006'::uuid,         -- TWIN_CLASS_TWINS_GLOSSARY
    null::uuid,                                            -- domain_id (null for system class)
    'TWINS_GLOSSARY'::varchar(100),
    false::boolean, false::boolean,
    null::uuid, null::uuid, null::uuid, null::uuid, null::varchar,
    '00000000-0000-0000-0000-000000000000'::uuid,          -- USER_SYSTEM
    CURRENT_TIMESTAMP,
    'system'::varchar,                                     -- owner_type_id (lowercase)
    0::integer,
    null::uuid,
    '00000000-0000-0000-0020-000000000001'::uuid,          -- tag_data_list_id → GLOSSARY_CATEGORY
    false::boolean, false::boolean, false::boolean,
    null::uuid, null::ltree, null::ltree,
    null::integer, null::hstore,
    null::uuid, null::uuid, null::uuid, null::uuid
) ON CONFLICT (id) DO NOTHING;

-- 2. i18n entries — 4 rows for statuses + 2 rows for GLOSSARY_SEE_ALSO link names
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES
    -- Status ACTUAL name/description
    ('00000000-0000-0000-0012-000000000039'::uuid, null, null, 'twinStatusName'::varchar,       null::uuid),
    ('00000000-0000-0000-0012-000000000040'::uuid, null, null, 'twinStatusDescription'::varchar, null::uuid),
    -- Status DELETED name/description
    ('00000000-0000-0000-0012-000000000041'::uuid, null, null, 'twinStatusName'::varchar,       null::uuid),
    ('00000000-0000-0000-0012-000000000042'::uuid, null, null, 'twinStatusDescription'::varchar, null::uuid),
    -- GLOSSARY_SEE_ALSO link forward/backward names
    ('00000000-0000-0000-0012-000000000043'::uuid, null, null, 'linkForwardName'::varchar,      null::uuid),
    ('00000000-0000-0000-0012-000000000044'::uuid, null, null, 'linkBackwardName'::varchar,     null::uuid)
ON CONFLICT (id) DO NOTHING;

-- 3. i18n English translations (usage_counter = 0, following SystemEntityService pattern)
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('00000000-0000-0000-0012-000000000039'::uuid, 'en', 'Actual',                                                                       0),
    ('00000000-0000-0000-0012-000000000040'::uuid, 'en', 'Glossary entry is in sync with its markdown source file',                       0),
    ('00000000-0000-0000-0012-000000000041'::uuid, 'en', 'Deleted',                                                                       0),
    ('00000000-0000-0000-0012-000000000042'::uuid, 'en', 'Source markdown file removed; Twin retained for referential integrity',           0),
    ('00000000-0000-0000-0012-000000000043'::uuid, 'en', 'See also',                                                                      0),
    ('00000000-0000-0000-0012-000000000044'::uuid, 'en', 'Referenced by',                                                                 0)
ON CONFLICT (i18n_id, locale) DO NOTHING;

-- 4. TwinStatus: ACTUAL + DELETED
--    UUIDs: SystemEntityService.TWIN_STATUS_GLOSSARY_ACTUAL / _DELETED
INSERT INTO public.twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, logo, background_color, key, font_color) VALUES
    ('00000000-0000-0000-0003-000000001001'::uuid,         -- TWIN_STATUS_GLOSSARY_ACTUAL
     '00000000-0000-0000-0001-000000000006'::uuid,         -- TWIN_CLASS_TWINS_GLOSSARY
     '00000000-0000-0000-0012-000000000039'::uuid,
     '00000000-0000-0000-0012-000000000040'::uuid,
     null::varchar, null::varchar, 'ACTUAL'::varchar, null::varchar),
    ('00000000-0000-0000-0003-000000001002'::uuid,         -- TWIN_STATUS_GLOSSARY_DELETED
     '00000000-0000-0000-0001-000000000006'::uuid,
     '00000000-0000-0000-0012-000000000041'::uuid,
     '00000000-0000-0000-0012-000000000042'::uuid,
     null::varchar, null::varchar, 'DELETED'::varchar, null::varchar)
ON CONFLICT (id) DO NOTHING;

-- 5. TwinClassFields — see plan §4 for the field schema.
--    Field typer featurer IDs (verified in FeaturerTwins.java):
--      1301 = FieldTyperTextField (indexed, short searchable)
--      1336 = FieldTyperTextNonIndexedField (long markdown bodies)
--      1306 = FieldTyperBooleanV1
--      1302 = FieldTyperTimestamp
--    name_i18n_id and description_i18n_id are nullable — left null for MVP
--    (field keys are self-describing; admin UI can show key directly).
--    NOTE: ## Summary is stored on Twin.description (inherited base field from
--    GLOBAL_ANCESTOR via SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION)
--    so no TwinClassField is created for it.

-- 5a. Long-text section fields (non-indexed, featurer 1336)
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES
    ('00000000-0000-0000-0011-000000001000'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'purpose'::varchar,            null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001001'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'fields'::varchar,             null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, true),
    ('00000000-0000-0000-0011-000000001002'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'relations_overview'::varchar, null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001003'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'api'::varchar,                null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001004'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'api_deprecated'::varchar,     null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001005'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'examples'::varchar,           null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001006'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'dev_notes'::varchar,          null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false)
ON CONFLICT (id) DO NOTHING;

-- 5b. Short indexed-text fields (featurer 1301)
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES
    ('00000000-0000-0000-0011-000000001007'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'jpa_class'::varchar,       null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001008'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'db_table'::varchar,        null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000001009'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'markdown_source'::varchar, null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, true),
    ('00000000-0000-0000-0011-000000001010'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'markdown_hash'::varchar,   null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, true)
ON CONFLICT (id) DO NOTHING;

-- 5c. Boolean + date fields
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES
    ('00000000-0000-0000-0011-000000001011'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'is_system'::varchar,     null::uuid, null::uuid, 1306, null::hstore, null::uuid, null::uuid, true),
    ('00000000-0000-0000-0011-000000001012'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'actualized_at'::varchar, null::uuid, null::uuid, 1302, null::hstore, null::uuid, null::uuid, true)
ON CONFLICT (id) DO NOTHING;

-- 6. Link type: GLOSSARY_SEE_ALSO (glossary Twin → glossary Twin, many-to-many)
--    UUID: SystemEntityService.LINK_GLOSSARY_SEE_ALSO
INSERT INTO public.link (
    id, domain_id, src_twin_class_id, dst_twin_class_id,
    forward_name_i18n_id, backward_name_i18n_id, link_type_id,
    link_strength_id, created_by_user_id, created_at
) VALUES (
    '00000000-0000-0000-0019-000000000001'::uuid,
    null::uuid,                                            -- domain_id (null — system class)
    '00000000-0000-0000-0001-000000000006'::uuid,          -- TWINS_GLOSSARY → TWINS_GLOSSARY
    '00000000-0000-0000-0001-000000000006'::uuid,
    '00000000-0000-0000-0012-000000000043'::uuid,          -- "See also" (forward)
    '00000000-0000-0000-0012-000000000044'::uuid,          -- "Referenced by" (backward)
    'ManyToMany'::varchar,
    'OPTIONAL'::varchar,
    '00000000-0000-0000-0000-000000000000'::uuid,          -- USER_SYSTEM
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- 7. DataList: GLOSSARY_CATEGORY (used as TwinTag source for categorizing glossary Twins)
INSERT INTO public.data_list (id, name, description, updated_at) VALUES
    ('00000000-0000-0000-0020-000000000001'::uuid, 'GLOSSARY_CATEGORY', 'Categories for glossary entries', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.data_list_option (id, data_list_id, option, data_list_option_status_id, "order") VALUES
    ('00000000-0000-0020-0001-000000000001'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'core',            'active', 1),
    ('00000000-0000-0020-0001-000000000002'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'workflow',        'active', 2),
    ('00000000-0000-0020-0001-000000000003'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'multi-tenancy',   'active', 3),
    ('00000000-0000-0020-0001-000000000004'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'permissions',     'active', 4),
    ('00000000-0000-0020-0001-000000000005'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'content',         'active', 5),
    ('00000000-0000-0020-0001-000000000006'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'cross-cutting',   'active', 6),
    ('00000000-0000-0020-0001-000000000007'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'fields',          'active', 7),
    ('00000000-0000-0020-0001-000000000008'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'validation',      'active', 8),
    ('00000000-0000-0020-0001-000000000009'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'other',           'active', 9)
ON CONFLICT (id) DO NOTHING;

-- 8. Index FK columns (CLAUDE.md rule — every FK gets an index)
CREATE INDEX IF NOT EXISTS idx_twin_class_field_twin_class_id
    ON public.twin_class_field (twin_class_id);
CREATE INDEX IF NOT EXISTS idx_twin_status_twins_class_id
    ON public.twin_status (twins_class_id);
CREATE INDEX IF NOT EXISTS idx_link_src_twin_class_id
    ON public.link (src_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_link_dst_twin_class_id
    ON public.link (dst_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_data_list_option_data_list_id
    ON public.data_list_option (data_list_id);
```

**Status:** all design decisions resolved — migration is ready for implementation. Verified against:
- `V1.3.98.01__TWINS_init.sql` (initial schema + seed tables `link_type`, `link_strength`, `data_list_option_status`, `twin_class_owner_type`)
- `V1.3.216.01__TWINS-362_ak1_globa_ancestor_class.sql` (reference pattern for system TwinClass + TwinClassField)
- `V1.3.316.02__TWINS-438_kk1_add_sketch_status.sql` (reference pattern for TwinStatus + i18n + i18n_translation)
- `core/.../enums/i18n/I18nType.java` (i18n_type_id lowercase values)
- `core/.../enums/link/LinkType.java` (link_type_id enum values: `ManyToOne`, `ManyToMany`, `OneToOne`)

---

## 18. Test Plan

All tests live under `core/src/test/java/org/twins/bootstrap/` and use the existing Testcontainers PostgreSQL fixture (see `TwinServiceIntegrationTest` for the pattern).

### 18.1 Unit tests (no Spring context)

| Test | Validates |
|---|---|
| `GlossaryMarkdownParserTest.parse_minimalValidFile_returnsDto` | Happy path: frontmatter + H2 sections → DTO with all fields populated |
| `GlossaryMarkdownParserTest.parse_missingFrontmatter_throws` | File without `---` block → `GlossaryParseException` |
| `GlossaryMarkdownParserTest.parse_missingRequiredSection_throws` | Body without `## Summary` or `## Fields` → exception |
| `GlossaryMarkdownParserTest.parse_unknownH2Section_logsWarningSkips` | Body with `## Custom Section` → not in DTO, WARN logged |
| `GlossaryMarkdownParserTest.parse_invalidSlug_throws` | `slug: Foo Bar` (uppercase + space) → exception |
| `GlossaryMarkdownParserTest.parse_invalidCategory_throws` | `category: unknown` → exception |
| `GlossaryMarkdownParserTest.parse_actualizedAtInvalidIsoDate_throws` | `actualized_at: 17/06/2026` → exception |
| `GlossaryMarkdownParserTest.parse_crlfLineEndings_handledCorrectly` | File saved on Windows → no parsing errors |
| `GlossaryMarkdownParserTest.parse_utf8Bom_stripped` | File starting with BOM → frontmatter parses |
| `GlossaryMarkdownParserTest.parseAll_oneInvalidFile_skipsInvalidKeepsValid` | Mixed dir → invalid dropped, valid returned |
| `GlossaryEntityDtoTest.computeTwinId_deterministicForSameSlug` | Same slug → same UUID across calls |

### 18.2 Integration tests (Spring context + PostgreSQL Testcontainer)

Tests run with `twins.glossary.bootstrap.enabled=true` so the runner fires. Each test method can additionally invoke `bootstrapService.bootstrap()` directly for re-runs.

| Test | Validates |
|---|---|
| `GlossaryBootstrapIntegrationTest.bootstrap_emptyDb_createsAllTwins` | Run on empty DB with 3 markdown files → 3 Twins created. `## Summary` body lands in `Twin.description`; remaining sections land in matching TwinFields; all new Twins start in `ACTUAL` status. |
| `GlossaryBootstrapIntegrationTest.bootstrap_reRunWithNoChanges_skipsAll` | Second run with same files → 0 created, 0 updated, 3 skipped (hash match) |
| `GlossaryBootstrapIntegrationTest.bootstrap_markdownChanged_updatesFields` | Edit `## Summary` in one file → 1 Twin updated, hash rotated |
| `GlossaryBootstrapIntegrationTest.bootstrap_newFileAdded_createsNewTwin` | Add 4th file → 1 created on next run, status `ACTUAL` |
| `GlossaryBootstrapIntegrationTest.bootstrap_fileRemoved_marksOrphanDeleted` | Remove one file → orphan Twin's status transitions ACTUAL → DELETED. Twin still returned by standard search (DELETED not excluded). |
| `GlossaryBootstrapIntegrationTest.bootstrap_orphanRestored_resetsStatusToActual` | Re-add the removed file → Twin's status transitions DELETED → ACTUAL (PHASE 2 RESTORE action), fields refreshed |
| `GlossaryBootstrapIntegrationTest.bootstrap_seeAlsoChanged_reconcilesLinks` | Add/remove entry in frontmatter `see_also` → TwinLink added/removed |
| `GlossaryBootstrapIntegrationTest.bootstrap_categoryChanged_reconcilesTag` | Change `category` from `core` to `workflow` → old tag removed, new tag added |
| `GlossaryBootstrapIntegrationTest.bootstrap_invalidFilePresent_loadsOthersOnly` | Mix 2 valid + 1 invalid file → 2 Twins created, invalid logged, no crash |
| `GlossaryBootstrapIntegrationTest.bootstrap_conditionalPropertyDisabled_runnerDoesNotFire` | Set `twins.glossary.bootstrap.enabled=false` → bean not in context, no Twins created |
| `GlossaryBootstrapIntegrationTest.bootstrap_deterministicUuid_sameAcrossRestarts` | UUIDs of Twins stable across two runs of bootstrap |
| `GlossaryBootstrapIntegrationTest.bootstrap_deletedTwinVisibleInStandardSearch` | Twin in DELETED status is returned by `/private/twin/search/v1` (no implicit filtering) |

### 18.3 Performance check (optional, in `@Tag("slow")`)

| Test | Validates |
|---|---|
| `GlossaryBootstrapPerformanceTest.bootstrap_40files_completesUnder500ms` | With 40 sample markdown files → bootstrap pass under 500ms after JVM warmup |

### 18.4 Test fixtures

- `core/src/test/resources/glossary-fixtures/` — sample markdown files covering all validation paths.
- `GlossaryTestFixtures.java` — builders for `GlossaryEntityDto` in unit tests (no I/O needed).
- `application-test.properties` overrides: `twins.glossary.bootstrap.enabled=false` for the **majority** of existing tests (so they're not slowed by the runner); only the glossary-specific tests opt back in via `@TestPropertySource`.

### 18.5 Definition of done

Phase 2 ships when:

1. All `GlossaryMarkdownParserTest` unit tests pass.
2. All `GlossaryBootstrapIntegrationTest` tests pass against PostgreSQL Testcontainer.
3. Manual smoke: run `./gradlew bootRun` locally with 3 fixture markdown files → log shows `created=3, updated=0, skipped=0`.
4. Manual smoke: restart without changes → log shows `created=0, updated=0, skipped=3` (hash-skip path works).
5. The remaining ~27 entity files (40 − 13 already drafted) are converted from `docs/glossary.md` — this is content work, may ship as a follow-up PR.
