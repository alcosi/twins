# Handoff: Domain configuration versioning (Twins)

## Purpose

This document transfers the design context from a ChatGPT discussion to an IDE coding agent. It captures the intended architecture, current SQL drafts, decisions, unresolved design questions, and implementation priorities.

**Important:** The SQL shown below is conversation-draft code, not verified production code. Review it against the actual schema, migration conventions, PostgreSQL version, trigger behavior, and transaction boundaries before implementing.

## 1. Technology and project context

- Backend: Java / Spring Boot, PostgreSQL.
- The application has domain-scoped configuration tables.
- A domain is the top-level organization.
- We want configuration versioning implemented primarily in PostgreSQL, with Java responsible for operations such as building the full JSON snapshot and calculating its hash.
- Keep the implementation explicit and understandable. Avoid per-row queries and N+1 behavior.
- SQL/code comments should be in English.

## 2. Intended user-visible workflow

There are four operations/stages:

1. **Edit configuration** — ordinary inserts/updates accumulate changes in the current working version.
2. **Release/export** — generate and store a full configuration snapshot (JSON plus hash), then mark the version as released.
3. **Import snapshot** — load a previously exported full snapshot into another environment.
4. **Restore a previous version** — use the same general mechanics as importing a snapshot.

A released version is an immutable historical snapshot. Export stores a **full snapshot**, not merely a delta.

## 3. Core data model and invariants

### `domain_version`

The current draft schema discussed:

```sql
CREATE TABLE IF NOT EXISTS domain_version (
    id uuid NOT NULL PRIMARY KEY,
    domain_id uuid NOT NULL REFERENCES domain
        ON UPDATE CASCADE ON DELETE CASCADE,
    version varchar NOT NULL,
    name varchar,
    description varchar,
    domain_version_status_id varchar NOT NULL DEFAULT 'OPEN'
        REFERENCES domain_version_status(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    json_file jsonb,
    hash varchar,
    previous_domain_version_id uuid REFERENCES domain_version
        ON UPDATE CASCADE ON DELETE SET NULL,
    started_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    start_duration bigint,
    released_at timestamp,
    release_duration bigint
);
```

Statuses:

- `domain_version_status`: `OPEN`, `RELEASED`
- `domain_operation_status`: `IDLE`, `IMPORTING`, `RELEASING`

The discussed indexes:

```sql
CREATE UNIQUE INDEX domain_version_one_open_per_domain
    ON domain_version(domain_id)
    WHERE domain_version_status_id = 'OPEN';

CREATE UNIQUE INDEX domain_version_unique
    ON domain_version(domain_id, version);

CREATE INDEX domain_version_prev_idx
    ON domain_version(previous_domain_version_id);

CREATE INDEX domain_version_released_idx
    ON domain_version(domain_id, released_at DESC);

CREATE INDEX domain_version_domain_id_index
    ON domain_version(domain_id);
```

`domain` has:

- `current_domain_version_id uuid REFERENCES domain_version ON UPDATE CASCADE ON DELETE RESTRICT`
- `domain_operation_status_id varchar NOT NULL DEFAULT 'IDLE' REFERENCES domain_operation_status(id) ON UPDATE CASCADE ON DELETE RESTRICT`

There is also a discussed `domain_version_ghost` table:

```sql
CREATE TABLE IF NOT EXISTS domain_version_ghost (
    domain_id uuid NOT NULL REFERENCES public.domain
        ON UPDATE CASCADE ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES public."user"
        ON UPDATE CASCADE ON DELETE CASCADE,
    table_name varchar(50) NOT NULL,
    PRIMARY KEY (domain_id, user_id, table_name)
);
```

Check the actual repository migrations before assuming all of this is already present.

### Configuration table invariant

- Every domain-scoped configuration row must have a non-null `domain_version_id`.
- There must not be configuration rows with `domain_version_id IS NULL`.
- Configuration table foreign keys should reference `domain_version(id)` with `ON UPDATE CASCADE`, enabling the key-shift strategy below.
- Core/global rows with `domain_id IS NULL` are not versioned; helper returns `NULL` for them if that is consistent with each table's schema.

### Critical schema contradiction to resolve first

The draft `domain_version.version` is `NOT NULL`, but `domain_version_get_current()` sets `version = NULL` when opening a working version. This cannot work as written.

