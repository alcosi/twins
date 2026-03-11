-- Add cachable field to twin_factory_condition_set
ALTER TABLE twin_factory_condition_set ADD COLUMN IF NOT EXISTS cachable boolean DEFAULT true NOT NULL;
