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
CREATE OR REPLACE FUNCTION get_current_actor_user_id() RETURNS UUID AS $$
BEGIN
    BEGIN
        RETURN current_setting('app.current_user_id', true)::UUID;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
    END;
END;
$$ LANGUAGE plpgsql;

-- Write audit record
CREATE OR REPLACE FUNCTION domain_config_audit_write(
    p_table_name TEXT,
    p_operation TEXT,
    p_row_id UUID,
    p_snapshot JSONB
) RETURNS void AS $$
DECLARE
    v_domain_id UUID;
BEGIN
    -- Try to get domain_id directly from snapshot
    v_domain_id := (p_snapshot->>'domain_id')::UUID;

    -- If not found, try through domain_version_id
    IF v_domain_id IS NULL AND (p_snapshot->>'domain_version_id') IS NOT NULL THEN
        SELECT domain_id INTO v_domain_id
        FROM domain_version
        WHERE id = (p_snapshot->>'domain_version_id')::UUID;
    END IF;

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
        gen_random_uuid(),
        v_domain_id,
        p_table_name,
        p_row_id,
        p_operation,
        p_snapshot,
        CURRENT_TIMESTAMP,
        get_current_actor_user_id()
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 2. Update EXISTING wrapper functions - add audit at the END
-- ============================================================================

-- twin_class_after_insert_wrapper - preserve existing logic
CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper()
    returns trigger
    language plpgsql
as $$
begin
    -- Call tree update on insert when extends_twin_class_id is set
    perform hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);

    -- Set inherited face data for new twin_class
    perform twin_class_set_inherited_face_on_insert(old, new);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));

    return new;
end;
$$;

-- twin_class_after_update_wrapper - preserve all existing logic
CREATE OR REPLACE FUNCTION twin_class_after_update_wrapper()
    returns trigger
    language plpgsql
as $$
begin
    -- Update tree if extends_twin_class_id changed
    if new.extends_twin_class_id is distinct from old.extends_twin_class_id then
        perform hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    end if;

    -- Update tree if head_twin_class_id changed
    if new.head_twin_class_id is distinct from old.head_twin_class_id then
        perform hierarchy_twin_class_head_process_tree_update(old, new, TG_OP);
    end if;

    -- Recalculate hierarchy if schema space fields changed
    if (new.permission_schema_space is distinct from old.permission_schema_space)
        or (new.twinflow_schema_space is distinct from old.twinflow_schema_space)
        or (new.twin_class_schema_space is distinct from old.twin_class_schema_space)
        or (new.alias_space is distinct from old.alias_space) then
        perform twin_class_hierarchy_recalculate(old, new);
    end if;

    -- Auto update permissions if key changed
    if new.key is distinct from old.key then
        perform permissions_autoupdate_on_twin_class_update(old, new);
    end if;

    -- Update inherited breadcrumbs face id if changed
    if new.bread_crumbs_face_id is distinct from old.bread_crumbs_face_id then
        perform twin_class_update_inherited_bread_crumbs_face_id(old, new);
    end if;

    -- Update inherited page face id if changed
    if new.page_face_id is distinct from old.page_face_id then
        perform twin_class_update_inherited_page_face_id(old, new);
    end if;

    -- Add audit record (only if data actually changed)
    if (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') then
        perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW) - 'updated_at');
    end if;

    return new;
end;
$$;

-- twin_class_after_delete_wrapper - preserve existing logic
CREATE OR REPLACE FUNCTION twin_class_after_delete_wrapper()
    returns trigger
    language plpgsql
as $$
begin
    -- Remove i18n and translations for deleted twin_class
    perform twin_class_on_delete_i18n_and_translations_delete(old);

    -- Add audit record BEFORE delete
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));

    return old;
end;
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

-- Data list tables
SELECT create_domain_config_audit_wrapper('data_list');
SELECT create_domain_config_audit_wrapper('data_list_option');
SELECT create_domain_config_audit_wrapper('data_list_subset');
SELECT create_domain_config_audit_wrapper('data_list_subset_option');

-- Domain tables
SELECT create_domain_config_audit_wrapper('domain_locale');

-- Error tables
SELECT create_domain_config_audit_wrapper('error');

-- Face tables
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

-- I18n tables
SELECT create_domain_config_audit_wrapper('i18n');
SELECT create_domain_config_audit_wrapper('i18n_translation');
SELECT create_domain_config_audit_wrapper('i18n_translation_bin');
SELECT create_domain_config_audit_wrapper('i18n_translation_style');

-- Identity provider tables
SELECT create_domain_config_audit_wrapper('identity_provider');
SELECT create_domain_config_audit_wrapper('identity_provider_internal_token');
SELECT create_domain_config_audit_wrapper('identity_provider_internal_user');

-- Link tables
SELECT create_domain_config_audit_wrapper('link_trigger');
SELECT create_domain_config_audit_wrapper('link_validator');

-- Notification tables
SELECT create_domain_config_audit_wrapper('notification_email');

-- Permission grant tables
SELECT create_domain_config_audit_wrapper('permission_grant_assignee_propagation');
SELECT create_domain_config_audit_wrapper('permission_grant_global');
SELECT create_domain_config_audit_wrapper('permission_grant_space_role');
SELECT create_domain_config_audit_wrapper('permission_grant_twin_role');
SELECT create_domain_config_audit_wrapper('permission_grant_user');
SELECT create_domain_config_audit_wrapper('permission_grant_user_group');

-- Resource tables
SELECT create_domain_config_audit_wrapper('resource');

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

-- Tier tables
SELECT create_domain_config_audit_wrapper('tier');

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
SELECT create_domain_config_audit_wrapper('twinflow_schema_map');
SELECT create_domain_config_audit_wrapper('twinflow_transition_alias');
SELECT create_domain_config_audit_wrapper('twinflow_transition_trigger');
SELECT create_domain_config_audit_wrapper('twinflow_transition_validator_rule');

-- Email sender
SELECT create_domain_config_audit_wrapper('email_sender');

-- Clean up helper function
DROP FUNCTION create_domain_config_audit_wrapper(TEXT);
