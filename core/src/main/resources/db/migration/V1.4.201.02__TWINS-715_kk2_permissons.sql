-- TWIN TRIGGER permissions

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019c6ac7-8a8c-7efa-a06e-049b5f8144af', 'Twin trigger manage name', null, 'permissionName', null),
    ('019c6ac8-27ea-7022-b5a9-d155d1ae443b', 'Twin trigger manage description', null, 'permissionDescription', null),
    ('019c6ac8-42d7-7845-a454-58dc2a7eaee4', 'Twin trigger create name', null, 'permissionName', null),
    ('019c6ac8-86b6-79a3-81bc-68080e26aa35', 'Twin trigger create description', null, 'permissionDescription', null),
    ('019c6ac8-a697-7960-a47e-01d127c49b67', 'Twin trigger view name', null, 'permissionName', null),
    ('019c6ac8-c7e4-7d83-9078-9aeec1f41d34', 'Twin trigger view description', null, 'permissionDescription', null),
    ('019c6ac8-eb2f-780c-8b0f-22cbf80f23eb', 'Twin trigger update name', null, 'permissionName', null),
    ('019c6ac9-1f82-7eb5-89bb-5884d23239f2', 'Twin trigger update description', null, 'permissionDescription', null),
    ('019c6ac9-4341-7dbd-92a5-e7fe73c857d7', 'Twin trigger delete name', null, 'permissionName', null),
    ('019c6ac9-657d-74eb-8363-82fd237255ec', 'Twin trigger delete description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019c6ac7-8a8c-7efa-a06e-049b5f8144af', 'en', 'Twin trigger manage', DEFAULT),
    ('019c6ac8-27ea-7022-b5a9-d155d1ae443b', 'en', 'Twin trigger manage', DEFAULT),
    ('019c6ac8-42d7-7845-a454-58dc2a7eaee4', 'en', 'Twin trigger create', DEFAULT),
    ('019c6ac8-86b6-79a3-81bc-68080e26aa35', 'en', 'Twin trigger create', DEFAULT),
    ('019c6ac8-a697-7960-a47e-01d127c49b67', 'en', 'Twin trigger view', DEFAULT),
    ('019c6ac8-c7e4-7d83-9078-9aeec1f41d34', 'en', 'Twin trigger view', DEFAULT),
    ('019c6ac8-eb2f-780c-8b0f-22cbf80f23eb', 'en', 'Twin trigger update', DEFAULT),
    ('019c6ac9-1f82-7eb5-89bb-5884d23239f2', 'en', 'Twin trigger update', DEFAULT),
    ('019c6ac9-4341-7dbd-92a5-e7fe73c857d7', 'en', 'Twin trigger delete', DEFAULT),
    ('019c6ac9-657d-74eb-8363-82fd237255ec', 'en', 'Twin trigger delete', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0054-000000000001', 'TWIN_TRIGGER_MANAGE', '00000000-0000-0000-0005-000000000001', '019c6ac7-8a8c-7efa-a06e-049b5f8144af', '019c6ac8-27ea-7022-b5a9-d155d1ae443b'),
    ('00000000-0000-0004-0054-000000000002', 'TWIN_TRIGGER_CREATE', '00000000-0000-0000-0005-000000000001', '019c6ac8-42d7-7845-a454-58dc2a7eaee4', '019c6ac8-86b6-79a3-81bc-68080e26aa35'),
    ('00000000-0000-0004-0054-000000000003', 'TWIN_TRIGGER_VIEW', '00000000-0000-0000-0005-000000000001', '019c6ac8-a697-7960-a47e-01d127c49b67', '019c6ac8-c7e4-7d83-9078-9aeec1f41d34'),
    ('00000000-0000-0004-0054-000000000004', 'TWIN_TRIGGER_UPDATE', '00000000-0000-0000-0005-000000000001', '019c6ac8-eb2f-780c-8b0f-22cbf80f23eb', '019c6ac9-1f82-7eb5-89bb-5884d23239f2'),
    ('00000000-0000-0004-0054-000000000005', 'TWIN_TRIGGER_DELETE', '00000000-0000-0000-0005-000000000001', '019c6ac9-4341-7dbd-92a5-e7fe73c857d7', '019c6ac9-657d-74eb-8363-82fd237255ec')
    ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000212'::uuid, '00000000-0000-0004-0054-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000213'::uuid, '00000000-0000-0004-0054-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000214'::uuid, '00000000-0000-0004-0054-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000215'::uuid, '00000000-0000-0004-0054-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000216'::uuid, '00000000-0000-0004-0054-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
