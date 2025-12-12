create or replace function twin_attachment_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
declare
    dom_id uuid;
    ba_id uuid;
begin
    select twin_class.domain_id, twin_archive.owner_business_account_id
    into dom_id, ba_id
    from twin_archive
             join twin_class on twin_archive.twin_class_id = twin_class.id
    where twin_archive.id = old.twin_id;

    -- decrease counters for businessAccount
    update domain_business_account
    set attachments_storage_used_count = attachments_storage_used_count - 1,
        attachments_storage_used_size = attachments_storage_used_size - old.size
    where domain_business_account.business_account_id = ba_id;

    -- decrease counters for domain
    update domain
    set attachments_storage_used_count = attachments_storage_used_count - 1,
        attachments_storage_used_size = attachments_storage_used_size - old.size
    where domain.id = dom_id;

    insert into twin_attachment_delete_task(id, twin_attachment_id, twin_id, domain_id, twin_owner_business_account_id, twin_created_by_user_id, storage_id, status, storage_file_key, created_at)
    values (uuid_generate_v4(), old.id, old.twin_id, dom_id, ba_id, old.created_by_user_id, old.storage_id, 'NEED_START', old.storage_file_key, now());

    return old;
end;
$$;
