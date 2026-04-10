alter table public.twinflow_transition_alias
    alter column domain_id drop not null;

-- TWINS-770: JOB (abstract) and JOB_SIMPLE classes

-- Insert featurer for Decimal Increment
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1350::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperDecimalIncrement'::varchar, 'Decimal Increment'::varchar, 'Decimal field with atomic increment/decrement support (+N/-N format)'::varchar(255), false::boolean)
    on conflict do nothing;

-- ==================== JOB CLASS (abstract, no twinflow, no statuses, no fields) ====================

-- i18n for JOB twin_class
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000061', null, null, 'twinClassName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000061', 'en', 'Job', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000062', null, null, 'twinClassDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000062', 'en', 'Abstract Job class', 0) on conflict do nothing;

-- twin_class JOB (abstract, extends GLOBAL_ANCESTOR)
INSERT INTO twin_class (id, domain_id, key, permission_schema_space, abstract, head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, created_by_user_id, created_at, twin_class_owner_type_id, domain_alias_counter, marker_data_list_id, tag_data_list_id, twinflow_schema_space, twin_class_schema_space, alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree, head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id, edit_permission_id, delete_permission_id, page_face_id, assignee_required, general_attachment_restriction_id, comment_attachment_restriction_id, external_id, bread_crumbs_face_id, inherited_bread_crumbs_face_id, inherited_page_face_id, inherited_bread_crumbs_twin_class_id, inherited_page_twin_class_id, external_properties, icon_dark_resource_id, icon_light_resource_id, external_json, segment, has_segments, twin_class_freeze_id, inherited_marker_data_list_id, inherited_marker_data_list_twin_class_id, inherited_tag_data_list_id, inherited_tag_data_list_twin_class_id, has_dynamic_markers, extends_hierarchy_counter_direct_children, head_hierarchy_counter_direct_children, unique_name, twin_counter)
VALUES ('00000000-0000-0000-0001-000000000006', null, 'JOB', false, true, null, '00000000-0000-0000-0001-000000000004', '00000000-0000-0000-0012-000000000061', '00000000-0000-0000-0012-000000000062', '608c6d7d-99c8-4d87-89c6-2f72d0f5d673', '2026-03-19 11:44:16.824836', 'domain', 0, null, null, false, false, false, null, '00000000_0000_0000_0001_000000000006', '00000000_0000_0000_0001_000000000004.3a2fcc3a_3f53_4ba9_b7ab_0b1ae1635be4.00000000_0000_0000_0001_000000000006', null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, false, false, null, null, null, null, null, false, 0, 0, false, 0) on conflict do nothing;

-- ==================== JOB_SIMPLE CLASS (with twinflow, 3 statuses, 3 fields) ====================

-- i18n for JOB_SIMPLE twin_class
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000053', null, null, 'twinClassName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000053', 'en', 'Job Simple', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000054', null, null, 'twinClassDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000054', 'en', 'Simple Job with status tracking', 0) on conflict do nothing;

-- twin_class JOB_SIMPLE (extends JOB, not abstract)
INSERT INTO twin_class (id, domain_id, key, permission_schema_space, abstract, head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, created_by_user_id, created_at, twin_class_owner_type_id, domain_alias_counter, marker_data_list_id, tag_data_list_id, twinflow_schema_space, twin_class_schema_space, alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree, head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id, edit_permission_id, delete_permission_id, page_face_id, assignee_required, general_attachment_restriction_id, comment_attachment_restriction_id, external_id, bread_crumbs_face_id, inherited_bread_crumbs_face_id, inherited_page_face_id, inherited_bread_crumbs_twin_class_id, inherited_page_twin_class_id, external_properties, icon_dark_resource_id, icon_light_resource_id, external_json, segment, has_segments, twin_class_freeze_id, inherited_marker_data_list_id, inherited_marker_data_list_twin_class_id, inherited_tag_data_list_id, inherited_tag_data_list_twin_class_id, has_dynamic_markers, extends_hierarchy_counter_direct_children, head_hierarchy_counter_direct_children, unique_name, twin_counter)
VALUES ('00000000-0000-0000-0001-000000000007', null, 'JOB_SIMPLE', false, false, null, '00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0012-000000000053', '00000000-0000-0000-0012-000000000054', '608c6d7d-99c8-4d87-89c6-2f72d0f5d673', '2026-04-10 00:00:00.000000', 'domain', 0, null, null, false, false, false, null, '00000000_0000_0000_0001_000000000007', '00000000_0000_0000_0001_000000000006.00000000_0000_0000_0001_000000000007', null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, false, false, null, null, null, null, null, false, 0, 0, false, 0) on conflict do nothing;

