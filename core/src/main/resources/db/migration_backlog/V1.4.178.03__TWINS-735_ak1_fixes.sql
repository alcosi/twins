UPDATE twin t
SET view_permission_id = tc.view_permission_id
FROM twin_class tc
WHERE not t.view_permission_custom and t.twin_class_id = tc.id and t.view_permission_id is null;

UPDATE twin SET view_permission_custom = false WHERE view_permission_id is null;
ALTER TABLE twin ALTER COLUMN view_permission_custom SET NOT NULL;

create or replace function twin_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS NULL THEN
--         permission_schema_id will recalculatred in after_update_wrapper.detecthierarchy
        NEW.permission_schema_id = permission_schema_detect(NEW.permission_schema_space_id, new.owner_business_account_id, new.twin_class_id);
    end if;
    IF new.view_permission_custom is null THEN
        new.view_permission_custom = false; -- delete me in f'yuche
    end if;
    IF not new.view_permission_custom THEN
        SELECT view_permission_id INTO NEW.view_permission_id FROM twin_class WHERE id = NEW.twin_class_id;
    END IF;
    RETURN NEW;
END;
$$;
