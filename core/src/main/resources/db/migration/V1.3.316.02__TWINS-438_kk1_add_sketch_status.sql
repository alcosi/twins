INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000001-0000-0000-0000-000000000001', null, null, 'twinStatusName', null) on conflict do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000001-0000-0000-0000-000000000002', null, null, 'twinStatusDescription', null) on conflict do nothing;

INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000001-0000-0000-0000-000000000001', 'en', 'Sketch', 0) on conflict do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000001-0000-0000-0000-000000000002', 'en', '', 0) on conflict do nothing;

INSERT INTO public.twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, logo, background_color, key, font_color) VALUES ('00000001-0000-0000-0000-000000000001', '00000000-0000-0000-0001-000000000004', '00000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000002', null, '#000000', 'sketch', '#000000') on conflict do nothing;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES  ('19aa7ccf-cbec-4922-80a2-1b5ca00dd3b8', 'Twin sketch create', null, 'permissionName'),   ('ef4d65eb-594d-3b17-b5d3-66ab79956725', 'Twinflow create description', null, 'permissionDescription')  on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;

INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('19aa7ccf-cbec-4922-80a2-1b5ca00dd3b8', 'en', 'Twin sketch create', 0), ('ef4d65eb-594d-3b17-b5d3-66ab79956725', 'en', 'Twinflow create', 0) on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id) VALUES ('00000000-0000-0004-0011-000000000006', 'TWIN_SKETCH_CREATE', '00000000-0000-0000-0005-000000000001', '19aa7ccf-cbec-4922-80a2-1b5ca00dd3b8') on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000186', '00000000-0000-0004-0011-000000000006', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;