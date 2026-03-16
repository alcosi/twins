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

-- twin_class_dynamic_marker: resolve through twin_class.domain_id
('twin_class_dynamic_marker', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- twin_class_field_attribute: resolve through twin_class.domain_id
('twin_class_field_attribute', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
-- twin_class_field_rule_map: resolve through twin_class_field -> twin_class.domain_id
('twin_class_field_rule_map', 'SELECT tc.domain_id FROM twin_class_field tcf JOIN twin_class tc ON tcf.twin_class_id = tc.id WHERE tcf.id = ($1->>''twin_class_field_id'')::UUID'),

-- twin_class_field_search_predicate: resolve through twin_class_field_search -> twin_class.domain_id
('twin_class_field_search_predicate', 'SELECT tc.domain_id FROM twin_class_field_search tcfs JOIN twin_class_field tcf ON tcfs.twin_class_field_id = tcf.id JOIN twin_class tc ON tcf.twin_class_id = tc.id WHERE tcfs.id = ($1->>''id'')::UUID'),

-- twin_class_search_predicate: resolve through twin_class_search.domain_id
('twin_class_search_predicate', 'SELECT domain_id FROM twin_class_search WHERE id = ($1->>''twin_class_search_id'')::UUID'),

-- twin_class_freeze: resolve through twin_status -> twin_class.domain_id
('twin_class_freeze', 'SELECT tc.domain_id FROM twin_status ts JOIN twin_class tc ON ts.twin_class_id = tc.id WHERE ts.id = ($1->>''twin_status_id'')::UUID'),

-- twin_pointer_validator_rule: resolve through twin_pointer_id -> twin_pointer -> twin_class
('twin_pointer_validator_rule', 'SELECT tc.domain_id FROM twin_pointer tp JOIN twin_class tc ON tp.twin_class_id = tc.id WHERE tp.id = ($1->>''twin_pointer_id'')::UUID'),

-- twin_class_field_condition: resolve through base_twin_class_field_id -> twin_class_field -> twin_class
('twin_class_field_condition', 'SELECT tc.domain_id FROM twin_class_field tcf JOIN twin_class tc ON tcf.twin_class_id = tc.id WHERE tcf.id = ($1->>''base_twin_class_field_id'')::UUID'),


-- TODO incorrect select twin_class_field_rule
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
('twin_factory_condition', 'SELECT domain_id FROM twin_factory_condition_set WHERE id = ($1->>''twin_factory_condition_set_id'')::UUID'),
('twin_factory_eraser', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_multiplier', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_multiplier_filter', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twin_factory_pipeline', 'SELECT domain_id FROM twin_factory WHERE id = ($1->>''twin_factory_id'')::UUID'),
('twin_factory_pipeline_step', 'SELECT domain_id FROM twin_factory WHERE id = (SELECT tfp.twin_factory_id FROM twin_factory_pipeline tfp WHERE tfp.id = ($1->>''twin_factory_pipeline_id'')::UUID)'),

-- twin_validator tables
('twin_validator', 'SELECT domain_id FROM twin_validator_set WHERE id = ($1->>''twin_validator_set_id'')::UUID'),

-- twinflow tables
('twinflow', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twinflow_factory', 'SELECT tc.domain_id FROM twinflow tf JOIN twin_class tc ON tf.twin_class_id = tc.id WHERE tf.id = ($1->>''twinflow_id'')::UUID'),
('twinflow_schema_map', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twinflow_transition', 'SELECT domain_id FROM twin_class WHERE id = (SELECT tf.twin_class_id FROM twinflow tf WHERE tf.id = ($1->>''twinflow_id'')::UUID)'),
('twinflow_transition_trigger', 'SELECT domain_id FROM twin_class WHERE id = (SELECT tf.twin_class_id FROM twinflow tf JOIN twinflow_transition tt ON tt.twinflow_id = tf.id WHERE tt.id = ($1->>''twinflow_transition_id'')::UUID)'),
('twinflow_transition_alias', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),
('twinflow_transition_validator_rule', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- Data list tables: resolve through data_list_option_search (has domain_id directly)
('data_list_option_search_predicate', 'SELECT domain_id FROM data_list_option_search WHERE id = ($1->>''data_list_option_search_id'')::UUID'),

-- Projection tables: resolve through projection_type.domain_id
('data_list_option_projection', 'SELECT pt.domain_id FROM projection_type pt WHERE pt.id = ($1->>''projection_type_id'')::UUID'),
('projection', 'SELECT pt.domain_id FROM projection_type pt WHERE pt.id = ($1->>''projection_type_id'')::UUID'),

-- Data list tables: resolve through data_list.domain_id
('data_list_option', 'SELECT dl.domain_id FROM data_list dl WHERE dl.id = ($1->>''data_list_id'')::UUID'),

-- Data list subset tables: resolve through data_list_subset -> data_list.domain_id
('data_list_subset', 'SELECT dl.domain_id FROM data_list WHERE dl.id = (SELECT data_list_id FROM data_list_subset WHERE id = ($1->>''id'')::UUID)'),
('data_list_subset_option', 'SELECT dl.domain_id FROM data_list dl WHERE dl.id = (SELECT dls.data_list_id FROM data_list_subset dls WHERE dls.id = ($1->>''data_list_subset_id'')::UUID)'),

-- Eraseflow tables: resolve through twin_class.domain_id
('eraseflow', 'SELECT tc.domain_id FROM twin_class tc WHERE tc.id = ($1->>''twin_class_id'')::UUID'),
('eraseflow_link_cascade', 'SELECT tc.domain_id FROM twin_class tc WHERE tc.id = (SELECT e.twin_class_id FROM eraseflow e WHERE e.id = ($1->>''eraseflow_id'')::UUID)'),

-- Face variant tables: resolve through face.domain_id
('face_bc001', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_bc001_item', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT fb.face_id FROM face_bc001 fb WHERE fb.id = ($1->>''face_bc001_id'')::UUID)'),
('face_navbar_nb001', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),

-- Face menu items: resolve through face.domain_id
('face_navbar_nb001_menu_item', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),

-- Face pg001 tables: resolve through face.domain_id
('face_pg001', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_pg001_widget', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),

-- Face pg002 tables: resolve through face.domain_id
('face_pg002', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_pg002_tab', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT fp.face_id FROM face_pg002 fp WHERE fp.id = ($1->>''face_pg002_id'')::UUID)'),
('face_pg002_widget', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''widget_face_id'')::UUID'),

-- Face tc001 tables: resolve through face.domain_id
('face_tc001', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_tc001_option', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT ft.face_id FROM face_tc001 ft WHERE ft.id = ($1->>''face_tc001_id'')::UUID)'),

-- Face tw001 tables: resolve through face.domain_id
('face_tw001', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),

-- Face tw002 tables: resolve through face.domain_id
('face_tw002', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_tw002_accordion_item', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT ft.face_id FROM face_tw002 ft WHERE ft.id = ($1->>''face_tw002_id'')::UUID)'),

-- Face tw004, tw005, tw006, wt001, wt002, wt003: resolve through face.domain_id
('face_tw004', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_tw005', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_tw005_button', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT ft.face_id FROM face_tw005 ft WHERE ft.id = ($1->>''face_tw005_id'')::UUID)'),
('face_tw006', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_tw006_action', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT ft.face_id FROM face_tw006 ft WHERE ft.id = ($1->>''face_tw006_id'')::UUID)'),
('face_tw007', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_wt001', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_wt001_column', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT fw.face_id FROM face_wt001 fw WHERE fw.id = ($1->>''face_wt001_id'')::UUID)'),
('face_wt002', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),
('face_wt002_button', 'SELECT f.domain_id FROM face f WHERE f.id = (SELECT fw.face_id FROM face_wt002 fw WHERE fw.id = ($1->>''face_wt002_id'')::UUID)'),
('face_wt003', 'SELECT f.domain_id FROM face f WHERE f.id = ($1->>''face_id'')::UUID'),

-- History notification collector: resolve through history_notification_recipient.domain_id
('history_notification_recipient_collector', 'SELECT domain_id FROM history_notification_recipient WHERE id = ($1->>''history_notification_recipient_id'')::UUID'),

-- History type config tables: resolve through twin_class -> domain_id
('history_type_config_twin_class_field', 'SELECT tc.domain_id FROM twin_class_field tcf JOIN twin_class tc ON tcf.twin_class_id = tc.id WHERE tcf.id = ($1->>''twin_class_field_id'')::UUID'),

-- Link tables: resolve through link.domain_id
('link_trigger', 'SELECT domain_id FROM link WHERE id = ($1->>''link_id'')::UUID'),
('link_validator', 'SELECT domain_id FROM link WHERE id = ($1->>''link_id'')::UUID'),

-- Notification channel event: resolve through notification_channel.domain_id
('notification_channel_event', 'SELECT domain_id FROM notification_channel WHERE id = ($1->>''notification_channel_id'')::UUID'),

-- Notification context collector: resolve through notification_context.domain_id
('notification_context_collector', 'SELECT domain_id FROM notification_context WHERE id = ($1->>''notification_context_id'')::UUID'),

-- Permission: resolve through permission_group.domain_id
('permission', 'SELECT domain_id FROM permission_group WHERE id = ($1->>''permission_group_id'')::UUID'),

-- twin_status_group_map: resolve through twin_status -> twin_class.domain_id
('twin_status_group_map', 'SELECT tc.domain_id FROM twin_status ts JOIN twin_class tc ON ts.twin_class_id = tc.id WHERE ts.id = ($1->>''twin_status_id'')::UUID'),

-- twin_status_transition_trigger: resolve through twin_status -> twin_class.domain_id
('twin_status_transition_trigger', 'SELECT tc.domain_id FROM twin_status ts JOIN twin_class tc ON ts.twin_class_id = tc.id WHERE ts.id = ($1->>''twin_status_id'')::UUID'),

-- Permission grant tables: resolve through permission_schema.domain_id
('permission_grant_assignee_propagation', 'SELECT domain_id FROM permission_schema WHERE id = ($1->>''permission_schema_id'')::UUID'),
('permission_grant_space_role', 'SELECT domain_id FROM permission_schema WHERE id = ($1->>''permission_schema_id'')::UUID'),
('permission_grant_twin_role', 'SELECT domain_id FROM permission_schema WHERE id = ($1->>''permission_schema_id'')::UUID'),

-- Space role: resolve through twin_class.domain_id
('space_role', 'SELECT domain_id FROM twin_class WHERE id = ($1->>''twin_class_id'')::UUID'),

-- User search tables: resolve through user_search.domain_id
('user_search_predicate', 'SELECT domain_id FROM user_search WHERE id = ($1->>''user_search_id'')::UUID');

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

CREATE OR REPLACE FUNCTION create_domain_config_audit_insert_wrapper(p_table_name TEXT) RETURNS void AS $$
BEGIN
    -- Create insert wrapper function
    EXECUTE format('
        CREATE OR REPLACE FUNCTION %I_after_insert_wrapper() returns trigger
        language plpgsql
        as $func$
        begin
            perform domain_config_audit_write(%L, ''INSERT'', NEW.id, to_jsonb(NEW));
            return new;
        end;
        $func$;', p_table_name, p_table_name);

    -- Create insert trigger
    EXECUTE format('DROP TRIGGER IF EXISTS %I_after_insert_wrapper_trigger ON %I;', p_table_name, p_table_name);
    EXECUTE format('
        CREATE TRIGGER %I_after_insert_wrapper_trigger
        AFTER INSERT ON %I
        FOR EACH ROW
        EXECUTE FUNCTION %I_after_insert_wrapper();',
                   p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION create_domain_config_audit_update_wrapper(p_table_name TEXT) RETURNS void AS $$
BEGIN
    -- Create update wrapper function
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

    -- Create update trigger
    EXECUTE format('DROP TRIGGER IF EXISTS %I_after_update_wrapper_trigger ON %I;', p_table_name, p_table_name);
    EXECUTE format('
        CREATE TRIGGER %I_after_update_wrapper_trigger
        AFTER UPDATE ON %I
        FOR EACH ROW
        EXECUTE FUNCTION %I_after_update_wrapper();',
                   p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION create_domain_config_audit_delete_wrapper(p_table_name TEXT) RETURNS void AS $$
BEGIN
    -- Create delete wrapper function
    EXECUTE format('
        CREATE OR REPLACE FUNCTION %I_after_delete_wrapper() returns trigger
        language plpgsql
        as $func$
        begin
            perform domain_config_audit_write(%L, ''DELETE'', OLD.id, to_jsonb(OLD));
            return old;
        end;
        $func$;', p_table_name, p_table_name);

    -- Create delete trigger
    EXECUTE format('DROP TRIGGER IF EXISTS %I_after_delete_wrapper_trigger ON %I;', p_table_name, p_table_name);
    EXECUTE format('
        CREATE TRIGGER %I_after_delete_wrapper_trigger
        AFTER DELETE ON %I
        FOR EACH ROW
        EXECUTE FUNCTION %I_after_delete_wrapper();',
                   p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION create_domain_config_audit_wrapper(p_table_name TEXT) RETURNS void AS $$
BEGIN
    -- Protection: check if table already has custom triggers with business logic
    IF EXISTS (
        SELECT 1
        FROM pg_trigger t
                 JOIN pg_proc p ON t.tgfoid = p.oid
        WHERE t.tgrelid = p_table_name::regclass
          AND NOT t.tgisinternal
          AND (
            p.prosrc ~* '(UPDATE|DELETE\s+FROM|INSERT\s+INTO)'
                OR p.prosrc ~* 'PERFORM\s+(?!domain_config_audit_write)'
                OR p.prosrc ~* 'permission_mater|twin_class_field_is_dependent'
            )
    ) THEN
        RAISE EXCEPTION
            'Table % already has custom wrapper triggers with business logic. Cannot use create_domain_config_audit_wrapper() - use custom wrapper definition instead.',
            p_table_name;
    END IF;

    -- Create wrappers
    PERFORM create_domain_config_audit_insert_wrapper(p_table_name);
    PERFORM create_domain_config_audit_update_wrapper(p_table_name);
    PERFORM create_domain_config_audit_delete_wrapper(p_table_name);
END;
$$ LANGUAGE plpgsql;

SELECT create_domain_config_audit_wrapper('domain_locale');
SELECT create_domain_config_audit_wrapper('face');
SELECT create_domain_config_audit_wrapper('face_bc001');
SELECT create_domain_config_audit_wrapper('face_bc001_item');
SELECT create_domain_config_audit_wrapper('i18n');
SELECT create_domain_config_audit_wrapper('permission');
SELECT create_domain_config_audit_wrapper('permission_group');
SELECT create_domain_config_audit_wrapper('permission_schema');
SELECT create_domain_config_audit_wrapper('data_list');
SELECT create_domain_config_audit_wrapper('data_list_subset');
SELECT create_domain_config_audit_wrapper('data_list_option');
SELECT create_domain_config_audit_wrapper('data_list_option_search');
SELECT create_domain_config_audit_wrapper('data_list_option_search_predicate');
SELECT create_domain_config_audit_wrapper('data_list_option_projection');
SELECT create_domain_config_audit_wrapper('user_search');
SELECT create_domain_config_audit_wrapper('user_search_predicate');
SELECT create_domain_config_audit_wrapper('face_navbar_nb001_menu_item');
SELECT create_domain_config_audit_wrapper('face_pg001');
SELECT create_domain_config_audit_wrapper('face_pg001_widget');
SELECT create_domain_config_audit_wrapper('face_pg002');
SELECT create_domain_config_audit_wrapper('face_pg002_tab');
SELECT create_domain_config_audit_wrapper('face_pg002_widget');
SELECT create_domain_config_audit_wrapper('face_tc001');
SELECT create_domain_config_audit_wrapper('face_tc001_option');
SELECT create_domain_config_audit_wrapper('face_tw001');
SELECT create_domain_config_audit_wrapper('face_tw002');
SELECT create_domain_config_audit_wrapper('face_tw002_accordion_item');
SELECT create_domain_config_audit_wrapper('face_tw004');
SELECT create_domain_config_audit_wrapper('face_tw005');
SELECT create_domain_config_audit_wrapper('face_tw005_button');
SELECT create_domain_config_audit_wrapper('face_tw006');
SELECT create_domain_config_audit_wrapper('face_tw006_action');
SELECT create_domain_config_audit_wrapper('face_tw007');
SELECT create_domain_config_audit_wrapper('face_wt001');
SELECT create_domain_config_audit_wrapper('face_wt001_column');
SELECT create_domain_config_audit_wrapper('face_wt002');
SELECT create_domain_config_audit_wrapper('face_wt002_button');
SELECT create_domain_config_audit_wrapper('face_wt003');
SELECT create_domain_config_audit_wrapper('history_type_config_domain');
SELECT create_domain_config_audit_wrapper('history_type_config_twin_class');
SELECT create_domain_config_audit_wrapper('history_type_config_twin_class_field');
SELECT create_domain_config_audit_wrapper('history_notification_recipient');
SELECT create_domain_config_audit_wrapper('history_notification_recipient_collector');
SELECT create_domain_config_audit_wrapper('i18n_translation_style');
SELECT create_domain_config_audit_wrapper('link');
SELECT create_domain_config_audit_wrapper('link_trigger');
SELECT create_domain_config_audit_wrapper('link_validator');
SELECT create_domain_config_audit_wrapper('notification_channel');
SELECT create_domain_config_audit_wrapper('notification_channel_event');
SELECT create_domain_config_audit_wrapper('notification_context');
SELECT create_domain_config_audit_wrapper('notification_context_collector');
SELECT create_domain_config_audit_wrapper('notification_email');
SELECT create_domain_config_audit_wrapper('notification_schema');
SELECT create_domain_config_audit_wrapper('permission_grant_assignee_propagation');
SELECT create_domain_config_audit_wrapper('permission_grant_twin_role');
SELECT create_domain_config_audit_wrapper('resource');
SELECT create_domain_config_audit_wrapper('projection_type');
SELECT create_domain_config_audit_wrapper('projection_type_group');
SELECT create_domain_config_audit_wrapper('space_role');
SELECT create_domain_config_audit_wrapper('storage');
SELECT create_domain_config_audit_wrapper('template_generator');
SELECT create_domain_config_audit_wrapper('scheduler');
SELECT create_domain_config_audit_wrapper('twin_action_permission');
SELECT create_domain_config_audit_wrapper('twin_action_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_attachment_action_alien_permission');
SELECT create_domain_config_audit_wrapper('twin_attachment_action_alien_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_attachment_action_self_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_attachment_restriction');
SELECT create_domain_config_audit_wrapper('twin_class_field');
SELECT create_domain_config_audit_wrapper('twin_class_schema_map');
SELECT create_domain_config_audit_wrapper('twin_comment_action_alien_permission');
SELECT create_domain_config_audit_wrapper('twin_comment_action_alien_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_comment_action_self');
SELECT create_domain_config_audit_wrapper('twin_factory_branch');
SELECT create_domain_config_audit_wrapper('twin_factory_condition');
SELECT create_domain_config_audit_wrapper('twin_factory_eraser');
SELECT create_domain_config_audit_wrapper('twin_factory_multiplier');
SELECT create_domain_config_audit_wrapper('twin_factory_multiplier_filter');
SELECT create_domain_config_audit_wrapper('twin_factory_pipeline');
SELECT create_domain_config_audit_wrapper('twin_factory_pipeline_step');
SELECT create_domain_config_audit_wrapper('twin_pointer');
SELECT create_domain_config_audit_wrapper('twin_pointer_validator_rule');
SELECT create_domain_config_audit_wrapper('twin_status');
SELECT create_domain_config_audit_wrapper('twin_status_group');
SELECT create_domain_config_audit_wrapper('twin_status_group_map');
SELECT create_domain_config_audit_wrapper('twin_status_transition_trigger');
SELECT create_domain_config_audit_wrapper('twin_validator');
SELECT create_domain_config_audit_wrapper('twin_validator_set');
SELECT create_domain_config_audit_wrapper('twin_statistic');
SELECT create_domain_config_audit_wrapper('twin_class_schema');
SELECT create_domain_config_audit_wrapper('twin_class_dynamic_marker');
SELECT create_domain_config_audit_wrapper('twin_class_field_attribute');
SELECT create_domain_config_audit_wrapper('twin_class_field_search');
SELECT create_domain_config_audit_wrapper('twin_class_field_search_predicate');
SELECT create_domain_config_audit_wrapper('twin_class_field_rule');
SELECT create_domain_config_audit_wrapper('twin_class_freeze');
SELECT create_domain_config_audit_wrapper('twin_class_search');
SELECT create_domain_config_audit_wrapper('twin_class_search_predicate');
SELECT create_domain_config_audit_wrapper('twin_factory');
SELECT create_domain_config_audit_wrapper('twin_factory_condition_set');
SELECT create_domain_config_audit_wrapper('history_type_domain_template');
SELECT create_domain_config_audit_wrapper('eraseflow');
SELECT create_domain_config_audit_wrapper('eraseflow_link_cascade');
SELECT create_domain_config_audit_wrapper('twinflow');
SELECT create_domain_config_audit_wrapper('twinflow_factory');
SELECT create_domain_config_audit_wrapper('twinflow_schema');
SELECT create_domain_config_audit_wrapper('twinflow_schema_map');
SELECT create_domain_config_audit_wrapper('twinflow_transition');
SELECT create_domain_config_audit_wrapper('twinflow_transition_alias');
SELECT create_domain_config_audit_wrapper('twinflow_transition_trigger');
SELECT create_domain_config_audit_wrapper('twinflow_transition_validator_rule');
SELECT create_domain_config_audit_wrapper('email_sender');

SELECT create_domain_config_audit_insert_wrapper('tier');
SELECT create_domain_config_audit_delete_wrapper('tier');


create or replace function twin_class_field_condition_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform twin_class_field_has_dependent_fields_check(old.base_twin_class_field_id);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));

    return old;
end;
$$;

create or replace function twin_class_field_condition_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform twin_class_field_has_dependent_fields_check(new.base_twin_class_field_id);

    -- Add audit record
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));

    return new;
end;
$$;

create or replace function twin_class_field_condition_after_update_wrapper() returns trigger
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

create or replace function twin_class_field_rule_map_after_insert_wrapper() returns trigger
    language plpgsql
as $$
BEGIN
    PERFORM twin_class_field_is_dependent_field_check(NEW.twin_class_field_id);
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));
    RETURN NEW;
END;
$$;

create or replace function twin_class_field_rule_map_after_update_wrapper() returns trigger
    language plpgsql
as $$
BEGIN
    PERFORM twin_class_field_is_dependent_field_check(NEW.twin_class_field_id);
    PERFORM twin_class_field_is_dependent_field_check(OLD.twin_class_field_id);
    perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW));
    RETURN NEW;
END;
$$;

create or replace function twin_class_field_rule_map_after_delete_wrapper() returns trigger
    language plpgsql
as $$
BEGIN
    PERFORM twin_class_field_is_dependent_field_check(OLD.twin_class_field_id);
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));
    RETURN OLD;
END;
$$;

create or replace function twin_class_after_insert_wrapper() returns trigger
    language plpgsql
as
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

create or replace function twin_class_after_delete_wrapper() returns trigger
    language plpgsql
as
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

create or replace function twin_class_after_update_wrapper()
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

    -- Add audit record (only if data actually changed, excluding counters and timestamps)
    IF (to_jsonb(OLD) - 'updated_at' - 'twin_counter') != (to_jsonb(NEW) - 'updated_at' - 'twin_counter') THEN
        PERFORM domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW) - 'updated_at' - 'twin_counter');
    END IF;

    RETURN NEW;
