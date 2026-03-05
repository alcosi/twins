create or replace function twin_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS NULL THEN
        -- permission_schema_id will recalculatred in after_update_wrapper.detecthierarchy
        NEW.permission_schema_id = permission_schema_detect(NEW.permission_schema_space_id, new.owner_business_account_id, new.twin_class_id);
    end if;
    new.head_hierarchy_counter_direct_children := 0;
    RETURN NEW;
END;
$$;

