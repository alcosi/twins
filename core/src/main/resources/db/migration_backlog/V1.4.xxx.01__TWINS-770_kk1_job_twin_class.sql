INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1350::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperDecimalIncrement'::varchar, 'Decimal Increment'::varchar, 'Decimal field with atomic increment/decrement support (+N/-N format)'::varchar(255), false::boolean)
    on conflict do nothing;

-- i18n for twin_class
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('019d05e9-1dae-7201-aabd-5187fc473008', null, null, 'twinClassName', '0bc892b6-ef88-47c4-ad92-19cc89576f65') on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d05e9-1dae-7201-aabd-5187fc473008', 'en', 'Job', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('019d05e9-1db1-779c-9ad7-72c451a0ae7c', null, null, 'twinClassDescription', '0bc892b6-ef88-47c4-ad92-19cc89576f65') on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d05e9-1db1-779c-9ad7-72c451a0ae7c', 'en', 'Job for tracking task status', 0) on conflict do nothing;

-- twin_class (system UUID from SystemEntityService)
INSERT INTO twin_class (id, domain_id, key, permission_schema_space, abstract, head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, created_by_user_id, created_at, twin_class_owner_type_id, domain_alias_counter, marker_data_list_id, tag_data_list_id, twinflow_schema_space, twin_class_schema_space, alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree, head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id, edit_permission_id, delete_permission_id, page_face_id, assignee_required, general_attachment_restriction_id, comment_attachment_restriction_id, external_id, bread_crumbs_face_id, inherited_bread_crumbs_face_id, inherited_page_face_id, inherited_bread_crumbs_twin_class_id, inherited_page_twin_class_id, external_properties, icon_dark_resource_id, icon_light_resource_id, external_json, segment, has_segments, twin_class_freeze_id, inherited_marker_data_list_id, inherited_marker_data_list_twin_class_id, inherited_tag_data_list_id, inherited_tag_data_list_twin_class_id, has_dynamic_markers, extends_hierarchy_counter_direct_children, head_hierarchy_counter_direct_children, unique_name, twin_counter)
VALUES ('00000000-0000-0000-0001-000000000006', null, 'JOB', false, false, null, '00000000-0000-0000-0001-000000000004', '019d05e9-1dae-7201-aabd-5187fc473008', '019d05e9-1db1-779c-9ad7-72c451a0ae7c', '608c6d7d-99c8-4d87-89c6-2f72d0f5d673', '2026-03-19 11:44:16.824836', 'domain', 0, null, null, false, false, false, null, '00000000_0000_0000_0001_000000000006', '00000000_0000_0000_0001_000000000004.3a2fcc3a_3f53_4ba9_b7ab_0b1ae1635be4.00000000_0000_0000_0001_000000000006', null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, false, false, null, null, null, null, null, false, 0, 0, false, 0) on conflict do nothing;

-- i18n for twin_status
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000044', null, null, 'twinStatusName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000044', 'en', 'Initial', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000046', null, null, 'twinStatusDescription', null) on conflict do nothing;

-- twin_status (system UUID from SystemEntityService)
INSERT INTO twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, background_color, key, font_color, icon_dark_resource_id, icon_light_resource_id, twin_status_type)
VALUES ('00000000-0000-0000-0003-000000000005', '00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0012-000000000044', '00000000-0000-0000-0012-000000000046', null, 'init', null, null, null, 'BASIC') on conflict do nothing;

-- i18n for twin_class fields (names)
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

-- twin_class_fields (system UUIDs from SystemEntityService)
INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required, external_id, fe_validation_error_i18n_id, be_validation_error_i18n_id, twin_sorter_featurer_id, twin_sorter_params, external_properties, system, dependent_field, has_dependent_fields, "order", projection_field, has_projected_fields, field_initializer_featurer_id, field_initializer_params)
VALUES ('00000000-0000-0000-0011-000000000017'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'total_count'::varchar(100), '00000000-0000-0000-0012-000000000039'::uuid, '00000000-0000-0000-0012-000000000041'::uuid, 1317::integer, null::hstore, null::uuid, null::uuid, DEFAULT, null::varchar(255), null::uuid, null::uuid, 4101::integer, null::hstore, null::hstore, DEFAULT, DEFAULT, DEFAULT, null::integer, DEFAULT, DEFAULT, 5301::integer, null::hstore) on conflict do nothing;

INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required, external_id, fe_validation_error_i18n_id, be_validation_error_i18n_id, twin_sorter_featurer_id, twin_sorter_params, external_properties, system, dependent_field, has_dependent_fields, "order", projection_field, has_projected_fields, field_initializer_featurer_id, field_initializer_params)
VALUES ('00000000-0000-0000-0011-000000000018'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'current_count'::varchar(100), '00000000-0000-0000-0012-000000000040'::uuid, '00000000-0000-0000-0012-000000000043'::uuid, 1350::integer, null::hstore, null::uuid, null::uuid, DEFAULT, null::varchar(255), null::uuid, null::uuid, 4101::integer, null::hstore, null::hstore, DEFAULT, DEFAULT, DEFAULT, null::integer, DEFAULT, DEFAULT, 5301::integer, null::hstore) on conflict do nothing;

INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required, external_id, fe_validation_error_i18n_id, be_validation_error_i18n_id, twin_sorter_featurer_id, twin_sorter_params, external_properties, system, dependent_field, has_dependent_fields, "order", projection_field, has_projected_fields, field_initializer_featurer_id, field_initializer_params)
VALUES ('00000000-0000-0000-0011-000000000019'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'error_count'::varchar(100), '00000000-0000-0000-0012-000000000042'::uuid, '00000000-0000-0000-0012-000000000045'::uuid, 1350::integer, null::hstore, null::uuid, null::uuid, DEFAULT, null::varchar(255), null::uuid, null::uuid, 4101::integer, null::hstore, null::hstore, DEFAULT, DEFAULT, DEFAULT, null::integer, DEFAULT, DEFAULT, 5301::integer, null::hstore) on conflict do nothing;

-- i18n for twinflow
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('019d05e9-1e31-74c0-96f7-92449a2b2f4b', null, null, 'twinflowName', '0bc892b6-ef88-47c4-ad92-19cc89576f65') on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d05e9-1e31-74c0-96f7-92449a2b2f4b', 'en', 'Default JOB twinflow', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('019d05e9-1e32-7eb7-8840-b0082677b8c0', null, null, 'twinflowDescription', '0bc892b6-ef88-47c4-ad92-19cc89576f65') on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d05e9-1e32-7eb7-8840-b0082677b8c0', 'en', 'Default JOB twinflow', 0) on conflict do nothing;

-- twinflow (uses system UUIDs from SystemEntityService)
INSERT INTO twinflow (id, twin_class_id, created_by_user_id, created_at, initial_twin_status_id, name_i18n_id, description_i18n_id, eraseflow_id, initial_sketch_twin_status_id)
VALUES ('019d05e9-1e35-75da-9bdc-b90944918b09', '00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0000-000000000000', '2026-03-19 11:44:16.948183', '00000000-0000-0000-0003-000000000005', '019d05e9-1e31-74c0-96f7-92449a2b2f4b', '019d05e9-1e32-7eb7-8840-b0082677b8c0', null, null) on conflict do nothing;

INSERT INTO twinflow_schema_map (id, twinflow_schema_id, twin_class_id, twinflow_id)
VALUES ('019d05e9-1e36-7ae0-ad8f-61ea7a2a9cdf', '8026d975-b9f9-4f0a-82ea-e426268321c5', '00000000-0000-0000-0001-000000000006', '019d05e9-1e35-75da-9bdc-b90944918b09') on conflict do nothing;

ALTER TABLE twin_trigger
ADD COLUMN IF NOT EXISTS job_twin_class_id UUID REFERENCES twin_class(id);
