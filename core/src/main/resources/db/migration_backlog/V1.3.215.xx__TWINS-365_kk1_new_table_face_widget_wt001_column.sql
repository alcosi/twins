DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'face_widget_wt001_column') THEN
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'face') THEN
                IF EXISTS (SELECT 1
                           FROM information_schema.columns
                           WHERE table_name = 'face'
                             AND column_name = 'id') THEN
                    CREATE TABLE face_widget_wt001_column
                    (
                        id                  UUID PRIMARY KEY,
                        face_id             UUID    NOT NULL,
                        twin_class_field_id UUID    NOT NULL,
                        "order"             INTEGER NOT NULL,
                        label_i18n_id       UUID,

                        CONSTRAINT fk_face_widget_wt001_column_face
                            FOREIGN KEY (face_id) REFERENCES face (id),
                        CONSTRAINT fk_face_widget_wt001_column_twin_class_field
                            FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field (id),
                        CONSTRAINT fk_face_widget_wt001_column_label_i18n
                            FOREIGN KEY (label_i18n_id) REFERENCES i18n (id)
                    );
                END IF;
            END IF;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_face_widget_wt001_column_face_id') THEN
            CREATE INDEX idx_face_widget_wt001_column_face_id ON face_widget_wt001_column (face_id);
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM pg_indexes
                       WHERE indexname = 'idx_face_widget_wt001_column_twin_class_field_id') THEN
            CREATE INDEX idx_face_widget_wt001_column_twin_class_field_id ON face_widget_wt001_column (twin_class_field_id);
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_face_widget_wt001_column_label_i18n_id') THEN
            CREATE INDEX idx_face_widget_wt001_column_label_i18n_id ON face_widget_wt001_column (label_i18n_id);
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'face_widget_wt001'
                     AND column_name = 'show_columns') THEN
            ALTER TABLE face_widget_wt001
                DROP COLUMN show_columns;
        END IF;
    END
$$;