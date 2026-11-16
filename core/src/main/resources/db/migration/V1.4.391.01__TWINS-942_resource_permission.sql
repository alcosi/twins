-- RESOURCE permissions (MCRUD): guard the standalone resource endpoints
-- (POST /private/resource/create/v1 -> RESOURCE_CREATE, GET/POST search,view -> RESOURCE_VIEW,
-- PUT /private/resource/v1 -> RESOURCE_UPDATE, DELETE /private/resource/{id}/v1 -> RESOURCE_DELETE).
insert into i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc101', 'Resource manage name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956701', 'Resource manage description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc102', 'Resource create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956702', 'Resource create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc103', 'Resource view name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956703', 'Resource view description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc104', 'Resource update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956704', 'Resource update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc105', 'Resource delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956705', 'Resource delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc101', 'en', 'Resource manage', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956701', 'en', 'Resource manage', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc102', 'en', 'Resource create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956702', 'en', 'Resource create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc103', 'en', 'Resource view', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956703', 'en', 'Resource view', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc104', 'en', 'Resource update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956704', 'en', 'Resource update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc105', 'en', 'Resource delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956705', 'en', 'Resource delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
    ('00000000-0000-0004-0062-000000000001', 'RESOURCE_MANAGE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc101', 'ef4d65eb-594d-3b17-b5d3-66ab79956701'),
    ('00000000-0000-0004-0062-000000000002', 'RESOURCE_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc102', 'ef4d65eb-594d-3b17-b5d3-66ab79956702'),
    ('00000000-0000-0004-0062-000000000003', 'RESOURCE_VIEW',   '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc103', 'ef4d65eb-594d-3b17-b5d3-66ab79956703'),
    ('00000000-0000-0004-0062-000000000004', 'RESOURCE_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc104', 'ef4d65eb-594d-3b17-b5d3-66ab79956704'),
    ('00000000-0000-0004-0062-000000000005', 'RESOURCE_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc105', 'ef4d65eb-594d-3b17-b5d3-66ab79956705')
on conflict (id) do update set key=excluded.key;
-- grants to Domain admin group
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000461'::uuid, '00000000-0000-0004-0062-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000462'::uuid, '00000000-0000-0004-0062-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000463'::uuid, '00000000-0000-0004-0062-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000464'::uuid, '00000000-0000-0004-0062-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000465'::uuid, '00000000-0000-0004-0062-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
