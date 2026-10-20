-- TWINS-719: domain configuration versioning, model v2 (pilot).
-- Step 2/4: functions. GUC contract:
--   twins.version_op  — transaction-local, set by our own functions/wrappers around their writes
--                       into domain_version / audit / protected columns;
--   twins.import_mode — transaction-local, set by the importer (FULL/INCREMENTAL/rollback apply)
--                       around applying external content with explicit stamps and row statuses.
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
-- its LAST record in the window slice closes the history:
--   - physical DELETE (after IS NULL): a row born and deleted inside the window;
--   - RESTORE: the row returned to a previous state and left the window — written by the AFTER
--     wrapper for restore-class updates (identical-content resurrection, rollback apply);
--     RESTORE carries the restored image, hence no after-NULL requirement.
-- Backfilled rows carry synthetic INSERT records (03_backfill), so the baseline V1 correctly
-- counts as non-empty. Safety net for registry gaps: the stamp FK is ON DELETE RESTRICT.
CREATE OR REPLACE FUNCTION domain_version_window_has_stamped_rows(p_domain_id uuid, p_window_id uuid)
    RETURNS boolean AS
$$
    SELECT p_window_id IS NOT NULL AND EXISTS (
        SELECT 1
          FROM domain_version_audit a
         WHERE a.domain_id = p_domain_id
           AND a.domain_version_id = p_window_id
         GROUP BY a.table_name, a.row_id
        HAVING (array_agg(a.operation ORDER BY a.seq))[count(*)] <> 'RESTORE'
            AND ((array_agg(a.operation ORDER BY a.seq))[count(*)] <> 'DELETE'
                 OR (array_agg(a.after ORDER BY a.seq))[count(*)] IS NOT NULL)
    )
$$ LANGUAGE sql;

-- Physical cleanup of soft-deleted rows (release time). Runs under twins.version_op, so the
-- before-delete wrappers let the physical delete through; business AFTER DELETE triggers keep
-- i18n/counters consistent, the audit wrappers record the deletes with the RELEASING stage.
CREATE OR REPLACE FUNCTION domain_version_deleted_rows_cleanup(p_domain_id uuid) RETURNS integer AS
$$
DECLARE
    r         record;
    v_total   integer := 0;
    v_deleted integer;
BEGIN
    FOR r IN SELECT table_name FROM domain_version_tables() LOOP
        EXECUTE format('DELETE FROM %I WHERE domain_id = $1 AND row_status = ''DELETED''', r.table_name)
            USING p_domain_id;
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        v_total := v_total + v_deleted;
    END LOOP;
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 1. Edit gate: check the operation gate, lazily open the draft window, return the stamp
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_version_gate(p_domain_id uuid) RETURNS uuid AS
$$
DECLARE
    v_lock_key          bigint;
    v_operation_status  varchar;
    v_locked            boolean;
    v_version_id        uuid;
    v_version_status    varchar;
    v_new_id            uuid;
