-- Dedicated TWIN_LINK_* permissions for the twin_link view/search/count API.
-- Permission ids use the 0059 range (TwinPointer = 0058 was the last used holder).
-- Mirrors the twin_pointer permission seed migration (V1.4.323.01).

-- TWIN LINK permission i18n (name + description per action)
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('7a23ae71-1281-4f5d-a264-786503fb7a93', 'Twin link manage name', null, 'permissionName', null),
    ('92a35008-bb46-4f5a-bae4-2429efd905de', 'Twin link manage description', null, 'permissionDescription', null),
    ('0520e3be-4ffd-4331-be02-081e03666b83', 'Twin link create name', null, 'permissionName', null),
    ('c5584ed6-5987-4981-b867-edb2ecbb4d6d', 'Twin link create description', null, 'permissionDescription', null),
    ('a629bd3f-b366-4087-bc3c-12de966ad6ef', 'Twin link view name', null, 'permissionName', null),
    ('b0509430-1f10-4831-8cf1-a60bab3fe0e6', 'Twin link view description', null, 'permissionDescription', null),
    ('610c2509-a111-491b-b8a1-ca63e2058310', 'Twin link update name', null, 'permissionName', null),
    ('c217a331-7e04-413a-9781-c18453a08907', 'Twin link update description', null, 'permissionDescription', null),
    ('f0f19025-e1b0-4ca0-a3c5-bbdaf14abbd1', 'Twin link delete name', null, 'permissionName', null),
    ('f70bdadc-b8ac-4d4b-a116-52549d40ddbe', 'Twin link delete description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('7a23ae71-1281-4f5d-a264-786503fb7a93', 'en', 'Twin link manage', DEFAULT),
    ('92a35008-bb46-4f5a-bae4-2429efd905de', 'en', 'Twin link manage', DEFAULT),
    ('0520e3be-4ffd-4331-be02-081e03666b83', 'en', 'Twin link create', DEFAULT),
    ('c5584ed6-5987-4981-b867-edb2ecbb4d6d', 'en', 'Twin link create', DEFAULT),
    ('a629bd3f-b366-4087-bc3c-12de966ad6ef', 'en', 'Twin link view', DEFAULT),
    ('b0509430-1f10-4831-8cf1-a60bab3fe0e6', 'en', 'Twin link view', DEFAULT),
    ('610c2509-a111-491b-b8a1-ca63e2058310', 'en', 'Twin link update', DEFAULT),
    ('c217a331-7e04-413a-9781-c18453a08907', 'en', 'Twin link update', DEFAULT),
    ('f0f19025-e1b0-4ca0-a3c5-bbdaf14abbd1', 'en', 'Twin link delete', DEFAULT),
    ('f70bdadc-b8ac-4d4b-a116-52549d40ddbe', 'en', 'Twin link delete', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0059-000000000001', 'TWIN_LINK_MANAGE', '00000000-0000-0000-0005-000000000001', '7a23ae71-1281-4f5d-a264-786503fb7a93', '92a35008-bb46-4f5a-bae4-2429efd905de'),
    ('00000000-0000-0004-0059-000000000002', 'TWIN_LINK_CREATE', '00000000-0000-0000-0005-000000000001', '0520e3be-4ffd-4331-be02-081e03666b83', 'c5584ed6-5987-4981-b867-edb2ecbb4d6d'),
    ('00000000-0000-0004-0059-000000000003', 'TWIN_LINK_VIEW',   '00000000-0000-0000-0005-000000000001', 'a629bd3f-b366-4087-bc3c-12de966ad6ef', 'b0509430-1f10-4831-8cf1-a60bab3fe0e6'),
    ('00000000-0000-0004-0059-000000000004', 'TWIN_LINK_UPDATE', '00000000-0000-0000-0005-000000000001', '610c2509-a111-491b-b8a1-ca63e2058310', 'c217a331-7e04-413a-9781-c18453a08907'),
    ('00000000-0000-0004-0059-000000000005', 'TWIN_LINK_DELETE', '00000000-0000-0000-0005-000000000001', 'f0f19025-e1b0-4ca0-a3c5-bbdaf14abbd1', 'f70bdadc-b8ac-4d4b-a116-52549d40ddbe')
    ON CONFLICT DO NOTHING;

-- Grant all 5 TWIN_LINK permissions to the DOMAIN_ADMIN user group
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('8d6880e9-d5da-4dfe-833c-45075a5d8f65', '00000000-0000-0004-0059-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('8973bc4c-2a9f-44d5-a327-3f77493c2abf', '00000000-0000-0004-0059-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('9b05a554-2315-4439-a8a8-59c609d6606c', '00000000-0000-0004-0059-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('32d6e8ae-9aeb-4edf-85a6-2a86fd79926c', '00000000-0000-0004-0059-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('491725b9-c435-4c1c-8c9b-eb1862dcc515', '00000000-0000-0004-0059-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
