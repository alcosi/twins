CREATE TABLE IF NOT EXISTS domain_config_audit
(
    id               uuid PRIMARY KEY,
    domain_id        uuid REFERENCES domain ON UPDATE CASCADE ON DELETE CASCADE,
    "table"          varchar         NOT NULL,
    row_id           uuid            NOT NULL,
    operation        varchar         NOT NULL,
    snapshot         jsonb           NOT NULL,
    changed_at       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id    uuid     REFERENCES "user" ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS domain_config_audit_domain_id_index
    ON domain_config_audit (domain_id);

CREATE INDEX IF NOT EXISTS domain_config_audit_actor_user_id_index
    ON domain_config_audit (actor_user_id);


-- Domain Config Audit Triggers
-- This migration adds CUD wrappers with audit to all domain configuration tables

-- ============================================================================
-- 1. Helper functions
-- ============================================================================

-- Get current user from session context
CREATE OR REPLACE FUNCTION current_actor_user_id_get() RETURNS UUID AS $$
BEGIN
    BEGIN
        RETURN current_setting('app.current_user_id', true)::UUID;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
    END;
END;
$$ LANGUAGE plpgsql;

-- Table to store domain_id resolution rules for tables without direct domain_id
CREATE TABLE IF NOT EXISTS domain_config_audit_resolve (
    table_name VARCHAR PRIMARY KEY,
    resolve_sql TEXT NOT NULL  -- SQL snippet that returns domain_id
);

-- Drop and recreate resolve rules with correct SQL (uses $1 as placeholder for p_snapshot)
TRUNCATE TABLE domain_config_audit_resolve;

INSERT INTO domain_config_audit_resolve (table_name, resolve_sql) VALUES
-- i18n tables: resolve through i18n_id -> i18n.domain_id
('i18n_translation', 'SELECT domain_id FROM i18n WHERE id = ($1->>''i18n_id'')::UUID'),
('i18n_translation_bin', 'SELECT domain_id FROM i18n WHERE id = ($1->>''i18n_id'')::UUID'),
('i18n_translation_style', 'SELECT domain_id FROM i18n WHERE id = ($1->>''i18n_id'')::UUID'),

-- twin_class tables: resolve through twin_class_id -> twin_class.domain_id
('twin_class_field', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_pointer', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_class_schema_map', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_action_permission', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_action_validator_rule', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- twin_pointer_validator_rule: resolve through twin_pointer_id -> twin_pointer -> twin_class
('twin_pointer_validator_rule', 'SELECT tc.domain_id FROM twin_pointer tp JOIN twin_class tc ON tp.twin_class_id = tc.id WHERE tp.id = ($1->>''twin_pointer_id'')::UUID'),

-- twin_class_field_condition: resolve through base_twin_class_field_id -> twin_class_field -> twin_class
('twin_class_field_condition', 'SELECT tc.domain_id FROM twin_class_field tcf JOIN twin_class tc ON tcf.twin_class_id = tc.id WHERE tcf.id = ($1->>''base_twin_class_field_id'')::UUID'),

-- twin_class_field_rule: resolve through twin_class_field_id -> twin_class_field -> twin_class
('twin_class_field_rule', 'SELECT tc.domain_id FROM twin_class_field tcf JOIN twin_class tc ON tcf.twin_class_id = tc.id WHERE tcf.id = ($1->>''twin_class_field_id'')::UUID'),

-- twin_comment tables
('twin_comment_action_alien_permission', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_comment_action_alien_validator_rule', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_comment_action_self', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- twin_attachment tables
('twin_attachment_action_alien_permission', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_attachment_action_alien_validator_rule', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_attachment_action_self_validator_rule', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- twin_factory tables
('twin_factory_branch', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_condition', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_eraser', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_multiplier', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_multiplier_filter', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_pipeline', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_pipeline_step', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- twin_validator tables
('twin_validator', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_validator_set', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- twinflow tables
('twinflow_schema_map', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twinflow_transition_alias', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twinflow_transition_trigger', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twinflow_transition_validator_rule', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID');

-- Write audit record
CREATE OR REPLACE FUNCTION domain_config_audit_write(
    p_table_name TEXT,
    p_operation TEXT,
    p_row_id UUID,
    p_snapshot JSONB
) RETURNS void AS $$
DECLARE
    v_domain_id UUID;
    v_resolve_sql TEXT;
BEGIN
    -- 1. Try to get domain_id directly from snapshot (may be NULL for global records)
    v_domain_id := (p_snapshot->>'domain_id')::UUID;

    -- 2. If not found, try through domain_version_id
    IF v_domain_id IS NULL AND (p_snapshot->>'domain_version_id') IS NOT NULL THEN
        SELECT domain_id INTO v_domain_id
        FROM domain_version
        WHERE id = (p_snapshot->>'domain_version_id')::UUID;
    END IF;

    -- 3. If still not found, try through FK chains using resolve rules
    IF v_domain_id IS NULL THEN
        SELECT resolve_sql INTO v_resolve_sql
        FROM domain_config_audit_resolve
        WHERE table_name = p_table_name;

        IF v_resolve_sql IS NOT NULL THEN
            EXECUTE 'SELECT (' || v_resolve_sql || ')::uuid'
            INTO v_domain_id
            USING p_snapshot;
        END IF;
    END IF;

    -- 4. Write audit record (domain_id can be NULL for global records)
    INSERT INTO domain_config_audit (
        id,
        domain_id,
        "table",
        row_id,
        operation,
        snapshot,
        changed_at,
        actor_user_id
    ) VALUES (
        uuid_generate_v7_custom(),
        v_domain_id,
        p_table_name,
        p_row_id,
        p_operation,
        p_snapshot,
        CURRENT_TIMESTAMP,
        current_actor_user_id_get()
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 2. Update EXISTING wrapper functions - add audit at the END (from V1.4.130.01 + V1.4.170.01)
-- ============================================================================

-- twin_class_after_insert_wrapper (from V1.4.130.01) + audit
CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
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

    -- Add audit record
    PERFORM domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));

    RETURN NEW;
END;
$$;

-- twin_class_after_delete_wrapper (from V1.4.130.01) + audit
CREATE OR REPLACE FUNCTION twin_class_after_delete_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
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

    -- Add audit record
    PERFORM domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));

    RETURN OLD;
END;
$$;

-- twin_class_after_update_wrapper (from V1.4.170.01) + audit
CREATE OR REPLACE FUNCTION twin_class_after_update_wrapper()
    returns trigger
    language plpgsql
as
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

    -- Add audit record (only if data actually changed)
    IF (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') THEN
        PERFORM domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW) - 'updated_at');
    END IF;

    RETURN NEW;
END;
$$;

-- twin_class_field_condition wrappers - preserve existing logic
CREATE OR REPLACE FUNCTION twin_class_field_condition_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform twin_class_field_has_dependent_fields_check(new.base_twin_class_field_id);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));

    return new;
