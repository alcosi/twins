ALTER TABLE featurer
    ADD COLUMN IF NOT EXISTS deprecated BOOLEAN DEFAULT FALSE NOT NULL;