-- TWINS-719: domain configuration versioning, model v2 (pilot).
-- Step 4/4: triggers (docs/db_trigger_functions_convention.md: thin wrappers, logic in procedures).
--
-- twin_class already has canonical business wrappers: the versioning blocks are merged INTO
-- them (per the convention: one wrapper per operation per table). The other six tables have
-- no wrappers yet and get canonical ones carrying only the versioning logic.

-- ============================================================================
-- 0. Shared logic used by the wrappers below
-- ============================================================================

-- Is this a service path? import apply / internal versioning write / nested trigger write.
-- NULL-safe: an unset GUC reads as NULL, and NULL OR false is NULL which would silently
-- disable the whole versioning block guarded by IF NOT ... below.
CREATE OR REPLACE FUNCTION domain_version_protected_path() RETURNS boolean AS
$$
    SELECT coalesce(current_setting('twins.import_mode', true) = 'on', false)
        OR coalesce(current_setting('twins.version_op', true) = 'on', false)
        OR pg_trigger_depth() > 1;
$$ LANGUAGE sql;

-- Was the row born inside the current draft window (no release has ever published it)?
-- Only meaningful when the current version is an OPEN window; a RELEASED current means every
-- existing row was published (or restored), so the answer is always false.
CREATE OR REPLACE FUNCTION domain_version_row_is_born_in_window(
    p_table text, p_domain_id uuid, p_row_id uuid) RETURNS boolean AS
$$
DECLARE
    v_window  uuid;
    v_status  varchar;
    v_op      varchar;
BEGIN
    SELECT d.current_domain_version_id, dv.status
      INTO v_window, v_status
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = p_domain_id;

    IF v_status IS DISTINCT FROM 'OPEN' THEN
        RETURN false;
    END IF;

    SELECT a.operation
      INTO v_op
      FROM domain_version_audit a
     WHERE a.table_name = p_table
       AND a.row_id = p_row_id
       AND a.domain_version_id = v_window
     ORDER BY a.seq
     LIMIT 1;

    RETURN coalesce(v_op = 'INSERT', false);
END;
$$ LANGUAGE plpgsql;

-- INSERT core: validate incoming versioning columns, return the stamp
CREATE OR REPLACE FUNCTION domain_version_before_insert_core(
    p_table text, p_row_id uuid, p_domain_id uuid, p_version_id uuid, p_row_status varchar)
    RETURNS uuid AS
$$
BEGIN
    IF p_version_id IS NOT NULL THEN
        RAISE EXCEPTION '% %: domain_version_id is assigned by the versioning triggers only', p_table, p_row_id;
    END IF;
    IF p_row_status NOT IN ('ACTIVE', 'DISABLED') THEN
        RAISE EXCEPTION '% %: row_status must be ACTIVE or DISABLED on insert', p_table, p_row_id;
    END IF;
    RETURN domain_version_gate(p_domain_id);
END;
$$ LANGUAGE plpgsql;

-- INSERT resurrection: a repeated insert of a row that is soft-deleted in the current window
-- becomes an UPDATE of that row. Identical content restores the window-start image verbatim,
-- including the OLD stamp (the row leaves the window, no delta noise); different content is a
-- normal update stamped with the current window.
CREATE OR REPLACE FUNCTION domain_version_before_insert_resurrect(
    p_table text, p_pk_predicate text, p_new jsonb, p_row_id uuid)
    RETURNS boolean AS
$$
DECLARE
    v_old          jsonb;
    v_first_before jsonb;
    v_window       uuid;
    v_window_open  boolean;
    v_all_cols     text;
    v_cols         text;
