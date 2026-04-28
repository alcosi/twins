-- Add audit fields to twin_validator table
ALTER TABLE twin_validator
    ADD COLUMN IF NOT EXISTS created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_by_user_id uuid;

CREATE INDEX IF NOT EXISTS twin_validator_created_by_user_id_index
    ON twin_validator (created_by_user_id);
