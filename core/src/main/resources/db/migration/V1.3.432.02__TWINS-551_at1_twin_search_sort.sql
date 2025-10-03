DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twin_search_sort' AND column_name = 'twin_class_field_id'
        ) THEN
            ALTER TABLE twin_search_sort
                RENAME COLUMN twin_class_field__id TO twin_class_field_id;
    END IF;
END $$;
