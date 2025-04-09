-- a system permission
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('b9ebdb43-e334-46f2-93d8-d8d79d66c6a6', '', null, 'permissionName'),
    ('edf24088-797d-4485-98af-929710bc046c', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('b9ebdb43-e334-46f2-93d8-d8d79d66c6a6', 'en', 'Domain twins create any', 0),
    ('edf24088-797d-4485-98af-929710bc046c', 'en', 'domain twins create any', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000033', 'DOMAIN_TWINS_CREATE_ANY', '00000000-0000-0000-0005-000000000001', 'b9ebdb43-e334-46f2-93d8-d8d79d66c6a6', 'edf24088-797d-4485-98af-929710bc046c') on conflict on constraint permission_pk do nothing ;

-- permission for system group
INSERT INTO public.permission_grant_user_group (id, permission_schema_id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('cb787684-c0fd-4458-9416-f0e5313af7ae', 'af143656-9899-4e1f-8683-48795cdefeac', '00000000-0000-0000-0004-000000000033', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', '2025-03-10 07:00:23.579577') on conflict on constraint permission_schema_user_group_pk do nothing ;