Decide on a coherent model before coding:
- likely make `version` nullable for an `OPEN` working version and assign a version label/number during release; or
- retain a non-null version value for open versions and revise the uniqueness/naming model accordingly.

PostgreSQL unique indexes allow multiple `NULL` values by default, so `(domain_id, version)` can coexist with nullable working-version values. If changing nullability, add a constraint expressing the intended status-specific rule, if appropriate. Do not silently leave the contradiction.

Also verify whether `domain.current_domain_version_id` is intended to point to the current working row while editing and to the same row after release. The drafts assume it does.

## 4. Key design decision: open a working version by shifting the UUID

The intended optimization avoids updating every configuration table when the first edit occurs after a release.

When `domain_version_get_current(domain_id)` sees that the current version is `RELEASED`:

1. Generate a new UUID (`uuid_generate_v7_custom()` was used in the draft; verify that this function exists in the project).
2. Update the existing `domain_version.id` from the old UUID to the new UUID.
3. Rely on `ON UPDATE CASCADE` to shift all configuration rows' `domain_version_id` references, as well as `domain.current_domain_version_id`.
4. Insert a new `domain_version` row using the **old UUID**, copying the old released version metadata/snapshot. This restores the historical released version under its original ID.
5. Change the row under the **new UUID** into the `OPEN` working version: clear snapshot/hash/release fields, set `version = NULL` under the nullable-version design, set `previous_domain_version_id` to the old released UUID, and reset start metadata.
6. Return the new UUID. Triggers use it for configuration writes.

This is intended to avoid mass-updating each configuration table in application code. Confirm that every relevant FK really has `ON UPDATE CASCADE`, including any tables not yet inventoried.

## 5. Operation lifecycle

### Release

The intended lifecycle is split into phases because JSON generation may be long-running:

1. `domain_operation_release_start(domain_id)` runs in a short transaction and commits `domain_operation_status_id = 'RELEASING'`.
2. Java generates a full snapshot and its hash.
3. `domain_operation_release_end(...)` runs in another transaction, saves JSON/hash/release metadata, changes the version status to `RELEASED`, and resets operation status to `IDLE`.

The persistent operation status is intended to reject normal user edits while release is running. Do not keep a DB transaction/advisory transaction lock open for the whole Java export operation.

### Import / restore

- Import should set operation status to `IMPORTING` at start and reset it to `IDLE` at successful end.
- Normal edits must fail while `IMPORTING` or `RELEASING`.
- Import's own configuration DML must have an explicit, narrowly scoped way to bypass the normal “open a working version / reject external edits” trigger path. A transaction-local PostgreSQL GUC such as `set_config('myapp.mode', 'IMPORT', true)` was discussed as one possible mechanism, but no final choice was made. Do not infer that it is already implemented.
- Restore a previous version through the same import-like mechanics.
- Define error recovery for failures between start and end. Otherwise a domain could remain stuck in `IMPORTING` or `RELEASING`. Consider an explicit abort/fail function, operation token/owner, audit metadata, and safe retry behavior.

## 6. Concurrency is a blocker to resolve before production

The current drafts are **not yet race-safe**.

Problem:
- `domain_version_get_current()` checks operation status and returns immediately on the `OPEN` fast path without taking the advisory lock.
- `domain_operation_release_start()` takes an advisory transaction lock, checks state, and sets `RELEASING`.
- An edit can read `IDLE`/`OPEN`, then continue while release starts. The release code may snapshot data before the edit transaction commits, so the snapshot could omit a concurrent committed edit or include inconsistent data.
- An advisory transaction lock only lasts until its transaction commits. It does not protect the long Java snapshot-generation interval.

Before implementing triggers, define a serialization protocol that guarantees:
1. An edit cannot pass the operation-state check and then commit concurrently with a release/import start in a way that makes the snapshot inconsistent.
2. `release_start` waits for already-running edits to finish before marking the domain `RELEASING`.
3. New edits fail after the start transaction commits.
4. Import DML can proceed only through its explicit authorized path.

A possible straightforward design is to have each edit transaction and operation-start transaction lock the same `domain` row (`SELECT ... FOR UPDATE`) and check status while holding that lock. However, locking the domain row for every configuration write serializes writes per domain and may be expensive. Evaluate the expected write rate and consider a shared/exclusive advisory-lock protocol or another explicit gate. Be precise about lock lifetime and ordering. A status check without a matching synchronization protocol is insufficient.

