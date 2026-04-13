alter table twinflow_transition_alias
    alter column domain_id drop not null;

-- Insert featurer for Decimal Increment
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1350::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperDecimalIncrement'::varchar, 'Decimal Increment'::varchar, 'Decimal field with atomic increment/decrement support (+N/-N format)'::varchar(255), false::boolean)
    on conflict do nothing;

-- i18n for JOB twin_class
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000061', null, null, 'twinClassName', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000061', 'en', 'Job', 0) on conflict do nothing;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000000-0000-0000-0012-000000000062', null, null, 'twinClassDescription', null) on conflict do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-000000000062', 'en', 'Abstract Job class', 0) on conflict do nothing;

-- twin_class JOB (abstract, extends GLOBAL_ANCESTOR)
INSERT INTO twin_class (id, domain_id, key, permission_schema_space, abstract, head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, created_by_user_id, created_at, twin_class_owner_type_id, domain_alias_counter, marker_data_list_id, tag_data_list_id, twinflow_schema_space, twin_class_schema_space, alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree, head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id, edit_permission_id, delete_permission_id, page_face_id, assignee_required, general_attachment_restriction_id, comment_attachment_restriction_id, external_id, bread_crumbs_face_id, inherited_bread_crumbs_face_id, inherited_page_face_id, inherited_bread_crumbs_twin_class_id, inherited_page_twin_class_id, external_properties, icon_dark_resource_id, icon_light_resource_id, external_json, segment, has_segments, twin_class_freeze_id, inherited_marker_data_list_id, inherited_marker_data_list_twin_class_id, inherited_tag_data_list_id, inherited_tag_data_list_twin_class_id, has_dynamic_markers, extends_hierarchy_counter_direct_children, head_hierarchy_counter_direct_children, unique_name, twin_counter)
VALUES ('00000000-0000-0000-0001-000000000006', null, 'JOB', false, true, null, '00000000-0000-0000-0001-000000000004', '00000000-0000-0000-0012-000000000061', '00000000-0000-0000-0012-000000000062', '608c6d7d-99c8-4d87-89c6-2f72d0f5d673', '2026-03-19 11:44:16.824836', 'domain', 0, null, null, false, false, false, null, '00000000_0000_0000_0001_000000000006', '00000000_0000_0000_0001_000000000004.3a2fcc3a_3f53_4ba9_b7ab_0b1ae1635be4.00000000_0000_0000_0001_000000000006', null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, false, false, null, null, null, null, null, false, 0, 0, false, 0) on conflict do nothing;

-- Add job_twin_class_id column to twin_trigger table
ALTER TABLE twin_trigger
ADD COLUMN IF NOT EXISTS job_twin_class_id UUID REFERENCES twin_class(id);
