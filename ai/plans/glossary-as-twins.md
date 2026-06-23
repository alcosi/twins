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

The glossary Twins live in a single TwinClass with key `TWINS_GLOSSARY`. Each field in this class corresponds to a section of the source markdown — see §3.2 for the mapping.

### Fields

| Field key | Field type | Required | Source | Admin UI tier |
|---|---|---|---|---|
| `category` | DataList option (`GLOSSARY_CATEGORY`) | yes | frontmatter `category` | always visible (chip) |
| `actualized_at` | date | yes | frontmatter `actualized_at` | always visible (meta line) |
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

**Note:** the `## Summary` body section is stored on the base `Twin.description` field (base entity field), not as a separate TwinClass field. This avoids duplicating a long-text column that already exists on every Twin.

**Markdown rendering** — `fields`, `api`, `api_deprecated`, `examples`, `dev_notes` are stored as raw markdown. Admin UI renders them with a markdown renderer. Inline links like `[TwinClass](twin-class.md)` are intercepted and routed to the corresponding glossary Twin (lookup by `externalId`), not to the file system.

**Staleness flag** — admin UI computes `days_since(actualized_at)` and badges entries older than 180 days as "stale — needs review". Threshold configurable per environment.

### Twin built-ins used

- `Twin.name` ← frontmatter `title`
- `Twin.externalId` ← `"glossary:" + slug` — used for idempotent upsert
- `Twin.description` ← body `## Summary` section (card preview text; reuses the base long-text field instead of creating a parallel TwinClass field)

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
          │   - orchestrates Pass 1-4                  │
          │   - pre-validates DTO list, drops invalid  │
          │   - returns GlossaryBootstrapResult        │
          └──┬──────────────┬─────────────┬────────────┘
             │              │             │
             ▼              ▼             ▼
   ┌─────────────────┐  ┌──────────────┐  ┌──────────────────────┐
   │ MarkdownParser  │  │ entitySmart  │  │ TwinLinkService      │
   │ - @Component    │  │ Service      │  │ TwinTagService       │
   │ - reads .md     │  │ .saveAll     │  │ (existing services)  │
   │   from disk     │  │ AndLog()     │  │                      │
   │ - parses YAML + │  │              │  │                      │
   │   H2 sections   │  │              │  │                      │
   └────────┬────────┘  └──────────────┘  └──────────────────────┘
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
package org.twins.core.service.glossary;

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
package org.twins.core.service.glossary;

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
package org.twins.core.service.glossary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GlossaryBootstrapService {

    final GlossaryMarkdownParser parser;
    final EntitySmartService entitySmartService;
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinLinkService twinLinkService;
    final TwinTagService twinTagService;

    /**
     * Run the full bootstrap pipeline (Pass 1-4).
     * Single @Transactional — all-or-nothing for the batch save,
     * but invalid files are dropped BEFORE entering the transaction
     * (so broken markdown doesn't abort the whole pass).
     */
    @Transactional(rollbackFor = Throwable.class)
    public GlossaryBootstrapResult bootstrap() throws ServiceException;

    // Pass 1: upsert all Twins (batch saveAllAndLog)
    // Pass 1: upsert all Twins (batch saveAllAndLog).
    // Each TwinEntity is built with:
    //   .setId(dto.twinId())
    //   .setName(dto.title())
    //   .setTwinClassId(glossaryClassId)
    //   .setDescription(dto.sections().get("Summary"))  // reuses base field, no TwinClass field needed
    //   .setExternalId("glossary:" + dto.slug())
    //   + TwinField rows for purpose/fields/relations_overview/api/api_deprecated/examples/dev_notes/
    //     jpa_class/db_table/markdown_source/markdown_hash/is_system/actualized_at/category
    private List<TwinEntity> upsertTwins(List<GlossaryEntityDto> dtos, UUID glossaryClassId);

    // Pass 2: reconcile GLOSSARY_SEE_ALSO TwinLinks (add new, remove stale)
    private void reconcileLinks(List<GlossaryEntityDto> dtos);

    // Pass 3: reconcile category TwinTag (set new, remove old if changed)
    private void reconcileTags(List<GlossaryEntityDto> dtos);

    // Pass 4: tag orphans (Twins whose .md no longer exists on classpath)
    private void tagOrphans(Set<String> existingSlugs);
}
```

### 15.4 GlossaryBootstrapRunner

```java
package org.twins.core.service.glossary;

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
            log.info("Glossary bootstrap: created={}, updated={}, skipped={}, links_added={}, links_removed={}, orphans={}",
                    result.created(), result.updated(), result.skipped(),
                    result.linksAdded(), result.linksRemoved(), result.orphansTagged());
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
        int orphansTagged,
        List<String> invalidFiles  // filenames that were dropped during parse
) {}
```

---

## 16. Sequence Flow (Pass 1-4 with Transaction Boundaries)

```
ApplicationReadyEvent fires
  │
  ▼
