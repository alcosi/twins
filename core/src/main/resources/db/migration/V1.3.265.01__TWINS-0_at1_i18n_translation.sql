alter table i18n_translation
    drop constraint fk_i18n_translation_locale;

alter table i18n_translation
    add constraint fk_i18n_translation_locale
        foreign key (locale) references public.i18n_locale
            on update cascade on delete cascade;