create table if not exists permission_mater_space_user_group
(
    user_group_footprint_registry_id uuid not null
        references user_group_footprint_registry(id)
            on delete cascade,
    twin_id            uuid not null
        constraint permission_mater_space_user_group_twin_id_fk
            references twin
            on update cascade
            on delete cascade,
    permission_schema_id uuid not null
        references permission_schema
            on update cascade
            on delete cascade,
    permission_id      uuid not null
        constraint permission_mater_space_user_group_permission_id_fk
            references permission
            on update cascade
            on delete cascade,
    user_group_footprint_id            uuid not null
        constraint permission_mater_space_user_group_user_id_fk
            references user_group_footprint
            on update cascade on delete cascade,
    grants_count            int not null default 0,
    constraint permission_mater_space_user_group_pk
        primary key (twin_id, permission_schema_id, permission_id, user_group_footprint_id)
);


drop index if exists idx_permission_mater_space_user_group_grants_count;
create index idx_permission_mater_space_user_group_grants_count
    on permission_mater_space_user_group (grants_count);



-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function permission_mater_space_level_by_space_role_user_group_insert(
    p_new_twin_id uuid,
    p_new_space_role_id uuid,
    p_new_user_group_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
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
        p_new_twin_id,
        pgsr.permission_schema_id,
        pgsr.permission_id,
        r.user_group_footprint_id,
        1
    from twin t
             join twin_class tc
                  on tc.id = t.twin_class_id
             join user_group_footprint_map m
                  on m.user_group_id = p_new_user_group_id
             join user_group_footprint_registry r
                  on r.user_group_footprint_id = m.user_group_footprint_id
                      and r.domain_id = tc.domain_id  -- that is why twin_class domain change should be restricted
             join permission_grant_space_role pgsr
                  on pgsr.space_role_id = p_new_space_role_id
    where t.id = p_new_twin_id
    on conflict (twin_id, permission_schema_id, permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_mater_space_user_group.grants_count + 1;
END;
$$;

create or replace function permission_mater_space_level_by_space_role_user_group_delete(p_old_twin_id uuid, p_old_space_role_id uuid, p_old_user_group_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update permission_mater_space_user_group pmsg
    set grants_count = pmsg.grants_count - 1
    from twin t
             join twin_class tc
                  on tc.id = t.twin_class_id
             join user_group_footprint_map m
                  on m.user_group_id = p_old_user_group_id
             join user_group_footprint_registry r
                  on r.user_group_footprint_id = m.user_group_footprint_id
                      and r.domain_id = tc.domain_id
             join permission_grant_space_role pgsr
                  on pgsr.space_role_id = p_old_space_role_id
    where t.id = p_old_twin_id
      and pmsg.twin_id = p_old_twin_id
      and pmsg.permission_schema_id = pgsr.permission_schema_id
      and pmsg.permission_id = pgsr.permission_id
      and pmsg.user_group_footprint_id = r.user_group_footprint_id;
END;
$$;


------------------------------------------------------------------------------

create or replace function space_role_user_group_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_space_role_user_group_insert(NEW.twin_id, NEW.space_role_id, NEW.user_group_id);
    RETURN NEW;
END;
$$;

create or replace function space_role_user_group_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.twin_id IS DISTINCT FROM OLD.twin_id OR
       NEW.user_group_id IS DISTINCT FROM OLD.user_group_id OR
       NEW.space_role_id IS DISTINCT FROM OLD.space_role_id
    THEN
        PERFORM permission_mater_space_level_by_space_role_user_group_insert(NEW.twin_id, NEW.space_role_id, NEW.user_group_id);
        PERFORM permission_mater_space_level_by_space_role_user_group_delete(OLD.twin_id, OLD.space_role_id, OLD.user_group_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function space_role_user_group_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_space_role_user_group_delete(OLD.twin_id, OLD.space_role_id, OLD.user_group_id);
    RETURN OLD;
END;
$$;


drop trigger if exists space_role_user_group_after_delete_wrapper_trigger on space_role_user_group;
create trigger space_role_user_group_after_delete_wrapper_trigger
    after delete
    on space_role_user_group
    for each row
execute procedure space_role_user_group_after_delete_wrapper();
drop trigger if exists space_role_user_group_after_insert_wrapper_trigger on space_role_user_group;
create trigger space_role_user_group_after_insert_wrapper_trigger
    after insert
    on space_role_user_group
    for each row
execute procedure space_role_user_group_after_insert_wrapper();
drop trigger if exists space_role_user_group_after_update_wrapper_trigger on space_role_user_group;
create trigger space_role_user_group_after_update_wrapper_trigger
    after update
    on space_role_user_group
    for each row
execute procedure space_role_user_group_after_update_wrapper();


select permission_mater_space_level_by_space_role_user_group_insert(twin_id, space_role_id, user_group_id)
from space_role_user_group;

