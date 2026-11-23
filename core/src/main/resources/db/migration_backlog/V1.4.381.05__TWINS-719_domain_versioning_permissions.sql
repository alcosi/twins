-- TWINS-719: dedicated DOMAIN_VERSION_* / DOMAIN_VERSION_AUDIT_* permissions for the
-- domain versioning API (search/count controllers of domain_version and domain_version_audit).
-- Permission ids use the new 0062 (version) and 0063 (audit) ranges
-- (AttachmentRestriction = 0061 was the last used holder).
-- Audit is trigger-written, read-only data — no CREATE/UPDATE/DELETE permissions for it.
-- Mirrors the twin_pointer permission seed migration (V1.4.323.01).

-- DOMAIN VERSION permission i18n (name + description per action)
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('f5b9d69c-31a1-47de-8135-d84372112314', 'Domain version manage name', null, 'permissionName', null),
    ('6716c26e-f0e5-46a0-bcfa-2209763efff5', 'Domain version manage description', null, 'permissionDescription', null),
    ('a6adf0a1-01e0-4657-9aee-83263a1f95a9', 'Domain version create name', null, 'permissionName', null),
    ('a6bf1c0b-8e53-43f5-a434-9250bb2ed016', 'Domain version create description', null, 'permissionDescription', null),
    ('7a8fe03f-e9a0-4302-bffc-4ad27dda3aa7', 'Domain version view name', null, 'permissionName', null),
    ('e584dafe-022e-4c3d-ba9c-f2c8e243e478', 'Domain version view description', null, 'permissionDescription', null),
    ('5324ce97-f70c-4ffd-98ba-e10df30b8192', 'Domain version update name', null, 'permissionName', null),
    ('07c6a757-560a-45e9-bb34-5c517c6dc695', 'Domain version update description', null, 'permissionDescription', null),
    ('bee750a5-11a1-455e-963f-979d02f3af48', 'Domain version delete name', null, 'permissionName', null),
    ('d4f8aa4d-8e97-4d91-8941-61ae7fdc62cd', 'Domain version delete description', null, 'permissionDescription', null),
    ('4a8f00d0-262e-4554-b0c0-93978a991f9f', 'Domain version audit manage name', null, 'permissionName', null),
    ('b8a988c5-2906-4aa4-a39d-d17e9dfed41f', 'Domain version audit manage description', null, 'permissionDescription', null),
    ('9287c393-d338-412a-a3a5-9a0b5f57bb40', 'Domain version audit view name', null, 'permissionName', null),
    ('2a45c1f7-8a61-4548-8cf0-9ea386bfbff1', 'Domain version audit view description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('f5b9d69c-31a1-47de-8135-d84372112314', 'en', 'Domain version manage', DEFAULT),
    ('6716c26e-f0e5-46a0-bcfa-2209763efff5', 'en', 'Domain version manage', DEFAULT),
    ('a6adf0a1-01e0-4657-9aee-83263a1f95a9', 'en', 'Domain version create', DEFAULT),
    ('a6bf1c0b-8e53-43f5-a434-9250bb2ed016', 'en', 'Domain version create', DEFAULT),
    ('7a8fe03f-e9a0-4302-bffc-4ad27dda3aa7', 'en', 'Domain version view', DEFAULT),
    ('e584dafe-022e-4c3d-ba9c-f2c8e243e478', 'en', 'Domain version view', DEFAULT),
    ('5324ce97-f70c-4ffd-98ba-e10df30b8192', 'en', 'Domain version update', DEFAULT),
    ('07c6a757-560a-45e9-bb34-5c517c6dc695', 'en', 'Domain version update', DEFAULT),
    ('bee750a5-11a1-455e-963f-979d02f3af48', 'en', 'Domain version delete', DEFAULT),
    ('d4f8aa4d-8e97-4d91-8941-61ae7fdc62cd', 'en', 'Domain version delete', DEFAULT),
    ('4a8f00d0-262e-4554-b0c0-93978a991f9f', 'en', 'Domain version audit manage', DEFAULT),
    ('b8a988c5-2906-4aa4-a39d-d17e9dfed41f', 'en', 'Domain version audit manage', DEFAULT),
    ('9287c393-d338-412a-a3a5-9a0b5f57bb40', 'en', 'Domain version audit view', DEFAULT),
    ('2a45c1f7-8a61-4548-8cf0-9ea386bfbff1', 'en', 'Domain version audit view', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0062-000000000001', 'DOMAIN_VERSION_MANAGE', '00000000-0000-0000-0005-000000000001', 'f5b9d69c-31a1-47de-8135-d84372112314', '6716c26e-f0e5-46a0-bcfa-2209763efff5'),
    ('00000000-0000-0004-0062-000000000002', 'DOMAIN_VERSION_CREATE', '00000000-0000-0000-0005-000000000001', 'a6adf0a1-01e0-4657-9aee-83263a1f95a9', 'a6bf1c0b-8e53-43f5-a434-9250bb2ed016'),
    ('00000000-0000-0004-0062-000000000003', 'DOMAIN_VERSION_VIEW',   '00000000-0000-0000-0005-000000000001', '7a8fe03f-e9a0-4302-bffc-4ad27dda3aa7', 'e584dafe-022e-4c3d-ba9c-f2c8e243e478'),
    ('00000000-0000-0004-0062-000000000004', 'DOMAIN_VERSION_UPDATE', '00000000-0000-0000-0005-000000000001', '5324ce97-f70c-4ffd-98ba-e10df30b8192', '07c6a757-560a-45e9-bb34-5c517c6dc695'),
    ('00000000-0000-0004-0062-000000000005', 'DOMAIN_VERSION_DELETE', '00000000-0000-0000-0005-000000000001', 'bee750a5-11a1-455e-963f-979d02f3af48', 'd4f8aa4d-8e97-4d91-8941-61ae7fdc62cd'),
    ('00000000-0000-0004-0063-000000000001', 'DOMAIN_VERSION_AUDIT_MANAGE', '00000000-0000-0000-0005-000000000001', '4a8f00d0-262e-4554-b0c0-93978a991f9f', 'b8a988c5-2906-4aa4-a39d-d17e9dfed41f'),
    ('00000000-0000-0004-0063-000000000003', 'DOMAIN_VERSION_AUDIT_VIEW',   '00000000-0000-0000-0005-000000000001', '9287c393-d338-412a-a3a5-9a0b5f57bb40', '2a45c1f7-8a61-4548-8cf0-9ea386bfbff1')
    ON CONFLICT DO NOTHING;

-- Grant all DOMAIN_VERSION / DOMAIN_VERSION_AUDIT permissions to the DOMAIN_ADMIN user group
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('f7024522-ab69-41ff-b2d4-d97f26f7ade7', '00000000-0000-0004-0062-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('2af408a3-7d04-4cfd-ae3e-62e219549243', '00000000-0000-0004-0062-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('89223f02-2f69-4a41-a107-115b828984f8', '00000000-0000-0004-0062-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('148a1bb9-b318-42ec-aa1e-4d599680d1b4', '00000000-0000-0004-0062-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('043dcb39-85d7-4677-873b-2216847ada60', '00000000-0000-0004-0062-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('7ae825ea-3e43-4867-b4f2-d95d15cd9e7d', '00000000-0000-0004-0063-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('feb3647e-17cb-434f-b949-e1b36ce07a81', '00000000-0000-0004-0063-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) ON CONFLICT DO NOTHING;
