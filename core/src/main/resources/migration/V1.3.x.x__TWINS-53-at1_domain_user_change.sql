alter table domain_user
    add column if not exists i18n_locale_id varchar;

alter table domain_user
    drop constraint if exists domain_user_i18n_locale_id_fk;

alter table domain_user
    add constraint domain_user_i18n_locale_id_fk
        foreign key (i18n_locale_id) references i18n_locale;

alter table domain
    add column if not exists default_i18n_locale_id varchar;

alter table domain
    drop constraint if exists domain_i18n_locale_id_fk;

alter table domain
    add constraint domain_i18n_locale_id_fk
        foreign key (default_i18n_locale_id) references i18n_locale;

