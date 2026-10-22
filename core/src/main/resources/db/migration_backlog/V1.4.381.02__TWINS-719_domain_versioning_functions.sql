-- TWINS-719: domain configuration versioning, model v2 (pilot).
-- Step 2/4: functions. GUC contract: a single transaction-local flag marks the protected path.
--   twins.domain_version_op — 'on' around writes into domain_version / audit / protected columns,
--                       physical deletes and explicit-stamp writes. Set by our own
--                       functions/wrappers around their internal writes AND by the importer
--                       (FULL/INCREMENTAL/rollback apply) around applying external content with
--                       explicit stamps and row statuses; never left set across statements.
--   twins.domain_version_reconcile — 'on' only around domain_version_reconcile's version-row
--                       delete: authorizes removing RELEASED versions that fell out of the
--                       chain (the domain_version delete guard checks it).
-- Design: ai/plans/domain-versioning-v2.md; implementation plan: ai/plans/domain-versioning-slice-a.md

-- ============================================================================
-- 0. Shared helpers
-- ============================================================================

-- Single registry of the versioned configuration tables (pilot: 7). A new wave adds one row.
-- excluded_columns: technical columns stripped by the meaningful change detection — common
-- columns are prepended, table-specific derived columns (counters, hierarchy trees) are listed
-- in the row. created_at is common: it never changes on UPDATE and is pure noise on a
-- re-insert (resurrection).
CREATE OR REPLACE FUNCTION domain_version_tables() RETURNS TABLE(table_name text, excluded_columns text[]) AS
$$
    SELECT v.t, ARRAY['updated_at', 'domain_version_id', 'created_at'] || v.specific
      FROM (VALUES
            ('twin_class',              ARRAY['twin_counter', 'head_hierarchy_counter_direct_children',
                                                 'extends_hierarchy_counter_direct_children',
                                                 'extends_hierarchy_tree', 'head_hierarchy_tree']),
            ('twin_class_field',        ARRAY[]::text[]),
            ('twin_status',             ARRAY[]::text[]),
            ('data_list',               ARRAY[]::text[]),
            ('data_list_option',        ARRAY[]::text[]),
            ('data_list_subset',        ARRAY[]::text[]),
            ('data_list_subset_option', ARRAY[]::text[])
           ) AS v(t, specific)
$$ LANGUAGE sql IMMUTABLE;

-- Advisory lock key: one key per domain for the whole edit/operation protocol
CREATE OR REPLACE FUNCTION domain_version_edit_lock_key(p_domain_id uuid) RETURNS bigint AS
$$
    SELECT hashtext('domain_version_edit-' || p_domain_id::text)::bigint;
$$ LANGUAGE sql IMMUTABLE;

-- Per-table technical columns excluded from the meaningful change detection.
-- Common columns live in the default branch; table-specific derived columns (counters,
-- hierarchy trees) are listed per table. A new wave adds one CASE line here.
-- Technical columns excluded from the meaningful change detection, per registry row
-- (see domain_version_tables). Unknown tables fall back to the common set.
CREATE OR REPLACE FUNCTION domain_version_excluded_columns(p_table text) RETURNS text[] AS
$$
    SELECT coalesce((SELECT r.excluded_columns FROM domain_version_tables() r WHERE r.table_name = p_table),
                    ARRAY['updated_at', 'domain_version_id', 'created_at'])
$$ LANGUAGE sql IMMUTABLE;

-- Meaningful change detection, shared by stamping and audit. row_status IS meaningful
-- (DISABLE/ENABLE is a real configuration change).
CREATE OR REPLACE FUNCTION domain_version_is_meaningful_change(p_table text, p_old jsonb, p_new jsonb) RETURNS boolean AS
$$
    SELECT (p_old - domain_version_excluded_columns(p_table))
           IS DISTINCT FROM
           (p_new - domain_version_excluded_columns(p_table));
$$ LANGUAGE sql IMMUTABLE;

-- Actor of the change: Java sets the GUC per request; absent -> NULL (service/legacy paths)
CREATE OR REPLACE FUNCTION current_actor_user_id_get() RETURNS uuid AS
$$
BEGIN
    BEGIN
        RETURN current_setting('app.current_user_id', true)::uuid;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
    END;
END;
$$ LANGUAGE plpgsql;

