alter table public.twinflow
    drop constraint if exists twinflow_twin_class_id_fk;

alter table public.twinflow
    add constraint twinflow_twin_class_id_fk
        foreign key (twin_class_id) references public.twin_class
            on update cascade on delete cascade;

alter table public.twinflow_schema_map
    drop constraint if exists twinflow_schema_map_twin_class_id_fk;

alter table public.twinflow_schema_map
    add constraint twinflow_schema_map_twin_class_id_fk
        foreign key (twin_class_id) references public.twin_class
            on update cascade on delete cascade;

ALTER TABLE public.permission_group
DROP CONSTRAINT IF EXISTS permission_group_twin_class_id_fk;

ALTER TABLE public.permission_group
    ADD CONSTRAINT permission_group_twin_class_id_fk
        FOREIGN KEY (twin_class_id) REFERENCES public.twin_class
            ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE public.permission
    DROP CONSTRAINT IF EXISTS permission_permission_group_id_fk;

ALTER TABLE public.permission
    ADD CONSTRAINT permission_permission_group_id_fk
        FOREIGN KEY (permission_group_id) REFERENCES public.permission_group
            ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE public.permission_grant_assignee_propagation
    DROP CONSTRAINT IF EXISTS permission_schema_assignee_propag_permission_id_fkey;

ALTER TABLE public.permission_grant_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_permission_id_fkey
        FOREIGN KEY (permission_id) REFERENCES public.permission
            ON DELETE CASCADE;

ALTER TABLE public.permission_grant_space_role
    DROP CONSTRAINT IF EXISTS permission_schema_space_roles_permission_id_fk;

ALTER TABLE public.permission_grant_space_role
    ADD CONSTRAINT permission_schema_space_roles_permission_id_fk
        FOREIGN KEY (permission_id) REFERENCES public.permission
            ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE public.permission_grant_twin_role
    DROP CONSTRAINT IF EXISTS permission_schema_twin_role_permission_id_fkey;

ALTER TABLE public.permission_grant_twin_role
    ADD CONSTRAINT permission_schema_twin_role_permission_id_fkey
        FOREIGN KEY (permission_id) REFERENCES public.permission
            ON DELETE CASCADE;

ALTER TABLE public.permission_grant_user
    DROP CONSTRAINT IF EXISTS permission_schema_user_permission_id_fk;

ALTER TABLE public.permission_grant_user
    ADD CONSTRAINT permission_schema_user_permission_id_fk
        FOREIGN KEY (permission_id) REFERENCES public.permission
            ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE public.permission_grant_user_group
    DROP CONSTRAINT IF EXISTS permission_schema_user_group_permission_id_fk;

ALTER TABLE public.permission_grant_user_group
    ADD CONSTRAINT permission_schema_user_group_permission_id_fk
        FOREIGN KEY (permission_id) REFERENCES public.permission
            ON UPDATE CASCADE ON DELETE CASCADE;



DROP TRIGGER IF EXISTS permissions_on_twin_class_update ON twin_class;
DROP TRIGGER IF EXISTS twin_class_on_delete_i18n_and_translations_delete ON twin_class;
DROP TRIGGER IF EXISTS permission_on_delete_i18n_and_translations_delete ON permission;
DROP FUNCTION IF EXISTS permissions_autoupdate_on_twin_class_update CASCADE;
DROP FUNCTION IF EXISTS permission_on_delete_i18n_and_translations_delete CASCADE;
DROP FUNCTION IF EXISTS twin_class_on_delete_i18n_and_translations_delete CASCADE;

CREATE OR REPLACE FUNCTION permissions_autoupdate_on_twin_class_update()
    RETURNS TRIGGER AS
$$
DECLARE
    old_twin_class_key  TEXT;
    new_twin_class_key  TEXT;
    permission_id       UUID;
    perm_group_id       UUID;
    i18n_id_name        UUID;
    i18n_id_description UUID;
    perm                VARCHAR;
    perm_arr            VARCHAR[] := ARRAY ['CREATE','EDIT','DELETE','VIEW'];
BEGIN
    IF (NEW.domain_id IS NULL) THEN
        return NEW;
    end if;
    IF (OLD.key = NEW.key) THEN
        RETURN NEW;
    END IF;

    old_twin_class_key := OLD.key;
    new_twin_class_key := NEW.key;

    SELECT id INTO perm_group_id FROM permission_group WHERE twin_class_id = NEW.id AND domain_id = NEW.domain_id;

    UPDATE permission_group SET key  = new_twin_class_key || '_PERMISSIONS', name = lower(replace(new_twin_class_key, '_', ' ') || ' permissions') WHERE twin_class_id = NEW.id AND domain_id = NEW.domain_id;

    FOREACH perm SLICE 0 IN ARRAY perm_arr
        LOOP
            SELECT id, name_i18n_id, description_i18n_id INTO permission_id, i18n_id_name, i18n_id_description FROM permission WHERE key = old_twin_class_key || '_' || perm and permission_group_id = perm_group_id;
            UPDATE i18n_translation SET translation = lower(replace(new_twin_class_key, '_', ' ')) || ' ' || lower(perm) || ' permission' WHERE i18n_id = i18n_id_name AND locale = 'en';
            UPDATE i18n_translation SET translation = lower(replace(new_twin_class_key, '_', ' ')) || ' ' || lower(perm) || ' permission' WHERE i18n_id = i18n_id_description AND locale = 'en';
            UPDATE permission SET key = new_twin_class_key || '_' || perm WHERE id = permission_id;
        END LOOP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER permissions_on_twin_class_update
    AFTER UPDATE OF key
    ON twin_class
    FOR EACH ROW
EXECUTE FUNCTION permissions_autoupdate_on_twin_class_update();

CREATE OR REPLACE FUNCTION permission_on_delete_i18n_and_translations_delete() RETURNS TRIGGER AS
$$
BEGIN
    DELETE FROM i18n_translation WHERE i18n_id = OLD.name_i18n_id OR i18n_id = OLD.description_i18n_id;
    DELETE FROM i18n WHERE id = OLD.name_i18n_id OR id = OLD.description_i18n_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER permission_on_delete_i18n_and_translations_delete
    AFTER DELETE
    ON permission
    FOR EACH ROW
EXECUTE FUNCTION permission_on_delete_i18n_and_translations_delete();

CREATE OR REPLACE FUNCTION twin_class_on_delete_i18n_and_translations_delete() RETURNS TRIGGER AS
$$
BEGIN
    DELETE FROM i18n_translation WHERE i18n_id = OLD.name_i18n_id OR i18n_id = OLD.description_i18n_id;
    DELETE FROM i18n WHERE id = OLD.name_i18n_id OR id = OLD.description_i18n_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER twin_class_on_delete_i18n_and_translations_delete
    AFTER DELETE
    ON twin_class
    FOR EACH ROW
EXECUTE FUNCTION twin_class_on_delete_i18n_and_translations_delete();