GlossaryBootstrapRunner.onApplicationEvent
  │  (no transaction yet — outside @Transactional)
  ▼
GlossaryBootstrapService.bootstrap()   ← @Transactional BEGINS
  │
  │  ─── PRE-VALIDATE PHASE (in-tx, in-memory only) ───────────
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
  │  resolve glossaryClassId via twinClassRepository.findByDomainIdAndKey(systemDomainId, "TWINS_GLOSSARY")
  │
  │  ─── PASS 1: UPSERT TWINS (single batch save) ───────────────
  │  upsertTwins(dtos, glossaryClassId):
  │    ├─ load all existing glossary Twins by id IN [dto.twinId for dto in dtos]
  │    │  (single SELECT — uses IN clause on PK)
  │    ├─ classify each dto:
  │    │    - not in DB → CREATE: build TwinEntity with name=title,
  │    │      description=sections["Summary"], externalId="glossary:"+slug,
  │    │      + 14 TwinField rows (purpose/fields/.../markdown_hash/category)
  │    │    - in DB, markdown_hash matches → SKIP (no-op)
  │    │    - in DB, hash differs → UPDATE: refresh description + TwinField rows
  │    ├─ build List<TwinEntity> for all CREATE + UPDATE
  │    └─ entitySmartService.saveAllAndLog(list, twinRepository)
  │       (ONE batch SQL — that's the performance win)
  │
  │  ─── PASS 2: RECONCILE TWINLINKS (GLOSSARY_SEE_ALSO) ──────
  │  reconcileLinks(dtos):
  │    ├─ load all existing GLOSSARY_SEE_ALSO TwinLinks where
  │    │  srcTwinId IN [dto.twinId for dto in dtos]
  │    │  (single SELECT)
  │    ├─ compute diff:
  │    │    - toAdd: (dto.twinId → seeAlsoSlug.twinId) pairs not in DB
  │    │    - toRemove: TwinLinks in DB not present in any dto.seeAlso
  │    ├─ batch INSERT new TwinLinkEntity list
  │    └─ batch DELETE stale TwinLinks
  │
  │  ─── PASS 3: RECONCILE TWIN-TAGS (category) ────────────────
  │  reconcileTags(dtos):
  │    ├─ load DataListOption for each dto.category (one query, all categories)
  │    ├─ load existing twin_tag rows for all glossary Twins
  │    ├─ classify:
  │    │    - tag already correct → SKIP
  │    │    - tag differs → remove old tag, add new tag
  │    │    - no tag yet → add new tag
  │    └─ batch INSERT/DELETE twin_tag rows
  │
  │  ─── PASS 4: ORPHAN CLEANUP ────────────────────────────────
  │  tagOrphans(existingSlugs):
  │    ├─ SELECT all glossary Twins (by twinClassId = glossaryClassId)
  │    ├─ for each Twin whose markdownSource is NOT in existingSlugs set:
  │    │    - check if already tagged GLOSSARY_DEPRECATED → skip if yes
  │    │    - else INSERT twin_tag (twin_id, GLOSSARY_DEPRECATED_TAG)
  │    └─ return orphanCount
  │
  │  return GlossaryBootstrapResult(created, updated, skipped, ...)
  │
  ▼
@Transactional COMMITS (or rolls back on DB error — entire batch)
  │
  ▼
Runner logs summary, returns. Application startup continues.
```

**Transaction boundary rules:**

1. **Single `@Transactional` on `bootstrap()`** — entire pipeline is one tx. DB error anywhere rolls back the whole batch.
2. **Invalid files excluded pre-batch** — parsing & validation happens inside the tx but is in-memory only (no DB writes). If a file is invalid, it's logged + dropped before any DB write. This preserves resilience without fragmenting the tx.
3. **All DB writes use batch APIs** — `saveAllAndLog(list)` on each repository, no per-entity save loops. Reduces round-trips from 40×N to 4 (one per pass).
4. **Failure mode** — DB constraint violation aborts the tx. The runner catches, logs, and **does not crash the app**. Glossary may be in pre-bootstrap state until next restart with a fix.

---

## 17. Flyway Migration Skeleton

**File:** `core/src/main/resources/db/migration/V1.4.100.01__TWINS-854_glossary_class.sql`

```sql
-- TWINS-854: Glossary-as-Twins — bootstrap class, fields, link, data list
-- Creates TWINS_GLOSSARY TwinClass + 15 TwinClassFields + 1 link type +
-- 1 DataList (GLOSSARY_CATEGORY) + 1 DataList (GLOSSARY_FLAGS).
-- Glossary Twins themselves are created at app startup by GlossaryBootstrapService.

-- Use deterministic UUID for the class itself so bootstrap service can find it
-- by (domainId, key) — UUID value here doesn't need to be deterministic.
-- Replace <SYSTEM_DOMAIN_ID> with the actual system-domain UUID (already bootstrapped).

-- 1. TwinClass: TWINS_GLOSSARY
INSERT INTO twin_class (
    id, domain_id, key, twin_class_owner_type_id, abstract,
    created_by_user_id, created_at, twin_class_schema_space,
    twin_counter, extends_hierarchy_counter_direct_children,
    head_hierarchy_counter_direct_children
) VALUES (
    '11111111-0000-0000-0001-000000000001',  -- TODO: replace with deterministic UUID
    '<SYSTEM_DOMAIN_ID>',
    'TWINS_GLOSSARY',
    'SYSTEM',  -- owner_type
    false,
    '00000000-0000-0000-0000-000000000000',  -- USER_SYSTEM
    CURRENT_TIMESTAMP,
    false, 0, 0, 0
);

-- 2. TwinClassFields — see plan §4 for the field schema.
--    Field typer featurer IDs (verified in FeaturerTwins.java):
--      1301 = FieldTyperTextField (indexed, short searchable)
--      1336 = FieldTyperTextNonIndexedField (long markdown bodies)
--      1306 = FieldTyperBooleanV1
--      1302 = FieldTyperTimestamp

-- 2a. Long-text section fields (non-indexed).
--    NOTE: ## Summary is stored on Twin.description (inherited base field from
--    GLOBAL_ANCESTOR via SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION)
--    so no TwinClassField is created for it.
INSERT INTO twin_class_field (id, twin_class_id, key, field_typer_featurer_id, required) VALUES
    ('11111111-0000-0000-0011-000000000102', '11111111-0000-0000-0001-000000000001', 'purpose',             1336, false),
    ('11111111-0000-0000-0011-000000000103', '11111111-0000-0000-0001-000000000001', 'fields',              1336, true),
    ('11111111-0000-0000-0011-000000000104', '11111111-0000-0000-0001-000000000001', 'relations_overview',  1336, false),
    ('11111111-0000-0000-0011-000000000105', '11111111-0000-0000-0001-000000000001', 'api',                 1336, false),
    ('11111111-0000-0000-0011-000000000106', '11111111-0000-0000-0001-000000000001', 'api_deprecated',      1336, false),
    ('11111111-0000-0000-0011-000000000107', '11111111-0000-0000-0001-000000000001', 'examples',            1336, false),
    ('11111111-0000-0000-0011-000000000108', '11111111-0000-0000-0001-000000000001', 'dev_notes',           1336, false);

