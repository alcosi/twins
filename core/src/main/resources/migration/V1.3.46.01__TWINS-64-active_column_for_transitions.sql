ALTER TABLE twinflow_transition_validator ADD COLUMN if not exists active BOOLEAN DEFAULT TRUE;
ALTER TABLE twinflow_transition_trigger ADD COLUMN if not exists active BOOLEAN DEFAULT TRUE;
