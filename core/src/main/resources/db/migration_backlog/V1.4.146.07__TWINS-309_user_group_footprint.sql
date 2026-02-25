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

    user_group_footprint_id uuid not null
        references user_group_footprint
            on update cascade
            on delete cascade,

    unique (domain_id, user_group_footprint_id)
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

drop trigger if exists user_group_footprint_map_after_delete_wrapper_trigger on user_group_footprint_map;
create trigger user_group_footprint_map_after_delete_wrapper_trigger
    after delete on user_group_footprint_map
    for each row
execute function user_group_footprint_invalidate_wrapper();

drop trigger if exists user_group_footprint_map_after_update_wrapper_trigger on user_group_footprint_map;
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

        PERFORM permission_mater_global_init(v_hash);
    end if;

    -- 5 Возвращаем ID footprint
    return v_hash;
end;
$$;

create or replace function user_group_footprint_get(p_domain_id uuid, p_group_ids uuid[])
    returns uuid
    language plpgsql
    volatile
as
$$
declare
    v_footprint uuid;
    v_exists    boolean;
begin
    -- 1 Вычисляем footprint ID через immutable функцию
    v_footprint := user_group_footprint_create(p_group_ids);

    -- 2 Проверяем, существует ли уже footprint
    select exists(select 1
                  from user_group_footprint_registry
                  where user_group_footprint_id = v_footprint and domain_id = p_domain_id)
    into v_exists;

    if not v_exists then
        perform permission_mater_user_group_init(p_domain_id, v_footprint);
    end if;

    -- 3 Возвращаем ID footprint
    return v_footprint;
end;
$$;