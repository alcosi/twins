create or replace function twin_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS NULL THEN
        -- permission_schema_id will recalculatred in after_update_wrapper.detecthierarchy
        NEW.permission_schema_id = permission_schema_detect(NEW.permission_schema_space_id, new.owner_business_account_id, new.twin_class_id);
    end if;

    RETURN NEW;
END;
$$;

drop trigger if exists twin_before_insert_wrapper_trigger on twin;
create trigger twin_before_insert_wrapper_trigger
    before insert
    on twin
    for each row
execute procedure twin_before_insert_wrapper();

