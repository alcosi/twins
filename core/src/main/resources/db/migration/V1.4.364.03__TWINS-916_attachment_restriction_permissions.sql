-- ATTACHMENT RESTRICTION permissions
-- TODO: replace TWINS-XXX in the filename with the actual ticket number.

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('00000000-0000-0000-0012-0000000011c8', 'Attachment restriction manage name', null, 'permissionName', null),
    ('00000000-0000-0000-0012-0000000011c9', 'Attachment restriction manage description', null, 'permissionDescription', null),
    ('00000000-0000-0000-0012-0000000011ca', 'Attachment restriction create name', null, 'permissionName', null),
    ('00000000-0000-0000-0012-0000000011cb', 'Attachment restriction create description', null, 'permissionDescription', null),
    ('00000000-0000-0000-0012-0000000011cc', 'Attachment restriction view name', null, 'permissionName', null),
    ('00000000-0000-0000-0012-0000000011cd', 'Attachment restriction view description', null, 'permissionDescription', null),
    ('00000000-0000-0000-0012-0000000011ce', 'Attachment restriction update name', null, 'permissionName', null),
    ('00000000-0000-0000-0012-0000000011cf', 'Attachment restriction update description', null, 'permissionDescription', null),
    ('00000000-0000-0000-0012-0000000011d0', 'Attachment restriction delete name', null, 'permissionName', null),
    ('00000000-0000-0000-0012-0000000011d1', 'Attachment restriction delete description', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('00000000-0000-0000-0012-0000000011c8', 'en', 'Attachment restriction manage', DEFAULT),
    ('00000000-0000-0000-0012-0000000011c9', 'en', 'Attachment restriction manage', DEFAULT),
    ('00000000-0000-0000-0012-0000000011ca', 'en', 'Attachment restriction create', DEFAULT),
    ('00000000-0000-0000-0012-0000000011cb', 'en', 'Attachment restriction create', DEFAULT),
    ('00000000-0000-0000-0012-0000000011cc', 'en', 'Attachment restriction view', DEFAULT),
    ('00000000-0000-0000-0012-0000000011cd', 'en', 'Attachment restriction view', DEFAULT),
    ('00000000-0000-0000-0012-0000000011ce', 'en', 'Attachment restriction update', DEFAULT),
    ('00000000-0000-0000-0012-0000000011cf', 'en', 'Attachment restriction update', DEFAULT),
    ('00000000-0000-0000-0012-0000000011d0', 'en', 'Attachment restriction delete', DEFAULT),
    ('00000000-0000-0000-0012-0000000011d1', 'en', 'Attachment restriction delete', DEFAULT)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0061-000000000001', 'ATTACHMENT_RESTRICTION_MANAGE', '00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0012-0000000011c8', '00000000-0000-0000-0012-0000000011c9'),
    ('00000000-0000-0004-0061-000000000002', 'ATTACHMENT_RESTRICTION_CREATE', '00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0012-0000000011ca', '00000000-0000-0000-0012-0000000011cb'),
    ('00000000-0000-0004-0061-000000000003', 'ATTACHMENT_RESTRICTION_VIEW', '00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0012-0000000011cc', '00000000-0000-0000-0012-0000000011cd'),
    ('00000000-0000-0004-0061-000000000004', 'ATTACHMENT_RESTRICTION_UPDATE', '00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0012-0000000011ce', '00000000-0000-0000-0012-0000000011cf'),
    ('00000000-0000-0004-0061-000000000005', 'ATTACHMENT_RESTRICTION_DELETE', '00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0012-0000000011d0', '00000000-0000-0000-0012-0000000011d1')
ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000356'::uuid, '00000000-0000-0004-0061-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000357'::uuid, '00000000-0000-0004-0061-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000358'::uuid, '00000000-0000-0004-0061-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000359'::uuid, '00000000-0000-0004-0061-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000360'::uuid, '00000000-0000-0004-0061-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