BEGIN
    -- DELETED rows live only inside the open draft window (the release cleanup removes them
    -- physically), so resurrection is only possible while the current version is OPEN
    SELECT dv.id, dv.status = 'OPEN'
      INTO v_window, v_window_open
      FROM domain d
      JOIN domain_version dv ON dv.id = d.current_domain_version_id
     WHERE d.id = (p_new->>'domain_id')::uuid;
    IF NOT coalesce(v_window_open, false) THEN
        RETURN false;
    END IF;

    EXECUTE format('SELECT to_jsonb(r) FROM %I r WHERE %s AND r.row_status = ''DELETED''',
                   p_table, p_pk_predicate) INTO v_old;
    IF v_old IS NULL THEN
        RETURN false;
    END IF;

    -- window-start image of the row = before-image of its first audit record in this window
    SELECT a.before INTO v_first_before
      FROM domain_version_audit a
     WHERE a.table_name = p_table
       AND a.row_id = p_row_id
       AND a.domain_version_id = v_window
     ORDER BY a.seq
     LIMIT 1;

    SELECT string_agg(quote_ident(column_name), ', ' ORDER BY ordinal_position)
      INTO v_all_cols
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = p_table;
    SELECT string_agg(quote_ident(column_name), ', ' ORDER BY ordinal_position)
      INTO v_cols
      FROM information_schema.columns
     WHERE table_schema = 'public' AND table_name = p_table
       AND column_name NOT IN ('domain_version_id', 'row_status');

    PERFORM set_config('twins.version_op', 'on', true);
    IF v_first_before IS NOT NULL
       AND NOT domain_version_is_meaningful_change(p_table, v_first_before, p_new) THEN
        EXECUTE format('UPDATE %I r SET (%s) = (SELECT %s FROM jsonb_populate_record(null::%I, $1)) WHERE %s',
                       p_table, v_all_cols, v_all_cols, p_table, p_pk_predicate)
        USING v_first_before;
        -- no marker needed: the restoring UPDATE changes the stamp, so the AFTER wrapper
        -- classifies it as RESTORE in the window slice by itself
    ELSE
        EXECUTE format('UPDATE %I r SET row_status = ''ACTIVE'', domain_version_id = $2, (%s) = (SELECT %s FROM jsonb_populate_record(null::%I, $1)) WHERE %s',
                       p_table, v_cols, v_cols, p_table, p_pk_predicate)
        USING p_new, v_window;
    END IF;
    PERFORM set_config('twins.version_op', '', true);
    RETURN true;
END;
$$ LANGUAGE plpgsql;

-- DELETE core for the AFTER wrappers. A row born inside the current window and deleted in it
-- leaves the window with zero net effect — the same closing record as the identical-content
-- resurrection: RESTORE with after NULL. Everything else (soft-deleted published rows removed
-- by the release cleanup, import reconcile) is a plain DELETE.
CREATE OR REPLACE FUNCTION domain_version_after_delete_core(
    p_table text, p_domain_id uuid, p_version_id uuid, p_row_id uuid, p_before jsonb) RETURNS void AS
$$
BEGIN
    PERFORM domain_version_audit_write(p_table, p_domain_id, p_version_id, p_row_id,
        CASE WHEN domain_version_row_is_born_in_window(p_table, p_domain_id, p_row_id)
             THEN 'RESTORE' ELSE 'DELETE' END,
        p_before, NULL);
END;
$$ LANGUAGE plpgsql;

-- UPDATE core: protection of the versioning columns + restamp on a meaningful change.
-- Returns the new stamp, or NULL when the update is a no-op (nothing is restamped, no audit).
CREATE OR REPLACE FUNCTION domain_version_before_update_core(
    p_table text, p_row_id uuid,
    p_old_domain_id uuid, p_new_domain_id uuid,
    p_old_version_id uuid, p_new_version_id uuid,
    p_old_row_status varchar, p_new_row_status varchar,
    p_old jsonb, p_new jsonb)
    RETURNS uuid AS
$$
BEGIN
    IF p_new_domain_id IS DISTINCT FROM p_old_domain_id THEN
        RAISE EXCEPTION '% %: domain_id cannot be changed directly', p_table, p_row_id;
    END IF;
    IF p_new_version_id IS DISTINCT FROM p_old_version_id THEN
        RAISE EXCEPTION '% %: domain_version_id is assigned by the versioning triggers only', p_table, p_row_id;
    END IF;
    IF p_old_row_status <> 'DELETED' AND p_new_row_status = 'DELETED' THEN
        RAISE EXCEPTION '% %: use DELETE to delete a row (soft delete is trigger-managed)', p_table, p_row_id;
    END IF;
    IF p_old_row_status = 'DELETED' AND domain_version_is_meaningful_change(p_table, p_old, p_new) THEN
        RAISE EXCEPTION '% %: the row is deleted, restore it before editing', p_table, p_row_id;
    END IF;
    IF domain_version_is_meaningful_change(p_table, p_old, p_new) THEN
        RETURN domain_version_gate(p_new_domain_id);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- DELETE core. Returns true when the physical DELETE must be SKIPPED (converted / already
-- deleted / row physically removed here). p_pk_predicate is a fully rendered predicate,
-- e.g. format('id = %L', OLD.id) or the two-column variant for composite keys.
CREATE OR REPLACE FUNCTION domain_version_before_delete_core(
    p_table text, p_domain_id uuid, p_row_id uuid, p_row_status varchar,
    p_pk_predicate text) RETURNS boolean AS