Also review the advisory-lock key: `pg_advisory_xact_lock(hashtext(domain_id::text))` can collide between domains (causing unnecessary serialization, usually not incorrectness). A two-key/namespaced strategy may be clearer.

## 7. SQL function drafts from the discussion

These are the most recent drafts. They are included for context, not as production-ready code. They have the schema contradiction and concurrency limitations described above.

### 7.1 `domain_version_get_current`

```sql
CREATE OR REPLACE FUNCTION domain_version_get_current(p_domain_id UUID)
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_version_id        UUID;
    v_version_status    TEXT;
    v_operation_status  TEXT;
    v_hash              TEXT;
    v_json              JSONB;
    v_new_id             UUID;
BEGIN
    -- Core/global rows are not versioned.
    IF p_domain_id IS NULL THEN
        RETURN NULL;
    END IF;

    -- Fast path: read current version and operation status.
    SELECT dv.id,
           dv.domain_version_status_id,
           d.domain_operation_status_id,
           dv.hash,
           dv.json_file
    INTO v_version_id,
         v_version_status,
         v_operation_status,
         v_hash,
         v_json
    FROM domain d
    JOIN domain_version dv ON dv.id = d.current_domain_version_id
    WHERE d.id = p_domain_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Current domain version not found for domain %', p_domain_id;
    END IF;

    -- Block normal edits while a long-running operation is active.
    IF v_operation_status = 'RELEASING' THEN
        RAISE EXCEPTION 'Domain configuration release is in progress for domain %', p_domain_id;
    END IF;

    IF v_operation_status = 'IMPORTING' THEN
        RAISE EXCEPTION 'Domain configuration import is in progress for domain %', p_domain_id;
    END IF;

    IF v_version_status = 'OPEN' THEN
        RETURN v_version_id;
    END IF;

    -- Slow path: serialize creation of the working version.
    PERFORM pg_advisory_xact_lock(hashtext(p_domain_id::text));

    -- Recheck state after acquiring the lock.
    SELECT dv.id,
           dv.domain_version_status_id,
           d.domain_operation_status_id,
           dv.hash,
           dv.json_file
    INTO v_version_id,
         v_version_status,
         v_operation_status,
         v_hash,
         v_json
    FROM domain d
    JOIN domain_version dv ON dv.id = d.current_domain_version_id
    WHERE d.id = p_domain_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Current domain version not found for domain %', p_domain_id;
    END IF;

    IF v_operation_status = 'RELEASING' THEN
        RAISE EXCEPTION 'Domain configuration release is in progress for domain %', p_domain_id;
    END IF;

    IF v_operation_status = 'IMPORTING' THEN
        RAISE EXCEPTION 'Domain configuration import is in progress for domain %', p_domain_id;
    END IF;

    IF v_version_status = 'OPEN' THEN
        RETURN v_version_id;
    END IF;

    IF v_version_status <> 'RELEASED' THEN
        RAISE EXCEPTION 'Unsupported domain version status % for domain %',
            v_version_status, p_domain_id;
    END IF;

    -- Move all FK references to a new working-version ID via ON UPDATE CASCADE.
    v_new_id := uuid_generate_v7_custom();

    UPDATE domain_version
    SET id = v_new_id
    WHERE id = v_version_id;

    -- Recreate the released snapshot under its original ID.
    INSERT INTO domain_version (
        id,
        domain_id,
        version,
        name,
        description,
        domain_version_status_id,
        json_file,
        hash,
        previous_domain_version_id,
        started_at,
        start_duration,
        released_at,
        release_duration
    )
    SELECT v_version_id,
           domain_id,
           version,
           name,
           description,
           'RELEASED',
           json_file,
           hash,
           previous_domain_version_id,
           started_at,
           start_duration,
           released_at,
           release_duration
    FROM domain_version
    WHERE id = v_new_id;

    -- Turn the shifted row into the new working version.
    UPDATE domain_version
    SET domain_version_status_id   = 'OPEN',
        json_file                  = NULL,
        hash                       = NULL,
        version                    = NULL,
        previous_domain_version_id = v_version_id,
        started_at                 = CURRENT_TIMESTAMP,
        start_duration             = NULL,
        released_at                = NULL,
        release_duration           = NULL
    WHERE id = v_new_id;

    RETURN v_new_id;
END;
$$;
```

