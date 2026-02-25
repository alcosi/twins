create table if not exists permission_mater_user_group
(
    user_group_footprint_registry_id uuid not null
        references user_group_footprint_registry(id)
            on delete cascade,
    permission_schema_id uuid not null
        references permission_schema
            on update cascade
            on delete cascade,
    permission_id uuid not null
        references permission
            on update cascade
            on delete cascade,
    user_group_footprint_id uuid not null
        references user_group_footprint
            on update cascade
            on delete cascade,
    grants_count int not null default 0,
    primary key (
                 permission_schema_id,
                 permission_id,
                 user_group_footprint_id
        )
);


create or replace function permission_mater_user_group_init(
    p_footprint uuid,
    p_domain_id uuid,
    p_business_account_id uuid
)
    returns void
    language plpgsql
as
$$
declare
    v_registry_id uuid;
    v_lock_key bigint;
begin
    -- 0 Compute advisory lock key from domain + business_account + footprint
    v_lock_key := hashtext('permission_mater_user_group_init- ' || p_domain_id::text || '-' || coalesce(p_business_account_id::text,'') || '-' || p_footprint::text)::bigint;

    -- 1 Acquire session-level advisory lock
    perform pg_advisory_lock(v_lock_key);

    -- 2 Insert footprint into registry if not exists
    insert into user_group_footprint_registry(id, domain_id, business_account_id, user_group_footprint_id)
    values (gen_random_uuid(), p_domain_id, p_business_account_id, p_footprint)
    on conflict (domain_id, business_account_id, user_group_footprint_id) do nothing;

    -- Retrieve registry_id
    select id into v_registry_id
    from user_group_footprint_registry
    where domain_id = p_domain_id
      and business_account_id is not distinct from p_business_account_id
      and user_group_footprint_id = p_footprint;

    -- 3 Materialize permissions from user_group grants (join with footprint map)
    insert into permission_mater_user_group(
        user_group_footprint_registry_id,
        permission_schema_id,
        permission_id,
        user_group_footprint_id,
        grants_count
    )
    select
        v_registry_id,
        s.id,
        g.permission_id,
        p_footprint,
        count(*) as grt_count
    from permission_schema s
             join permission_grant_user_group g
                  on g.permission_schema_id = s.id
             join user_group_footprint_map m
                  on m.user_group_footprint_id = p_footprint
                      and m.user_group_id = g.user_group_id
    where s.domain_id = p_domain_id
      and (s.business_account_id is null or s.business_account_id = p_business_account_id)
    group by
        v_registry_id,
        s.id,
        g.permission_id,
        p_footprint
    on conflict (permission_schema_id, permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_mater_user_group.grants_count + excluded.grants_count;


    -- 4 Release advisory lock
    perform pg_advisory_unlock(v_lock_key);

end;
$$;


create or replace function permission_mater_schema_level_invalidate_domain(p_domain_id uuid) returns void
    language plpgsql
as
$$
begin
    delete from user_group_footprint_registry
    where domain_id = p_domain_id;
end;
$$;

create or replace function permission_mater_schema_level_invalidate_domain(p_domain_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin
    if p_domain_id is not null then
        delete
        from user_group_footprint_registry ugfr
            using user_group_footprint_map umf
        where umf.user_group_footprint_id = ugfr.user_group_footprint_id
          and umf.user_group_id = p_user_group_id
          and domain_id = p_domain_id;
    else
        delete
        from user_group_footprint_registry ugfr
            using user_group_footprint_map umf
        where umf.user_group_footprint_id = ugfr.user_group_footprint_id
          and umf.user_group_id = p_user_group_id
          and domain_id = p_domain_id;
    end if;
end;
$$;

create or replace function permission_mater_schema_level_by_perm_grant_user_group_insert(
    p_schema_id uuid,
    p_permission_id uuid,
    p_user_group_id uuid
)
    returns void
    language plpgsql
as
$$
begin
    insert into permission_mater_user_group (
        user_group_footprint_registry_id,
        permission_schema_id,
        permission_id,
        user_group_footprint_id,
        grants_count
    )
    select
        r.id,
        p_schema_id,
        p_permission_id,
        r.user_group_footprint_id,
        1
    from user_group_footprint_map m
             join user_group_footprint_registry r
                  on r.user_group_footprint_id = m.user_group_footprint_id
    where m.user_group_id = p_user_group_id
    on conflict (permission_schema_id, permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_mater_user_group.grants_count + 1;
end;
$$;

create or replace function permission_mater_schema_level_by_perm_grant_user_group_delete(
    p_schema_id uuid,
    p_permission_id uuid,
    p_user_group_id uuid
)
    returns void
    language plpgsql
as
$$
begin
    -- 1 Decrement grants_count for all footprints containing the user group
    update permission_mater_user_group pmsl
    set grants_count = pmsl.grants_count - 1
    from user_group_footprint_map m
             join user_group_footprint_registry r
                  on r.user_group_footprint_id = m.user_group_footprint_id
    where pmsl.permission_schema_id = p_schema_id
      and pmsl.permission_id = p_permission_id
      and pmsl.user_group_footprint_id = r.user_group_footprint_id
      and pmsl.user_group_footprint_registry_id = r.id
      and m.user_group_id = p_user_group_id;

--     -- 2 Remove rows where grants_count <= 0 commented for debugging. <0 will indicated that some bug is present
--     delete from permission_mater_user_group
--     where permission_schema_id = p_schema_id
--       and permission_id = p_permission_id
--       and grants_count <= 0;

end;
$$;


create or replace function permission_grant_user_group_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_schema_level_by_perm_grant_user_group_insert(new.permission_schema_id, new.permission_id, new.user_group_id);
    return new;
end;
$$;

create or replace function permission_grant_user_group_after_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if new.permission_schema_id is distinct from old.permission_schema_id
        or new.permission_id is distinct from old.permission_id
        or new.user_group_id is distinct from old.user_group_id
    then
        perform permission_mater_schema_level_by_perm_grant_user_group_insert(new.permission_schema_id, new.permission_id, new.user_group_id);
        perform permission_mater_schema_level_by_perm_grant_user_group_delete(old.permission_schema_id, old.permission_id, old.user_group_id);
    end if;
    return new;
end;
$$;

create or replace function permission_grant_user_group_after_delete_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_schema_level_by_perm_grant_user_group_delete(old.permission_schema_id, old.permission_id, old.user_group_id);
    return old;
end;
$$;


drop trigger if exists permission_grant_user_group_after_insert_wrapper_trigger on permission_grant_user_group;
create trigger permission_grant_user_group_after_insert_wrapper_trigger
    after insert on permission_grant_user_group
    for each row
execute function permission_grant_user_group_after_insert_wrapper();
drop trigger if exists permission_grant_user_group_after_update_wrapper_trigger on permission_grant_user_group;
create trigger permission_grant_user_group_after_update_wrapper_trigger
    after update on permission_grant_user_group
    for each row
execute function permission_grant_user_group_after_update_wrapper();
drop trigger if exists permission_grant_user_group_after_delete_wrapper_trigger on permission_grant_user_group;
create trigger permission_grant_user_group_after_delete_wrapper_trigger
    after delete on permission_grant_user_group
    for each row
execute function permission_grant_user_group_after_delete_wrapper();
