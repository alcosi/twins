create table permission_mater_global
(
    permission_id           uuid   not null
        references permission
            on update cascade
            on delete cascade,
    user_group_footprint_id uuid   not null
        references user_group_footprint
            on update cascade
            on delete cascade,
    grants_count            bigint not null,
    primary key (permission_id, user_group_footprint_id)
);

create or replace function permission_mater_global_init(
    p_footprint uuid
)
    returns void
    language plpgsql
as
$$
declare
    v_lock_key bigint;
begin
    -- 0 Compute advisory lock key from domain + business_account + footprint
    v_lock_key := hashtext('permission_mater_global_init-' || p_footprint::text)::bigint;

    -- 1 Acquire session-level advisory lock
    perform pg_advisory_lock(v_lock_key);

    -- 2 Materialize permissions from global grants (join with footprint map)
    insert into permission_mater_global(
        permission_id,
        user_group_footprint_id,
        grants_count
    )
    select
        g.permission_id,
        p_footprint,
        count(*) as grt_count
    from permission_grant_global g
             join user_group_footprint_map m
                  on g.user_group_id = m.user_group_id and m.user_group_id = p_footprint
    group by
        g.permission_id,
        p_footprint
    on conflict (permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_mater_global.grants_count + excluded.grants_count;

    -- 3 Release advisory lock
    perform pg_advisory_unlock(v_lock_key);

end;
$$;


create or replace function permission_mater_global_by_perm_grant_global_insert(p_permission_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin

end;
$$;

create or replace function permission_mater_global_by_perm_grant_global_delete(p_permission_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin

end;
$$;


create or replace function permission_grant_global_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_global_by_perm_grant_global_insert(new.permission_id,new.user_group_id);
    return new;
end;
$$;

create or replace function permission_grant_global_after_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if new.permission_id is distinct from old.permission_id
        or new.user_group_id is distinct from old.user_group_id
    then
        perform permission_mater_global_by_perm_grant_global_insert(new.permission_id, new.user_group_id);
        perform permission_mater_global_by_perm_grant_global_delete(old.permission_id, old.user_group_id);
    end if;

    return new;
end;
$$;

create or replace function permission_grant_global_after_delete_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_global_by_perm_grant_global_delete(old.permission_id, old.user_group_id);
    return old;
end;
$$;


drop trigger if exists permission_grant_global_after_insert_wrapper_trigger on permission_grant_global;
create trigger permission_grant_global_after_insert_wrapper_trigger
    after insert on permission_grant_global
    for each row
execute function permission_grant_global_after_insert_wrapper();
drop trigger if exists permission_grant_global_after_update_wrapper_trigger on permission_grant_global;
create trigger permission_grant_global_after_update_wrapper_trigger
    after update on permission_grant_global
    for each row
execute function permission_grant_global_after_update_wrapper();
drop trigger if exists permission_grant_global_after_delete_wrapper_trigger on permission_grant_global;
create trigger permission_grant_global_after_delete_wrapper_trigger
    after delete on permission_grant_global
    for each row
execute function permission_grant_global_after_delete_wrapper();