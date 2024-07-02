-- INSERT
INSERT INTO public.twin_class (id, domain_id, key, permission_schema_space, abstract, head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, logo, created_by_user_id, created_at, twin_class_owner_type_id, domain_alias_counter, marker_data_list_id, tag_data_list_id, twinflow_schema_space, twin_class_schema_space, alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree, head_hunter_featurer_id, head_hunter_featurer_params) VALUES ('90fe039a-be5f-4db6-b715-79456fef744d', 'f01a8f45-6fad-4189-a320-bab0124d8bcb', 'ALCOSI_ANCESTOR', false, true, null, null, null, null, null, '00000000-0000-0000-0000-000000000000', '2023-09-25 14:48:05.581332', 'domainBusinessAccount', 0, null, null, false, false, false, null, '90fe039a-be5f-4db6-b715-79456fef744d'::ltree, '90fe039a-be5f-4db6-b715-79456fef744d'::ltree, null, null::hstore) on conflict do nothing;

-- SET EXIST DATA
UPDATE domain
SET ancestor_twin_class_id = '90fe039a-be5f-4db6-b715-79456fef744d'
WHERE id = 'f01a8f45-6fad-4189-a320-bab0124d8bcb';


ALTER TABLE domain
    ALTER COLUMN ancestor_twin_class_id SET NOT NULL;