-- Does the window still carry net changes? Collapse precondition for rollback/import ends.
-- ONE query over the audit journal (no per-table probing). A row group counts as net-zero when
-- its LAST record is RESTORE — the row left the window with zero net effect. RESTORE is written
-- by the AFTER wrappers at the moment the row leaves: identical-content resurrection, rollback
-- apply, born-and-deleted inside the window. Everything else (last INSERT/UPDATE/DELETE) is a
-- live change. DISTINCT ON takes the last record of each group straight from the window index
-- (domain_id, domain_version_id, table_name, row_id, seq DESC): one index entry per changed row,
-- no aggregation. Backfilled rows carry synthetic INSERT records (03_backfill), so the baseline
-- V1 correctly counts as non-empty. Safety net: the stamp FK is ON DELETE RESTRICT.
CREATE OR REPLACE FUNCTION domain_version_window_has_stamped_rows(p_domain_id uuid, p_window_id uuid)
    RETURNS boolean AS
$$
    SELECT p_window_id IS NOT NULL AND EXISTS (
        SELECT 1
          FROM (SELECT DISTINCT ON (a.table_name, a.row_id)
                       a.operation
                  FROM domain_version_audit a
                 WHERE a.domain_id = p_domain_id
                   AND a.domain_version_id = p_window_id
                 ORDER BY a.table_name, a.row_id, a.seq DESC) last_per_row
         WHERE last_per_row.operation <> 'RESTORE'
    )
$$ LANGUAGE sql;

-- Physical cleanup of soft-deleted rows (release time ONLY: the DELETED rows must survive until
-- release_end collects them into the delta as delete-ops, a premature purge loses those ops).
-- Guarded by twins.domain_version_op the same way as the other internal writes — release_end sets it
-- around its call; a direct invocation without the GUC is rejected. The cleanup deletes run under
-- version_op, so the before-delete wrappers let the physical delete through; business AFTER
-- DELETE triggers keep i18n/counters consistent, the audit wrappers record the deletes with the
-- RELEASING stage.
CREATE OR REPLACE FUNCTION domain_version_deleted_rows_cleanup(p_domain_id uuid, p_version_id uuid) RETURNS integer AS
$$
DECLARE
    r         record;
    v_total   integer := 0;
    v_deleted integer;
BEGIN
    IF current_setting('twins.domain_version_op', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'domain_version_deleted_rows_cleanup is a release-time internal step of domain_operation_release_end, it cannot be called directly';
    END IF;
    -- the audit journal routes the cleanup, the row is the truth: only tables that recorded
    -- soft deletes in this release's slice are visited (at >100 versioned tables a typical
    -- release touches a few); the DELETE itself trusts row_status='DELETED', because a DELETE
    -- record alone does not mean the row is still deleted (it may have been resurrected since —
    -- an extra visited table is harmless, its bulk DELETE just matches nothing). The routing is
    -- evaluated once before any delete runs, so the RELEASING records written by the cleanup
    -- itself cannot interfere. Born-deletes never pollute the routing: they close with RESTORE.
    FOR r IN SELECT DISTINCT a.table_name
               FROM domain_version_audit a
              WHERE a.domain_id = p_domain_id
                AND a.domain_version_id = p_version_id
                AND a.operation = 'DELETE'
              LOOP
        EXECUTE format('DELETE FROM %I WHERE domain_id = $1 AND domain_version_id = $2 AND row_status = ''DELETED''',
                       r.table_name)
            USING p_domain_id, p_version_id;
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        v_total := v_total + v_deleted;
    END LOOP;
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 1. Edit gate: check the operation gate, lazily open the draft window, return the stamp
-- ============================================================================

-- Single source of the domain/version state read. LANGUAGE sql STABLE: the planner
-- inlines it into the caller, zero call overhead.
CREATE OR REPLACE FUNCTION domain_version_current_state(p_domain_id uuid)
    RETURNS TABLE(operation_status varchar, locked boolean, version_id uuid, version_status varchar)
    LANGUAGE sql STABLE AS
$$
    SELECT d.operation_status, d.locked, dv.id, dv.status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;
$$;

-- ============================================================================
-- Shared preconditions of the gate and the operations: tiny asserts over the
-- already-read state (explicit parameters, no hidden re-reads).
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_version_assert_version_found(p_domain_id uuid, p_version_id uuid) RETURNS void AS
$$
BEGIN
    IF p_version_id IS NULL THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_assert_supported_version_status(p_domain_id uuid, p_version_status varchar) RETURNS void AS
