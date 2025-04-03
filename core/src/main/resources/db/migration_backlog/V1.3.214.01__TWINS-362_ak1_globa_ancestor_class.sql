INSERT INTO public.twin_class (id, domain_id, key, permission_schema_space, abstract, head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, logo, created_by_user_id, created_at, twin_class_owner_type_id, domain_alias_counter, marker_data_list_id, tag_data_list_id, twinflow_schema_space, twin_class_schema_space, alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree, head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id, edit_permission_id, delete_permission_id, page_face_id) VALUES ('00000000-0000-0000-0001-000000000004'::uuid, null::uuid, 'GLOBAL_ANCESTOR'::varchar(100), false::boolean, false::boolean, null::uuid, null::uuid, null::uuid, null::uuid, null::varchar, '00000000-0000-0000-0000-000000000000'::uuid, '2023-11-09 13:54:48.521000'::timestamp, 'system'::varchar, 0::integer, null::uuid, null::uuid, false::boolean, false::boolean, false::boolean, null::uuid, null::ltree, null::ltree, null::integer, null::hstore, null::uuid, null::uuid, null::uuid, null::uuid) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1321, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperBaseField', 'Base', '', false) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('a3308f77-df67-43b6-9000-024d374b996e'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('ebe07a73-9dc4-4ab3-ad45-eec4ba5d7219'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('63a6d525-776d-4cc6-8f82-847d5aa395b3'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('453c49a1-62a1-437f-87f2-9c5153601e60'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00523831-f90e-401f-928f-a499f71b0440'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('7034f79f-0848-4961-a7eb-1c45983ade80'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('7d06fe90-bb3b-4bd9-aa3f-15ea6143954d'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('fd0d5e15-73d2-41f2-9280-f10433079790'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('0c56b090-9926-4144-b67c-9f3bf80c99a8'::uuid, DEFAULT, null::varchar(255), 'twinClassFieldName'::varchar, null::uuid) on conflict (id) do nothing;


INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('f86d262e-2555-47e1-a8d5-883dcaecb09e'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('71883d3a-6831-4a06-a3e5-08d59e982ccc'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('815a9ce0-2a81-432d-b10c-d36a11b0c6fb'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('d1107bbc-2e87-48fc-ad98-364c091ff5e2'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('3e564eae-6e47-443b-9c2b-808b8ec71d67'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('26aa0652-5a6b-4956-bf38-3341da01c862'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('6229ff5b-bd30-409e-aa95-d21f64d16d7d'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('291c9cb0-424f-446d-a8be-302a263c816d'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('81274d5a-6670-4423-9714-c9c5ae4864b0'::uuid, null::varchar(255), null::varchar(255), 'twinClassFieldDescription'::varchar, null::uuid) on conflict (id) do nothing;

INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000003'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_name'::varchar(100), 'a3308f77-df67-43b6-9000-024d374b996e'::uuid, 'f86d262e-2555-47e1-a8d5-883dcaecb09e'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000004'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_description'::varchar(100), 'ebe07a73-9dc4-4ab3-ad45-eec4ba5d7219'::uuid, '71883d3a-6831-4a06-a3e5-08d59e982ccc'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000005'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_external_id'::varchar(100), '63a6d525-776d-4cc6-8f82-847d5aa395b3'::uuid, '815a9ce0-2a81-432d-b10c-d36a11b0c6fb'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000006'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_owner_user'::varchar(100), '453c49a1-62a1-437f-87f2-9c5153601e60'::uuid, 'd1107bbc-2e87-48fc-ad98-364c091ff5e2'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000007'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_assignee_user'::varchar(100), '00523831-f90e-401f-928f-a499f71b0440'::uuid, '3e564eae-6e47-443b-9c2b-808b8ec71d67'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000008'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_creator_user'::varchar(100), '7034f79f-0848-4961-a7eb-1c45983ade80'::uuid, '26aa0652-5a6b-4956-bf38-3341da01c862'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000009'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_head'::varchar(100), '7d06fe90-bb3b-4bd9-aa3f-15ea6143954d'::uuid, '6229ff5b-bd30-409e-aa95-d21f64d16d7d'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000010'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_status'::varchar(100), 'fd0d5e15-73d2-41f2-9280-f10433079790'::uuid, '291c9cb0-424f-446d-a8be-302a263c816d'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000011'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_created_at'::varchar(100), '0c56b090-9926-4144-b67c-9f3bf80c99a8'::uuid, '81274d5a-6670-4423-9714-c9c5ae4864b0'::uuid, 1321::integer, null::hstore, null::uuid, null::uuid, false::boolean) on conflict (id) do nothing;
