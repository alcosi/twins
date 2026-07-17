-- TWINS-875: dedicated TWIN_POINTER_* permissions for the twin_pointer settings CRUD API.
-- Permission ids use the new 0058 range (NotificationSchema = 0057 was the last used holder).
-- Mirrors the notification_schema permission seed migration (V1.4.264.01).

-- TWIN POINTER permission i18n (name + description per action)
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('4604540d-4fda-4757-b165-687cd05534b7', 'Twin pointer manage name', null, 'permissionName', null),
    ('1738f500-1995-4473-91fb-f499d5a47001', 'Twin pointer manage description', null, 'permissionDescription', null),
    ('d0df590a-99f8-4544-ba63-f7c3d4edd599', 'Twin pointer create name', null, 'permissionName', null),
    ('ba1c3367-7f08-4b47-bd87-1d45de2d18b3', 'Twin pointer create description', null, 'permissionDescription', null),
    ('bb3643a8-de14-4864-b0b7-d54ad7b9a1d2', 'Twin pointer view name', null, 'permissionName', null),
    ('1ad040e7-82b1-4540-8988-b3baeb4e2ac9', 'Twin pointer view description', null, 'permissionDescription', null),
    ('574dd267-b23a-4463-be9c-6dbf1f8d958f', 'Twin pointer update name', null, 'permissionName', null),
    ('e16e2aea-fc5f-4cec-aa9b-afa00de45efa', 'Twin pointer update description', null, 'permissionDescription', null),
    ('0198579f-1b21-4d95-b494-dd9c5a2b3348', 'Twin pointer delete name', null, 'permissionName', null),
    ('8e245991-69c9-4748-b6be-084b2ed5321f', 'Twin pointer delete description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('4604540d-4fda-4757-b165-687cd05534b7', 'en', 'Twin pointer manage', DEFAULT),
    ('1738f500-1995-4473-91fb-f499d5a47001', 'en', 'Twin pointer manage', DEFAULT),
    ('d0df590a-99f8-4544-ba63-f7c3d4edd599', 'en', 'Twin pointer create', DEFAULT),
    ('ba1c3367-7f08-4b47-bd87-1d45de2d18b3', 'en', 'Twin pointer create', DEFAULT),
    ('bb3643a8-de14-4864-b0b7-d54ad7b9a1d2', 'en', 'Twin pointer view', DEFAULT),
    ('1ad040e7-82b1-4540-8988-b3baeb4e2ac9', 'en', 'Twin pointer view', DEFAULT),
    ('574dd267-b23a-4463-be9c-6dbf1f8d958f', 'en', 'Twin pointer update', DEFAULT),
    ('e16e2aea-fc5f-4cec-aa9b-afa00de45efa', 'en', 'Twin pointer update', DEFAULT),
    ('0198579f-1b21-4d95-b494-dd9c5a2b3348', 'en', 'Twin pointer delete', DEFAULT),
    ('8e245991-69c9-4748-b6be-084b2ed5321f', 'en', 'Twin pointer delete', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0058-000000000001', 'TWIN_POINTER_MANAGE', '00000000-0000-0000-0005-000000000001', '4604540d-4fda-4757-b165-687cd05534b7', '1738f500-1995-4473-91fb-f499d5a47001'),
    ('00000000-0000-0004-0058-000000000002', 'TWIN_POINTER_CREATE', '00000000-0000-0000-0005-000000000001', 'd0df590a-99f8-4544-ba63-f7c3d4edd599', 'ba1c3367-7f08-4b47-bd87-1d45de2d18b3'),
    ('00000000-0000-0004-0058-000000000003', 'TWIN_POINTER_VIEW',   '00000000-0000-0000-0005-000000000001', 'bb3643a8-de14-4864-b0b7-d54ad7b9a1d2', '1ad040e7-82b1-4540-8988-b3baeb4e2ac9'),
    ('00000000-0000-0004-0058-000000000004', 'TWIN_POINTER_UPDATE', '00000000-0000-0000-0005-000000000001', '574dd267-b23a-4463-be9c-6dbf1f8d958f', 'e16e2aea-fc5f-4cec-aa9b-afa00de45efa'),
    ('00000000-0000-0004-0058-000000000005', 'TWIN_POINTER_DELETE', '00000000-0000-0000-0005-000000000001', '0198579f-1b21-4d95-b494-dd9c5a2b3348', '8e245991-69c9-4748-b6be-084b2ed5321f')
    ON CONFLICT DO NOTHING;

-- Grant all 5 TWIN_POINTER permissions to the DOMAIN_ADMIN user group
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('463aa07c-1671-4052-b3de-a834f23ed627', '00000000-0000-0004-0058-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('3631d570-5d2c-43c3-927d-bbbbb1af2fa7', '00000000-0000-0004-0058-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('e0a703c0-24e5-4ca6-a4ce-b904ff89e6cc', '00000000-0000-0004-0058-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('b4752b0e-cdd9-40e1-99f2-0e78673d749f', '00000000-0000-0004-0058-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('b35b0326-f136-44d1-ae98-dce239d92911', '00000000-0000-0004-0058-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
