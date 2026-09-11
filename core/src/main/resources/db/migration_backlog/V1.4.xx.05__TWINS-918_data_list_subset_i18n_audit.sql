-- Data list subset: i18n name/description + audit columns
-- i18n types for subset name/description
INSERT INTO i18n_type (id, name) VALUES ('dataListSubsetName', 'Data List Subset Name') on conflict on constraint i18n_type_pk do nothing;
INSERT INTO i18n_type (id, name) VALUES ('dataListSubsetDescription', 'Data List Subset Description') on conflict on constraint i18n_type_pk do nothing;

ALTER TABLE data_list_subset ADD COLUMN IF NOT EXISTS name_i18n_id uuid;
ALTER TABLE data_list_subset ADD COLUMN IF NOT EXISTS description_i18n_id uuid;

-- transfer existing varchar name/description values into i18n + 'en' translations
-- (domain_id of each i18n is taken from the owning data list — precedent V1.3.215.02)
DO $$
DECLARE
    r RECORD;
    nameI18nId uuid;
    descriptionI18nId uuid;
    domainId uuid;
BEGIN
    FOR r IN SELECT * FROM data_list_subset WHERE (name IS NOT NULL AND name_i18n_id IS NULL) OR (description IS NOT NULL AND description_i18n_id IS NULL) LOOP
        SELECT domain_id INTO domainId FROM data_list WHERE data_list.id = r.data_list_id;
        IF r.name IS NOT NULL AND r.name_i18n_id IS NULL THEN
            nameI18nId := gen_random_uuid();
            INSERT INTO i18n (id, i18n_type_id, domain_id) VALUES (nameI18nId, 'dataListSubsetName', domainId);
            INSERT INTO i18n_translation (i18n_id, locale, translation) VALUES (nameI18nId, 'en', r.name);
            UPDATE data_list_subset SET name_i18n_id = nameI18nId WHERE id = r.id;
        END IF;
        IF r.description IS NOT NULL AND r.description_i18n_id IS NULL THEN
            descriptionI18nId := gen_random_uuid();
            INSERT INTO i18n (id, i18n_type_id, domain_id) VALUES (descriptionI18nId, 'dataListSubsetDescription', domainId);
            INSERT INTO i18n_translation (i18n_id, locale, translation) VALUES (descriptionI18nId, 'en', r.description);
            UPDATE data_list_subset SET description_i18n_id = descriptionI18nId WHERE id = r.id;
        END IF;
    END LOOP;
END $$;

ALTER TABLE data_list_subset DROP COLUMN IF EXISTS name;
ALTER TABLE data_list_subset DROP COLUMN IF EXISTS description;

-- audit columns
ALTER TABLE data_list_subset ADD COLUMN IF NOT EXISTS created_at timestamp;
ALTER TABLE data_list_subset ADD COLUMN IF NOT EXISTS created_by_user_id uuid;

CREATE INDEX IF NOT EXISTS idx_data_list_subset_created_by_user_id ON data_list_subset(created_by_user_id);