$$
BEGIN
    IF p_version_status NOT IN ('OPEN', 'RELEASED') THEN
        RAISE EXCEPTION 'unsupported domain version status % for domain %', p_version_status, p_domain_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_assert_no_operation_running(p_domain_id uuid, p_operation_status varchar) RETURNS void AS
$$
BEGIN
    IF p_operation_status <> 'IDLE' THEN
        RAISE EXCEPTION 'another operation (%) is already running for domain %', p_operation_status, p_domain_id
            USING ERRCODE = '55006';
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_assert_not_locked(p_domain_id uuid, p_locked boolean) RETURNS void AS
$$
BEGIN
    IF p_locked THEN
        RAISE EXCEPTION 'domain % is frozen (locked)', p_domain_id USING ERRCODE = '55006';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- p_action describes the attempted step: 'release', 'roll back', 'finish the release', ...
CREATE OR REPLACE FUNCTION domain_version_assert_open_window(p_domain_id uuid, p_version_status varchar, p_action text) RETURNS void AS
$$
BEGIN
    IF p_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'cannot %: current version of domain % must be OPEN, got %', p_action, p_domain_id, p_version_status;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_assert_operation_state(p_domain_id uuid, p_operation_status varchar, p_expected varchar) RETURNS void AS
$$
BEGIN
    IF p_operation_status <> p_expected THEN
        RAISE EXCEPTION 'domain % is not in % state', p_domain_id, p_expected;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Single copy of the edit gates: no operation may run, the domain must not be frozen.
CREATE OR REPLACE FUNCTION domain_version_assert_edit_allowed(
    p_domain_id uuid, p_operation_status varchar, p_locked boolean) RETURNS void AS
$$
BEGIN
    PERFORM domain_version_assert_no_operation_running(p_domain_id, p_operation_status);
    PERFORM domain_version_assert_not_locked(p_domain_id, p_locked);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_gate(p_domain_id uuid) RETURNS uuid AS
$$
DECLARE
    v_lock_key bigint;
    v_state    record;
    v_shared   boolean;
    v_new_id   uuid;
BEGIN
    -- core/global rows are not versioned; the protected path (import apply, internal writes)
    -- never resolves a stamp here — import applies explicit stamps, internal writes know theirs
    IF p_domain_id IS NULL OR current_setting('twins.domain_version_op', true) = 'on' THEN
        RETURN NULL;
    END IF;

    v_lock_key := domain_version_edit_lock_key(p_domain_id);

    -- 1. lock-free pre-read: its ONLY job is choosing the lock mode
    SELECT * INTO v_state FROM domain_version_current_state(p_domain_id);
    PERFORM domain_version_assert_version_found(p_domain_id, v_state.version_id);
    PERFORM domain_version_assert_supported_version_status(p_domain_id, v_state.version_status);

    -- 2. take the lock: shared for an open window (edits do not block each other),
    --    exclusive for a released one (the first edit opens the new window). A shared
    --    lock is never taken before the exclusive one — no upgrade deadlocks
    v_shared := v_state.version_status = 'OPEN';
    IF v_shared THEN
        PERFORM pg_advisory_xact_lock_shared(v_lock_key);
    ELSE
        PERFORM pg_advisory_xact_lock(v_lock_key);
    END IF;

    -- 3. authoritative re-read under the lock. The pre-read may be stale: a whole
    --    release can complete between the pre-read and the lock (operation_status
    --    returns to IDLE while the version is already RELEASED); returning a stale
    --    window id would stamp the edit into a closed version and silently lose it
    --    from every future delta. While the lock is held, current cannot change:
    --    every operation changing it takes the exclusive lock first.
    SELECT * INTO v_state FROM domain_version_current_state(p_domain_id);
    PERFORM domain_version_assert_version_found(p_domain_id, v_state.version_id);
    PERFORM domain_version_assert_edit_allowed(p_domain_id, v_state.operation_status, v_state.locked);

    -- 4. resolve the stamp
    IF v_state.version_status = 'OPEN' THEN
        RETURN v_state.version_id;             -- live window (possibly opened by someone
                                                -- else while we waited on the exclusive lock)
    ELSIF v_shared THEN
        -- the version closed while we were acquiring the shared lock. A window cannot
        -- be opened under a shared lock and the upgrade is a deadlock hazard: ask for
        -- a retry, the retry pre-reads RELEASED and takes the slow path
        RAISE EXCEPTION 'domain % version state changed concurrently, retry the transaction', p_domain_id
            USING ERRCODE = '40001';
    ELSIF v_state.version_status <> 'RELEASED' THEN
        -- defensive, see step 3: always raises, control never reaches step 5 here
        PERFORM domain_version_assert_supported_version_status(p_domain_id, v_state.version_status);
    END IF;

    -- 5. exclusive lock held, current is RELEASED: lazily open the new draft window
    v_new_id := uuid_generate_v7_custom();
    PERFORM set_config('twins.domain_version_op', 'on', true);   -- domain_version is writable
    INSERT INTO domain_version (id, domain_id, version, status)          -- only via the functions
    VALUES (v_new_id, p_domain_id, 'draft', 'OPEN');
    UPDATE domain SET current_domain_version_id = v_new_id WHERE id = p_domain_id;
    PERFORM set_config('twins.domain_version_op', '', true);
    RETURN v_new_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 2. Domain birth: create V1. Used by the domain before-insert trigger (04) and the backfill (03)
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_version_init(p_domain_id uuid) RETURNS uuid AS
$$
DECLARE
    v_id uuid;
