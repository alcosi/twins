create table permission_materialization_schema_level
(
    permission_schema_id            uuid not null
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
        primary key (permission_schema_id, permission_id, user_id)
);

drop index if exists idx_permission_materialization_space_level_grants_count;
create index idx_permission_materialization_space_level_grants_count
    on permission_materialization_schema_level (grants_count);


create or replace function permission_mater_schema_by_user_insert(
    p_schema_id uuid,
    p_permission_id uuid,
    p_user_id uuid
) returns void
    language plpgsql
as
$$
begin
    insert into permission_materialization_schema_level
    (permission_schema_id, permission_id, user_id, grants_count)
    values
        (p_schema_id, p_permission_id, p_user_id, 1)
    on conflict (permission_schema_id, permission_id, user_id)
        do update set grants_count =
                          permission_materialization_schema_level.grants_count + 1;
end;
$$;


create or replace function permission_mater_schema_by_user_delete(
    p_schema_id uuid,
    p_permission_id uuid,
    p_user_id uuid
) returns void
    language plpgsql
as
$$
begin
    update permission_materialization_schema_level
    set grants_count = grants_count - 1
    where permission_schema_id = p_schema_id
      and permission_id = p_permission_id
      and user_id = p_user_id;
end;
$$;


create or replace function permission_grant_user_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_schema_by_user_insert(
            new.permission_schema_id,
            new.permission_id,
            new.user_id
            );
    return new;
end;
$$;


create or replace function permission_grant_user_after_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if new.permission_schema_id is distinct from old.permission_schema_id
        or new.permission_id is distinct from old.permission_id
        or new.user_id is distinct from old.user_id
    then
        perform permission_mater_schema_by_user_insert(
                new.permission_schema_id,
                new.permission_id,
                new.user_id
                );

        perform permission_mater_schema_by_user_delete(
                old.permission_schema_id,
                old.permission_id,
                old.user_id
                );
    end if;

    return new;
end;
$$;

create or replace function permission_grant_user_after_delete_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_schema_by_user_delete(
            old.permission_schema_id,
            old.permission_id,
            old.user_id
            );
    return old;
end;
$$;


create or replace function permission_mater_schema_by_user_group_insert(
    p_schema_id uuid,
    p_permission_id uuid,
    p_user_group_id uuid
) returns void
    language plpgsql
as
$$
begin
    insert into permission_materialization_schema_level
    (permission_schema_id, permission_id, user_id, grants_count)
    select
        p_schema_id,
        p_permission_id,
        ugm.user_id,
        1
    from user_group_map ugm
    where ugm.user_group_id = p_user_group_id
    on conflict (permission_schema_id, permission_id, user_id)
        do update set grants_count =
                          permission_materialization_schema_level.grants_count + 1;
end;
$$;

create or replace function permission_mater_schema_by_user_group_delete(
    p_schema_id uuid,
    p_permission_id uuid,
    p_user_group_id uuid
) returns void
    language plpgsql
as
$$
begin
    update permission_materialization_schema_level pmsl
    set grants_count = grants_count - 1
    from user_group_map ugm
    where ugm.user_group_id = p_user_group_id
      and pmsl.user_id = ugm.user_id
      and pmsl.permission_schema_id = p_schema_id
      and pmsl.permission_id = p_permission_id;
end;
$$;

create or replace function permission_grant_user_group_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_schema_by_user_group_insert(
            new.permission_schema_id,
            new.permission_id,
            new.user_group_id
            );
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
        perform permission_mater_schema_by_user_group_insert(
                new.permission_schema_id,
                new.permission_id,
                new.user_group_id
                );

        perform permission_mater_schema_by_user_group_delete(
                old.permission_schema_id,
                old.permission_id,
                old.user_group_id
                );
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
    perform permission_mater_schema_by_user_group_delete(
            old.permission_schema_id,
            old.permission_id,
            old.user_group_id
            );
    return old;
end;
$$;

create or replace function permission_mater_schema_by_global_insert(
    p_permission_id uuid,
    p_user_group_id uuid
) returns void
    language plpgsql
as
$$
begin
    insert into permission_materialization_schema_level
    (permission_schema_id, permission_id, user_id, grants_count)
    select
        d.permission_schema_id,
        p_permission_id,
        ugm.user_id,
        1
    from user_group_map ugm
             join domain d
                  on d.id = ugm.domain_id
    where ugm.user_group_id = p_user_group_id
      and d.permission_schema_id is not null
    on conflict (permission_schema_id, permission_id, user_id)
        do update set grants_count =
                          permission_materialization_schema_level.grants_count + 1;
end;
$$;

create or replace function permission_mater_schema_by_global_delete(
    p_permission_id uuid,
    p_user_group_id uuid
) returns void
    language plpgsql
as
$$
begin
    update permission_materialization_schema_level pmsl
    set grants_count = grants_count - 1
    from user_group_map ugm
             join domain d
                  on d.id = ugm.domain_id
    where ugm.user_group_id = p_user_group_id
      and pmsl.user_id = ugm.user_id
      and pmsl.permission_schema_id = d.permission_schema_id
      and pmsl.permission_id = p_permission_id;
end;
$$;

create or replace function permission_grant_global_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform permission_mater_schema_by_global_insert(
            new.permission_id,
            new.user_group_id
            );
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
        perform permission_mater_schema_by_global_insert(
                new.permission_id,
                new.user_group_id
                );

        perform permission_mater_schema_by_global_delete(
                old.permission_id,
                old.user_group_id
                );
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
    perform permission_mater_schema_by_global_delete(
            old.permission_id,
            old.user_group_id
            );
    return old;
end;
$$;


todo user_group_map