END;
$$;

create or replace function tier_after_update_wrapper()
    returns trigger as $$
BEGIN
    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id OR
       OLD.twinflow_schema_id IS DISTINCT FROM NEW.twinflow_schema_id OR
       OLD.twin_class_schema_id IS DISTINCT FROM NEW.twin_class_schema_id OR
       OLD.notification_schema_id IS DISTINCT FROM NEW.notification_schema_id OR
       (OLD.custom IS DISTINCT FROM NEW.custom AND NOT NEW.custom) THEN

        PERFORM domain_business_account_update_props_on_update_tier(
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

    RETURN NEW;
END;
$$ language plpgsql;

-- TODO add primary key to table
create or replace function data_list_subset_option_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.data_list_subset_id, to_jsonb(NEW));
    return new;
end;
$$;

create or replace function data_list_subset_option_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.data_list_subset_id, to_jsonb(NEW) - 'updated_at');
end;
$$;

create or replace function data_list_subset_option_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.data_list_subset_id, to_jsonb(OLD));
    return old;
end;
$$;

create or replace function face_navbar_nb001_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('face_navbar_nb001', 'INSERT', NEW.face_id, to_jsonb(NEW));
    return new;
end;
$$;

create or replace function face_navbar_nb001_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('face_navbar_nb001', 'UPDATE', NEW.face_id, to_jsonb(NEW) - 'updated_at');
    return new;