BEGIN
    IF p_domain_id IS NULL THEN
        RETURN NULL;
    END IF;
    -- idempotent: a domain that already has a current version keeps it
    SELECT current_domain_version_id INTO v_id FROM domain WHERE id = p_domain_id;
    IF v_id IS NOT NULL THEN
        RETURN v_id;
    END IF;
    v_id := uuid_generate_v7_custom();
    PERFORM set_config('twins.domain_version_op', 'on', true);
    -- domain row may not exist yet (before-insert trigger): domain_id FK is DEFERRABLE
    INSERT INTO domain_version (id, domain_id, version, status)
    VALUES (v_id, p_domain_id, 'draft', 'OPEN');
    PERFORM set_config('twins.domain_version_op', '', true);
    RETURN v_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 3. Audit write (called by AFTER wrappers of every path, the stage tag comes from the domain)
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_version_audit_write(
    p_table_name text, p_domain_id uuid, p_domain_version_id uuid,
    p_row_id uuid, p_operation text, p_before jsonb, p_after jsonb) RETURNS void AS
$$
DECLARE
    v_operation_status varchar;
BEGIN
    IF p_domain_id IS NULL THEN
        RETURN; -- system rows are not versioned/audited
    END IF;
    SELECT operation_status INTO v_operation_status FROM domain WHERE id = p_domain_id;
    INSERT INTO domain_version_audit (id, domain_id, domain_version_id, table_name, row_id,
                                      operation, domain_operation_status, before, after, changed_at, actor_user_id)
    VALUES (uuid_generate_v7_custom(), p_domain_id, p_domain_version_id, p_table_name, p_row_id,
            p_operation, coalesce(v_operation_status, 'IDLE'), p_before, p_after, CURRENT_TIMESTAMP,
            current_actor_user_id_get());
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 4. Release
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_operation_release_start(p_domain_id uuid) RETURNS uuid AS
$$
DECLARE
    v_lock_key         bigint;
    v_operation_status varchar;
    v_locked           boolean;
    v_version_id       uuid;
    v_version_status   varchar;
BEGIN
    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, d.locked, dv.id, dv.status
      INTO v_operation_status, v_locked, v_version_id, v_version_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    PERFORM domain_version_assert_version_found(p_domain_id, v_version_id);
    PERFORM domain_version_assert_no_operation_running(p_domain_id, v_operation_status);
    PERFORM domain_version_assert_not_locked(p_domain_id, v_locked);
    PERFORM domain_version_assert_open_window(p_domain_id, v_version_status, 'release');

    UPDATE domain SET operation_status = 'RELEASING', operation_started_at = CURRENT_TIMESTAMP
     WHERE id = p_domain_id;
    RETURN v_version_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_operation_release_end(
    p_domain_id uuid, p_version varchar, p_json_file jsonb, p_hash varchar,
    p_delta_file jsonb, p_delta_hash varchar,
    p_rollback_delta_file jsonb DEFAULT NULL, p_rollback_delta_hash varchar DEFAULT NULL,
    p_release_duration bigint DEFAULT NULL, p_name varchar DEFAULT NULL, p_description varchar DEFAULT NULL)
    RETURNS uuid AS
