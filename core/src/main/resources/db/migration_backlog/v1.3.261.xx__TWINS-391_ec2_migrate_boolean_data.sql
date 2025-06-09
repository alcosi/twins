DO $$
    DECLARE
        batch_size INTEGER := 1000;
        rows_moved INTEGER := 1;
    BEGIN
        WHILE rows_moved > 0 LOOP
                WITH migration_batch AS (
                    DELETE FROM twin_field_simple
                        WHERE id IN (
                            SELECT id
                            FROM twin_field_simple
                            WHERE value = 'true' OR value = 'false'
                            LIMIT batch_size
                        )
                        RETURNING *
                ),
                     inserted_rows AS (
                         INSERT INTO twin_field_boolean(id, twin_id, twin_class_field_id, value)
                             SELECT id, twin_id, twin_class_field_id, value::boolean
                             FROM migration_batch
                             RETURNING 1
                     )
                SELECT COUNT(*) INTO rows_moved FROM inserted_rows;

                RAISE NOTICE 'Rows migrated: %', rows_moved;
                COMMIT;

                PERFORM pg_sleep(0.1);
            END LOOP;
    END $$;