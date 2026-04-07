-- TWIN VALIDATOR permissions

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019c6ac7-8a8c-7efa-a06e-049b5f8144ab', 'Twin validator manage name', null, 'permissionName', null),
    ('019c6ac8-27ea-7022-b5a9-d155d1ae443b', 'Twin validator manage description', null, 'permissionDescription', null),
    ('019c6ac8-42d7-7845-a454-58dc2a7eaee4', 'Twin validator create name', null, 'permissionName', null),
    ('019c6ac8-86b6-79a3-81bc-68080e26aa35', 'Twin validator create description', null, 'permissionDescription', null),
    ('019c6ac8-a697-7960-a47e-01d127c49b67', 'Twin validator view name', null, 'permissionName', null),
    ('019c6ac8-c7e4-7d83-9078-9aeec1f41d34', 'Twin validator view description', null, 'permissionDescription', null),
    ('019c6ac8-eb2f-780c-8b0f-22cbf80f23eb', 'Twin validator update name', null, 'permissionName', null),
    ('019c6ac9-1f82-7eb5-89bb-5884d23239f2', 'Twin validator update description', null, 'permissionDescription', null),
    ('019c6ac9-4341-7dbd-92a5-e7fe73c857d7', 'Twin validator delete name', null, 'permissionName', null),
    ('019c6ac9-657d-74eb-8363-82fd237255ec', 'Twin validator delete description', null, 'permissionDescription', null)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019c6ac7-8a8c-7efa-a06e-049b5f8144ab', 'en', 'Twin validator manage', DEFAULT),
    ('019c6ac8-27ea-7022-b5a9-d155d1ae443b', 'en', 'Twin validator manage', DEFAULT),
    ('019c6ac8-42d7-7845-a454-58dc2a7eaee4', 'en', 'Twin validator create', DEFAULT),
    ('019c6ac8-86b6-79a3-81bc-68080e26aa35', 'en', 'Twin validator create', DEFAULT),
    ('019c6ac8-a697-7960-a47e-01d127c49b67', 'en', 'Twin validator view', DEFAULT),
    ('019c6ac8-c7e4-7d83-9078-9aeec1f41d34', 'en', 'Twin validator view', DEFAULT),
    ('019c6ac8-eb2f-780c-8b0f-22cbf80f23eb', 'en', 'Twin validator update', DEFAULT),
    ('019c6ac9-1f82-7eb5-89bb-5884d23239f2', 'en', 'Twin validator update', DEFAULT),
    ('019c6ac9-4341-7dbd-92a5-e7fe73c857d7', 'en', 'Twin validator delete', DEFAULT),
    ('019c6ac9-657d-74eb-8363-82fd237255ec', 'en', 'Twin validator delete', DEFAULT)
    ON CONFLICT (i18n_id, locale) DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0056-000000000001', 'TWIN_VALIDATOR_MANAGE', '00000000-0000-0000-0005-000000000001', '019c6ac7-8a8c-7efa-a06e-049b5f8144ab', '019c6ac8-27ea-7022-b5a9-d155d1ae443b'),
    ('00000000-0000-0004-0056-000000000002', 'TWIN_VALIDATOR_CREATE', '00000000-0000-0000-0005-000000000001', '019c6ac8-42d7-7845-a454-58dc2a7eaee4', '019c6ac8-86b6-79a3-81bc-68080e26aa35'),
    ('00000000-0000-0004-0056-000000000003', 'TWIN_VALIDATOR_VIEW', '00000000-0000-0000-0005-000000000001', '019c6ac8-a697-7960-a47e-01d127c49b67', '019c6ac8-c7e4-7d83-9078-9aeec1f41d34'),
    ('00000000-0000-0004-0056-000000000004', 'TWIN_VALIDATOR_UPDATE', '00000000-0000-0000-0005-000000000001', '019c6ac8-eb2f-780c-8b0f-22cbf80f23eb', '019c6ac9-1f82-7eb5-89bb-5884d23239f2'),
    ('00000000-0000-0004-0056-000000000005', 'TWIN_VALIDATOR_DELETE', '00000000-0000-0000-0005-000000000001', '019c6ac9-4341-7dbd-92a5-e7fe73c857d7', '019c6ac9-657d-74eb-8363-82fd237255ec')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES
    ('00000000-0000-0000-0007-000000000217'::uuid, '00000000-0000-0004-0056-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT),
    ('00000000-0000-0000-0007-000000000218'::uuid, '00000000-0000-0004-0056-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT),
    ('00000000-0000-0000-0007-000000000219'::uuid, '00000000-0000-0004-0056-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT),
    ('00000000-0000-0000-0007-000000000220'::uuid, '00000000-0000-0004-0056-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT),
    ('00000000-0000-0000-0007-000000000221'::uuid, '00000000-0000-0004-0056-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT)
    ON CONFLICT (id) DO NOTHING;