$$
DECLARE
    v_window uuid;
BEGIN
    -- service paths (release cleanup / rollback apply / import reconcile) delete physically
    IF domain_version_protected_path() THEN
        RETURN false;
    END IF;
    -- system rows are not versioned
    IF p_domain_id IS NULL THEN
        RETURN false;
    END IF;
    -- already soft-deleted: nothing to do
    IF p_row_status = 'DELETED' THEN
        RETURN true;
    END IF;
    -- born in the current window: no release has ever seen this row -> delete physically right away
    IF domain_version_row_is_born_in_window(p_table, p_domain_id, p_row_id) THEN
        PERFORM set_config('twins.version_op', 'on', true);
        EXECUTE format('DELETE FROM %I WHERE %s', p_table, p_pk_predicate);
        PERFORM set_config('twins.version_op', '', true);
        RETURN true;
    END IF;
    -- normal path: convert the delete into a soft delete stamped with the current window;
    -- the row physically disappears at the release cleanup
    v_window := domain_version_gate(p_domain_id);
    PERFORM set_config('twins.version_op', 'on', true);
    EXECUTE format('UPDATE %I SET row_status = ''DELETED'', domain_version_id = %L WHERE %s',
                   p_table, v_window, p_pk_predicate);
    PERFORM set_config('twins.version_op', '', true);
    RETURN true;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 1. twin_class (root table): versioning is merged INTO the canonical business wrappers
--    (docs/db_trigger_functions_convention.md: one wrapper per operation per table).
--    Business calls keep their position; the versioning block runs after the
--    inherited-fields fill (before_insert) / last (before_update, after_*). The five
--    existing triggers are untouched (definitions unchanged, bodies replaced below);
--    twin_class_before_delete_wrapper_trigger is new (no business before-delete existed).
-- ============================================================================
CREATE OR REPLACE FUNCTION twin_class_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    new := twin_class_set_inherited_fields_on_insert(new);

    -- versioning (TWINS-719): after the inherited-fields fill, the resurrect comparison
    -- must see the final image
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    if new.extends_hierarchy_tree is distinct from old.extends_hierarchy_tree and old.extends_hierarchy_tree is not null then
        new := twin_class_update_extends_hierarchy_tree(old, new);
    end if;

    -- versioning (TWINS-719): last, the meaningful-change check must see the final NEW
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS twin_class_before_delete_wrapper_trigger ON twin_class;
CREATE TRIGGER twin_class_before_delete_wrapper_trigger
    BEFORE DELETE ON twin_class FOR EACH ROW
    EXECUTE FUNCTION twin_class_before_delete_wrapper();

CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    -- Call tree update on insert when extends_twin_class_id is set
    PERFORM hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    PERFORM twin_class_has_segments_check(new.head_twin_class_id);

    -- Update direct children counters for parents
    IF NEW.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(NEW.extends_twin_class_id, 'extends');
    END IF;

    IF NEW.head_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(NEW.head_twin_class_id, 'head');
    END IF;

    -- versioning (TWINS-719)
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    -- fn's if view_permission_id changed
    IF NEW.view_permission_id IS DISTINCT FROM OLD.view_permission_id THEN
        UPDATE twin t SET view_permission_id = NEW.view_permission_id FROM twin_class tc WHERE not t.view_permission_custom and t.twin_class_id = NEW.id;
    END IF;

    -- Update tree if extends_twin_class_id changed
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id
        -- we need to update tree only in case if extends_twin_class_id was updated and tree wasn't
        AND NEW.extends_hierarchy_tree IS NOT DISTINCT FROM OLD.extends_hierarchy_tree THEN
        PERFORM hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    END IF;

    -- Update tree and has_segments if head_twin_class_id changed
    IF NEW.head_twin_class_id IS DISTINCT FROM OLD.head_twin_class_id THEN
        PERFORM hierarchy_twin_class_head_process_tree_update(old, new, TG_OP);
        PERFORM twin_class_has_segments_check(old.head_twin_class_id);
        PERFORM twin_class_has_segments_check(new.head_twin_class_id);
    END IF;

    -- Update has_segments if segment changed
    IF NEW.segment IS DISTINCT FROM OLD.segment THEN
        PERFORM twin_class_has_segments_check(new.head_twin_class_id);
    END IF;

    -- Recalculate hierarchy if schema space fields changed
    IF (NEW.permission_schema_space IS DISTINCT FROM OLD.permission_schema_space)
        OR (NEW.twinflow_schema_space IS DISTINCT FROM OLD.twinflow_schema_space)
        OR (NEW.twin_class_schema_space IS DISTINCT FROM OLD.twin_class_schema_space)
        OR (NEW.alias_space IS DISTINCT FROM OLD.alias_space) THEN
        PERFORM twin_class_hierarchy_recalculate(old, new);
    END IF;

    -- Auto update permissions if key changed
    IF NEW.key IS DISTINCT FROM OLD.key THEN
        PERFORM permissions_autoupdate_on_twin_class_update(old, new);
    END IF;

    -- Update inherited bread_crumbs_face_id if changed
    IF NEW.bread_crumbs_face_id IS DISTINCT FROM OLD.bread_crumbs_face_id THEN
        PERFORM twin_class_update_inherited_bread_crumbs_face_id(old, new);
    END IF;

    -- Update inherited page_face_id if changed
    IF NEW.page_face_id IS DISTINCT FROM OLD.page_face_id THEN
        PERFORM twin_class_update_inherited_page_face_id(old, new);
    END IF;

    -- Update inherited marker_data_list_id id if changed
    IF NEW.marker_data_list_id IS DISTINCT FROM OLD.marker_data_list_id THEN
        PERFORM twin_class_update_inherited_marker_data_list(old, new);
    END IF;

    -- Update inherited tag_data_list_id id if changed
    IF NEW.tag_data_list_id IS DISTINCT FROM OLD.tag_data_list_id THEN
        PERFORM twin_class_update_inherited_tag_data_list(old, new);
    END IF;

    -- Update inherited flavor_data_list_id id if changed
    IF NEW.flavor_data_list_id IS DISTINCT FROM OLD.flavor_data_list_id THEN
        PERFORM twin_class_update_inherited_flavor_data_list(old, new);
    END IF;

    -- Update direct children counters if parent references changed
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id THEN
        -- Update old parent's counter
        IF OLD.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(OLD.extends_twin_class_id, 'extends');
        END IF;

        -- Update new parent's counter
        IF NEW.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(NEW.extends_twin_class_id, 'extends');
        END IF;
    END IF;

    IF NEW.head_twin_class_id IS DISTINCT FROM OLD.head_twin_class_id THEN
        -- Update old parent's counter
        IF OLD.head_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(OLD.head_twin_class_id, 'head');
        END IF;

        -- Update new parent's counter
        IF NEW.head_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(NEW.head_twin_class_id, 'head');
        END IF;
    END IF;

    -- versioning (TWINS-719)
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    -- Remove i18n and translations for deleted twin_class
    PERFORM twin_class_on_delete_i18n_and_translations_delete(old);
    PERFORM twin_class_has_segments_check(old.head_twin_class_id);

    -- Update direct children counters for parents
    IF OLD.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(OLD.extends_twin_class_id, 'extends');
    END IF;

    IF OLD.head_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(OLD.head_twin_class_id, 'head');
    END IF;

    -- versioning (TWINS-719)
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 2. twin_class_field (child of twin_class, uuid PK)
-- ============================================================================
CREATE OR REPLACE FUNCTION twin_class_field_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    -- domain consistency: derive from the parent row or verify the explicit value
    IF NEW.domain_id IS NULL THEN
        SELECT tc.domain_id INTO NEW.domain_id FROM twin_class tc WHERE tc.id = NEW.twin_class_id;
    ELSIF NEW.domain_id IS DISTINCT FROM (SELECT tc.domain_id FROM twin_class tc WHERE tc.id = NEW.twin_class_id) THEN
        RAISE EXCEPTION 'twin_class_field % belongs to a different domain than twin_class %', NEW.id, NEW.twin_class_id;
    END IF;
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS twin_class_field_before_insert_wrapper_trigger ON twin_class_field;
CREATE TRIGGER twin_class_field_before_insert_wrapper_trigger
    BEFORE INSERT ON twin_class_field FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_before_insert_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_before_update_wrapper_trigger ON twin_class_field;
CREATE TRIGGER twin_class_field_before_update_wrapper_trigger
    BEFORE UPDATE ON twin_class_field FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_before_update_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_before_delete_wrapper_trigger ON twin_class_field;
CREATE TRIGGER twin_class_field_before_delete_wrapper_trigger
    BEFORE DELETE ON twin_class_field FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_before_delete_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_after_insert_wrapper_trigger ON twin_class_field;
