DO $$
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'face_twidget_tw004'
              AND column_name = 'field_filter_featurer_id'
        ) THEN
            ALTER TABLE face_twidget_tw004
                RENAME COLUMN "field_filter_featurer_id" TO required_field_filter_featurer_id;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'face_twidget_tw004'
              AND column_name = 'field_filter_params'
        ) THEN
            ALTER TABLE face_twidget_tw004
                RENAME COLUMN "field_filter_params" TO required_field_filter_params;
        END IF;
    END $$;