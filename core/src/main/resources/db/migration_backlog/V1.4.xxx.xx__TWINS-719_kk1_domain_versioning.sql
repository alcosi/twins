-- Domain versioning tables (from TWINS-458)
CREATE TABLE IF NOT EXISTS domain_version_status(
    id varchar PRIMARY KEY
);

INSERT INTO domain_version_status (id) VALUES
                                           ('OPEN'),
                                           ('LOCKED'),
                                           ('RELEASED')
ON CONFLICT DO NOTHING;

-- domain versions
CREATE TABLE IF NOT EXISTS domain_version
(
    id                         uuid                                NOT NULL
        CONSTRAINT domain_version_pk
            PRIMARY KEY,
    domain_id                  uuid                                NOT NULL
        CONSTRAINT domain_version_domain_id_fk
            REFERENCES domain
            ON UPDATE CASCADE ON DELETE CASCADE,
    name                       varchar,
    version                    varchar,
    domain_version_status_id   varchar                             NOT NULL
        CONSTRAINT domain_version_status_fk
            REFERENCES domain_version_status (id)
            ON UPDATE CASCADE ON DELETE RESTRICT,
    json_file                  jsonb,
    hash                       varchar,
    previous_domain_version_id uuid
        CONSTRAINT domain_version_prev_version_fk
            REFERENCES domain_version
            ON UPDATE CASCADE ON DELETE SET NULL,
    created_at                 timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    released_at                timestamp,
    release_duration           bigint
);

-- only one OPEN version per domain
CREATE UNIQUE INDEX IF NOT EXISTS domain_version_one_open_per_domain
    ON domain_version (domain_id)
    WHERE domain_version_status_id = 'OPEN';

-- unique version inside domain
CREATE UNIQUE INDEX IF NOT EXISTS domain_version_unique
    ON domain_version (domain_id, version);

-- fast navigation between versions
CREATE INDEX IF NOT EXISTS domain_version_prev_idx
    ON domain_version (previous_domain_version_id);

-- often useful for getting latest released version
CREATE INDEX IF NOT EXISTS domain_version_released_idx
    ON domain_version (domain_id, released_at DESC);

CREATE INDEX IF NOT EXISTS domain_version_domain_id_index
    ON domain_version (domain_id);

ALTER TABLE domain
    ADD IF NOT EXISTS current_domain_version_id uuid
        constraint domain_domain_version_id_fk
            references domain_version
            on update cascade on delete restrict;

CREATE TABLE IF NOT EXISTS domain_version_ghost
(
    domain_id  uuid        not null
        constraint domain_version_ghost_domain_id_fk
            references public.domain
            on update cascade on delete cascade,
    user_id    uuid        not null
        constraint domain_version_ghost_user_id_fk
            references public."user"
            on update cascade on delete cascade,
    table_name varchar(50) not null,
    constraint domain_version_ghost_pk
        primary key (domain_id, user_id, table_name)
);

-- Function to add domain_version_id column safely
CREATE OR REPLACE FUNCTION add_domain_version_column(tbl text, nullable boolean default true) RETURNS void AS
$$
DECLARE
    v_nullable text := CASE WHEN nullable THEN 'NULL' ELSE 'NOT NULL' END;
BEGIN
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS domain_version_id UUID ' || v_nullable, tbl);
    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I', tbl, tbl || '_domain_version_id_fk');
    EXECUTE format(
            'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (domain_version_id) REFERENCES domain_version(id) ON DELETE SET NULL',
            tbl, tbl || '_domain_version_id_fk');
END;
$$ LANGUAGE plpgsql;

-- Add domain_version_id column to all domain tables (from TWINS-458-1)