end;
$$;

create or replace function face_navbar_nb001_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('face_navbar_nb001', 'DELETE', OLD.face_id, to_jsonb(OLD));
    return old;
end;
$$;

create or replace function i18n_translation_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation', 'INSERT', NEW.i18n_id, to_jsonb(NEW));
    return new;
end;
$$;

create or replace function i18n_translation_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    if (to_jsonb(OLD) - 'updated_at') != (to_jsonb(NEW) - 'updated_at') then
        perform domain_config_audit_write('i18n_translation', 'UPDATE', NEW.i18n_id, to_jsonb(NEW) - 'updated_at');
    end if;
    return new;
end;
$$;

create or replace function i18n_translation_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation', 'DELETE', OLD.i18n_id, to_jsonb(OLD));
    return old;
end;
$$;

create or replace function i18n_translation_bin_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation_bin', 'INSERT', NEW.i18n_id, to_jsonb(NEW));
    return new;
end;
$$;

create or replace function i18n_translation_bin_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation_bin', 'UPDATE', NEW.i18n_id, to_jsonb(NEW) - 'updated_at');
    return new;
end;
$$;

create or replace function i18n_translation_bin_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    perform domain_config_audit_write('i18n_translation_bin', 'DELETE', OLD.i18n_id, to_jsonb(OLD));
    return old;
