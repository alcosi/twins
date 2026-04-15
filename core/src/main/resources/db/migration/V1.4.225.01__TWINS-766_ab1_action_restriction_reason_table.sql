-- Add i18n type for action restriction reasons
INSERT INTO i18n_type (id, name) VALUES ('actionRestrictionReasonDescription', 'Action restriction description') ON CONFLICT (id) DO NOTHING;
-- Create i18n entry for RESTRICTED_BY_PERMISSION
INSERT INTO i18n (id, name, "key", i18n_type_id) VALUES ('019d3fc2-4683-7351-b95e-dbcdccd99372', 'Action restricted by permission description', null, 'actionRestrictionReasonDescription') ON CONFLICT (id) DO NOTHING;
-- Create i18n entry for RESTRICTED_BY_VALIDATOR
INSERT INTO i18n (id, name, "key", i18n_type_id) VALUES ('019d404b-dc5b-79bc-86d0-e3b5136c4f51', 'Action restricted by validator description', null, 'actionRestrictionReasonDescription') ON CONFLICT (id) DO NOTHING;
-- Add translations for RESTRICTED_BY_PERMISSION
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d3fc2-4683-7351-b95e-dbcdccd99372', 'en', 'Action restricted by permission', DEFAULT) ON CONFLICT DO NOTHING;
-- Add translations for RESTRICTED_BY_VALIDATOR
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('019d404b-dc5b-79bc-86d0-e3b5136c4f51', 'en', 'Action restricted by validator', DEFAULT) ON CONFLICT DO NOTHING;

