UPDATE twin t
SET view_permission_id = tc.view_permission_id
    FROM twin_class tc
WHERE not t.view_permission_custom and t.twin_class_id = tc.id and t.view_permission_id is null;

create or replace function twin_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS NULL THEN
        -- permission_schema_id will recalculated in after_update_wrapper.detecthierarchy
        NEW.permission_schema_id = permission_schema_detect(
            NEW.permission_schema_space_id,
            NEW.owner_business_account_id,
            NEW.twin_class_id
        );
    END IF;

    IF NOT NEW.view_permission_custom THEN
        SELECT view_permission_id
        INTO NEW.view_permission_id
        FROM twin_class
        WHERE id = NEW.twin_class_id;
    END IF;

    NEW.head_hierarchy_counter_direct_children := 0;
    RETURN NEW;
END;
$$;

drop trigger if exists twin_before_insert_wrapper_trigger on twin;
create trigger twin_before_insert_wrapper_trigger
    before insert
    on twin
    for each row
execute procedure twin_before_insert_wrapper();

