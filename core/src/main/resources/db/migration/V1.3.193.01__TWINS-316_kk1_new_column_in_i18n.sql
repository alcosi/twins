ALTER TABLE public.i18n
    ADD COLUMN IF NOT EXISTS domain_id uuid;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conrelid = 'public.i18n'::regclass
              AND conname = 'i18n_domain_id_fk'
        ) THEN

            ALTER TABLE public.i18n
                ADD CONSTRAINT i18n_domain_id_fk
                    FOREIGN KEY (domain_id)
                        REFERENCES public.domain(id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE;
        END IF;
    END $$ LANGUAGE plpgsql;