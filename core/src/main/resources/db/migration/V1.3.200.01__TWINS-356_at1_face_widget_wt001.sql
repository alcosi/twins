DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_widget_wt001' AND column_name = 'hide_columns'
        ) THEN
            ALTER TABLE face_widget_wt001
                RENAME COLUMN hide_columns TO show_columns;
        END IF;
    END $$;