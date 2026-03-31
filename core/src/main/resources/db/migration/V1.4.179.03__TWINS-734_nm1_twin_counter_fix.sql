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

-- fixing incorrect descriptions
UPDATE scheduler SET cron = '6 0 * * * *', description = 'Scheduler to check permission materialization user groups grants count is not less then 0' WHERE id = '00000000-0000-0000-0015-000000000012';
UPDATE scheduler SET cron = '5 0 * * * *', description = 'Scheduler to check permission materialization global grants count is not less then 0' WHERE id = '00000000-0000-0000-0015-000000000011';
UPDATE scheduler SET cron = '9 0 * * * *', description = 'Scheduler to check permission materialization space user grants count is not less then 0' WHERE id = '00000000-0000-0000-0015-000000000015';
UPDATE scheduler SET cron = '7 0 * * * *', description = 'Scheduler to check permission schema detect mismatches in twin' WHERE id = '00000000-0000-0000-0015-000000000013';
UPDATE scheduler SET cron = '8 0 * * * *', description = 'Scheduler to check permission materialization space user groups grants count is not less then 0' WHERE id = '00000000-0000-0000-0015-000000000014';