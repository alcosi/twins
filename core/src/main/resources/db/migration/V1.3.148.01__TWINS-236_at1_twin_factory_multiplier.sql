alter table public.twin_factory_multiplier
    add if not exists active boolean default true not null;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twin_factory_multiplier' AND column_name = 'description'
        ) THEN
        ALTER TABLE twin_factory_multiplier
            RENAME COLUMN comment TO description;
    END IF;
END $$;
