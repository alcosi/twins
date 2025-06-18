DO $$
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'face_widget_wt002_button'
              AND column_name = 'twin_class_id'
        ) THEN
            ALTER TABLE face_widget_wt002_button DROP COLUMN twin_class_id;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'face_widget_wt002_button'
              AND column_name = 'extends_depth'
        ) THEN
            ALTER TABLE face_widget_wt002_button DROP COLUMN extends_depth;
        END IF;
    END $$;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'face_widget_wt002_button'
              AND column_name = 'face_twin_create_id'
        ) THEN
            ALTER TABLE face_widget_wt002_button
                ADD COLUMN face_twin_create_id UUID;

            IF NOT EXISTS (
                SELECT 1 FROM pg_constraint
                WHERE conrelid = 'face_widget_wt002_button'::regclass
                  AND conname = 'face_widget_wt002_button_twin_create_face_id_fkey'
            ) THEN
                ALTER TABLE face_widget_wt002_button
                    ADD CONSTRAINT fk_face_widget_wt002_button_face
                        FOREIGN KEY (face_twin_create_id)
                            REFERENCES face(id)
                            ON UPDATE CASCADE ON DELETE CASCADE ;
            END IF;

            IF NOT EXISTS (
                SELECT 1 FROM pg_indexes
                WHERE tablename = 'face_widget_wt002_button'
                  AND indexname = 'idx_face_widget_wt002_button_face_twin_create_id'
            ) THEN
                CREATE INDEX idx_face_widget_wt002_button_face_twin_create_id
                    ON face_widget_wt002_button(face_twin_create_id);
            END IF;
        END IF;
    END $$;