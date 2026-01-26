INSERT INTO public.i18n_type (id, name) VALUES ('twinClassFreezeName'::varchar, 'Twin class freeze name'::varchar(255)) on conflict (id) do nothing;
INSERT INTO public.i18n_type (id, name) VALUES ('twinClassFreezeDescription'::varchar, 'Twin class freeze description'::varchar(255)) on conflict (id) do nothing;



CREATE TABLE IF NOT EXISTS twin_class_freeze
(
    id                  UUID NOT NULL,
    key                 VARCHAR(255),
    twin_status_id      UUID not null,
    name_i18n_id        UUID,
    description_i18n_id UUID,
    CONSTRAINT pk_twin_class_freeze PRIMARY KEY (id),
    CONSTRAINT fk_twin_class_freeze_twin_status_id FOREIGN KEY (twin_status_id) REFERENCES twin_status (id),
    CONSTRAINT fk_twin_class_freeze_name_i18n_id FOREIGN KEY (name_i18n_id) REFERENCES i18n (id),
    CONSTRAINT fk_twin_class_freeze_description_i18n_id FOREIGN KEY (description_i18n_id) REFERENCES i18n (id)
);


DO
$$
    BEGIN
        IF NOT EXISTS (SELECT
                       FROM information_schema.columns
                       WHERE table_name = 'twin_class'
                         AND column_name = 'twin_class_freeze_id') THEN
            ALTER TABLE twin_class
                ADD COLUMN twin_class_freeze_id UUID,
                ADD CONSTRAINT fk_twin_class_twin_class_freeze_id
                    FOREIGN KEY (twin_class_freeze_id)
                        REFERENCES twin_class_freeze (id);
        END IF;
    END
$$;


