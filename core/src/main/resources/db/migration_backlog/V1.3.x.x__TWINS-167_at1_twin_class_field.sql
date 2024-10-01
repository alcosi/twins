-- data for email
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('00000000-0000-0000-1111-000000000001', null, null, 'twinClassFieldName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-1111-000000000001', 'en', 'Email', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1318, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperAssigneeEmail', 'FieldTyperAssigneeEmail', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1318, false, 1, 'userEmail', 'userEmail', null, 'STRING') on conflict on constraint featurer_param_pk do nothing ;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000001', '00000000-0000-0000-0001-000000000001', 'email', '00000000-0000-0000-1111-000000000001', null, 1318, null, null, null, false) on conflict on constraint twin_class_field_pk do nothing ;

-- data for avatar
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('00000000-0000-0000-1111-000000000002', null, null, 'twinClassFieldName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-1111-000000000002', 'en', 'Avatar', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1319, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperAssigneeAvatar', 'FieldTyperAssigneeAvatar', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1319, false, 1, 'userAvatar', 'userAvatar', null, 'STRING') on conflict on constraint featurer_param_pk do nothing ;
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES ('00000000-0000-0000-0011-000000000002', '00000000-0000-0000-0001-000000000001', 'avatar', '00000000-0000-0000-1111-000000000002', null, 1319, null, null, null, false) on conflict on constraint twin_class_field_pk do nothing ;


DROP FUNCTION IF EXISTS user_insert_or_update() CASCADE;

CREATE OR REPLACE FUNCTION user_insert_or_update()
    RETURNS TRIGGER AS $$
BEGIN
    -- Inserting a new record into twin when adding a record to user
    IF TG_OP = 'INSERT' THEN
        INSERT INTO twin (id, twin_class_id, twin_status_id, name, created_by_user_id, assigner_user_id, created_at)
        VALUES (NEW.id, '00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0003-000000000001', NEW.name, '00000000-0000-0000-0000-000000000000', NEW.id ,NEW.created_at)
        ON CONFLICT (id) DO NOTHING;

    -- Updating name a field in twin when updating a record in user
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE twin
        SET name = NEW.name
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_user_insert_or_update
    AFTER INSERT OR UPDATE ON "user"
    FOR EACH ROW
EXECUTE FUNCTION user_insert_or_update();



