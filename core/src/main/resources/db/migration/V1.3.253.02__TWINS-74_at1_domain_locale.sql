alter table domain_locale
    drop constraint domain_locale_i18n_locale_locale_fk;

alter table domain_locale
    add constraint domain_locale_i18n_locale_locale_fk
        foreign key (i18n_locale_id) references i18n_locale
            on update cascade;
