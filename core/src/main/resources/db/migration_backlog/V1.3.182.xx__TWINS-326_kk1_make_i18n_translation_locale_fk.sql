INSERT INTO i18n_locale (locale, name)
VALUES ('be', 'Belarusian')
ON CONFLICT (locale) DO NOTHING;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_i18n_translation_locale'
        ) THEN
            ALTER TABLE i18n_translation
                ADD CONSTRAINT fk_i18n_translation_locale
                    FOREIGN KEY (locale) REFERENCES i18n_locale (locale) ON DELETE CASCADE;
        END IF;
    END $$;