end;
$$;

create or replace function permission_grant_space_role_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    PERFORM permission_mater_space_by_permiss_grant_space_role_insert(
            NEW.permission_schema_id,
            NEW.permission_id,
            NEW.space_role_id
            );
    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));
    return NEW;
end;
$$;

create or replace function permission_grant_space_role_after_delete_wrapper() returns trigger
    language plpgsql
as $$
begin
    PERFORM permission_mater_space_by_permiss_grant_space_role_delete(
            OLD.permission_schema_id,
            OLD.permission_id,
            OLD.space_role_id
            );
    perform domain_config_audit_write(TG_TABLE_NAME, 'DELETE', OLD.id, to_jsonb(OLD));
    return OLD;
end;
$$;

create or replace function permission_grant_space_role_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    if NEW.permission_schema_id is distinct from OLD.permission_schema_id
        or NEW.permission_id is distinct from OLD.permission_id
        or NEW.space_role_id is distinct from OLD.space_role_id
    then
        PERFORM permission_mater_space_by_permiss_grant_space_role_insert(
                NEW.permission_schema_id,
                NEW.permission_id,
                NEW.space_role_id
                );
        PERFORM permission_mater_space_by_permiss_grant_space_role_delete(
                OLD.permission_schema_id,
                OLD.permission_id,
                OLD.space_role_id
                );
    end if;
    perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW));
    return NEW;
