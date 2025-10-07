CREATE TABLE IF NOT EXISTS twin_class_availability
(
    id                  UUID NOT NULL,
    key                 VARCHAR(255),
    name_i18n_id        UUID,
    description_i18n_id UUID,
    CONSTRAINT pk_twin_class_availability PRIMARY KEY (id),
    CONSTRAINT fk_twin_class_availability_name_i18n_id FOREIGN KEY (name_i18n_id) REFERENCES i18n (id),
    CONSTRAINT fk_twin_class_availability_description_i18n_id FOREIGN KEY (description_i18n_id) REFERENCES i18n (id)
);

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT
                       FROM information_schema.columns
                       WHERE table_name = 'twin_class'
                         AND column_name = 'twin_class_availability_id') THEN
            ALTER TABLE twin_class
                ADD COLUMN twin_class_availability_id UUID,
                ADD CONSTRAINT fk_twin_class_twin_class_availability_id
                    FOREIGN KEY (twin_class_availability_id)
                        REFERENCES twin_class_availability (id);
        END IF;
    END
$$;

-- todo add system, set default values

alter table public.twin_class
    alter column twin_class_availability_id set not null;