SELECT add_domain_version_column('twin_status');
SELECT add_domain_version_column('permission');
SELECT add_domain_version_column('data_list');
SELECT add_domain_version_column('data_list_option');
SELECT add_domain_version_column('data_list_subset');
SELECT add_domain_version_column('data_list_subset_option');
SELECT add_domain_version_column('domain_locale');
SELECT add_domain_version_column('email_sender');
SELECT add_domain_version_column('face_navbar_nb001');
SELECT add_domain_version_column('face_navbar_nb001_menu_item');
SELECT add_domain_version_column('face_pg001');
SELECT add_domain_version_column('face_pg001_widget');
SELECT add_domain_version_column('face_pg002');
SELECT add_domain_version_column('face_pg002_tab');
SELECT add_domain_version_column('face_pg002_widget');
SELECT add_domain_version_column('face_tc001');
SELECT add_domain_version_column('face_tc001_option');
SELECT add_domain_version_column('face_tw001');
SELECT add_domain_version_column('face_tw002');
SELECT add_domain_version_column('face_tw002_accordion_item');
SELECT add_domain_version_column('face_tw004');
SELECT add_domain_version_column('face_tw005');
SELECT add_domain_version_column('face_tw005_button');
SELECT add_domain_version_column('face_tw006');
SELECT add_domain_version_column('face_tw006_action');
SELECT add_domain_version_column('face_tw007');
SELECT add_domain_version_column('face_wt001');
SELECT add_domain_version_column('face_wt001_column');
SELECT add_domain_version_column('face_wt002');
SELECT add_domain_version_column('face_wt002_button');
SELECT add_domain_version_column('face_wt003');
SELECT add_domain_version_column('history_type_config_domain');
SELECT add_domain_version_column('history_type_config_twin_class');
SELECT add_domain_version_column('history_type_config_twin_class_field');
SELECT add_domain_version_column('i18n');
SELECT add_domain_version_column('i18n_translation');
SELECT add_domain_version_column('i18n_translation_bin');
SELECT add_domain_version_column('i18n_translation_style');
SELECT add_domain_version_column('link_trigger');
SELECT add_domain_version_column('link_validator');
SELECT add_domain_version_column('notification_email');
SELECT add_domain_version_column('permission_grant_assignee_propagation');
SELECT add_domain_version_column('permission_grant_space_role');
SELECT add_domain_version_column('permission_grant_twin_role');
SELECT add_domain_version_column('permission_group');
SELECT add_domain_version_column('resource');
SELECT add_domain_version_column('space_role');
SELECT add_domain_version_column('storage');
SELECT add_domain_version_column('template_generator');
SELECT add_domain_version_column('tier');
SELECT add_domain_version_column('twin_action_permission');
SELECT add_domain_version_column('twin_action_validator_rule');
SELECT add_domain_version_column('twin_attachment_action_alien_permission');
SELECT add_domain_version_column('twin_attachment_action_alien_validator_rule');
SELECT add_domain_version_column('twin_attachment_action_self_validator_rule');
SELECT add_domain_version_column('twin_attachment_restriction');
SELECT add_domain_version_column('twin_class_field');
SELECT add_domain_version_column('twin_class_schema_map');
SELECT add_domain_version_column('twin_comment_action_alien_permission');
SELECT add_domain_version_column('twin_comment_action_alien_validator_rule');
SELECT add_domain_version_column('twin_comment_action_self');
SELECT add_domain_version_column('twin_factory_branch');
SELECT add_domain_version_column('twin_factory_condition');
SELECT add_domain_version_column('twin_factory_eraser');
SELECT add_domain_version_column('twin_factory_multiplier');
SELECT add_domain_version_column('twin_factory_multiplier_filter');
SELECT add_domain_version_column('twin_factory_pipeline');
SELECT add_domain_version_column('twin_factory_pipeline_step');
SELECT add_domain_version_column('twin_pointer');
SELECT add_domain_version_column('twin_pointer_validator_rule');
SELECT add_domain_version_column('twin_status_group');
SELECT add_domain_version_column('twin_status_group_map');
SELECT add_domain_version_column('twin_status_transition_trigger');
SELECT add_domain_version_column('twin_validator');
SELECT add_domain_version_column('twin_validator_set');
SELECT add_domain_version_column('twin_statistic');
SELECT add_domain_version_column('twinflow');
SELECT add_domain_version_column('twinflow_factory');
SELECT add_domain_version_column('twinflow_schema');
SELECT add_domain_version_column('twinflow_schema_map');
SELECT add_domain_version_column('twinflow_transition_alias');
SELECT add_domain_version_column('twinflow_transition_trigger');
SELECT add_domain_version_column('twinflow_transition_validator_rule');
SELECT add_domain_version_column('link');
SELECT add_domain_version_column('face');
SELECT add_domain_version_column('face_bc001');
SELECT add_domain_version_column('face_bc001_item');
SELECT add_domain_version_column('permission_schema');
SELECT add_domain_version_column('data_list_option_search');
SELECT add_domain_version_column('history_notification_recipient');
SELECT add_domain_version_column('history_notification_recipient_collector');
SELECT add_domain_version_column('notification_channel');
SELECT add_domain_version_column('notification_channel_event');
SELECT add_domain_version_column('notification_context');
SELECT add_domain_version_column('notification_context_collector');
SELECT add_domain_version_column('notification_schema');
SELECT add_domain_version_column('projection_type');
SELECT add_domain_version_column('projection_type_group');
SELECT add_domain_version_column('scheduler');
SELECT add_domain_version_column('twin_class');
SELECT add_domain_version_column('twin_class_dynamic_marker');
SELECT add_domain_version_column('twin_class_field_attribute');
SELECT add_domain_version_column('twin_class_field_rule_map');
SELECT add_domain_version_column('twin_class_field_search');
SELECT add_domain_version_column('twin_class_field_search_predicate');
SELECT add_domain_version_column('twin_class_freeze');
SELECT add_domain_version_column('twin_class_schema');
SELECT add_domain_version_column('twin_class_search');
SELECT add_domain_version_column('twin_class_search_predicate');
SELECT add_domain_version_column('twin_factory');
SELECT add_domain_version_column('twin_factory_condition_set');
SELECT add_domain_version_column('history_type_domain_template');
SELECT add_domain_version_column('data_list_option_search_predicate');
SELECT add_domain_version_column('data_list_option_projection');
SELECT add_domain_version_column('projection');
SELECT add_domain_version_column('eraseflow');
SELECT add_domain_version_column('eraseflow_link_cascade');
SELECT add_domain_version_column('user_search');
SELECT add_domain_version_column('user_search_predicate');

DROP FUNCTION IF EXISTS add_domain_version_column(text);