end;
$$;

create or replace function projection_after_insert_wrapper() returns trigger
    language plpgsql
as $$
begin
    update twin_class_field
    set has_projected_fields=true
    where id=new.src_twin_class_field_id;

    update twin_class_field
    set projection_field=true
    where id=new.dst_twin_class_field_id;

    perform domain_config_audit_write(TG_TABLE_NAME, 'INSERT', NEW.id, to_jsonb(NEW));
    return NEW;
end;
$$;

create or replace function projection_after_update_wrapper() returns trigger
    language plpgsql
as $$
begin
    update twin_class_field
    set has_projected_fields=true
    where id=new.src_twin_class_field_id;

    update twin_class_field
    set projection_field=true
    where id=new.dst_twin_class_field_id;

    perform domain_config_audit_write(TG_TABLE_NAME, 'UPDATE', NEW.id, to_jsonb(NEW));
    return NEW;
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

DROP TRIGGER IF EXISTS data_list_subset_after_insert_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_after_insert_wrapper_trigger
    AFTER INSERT ON data_list_subset
    FOR EACH ROW
EXECUTE FUNCTION data_list_subset_after_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_after_update_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_after_update_wrapper_trigger
    AFTER UPDATE ON data_list_subset
    FOR EACH ROW
EXECUTE FUNCTION data_list_subset_after_update_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_after_delete_wrapper_trigger ON data_list_subset;
CREATE TRIGGER data_list_subset_after_delete_wrapper_trigger
    AFTER DELETE ON data_list_subset
    FOR EACH ROW