end;
$$;

CREATE OR REPLACE FUNCTION twin_class_field_condition_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    if new.base_twin_class_field_id is distinct from old.base_twin_class_field_id then
        perform twin_class_field_has_dependent_fields_check(old.base_twin_class_field_id);
        perform twin_class_field_has_dependent_fields_check(new.base_twin_class_field_id);
    end if;

    -- Add audit record (only if data actually changed)
    if (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') then
        perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW) - 'updated_at');
    end if;

    return new;
end;
$$;

CREATE OR REPLACE FUNCTION twin_class_field_condition_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform twin_class_field_has_dependent_fields_check(old.base_twin_class_field_id);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));

    return old;
end;
$$;

-- twin_class_field_rule wrappers - preserve existing logic
CREATE OR REPLACE FUNCTION twin_class_field_rule_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform twin_class_field_is_dependent_field_check(new.twin_class_field_id);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));

    return new;
end;
$$;

CREATE OR REPLACE FUNCTION twin_class_field_rule_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    if new.twin_class_field_id is distinct from old.twin_class_field_id then
        perform twin_class_field_is_dependent_field_check(old.twin_class_field_id);
        perform twin_class_field_is_dependent_field_check(new.twin_class_field_id);
    end if;

    -- Add audit record (only if data actually changed)
    if (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') then
        perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW) - 'updated_at');
    end if;

    return new;
end;
$$;

CREATE OR REPLACE FUNCTION twin_class_field_rule_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform twin_class_field_is_dependent_field_check(old.twin_class_field_id);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));

    return old;
end;
$$;

-- ============================================================================
-- Create triggers for updated wrapper functions
-- ============================================================================

DROP TRIGGER IF EXISTS twin_class_after_insert_wrapper_trigger ON twin_class;
CREATE TRIGGER twin_class_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_after_insert_wrapper();