BEGIN
    -- core/global rows are not versioned
    IF p_domain_id IS NULL THEN
        RETURN NULL;
    END IF;
    -- import path applies external content with explicit stamps
    IF current_setting('twins.import_mode', true) = 'on' THEN
        RETURN NULL;
    END IF;

    v_lock_key := domain_version_edit_lock_key(p_domain_id);

    SELECT d.operation_status, d.locked, dv.id, dv.status
      INTO v_operation_status, v_locked, v_version_id, v_version_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;

    IF v_version_status = 'OPEN' THEN
        -- fast path: shared lock held until commit, gate rechecked under the lock so that no
        -- operation can start between the check and the commit of this edit
        PERFORM pg_advisory_xact_lock_shared(v_lock_key);
        SELECT d.operation_status, d.locked
          INTO v_operation_status, v_locked
          FROM domain d
         WHERE d.id = p_domain_id;
        IF v_operation_status <> 'IDLE' THEN
            RAISE EXCEPTION 'domain % configuration is locked by a running operation (%)',
                p_domain_id, v_operation_status USING ERRCODE = '55006';
        END IF;
        IF v_locked THEN
            RAISE EXCEPTION 'domain % is frozen (locked)', p_domain_id USING ERRCODE = '55006';
        END IF;
        RETURN v_version_id;
    END IF;

    IF v_version_status <> 'RELEASED' THEN
        RAISE EXCEPTION 'unsupported domain version status % for domain %', v_version_status, p_domain_id;
    END IF;

    -- slow path: the first edit after a release opens a new draft window (exclusive lock; a
    -- shared lock is never taken before it, so there are no upgrade deadlocks)
    PERFORM pg_advisory_xact_lock(v_lock_key);

    SELECT d.operation_status, d.locked, dv.id, dv.status
      INTO v_operation_status, v_locked, v_version_id, v_version_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'IDLE' THEN
        RAISE EXCEPTION 'domain % configuration is locked by a running operation (%)',
            p_domain_id, v_operation_status USING ERRCODE = '55006';
    END IF;
    IF v_locked THEN
        RAISE EXCEPTION 'domain % is frozen (locked)', p_domain_id USING ERRCODE = '55006';
    END IF;
    IF v_version_status = 'OPEN' THEN
        RETURN v_version_id; -- another first-edit opened the window while we were waiting
    END IF;

    v_new_id := uuid_generate_v7_custom();
    PERFORM set_config('twins.version_op', 'on', true);
    INSERT INTO domain_version (id, domain_id, version, status)
    VALUES (v_new_id, p_domain_id, 'draft', 'OPEN');
    UPDATE domain SET current_domain_version_id = v_new_id WHERE id = p_domain_id;
    PERFORM set_config('twins.version_op', '', true);
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
    PERFORM set_config('twins.version_op', 'on', true);
    -- domain row may not exist yet (before-insert trigger): domain_id FK is DEFERRABLE
    INSERT INTO domain_version (id, domain_id, version, status)
    VALUES (v_id, p_domain_id, 'draft', 'OPEN');
    PERFORM set_config('twins.version_op', '', true);
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

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'IDLE' THEN
        RAISE EXCEPTION 'another operation (%) is already running for domain %', v_operation_status, p_domain_id
            USING ERRCODE = '55006';
    END IF;
    IF v_locked THEN
        RAISE EXCEPTION 'domain % is frozen (locked)', p_domain_id USING ERRCODE = '55006';
    END IF;
    IF v_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'nothing to release: domain % has no open draft window (no changes since the last release)',
            p_domain_id;
    END IF;

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

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'RELEASING' THEN
        RAISE EXCEPTION 'domain % is not in RELEASING state', p_domain_id;
    END IF;
    IF v_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'current version of domain % must be OPEN, got %', p_domain_id, v_version_status;
    END IF;

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

    PERFORM set_config('twins.version_op', 'on', true);
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

    PERFORM domain_version_deleted_rows_cleanup(p_domain_id);

    PERFORM set_config('twins.version_op', '', true);
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

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'IDLE' THEN
        RAISE EXCEPTION 'another operation (%) is already running for domain %', v_operation_status, p_domain_id
            USING ERRCODE = '55006';
    END IF;

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

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'IMPORTING' THEN
        RAISE EXCEPTION 'domain % is not in IMPORTING state', p_domain_id;
    END IF;

    -- the import reconciled everything: an open draft window must be empty by now, collapse it
    IF v_current_status = 'OPEN' THEN
        IF domain_version_window_has_stamped_rows(p_domain_id, v_current_id) THEN
            RAISE EXCEPTION 'open window of domain % still has stamped rows: the import did not reconcile everything',
                p_domain_id;
        END IF;
        PERFORM set_config('twins.version_op', 'on', true);
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

    PERFORM set_config('twins.version_op', 'on', true);
    INSERT INTO domain_version (id, domain_id, version, status, "order", hash, previous_domain_version_id, released_at)
    VALUES (v_import_id, p_domain_id, v_label, 'RELEASED', v_order, p_content_hash, v_previous_id, CURRENT_TIMESTAMP);
    UPDATE domain SET current_domain_version_id = v_import_id WHERE id = p_domain_id;
    IF v_keep_window IS NOT NULL THEN
        DELETE FROM domain_version WHERE id = v_keep_window;
    END IF;
    PERFORM set_config('twins.version_op', '', true);

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

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'IDLE' THEN
        RAISE EXCEPTION 'another operation (%) is already running for domain %', v_operation_status, p_domain_id
            USING ERRCODE = '55006';
    END IF;
    IF v_locked THEN
        RAISE EXCEPTION 'domain % is frozen (locked)', p_domain_id USING ERRCODE = '55006';
    END IF;
    IF v_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'nothing to roll back: domain % has no open draft window', p_domain_id;
    END IF;

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

    IF NOT FOUND THEN
        RAISE EXCEPTION 'current domain version not found for domain %', p_domain_id;
    END IF;
    IF v_operation_status <> 'ROLLBACK' THEN
        RAISE EXCEPTION 'domain % is not in ROLLBACK state', p_domain_id;
    END IF;
    IF v_version_status <> 'OPEN' THEN
        RAISE EXCEPTION 'current version of domain % must be OPEN, got %', p_domain_id, v_version_status;
    END IF;

    -- collapse the empty window: the domain returns to the last released state verbatim
    IF NOT domain_version_window_has_stamped_rows(p_domain_id, v_window_id) THEN
        SELECT id INTO v_previous_id
          FROM domain_version
         WHERE domain_id = p_domain_id AND status = 'RELEASED'
         ORDER BY "order" DESC NULLS LAST, released_at DESC
         LIMIT 1;
        IF v_previous_id IS NOT NULL THEN
            PERFORM set_config('twins.version_op', 'on', true);
            -- move the pointer away first: the FK from domain.current would block the delete
            UPDATE domain SET current_domain_version_id = v_previous_id WHERE id = p_domain_id;
            DELETE FROM domain_version WHERE id = v_window_id;
            PERFORM set_config('twins.version_op', '', true);
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
CREATE OR REPLACE FUNCTION domain_version_reconcile(p_domain_id uuid, p_dry_run boolean DEFAULT true)
    RETURNS TABLE (table_name text, row_ctid tid, domain_version_id uuid) AS
$$
DECLARE
    r        record;
    v_chain  uuid[];
BEGIN
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

    FOR r IN SELECT table_name FROM domain_version_tables() LOOP
        RETURN QUERY
        EXECUTE format(
            'SELECT %L::text, ctid, domain_version_id FROM %I WHERE domain_id = $1 AND domain_version_id <> ALL($2)',
            r.table_name, r.table_name)
        USING p_domain_id, v_chain;
    END LOOP;

    IF NOT p_dry_run THEN
        FOR r IN SELECT table_name FROM domain_version_tables() LOOP
            EXECUTE format(
                'DELETE FROM %I WHERE domain_id = $1 AND domain_version_id <> ALL($2)',
                r.table_name)
            USING p_domain_id, v_chain;
        END LOOP;
    END IF;
END;
$$ LANGUAGE plpgsql;