EXECUTE FUNCTION data_list_subset_after_delete_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_after_insert_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_after_insert_wrapper_trigger
    AFTER INSERT ON data_list_subset_option
    FOR EACH ROW
EXECUTE FUNCTION data_list_subset_option_after_insert_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_after_update_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_after_update_wrapper_trigger
    AFTER UPDATE ON data_list_subset_option
    FOR EACH ROW
EXECUTE FUNCTION data_list_subset_option_after_update_wrapper();

DROP TRIGGER IF EXISTS data_list_subset_option_after_delete_wrapper_trigger ON data_list_subset_option;
CREATE TRIGGER data_list_subset_option_after_delete_wrapper_trigger
    AFTER DELETE ON data_list_subset_option
    FOR EACH ROW
EXECUTE FUNCTION data_list_subset_option_after_delete_wrapper();

DROP TRIGGER IF EXISTS face_navbar_nb001_after_insert_wrapper_trigger ON face_navbar_nb001;
CREATE TRIGGER face_navbar_nb001_after_insert_wrapper_trigger
    AFTER INSERT ON face_navbar_nb001
    FOR EACH ROW
EXECUTE FUNCTION face_navbar_nb001_after_insert_wrapper();

DROP TRIGGER IF EXISTS face_navbar_nb001_after_update_wrapper_trigger ON face_navbar_nb001;
CREATE TRIGGER face_navbar_nb001_after_update_wrapper_trigger
    AFTER UPDATE ON face_navbar_nb001
    FOR EACH ROW
