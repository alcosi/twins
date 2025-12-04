drop table if exists twin_attachment_delete_task;

create table if not exists twin_attachment_delete_task
(
    id                             uuid primary key,
    twin_attachment_id             uuid         not null,
    twin_id                        uuid         not null,
    domain_id                      uuid         not null,
    twin_owner_business_account_id uuid         not null,
    twin_created_by_user_id        uuid         not null,
    storage_id                     uuid         not null references storage on update cascade on delete cascade,
    storage_file_key               varchar(255) not null,
    status                         varchar(50)  not null default 'NEED_START',
    created_at                     timestamp             default CURRENT_TIMESTAMP
);

create index if not exists twin_attachment_delete_task_storage_id_index
    on twin_attachment_delete_task (storage_id);

create table if not exists twin_archive
(
    id                        uuid not null primary key,
    twin_class_id             uuid not null,
    head_twin_id              uuid,
    external_id               varchar,
    twin_status_id            uuid not null,
    created_by_user_id        uuid not null,
    created_at                timestamp default CURRENT_TIMESTAMP,
    owner_business_account_id uuid,
    owner_user_id             uuid,
    hierarchy_tree            ltree
);

create or replace function twin_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    raise notice 'Process insert for: %', new.id;
    perform hierarchyUpdateTreeHard(new.id, hierarchyDetectTree(new.id));

    return new;
end;
$$;

create or replace function twin_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    if old.head_twin_id is distinct from new.head_twin_id then
        raise notice 'Process update for: %', new.id;
        perform hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));
    end if;

    return new;
end;
$$;

create or replace function twin_before_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    insert into twin_archive(id, twin_class_id, head_twin_id, external_id, twin_status_id,
                             created_by_user_id, created_at, owner_business_account_id, owner_user_id,
                             hierarchy_tree)
    select id, twin_class_id, head_twin_id, external_id,
           twin_status_id, created_by_user_id, created_at,
           owner_business_account_id, owner_user_id, hierarchy_tree
    from twin
    where twin.id = old.id;

    return old;
end;
$$;

create or replace trigger twin_after_insert_wrapper_trigger
    after insert
    on twin
    for each row
execute procedure twin_after_insert_wrapper();

create or replace trigger twin_after_update_wrapper_trigger
    after update
    on twin
    for each row
execute procedure twin_after_update_wrapper();

create or replace trigger twin_before_delete_wrapper_trigger
    before delete
    on twin
    for each row
execute procedure twin_before_delete_wrapper();

drop trigger if exists hierarchyupdatetreetrigger on twin;
drop function if exists hierarchyprocesstreeupdate;

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

    insert into twin_attachment_delete_task(id, twin_attachment_id, twin_id, domain_id, twin_business_account_id, twin_created_by_user_id, storage_id, status, storage_file_key, created_at)
    values (uuid_generate_v4(), old.id, old.twin_id, dom_id, ba_id, old.created_by_user_id, old.storage_id, 'NEED_START', old.storage_file_key, now());

    return old;
end;
$$;
