create table permission_materialization_space_level
(
    twin_id            uuid not null
        constraint permission_materialization_space_level_twin_id_fk
            references twin
            on update cascade on delete cascade,
    permission_id      uuid not null
        constraint permission_materialization_space_level_permission_id_fk
            references permission
            on update cascade on delete cascade,
    user_id            uuid not null
        constraint permission_materialization_space_level_user_id_fk
            references "user"
            on update cascade on delete cascade,
    grants_count            int not null default 0,
    constraint permission_materialization_space_level_pk
        primary key (twin_id, permission_id, user_id)
);

drop index if exists idx_permission_materialization_space_level_grants_count;
create index idx_permission_materialization_space_level_grants_count
    on permission_materialization_space_level (grants_count);

create or replace function permission_mater_space_level_by_user_group_map_insert(p_new_user_group_id uuid, p_new_user_id uuid, p_new_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    insert into permission_materialization_space_level (twin_id, permission_id, user_id, grants_count)
    select t.id, pgsr.permission_id, p_new_user_id  from space_role_user_group srug
                                                             join space s on s.twin_id = srug.id
                                                             join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
                                                             join twin t on t.owner_business_account_id = p_new_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_new_user_group_id
    on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function permission_mater_space_level_by_user_group_map_delete(p_old_user_group_id uuid, p_old_user_id uuid, p_old_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update permission_materialization_space_level pmsl set grants_count = grants_count - 1
    from space_role_user_group srug
             join space s on s.twin_id = srug.id
             join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
             join twin t on t.owner_business_account_id = p_old_business_account_id and t.id = srug.twin_id
    where pmsl.user_id = p_old_user_id and srug.user_group_id = p_old_user_group_id;
END;
$$;

-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function permission_mater_space_level_by_permiss_grant_space_role_insert(p_new_permission_schema_id uuid, p_new_permission_id uuid, p_new_space_role_id uuid) returns void
    volatile
    language plpgsql
as
$$
    -- todo check
BEGIN
    insert into permission_materialization_space_level (twin_id, permission_id, user_id, grants_count)
    select t.id, p_new_permission_id, ugm.user_id from user_group_map ugm
                                                             join space_role_user_group srug on p_new_space_role_id = srug.space_role_id and srug.user_group_id = ugm.user_group_id
                                                             join twin t on t.owner_business_account_id = ugm.business_account_id and t.id = srug.twin_id
                                                             join space s on s.twin_id = srug.id and s.permission_schema_id = p_new_permission_schema_id
    on conflict do update set grants_count = grants_count + 1;
    insert into permission_materialization_space_level (twin_id, permission_id, user_id, grants_count)
    select s.twin_id, p_new_permission_id, sru.user_id from space_role_user sru
                                                          join space s on s.twin_id = sru.id and s.permission_schema_id = p_new_permission_schema_id
                                                          where sru.space_role_id = p_new_space_role_id
    on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function permission_mater_space_level_by_permiss_grant_space_role_delete(p_old_permission_schema_id uuid, p_old_permission_id uuid, p_old_space_role_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update permission_materialization_space_level set grants_count = grants_count - 1
    from user_group_map ugm
             join space_role_user_group srug on p_old_space_role_id = srug.space_role_id and srug.user_group_id = ugm.user_group_id                                                          join space s on s.twin_id = srug.id
             join twin t on t.owner_business_account_id = ugm.business_account_id and t.id = srug.twin_id
             join space s on s.twin_id = srug.id and s.permission_schema_id = p_old_permission_schema_id
    where permission_id = p_old_permission_id;

    update permission_materialization_space_level set grants_count = grants_count - 1
    from space_role_user sru
             join space s on s.twin_id = sru.id and s.permission_schema_id = p_old_permission_schema_id
    where sru.space_role_id = p_old_space_role_id;
END;
$$;
-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function permission_mater_space_level_by_space_role_user_group_insert(p_new_twin_id uuid, p_new_space_role_id uuid, p_new_user_group_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    insert into permission_materialization_space_level (twin_id, permission_id, user_id, grants_count)
    select p_new_twin_id, pgsr.permission_id, ugm.user_id from user_group_map ugm
                                                             join space s on s.twin_id = p_new_twin_id
                                                             join permission_grant_space_role pgsr on pgsr.permission_schema_id = s.permission_schema_id and pgsr.space_role_id = p_new_space_role_id
                                                             join twin t on t.owner_business_account_id = ugm.business_account_id and t.id = p_new_twin_id
                                                         where ugm.user_group_id = p_new_user_group_id
    on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function permission_mater_space_level_by_space_role_user_group_delete(p_old_twin_id uuid, p_old_space_role_id uuid, p_old_user_group_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update permission_materialization_space_level set grants_count = grants_count - 1
    from user_group_map ugm
             join space s on s.twin_id = p_old_twin_id
             join permission_grant_space_role pgsr on pgsr.permission_schema_id = s.permission_schema_id and pgsr.space_role_id = p_old_space_role_id
             join twin t on t.owner_business_account_id = ugm.business_account_id and t.id = p_old_twin_id
    where ugm.user_group_id = p_old_user_group_id;
END;
$$;
-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function permission_mater_space_level_by_space_role_user_insert(p_new_twin_id uuid, p_new_space_role_id uuid, p_new_user_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    insert into permission_materialization_space_level (twin_id, permission_id, user_id, grants_count)
    select p_new_twin_id, pgsr.permission_id, p_new_user_id from space s
                                                            join permission_grant_space_role pgsr on pgsr.permission_schema_id = s.permission_schema_id and pgsr.space_role_id = p_new_space_role_id
                                                            where s.twin_id = p_new_twin_id
    on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function permission_mater_space_level_by_space_role_user_delete(p_old_twin_id uuid, p_old_space_role_id uuid, p_old_user_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update permission_materialization_space_level pmsl set grants_count = grants_count - 1
    from space s
             join permission_grant_space_role pgsr on pgsr.permission_schema_id = s.permission_schema_id and pgsr.space_role_id = p_old_space_role_id
    where s.twin_id = p_old_twin_id and pmsl.user_id = p_old_user_id;
END;
$$;
-----------------------------------------------------------------


create or replace function permission_grant_space_role_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_permiss_grant_space_role_insert(NEW.permission_schema_id, NEW.permission_id, NEW.space_role_id);
    RETURN NEW;
END;
$$;

create or replace function permission_grant_space_role_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS DISTINCT FROM OLD.permission_schema_id OR
       NEW.permission_id IS DISTINCT FROM OLD.permission_id OR
       NEW.space_role_id IS DISTINCT FROM OLD.space_role_id
    THEN
        PERFORM permission_mater_space_level_by_permiss_grant_space_role_insert(NEW.permission_schema_id, NEW.permission_id, NEW.space_role_id);
        PERFORM permission_mater_space_level_by_permiss_grant_space_role_delete(OLD.permission_schema_id, OLD.permission_id, OLD.space_role_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function permission_grant_space_role_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_permiss_grant_space_role_delete(OLD.permission_schema_id, OLD.permission_id, OLD.space_role_id);
    RETURN OLD;
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
------------------------------------------------------------------------------
create or replace function space_role_user_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_space_role_user_insert(NEW.twin_id, NEW.space_role_id, NEW.user_id);
    RETURN NEW;
END;
$$;

create or replace function space_role_user_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.twin_id IS DISTINCT FROM OLD.twin_id OR
       NEW.user_id IS DISTINCT FROM OLD.user_id OR
       NEW.space_role_id IS DISTINCT FROM OLD.space_role_id
    THEN
        PERFORM permission_mater_space_level_by_space_role_user_insert(NEW.twin_id, NEW.space_role_id, NEW.user_id);
        PERFORM permission_mater_space_level_by_space_role_user_delete(OLD.twin_id, OLD.space_role_id, OLD.user_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function space_role_user_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_space_role_user_delete(OLD.twin_id, OLD.space_role_id, OLD.user_id);
    RETURN OLD;
END;
$$;
------------------------------------------------------------------------------
create or replace function user_group_map_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_user_group_map_insert(NEW.user_id, NEW.user_group_id, NEW.business_account_id);
    RETURN NEW;
END;
$$;


create or replace function user_group_map_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    --todo
    IF NEW.user_id IS DISTINCT FROM OLD.user_id OR
       NEW.user_group_id IS DISTINCT FROM OLD.user_group_id
    THEN
        PERFORM permission_mater_space_level_by_user_group_map_insert(NEW.user_id, NEW.user_group_id, NEW.business_account_id);
        PERFORM permission_mater_space_level_by_user_group_map_delete(OLD.user_id, OLD.user_group_id, OLD.business_account_id);
    END IF;
    RETURN NEW;
END;
$$;

create or replace function user_group_map_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM permission_mater_space_level_by_user_group_map_delete(OLD.user_id, OLD.user_group_id, OLD.business_account_id);
    RETURN OLD;
END;
$$;

create or replace function user_group_map_before_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    if new.involves_counter is distinct from old.involves_counter and new.added_manually is distinct from old.added_manually then
        raise exception 'You cant change added_manually & involves_counter fields both together.';
    end if;
    PERFORM user_group_map_validate_domain_and_business_account(NEW);
    RETURN NEW;
END;
$$;

create or replace function user_group_map_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM user_group_map_validate_domain_and_business_account(NEW);
    if NEW.involves_counter = -9999 then
        NEW.added_manually = FALSE;
        NEW.involves_counter = 1;
    else
        NEW.added_manually = true;
    end if;
    RETURN NEW;
END;
$$;

----------------------------------------------------------------------------------
drop trigger if exists permission_grant_space_role_after_delete_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_delete_wrapper_trigger
    after delete
    on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_delete_wrapper();
drop trigger if exists permission_grant_space_role_after_insert_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_insert_wrapper_trigger
    after insert
    on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_insert_wrapper();
drop trigger if exists permission_grant_space_role_after_update_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_update_wrapper_trigger
    after update
    on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_update_wrapper();
--------------------------------------------------
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
-----------------------------------------------------------------
drop trigger if exists space_role_user_after_delete_wrapper_trigger on space_role_user;
create trigger space_role_user_after_delete_wrapper_trigger
    after delete
    on space_role_user
    for each row
execute procedure space_role_user_after_delete_wrapper();
drop trigger if exists space_role_user_after_insert_wrapper_trigger on space_role_user;
create trigger space_role_user_after_insert_wrapper_trigger
    after insert
    on space_role_user
    for each row
execute procedure space_role_user_after_insert_wrapper();
drop trigger if exists space_role_user_after_update_wrapper_trigger on space_role_user;
create trigger space_role_user_after_update_wrapper_trigger
    after update
    on space_role_user
    for each row
execute procedure space_role_user_after_update_wrapper();
-----------------------------------------------------------------
drop trigger if exists user_group_map_after_delete_wrapper_trigger on user_group_map;
create trigger user_group_map_after_delete_wrapper_trigger
    after delete
    on user_group_map
    for each row
execute procedure user_group_map_after_delete_wrapper();
drop trigger if exists user_group_map_after_insert_wrapper_trigger on user_group_map;
create trigger user_group_map_after_insert_wrapper_trigger
    after insert
    on user_group_map
    for each row
execute procedure user_group_map_after_insert_wrapper();
drop trigger if exists user_group_map_after_update_wrapper_trigger on user_group_map;
create trigger user_group_map_after_update_wrapper_trigger
    after update
    on user_group_map
    for each row
execute procedure user_group_map_after_update_wrapper();
drop trigger if exists user_group_map_before_insert_wrapper_trigger on user_group_map;
create trigger user_group_map_before_insert_wrapper_trigger
    before insert
    on user_group_map
    for each row
execute procedure user_group_map_before_insert_wrapper();
drop trigger if exists user_group_map_before_update_wrapper_trigger on user_group_map;
create trigger user_group_map_before_update_wrapper_trigger
    before update
    on user_group_map
    for each row
execute procedure user_group_map_before_update_wrapper();

select permission_mater_space_level_by_user_group_map_insert(t2.user_group_id, t2.user_id, t2.business_account_id)
from user_group_map_type2 t2;
select permission_mater_space_level_by_permiss_grant_space_role_insert(permission_schema_id, permission_id, space_role_id)
from permission_grant_space_role;
select permission_mater_space_level_by_space_role_user_group_insert(twin_id, space_role_id, user_group_id)
from space_role_user_group;
select permission_mater_space_level_by_space_role_user_insert(twin_id, space_role_id, user_id)
from space_role_user;