-- Action restriction reason table
CREATE TABLE IF NOT EXISTS action_restriction_reason
(
    id                    UUID PRIMARY KEY,
    domain_id             UUID       NULL REFERENCES domain (id) ON UPDATE CASCADE ON DELETE CASCADE,
    type                  VARCHAR    NOT NULL,
    description_i18n_id   UUID       NOT NULL REFERENCES i18n (id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Insert RESTRICTED_BY_PERMISSION action restriction reason
INSERT INTO action_restriction_reason (id, domain_id, type, description_i18n_id) VALUES ('00000000-0000-0000-0000-000000000001', NULL, 'RESTRICTED_BY_PERMISSION', '019d3fc2-4683-7351-b95e-dbcdccd99372') ON CONFLICT (id) DO NOTHING;
-- Insert RESTRICTED_BY_VALIDATOR action restriction reason
INSERT INTO action_restriction_reason (id, domain_id, type, description_i18n_id) VALUES ('00000000-0000-0000-0000-000000000002', NULL, 'RESTRICTED_BY_VALIDATOR', '019d404b-dc5b-79bc-86d0-e3b5136c4f51') ON CONFLICT (id) DO NOTHING;

-- Add action_restriction_reason_id to twin_action_permission
ALTER TABLE twin_action_permission ADD COLUMN IF NOT EXISTS action_restriction_reason_id UUID REFERENCES action_restriction_reason (id) ON UPDATE CASCADE ON DELETE CASCADE;
UPDATE twin_action_permission SET action_restriction_reason_id = '00000000-0000-0000-0000-000000000001' WHERE action_restriction_reason_id IS NULL;
ALTER TABLE twin_action_permission ALTER COLUMN action_restriction_reason_id SET NOT NULL;

-- Add action_restriction_reason_id to twin_action_validator_rule
ALTER TABLE twin_action_validator_rule ADD COLUMN IF NOT EXISTS action_restriction_reason_id UUID REFERENCES action_restriction_reason (id) ON UPDATE CASCADE ON DELETE CASCADE;
UPDATE twin_action_validator_rule SET action_restriction_reason_id = '00000000-0000-0000-0000-000000000002' WHERE action_restriction_reason_id IS NULL;
ALTER TABLE twin_action_validator_rule ALTER COLUMN action_restriction_reason_id SET NOT NULL;

INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019d621d-d09e-712b-8e61-984e8fc77b3a', 'Action restriction reason manage name', null, 'permissionName', null),
    ('019d621e-0014-7580-8a79-71284326c23f', 'Action restriction reason manage description', null, 'permissionDescription', null),
    ('019d621e-28cf-732c-a4aa-fd8899f98356', 'Action restriction reason create name', null, 'permissionName', null),
    ('019d621e-4e3e-72c1-bf6c-b162a592d444', 'Action restriction reason create description', null, 'permissionDescription', null),
    ('019d621e-7055-755f-892a-2d2906902062', 'Action restriction reason view name', null, 'permissionName', null),
    ('019d621e-9339-75e6-a260-f281c9d9d85b', 'Action restriction reason view description', null, 'permissionDescription', null),
    ('019d621e-b999-79e7-86a4-fb23107dd150', 'Action restriction reason update name', null, 'permissionName', null),
    ('019d621e-e01e-70c0-8f70-88a1bc7294c2', 'Action restriction reason update description', null, 'permissionDescription', null),
    ('019d621f-1068-71f8-8bec-5d0c95e72a78', 'Action restriction reason delete name', null, 'permissionName', null),
    ('019d621f-4c98-714c-a202-605d68b63d9f', 'Action restriction reason delete description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019d621d-d09e-712b-8e61-984e8fc77b3a', 'en', 'Action restriction reason manage', DEFAULT),
    ('019d621e-0014-7580-8a79-71284326c23f', 'en', 'Action restriction reason manage', DEFAULT),
    ('019d621e-28cf-732c-a4aa-fd8899f98356', 'en', 'Action restriction reason create', DEFAULT),
    ('019d621e-4e3e-72c1-bf6c-b162a592d444', 'en', 'Action restriction reason create', DEFAULT),
    ('019d621e-7055-755f-892a-2d2906902062', 'en', 'Action restriction reason view', DEFAULT),
    ('019d621e-9339-75e6-a260-f281c9d9d85b', 'en', 'Action restriction reason view', DEFAULT),
    ('019d621e-b999-79e7-86a4-fb23107dd150', 'en', 'Action restriction reason update', DEFAULT),
    ('019d621e-e01e-70c0-8f70-88a1bc7294c2', 'en', 'Action restriction reason update', DEFAULT),
    ('019d621f-1068-71f8-8bec-5d0c95e72a78', 'en', 'Action restriction reason delete', DEFAULT),
    ('019d621f-4c98-714c-a202-605d68b63d9f', 'en', 'Action restriction reason delete', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0056-000000000001', 'ACTION_RESTRICTION_REASON_MANAGE', '00000000-0000-0000-0005-000000000001', '019d621d-d09e-712b-8e61-984e8fc77b3a', '019d621e-0014-7580-8a79-71284326c23f'),
    ('00000000-0000-0004-0056-000000000002', 'ACTION_RESTRICTION_REASON_CREATE', '00000000-0000-0000-0005-000000000001', '019d621e-28cf-732c-a4aa-fd8899f98356', '019d621e-4e3e-72c1-bf6c-b162a592d444'),
    ('00000000-0000-0004-0056-000000000003', 'ACTION_RESTRICTION_REASON_VIEW', '00000000-0000-0000-0005-000000000001', '019d621e-7055-755f-892a-2d2906902062', '019d621e-9339-75e6-a260-f281c9d9d85b'),
    ('00000000-0000-0004-0056-000000000004', 'ACTION_RESTRICTION_REASON_UPDATE', '00000000-0000-0000-0005-000000000001', '019d621e-b999-79e7-86a4-fb23107dd150', '019d621e-e01e-70c0-8f70-88a1bc7294c2'),
    ('00000000-0000-0004-0056-000000000005', 'ACTION_RESTRICTION_REASON_DELETE', '00000000-0000-0000-0005-000000000001', '019d621f-1068-71f8-8bec-5d0c95e72a78', '019d621f-4c98-714c-a202-605d68b63d9f')
    ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000271'::uuid, '00000000-0000-0004-0056-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000272'::uuid, '00000000-0000-0004-0056-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000273'::uuid, '00000000-0000-0004-0056-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000274'::uuid, '00000000-0000-0004-0056-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000275'::uuid, '00000000-0000-0004-0056-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000276'::uuid, '00000000-0000-0004-0056-000000000001'::uuid, '00000000-0000-0000-0006-000000000003'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000277'::uuid, '00000000-0000-0004-0056-000000000003'::uuid, '00000000-0000-0000-0006-000000000003'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;