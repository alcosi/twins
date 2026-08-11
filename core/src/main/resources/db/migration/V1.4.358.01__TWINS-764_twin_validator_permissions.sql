-- TWIN VALIDATOR permissions
-- TODO: replace TWINS-XXX in the filename with the actual ticket number.

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('04654276-6673-4122-b472-02dd413c4033', 'Twin validator manage name', null, 'permissionName', null),
    ('c9e64d9f-e651-43f9-a48a-b582da37ff5e', 'Twin validator manage description', null, 'permissionDescription', null),
    ('e607440a-abce-4443-bb05-714898cf345b', 'Twin validator create name', null, 'permissionName', null),
    ('5e26ceea-3528-4950-965f-508e1061ef69', 'Twin validator create description', null, 'permissionDescription', null),
    ('27a5a18a-3715-4a67-a5d5-2a86983ae757', 'Twin validator view name', null, 'permissionName', null),
    ('8f213f98-c218-4867-8263-8b3a86278e36', 'Twin validator view description', null, 'permissionDescription', null),
    ('aa140b0a-72b5-4e7a-a255-bf59e07d1f3a', 'Twin validator update name', null, 'permissionName', null),
    ('f5b470ee-e522-4ac3-901f-640fa0a8df15', 'Twin validator update description', null, 'permissionDescription', null),
    ('1eed9a60-5705-44b2-a627-60f58451b944', 'Twin validator delete name', null, 'permissionName', null),
    ('522cbecd-a140-42bc-b26d-6e7d873b1e11', 'Twin validator delete description', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('04654276-6673-4122-b472-02dd413c4033', 'en', 'Twin validator manage', DEFAULT),
    ('c9e64d9f-e651-43f9-a48a-b582da37ff5e', 'en', 'Twin validator manage', DEFAULT),
    ('e607440a-abce-4443-bb05-714898cf345b', 'en', 'Twin validator create', DEFAULT),
    ('5e26ceea-3528-4950-965f-508e1061ef69', 'en', 'Twin validator create', DEFAULT),
    ('27a5a18a-3715-4a67-a5d5-2a86983ae757', 'en', 'Twin validator view', DEFAULT),
    ('8f213f98-c218-4867-8263-8b3a86278e36', 'en', 'Twin validator view', DEFAULT),
    ('aa140b0a-72b5-4e7a-a255-bf59e07d1f3a', 'en', 'Twin validator update', DEFAULT),
    ('f5b470ee-e522-4ac3-901f-640fa0a8df15', 'en', 'Twin validator update', DEFAULT),
    ('1eed9a60-5705-44b2-a627-60f58451b944', 'en', 'Twin validator delete', DEFAULT),
    ('522cbecd-a140-42bc-b26d-6e7d873b1e11', 'en', 'Twin validator delete', DEFAULT)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0060-000000000001', 'TWIN_VALIDATOR_MANAGE', '00000000-0000-0000-0005-000000000001', '04654276-6673-4122-b472-02dd413c4033', 'c9e64d9f-e651-43f9-a48a-b582da37ff5e'),
    ('00000000-0000-0004-0060-000000000002', 'TWIN_VALIDATOR_CREATE', '00000000-0000-0000-0005-000000000001', 'e607440a-abce-4443-bb05-714898cf345b', '5e26ceea-3528-4950-965f-508e1061ef69'),
    ('00000000-0000-0004-0060-000000000003', 'TWIN_VALIDATOR_VIEW', '00000000-0000-0000-0005-000000000001', '27a5a18a-3715-4a67-a5d5-2a86983ae757', '8f213f98-c218-4867-8263-8b3a86278e36'),
    ('00000000-0000-0004-0060-000000000004', 'TWIN_VALIDATOR_UPDATE', '00000000-0000-0000-0005-000000000001', 'aa140b0a-72b5-4e7a-a255-bf59e07d1f3a', 'f5b470ee-e522-4ac3-901f-640fa0a8df15'),
    ('00000000-0000-0004-0060-000000000005', 'TWIN_VALIDATOR_DELETE', '00000000-0000-0000-0005-000000000001', '1eed9a60-5705-44b2-a627-60f58451b944', '522cbecd-a140-42bc-b26d-6e7d873b1e11')
ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000344'::uuid, '00000000-0000-0004-0060-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000345'::uuid, '00000000-0000-0004-0060-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000346'::uuid, '00000000-0000-0004-0060-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000347'::uuid, '00000000-0000-0004-0060-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000348'::uuid, '00000000-0000-0004-0060-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
