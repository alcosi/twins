DO $$
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name='twinflow' AND column_name='initial_twin_factory_id'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name='twinflow' AND column_name='on_create_twin_factory_id'
        ) THEN
            ALTER TABLE twinflow
                RENAME COLUMN initial_twin_factory_id TO on_create_twin_factory_id;
        END IF;
    END $$;


DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name='twinflow' AND column_name='on_update_twin_factory_id'
        ) THEN
            ALTER TABLE twinflow
                ADD COLUMN on_update_twin_factory_id uuid;
        END IF;
    END $$;


DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_name='twinflow_on_update_twin_factory_id_fk'
        ) THEN
            ALTER TABLE twinflow
                ADD CONSTRAINT twinflow_on_update_twin_factory_id_fk
                    FOREIGN KEY (on_update_twin_factory_id)
                        REFERENCES twin_factory
                        ON UPDATE CASCADE ON DELETE RESTRICT;
        END IF;
    END $$;


DO $$
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_name='twinflow_initial_factory_id_fk'
        ) THEN
            ALTER TABLE twinflow
                DROP CONSTRAINT twinflow_initial_factory_id_fk;
        END IF;
    END $$;


DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_name='twinflow_on_create_twin_factory_id_fk'
        ) THEN
            ALTER TABLE twinflow
                ADD CONSTRAINT twinflow_on_create_twin_factory_id_fk
                    FOREIGN KEY (on_create_twin_factory_id)
                        REFERENCES twin_factory
                        ON UPDATE CASCADE ON DELETE RESTRICT;
        END IF;
    END $$;
