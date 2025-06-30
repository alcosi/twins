
-- ACT_AS_USER permission
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('fb8cd66e-db98-4882-9a4d-66cf78102290', 'Act as user permission name', null, 'permissionName'),
                                                          ('a007e960-9b60-4e41-8d8c-5f1acd2a3bf4', 'Act as user permission description', null, 'permissionDescription')
    on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('fb8cd66e-db98-4882-9a4d-66cf78102290', 'en', 'Act as user', 0),
                                                                                      ('a007e960-9b60-4e41-8d8c-5f1acd2a3bf4', 'en', 'Give possibility to act as other user', 0)
    on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
    ('00000000-0000-0004-0001-000000000401', 'ACT_AS_USER', '00000000-0000-0000-0005-000000000001', 'fb8cd66e-db98-4882-9a4d-66cf78102290', 'a007e960-9b60-4e41-8d8c-5f1acd2a3bf4')
    on conflict (id) do update set key=excluded.key;