$$
DECLARE
    v_lock_key         bigint;
    v_operation_status varchar;
    v_version_id       uuid;
    v_version_status   varchar;
    v_has_previous     boolean;
    v_previous_id      uuid;
    v_order            bigint;
BEGIN
    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, dv.id, dv.status
      INTO v_operation_status, v_version_id, v_version_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    PERFORM domain_version_assert_version_found(p_domain_id, v_version_id);
    PERFORM domain_version_assert_operation_state(p_domain_id, v_operation_status, 'RELEASING');
    PERFORM domain_version_assert_open_window(p_domain_id, v_version_status, 'finish the release');

    IF btrim(coalesce(p_version, '')) = '' THEN
        RAISE EXCEPTION 'release label must not be empty for domain %', p_domain_id;
    END IF;
    IF p_json_file IS NULL THEN
        RAISE EXCEPTION 'release snapshot (json_file) must not be null for domain %', p_domain_id;
    END IF;
    IF btrim(coalesce(p_hash, '')) = '' THEN
        RAISE EXCEPTION 'release hash must not be empty for domain %', p_domain_id;
    END IF;
    IF (p_delta_file IS NULL) <> (p_delta_hash IS NULL) THEN
        RAISE EXCEPTION 'forward delta must be a pair (delta_file, delta_hash) for domain %', p_domain_id;
    END IF;
    IF (p_rollback_delta_file IS NULL) <> (p_rollback_delta_hash IS NULL) THEN
        RAISE EXCEPTION 'rollback delta must be a pair (rollback_delta_file, rollback_delta_hash) for domain %', p_domain_id;
    END IF;

    SELECT EXISTS (SELECT 1 FROM domain_version WHERE domain_id = p_domain_id AND status = 'RELEASED')
      INTO v_has_previous;
    IF v_has_previous THEN
        IF p_delta_file IS NULL OR p_rollback_delta_file IS NULL THEN
            RAISE EXCEPTION 'a non-first release of domain % requires both forward and rollback deltas', p_domain_id;
        END IF;
    ELSE
        IF p_rollback_delta_file IS NOT NULL THEN
            RAISE EXCEPTION 'the first release of domain % has no rollback delta', p_domain_id;
        END IF;
    END IF;

    SELECT id INTO v_previous_id
      FROM domain_version
     WHERE domain_id = p_domain_id AND status = 'RELEASED'
     ORDER BY "order" DESC NULLS LAST, released_at DESC
     LIMIT 1;
    SELECT coalesce(max("order"), 0) + 1 INTO v_order FROM domain_version WHERE domain_id = p_domain_id;

    PERFORM set_config('twins.domain_version_op', 'on', true);
    UPDATE domain_version
       SET status = 'RELEASED',
           version = p_version,
           name = coalesce(p_name, name),
           description = coalesce(p_description, description),
           json_file = p_json_file,
           hash = p_hash,
           delta_file = p_delta_file,
           delta_hash = p_delta_hash,
           rollback_delta_file = p_rollback_delta_file,
           rollback_delta_hash = p_rollback_delta_hash,
           previous_domain_version_id = v_previous_id,
           "order" = v_order,
           start_duration = EXTRACT(epoch FROM (CURRENT_TIMESTAMP - started_at))::bigint,
           released_at = CURRENT_TIMESTAMP,
           release_duration = p_release_duration
     WHERE id = v_version_id;

    PERFORM domain_version_deleted_rows_cleanup(p_domain_id, v_version_id);

    PERFORM set_config('twins.domain_version_op', '', true);
    UPDATE domain SET operation_status = 'IDLE', operation_started_at = NULL WHERE id = p_domain_id;
    RETURN v_version_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 5. Import. LOCKed domains still allow import (the only door into a frozen domain)
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_operation_import_start(
    p_domain_id uuid, p_mode varchar, p_expected_base_hash varchar DEFAULT NULL) RETURNS uuid AS
$$
DECLARE
    v_lock_key         bigint;
    v_operation_status varchar;
    v_version_id       uuid;
    v_version_status   varchar;
    v_hash             varchar;
