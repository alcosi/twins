DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'twinflow'
                     AND column_name = 'initial_twin_factory_id') THEN
            ALTER TABLE twinflow
                RENAME COLUMN initial_twin_factory_id TO on_create_twin_factory_id;
        END IF;
    END
$$;


ALTER TABLE twinflow
    ADD COLUMN IF NOT EXISTS on_update_twin_factory_id uuid
        REFERENCES twin_factory
            ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE twinflow
    DROP CONSTRAINT IF EXISTS twinflow_initial_factory_id_fk;


DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_name = 'twinflow_on_create_twin_factory_id_fk') THEN
            ALTER TABLE twinflow
                ADD CONSTRAINT twinflow_on_create_twin_factory_id_fk
                    FOREIGN KEY (on_create_twin_factory_id)
                        REFERENCES twin_factory
                        ON UPDATE CASCADE ON DELETE RESTRICT;
        END IF;
    END
$$;
