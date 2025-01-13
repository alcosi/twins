-- rename comment into description
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twin_factory_multiplier_filter' AND column_name = 'description'
        ) THEN
            ALTER TABLE twin_factory_multiplier_filter
        RENAME COLUMN comment TO description;
    END IF;
END $$;