BEGIN
    IF p_mode NOT IN ('FULL', 'INCREMENTAL') THEN
        RAISE EXCEPTION 'unknown import mode %', p_mode;
    END IF;
    IF p_mode = 'FULL' AND p_expected_base_hash IS NOT NULL THEN
        RAISE EXCEPTION 'FULL import does not take an expected base hash';
    END IF;
    IF p_mode = 'INCREMENTAL' AND p_expected_base_hash IS NULL THEN
        RAISE EXCEPTION 'INCREMENTAL import requires the expected base hash';
    END IF;

    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, dv.id, dv.status, dv.hash
      INTO v_operation_status, v_version_id, v_version_status, v_hash
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    PERFORM domain_version_assert_version_found(p_domain_id, v_version_id);
    PERFORM domain_version_assert_no_operation_running(p_domain_id, v_operation_status);

    IF p_mode = 'INCREMENTAL' THEN
        IF v_version_status <> 'RELEASED' THEN
            RAISE EXCEPTION 'domain % has an open draft window: roll it back before an incremental import',
                p_domain_id USING ERRCODE = '55006';
        END IF;
        IF v_hash IS DISTINCT FROM p_expected_base_hash THEN
            RAISE EXCEPTION 'domain % state diverged from the delta base (expected %, actual %)',
                p_domain_id, p_expected_base_hash, v_hash USING ERRCODE = '55006';
        END IF;
    END IF;

    UPDATE domain SET operation_status = 'IMPORTING', operation_started_at = CURRENT_TIMESTAMP
     WHERE id = p_domain_id;
    RETURN v_version_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_operation_import_end(
    p_domain_id uuid, p_content_hash varchar, p_label varchar DEFAULT NULL) RETURNS uuid AS
$$
DECLARE
    v_lock_key         bigint;
    v_operation_status varchar;
    v_current_id       uuid;
    v_current_status   varchar;
    v_keep_window      uuid;
    v_import_id        uuid;
    v_previous_id      uuid;
    v_order            bigint;
    v_label            varchar;
BEGIN
    IF btrim(coalesce(p_content_hash, '')) = '' THEN
        RAISE EXCEPTION 'imported content hash must not be empty for domain %', p_domain_id;
    END IF;

    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, dv.id, dv.status
      INTO v_operation_status, v_current_id, v_current_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    PERFORM domain_version_assert_version_found(p_domain_id, v_current_id);
    PERFORM domain_version_assert_operation_state(p_domain_id, v_operation_status, 'IMPORTING');

    -- the import reconciled everything: an open draft window must be empty by now, collapse it
    IF v_current_status = 'OPEN' THEN
        IF domain_version_window_has_stamped_rows(p_domain_id, v_current_id) THEN
            RAISE EXCEPTION 'open window of domain % still has stamped rows: the import did not reconcile everything',
                p_domain_id;
        END IF;
        -- the window is still referenced by domain.current: the pointer moves to the import
        -- version below, only then the empty window row may be deleted
        v_keep_window := v_current_id;
    END IF;

    SELECT id INTO v_previous_id
      FROM domain_version
     WHERE domain_id = p_domain_id AND status = 'RELEASED'
     ORDER BY "order" DESC NULLS LAST, released_at DESC
     LIMIT 1;
    SELECT coalesce(max("order"), 0) + 1 INTO v_order FROM domain_version WHERE domain_id = p_domain_id;

    v_label := coalesce(nullif(btrim(p_label), ''), '(import) ' || v_order);
    v_import_id := uuid_generate_v7_custom();

    PERFORM set_config('twins.domain_version_op', 'on', true);
    INSERT INTO domain_version (id, domain_id, version, status, "order", hash, previous_domain_version_id, released_at)
    VALUES (v_import_id, p_domain_id, v_label, 'RELEASED', v_order, p_content_hash, v_previous_id, CURRENT_TIMESTAMP);
    UPDATE domain SET current_domain_version_id = v_import_id WHERE id = p_domain_id;
    IF v_keep_window IS NOT NULL THEN
        DELETE FROM domain_version WHERE id = v_keep_window;
    END IF;
    PERFORM set_config('twins.domain_version_op', '', true);

    UPDATE domain SET operation_status = 'IDLE', operation_started_at = NULL WHERE id = p_domain_id;
    RETURN v_import_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 6. Draft window rollback (before-images are applied by Java between start and end)
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_operation_rollback_start(p_domain_id uuid) RETURNS uuid AS
$$
DECLARE
    v_lock_key         bigint;
    v_operation_status varchar;
    v_locked           boolean;
    v_version_id       uuid;
    v_version_status   varchar;
