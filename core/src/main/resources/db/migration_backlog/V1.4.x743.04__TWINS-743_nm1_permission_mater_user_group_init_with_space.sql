alter table user_group_footprint
    add if not exists created_at timestamp default current_timestamp not null;

alter table user_group_footprint_registry
    add if not exists init_duration bigint default 0 not null;

alter table user_group_footprint_registry
    add if not exists created_at timestamp default current_timestamp not null;

create or replace function permission_mater_user_group_init(
    p_domain_id uuid,
    p_footprint uuid
)
    returns void
    language plpgsql
as
$$
declare
    v_registry_id uuid;
    v_lock_key bigint;
    v_started_at timestamptz;
    v_duration_ms bigint;
begin
    -- 0 Retrieve registry_id ti check if it's already initiated
    select id into v_registry_id
    from user_group_footprint_registry
    where domain_id = p_domain_id
      and user_group_footprint_id = p_footprint;

    if v_registry_id is not null then
        return;
    end if;

    -- 1 Compute an advisory lock key from domain + footprint
    v_lock_key := hashtextextended('permission_mater_user_group_init-' || p_domain_id::text || '-' || p_footprint::text, 0);

    -- 2 Acquire session-level advisory lock
    perform pg_advisory_lock(v_lock_key);

    -- 3 Retrieve registry_id one more time if some other thread overtakes the current
    select id into v_registry_id
    from user_group_footprint_registry
    where domain_id = p_domain_id
      and user_group_footprint_id = p_footprint;

    if v_registry_id is not null then
        perform pg_advisory_unlock(v_lock_key);
        return;
    end if;

    v_registry_id := gen_random_uuid();
    -- 4 Insert footprint into registry if not exists
    insert into user_group_footprint_registry(id, domain_id, user_group_footprint_id)
    values (v_registry_id, p_domain_id, p_footprint);

    v_started_at := clock_timestamp();

    -- 5 Materialize permissions from user_group grants (join with footprint map)
    insert into permission_mater_user_group(
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
        count(*) as grt_count
    from permission_schema s
             join permission_grant_user_group g
                  on g.permission_schema_id = s.id
             join user_group_footprint_map m
                  on m.user_group_footprint_id = p_footprint
                      and m.user_group_id = g.user_group_id
    where s.domain_id = p_domain_id
    group by
        v_registry_id,
        s.id,
        g.permission_id,
        p_footprint
    on conflict (permission_schema_id, permission_id, user_group_footprint_id)
        do update
        set grants_count = permission_mater_user_group.grants_count + excluded.grants_count;


    -- 6 Materialize SPACE permissions from space_role grants
    insert into permission_mater_space_user_group(
        user_group_footprint_registry_id,
        twin_id,
        permission_schema_id,
        permission_id,
        user_group_footprint_id,
        grants_count
    )
    select
        v_registry_id,
        srug.twin_id,
        g.permission_schema_id,
        g.permission_id,
        p_footprint,
        count(*) as grants_count
    from permission_grant_space_role g
             join space_role_user_group srug
                  on srug.space_role_id = g.space_role_id
             join user_group_footprint_map m
                  on m.user_group_id = srug.user_group_id
                      and m.user_group_footprint_id = p_footprint
             join twin t
                  on t.id = srug.twin_id
             join twin_class tc
                  on tc.id = t.twin_class_id
    where tc.domain_id = p_domain_id
    group by
        v_registry_id,
        srug.twin_id,
        g.permission_schema_id,
        g.permission_id,
        p_footprint
    on conflict (
        twin_id,
        permission_schema_id,
        permission_id,
        user_group_footprint_id
        )
        do update
        set grants_count =
                permission_mater_space_user_group.grants_count
                    + excluded.grants_count;

    v_duration_ms := (extract(epoch from (clock_timestamp() - v_started_at)) * 1000)::bigint;

    update user_group_footprint_registry
    set init_duration = v_duration_ms
    where id = v_registry_id;

    -- 7 Release advisory lock
    perform pg_advisory_unlock(v_lock_key);

end;
$$;