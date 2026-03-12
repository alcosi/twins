create or replace function permission_mater_global_by_perm_grant_global_insert(p_permission_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin
    IF current_setting('app.permission_mater_space_user_by_perm_grant_global_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

    insert into permission_mater_global(
        permission_id,
        user_group_footprint_id,
        grants_count
    )
    select
        p_permission_id,
        m.user_group_footprint_id,
        1
    from user_group_footprint_map m
    where m.user_group_id = p_user_group_id
    on conflict (permission_id, user_group_footprint_id)
        do update
        set grants_count =
                permission_mater_global.grants_count
                    + excluded.grants_count;
end;
$$;

create or replace function permission_mater_global_by_perm_grant_global_delete(p_permission_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin
    IF current_setting('app.permission_mater_space_user_by_perm_grant_global_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

    -- 1. Decrement grants_count for all affected footprints
    update permission_mater_global pgm
    set grants_count = pgm.grants_count - 1
    from user_group_footprint_map m
    where pgm.permission_id = p_permission_id
      and pgm.user_group_footprint_id = m.user_group_footprint_id
      and m.user_group_id = p_user_group_id;

    -- 2. Remove rows where counter dropped to zero
--     delete from permission_mater_global
--     where permission_id = p_permission_id
--       and grants_count <= 0;
end;
$$;


create or replace function permission_grant_global_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform set_config('app.permission_mater_space_user_by_perm_grant_global_trigger', 'on', true); -- function has direct call protection
    perform permission_mater_global_by_perm_grant_global_insert(new.permission_id,new.user_group_id);
    perform set_config('app.permission_mater_space_user_by_perm_grant_global_trigger', 'off', true);
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
        perform set_config('app.permission_mater_space_user_by_perm_grant_global_trigger', 'on', true); -- function has direct call protection
        perform permission_mater_global_by_perm_grant_global_insert(new.permission_id, new.user_group_id);
        perform permission_mater_global_by_perm_grant_global_delete(old.permission_id, old.user_group_id);
        perform set_config('app.permission_mater_space_user_by_perm_grant_global_trigger', 'off', true);
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
    perform set_config('app.permission_mater_space_user_by_perm_grant_global_trigger', 'on', true); -- function has direct call protection
    perform permission_mater_global_by_perm_grant_global_delete(old.permission_id, old.user_group_id);
    perform set_config('app.permission_mater_space_user_by_perm_grant_global_trigger', 'off', true);
    return old;
end;
$$;