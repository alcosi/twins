
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twin_status'
              AND column_name = 'twins_class_id'
        ) THEN
            ALTER TABLE twin_status
                RENAME COLUMN twins_class_id TO twin_class_id;
        END IF;
    END $$;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM pg_class
            WHERE relname = 'twin_status_twins_class_id_index'
        ) THEN
            ALTER INDEX twin_status_twins_class_id_index
                RENAME TO twin_status_twin_class_id_index;
        END IF;
    END $$;