CREATE TRIGGER twin_class_field_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class_field FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_after_insert_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_after_update_wrapper_trigger ON twin_class_field;
CREATE TRIGGER twin_class_field_after_update_wrapper_trigger
    AFTER UPDATE ON twin_class_field FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_after_update_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_after_delete_wrapper_trigger ON twin_class_field;
CREATE TRIGGER twin_class_field_after_delete_wrapper_trigger
    AFTER DELETE ON twin_class_field FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_after_delete_wrapper();

-- ============================================================================
-- 3. twin_status (child of twin_class via twins_class_id — historic spelling, uuid PK)
-- ============================================================================
CREATE OR REPLACE FUNCTION twin_status_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.domain_id IS NULL THEN
        SELECT tc.domain_id INTO NEW.domain_id FROM twin_class tc WHERE tc.id = NEW.twin_class_id;
    ELSIF NEW.domain_id IS DISTINCT FROM (SELECT tc.domain_id FROM twin_class tc WHERE tc.id = NEW.twin_class_id) THEN
        RAISE EXCEPTION 'twin_status % belongs to a different domain than twin_class %', NEW.id, NEW.twin_class_id;
    END IF;
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_status_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_status_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_status_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_status_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_status_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS twin_status_before_insert_wrapper_trigger ON twin_status;
CREATE TRIGGER twin_status_before_insert_wrapper_trigger
    BEFORE INSERT ON twin_status FOR EACH ROW
    EXECUTE FUNCTION twin_status_before_insert_wrapper();

DROP TRIGGER IF EXISTS twin_status_before_update_wrapper_trigger ON twin_status;
CREATE TRIGGER twin_status_before_update_wrapper_trigger
    BEFORE UPDATE ON twin_status FOR EACH ROW
    EXECUTE FUNCTION twin_status_before_update_wrapper();

DROP TRIGGER IF EXISTS twin_status_before_delete_wrapper_trigger ON twin_status;
CREATE TRIGGER twin_status_before_delete_wrapper_trigger
    BEFORE DELETE ON twin_status FOR EACH ROW
    EXECUTE FUNCTION twin_status_before_delete_wrapper();

DROP TRIGGER IF EXISTS twin_status_after_insert_wrapper_trigger ON twin_status;
CREATE TRIGGER twin_status_after_insert_wrapper_trigger
    AFTER INSERT ON twin_status FOR EACH ROW
    EXECUTE FUNCTION twin_status_after_insert_wrapper();

DROP TRIGGER IF EXISTS twin_status_after_update_wrapper_trigger ON twin_status;
CREATE TRIGGER twin_status_after_update_wrapper_trigger
    AFTER UPDATE ON twin_status FOR EACH ROW
    EXECUTE FUNCTION twin_status_after_update_wrapper();

DROP TRIGGER IF EXISTS twin_status_after_delete_wrapper_trigger ON twin_status;
CREATE TRIGGER twin_status_after_delete_wrapper_trigger
    AFTER DELETE ON twin_status FOR EACH ROW
    EXECUTE FUNCTION twin_status_after_delete_wrapper();

-- ============================================================================
-- 4. data_list (root table, uuid PK)
-- ============================================================================
CREATE OR REPLACE FUNCTION data_list_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS data_list_before_insert_wrapper_trigger ON data_list;
CREATE TRIGGER data_list_before_insert_wrapper_trigger
    BEFORE INSERT ON data_list FOR EACH ROW
    EXECUTE FUNCTION data_list_before_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_before_update_wrapper_trigger ON data_list;
CREATE TRIGGER data_list_before_update_wrapper_trigger
    BEFORE UPDATE ON data_list FOR EACH ROW
    EXECUTE FUNCTION data_list_before_update_wrapper();

DROP TRIGGER IF EXISTS data_list_before_delete_wrapper_trigger ON data_list;
CREATE TRIGGER data_list_before_delete_wrapper_trigger
    BEFORE DELETE ON data_list FOR EACH ROW
    EXECUTE FUNCTION data_list_before_delete_wrapper();

DROP TRIGGER IF EXISTS data_list_after_insert_wrapper_trigger ON data_list;
CREATE TRIGGER data_list_after_insert_wrapper_trigger
    AFTER INSERT ON data_list FOR EACH ROW
    EXECUTE FUNCTION data_list_after_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_after_update_wrapper_trigger ON data_list;