Notes:
- `v_hash` and `v_json` are selected but not used; remove them if unnecessary.
- Verify trigger interactions on updates to `domain_version`, especially if generic configuration triggers might fire on version metadata tables.
- Consider whether start duration is measured at opening time or supplied by Java.
- Make sure constraints and FKs permit the temporary PK shift within the same statement/transaction.

### 7.2 `domain_operation_release_start`

```sql
CREATE OR REPLACE FUNCTION domain_operation_release_start(p_domain_id UUID)
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_version_id       UUID;
    v_version_status   TEXT;
    v_operation_status TEXT;
BEGIN
    IF p_domain_id IS NULL THEN
        RAISE EXCEPTION 'Domain id must not be null';
    END IF;

    -- Serialize version-changing operations within a domain.
    PERFORM pg_advisory_xact_lock(hashtext(p_domain_id::text));

    SELECT dv.id,
           dv.domain_version_status_id,
           d.domain_operation_status_id
    INTO v_version_id,
         v_version_status,
         v_operation_status
    FROM domain d
    JOIN domain_version dv ON dv.id = d.current_domain_version_id
    WHERE d.id = p_domain_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Current domain version not found for domain %', p_domain_id;
    END IF;

    IF v_operation_status = 'RELEASING' THEN
        RAISE EXCEPTION 'Domain configuration release is already in progress for domain %',
            p_domain_id;
    END IF;

    IF v_operation_status = 'IMPORTING' THEN
        RAISE EXCEPTION 'Domain configuration import is in progress for domain %', p_domain_id;
    END IF;

    IF v_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'Current domain version % for domain % must be OPEN, but was %',
            v_version_id, p_domain_id, v_version_status;
    END IF;

    UPDATE domain
    SET domain_operation_status_id = 'RELEASING'
    WHERE id = p_domain_id;

    RETURN v_version_id;
END;
$$;
```

### 7.3 `domain_operation_release_end`

```sql
CREATE OR REPLACE FUNCTION domain_operation_release_end(
    p_domain_id UUID,
    p_json_file JSONB,
    p_hash VARCHAR,
    p_release_duration BIGINT,
    p_version VARCHAR DEFAULT NULL,
    p_name VARCHAR DEFAULT NULL,
    p_description VARCHAR DEFAULT NULL
)
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_version_id       UUID;
    v_version_status   TEXT;
    v_operation_status TEXT;
BEGIN
    IF p_domain_id IS NULL THEN
        RAISE EXCEPTION 'Domain id must not be null';
    END IF;

    IF p_json_file IS NULL THEN
        RAISE EXCEPTION 'Release json_file must not be null for domain %', p_domain_id;
    END IF;

    IF p_hash IS NULL OR btrim(p_hash) = '' THEN
        RAISE EXCEPTION 'Release hash must not be null or empty for domain %', p_domain_id;
    END IF;

    -- Serialize with version-opening, import, and parallel release operations.
    PERFORM pg_advisory_xact_lock(hashtext(p_domain_id::text));

    SELECT dv.id,
           dv.domain_version_status_id,
           d.domain_operation_status_id
    INTO v_version_id,
         v_version_status,
         v_operation_status
    FROM domain d
    JOIN domain_version dv ON dv.id = d.current_domain_version_id
    WHERE d.id = p_domain_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Current domain version not found for domain %', p_domain_id;
    END IF;

    IF v_operation_status <> 'RELEASING' THEN
        RAISE EXCEPTION 'Domain % is not in RELEASING state', p_domain_id;
    END IF;

    IF v_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'Current domain version % for domain % must be OPEN, but was %',
            v_version_id, p_domain_id, v_version_status;
    END IF;

    UPDATE domain_version
    SET domain_version_status_id = 'RELEASED',
        json_file                = p_json_file,
        hash                     = p_hash,
        released_at              = CURRENT_TIMESTAMP,
        release_duration         = p_release_duration,
        version                  = COALESCE(p_version, version),
        name                     = COALESCE(p_name, name),
        description              = COALESCE(p_description, description)
    WHERE id = v_version_id;

    UPDATE domain
    SET domain_operation_status_id = 'IDLE'
    WHERE id = p_domain_id;

    RETURN v_version_id;
END;
$$;
```

