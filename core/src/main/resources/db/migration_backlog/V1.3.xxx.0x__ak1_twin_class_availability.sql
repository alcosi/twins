INSERT INTO public.i18n_type (id, name) VALUES ('twinClassAvailabilityName'::varchar, 'Twin class availability name'::varchar(255)) on conflict (id) do nothing;
INSERT INTO public.i18n_type (id, name) VALUES ('twinClassAvailabilityDescription'::varchar, 'Twin class availability description'::varchar(255)) on conflict (id) do nothing;
INSERT INTO public.twin_class_availability (id, key, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0015-000000000001', 'SPIRIT', null, null) on conflict (id) do nothing;
INSERT INTO public.twin_class_availability (id, key, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0015-000000000002', 'ACTIVE', null, null) on conflict (id) do nothing;


INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000001-0000-0000-0000-000000000003', null, null, 'twinStatusName', null) on conflict do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('00000001-0000-0000-0000-000000000004', null, null, 'twinStatusDescription', null) on conflict do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000001-0000-0000-0000-000000000003', 'en', 'Spirit', 0) on conflict do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000001-0000-0000-0000-000000000004', 'en', '', 0) on conflict do nothing;
INSERT INTO public.twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, background_color, key, font_color)
    VALUES ('00000001-0000-0000-0000-000000000002', '00000000-0000-0000-0001-000000000004', '00000001-0000-0000-0000-000000000003', '00000001-0000-0000-0000-000000000004', '#000000', 'spirit', '#000000') on conflict do nothing;

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

UPDATE twin_class SET twin_class_availability_id = '00000000-0000-0000-0015-000000000002' WHERE twin_class_availability_id IS NULL;

alter table public.twin_class alter column twin_class_availability_id set not null;