CREATE TRIGGER data_list_after_update_wrapper_trigger
    AFTER UPDATE ON data_list FOR EACH ROW
    EXECUTE FUNCTION data_list_after_update_wrapper();

DROP TRIGGER IF EXISTS data_list_after_delete_wrapper_trigger ON data_list;
CREATE TRIGGER data_list_after_delete_wrapper_trigger
    AFTER DELETE ON data_list FOR EACH ROW
    EXECUTE FUNCTION data_list_after_delete_wrapper();

-- ============================================================================
-- 5. data_list_option (child of data_list, uuid PK)
-- ============================================================================
CREATE OR REPLACE FUNCTION data_list_option_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.domain_id IS NULL THEN
        SELECT dl.domain_id INTO NEW.domain_id FROM data_list dl WHERE dl.id = NEW.data_list_id;
    ELSIF NEW.domain_id IS DISTINCT FROM (SELECT dl.domain_id FROM data_list dl WHERE dl.id = NEW.data_list_id) THEN
        RAISE EXCEPTION 'data_list_option % belongs to a different domain than data_list %', NEW.id, NEW.data_list_id;
    END IF;
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_option_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_option_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_option_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_option_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_option_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS data_list_option_before_insert_wrapper_trigger ON data_list_option;
CREATE TRIGGER data_list_option_before_insert_wrapper_trigger
    BEFORE INSERT ON data_list_option FOR EACH ROW
    EXECUTE FUNCTION data_list_option_before_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_option_before_update_wrapper_trigger ON data_list_option;
CREATE TRIGGER data_list_option_before_update_wrapper_trigger
    BEFORE UPDATE ON data_list_option FOR EACH ROW
    EXECUTE FUNCTION data_list_option_before_update_wrapper();

DROP TRIGGER IF EXISTS data_list_option_before_delete_wrapper_trigger ON data_list_option;
CREATE TRIGGER data_list_option_before_delete_wrapper_trigger
    BEFORE DELETE ON data_list_option FOR EACH ROW
    EXECUTE FUNCTION data_list_option_before_delete_wrapper();

DROP TRIGGER IF EXISTS data_list_option_after_insert_wrapper_trigger ON data_list_option;
CREATE TRIGGER data_list_option_after_insert_wrapper_trigger
    AFTER INSERT ON data_list_option FOR EACH ROW
    EXECUTE FUNCTION data_list_option_after_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_option_after_update_wrapper_trigger ON data_list_option;
CREATE TRIGGER data_list_option_after_update_wrapper_trigger
    AFTER UPDATE ON data_list_option FOR EACH ROW
    EXECUTE FUNCTION data_list_option_after_update_wrapper();

DROP TRIGGER IF EXISTS data_list_option_after_delete_wrapper_trigger ON data_list_option;
CREATE TRIGGER data_list_option_after_delete_wrapper_trigger
    AFTER DELETE ON data_list_option FOR EACH ROW
    EXECUTE FUNCTION data_list_option_after_delete_wrapper();

-- ============================================================================
-- 6. data_list_subset (child of data_list, uuid PK)
-- ============================================================================
CREATE OR REPLACE FUNCTION data_list_subset_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.domain_id IS NULL THEN
        SELECT dl.domain_id INTO NEW.domain_id FROM data_list dl WHERE dl.id = NEW.data_list_id;
    ELSIF NEW.domain_id IS DISTINCT FROM (SELECT dl.domain_id FROM data_list dl WHERE dl.id = NEW.data_list_id) THEN
        RAISE EXCEPTION 'data_list_subset % belongs to a different domain than data_list %', NEW.id, NEW.data_list_id;
    END IF;
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS data_list_subset_before_insert_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_before_insert_wrapper_trigger
    BEFORE INSERT ON data_list_subset FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_before_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_before_update_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_before_update_wrapper_trigger
    BEFORE UPDATE ON data_list_subset FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_before_update_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_before_delete_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_before_delete_wrapper_trigger
    BEFORE DELETE ON data_list_subset FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_before_delete_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_after_insert_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_after_insert_wrapper_trigger
    AFTER INSERT ON data_list_subset FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_after_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_after_update_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_after_update_wrapper_trigger
    AFTER UPDATE ON data_list_subset FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_after_update_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_after_delete_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_after_delete_wrapper_trigger
    AFTER DELETE ON data_list_subset FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_after_delete_wrapper();

