-- Add machine_user_id column to history table
ALTER TABLE history ADD COLUMN IF NOT EXISTS machine_user_id UUID REFERENCES user(id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Add index for machine_user_id
CREATE INDEX IF NOT EXISTS idx_history_machine_user_id ON history(machine_user_id);


-- Grant HISTORY_MACHINE_USER_VIEW permission to system admins
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000207'::uuid, '00000000-0000-0004-0044-000000000006'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT)
    on conflict do nothing;
