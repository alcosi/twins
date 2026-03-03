create table if not exists permission_mater_space_user
(
    twin_id            uuid not null
        constraint permission_mater_space_user_twin_id_fk
            references twin
            on update cascade on delete cascade,
    permission_schema_id uuid not null
        references permission_schema
            on update cascade
            on delete cascade,
    permission_id      uuid not null
        constraint permission_mater_space_user_permission_id_fk
            references permission
            on update cascade on delete cascade,
    user_id            uuid not null
        constraint permission_mater_space_user_user_id_fk
            references "user"
            on update cascade on delete cascade,
    grants_count            int not null default 0,
    constraint permission_mater_space_user_pk
        primary key (twin_id, permission_id, permission_schema_id, user_id)
);

drop index if exists idx_permission_mater_space_user_grants_count;
create index idx_permission_mater_space_user_grants_count
    on permission_mater_space_user (grants_count);

drop index if exists idx_permission_mater_space_user_permission_schema_id;
create index idx_permission_mater_space_user_permission_schema_id
    on permission_mater_space_user (permission_schema_id);

drop index if exists idx_permission_mater_space_user_user_id;
create index idx_permission_mater_space_user_user_id
    on permission_mater_space_user (user_id);

-----------------------------------------------------------------
create or replace function permission_mater_space_user_by_space_role_user_insert(p_new_twin_id uuid, p_new_space_role_id uuid, p_new_user_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    IF current_setting('app.permission_mater_space_user_by_space_role_user_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

    insert into permission_mater_space_user (
                                             twin_id,
                                             permission_schema_id,
                                             permission_id,
                                             user_id,
                                             grants_count)
    select
        p_new_twin_id,
        pgsr.permission_schema_id,
        pgsr.permission_id,
        p_new_user_id,
        1
    from permission_grant_space_role pgsr
    where pgsr.space_role_id = p_new_space_role_id
    on conflict (twin_id, permission_id, permission_schema_id, user_id)
        do update set grants_count = permission_mater_space_user.grants_count + 1;
END;
$$;

create or replace function permission_mater_space_user_by_space_role_user_delete(p_old_twin_id uuid, p_old_space_role_id uuid, p_old_user_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    IF current_setting('app.permission_mater_space_user_by_space_role_user_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;
    update permission_mater_space_user pmsu
    set grants_count = pmsu.grants_count - 1
    from permission_grant_space_role pgsr
    where pgsr.space_role_id = p_old_space_role_id
      and pmsu.twin_id = p_old_twin_id
      and pmsu.user_id = p_old_user_id
      and pmsu.permission_schema_id = pgsr.permission_schema_id
      and pmsu.permission_id = pgsr.permission_id;

END;
$$;

-- full rematerialize permissions for space role users
create or replace function permission_mater_space_user_by_space_role_user_init(
    p_new_twin_id uuid,
    p_new_space_role_id uuid,
    p_new_user_id uuid
) returns void
    language plpgsql
as
$$
begin
    insert into permission_mater_space_user (
        twin_id,
        permission_schema_id,
        permission_id,
        user_id,
        grants_count
    )
    select
        p_new_twin_id,
        pgsr.permission_schema_id,
        pgsr.permission_id,
        p_new_user_id,
        1
    from permission_grant_space_role pgsr
    where pgsr.space_role_id = p_new_space_role_id
    on conflict (twin_id, permission_id, permission_schema_id, user_id)
        do update
        set grants_count = permission_mater_space_user.grants_count; -- не прибавляем
end;
$$;

------------------------------------------------------------------------------
create or replace function space_role_user_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM set_config('app.permission_mater_space_user_by_space_role_user_trigger', 'on', true); -- function has direct call protection
    PERFORM permission_mater_space_user_by_space_role_user_insert(NEW.twin_id, NEW.space_role_id, NEW.user_id);
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
        PERFORM set_config('app.permission_mater_space_user_by_space_role_user_trigger', 'on', true); -- function has direct call protection
        PERFORM permission_mater_space_user_by_space_role_user_insert(NEW.twin_id, NEW.space_role_id, NEW.user_id);
        PERFORM permission_mater_space_user_by_space_role_user_delete(OLD.twin_id, OLD.space_role_id, OLD.user_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function space_role_user_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM set_config('app.permission_mater_space_user_by_space_role_user_trigger', 'on', true); -- function has direct call protection
    PERFORM permission_mater_space_user_by_space_role_user_delete(OLD.twin_id, OLD.space_role_id, OLD.user_id);
    RETURN OLD;
END;
$$;

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
drop trigger if exists space_role_user_after_delete_wrapper_trigger on space_role_user;
create trigger space_role_user_after_delete_wrapper_trigger
    after delete
    on space_role_user
    for each row
execute procedure space_role_user_after_delete_wrapper();
-----------------------------------------------------------------

select permission_mater_space_user_by_space_role_user_init(twin_id, space_role_id, user_id)
from space_role_user;