-- 2b. Short indexed-text fields
INSERT INTO twin_class_field (id, twin_class_id, key, field_typer_featurer_id, required) VALUES
    ('11111111-0000-0000-0011-000000000110', '11111111-0000-0000-0001-000000000001', 'jpa_class',         1301, false),
    ('11111111-0000-0000-0011-000000000111', '11111111-0000-0000-0001-000000000001', 'db_table',          1301, false),
    ('11111111-0000-0000-0011-000000000112', '11111111-0000-0000-0001-000000000001', 'markdown_source',   1301, true),
    ('11111111-0000-0000-0011-000000000113', '11111111-0000-0000-0001-000000000001', 'markdown_hash',     1301, true);

-- 2c. Boolean + date fields
INSERT INTO twin_class_field (id, twin_class_id, key, field_typer_featurer_id, required) VALUES
    ('11111111-0000-0000-0011-000000000114', '11111111-0000-0000-0001-000000000001', 'is_system',     1306, true),
    ('11111111-0000-0000-0011-000000000115', '11111111-0000-0000-0001-000000000001', 'actualized_at', 1302, true);

-- 3. Link type: GLOSSARY_SEE_ALSO (glossary Twin → glossary Twin, many-to-many)
INSERT INTO link (
    id, domain_id, src_twin_class_id, dst_twin_class_id,
    forward_name_i18n_id, backward_name_i18n_id, link_type_id,
    link_strength_id, created_by_user_id, created_at
) VALUES (
    '22222222-0000-0000-0001-000000000001',
    '<SYSTEM_DOMAIN_ID>',
    '11111111-0000-0000-0001-000000000001',  -- TWINS_GLOSSARY → TWINS_GLOSSARY
    '11111111-0000-0000-0001-000000000001',
    '<GLOSSARY_SEE_ALSO_FWD_I18N_ID>',       -- TODO: seed i18n rows first
    '<GLOSSARY_SEE_ALSO_BWD_I18N_ID>',
    'SEE_ALSO',                              -- TODO: confirm link_type_id from enum/sead table
    'OPTIONAL',
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP
);

