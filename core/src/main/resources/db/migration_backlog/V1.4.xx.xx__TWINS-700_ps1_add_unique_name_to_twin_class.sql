ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS unique_name boolean NOT NULL DEFAULT false;
