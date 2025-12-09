create or replace function get_data_for_attachment_delete_task(old twin_attachment)
returns table (
    domain_id uuid,
    owner_business_account_id uuid
)
    language plpgsql as
$$
begin
    -- if twin archive record is not existing then go to twin table
    return query
    select twin_class.domain_id, twin_archive.owner_business_account_id
    from twin_archive
             join twin_class on twin_archive.twin_class_id = twin_class.id
    where twin_archive.id = old.twin_id
    union all
    select twin_class.domain_id, twin.owner_business_account_id
    from twin
             join twin_class on twin.twin_class_id = twin_class.id
    where twin.id = old.twin_id
      and not exists (select 1 from twin_archive where twin_archive.id = old.twin_id);
end;
$$;

create or replace function decrease_business_acc_counters(old twin_attachment, ba_id uuid)
    returns void
    language plpgsql
as
$$
begin
    update domain_business_account
    set attachments_storage_used_count = attachments_storage_used_count - 1,
        attachments_storage_used_size = attachments_storage_used_size - old.size
    where domain_business_account.business_account_id = ba_id;
end;
$$;

create or replace function decrease_domain_counters(old twin_attachment, dom_id uuid)
    returns void
    language plpgsql
as
$$
begin
    update domain
    set attachments_storage_used_count = attachments_storage_used_count - 1,
        attachments_storage_used_size = attachments_storage_used_size - old.size
    where domain.id = dom_id;
end;
$$;

create or replace function create_attachment_delete_task_and_decrease_storage_counters(old twin_attachment)
    returns void
    language plpgsql
as
$$
declare
    dom_id uuid;
    ba_id uuid;
begin
    select domain_id, owner_business_account_id
    into dom_id, ba_id
    from get_data_for_attachment_delete_task(old);

    perform decrease_business_acc_counters(old, ba_id);
    perform decrease_domain_counters(old, dom_id);

    insert into twin_attachment_delete_task(id, twin_attachment_id, twin_id, domain_id, twin_owner_business_account_id, twin_created_by_user_id, storage_id, status, storage_file_key, created_at)
    values (uuid_generate_v4(), old.id, old.twin_id, dom_id, ba_id, old.created_by_user_id, old.storage_id, 'NEED_START', old.storage_file_key, now());
end;
$$;

create or replace function twin_attachment_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform create_attachment_delete_task_and_decrease_storage_counters(old);

    return old;
end;
$$;
