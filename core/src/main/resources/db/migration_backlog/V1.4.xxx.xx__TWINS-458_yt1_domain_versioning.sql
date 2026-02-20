


-- Domain versioning tables (from TWINS-458)
CREATE TABLE IF NOT EXISTS domain_version
(
    id          uuid                                not null
        constraint domain_version_pk
            primary key,
    domain_id   uuid                                not null
        constraint domain_version_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name        varchar,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    released_at timestamp
);

CREATE INDEX IF NOT EXISTS domain_version_domain_id_index
    ON domain_version (domain_id);

ALTER TABLE domain
    ADD IF NOT EXISTS current_domain_version_id uuid
        constraint domain_domain_version_id_fk
            references domain_version
            on update cascade on delete restrict;

CREATE TABLE IF NOT EXISTS domain_version_changes
(
    id                uuid
        constraint domain_version_changes_pk
            primary key,
    domain_version_id uuid     not null
        constraint domain_version_changes_domain_version_id_fk
            references domain_version
            on update cascade on delete cascade,
    time_in_ms        integer  not null,
    table_name        varchar  not null,
    operation         smallint not null,
    row_id            varchar  not null
);

CREATE INDEX IF NOT EXISTS domain_version_changes_domain_version_id_index
    ON domain_version_changes (domain_version_id);

CREATE INDEX IF NOT EXISTS domain_version_changes_table_name_index
    ON domain_version_changes (table_name);

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
Но
-- Function to add domain_version_id column safely
CREATE OR REPLACE FUNCTION add_domain_version_column(tbl text) RETURNS void AS
$$
BEGIN
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS domain_version_id UUID', tbl);
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
SELECT add_domain_version_column('error');
SELECT add_domain_version_column('face_navbar_nb001');
SELECT add_domain_version_column('face_navbar_nb001_menu_item');
SELECT add_domain_version_column('face_pg001');
SELECT add_domain_version_column('face_pg001_widget');
SELECT add_domain_version_column('face_pg002');
SELECT add_domain_version_column('face_pg002_layout');
SELECT add_domain_version_column('face_pg002_tab');
SELECT add_domain_version_column('face_pg002_widget');
SELECT add_domain_version_column('face_tc001');
SELECT add_domain_version_column('face_tw001');
SELECT add_domain_version_column('face_tw002');
SELECT add_domain_version_column('face_tw002_accordion_item');
SELECT add_domain_version_column('face_tw004');
SELECT add_domain_version_column('face_tw005');
SELECT add_domain_version_column('face_tw005_button');
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
SELECT add_domain_version_column('identity_provider');
SELECT add_domain_version_column('identity_provider_internal_token');
SELECT add_domain_version_column('identity_provider_internal_user');
SELECT add_domain_version_column('link_trigger');
SELECT add_domain_version_column('link_validator');
SELECT add_domain_version_column('notification_email');
SELECT add_domain_version_column('permission_grant_assignee_propagation');
SELECT add_domain_version_column('permission_grant_global');
SELECT add_domain_version_column('permission_grant_space_role');
SELECT add_domain_version_column('permission_grant_twin_role');
SELECT add_domain_version_column('permission_grant_user');
SELECT add_domain_version_column('permission_grant_user_group');
SELECT add_domain_version_column('permission_group');

SELECT add_domain_version_column('resource');
SELECT add_domain_version_column('twin_search');
SELECT add_domain_version_column('twin_search_alias');
SELECT add_domain_version_column('twin_search_predicate');
SELECT add_domain_version_column('space_role');
SELECT add_domain_version_column('space_role_user_group');
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

SELECT add_domain_version_column('twinflow_schema_map');

SELECT add_domain_version_column('twinflow_transition_alias');
SELECT add_domain_version_column('twinflow_transition_trigger');
SELECT add_domain_version_column('twinflow_transition_validator_rule');


DROP FUNCTION add_domain_version_column(text);