BEGIN
    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, d.locked, dv.id, dv.status
      INTO v_operation_status, v_locked, v_version_id, v_version_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    PERFORM domain_version_assert_version_found(p_domain_id, v_version_id);
    PERFORM domain_version_assert_no_operation_running(p_domain_id, v_operation_status);
    PERFORM domain_version_assert_not_locked(p_domain_id, v_locked);
    PERFORM domain_version_assert_open_window(p_domain_id, v_version_status, 'roll back');

    UPDATE domain SET operation_status = 'ROLLBACK', operation_started_at = CURRENT_TIMESTAMP
     WHERE id = p_domain_id;
    RETURN v_version_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_operation_rollback_end(p_domain_id uuid) RETURNS uuid AS
$$
DECLARE
    v_lock_key         bigint;
    v_operation_status varchar;
    v_window_id        uuid;
    v_version_status   varchar;
    v_previous_id      uuid;
BEGIN
    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, dv.id, dv.status
      INTO v_operation_status, v_window_id, v_version_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    PERFORM domain_version_assert_version_found(p_domain_id, v_window_id);
    PERFORM domain_version_assert_operation_state(p_domain_id, v_operation_status, 'ROLLBACK');
    PERFORM domain_version_assert_open_window(p_domain_id, v_version_status, 'finish the rollback');

    -- collapse the empty window: the domain returns to the last released state verbatim
    IF NOT domain_version_window_has_stamped_rows(p_domain_id, v_window_id) THEN
        SELECT id INTO v_previous_id
          FROM domain_version
         WHERE domain_id = p_domain_id AND status = 'RELEASED'
         ORDER BY "order" DESC NULLS LAST, released_at DESC
         LIMIT 1;
        IF v_previous_id IS NOT NULL THEN
            PERFORM set_config('twins.domain_version_op', 'on', true);
            -- move the pointer away first: the FK from domain.current would block the delete
            UPDATE domain SET current_domain_version_id = v_previous_id WHERE id = p_domain_id;
            DELETE FROM domain_version WHERE id = v_window_id;
            PERFORM set_config('twins.domain_version_op', '', true);
        END IF; -- a domain with only the empty V1 keeps it: current_domain_version_id is NOT NULL
    END IF;

    UPDATE domain SET operation_status = 'IDLE', operation_started_at = NULL WHERE id = p_domain_id;
    RETURN (SELECT current_domain_version_id FROM domain WHERE id = p_domain_id);
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 7. Stuck operation abort
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_operation_abort(p_domain_id uuid) RETURNS void AS
$$
DECLARE
    v_lock_key bigint;
BEGIN
    v_lock_key := domain_version_edit_lock_key(p_domain_id);
    PERFORM pg_advisory_xact_lock(v_lock_key);
    UPDATE domain SET operation_status = 'IDLE', operation_started_at = NULL
     WHERE id = p_domain_id AND operation_status <> 'IDLE';
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 8. Orphan hygiene: rows stamped with versions outside the chain of the current version.
--    Diagnostic / repair tool, NOT a part of the import reconcile.
-- ============================================================================
-- Routing goes through the audit journal (the domain_version_deleted_rows_cleanup pattern):
-- every stamp ever written by the wrappers/backfill has a journal record, so ONE DISTINCT query
-- over the window index (domain_id, domain_version_id, table_name) enumerates exactly the
-- (table, stamp) pairs carrying orphan rows — no per-table probing at >100 versioned tables.
-- The contract covers journal-known orphans; a dangling stamp with neither a version row nor a
-- journal record (pre-VALIDATE legacy) is invisible here and left alone.
-- The phase order makes the ON DELETE RESTRICT stamp FK reconcile's own verifier: the orphan
-- version rows are deleted only AFTER the routed row deletes — if the routing missed a stamped
-- table, the version delete fails loudly with an FK violation instead of leaving dangling
-- stamps. The orphans' journal records are purged LAST, so a failed run keeps the evidence and
-- can simply be re-run.
-- The row semantic is "physically delete whatever carries an orphan stamp" (rows born in the
-- orphan AND rows that pre-existed it). A journal-restore variant — UPDATE/DELETE orphans
-- restored from the before-image instead of deleted — is a possible evolution: the routing
-- query already carries the operation info, it is just not used yet.
-- the return type changed (row_ctid tid -> rows_matched bigint): CREATE OR REPLACE cannot
-- alter it, drop the old signature first (no-op on a fresh database)
DROP FUNCTION IF EXISTS domain_version_reconcile(uuid, boolean);
CREATE OR REPLACE FUNCTION domain_version_reconcile(p_domain_id uuid, p_dry_run boolean DEFAULT true)
    RETURNS TABLE (table_name text, domain_version_id uuid, rows_matched bigint) AS
