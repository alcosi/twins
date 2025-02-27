DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tier' AND column_name = 'createdat') THEN
            ALTER TABLE public.tier DROP COLUMN created_at;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tier' AND column_name = 'updatedat') THEN
            ALTER TABLE public.tier DROP COLUMN updated_at;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tier' AND column_name = 'createdat') THEN
            ALTER TABLE public.tier ADD COLUMN created_at timestamp;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tier' AND column_name = 'updatedat') THEN
            ALTER TABLE public.tier ADD COLUMN updated_at timestamp;
        END IF;
    END $$ LANGUAGE plpgsql;