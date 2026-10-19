INSERT INTO i18n (id, name, key, i18n_type_id) VALUES  ('019e728f-efb7-721c-868e-f7f436c59345', 'Evict cache', null, 'permissionName') on conflict do nothing;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019e728f-efb7-721c-868e-f7f436c59345', 'en', 'Evict cache', 0) on conflict do nothing;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id) VALUES ('00000000-0000-0004-0001-000000000501', ' SYSTEM_CACHE_EVICT', '00000000-0000-0000-0005-000000000001', '019e728f-efb7-721c-868e-f7f436c59345') on conflict do nothing;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000343', '00000000-0000-0004-0001-000000000501', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;