EXECUTE FUNCTION face_navbar_nb001_after_update_wrapper();

DROP TRIGGER IF EXISTS face_navbar_nb001_after_delete_wrapper_trigger ON face_navbar_nb001;
CREATE TRIGGER face_navbar_nb001_after_delete_wrapper_trigger
    AFTER DELETE ON face_navbar_nb001
    FOR EACH ROW
EXECUTE FUNCTION face_navbar_nb001_after_delete_wrapper();

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

DROP TRIGGER IF EXISTS permission_grant_space_role_after_insert_wrapper_trigger ON permission_grant_space_role;
CREATE TRIGGER permission_grant_space_role_after_insert_wrapper_trigger
    AFTER INSERT ON permission_grant_space_role
    FOR EACH ROW
    EXECUTE FUNCTION permission_grant_space_role_after_insert_wrapper();

DROP TRIGGER IF EXISTS permission_grant_space_role_after_update_wrapper_trigger ON permission_grant_space_role;
CREATE TRIGGER permission_grant_space_role_after_update_wrapper_trigger
    AFTER UPDATE ON permission_grant_space_role
    FOR EACH ROW
    EXECUTE FUNCTION permission_grant_space_role_after_update_wrapper();

DROP TRIGGER IF EXISTS permission_grant_space_role_after_delete_wrapper_trigger ON permission_grant_space_role;
CREATE TRIGGER permission_grant_space_role_after_delete_wrapper_trigger
    AFTER DELETE ON permission_grant_space_role
    FOR EACH ROW
    EXECUTE FUNCTION permission_grant_space_role_after_delete_wrapper();