-- 4. DataList: GLOSSARY_CATEGORY
INSERT INTO data_list (id, name, description, updated_at) VALUES
    ('33333333-0000-0000-0001-000000000001', 'GLOSSARY_CATEGORY', 'Categories for glossary entries', CURRENT_TIMESTAMP);

INSERT INTO data_list_option (id, data_list_id, option, data_list_option_status_id, "order") VALUES
    ('33333333-0000-0001-0001-000000000001', '33333333-0000-0000-0001-000000000001', 'core',            'ACTIVE', 1),
    ('33333333-0000-0001-0001-000000000002', '33333333-0000-0000-0001-000000000001', 'workflow',        'ACTIVE', 2),
    ('33333333-0000-0001-0001-000000000003', '33333333-0000-0000-0001-000000000001', 'multi-tenancy',   'ACTIVE', 3),
    ('33333333-0000-0001-0001-000000000004', '33333333-0000-0000-0001-000000000001', 'permissions',     'ACTIVE', 4),
    ('33333333-0000-0001-0001-000000000005', '33333333-0000-0000-0001-000000000001', 'content',         'ACTIVE', 5),
    ('33333333-0000-0001-0001-000000000006', '33333333-0000-0000-0001-000000000001', 'cross-cutting',   'ACTIVE', 6),
    ('33333333-0000-0001-0001-000000000007', '33333333-0000-0000-0001-000000000001', 'fields',          'ACTIVE', 7),
    ('33333333-0000-0001-0001-000000000008', '33333333-0000-0000-0001-000000000001', 'validation',      'ACTIVE', 8),
    ('33333333-0000-0001-0001-000000000009', '33333333-0000-0000-0001-000000000001', 'other',           'ACTIVE', 9);

-- 5. DataList: GLOSSARY_FLAGS (for the deprecated soft-delete tag)
INSERT INTO data_list (id, name, description, updated_at) VALUES
    ('33333333-0000-0000-0002-000000000001', 'GLOSSARY_FLAGS', 'Operational flags for glossary entries (e.g. deprecated)', CURRENT_TIMESTAMP);

