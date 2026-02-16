-- TWIN VALIDATOR SET permissions

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('c1a8b520-d241-4a5c-b6e6-7f8f95d0a4f3', 'Twin validator set manage name', null, 'permissionName', null),
    ('d2b9c631-e352-4b6d-c7f7-8080a16e1b54', 'Twin validator set manage description', null, 'permissionDescription', null),
    ('e3c0d742-f463-5c7e-a8b9-9191a27f2c65', 'Twin validator set create name', null, 'permissionName', null),
    ('f4d1e853-0a74-6d8f-a9ca-02023893d716', 'Twin validator set create description', null, 'permissionDescription', null),
    ('a5e2f964-1168-7e9f-b0db-1c3c394e8d77', 'Twin validator set view name', null, 'permissionName', null),
    ('b6f3a075-2279-8f0a-c1ec-2e4e40d5e948', 'Twin validator set view description', null, 'permissionDescription', null),
    ('c7a4b186-d387-9a0b-d2fd-3b5b51e6f059', 'Twin validator set update name', null, 'permissionName', null),
    ('d8b5c297-e498-0b1b-a3be-4c6c62c7e1a0', 'Twin validator set update description', null, 'permissionDescription', null),
    ('e9c6d308-f5a9-1c0b-b4cf-5d7d73d8e2b1', 'Twin validator set delete name', null, 'permissionName', null),
    ('f0a7e419-06b0-2d1b-a5da-6e8e84e9d3c2', 'Twin validator set delete description', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('c1a8b520-d241-4a5c-b6e6-7f8f95d0a4f3', 'en', 'Twin validator set manage', DEFAULT),
    ('d2b9c631-e352-4b6d-c7f7-8080a16e1b54', 'en', 'Twin validator set manage', DEFAULT),
    ('e3c0d742-f463-5c7e-a8b9-9191a27f2c65', 'en', 'Twin validator set create', DEFAULT),
    ('f4d1e853-0a74-6d8f-a9ca-02023893d716', 'en', 'Twin validator set create', DEFAULT),
    ('a5e2f964-1168-7e9f-b0db-1c3c394e8d77', 'en', 'Twin validator set view', DEFAULT),
    ('b6f3a075-2279-8f0a-c1ec-2e4e40d5e948', 'en', 'Twin validator set view', DEFAULT),
    ('c7a4b186-d387-9a0b-d2fd-3b5b51e6f059', 'en', 'Twin validator set update', DEFAULT),
    ('d8b5c297-e498-0b1b-a3be-4c6c62c7e1a0', 'en', 'Twin validator set update', DEFAULT),
    ('e9c6d308-f5a9-1c0b-b4cf-5d7d73d8e2b1', 'en', 'Twin validator set delete', DEFAULT),
    ('f0a7e419-06b0-2d1b-a5da-6e8e84e9d3c2', 'en', 'Twin validator set delete', DEFAULT)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0053-000000000001', 'TWIN_VALIDATOR_SET_MANAGE', '00000000-0000-0000-0005-000000000001', 'c1a8b520-d241-4a5c-b6e6-7f8f95d0a4f3', 'd2b9c631-e352-4b6d-c7f7-8080a16e1b54'),
    ('00000000-0000-0004-0053-000000000002', 'TWIN_VALIDATOR_SET_CREATE', '00000000-0000-0000-0005-000000000001', 'e3c0d742-f463-5c7e-a8b9-9191a27f2c65', 'f4d1e853-0a74-6d8f-a9ca-02023893d716'),
    ('00000000-0000-0004-0053-000000000003', 'TWIN_VALIDATOR_SET_VIEW', '00000000-0000-0000-0005-000000000001', 'a5e2f964-1168-7e9f-b0db-1c3c394e8d77', 'b6f3a075-2279-8f0a-c1ec-2e4e40d5e948'),
    ('00000000-0000-0004-0053-000000000004', 'TWIN_VALIDATOR_SET_UPDATE', '00000000-0000-0000-0005-000000000001', 'c7a4b186-d387-9a0b-d2fd-3b5b51e6f059', 'd8b5c297-e498-0b1b-a3be-4c6c62c7e1a0'),
    ('00000000-0000-0004-0053-000000000005', 'TWIN_VALIDATOR_SET_DELETE', '00000000-0000-0000-0005-000000000001', 'e9c6d308-f5a9-1c0b-b4cf-5d7d73d8e2b1', 'f0a7e419-06b0-2d1b-a5da-6e8e84e9d3c2')
ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000202'::uuid, '00000000-0000-0004-0053-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000203'::uuid, '00000000-0000-0004-0053-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000204'::uuid, '00000000-0000-0004-0053-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000205'::uuid, '00000000-0000-0004-0053-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000206'::uuid, '00000000-0000-0004-0053-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;