DROP TRIGGER IF EXISTS twin_class_after_update_wrapper_trigger ON twin_class;
CREATE TRIGGER twin_class_after_update_wrapper_trigger
    AFTER UPDATE ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_after_update_wrapper();

DROP TRIGGER IF EXISTS twin_class_after_delete_wrapper_trigger ON twin_class;
CREATE TRIGGER twin_class_after_delete_wrapper_trigger
    AFTER DELETE ON twin_class
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_after_delete_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_condition_after_insert_wrapper_trigger ON twin_class_field_condition;
CREATE TRIGGER twin_class_field_condition_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class_field_condition
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_condition_after_insert_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_condition_after_update_wrapper_trigger ON twin_class_field_condition;
CREATE TRIGGER twin_class_field_condition_after_update_wrapper_trigger
    AFTER UPDATE ON twin_class_field_condition
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_condition_after_update_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_condition_after_delete_wrapper_trigger ON twin_class_field_condition;
CREATE TRIGGER twin_class_field_condition_after_delete_wrapper_trigger
    AFTER DELETE ON twin_class_field_condition
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_condition_after_delete_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_rule_after_insert_wrapper_trigger ON twin_class_field_rule;
CREATE TRIGGER twin_class_field_rule_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class_field_rule
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_rule_after_insert_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_rule_after_update_wrapper_trigger ON twin_class_field_rule;
CREATE TRIGGER twin_class_field_rule_after_update_wrapper_trigger
    AFTER UPDATE ON twin_class_field_rule
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_rule_after_update_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_rule_after_delete_wrapper_trigger ON twin_class_field_rule;
CREATE TRIGGER twin_class_field_rule_after_delete_wrapper_trigger
    AFTER DELETE ON twin_class_field_rule
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_rule_after_delete_wrapper();

-- tier_after_update_wrapper (from V1.4.50.01) + audit
CREATE OR REPLACE FUNCTION tier_after_update_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id OR
       OLD.twinflow_schema_id IS DISTINCT FROM NEW.twinflow_schema_id OR
       OLD.twin_class_schema_id IS DISTINCT FROM NEW.twin_class_schema_id OR
       OLD.notification_schema_id IS DISTINCT FROM NEW.notification_schema_id OR
       (OLD.custom IS DISTINCT FROM NEW.custom AND NOT NEW.custom) THEN

        PERFORM business_account_update_all(
                NEW.id,
                NEW.domain_id,
                NEW.permission_schema_id,
                NEW.twinflow_schema_id,
                NEW.twin_class_schema_id,
                NEW.notification_schema_id,
                NEW.custom
                );
    END IF;

    -- Add audit record (only if data actually changed)
    IF (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') THEN
        PERFORM domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW) - 'updated_at');
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- tier insert and delete wrappers (no insert/delete wrappers exist, only update in V1.4.50.01)
CREATE OR REPLACE FUNCTION tier_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    -- Add audit record
    PERFORM domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));

    return new;
end;
$$;

CREATE OR REPLACE FUNCTION tier_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    -- Add audit record
    PERFORM domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));

    return old;
end;
$$;

DROP TRIGGER IF EXISTS tier_after_insert_wrapper_trigger ON tier;
CREATE TRIGGER tier_after_insert_wrapper_trigger
    AFTER INSERT ON tier
    FOR EACH ROW
    EXECUTE FUNCTION tier_after_insert_wrapper();

DROP TRIGGER IF EXISTS tier_after_delete_wrapper_trigger ON tier;
CREATE TRIGGER tier_after_delete_wrapper_trigger
    AFTER DELETE ON tier
    FOR EACH ROW
    EXECUTE FUNCTION tier_after_delete_wrapper();

-- Note: tier_after_update_wrapper trigger already exists in V1.4.50.01
-- and was updated above with audit logic

-- Note: twin_class_field_condition and twin_class_field_rule triggers already exist in V1.3.451.01

-- ============================================================================
-- 3. Create NEW wrapper functions + triggers for remaining tables
-- ============================================================================

