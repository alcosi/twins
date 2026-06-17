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
  bootstrap/
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

**Removed from earlier draft:**

| Removed | Why |
|---|---|
| `package` | Derivable from `class` lookup; not displayed |
| `aliases` | Low value; full-text search on summary covers recall |
| `key_fields[]` with types | Too verbose, hard to maintain, duplicates JPA source |
| `relations[]` with cardinality/kind/description | Moved to body prose — narrative is enough; structured link only via `see_also` |

**Added:**

| Added | Why |
|---|---|
| `actualized_at` | Curation signal — operators see when the entry was last reviewed. Encourages periodic refresh; stale entries (>180 days) flagged in admin UI. |

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
Higher-level narrative: "Belongs to TwinClass, owns many TwinLinks, has optional
hierarchy via headTwinId, hosts Twinflow for state machine, …". Complements ## Fields.

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

**Section-to-field mapping** (each section maps to a `TWINS_GLOSSARY` field — see §4):

| Body section | TwinClass field | Required | Admin UI tier |
|---|---|---|---|
| `## Summary` | `summary` | yes | always visible (card preview) |
| `## Purpose` | `purpose` | no | default open |
| `## Fields` | `fields` | yes | default open |
| `## Relations` | `relations_overview` | no | default open |
| `## API` | `api` | no | default open |
| `## API (deprecated)` | `api_deprecated` | no | collapsible (deprecated badge) |
| `## Examples` | `examples` | no | collapsible |
| `## Dev notes` | `dev_notes` | no | **hidden** (developer mode toggle) |

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

The glossary Twins live in a single TwinClass with key `TWINS_GLOSSARY`. Each field in this class corresponds to a section of the source markdown — see §3.2 for the mapping.

### Fields

| Field key | Field type | Required | Source | Admin UI tier |
|---|---|---|---|---|
| `category` | DataList option (`GLOSSARY_CATEGORY`) | yes | frontmatter `category` | always visible (chip) |
| `actualized_at` | date | yes | frontmatter `actualized_at` | always visible (meta line) |
| `summary` | long text | yes | body `## Summary` | always visible (card preview) |
| `purpose` | long text | no | body `## Purpose` | default open |
| `fields` | long text (markdown) | yes | body `## Fields` | default open |
| `relations_overview` | long text | no | body `## Relations` | default open |
| `api` | long text (markdown) | no | body `## API` | default open |
| `api_deprecated` | long text (markdown) | no | body `## API (deprecated)` | collapsible (deprecated badge) |
| `examples` | long text (markdown) | no | body `## Examples` | collapsible |
| `dev_notes` | long text (markdown) | no | body `## Dev notes` | **hidden** (developer mode only) |
| `jpa_class` | simple string | no | frontmatter `class` | developer-only |
| `db_table` | simple string | no | frontmatter `table` | developer-only |
| `is_system` | boolean | yes | frontmatter `is_system` | developer-only (chip) |
| `markdown_source` | simple string | yes (auto) | computed at bootstrap | developer-only |
| `markdown_hash` | simple string | yes (auto) | sha256 of source content | hidden (internal) |

**Markdown rendering** — `fields`, `api`, `api_deprecated`, `examples`, `dev_notes` are stored as raw markdown. Admin UI renders them with a markdown renderer. Inline links like `[TwinClass](twin-class.md)` are intercepted and routed to the corresponding glossary Twin (lookup by `externalId`), not to the file system.

**Staleness flag** — admin UI computes `days_since(actualized_at)` and badges entries older than 180 days as "stale — needs review". Threshold configurable per environment.

### Twin built-ins used

- `Twin.name` ← frontmatter `title`
- `Twin.externalId` ← `"glossary:" + slug` — used for idempotent upsert
- `Twin.description` ← NOT USED (kept empty); content lives in the structured fields above

### Tags

- `category` value as `TwinTag` via `GLOSSARY_CATEGORY` DataList — for filtering in admin UI and MCP queries

### Links

| Link key | Source → Target | Cardinality | Created from |
|---|---|---|---|
| `GLOSSARY_SEE_ALSO` | any glossary Twin → any glossary Twin | many_to_many | frontmatter `see_also[]` |

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
| File deleted | Twin marked with `deprecated` tag (manual cleanup) |
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
TWINS_GLOSSARY_MANAGE  — required to create/update glossary Twins (system only)
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
    "twinfield_jpa_class_simple_text": "TwinEntity"
  }
}
```

Returns the glossary Twin for `TwinEntity`.

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
POST /private/twin/link/search/v1
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
- The TWINS_GLOSSARY class itself is created by a Flyway migration with `key = 'TWINS_GLOSSARY'` and `domainId = <system domain>`, so its UUID is deterministic via `UUID.nameUUIDFromBytes(("TWINS_GLOSSARY" + systemDomainId).getBytes())`.
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
| Slug rename breaks incoming links | Detect dangling references in pass 2 — log warning, do not fail; the deprecated tag on the old Twin flags the issue |
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