$$
DECLARE
    r         record;
    v_chain   uuid[];
    v_orphans uuid[];
BEGIN
    -- 1. the chain of the current version via previous-links: ONE recursive query, not a loop
    WITH RECURSIVE chain AS (
        SELECT dv.id
          FROM domain_version dv
          WHERE dv.id = (SELECT current_domain_version_id FROM domain WHERE id = p_domain_id)
        UNION
        SELECT dv.id
          FROM domain_version dv
          JOIN chain c ON dv.previous_domain_version_id = c.id
    )
    SELECT ARRAY(SELECT id FROM chain) INTO v_chain;

    -- 2. orphans = version rows outside the chain. NULL -> nothing to do; this is also the
    --    defensive branch for a broken current pointer (<> ALL(NULL) is UNKNOWN -> no orphans
    --    -> wipe nothing)
    SELECT array_agg(dv.id) INTO v_orphans
      FROM domain_version dv
     WHERE dv.domain_id = p_domain_id
       AND dv.id <> ALL (v_chain);
    IF v_orphans IS NULL THEN
        RETURN;
    END IF;

    IF NOT p_dry_run THEN
        PERFORM set_config('twins.domain_version_op', 'on', true);   -- physical deletes allowed
    END IF;

    -- 3. journal-routed row deletes: only the tables that actually recorded changes under an
    --    orphan stamp are visited
    FOR r IN SELECT DISTINCT a.table_name AS t, a.domain_version_id AS v
               FROM domain_version_audit a
              WHERE a.domain_id = p_domain_id
                AND a.domain_version_id = ANY (v_orphans)
              ORDER BY 1, 2
    LOOP
        IF p_dry_run THEN
            EXECUTE format('SELECT count(*) FROM %I WHERE domain_id = $1 AND domain_version_id = $2', r.t)
               INTO rows_matched
              USING p_domain_id, r.v;
        ELSE
            EXECUTE format('DELETE FROM %I WHERE domain_id = $1 AND domain_version_id = $2', r.t)
              USING p_domain_id, r.v;
            GET DIAGNOSTICS rows_matched = ROW_COUNT;
        END IF;
        table_name := r.t;
        domain_version_id := r.v;
        RETURN NEXT;
    END LOOP;

    -- 4. journal-silent orphans: no rows to delete, but the version row itself goes at step 5
    FOR r IN SELECT dv.id AS v
               FROM domain_version dv
              WHERE dv.id = ANY (v_orphans)
                AND NOT EXISTS (SELECT 1
                                  FROM domain_version_audit a
                                 WHERE a.domain_id = p_domain_id
                                   AND a.domain_version_id = dv.id)
              ORDER BY 1
    LOOP
        table_name := NULL;
        domain_version_id := r.v;
        rows_matched := 0;
        RETURN NEXT;
    END LOOP;

    IF NOT p_dry_run THEN
        -- 5. RESTRICT-verified: fails loudly if the routing above missed a stamped table.
        --    RELEASED rows are delete-protected by the domain_version guard; the second GUC
        --    names the single path allowed to remove out-of-chain history
        PERFORM set_config('twins.domain_version_reconcile', 'on', true);
        DELETE FROM domain_version dv WHERE dv.id = ANY (v_orphans);
        PERFORM set_config('twins.domain_version_reconcile', '', true);
        -- 6. purge the orphans' journal (incl. the records the routed deletes just wrote)
        DELETE FROM domain_version_audit a
         WHERE a.domain_id = p_domain_id AND a.domain_version_id = ANY (v_orphans);
        PERFORM set_config('twins.domain_version_op', '', true);
    END IF;
END;
$$ LANGUAGE plpgsql;
