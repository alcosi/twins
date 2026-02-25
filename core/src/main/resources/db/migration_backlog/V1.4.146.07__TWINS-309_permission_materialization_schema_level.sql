create table if not exists user_group_footprint
(
    id uuid primary key
);

create table if not exists user_group_footprint_map
(
    user_group_footprint_id uuid not null,
    user_group_id           uuid not null,

    primary key (user_group_footprint_id, user_group_id),

    constraint fk_footprint
        foreign key (user_group_footprint_id)
            references user_group_footprint (id)
            on update restrict
            on delete cascade,

    constraint fk_user_group
        foreign key (user_group_id)
            references user_group (id)
            on update cascade
            on delete cascade
);

create table if not exists user_group_footprint_registry
(
    id uuid primary key,

    domain_id uuid not null
        references domain
            on update cascade
            on delete cascade,

    business_account_id uuid null
        references business_account
            on update cascade
            on delete cascade,

    user_group_footprint_id uuid not null
        references user_group_footprint
            on update cascade
            on delete cascade,

    unique (domain_id, business_account_id, user_group_footprint_id)
);

create table if not exists permission_materialization_schema_level
(
    user_group_footprint_registry_id uuid not null
        references user_group_footprint_registry(id)
            on delete cascade,
    permission_schema_id uuid not null
        references permission_schema
            on update cascade on delete cascade,
    permission_id uuid not null
        references permission
            on update cascade on delete cascade,
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


create or replace function user_group_footprint_generate(p_group_ids uuid[])
    returns uuid
    language sql
    immutable
as
$$
select md5(array_to_string(array_agg(g order by g), ','))::uuid
from unnest(p_group_ids) as g;
$$;

create or replace function user_group_footprint_map_before_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if old.user_group_footprint_id <> new.user_group_footprint_id then
        raise exception 'Cannot change user_group_footprint_id: footprint is immutable';
    end if;
    return new;
end;
$$;

drop trigger if exists user_group_footprint_map_before_update_wrapper_trigger on user_group_footprint_map;
create trigger user_group_footprint_map_before_update_wrapper_trigger
    before update on user_group_footprint_map
    for each row
execute procedure user_group_footprint_map_before_update_wrapper();

create or replace function user_group_footprint_invalidate(p_footprint_id uuid)
    returns void
    language plpgsql
as
$$
begin
    delete from user_group_footprint_map
    where user_group_footprint_id = p_footprint_id;

    delete from user_group_footprint
    where id = p_footprint_id;
end;
$$;

create or replace function user_group_footprint_invalidate_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform user_group_footprint_invalidate(old.user_group_footprint_id);
    return old;
end;
$$;

create trigger user_group_footprint_map_after_delete_wrapper_trigger
    after delete on user_group_footprint_map
    for each row
execute function user_group_footprint_invalidate_wrapper();

create trigger user_group_footprint_map_after_update_wrapper_trigger
    after update on user_group_footprint_map
    for each row
execute function user_group_footprint_invalidate_wrapper();


create or replace function user_group_footprint_create(p_group_ids uuid[])
    returns uuid
    language plpgsql
    volatile
as
$$
declare
    v_hash uuid;
    v_exists boolean;
begin
    -- 1 Вычисляем footprint ID через immutable функцию
    v_hash := user_group_footprint_generate(p_group_ids);

    -- 2 Проверяем, существует ли уже footprint
    select exists(select 1 from user_group_footprint where id = v_hash)
    into v_exists;

    if not v_exists then
        -- 3 Вставляем footprint
        insert into user_group_footprint(id)
        values (v_hash);

        -- 4 Вставляем map для всех групп
        insert into user_group_footprint_map(user_group_footprint_id, user_group_id)
        select v_hash, g
        from unnest(p_group_ids) as g;
    end if;

    -- 5 Возвращаем ID footprint
    return v_hash;
end;
$$;

create or replace function lazy_materialize_footprint(
    p_footprint uuid,
    p_domain_id uuid,
    p_business_account_id uuid,
    p_group_ids uuid[],
    force_clean boolean
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
    -- Convert UUIDs to bigint using hashtext
    v_lock_key := hashtext(p_domain_id::text || '-' || coalesce(p_business_account_id::text,'') || '-' || p_footprint::text)::bigint;

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

    -- 3 Materialize permissions from user_group grants
    insert into permission_materialization_schema_level(
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
        1 as grants_count
    from permission_schema s
             join permission_grant_user_group g
                  on g.permission_schema_id = s.id
                      and g.user_group_id = any(p_group_ids)
    where s.domain_id = p_domain_id
      and (s.business_account_id is null or s.business_account_id = p_business_account_id)
    on conflict (permission_schema_id, permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_materialization_schema_level.grants_count + 1;

    -- 4 Materialize permissions from global grants
    insert into permission_materialization_schema_level(
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
        1 as grants_count
    from permission_schema s
             join permission_grant_global g
                  on g.user_group_id = any(p_group_ids)
    where s.domain_id = p_domain_id
      and (s.business_account_id is null or s.business_account_id = p_business_account_id)
    on conflict (permission_schema_id, permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_materialization_schema_level.grants_count + 1;

    -- 5 Release advisory lock
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

create or replace function permission_mater_schema_level_by_perm_grant_user_group_insert(p_schema_id uuid, p_permission_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin
    insert into permission_materialization_schema_level (permission_schema_id, permission_id, user_id, business_account_id, grants_count)
    select p_schema_id, p_permission_id, ugm.user_id,ugm.business_account_id, 1
    from user_group_map ugm
    where ugm.user_group_id = p_user_group_id
    on conflict (permission_schema_id, permission_id, user_id, business_account_id) do update set grants_count = grants_count + 1;
end;
$$;

create or replace function permission_mater_schema_level_by_perm_grant_user_group_delete(p_schema_id uuid, p_permission_id uuid, p_user_group_id uuid) returns void
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

create or replace function permission_mater_schema_level_by_perm_grant_global_insert(p_permission_id uuid, p_user_group_id uuid) returns void
    language plpgsql
as
$$
begin
    insert into permission_materialization_schema_level (permission_schema_id, permission_id, user_id, grants_count)
    select d.permission_schema_id, p_permission_id, ugm.user_id, 1
    from user_group_map ugm
             join domain d on d.id = ugm.domain_id
    where ugm.user_group_id = p_user_group_id
      and d.permission_schema_id is not null
    on conflict (permission_schema_id, permission_id, user_id) do update set grants_count = grants_count + 1;
end;
$$;

create or replace function permission_mater_schema_level_by_perm_grant_global_delete(p_permission_id uuid, p_user_group_id uuid) returns void
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
    perform permission_mater_schema_level_by_perm_grant_global_insert(new.permission_id,new.user_group_id);
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
        perform permission_mater_schema_level_by_perm_grant_global_insert(new.permission_id, new.user_group_id);
        perform permission_mater_schema_level_by_perm_grant_global_delete(old.permission_id, old.user_group_id);
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
    perform permission_mater_schema_level_by_perm_grant_global_delete(old.permission_id, old.user_group_id);
    return old;
end;
$$;
