-- USER_GROUP_INVOLVE_ACT_AS_USER permissions

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019d47ed-611b-7445-add5-fc8dedcbf249', 'User group involve act as user manage name', null, 'permissionName', null),
    ('019d47ed-611b-7664-84be-b29cd4a0050e', 'User group involve act as user manage description', null, 'permissionDescription', null),
    ('019d47ed-611b-7167-bc20-448ba7c7390a', 'User group involve act as user create name', null, 'permissionName', null),
    ('019d47ed-611b-754e-a55c-4b29d50d6149', 'User group involve act as user create description', null, 'permissionDescription', null),
    ('019d47ed-611b-7042-a59e-fe043d96e6b8', 'User group involve act as user view name', null, 'permissionName', null),
    ('019d47ed-611b-7db9-bf11-14bd2267a84f', 'User group involve act as user view description', null, 'permissionDescription', null),
    ('019d47ed-611b-7828-86e7-b9a4479300af', 'User group involve act as user update name', null, 'permissionName', null),
    ('019d47ed-611b-79a5-9bab-290e58e07de2', 'User group involve act as user update description', null, 'permissionDescription', null),
    ('019d47ed-611b-7100-821b-41c1cb9b0edb', 'User group involve act as user delete name', null, 'permissionName', null),
    ('019d47ed-611b-7f86-9ebf-8df7dc1e4487', 'User group involve act as user delete description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019d47ed-611b-7445-add5-fc8dedcbf249', 'en', 'User group involve act as user manage', DEFAULT),
    ('019d47ed-611b-7664-84be-b29cd4a0050e', 'en', 'User group involve act as user manage', DEFAULT),
    ('019d47ed-611b-7167-bc20-448ba7c7390a', 'en', 'User group involve act as user create', DEFAULT),
    ('019d47ed-611b-754e-a55c-4b29d50d6149', 'en', 'User group involve act as user create', DEFAULT),
    ('019d47ed-611b-7042-a59e-fe043d96e6b8', 'en', 'User group involve act as user view', DEFAULT),
    ('019d47ed-611b-7db9-bf11-14bd2267a84f', 'en', 'User group involve act as user view', DEFAULT),
    ('019d47ed-611b-7828-86e7-b9a4479300af', 'en', 'User group involve act as user update', DEFAULT),
    ('019d47ed-611b-79a5-9bab-290e58e07de2', 'en', 'User group involve act as user update', DEFAULT),
    ('019d47ed-611b-7100-821b-41c1cb9b0edb', 'en', 'User group involve act as user delete', DEFAULT),
    ('019d47ed-611b-7f86-9ebf-8df7dc1e4487', 'en', 'User group involve act as user delete', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0055-000000000001', 'USER_GROUP_INVOLVE_ACT_AS_USER_MANAGE', '00000000-0000-0000-0005-000000000001', '019d47ed-611b-7445-add5-fc8dedcbf249', '019d47ed-611b-7664-84be-b29cd4a0050e'),
    ('00000000-0000-0004-0055-000000000002', 'USER_GROUP_INVOLVE_ACT_AS_USER_CREATE', '00000000-0000-0000-0005-000000000001', '019d47ed-611b-7167-bc20-448ba7c7390a', '019d47ed-611b-754e-a55c-4b29d50d6149'),
    ('00000000-0000-0004-0055-000000000003', 'USER_GROUP_INVOLVE_ACT_AS_USER_VIEW', '00000000-0000-0000-0005-000000000001', '019d47ed-611b-7042-a59e-fe043d96e6b8', '019d47ed-611b-7db9-bf11-14bd2267a84f'),
    ('00000000-0000-0004-0055-000000000004', 'USER_GROUP_INVOLVE_ACT_AS_USER_UPDATE', '00000000-0000-0000-0005-000000000001', '019d47ed-611b-7828-86e7-b9a4479300af', '019d47ed-611b-79a5-9bab-290e58e07de2'),
    ('00000000-0000-0004-0055-000000000005', 'USER_GROUP_INVOLVE_ACT_AS_USER_DELETE', '00000000-0000-0000-0005-000000000001', '019d47ed-611b-7100-821b-41c1cb9b0edb', '019d47ed-611b-7f86-9ebf-8df7dc1e4487')
    ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000264'::uuid, '00000000-0000-0004-0055-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000265'::uuid, '00000000-0000-0004-0055-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000266'::uuid, '00000000-0000-0004-0055-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000267'::uuid, '00000000-0000-0004-0055-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000268'::uuid, '00000000-0000-0004-0055-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000269'::uuid, '00000000-0000-0004-0055-000000000001'::uuid, '00000000-0000-0000-0006-000000000003'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000270'::uuid, '00000000-0000-0004-0055-000000000003'::uuid, '00000000-0000-0000-0006-000000000003'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;