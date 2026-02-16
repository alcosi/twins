ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS unique_name boolean NOT NULL DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_twin_class_unique_name
    ON twin_class (unique_name);