-- Triggers for projection
DROP TRIGGER IF EXISTS projection_after_insert_wrapper_trigger ON projection;
CREATE TRIGGER projection_after_insert_wrapper_trigger
    AFTER INSERT ON projection
    FOR EACH ROW
    EXECUTE FUNCTION projection_after_insert_wrapper();

DROP TRIGGER IF EXISTS projection_after_update_wrapper_trigger ON projection;
CREATE TRIGGER projection_after_update_wrapper_trigger
    AFTER UPDATE ON projection
    FOR EACH ROW
    EXECUTE FUNCTION projection_after_update_wrapper();

-- Triggers for twin_class_field_rule_map
DROP TRIGGER IF EXISTS twin_class_field_rule_map_after_insert_wrapper_trigger ON twin_class_field_rule_map;
CREATE TRIGGER twin_class_field_rule_map_after_insert_wrapper_trigger
    AFTER INSERT ON twin_class_field_rule_map
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_rule_map_after_insert_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_rule_map_after_update_wrapper_trigger ON twin_class_field_rule_map;
CREATE TRIGGER twin_class_field_rule_map_after_update_wrapper_trigger
    AFTER UPDATE ON twin_class_field_rule_map
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_rule_map_after_update_wrapper();

DROP TRIGGER IF EXISTS twin_class_field_rule_map_after_delete_wrapper_trigger ON twin_class_field_rule_map;
CREATE TRIGGER twin_class_field_rule_map_after_delete_wrapper_trigger
    AFTER DELETE ON twin_class_field_rule_map
    FOR EACH ROW
    EXECUTE FUNCTION twin_class_field_rule_map_after_delete_wrapper();