INSERT INTO data_list_option (id, data_list_id, option, data_list_option_status_id, "order") VALUES
    ('33333333-0000-0002-0001-000000000001', '33333333-0000-0000-0002-000000000001', 'deprecated', 'ACTIVE', 1);

-- 6. Index FK columns (CLAUDE.md rule)
CREATE INDEX IF NOT EXISTS idx_twin_class_field_twin_class_id
    ON twin_class_field (twin_class_id);
CREATE INDEX IF NOT EXISTS idx_link_src_twin_class_id
    ON link (src_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_link_dst_twin_class_id
    ON link (dst_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_data_list_option_data_list_id
    ON data_list_option (data_list_id);
```

**Migration TODOs before implementation:**

1. Resolve `<SYSTEM_DOMAIN_ID>` — find the deterministic UUID of the system domain from existing migrations or `SystemEntityService`.
2. Resolve i18n IDs for `GLOSSARY_SEE_ALSO` link forward/backward names — seed i18n + i18n_translation rows.
3. Confirm `link_type_id` enum values — check the `link_type` seed table for valid keys (probably `'BIDIRECTIONAL'` or `'MANY_TO_MANY'`).
4. Confirm `data_list_option_status_id` enum values — likely `'ACTIVE'`/`'DEPRECATED'` (verify against existing seed data).
5. Confirm `twin_class_owner_type_id` accepts `'SYSTEM'` as string or requires lookup against an enum table.
6. Replace placeholder `11111111-...` UUIDs with deterministic values (UUIDv5 from readable names) so the bootstrap service can locate fields by class ID + key without hardcoding.

---

## 18. Test Plan

All tests live under `core/src/test/java/org/twins/core/service/glossary/` and use the existing Testcontainers PostgreSQL fixture (see `TwinServiceIntegrationTest` for the pattern).

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
| `GlossaryBootstrapIntegrationTest.bootstrap_emptyDb_createsAllTwins` | Run on empty DB with 3 markdown files → 3 Twins created. `## Summary` body lands in `Twin.description`; remaining sections land in matching TwinFields. |
| `GlossaryBootstrapIntegrationTest.bootstrap_reRunWithNoChanges_skipsAll` | Second run with same files → 0 created, 0 updated, 3 skipped (hash match) |
| `GlossaryBootstrapIntegrationTest.bootstrap_markdownChanged_updatesFields` | Edit `## Summary` in one file → 1 Twin updated, hash rotated |
| `GlossaryBootstrapIntegrationTest.bootstrap_newFileAdded_createsNewTwin` | Add 4th file → 1 created on next run |
| `GlossaryBootstrapIntegrationTest.bootstrap_fileRemoved_tagsOrphanDeprecated` | Remove one file → orphan Twin gets `deprecated` tag, not deleted |
| `GlossaryBootstrapIntegrationTest.bootstrap_orphanRestored_clearsDeprecatedTag` | Re-add the removed file → deprecated tag removed automatically |
| `GlossaryBootstrapIntegrationTest.bootstrap_seeAlsoChanged_reconcilesLinks` | Add/remove entry in frontmatter `see_also` → TwinLink added/removed |
| `GlossaryBootstrapIntegrationTest.bootstrap_categoryChanged_reconcilesTag` | Change `category` from `core` to `workflow` → old tag removed, new tag added |
| `GlossaryBootstrapIntegrationTest.bootstrap_invalidFilePresent_loadsOthersOnly` | Mix 2 valid + 1 invalid file → 2 Twins created, invalid logged, no crash |
| `GlossaryBootstrapIntegrationTest.bootstrap_conditionalPropertyDisabled_runnerDoesNotFire` | Set `twins.glossary.bootstrap.enabled=false` → bean not in context, no Twins created |
| `GlossaryBootstrapIntegrationTest.bootstrap_deterministicUuid_sameAcrossRestarts` | UUIDs of Twins stable across two runs of bootstrap |

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
