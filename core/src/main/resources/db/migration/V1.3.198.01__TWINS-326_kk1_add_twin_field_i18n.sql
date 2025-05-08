CREATE TABLE IF NOT EXISTS twin_field_i18n
(
    id                  UUID PRIMARY KEY,
    twin_id             UUID       NOT NULL REFERENCES twin (id) ON DELETE CASCADE,
    twin_class_field_id UUID       NOT NULL REFERENCES twin_class_field (id) ON DELETE CASCADE,
    locale              VARCHAR(2) NOT NULL REFERENCES i18n_locale (locale) ON DELETE CASCADE,
    translation         TEXT       NOT NULL,
    UNIQUE (twin_id, twin_class_field_id, locale)
);