alter table domain
    drop constraint if exists twinflow_on_update_twin_factory_id_fk;

alter table twinflow
    add if not exists on_update_twin_factory_id uuid
        constraint twinflow_on_update_twin_factory_id_fk
            references twin_factory;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_name = 'twinflow_initial_factory_id_fk'
              AND table_name = 'twinflow'
        ) THEN
            EXECUTE 'ALTER TABLE twinflow RENAME CONSTRAINT twinflow_initial_factory_id_fk TO twinflow_on_create_twin_factory_id_fk';
        END IF;
    END $$;
