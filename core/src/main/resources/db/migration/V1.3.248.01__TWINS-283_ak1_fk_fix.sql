alter table public.twin_class_owner_type
    drop constraint if exists fk_name_i18n;

alter table public.twin_class_owner_type
    drop constraint if exists fk_description_i18n;


ALTER TABLE twin_class_owner_type
    ADD CONSTRAINT fk_name_i18n
        FOREIGN KEY (name_i18n_id)
            REFERENCES i18n (id)
            ON DELETE RESTRICT;

ALTER TABLE twin_class_owner_type
    ADD CONSTRAINT fk_description_i18n
        FOREIGN KEY (description_i18n_id)
            REFERENCES i18n (id)
            ON DELETE RESTRICT;