-- Helper to create wrapper and trigger for a table
CREATE OR REPLACE FUNCTION create_domain_config_audit_wrapper(p_table_name TEXT) RETURNS void AS $$
BEGIN
    -- Create insert wrapper
    EXECUTE format('
        CREATE OR REPLACE FUNCTION %I_after_insert_wrapper() returns trigger
            language plpgsql
        as $func$
        begin
            perform domain_config_audit_write(%L, ''INSERT'', NEW.id, to_jsonb(NEW));
            return new;
        end;
        $func$;', p_table_name, p_table_name);

    -- Create update wrapper
    EXECUTE format('
        CREATE OR REPLACE FUNCTION %I_after_update_wrapper() returns trigger
            language plpgsql
        as $func$
        begin
            if (to_jsonb(OLD) - ''updated_at'') != (to_jsonb(NEW) - ''updated_at'') then
                perform domain_config_audit_write(%L, ''UPDATE'', NEW.id, to_jsonb(NEW) - ''updated_at'');
            end if;
            return new;
        end;
        $func$;', p_table_name, p_table_name);

    -- Create delete wrapper
    EXECUTE format('
        CREATE OR REPLACE FUNCTION %I_after_delete_wrapper() returns trigger
            language plpgsql
        as $func$
        begin
            perform domain_config_audit_write(%L, ''DELETE'', OLD.id, to_jsonb(OLD));
            return old;
        end;
        $func$;', p_table_name, p_table_name);

    -- Create triggers
    EXECUTE format('
        DROP TRIGGER IF EXISTS %I_after_insert_wrapper_trigger ON %I;
        CREATE TRIGGER %I_after_insert_wrapper_trigger
            AFTER INSERT ON %I
            FOR EACH ROW
            EXECUTE FUNCTION %I_after_insert_wrapper();',
        p_table_name, p_table_name, p_table_name, p_table_name, p_table_name);

    EXECUTE format('
        DROP TRIGGER IF EXISTS %I_after_update_wrapper_trigger ON %I;
        CREATE TRIGGER %I_after_update_wrapper_trigger
            AFTER UPDATE ON %I
            FOR EACH ROW
            EXECUTE FUNCTION %I_after_update_wrapper();',
        p_table_name, p_table_name, p_table_name, p_table_name, p_table_name);

    EXECUTE format('
        DROP TRIGGER IF EXISTS %I_after_delete_wrapper_trigger ON %I;
        CREATE TRIGGER %I_after_delete_wrapper_trigger
            AFTER DELETE ON %I
            FOR EACH ROW
            EXECUTE FUNCTION %I_after_delete_wrapper();',
        p_table_name, p_table_name, p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;

-- Create wrappers for all remaining domain config tables

-- Permission tables
SELECT create_domain_config_audit_wrapper('permission');
SELECT create_domain_config_audit_wrapper('permission_group');
SELECT create_domain_config_audit_wrapper('permission_schema');

-- Data list tables
SELECT create_domain_config_audit_wrapper('data_list');
SELECT create_domain_config_audit_wrapper('data_list_option');
SELECT create_domain_config_audit_wrapper('data_list_option_search');
SELECT create_domain_config_audit_wrapper('data_list_subset');
SELECT create_domain_config_audit_wrapper('data_list_subset_option');

-- Domain tables
SELECT create_domain_config_audit_wrapper('domain_locale');

-- Error tables
SELECT create_domain_config_audit_wrapper('error');

-- Face tables
SELECT create_domain_config_audit_wrapper('face');
SELECT create_domain_config_audit_wrapper('face_navbar_nb001');
SELECT create_domain_config_audit_wrapper('face_navbar_nb001_menu_item');
SELECT create_domain_config_audit_wrapper('face_pg001');
SELECT create_domain_config_audit_wrapper('face_pg001_widget');
SELECT create_domain_config_audit_wrapper('face_pg002');
SELECT create_domain_config_audit_wrapper('face_pg002_layout');
SELECT create_domain_config_audit_wrapper('face_pg002_tab');
SELECT create_domain_config_audit_wrapper('face_pg002_widget');
SELECT create_domain_config_audit_wrapper('face_tc001');
SELECT create_domain_config_audit_wrapper('face_tw001');
SELECT create_domain_config_audit_wrapper('face_tw002');
SELECT create_domain_config_audit_wrapper('face_tw002_accordion_item');
SELECT create_domain_config_audit_wrapper('face_tw004');
SELECT create_domain_config_audit_wrapper('face_tw005');
SELECT create_domain_config_audit_wrapper('face_tw005_button');
SELECT create_domain_config_audit_wrapper('face_wt001');
SELECT create_domain_config_audit_wrapper('face_wt001_column');
SELECT create_domain_config_audit_wrapper('face_wt002');
SELECT create_domain_config_audit_wrapper('face_wt002_button');
SELECT create_domain_config_audit_wrapper('face_wt003');

-- History type tables
SELECT create_domain_config_audit_wrapper('history_type_config_domain');
SELECT create_domain_config_audit_wrapper('history_type_config_twin_class');
SELECT create_domain_config_audit_wrapper('history_type_config_twin_class_field');
SELECT create_domain_config_audit_wrapper('history_notification_recipient');

-- I18n tables
SELECT create_domain_config_audit_wrapper('i18n');

-- i18n_translation and i18n_translation_bin have composite PK (i18n_id + locale), no single id column
-- Custom wrappers using i18n_id as row_id
CREATE OR REPLACE FUNCTION i18n_translation_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation', 'INSERT', NEW.i18n_id, to_jsonb(NEW));
    return new;
end;
$$;

CREATE OR REPLACE FUNCTION i18n_translation_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    if (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') then
        perform domain_config_audit_write('i18n_translation', 'UPDATE', NEW.i18n_id, to_jsonb(NEW) - 'updated_at');
    end if;
    return new;
end;
$$;

CREATE OR REPLACE FUNCTION i18n_translation_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation', 'DELETE', OLD.i18n_id, to_jsonb(OLD));
    return old;
end;
$$;

DROP TRIGGER IF EXISTS i18n_translation_after_insert_wrapper_trigger ON i18n_translation;
CREATE TRIGGER i18n_translation_after_insert_wrapper_trigger
    AFTER INSERT ON i18n_translation
    FOR EACH ROW
    EXECUTE FUNCTION i18n_translation_after_insert_wrapper();

DROP TRIGGER IF EXISTS i18n_translation_after_update_wrapper_trigger ON i18n_translation;
CREATE TRIGGER i18n_translation_after_update_wrapper_trigger
    AFTER UPDATE ON i18n_translation
    FOR EACH ROW
    EXECUTE FUNCTION i18n_translation_after_update_wrapper();

DROP TRIGGER IF EXISTS i18n_translation_after_delete_wrapper_trigger ON i18n_translation;
CREATE TRIGGER i18n_translation_after_delete_wrapper_trigger
    AFTER DELETE ON i18n_translation
    FOR EACH ROW
    EXECUTE FUNCTION i18n_translation_after_delete_wrapper();

CREATE OR REPLACE FUNCTION i18n_translation_bin_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation_bin', 'INSERT', NEW.i18n_id, to_jsonb(NEW));
    return new;
end;
$$;

CREATE OR REPLACE FUNCTION i18n_translation_bin_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    if (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') then
        perform domain_config_audit_write('i18n_translation_bin', 'UPDATE', NEW.i18n_id, to_jsonb(NEW) - 'updated_at');
    end if;
    return new;
end;
$$;

CREATE OR REPLACE FUNCTION i18n_translation_bin_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation_bin', 'DELETE', OLD.i18n_id, to_jsonb(OLD));
    return old;
end;
$$;

DROP TRIGGER IF EXISTS i18n_translation_bin_after_insert_wrapper_trigger ON i18n_translation_bin;
CREATE TRIGGER i18n_translation_bin_after_insert_wrapper_trigger
    AFTER INSERT ON i18n_translation_bin
    FOR EACH ROW
    EXECUTE FUNCTION i18n_translation_bin_after_insert_wrapper();

DROP TRIGGER IF EXISTS i18n_translation_bin_after_update_wrapper_trigger ON i18n_translation_bin;
CREATE TRIGGER i18n_translation_bin_after_update_wrapper_trigger
    AFTER UPDATE ON i18n_translation_bin
    FOR EACH ROW
    EXECUTE FUNCTION i18n_translation_bin_after_update_wrapper();

DROP TRIGGER IF EXISTS i18n_translation_bin_after_delete_wrapper_trigger ON i18n_translation_bin;
CREATE TRIGGER i18n_translation_bin_after_delete_wrapper_trigger
    AFTER DELETE ON i18n_translation_bin
    FOR EACH ROW
    EXECUTE FUNCTION i18n_translation_bin_after_delete_wrapper();

-- i18n_translation_style has single id column, can use generic wrapper
SELECT create_domain_config_audit_wrapper('i18n_translation_style');

-- Identity provider tables
SELECT create_domain_config_audit_wrapper('identity_provider');
SELECT create_domain_config_audit_wrapper('identity_provider_internal_token');
SELECT create_domain_config_audit_wrapper('identity_provider_internal_user');

-- Link tables
SELECT create_domain_config_audit_wrapper('link');
SELECT create_domain_config_audit_wrapper('link_trigger');
SELECT create_domain_config_audit_wrapper('link_validator');

-- Notification tables
SELECT create_domain_config_audit_wrapper('notification_channel');
SELECT create_domain_config_audit_wrapper('notification_context');
SELECT create_domain_config_audit_wrapper('notification_email');
SELECT create_domain_config_audit_wrapper('notification_schema');

-- Permission grant tables
SELECT create_domain_config_audit_wrapper('permission_grant_assignee_propagation');
SELECT create_domain_config_audit_wrapper('permission_grant_global');
SELECT create_domain_config_audit_wrapper('permission_grant_space_role');
SELECT create_domain_config_audit_wrapper('permission_grant_twin_role');
SELECT create_domain_config_audit_wrapper('permission_grant_user');
SELECT create_domain_config_audit_wrapper('permission_grant_user_group');

-- Resource tables
SELECT create_domain_config_audit_wrapper('resource');

-- Projection tables
SELECT create_domain_config_audit_wrapper('projection_type');
SELECT create_domain_config_audit_wrapper('projection_type_group');

-- Search tables
SELECT create_domain_config_audit_wrapper('twin_search');
SELECT create_domain_config_audit_wrapper('twin_search_alias');
SELECT create_domain_config_audit_wrapper('twin_search_predicate');

-- Space tables
SELECT create_domain_config_audit_wrapper('space_role');
SELECT create_domain_config_audit_wrapper('space_role_user_group');

-- Storage tables
SELECT create_domain_config_audit_wrapper('storage');

-- Template tables
SELECT create_domain_config_audit_wrapper('template_generator');

-- Scheduler tables
SELECT create_domain_config_audit_wrapper('scheduler');

-- Tier tables (updated separately above - tier_after_update_wrapper from V1.4.50.01 + audit)
-- Note: tier only has update wrapper in V1.4.50.01, insert/delete wrappers created below

-- Twin action tables
SELECT create_domain_config_audit_wrapper('twin_action_permission');
SELECT create_domain_config_audit_wrapper('twin_action_validator_rule');

-- Twin attachment tables
SELECT create_domain_config_audit_wrapper('twin_attachment_action_alien_permission');
SELECT create_domain_config_audit_wrapper('twin_attachment_action_alien_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_attachment_action_self_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_attachment_restriction');

-- Twin class tables
SELECT create_domain_config_audit_wrapper('twin_class_field');
SELECT create_domain_config_audit_wrapper('twin_class_schema_map');

-- Twin comment tables
SELECT create_domain_config_audit_wrapper('twin_comment_action_alien_permission');
SELECT create_domain_config_audit_wrapper('twin_comment_action_alien_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_comment_action_self');

-- Twin factory tables
SELECT create_domain_config_audit_wrapper('twin_factory_branch');
SELECT create_domain_config_audit_wrapper('twin_factory_condition');
SELECT create_domain_config_audit_wrapper('twin_factory_eraser');
SELECT create_domain_config_audit_wrapper('twin_factory_multiplier');
SELECT create_domain_config_audit_wrapper('twin_factory_multiplier_filter');
SELECT create_domain_config_audit_wrapper('twin_factory_pipeline');
SELECT create_domain_config_audit_wrapper('twin_factory_pipeline_step');

-- Twin pointer tables
SELECT create_domain_config_audit_wrapper('twin_pointer');
SELECT create_domain_config_audit_wrapper('twin_pointer_validator_rule');

-- Twin status tables
SELECT create_domain_config_audit_wrapper('twin_status');
SELECT create_domain_config_audit_wrapper('twin_status_group');
SELECT create_domain_config_audit_wrapper('twin_status_group_map');
SELECT create_domain_config_audit_wrapper('twin_status_transition_trigger');

-- Twin validator tables
SELECT create_domain_config_audit_wrapper('twin_validator');
SELECT create_domain_config_audit_wrapper('twin_validator_set');

-- Twinflow tables
SELECT create_domain_config_audit_wrapper('twinflow_schema');
SELECT create_domain_config_audit_wrapper('twinflow_schema_map');
SELECT create_domain_config_audit_wrapper('twinflow_transition_alias');
SELECT create_domain_config_audit_wrapper('twinflow_transition_trigger');
SELECT create_domain_config_audit_wrapper('twinflow_transition_validator_rule');

-- Email sender
SELECT create_domain_config_audit_wrapper('email_sender');

-- Clean up helper function
DROP FUNCTION create_domain_config_audit_wrapper(TEXT);
