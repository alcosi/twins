-- before insertion array of translations - clear it for duplicates
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('uuid', 'locale', 'translation', 0)
ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = EXCLUDED.translation;
