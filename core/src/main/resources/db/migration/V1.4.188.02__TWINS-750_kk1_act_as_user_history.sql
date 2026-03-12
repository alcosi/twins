-- Add machine_user_id column to history table
ALTER TABLE history ADD COLUMN IF NOT EXISTS machine_user_id UUID REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Add index for machine_user_id
CREATE INDEX IF NOT EXISTS idx_history_machine_user_id ON history(machine_user_id);

-- Add i18n for HISTORY_MACHINE_USER_VIEW permission
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019c6acb-0001-0001-0000-000000000001', 'History machine user view name', null, 'permissionName', null),
    ('019c6acb-0001-0001-0000-000000000002', 'History machine user view description', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019c6acb-0001-0001-0000-000000000001', 'en', 'History machine user view', DEFAULT),
    ('019c6acb-0001-0001-0000-000000000002', 'en', 'History machine user view', DEFAULT)
ON CONFLICT DO NOTHING;

-- Add HISTORY_MACHINE_USER_VIEW permission
INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0044-000000000006', 'HISTORY_MACHINE_USER_VIEW', '00000000-0000-0000-0005-000000000001', '019c6acb-0001-0001-0000-000000000001', '019c6acb-0001-0001-0000-000000000002')
ON CONFLICT DO NOTHING;

-- Grant HISTORY_MACHINE_USER_VIEW permission to system admins
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000207'::uuid, '00000000-0000-0004-0044-000000000006'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT)
    on conflict do nothing;
