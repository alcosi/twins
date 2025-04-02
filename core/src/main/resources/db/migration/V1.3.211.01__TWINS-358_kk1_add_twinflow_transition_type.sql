CREATE TABLE IF NOT EXISTS twinflow_transition_type (id VARCHAR(40) PRIMARY KEY, description TEXT);

INSERT INTO twinflow_transition_type (id, description) VALUES ('STATUS_CHANGE', 'status change'), ('OPERATION', 'operation') ON CONFLICT (id) DO NOTHING;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'twinflow_transition'
        AND column_name = 'twinflow_transition_type_id'
    ) THEN
        EXECUTE 'ALTER TABLE twinflow_transition
                ADD COLUMN twinflow_transition_type_id VARCHAR(40) NOT NULL
                DEFAULT ''STATUS_CHANGE''';
END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'twinflow_transition'
        AND constraint_name = 'fk_twinflow_transition_type'
    ) THEN
        EXECUTE 'ALTER TABLE twinflow_transition
                ADD CONSTRAINT fk_twinflow_transition_type
                FOREIGN KEY (twinflow_transition_type_id)
                REFERENCES twinflow_transition_type(id)
                ON UPDATE CASCADE
                ON DELETE RESTRICT';
END IF;
END
$$;
