
create or replace function permission_mater_space_by_permiss_grant_space_role_insert(
    p_permission_schema_id uuid,
    p_permission_id uuid,
    p_space_role_id uuid
)
    returns void
    volatile
    language plpgsql
as
$$
begin
    -- user-level materialization
    insert into permission_mater_space_user (twin_id, permission_schema_id, permission_id, user_id, grants_count)
    select
        sru.twin_id,
        p_permission_schema_id,
        p_permission_id,
        sru.user_id,
        1
    from space_role_user sru
    where sru.space_role_id = p_space_role_id
    on conflict (twin_id, permission_schema_id, permission_id, user_id)
        do update set grants_count = permission_mater_space_user.grants_count + 1;

    -- footprint-level materialization
    insert into permission_mater_space_user_group (
        user_group_footprint_registry_id,
        twin_id,
        permission_schema_id,
        permission_id,
        user_group_footprint_id,
        grants_count
    )
    select
        r.id,
        srug.twin_id,
        p_permission_schema_id,
        p_permission_id,
        r.user_group_footprint_id,
        1
    from space_role_user_group srug
             join user_group_footprint_map m
                  on m.user_group_id = srug.user_group_id
             join twin t
                  on t.id = srug.twin_id
             join twin_class tc
                  on tc.id = t.twin_class_id
             join user_group_footprint_registry r
                  on r.user_group_footprint_id = m.user_group_footprint_id
                      and r.domain_id = tc.domain_id
    where srug.space_role_id = p_space_role_id
    on conflict (twin_id, permission_schema_id, permission_id, user_group_footprint_id)
        do update set grants_count = permission_mater_space_user_group.grants_count + 1;
end;
$$;


create or replace function permission_mater_space_by_permiss_grant_space_role_delete(
    p_permission_schema_id uuid,
    p_permission_id uuid,
    p_space_role_id uuid
)
    returns void
    volatile
    language plpgsql
as
$$
begin
    -- user-level
    update permission_mater_space_user pmsu
    set grants_count = pmsu.grants_count - 1
    from space_role_user sru
    where sru.space_role_id = p_space_role_id
      and pmsu.twin_id = sru.twin_id
      and pmsu.user_id = sru.user_id
      and pmsu.permission_schema_id = p_permission_schema_id
      and pmsu.permission_id = p_permission_id;

--     delete from permission_mater_space_user
--     where grants_count <= 0;

    -- footprint-level
    update permission_mater_space_user_group pmsg
    set grants_count = pmsg.grants_count - 1
    from space_role_user_group srug
             join user_group_footprint_map m
                  on m.user_group_id = srug.user_group_id
             join twin t
                  on t.id = srug.twin_id
             join twin_class tc
                  on tc.id = t.twin_class_id
             join user_group_footprint_registry r
                  on r.user_group_footprint_id = m.user_group_footprint_id
                      and r.domain_id = tc.domain_id
    where srug.space_role_id = p_space_role_id
      and pmsg.twin_id = srug.twin_id
      and pmsg.permission_schema_id = p_permission_schema_id
      and pmsg.permission_id = p_permission_id
      and pmsg.user_group_footprint_id = r.user_group_footprint_id;

--     delete from permission_mater_space_user_group
--     where grants_count <= 0;
end;
$$;


create or replace function permission_grant_space_role_after_insert_wrapper()
returns trigger
language plpgsql
as
$$
begin
    PERFORM permission_mater_space_by_permiss_grant_space_role_insert(
        NEW.permission_schema_id,
        NEW.permission_id,
        NEW.space_role_id
    );
    return NEW;
end;
$$;


create or replace function permission_grant_space_role_after_delete_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    PERFORM permission_mater_space_by_permiss_grant_space_role_delete(
            OLD.permission_schema_id,
            OLD.permission_id,
            OLD.space_role_id
            );
    return OLD;
end;
$$;


create or replace function permission_grant_space_role_after_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if NEW.permission_schema_id is distinct from OLD.permission_schema_id
        or NEW.permission_id is distinct from OLD.permission_id
        or NEW.space_role_id is distinct from OLD.space_role_id
    then
        PERFORM permission_mater_space_by_permiss_grant_space_role_insert(
                NEW.permission_schema_id,
                NEW.permission_id,
                NEW.space_role_id
                );
        PERFORM permission_mater_space_by_permiss_grant_space_role_delete(
                OLD.permission_schema_id,
                OLD.permission_id,
                OLD.space_role_id
                );
    end if;
    return NEW;
end;
$$;

drop trigger if exists permission_grant_space_role_after_insert_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_insert_wrapper_trigger
    after insert on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_insert_wrapper();

drop trigger if exists permission_grant_space_role_after_delete_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_delete_wrapper_trigger
    after delete on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_delete_wrapper();

drop trigger if exists permission_grant_space_role_after_update_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_update_wrapper_trigger
    after update on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_update_wrapper();