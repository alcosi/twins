ALTER TABLE data_list ADD COLUMN IF NOT EXISTS created_by_user_id UUID;
CREATE INDEX IF NOT EXISTS idx_data_list_created_by_user_id ON data_list(created_by_user_id);
