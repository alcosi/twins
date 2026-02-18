-- TWIN VALIDATOR SET permissions

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019c6ac7-8a8c-7efa-a06e-049b5f8144ae', 'Twin validator set manage name', null, 'permissionName', null),
    ('019c6ac8-27ea-7022-b5a9-d155d1ae443a', 'Twin validator set manage description', null, 'permissionDescription', null),
    ('019c6ac8-42d7-7845-a454-58dc2a7eaee3', 'Twin validator set create name', null, 'permissionName', null),
    ('019c6ac8-86b6-79a3-81bc-68080e26aa34', 'Twin validator set create description', null, 'permissionDescription', null),
    ('019c6ac8-a697-7960-a47e-01d127c49b66', 'Twin validator set view name', null, 'permissionName', null),
    ('019c6ac8-c7e4-7d83-9078-9aeec1f41d33', 'Twin validator set view description', null, 'permissionDescription', null),
    ('019c6ac8-eb2f-780c-8b0f-22cbf80f23ea', 'Twin validator set update name', null, 'permissionName', null),
    ('019c6ac9-1f82-7eb5-89bb-5884d23239f1', 'Twin validator set update description', null, 'permissionDescription', null),
    ('019c6ac9-4341-7dbd-92a5-e7fe73c857d6', 'Twin validator set delete name', null, 'permissionName', null),
    ('019c6ac9-657d-74eb-8363-82fd237255eb', 'Twin validator set delete description', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019c6ac7-8a8c-7efa-a06e-049b5f8144ae', 'en', 'Twin validator set manage', DEFAULT),
    ('019c6ac8-27ea-7022-b5a9-d155d1ae443a', 'en', 'Twin validator set manage', DEFAULT),
    ('019c6ac8-42d7-7845-a454-58dc2a7eaee3', 'en', 'Twin validator set create', DEFAULT),
    ('019c6ac8-86b6-79a3-81bc-68080e26aa34', 'en', 'Twin validator set create', DEFAULT),
    ('019c6ac8-a697-7960-a47e-01d127c49b66', 'en', 'Twin validator set view', DEFAULT),
    ('019c6ac8-c7e4-7d83-9078-9aeec1f41d33', 'en', 'Twin validator set view', DEFAULT),
    ('019c6ac8-eb2f-780c-8b0f-22cbf80f23ea', 'en', 'Twin validator set update', DEFAULT),
    ('019c6ac9-1f82-7eb5-89bb-5884d23239f1', 'en', 'Twin validator set update', DEFAULT),
    ('019c6ac9-4341-7dbd-92a5-e7fe73c857d6', 'en', 'Twin validator set delete', DEFAULT),
    ('019c6ac9-657d-74eb-8363-82fd237255eb', 'en', 'Twin validator set delete', DEFAULT)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0053-000000000001', 'TWIN_VALIDATOR_SET_MANAGE', '00000000-0000-0000-0005-000000000001', '019c6ac7-8a8c-7efa-a06e-049b5f8144ae', '019c6ac8-27ea-7022-b5a9-d155d1ae443a'),
    ('00000000-0000-0004-0053-000000000002', 'TWIN_VALIDATOR_SET_CREATE', '00000000-0000-0000-0005-000000000001', '019c6ac8-42d7-7845-a454-58dc2a7eaee3', '019c6ac8-86b6-79a3-81bc-68080e26aa34'),
    ('00000000-0000-0004-0053-000000000003', 'TWIN_VALIDATOR_SET_VIEW', '00000000-0000-0000-0005-000000000001', '019c6ac8-a697-7960-a47e-01d127c49b66', '019c6ac8-c7e4-7d83-9078-9aeec1f41d33'),
    ('00000000-0000-0004-0053-000000000004', 'TWIN_VALIDATOR_SET_UPDATE', '00000000-0000-0000-0005-000000000001', '019c6ac8-eb2f-780c-8b0f-22cbf80f23ea', '019c6ac9-1f82-7eb5-89bb-5884d23239f1'),
    ('00000000-0000-0004-0053-000000000005', 'TWIN_VALIDATOR_SET_DELETE', '00000000-0000-0000-0005-000000000001', '019c6ac9-4341-7dbd-92a5-e7fe73c857d6', '019c6ac9-657d-74eb-8363-82fd237255eb')
ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000202'::uuid, '00000000-0000-0004-0053-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000203'::uuid, '00000000-0000-0004-0053-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000204'::uuid, '00000000-0000-0004-0053-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000205'::uuid, '00000000-0000-0004-0053-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000206'::uuid, '00000000-0000-0004-0053-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;