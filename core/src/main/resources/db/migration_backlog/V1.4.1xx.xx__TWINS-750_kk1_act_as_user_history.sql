-- Add machine_user_id column to history table
ALTER TABLE history ADD COLUMN IF NOT EXISTS machine_user_id UUID REFERENCES user(id) ON DELETE SET NULL;

-- Add index for machine_user_id
CREATE INDEX IF NOT EXISTS idx_history_machine_user_id ON history(machine_user_id);
