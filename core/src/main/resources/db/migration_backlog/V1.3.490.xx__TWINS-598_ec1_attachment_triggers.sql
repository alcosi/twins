create table if not exists twin_attachment_delete_task (
    id                 uuid primary key,
    twin_attachment_id uuid not null,
    twin_id            uuid not null references twin on update cascade on delete no action,  -- no action for cases when twin is deleted but attachment task is still in NEED_START
    storage_id         uuid not null references storage on update cascade on delete cascade,
    storage_file_key   varchar(255) not null,
    status             varchar(50) not null default 'NEED_START',
    created_at         timestamp default CURRENT_TIMESTAMP
);


create or replace function twin_attachment_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- increase counters for businessAccount
    update domain_business_account dba
    set attachments_storage_used_count = attachments_storage_used_count + 1,
        attachments_storage_used_size = attachments_storage_used_size + new.size
    from twin te where te.id = new.twin_id and dba.business_account_id = te.owner_business_account_id;

    -- increase counters for domain
    update domain d
    set attachments_storage_used_count = attachments_storage_used_count + 1,
        attachments_storage_used_size = attachments_storage_used_size + new.size
    from twin te join twin_class tc ON te.twin_class_id = tc.id where te.id = new.twin_id and d.id = tc.domain_id;

    return new;
end;
$$;

create or replace function twin_attachment_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- calculate counters for businessAccount
    update domain_business_account dba
    set attachments_storage_used_size = attachments_storage_used_size - old.size + new.size
    from twin te where te.id = new.twin_id and dba.business_account_id = te.owner_business_account_id;

    -- calculate counters for domain
    update domain d
    set attachments_storage_used_size = attachments_storage_used_size - old.size + new.size
    from public.twin te join twin_class tc on te.twin_class_id = tc.id where te.id = new.twin_id and d.id = tc.domain_id;

    return new;
end;
$$;

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

    insert into twin_attachment_delete_task(id, twin_attachment_id, storage_id, storage_file_key, status, created_at)
    values (uuid_generate_v4(), old.id, old.storage_id, old.storage_file_key, 'NEED_START', now());

    return old;
end;
$$;


create or replace trigger twin_attachment_after_insert_wrapper_trigger
    after insert
    on twin_attachment
    for each row
execute procedure twin_attachment_after_insert_wrapper();

create or replace trigger twin_attachment_after_update_wrapper_trigger
    after update
    on twin_attachment
    for each row
execute procedure twin_attachment_after_update_wrapper();

create or replace trigger twin_attachment_after_delete_wrapper_trigger
    after delete
    on twin_attachment
    for each row
execute procedure twin_attachment_after_delete_wrapper();


drop trigger if exists trg_update_attachment_storage on twin_attachment;
