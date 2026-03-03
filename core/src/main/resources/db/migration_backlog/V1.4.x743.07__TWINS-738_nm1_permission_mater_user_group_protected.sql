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
    IF current_setting('app.permission_mater_space_user_by_perm_grant_user_group_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

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
    IF current_setting('app.permission_mater_space_user_by_perm_grant_user_group_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

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
    perform set_config('app.permission_mater_space_user_by_perm_grant_user_group_trigger', 'on', true); -- function has direct call protection
    perform permission_mater_schema_level_by_perm_grant_user_group_insert(new.permission_schema_id, new.permission_id, new.user_group_id);
    PERFORM set_config('app.permission_mater_space_user_by_perm_grant_user_group_trigger', 'off', true);
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
        perform set_config('app.permission_mater_space_user_by_perm_grant_user_group_trigger', 'on', true); -- function has direct call protection
        perform permission_mater_schema_level_by_perm_grant_user_group_insert(new.permission_schema_id, new.permission_id, new.user_group_id);
        perform permission_mater_schema_level_by_perm_grant_user_group_delete(old.permission_schema_id, old.permission_id, old.user_group_id);
        PERFORM set_config('app.permission_mater_space_user_by_perm_grant_user_group_trigger', 'off', true);
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
    perform set_config('app.permission_mater_space_user_by_perm_grant_user_group_trigger', 'on', true); -- function has direct call protection
    perform permission_mater_schema_level_by_perm_grant_user_group_delete(old.permission_schema_id, old.permission_id, old.user_group_id);
    PERFORM set_config('app.permission_mater_space_user_by_perm_grant_user_group_trigger', 'off', true);
    return old;
end;
$$;
