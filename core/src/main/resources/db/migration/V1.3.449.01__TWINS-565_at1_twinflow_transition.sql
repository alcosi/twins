INSERT INTO twinflow_transition_type (id, description) VALUES ('MARKETING', 'marketing') on conflict on constraint twinflow_transition_type_pkey do nothing ;

alter table twinflow_transition
    drop constraint if exists twinflow_transition_uniq;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_name = 'twinflow_transition_uniq'
              AND table_name = 'twinflow_transition'
        ) THEN
            ALTER TABLE twinflow_transition
                ADD CONSTRAINT twinflow_transition_uniq
                    UNIQUE (twinflow_id, src_twin_status_id, twinflow_transition_alias_id, twinflow_transition_type_id);
        END IF;
    END $$;