create or replace function twin_attachment_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- decrease counters for businessAccount
    update domain_business_account dba
    set attachments_storage_used_count = attachments_storage_used_count - 1,
        attachments_storage_used_size = attachments_storage_used_size - old.size
    from twin te where te.id = old.twin_id and dba.business_account_id = te.owner_business_account_id;

    -- decrease counters for domain
    update domain d
    set attachments_storage_used_count = attachments_storage_used_count - 1,
        attachments_storage_used_size = attachments_storage_used_size - old.size
    from twin te join twin_class tc on te.twin_class_id = tc.id where te.id = old.twin_id and d.id = tc.domain_id;

    return old;
end;
$$;
