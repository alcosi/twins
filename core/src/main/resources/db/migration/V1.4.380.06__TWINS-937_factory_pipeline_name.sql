ALTER TABLE twin_factory_pipeline
    ADD COLUMN IF NOT EXISTS name VARCHAR(255);

ALTER TABLE twin_factory_pipeline_step
    ADD COLUMN IF NOT EXISTS name VARCHAR(255);
