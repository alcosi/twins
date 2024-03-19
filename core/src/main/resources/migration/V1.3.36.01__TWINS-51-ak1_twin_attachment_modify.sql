INSERT INTO public.featurer (id, featurer_type_id, class, name, description) VALUES (1316::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperAttachment'::varchar, 'FieldTyperAttachment'::varchar, 'Allow the field to have an attachment'::varchar(255)) ON CONFLICT (id) DO NOTHING;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1316::integer, false::boolean, 1::integer, 'multiple'::varchar(40), 'multiple'::varchar(40), 'Allow add multiple attachments to field'::varchar(255), 'BOOLEAN'::varchar(40)) ON CONFLICT (featurer_id, key) DO NOTHING;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1316::integer, false::boolean, 1::integer, 'fileSizeMbLimit'::varchar(40), 'fileSizeMbLimit'::varchar(40), 'Max size per file for attachment'::varchar(255), 'INT'::varchar(40)) ON CONFLICT (featurer_id, key) DO NOTHING;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1316::integer, false::boolean, 1::integer, 'fileExtensionList'::varchar(40), 'fileExtensionList'::varchar(40), 'Allowed extensions for attachment(ex: jpg,jpeg,png)'::varchar(255), 'STRING'::varchar(40)) ON CONFLICT (featurer_id, key) DO NOTHING;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1316::integer, false::boolean, 1::integer, 'fileNameRegexp'::varchar(40), 'fileNameRegexp'::varchar(40), 'File name must match this pattern'::varchar(255), 'STRING'::varchar(40)) ON CONFLICT (featurer_id, key) DO NOTHING;
DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_attachment'::regclass AND attname = 'twin_class_field_id' AND attnum > 0 AND NOT attisdropped) THEN
            ALTER TABLE twin_attachment ADD COLUMN twin_class_field_id UUID;
            ALTER TABLE twin_attachment ADD CONSTRAINT fk_attachment_fieldclass FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field(id);
        END IF;
    END $$;