-- i18n for JOB_SIMPLE statuses
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000047', DEFAULT, null::varchar(255), 'twinStatusName', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000048', DEFAULT, null::varchar(255), 'twinStatusDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000047', 'en'::varchar(2), 'In progress'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000048', 'en'::varchar(2), 'Job is in progress'::text, DEFAULT) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000049', DEFAULT, null::varchar(255), 'twinStatusName', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000050', DEFAULT, null::varchar(255), 'twinStatusDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000049', 'en'::varchar(2), 'Completed'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000050', 'en'::varchar(2), 'Job completed successfully'::text, DEFAULT) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000051', DEFAULT, null::varchar(255), 'twinStatusName', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000052', DEFAULT, null::varchar(255), 'twinStatusDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000051', 'en'::varchar(2), 'Error'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000052', 'en'::varchar(2), 'Job completed with error'::text, DEFAULT) on conflict do nothing;

-- twin_status for JOB_SIMPLE (In progress is initial)
INSERT INTO twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, background_color, key, font_color, icon_dark_resource_id, icon_light_resource_id, twin_status_type)
VALUES ('00000000-0000-0000-0003-000000000006', '00000000-0000-0000-0001-000000000007', '00000000-0000-0000-0012-000000000047', '00000000-0000-0000-0012-000000000048', null, 'in_progress', null, null, null, 'BASIC') on conflict do nothing;

INSERT INTO twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, background_color, key, font_color, icon_dark_resource_id, icon_light_resource_id, twin_status_type)
VALUES ('00000000-0000-0000-0003-000000000007', '00000000-0000-0000-0001-000000000007', '00000000-0000-0000-0012-000000000049', '00000000-0000-0000-0012-000000000050', null, 'completed', null, null, null, 'BASIC') on conflict do nothing;

INSERT INTO twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, background_color, key, font_color, icon_dark_resource_id, icon_light_resource_id, twin_status_type)
VALUES ('00000000-0000-0000-0003-000000000008', '00000000-0000-0000-0001-000000000007', '00000000-0000-0000-0012-000000000051', '00000000-0000-0000-0012-000000000052', null, 'error', null, null, null, 'BASIC') on conflict do nothing;

-- i18n for twin_class fields (names) - reusing existing i18n IDs
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000039', DEFAULT, null::varchar(255), 'twinClassFieldName', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000040', DEFAULT, null::varchar(255), 'twinClassFieldName', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000042', DEFAULT, null::varchar(255), 'twinClassFieldName', null) on conflict do nothing;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000039', 'en'::varchar(2), 'Total count'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000040', 'en'::varchar(2), 'Current progress'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000042', 'en'::varchar(2), 'Error count'::text, DEFAULT) on conflict do nothing;

-- i18n for twin_class fields (descriptions)
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000041', DEFAULT, null::varchar(255), 'twinClassFieldDescription', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000043', DEFAULT, null::varchar(255), 'twinClassFieldDescription', null) on conflict do nothing;
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000045', DEFAULT, null::varchar(255), 'twinClassFieldDescription', null) on conflict do nothing;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000041', 'en'::varchar(2), 'Total count'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000043', 'en'::varchar(2), 'Current progress'::text, DEFAULT) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000045', 'en'::varchar(2), 'Error count'::text, DEFAULT) on conflict do nothing;

-- twin_class_fields for JOB_SIMPLE
INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required, external_id, fe_validation_error_i18n_id, be_validation_error_i18n_id, twin_sorter_featurer_id, twin_sorter_params, external_properties, system, dependent_field, has_dependent_fields, "order", projection_field, has_projected_fields, field_initializer_featurer_id, field_initializer_params)
VALUES ('00000000-0000-0000-0011-000000000017'::uuid, '00000000-0000-0000-0001-000000000007'::uuid, 'total_count'::varchar(100), '00000000-0000-0000-0012-000000000039'::uuid, '00000000-0000-0000-0012-000000000041'::uuid, 1317::integer, null::hstore, null::uuid, null::uuid, DEFAULT, null::varchar(255), null::uuid, null::uuid, 4101::integer, null::hstore, null::hstore, DEFAULT, DEFAULT, DEFAULT, null::integer, DEFAULT, DEFAULT, 5301::integer, null::hstore) on conflict do nothing;

INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required, external_id, fe_validation_error_i18n_id, be_validation_error_i18n_id, twin_sorter_featurer_id, twin_sorter_params, external_properties, system, dependent_field, has_dependent_fields, "order", projection_field, has_projected_fields, field_initializer_featurer_id, field_initializer_params)
VALUES ('00000000-0000-0000-0011-000000000018'::uuid, '00000000-0000-0000-0001-000000000007'::uuid, 'current_count'::varchar(100), '00000000-0000-0000-0012-000000000040'::uuid, '00000000-0000-0000-0012-000000000043'::uuid, 1350::integer, null::hstore, null::uuid, null::uuid, DEFAULT, null::varchar(255), null::uuid, null::uuid, 4101::integer, null::hstore, null::hstore, DEFAULT, DEFAULT, DEFAULT, null::integer, DEFAULT, DEFAULT, 5301::integer, null::hstore) on conflict do nothing;

INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required, external_id, fe_validation_error_i18n_id, be_validation_error_i18n_id, twin_sorter_featurer_id, twin_sorter_params, external_properties, system, dependent_field, has_dependent_fields, "order", projection_field, has_projected_fields, field_initializer_featurer_id, field_initializer_params)
VALUES ('00000000-0000-0000-0011-000000000019'::uuid, '00000000-0000-0000-0001-000000000007'::uuid, 'error_count'::varchar(100), '00000000-0000-0000-0012-000000000042'::uuid, '00000000-0000-0000-0012-000000000045'::uuid, 1350::integer, null::hstore, null::uuid, null::uuid, DEFAULT, null::varchar(255), null::uuid, null::uuid, 4101::integer, null::hstore, null::hstore, DEFAULT, DEFAULT, DEFAULT, null::integer, DEFAULT, DEFAULT, 5301::integer, null::hstore) on conflict do nothing;

-- i18n for twinflow
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000055', null, null, 'twinflowName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000055', 'en', 'Default JOB_SIMPLE twinflow', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000056', null, null, 'twinflowDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000056', 'en', 'Default JOB_SIMPLE twinflow', 0) on conflict do nothing;

-- twinflow for JOB_SIMPLE (initial status: In progress)
INSERT INTO twinflow (id, twin_class_id, created_by_user_id, created_at, initial_twin_status_id, name_i18n_id, description_i18n_id, eraseflow_id, initial_sketch_twin_status_id)
VALUES ('00000000-0000-0000-0019-000000000001', '00000000-0000-0000-0001-000000000007', '00000000-0000-0000-0000-000000000000', '2026-04-10 00:00:00.000000', '00000000-0000-0000-0003-000000000006', '00000000-0000-0000-0012-000000000055', '00000000-0000-0000-0012-000000000056', null, null) on conflict do nothing;

INSERT INTO twinflow_schema_map (id, twinflow_schema_id, twin_class_id, twinflow_id)
VALUES ('00000000-0000-0000-0020-000000000001', '00000000-0000-0000-0017-000000000001', '00000000-0000-0000-0001-000000000007', '00000000-0000-0000-0019-000000000001') on conflict do nothing;

-- ==================== TRANSITIONS FOR JOB_SIMPLE ====================

-- i18n for transitions
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000057', null, null, 'twinflowTransitionName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000057', 'en', 'Complete', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000058', null, null, 'twinflowTransitionDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000058', 'en', 'Complete job successfully', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000059', null, null, 'twinflowTransitionName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000059', 'en', 'Error', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000060', null, null, 'twinflowTransitionDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000060', 'en', 'Complete job with error', 0) on conflict do nothing;

-- transition aliases (system, domain_id = null)
INSERT INTO twinflow_transition_alias (id, domain_id, alias)
VALUES ('00000000-0000-0000-0021-000000000001', null, 'complete') on conflict do nothing;

INSERT INTO twinflow_transition_alias (id, domain_id, alias)
VALUES ('00000000-0000-0000-0021-000000000002', null, 'error') on conflict do nothing;

-- transitions: In progress -> Completed
INSERT INTO twinflow_transition (id, twinflow_id, src_twin_status_id, dst_twin_status_id, twinflow_transition_alias_id, twinflow_transition_type_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0022-000000000001', '00000000-0000-0000-0019-000000000001', '00000000-0000-0000-0003-000000000006', '00000000-0000-0000-0003-000000000007', '00000000-0000-0000-0021-000000000001', 'STATUS_CHANGE', '00000000-0000-0000-0012-000000000057', '00000000-0000-0000-0012-000000000058') on conflict do nothing;

-- transitions: In progress -> Error
INSERT INTO twinflow_transition (id, twinflow_id, src_twin_status_id, dst_twin_status_id, twinflow_transition_alias_id, twinflow_transition_type_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0022-000000000002', '00000000-0000-0000-0019-000000000001', '00000000-0000-0000-0003-000000000006', '00000000-0000-0000-0003-000000000008', '00000000-0000-0000-0021-000000000002', 'STATUS_CHANGE', '00000000-0000-0000-0012-000000000059', '00000000-0000-0000-0012-000000000060') on conflict do nothing;

-- Add job_twin_class_id column to twin_trigger table
ALTER TABLE twin_trigger
ADD COLUMN IF NOT EXISTS job_twin_class_id UUID REFERENCES twin_class(id);
