create table if not exists domain_locale
(
    id             uuid    not null
        constraint domain_locale_pk
            primary key,
    domain_id      uuid    not null
        constraint domain_locale_domain_id_fk
            references domain,
    i18n_locale_id varchar not null
        constraint domain_locale_i18n_locale_locale_fk
            references i18n_locale,
    icon           varchar,
    active         boolean default true,
    CONSTRAINT domain_id_locale_id_uk UNIQUE(domain_id,i18n_locale_id)
);

alter table i18n_locale
    add if not exists native_name varchar;

alter table i18n_locale
    add if not exists icon varchar;
