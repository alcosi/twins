alter table if exists domain_user
    add column if not exists i18n_locale_id uuid;

alter table if exists domain
    add column if not exists default_i18n_locale_id uuid;