-- ============================================================================
-- 7. data_list_subset_option (child of data_list_subset; got a surrogate uuid PK in 01,
--    the composite (data_list_subset_id, data_list_option_id) is the business key)
-- ============================================================================
CREATE OR REPLACE FUNCTION data_list_subset_option_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.domain_id IS NULL THEN
        SELECT dls.domain_id INTO NEW.domain_id FROM data_list_subset dls WHERE dls.id = NEW.data_list_subset_id;
    ELSIF NEW.domain_id IS DISTINCT FROM (SELECT dls.domain_id FROM data_list_subset dls WHERE dls.id = NEW.data_list_subset_id) THEN
        RAISE EXCEPTION 'data_list_subset_option % belongs to a different domain than data_list_subset %',
            NEW.id, NEW.data_list_subset_id;
    END IF;
    IF NOT domain_version_protected_path() THEN
        IF domain_version_before_insert_resurrect(TG_TABLE_NAME, format('id = %L', NEW.id), to_jsonb(NEW), NEW.id) THEN
            RETURN NULL;  -- converted into a resurrection UPDATE of the soft-deleted row
        END IF;
        NEW.domain_version_id := domain_version_before_insert_core(
            TG_TABLE_NAME, NEW.id, NEW.domain_id, NEW.domain_version_id, NEW.row_status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_option_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NOT domain_version_protected_path() THEN
        NEW.domain_version_id := coalesce(domain_version_before_update_core(
            TG_TABLE_NAME, NEW.id,
            OLD.domain_id, NEW.domain_id, OLD.domain_version_id, NEW.domain_version_id,
            OLD.row_status, NEW.row_status, to_jsonb(OLD), to_jsonb(NEW)), NEW.domain_version_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_option_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF domain_version_before_delete_core(
            TG_TABLE_NAME, OLD.domain_id, OLD.id, OLD.row_status, format('id = %L', OLD.id)) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_option_after_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_audit_write(
        TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id, 'INSERT', NULL, to_jsonb(NEW));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_option_after_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.row_status = 'DELETED' AND OLD.row_status <> 'DELETED' THEN
        -- soft delete conversion: a user DELETE; the image lives in before, after is NULL like
        -- for any delete — the row still exists in the table with the DELETED status
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'DELETE', to_jsonb(OLD), NULL);
    ELSIF NEW.domain_version_id IS DISTINCT FROM OLD.domain_version_id
          AND NEW.domain_version_id IS DISTINCT FROM
              (SELECT d.current_domain_version_id FROM domain d WHERE d.id = NEW.domain_id)
          AND (SELECT d.operation_status FROM domain d WHERE d.id = NEW.domain_id) IN ('IDLE', 'ROLLBACK') THEN
        -- restore-class write: the stamp moved to an OLD version. A trigger restamp always
        -- stamps WITH the current window, so this only happens when the row RETURNS to a
        -- previous state and leaves the window: identical-content resurrection (IDLE) or the
        -- rollback apply (ROLLBACK). Import content application restamps rows with the import
        -- version and runs under IMPORTING — never matches here. One record: RESTORE in the
        -- window slice (OLD stamp), before = the state being left, after = the restored image.
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, OLD.domain_version_id, NEW.id,
            'RESTORE', to_jsonb(OLD), to_jsonb(NEW));
    ELSIF domain_version_is_meaningful_change(TG_TABLE_NAME, to_jsonb(OLD), to_jsonb(NEW)) THEN
        PERFORM domain_version_audit_write(
            TG_TABLE_NAME, NEW.domain_id, NEW.domain_version_id, NEW.id,
            'UPDATE', to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION data_list_subset_option_after_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    PERFORM domain_version_after_delete_core(
        TG_TABLE_NAME, OLD.domain_id, OLD.domain_version_id, OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS data_list_subset_option_before_insert_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_before_insert_wrapper_trigger
    BEFORE INSERT ON data_list_subset_option FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_option_before_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_before_update_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_before_update_wrapper_trigger
    BEFORE UPDATE ON data_list_subset_option FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_option_before_update_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_before_delete_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_before_delete_wrapper_trigger
    BEFORE DELETE ON data_list_subset_option FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_option_before_delete_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_after_insert_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_after_insert_wrapper_trigger
    AFTER INSERT ON data_list_subset_option FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_option_after_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_after_update_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_after_update_wrapper_trigger
    AFTER UPDATE ON data_list_subset_option FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_option_after_update_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_after_delete_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_after_delete_wrapper_trigger
    AFTER DELETE ON data_list_subset_option FOR EACH ROW
    EXECUTE FUNCTION data_list_subset_option_after_delete_wrapper();

-- ============================================================================
-- 8. domain birth: V1 is created before the domain row is inserted
--    (domain_version.domain_id FK is DEFERRABLE INITIALLY DEFERRED for exactly this)
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF NEW.current_domain_version_id IS NULL THEN
        NEW.current_domain_version_id := domain_version_init(NEW.id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS domain_before_insert_wrapper_trigger ON domain;
CREATE TRIGGER domain_before_insert_wrapper_trigger
    BEFORE INSERT ON domain FOR EACH ROW
    EXECUTE FUNCTION domain_before_insert_wrapper();

-- ============================================================================
-- 9. domain_version guard: writes only from the versioning functions (twins.version_op);
--    RELEASED rows are immutable unconditionally
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_version_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF current_setting('twins.version_op', true) <> 'on' AND pg_trigger_depth() <= 1 THEN
        RAISE EXCEPTION 'domain_version is written by the versioning functions only';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF OLD.status = 'RELEASED' THEN
        RAISE EXCEPTION 'released domain versions are immutable';
    END IF;
    IF current_setting('twins.version_op', true) <> 'on' AND pg_trigger_depth() <= 1 THEN
        RAISE EXCEPTION 'domain_version is written by the versioning functions only';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF OLD.status = 'RELEASED' THEN
        RAISE EXCEPTION 'released domain versions are immutable';
    END IF;
    IF current_setting('twins.version_op', true) <> 'on' AND pg_trigger_depth() <= 1 THEN
        RAISE EXCEPTION 'domain_version is written by the versioning functions only';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS domain_version_before_insert_wrapper_trigger ON domain_version;
CREATE TRIGGER domain_version_before_insert_wrapper_trigger
    BEFORE INSERT ON domain_version FOR EACH ROW
    EXECUTE FUNCTION domain_version_before_insert_wrapper();

DROP TRIGGER IF EXISTS domain_version_before_update_wrapper_trigger ON domain_version;
CREATE TRIGGER domain_version_before_update_wrapper_trigger
    BEFORE UPDATE ON domain_version FOR EACH ROW
    EXECUTE FUNCTION domain_version_before_update_wrapper();

DROP TRIGGER IF EXISTS domain_version_before_delete_wrapper_trigger ON domain_version;
CREATE TRIGGER domain_version_before_delete_wrapper_trigger
    BEFORE DELETE ON domain_version FOR EACH ROW
    EXECUTE FUNCTION domain_version_before_delete_wrapper();

-- ============================================================================
-- 10. domain_version_audit guard: INSERT only from the audit wrappers, history is immutable
--     (the future archive scheduler will run under twins.version_op)
-- ============================================================================
CREATE OR REPLACE FUNCTION domain_version_audit_before_insert_wrapper() RETURNS trigger AS
$$
BEGIN
    IF pg_trigger_depth() <= 1 AND current_setting('twins.version_op', true) <> 'on' THEN
        RAISE EXCEPTION 'domain_version_audit rows are written by the audit wrappers only';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_audit_before_update_wrapper() RETURNS trigger AS
$$
BEGIN
    IF current_setting('twins.version_op', true) <> 'on' THEN
        RAISE EXCEPTION 'domain_version_audit history is immutable';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_version_audit_before_delete_wrapper() RETURNS trigger AS
$$
BEGIN
    IF current_setting('twins.version_op', true) <> 'on' THEN
        RAISE EXCEPTION 'domain_version_audit history is immutable';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS domain_version_audit_before_insert_wrapper_trigger ON domain_version_audit;
CREATE TRIGGER domain_version_audit_before_insert_wrapper_trigger
    BEFORE INSERT ON domain_version_audit FOR EACH ROW
    EXECUTE FUNCTION domain_version_audit_before_insert_wrapper();

DROP TRIGGER IF EXISTS domain_version_audit_before_update_wrapper_trigger ON domain_version_audit;
CREATE TRIGGER domain_version_audit_before_update_wrapper_trigger
    BEFORE UPDATE ON domain_version_audit FOR EACH ROW
    EXECUTE FUNCTION domain_version_audit_before_update_wrapper();

DROP TRIGGER IF EXISTS domain_version_audit_before_delete_wrapper_trigger ON domain_version_audit;
CREATE TRIGGER domain_version_audit_before_delete_wrapper_trigger
    BEFORE DELETE ON domain_version_audit FOR EACH ROW
    EXECUTE FUNCTION domain_version_audit_before_delete_wrapper();
