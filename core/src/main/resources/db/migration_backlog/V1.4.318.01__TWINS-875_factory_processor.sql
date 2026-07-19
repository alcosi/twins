-- TWINS-875: pluggable FactoryProcessor featurer on twin_factory
-- The factory_processor_featurer_id points to a FactoryProcessor featurer that drives a single
-- factory run (multipliers, pipelines, branches, erasers, triggers). factory_processor_params holds
-- its hstore params. When null, FactoryExecutionService falls back to the default db-driven processor.

ALTER TABLE twin_factory ADD COLUMN IF NOT EXISTS factory_processor_featurer_id integer;
ALTER TABLE twin_factory ADD COLUMN IF NOT EXISTS factory_processor_params hstore;

-- FactoryProcessor featurer type + default db-driven implementation
INSERT INTO featurer_type (id, name, description)
VALUES (54, 'FactoryProcessor', 'Processes a single factory run (multipliers, pipelines, branches, erasers, triggers)')
ON CONFLICT (id) DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5401::integer, 54::integer, '', '', '', DEFAULT)
ON CONFLICT DO NOTHING;

-- Backfill existing factories with the default db-driven processor
UPDATE twin_factory
SET factory_processor_featurer_id = 5401
WHERE factory_processor_featurer_id IS NULL;

ALTER TABLE twin_factory ALTER COLUMN factory_processor_featurer_id SET NOT NULL;

-- Index for the featurer reference column
CREATE INDEX IF NOT EXISTS twin_factory_factory_processor_featurer_id_idx
    ON twin_factory (factory_processor_featurer_id);