Important note: if the chosen model makes `version` nullable for `OPEN`, `p_version` should be required at release end (or validated as non-null/non-empty), because `COALESCE(p_version, version)` would otherwise preserve `NULL`.

## 8. Trigger design discussed, not implemented

No configuration triggers were actually finalized or written in the conversation.

Intended behavior:
- On configuration `INSERT`, a trigger determines the domain ID, calls `domain_version_get_current(domain_id)`, and sets `NEW.domain_version_id` to the returned UUID.
- On configuration `UPDATE`, ensure the row is assigned to the current working version where appropriate.
- Avoid accidentally overriding `domain_version_id` during the `ON UPDATE CASCADE` that moves all configuration rows from a released version ID to a working-version ID.
- Account for tables with different domain-ID sources, global/core rows, composite keys, and special-case import DML.
- Keep trigger logic shared where feasible, but do not assume one generic trigger can discover the domain column identically for every table without metadata/configuration.
- Avoid a query per row beyond the necessary helper behavior; consider statement-level/bulk paths if write volume makes row-level trigger overhead material.

Before creating triggers, inventory every configuration table and its `domain_id` source and FK behavior.

## 9. Remaining implementation tasks

Suggested order:

1. **Inspect repository/migrations** and reconcile the actual schema with this handoff.
2. **Resolve `version` nullability/naming semantics** for `OPEN` vs `RELEASED`.
3. **Design and document concurrency/locking** for edits vs release/import. Treat this as a correctness blocker.
4. Finalize import/restore start/end and an explicit internal-import trigger bypass.
5. Add failure/abort recovery for stuck operations.
6. Implement and test `domain_version_get_current` with the chosen lock protocol.
7. Implement release start/end and enforce release metadata validation.
8. Inventory configuration tables and add triggers with correct `domain_version_id` handling.
9. Add constraints/indexes and verify every configuration FK has `ON UPDATE CASCADE`.
10. Add automated integration tests against real PostgreSQL for:
    - first edit after a released version,
    - repeated edits to an already-open version,
    - release success and validation failures,
    - edit racing with release start,
    - concurrent first edits,
    - concurrent release attempts,
    - import DML vs ordinary edits,
    - import failure and recovery,
    - restore from an older snapshot,
    - multiple domains running operations concurrently,
    - `domain_version_id` cascades and historical chain integrity.

## 10. Design decisions and non-decisions

### Decisions / intended direction

- Use a `domain_version` history table with `OPEN` and `RELEASED` statuses.
- Keep current operation state (`IDLE`, `IMPORTING`, `RELEASING`) on `domain`.
- Store full exported snapshots as `jsonb` plus a hash.
- Released versions are immutable.
- Each configuration row is attached to a non-null version.
- Reuse one general import mechanism for both importing and restoring snapshots.
- Avoid updating every configuration table explicitly when opening a new working version; use the PK-shift + `ON UPDATE CASCADE` approach, if validated against the actual schema.
- Long-running export/import work must not depend on one long-lived DB transaction.

### Not yet decided / must not be treated as accepted

- Whether `domain_version.version` becomes nullable or uses another numbering strategy.
- The final locking protocol.
- The exact internal-import bypass mechanism (transaction-local GUC was only an option).
- The import/restore start/end function signatures.
- Error recovery/operation lease or token model.
- Exact generic trigger architecture and table inventory.
- Whether `domain_version_ghost` is part of the final design and how it is used.
- Whether version name/description are assigned at release start, release end, or another point.

## 11. Guidance to the IDE agent

Start by inspecting the existing schema and migration history; do not blindly paste the drafts. Report:
- which objects already exist and how they differ from this document,
- all configuration tables and their `domain_id` / `domain_version_id` columns and FK actions,
- the current `domain.current_domain_version_id` initialization strategy,
- the actual UUIDv7 function available,
- trigger interactions and any constraints that make PK shifting unsafe.

Then propose a concrete, race-safe transaction/locking protocol and resolve the `version` nullability contradiction before implementing the triggers. Keep the design aligned with the invariants above and add PostgreSQL integration tests for concurrency, not only happy-path tests.
