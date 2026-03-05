drop index if exists uidx_user_group_involve_assignee;
drop index if exists idx_user_group_involve_assignee_by_user_id;
drop index if exists idx_user_group_involve_assignee_user_group_id;
drop index if exists idx_user_group_involve_assignee_twin_class_id;
drop index if exists idx_user_group_involve_assignee_twin_status_id;

create table if not exists user_group_involve_assignee
(
    id                            uuid not null
        constraint user_group_involve_assignee_pk
            primary key,
    user_group_id                 uuid not null
        constraint user_group_involve_assignee_user_group_id_fkey
            references user_group
            on update cascade on delete cascade,
    propagation_by_twin_class_id  uuid not null
        constraint user_group_involve_assignee_twin_class_id_fkey
            references twin_class
            on update cascade on delete cascade,
    propagation_by_twin_status_id uuid
        constraint user_group_involve_assignee_twin_status_id_fkey
            references twin_status
            on update cascade on delete cascade,
    created_by_user_id            uuid not null
        constraint user_group_involve_assignee_by_user_id_fkey
            references "user"
            on update cascade on delete cascade,
    created_at                    timestamp default CURRENT_TIMESTAMP
);

create unique index uidx_user_group_involve_assignee
    on user_group_involve_assignee (user_group_id, propagation_by_twin_class_id, propagation_by_twin_status_id)
    nulls not distinct;

create index idx_user_group_involve_assignee_by_user_id
    on user_group_involve_assignee (created_by_user_id);

create index idx_user_group_involve_assignee_user_group_id
    on user_group_involve_assignee (user_group_id);

create index idx_user_group_involve_assignee_twin_class_id
    on user_group_involve_assignee (propagation_by_twin_class_id);

create index idx_user_group_involve_assignee_twin_status_id
    on user_group_involve_assignee (propagation_by_twin_status_id);


create or replace function user_group_involve_assignee_validate(
    p_user_group_id uuid,
    p_twin_class_id uuid
)
    returns void
    language plpgsql
as
$$
declare
    v_group_type varchar;
    v_group_domain_id uuid;
    v_owner_type varchar;
    v_class_domain_id uuid;
begin
    -- Load user_group type and domain
    select ug.user_group_type_id,
           ug.domain_id
    into v_group_type,
        v_group_domain_id
    from user_group ug
    where ug.id = p_user_group_id;

    if not found then
        raise exception 'user_group not found: %', p_user_group_id;
    end if;

    -- Load twin_class owner type and domain
    select tc.twin_class_owner_type_id,
           tc.domain_id
    into v_owner_type,
        v_class_domain_id
    from twin_class tc
    where tc.id = p_twin_class_id;

    if not found then
        raise exception 'twin_class not found: %', p_twin_class_id;
    end if;

    -- Validate compatibility between twin_class owner type and user_group type
    case v_owner_type

        when 'user' then
            raise exception 'Propagation is not allowed for twin_class owner type=user';

        when 'businessAccount' then
            if v_group_type <> 'businessAccountScopeBusinessAccountManage' then
                raise exception 'Only businessAccountScopeBusinessAccountManage groups can access businessAccount classes';
            end if;

        when 'domainUser' then
            if v_group_type not in ('domainScopeDomainManage', 'systemScopeDomainManage') then
                raise exception 'Invalid group type for domainUser class';
            end if;

            -- For domainScopeDomainManage ensure same domain
            if v_group_type = 'domainScopeDomainManage'
                and v_class_domain_id is distinct from v_group_domain_id then
                raise exception 'Domain mismatch between user_group and twin_class';
            end if;

        when 'domainBusinessAccount' then
            if v_group_type not in ('domainScopeBusinessAccountManage',
                                    'domainAndBusinessAccountScopeBusinessAccountManage') then
                raise exception 'Invalid group type for domainBusinessAccount class';
            end if;

            -- Ensure same domain
            if v_class_domain_id is distinct from v_group_domain_id then
                raise exception 'Domain mismatch between user_group and twin_class';
            end if;

        when 'domain' then
            if v_group_type not in ('domainScopeDomainManage', 'systemScopeDomainManage') then
                raise exception 'Invalid group type for domain class';
            end if;

            -- For domainScopeDomainManage ensure same domain
            if v_group_type = 'domainScopeDomainManage'
                and v_class_domain_id is distinct from v_group_domain_id then
                raise exception 'Domain mismatch between user_group and twin_class';
            end if;

        when 'system' then
            raise exception 'Propagation is not allowed for twin_class owner type=system';

        else
            raise exception 'Unsupported twin_class_owner_type_id=%', v_owner_type;

        end case;

end;
$$;

create or replace function user_group_involve_assignee_before_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform user_group_involve_assignee_validate(new.user_group_id,new.propagation_by_twin_class_id);
    return new;
end;
$$;

create or replace function user_group_involve_assignee_before_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if new.user_group_id is distinct from old.user_group_id
        or new.propagation_by_twin_class_id is distinct from old.propagation_by_twin_class_id then
        perform user_group_involve_assignee_validate(new.user_group_id,new.propagation_by_twin_class_id);
    end if;
    return new;
end;
$$;

create or replace function user_group_involve_assignee_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'on', true); -- function has direct call protection
    perform user_group_involve_existed_twins_add(new.user_group_id, new.propagation_by_twin_class_id, new.propagation_by_twin_status_id);
    PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'off', true);
    return new;
end;
$$;

create or replace function user_group_involve_assignee_after_delete_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'on', true); -- function has direct call protection
    perform user_group_involve_existed_twins_delete(old.user_group_id, old.propagation_by_twin_class_id, old.propagation_by_twin_status_id);
    PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'off', true);
    return new;
end;
$$;

create or replace function user_group_involve_assignee_after_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if new.user_group_id is distinct from old.user_group_id or
       new.propagation_by_twin_class_id is distinct from old.propagation_by_twin_class_id or
       new.propagation_by_twin_status_id is distinct from old.propagation_by_twin_status_id
    then
        PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'on', true); -- function has direct call protection
        perform user_group_involve_existed_twins_delete(old.user_group_id, old.propagation_by_twin_class_id, old.propagation_by_twin_status_id);
        perform user_group_involve_existed_twins_add(new.user_group_id, new.propagation_by_twin_class_id, new.propagation_by_twin_status_id);
        PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'off', true);
    end if;
    return new;
end;
$$;

drop trigger if exists user_group_involve_assignee_before_insert_wrapper_trigger on user_group_involve_assignee;
create trigger user_group_involve_assignee_before_insert_wrapper_trigger
    before insert
    on user_group_involve_assignee
    for each row
execute procedure user_group_involve_assignee_before_insert_wrapper();

drop trigger if exists user_group_involve_assignee_before_update_wrapper_trigger on user_group_involve_assignee;
create trigger user_group_involve_assignee_before_update_wrapper_trigger
    before update
    on user_group_involve_assignee
    for each row
execute procedure user_group_involve_assignee_before_update_wrapper();

drop trigger if exists user_group_involve_assignee_after_insert_wrapper_trigger on user_group_involve_assignee;
create trigger user_group_involve_assignee_after_insert_wrapper_trigger
    after insert
    on user_group_involve_assignee
    for each row
execute procedure user_group_involve_assignee_after_insert_wrapper();

drop trigger if exists user_group_involve_assignee_after_update_wrapper_trigger on user_group_involve_assignee;
create trigger user_group_involve_assignee_after_update_wrapper_trigger
    after update
    on user_group_involve_assignee
    for each row
execute procedure user_group_involve_assignee_after_update_wrapper();

drop trigger if exists user_group_involve_assignee_after_delete_wrapper_trigger on user_group_involve_assignee;
create trigger user_group_involve_assignee_after_delete_wrapper_trigger
    after delete
    on user_group_involve_assignee
    for each row
execute procedure user_group_involve_assignee_after_delete_wrapper();



drop trigger if exists twin_after_delete_wrapper_trigger on twin;
-- auto-generated definition
create trigger twin_after_delete_wrapper_trigger
    after delete
    on twin
    for each row
execute procedure twin_after_delete_wrapper();

create or replace function twin_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    IF old.assigner_user_id IS NOT NULL THEN
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'on', true); -- function has direct call protection
        perform user_group_involve_by_assignee_propagation(null, old.assigner_user_id, null, old.twin_class_id, null, old.twin_status_id, old.owner_business_account_id);
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'off', true);
    END IF;

    IF OLD.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
    END IF;

    -- Update twin_class twin counter
    IF OLD.twin_class_id IS NOT NULL THEN
        PERFORM update_twin_class_twin_counter(OLD.twin_class_id);
    END IF;

    return old;
end;
$$;

create or replace function twin_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN

    IF NEW.assigner_user_id IS DISTINCT FROM OLD.assigner_user_id
           or NEW.twin_class_id IS DISTINCT FROM OLD.twin_class_id
           or NEW.twin_status_id IS DISTINCT FROM OLd.twin_status_id THEN
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'on', true); -- function has direct call protection
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, old.assigner_user_id,NEW.twin_class_id, old.twin_class_id, NEW.twin_status_id, old.twin_status_id, NEW.owner_business_account_id);
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'off', true);
    END IF;

    IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
        RAISE NOTICE 'Process update for: %', new.id;
        PERFORM hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));

        IF OLD.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
        END IF;
        IF NEW.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
        END IF;
    END IF;

    -- Update twin_class twin counters if twin_class_id changed
    IF OLD.twin_class_id IS DISTINCT FROM NEW.twin_class_id THEN
        IF OLD.twin_class_id IS NOT NULL THEN
            PERFORM update_twin_class_twin_counter(OLD.twin_class_id);
        END IF;
        IF NEW.twin_class_id IS NOT NULL THEN
            PERFORM update_twin_class_twin_counter(NEW.twin_class_id);
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

create or replace function twin_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    RAISE NOTICE 'Process insert for: %', new.id;
    PERFORM hierarchyUpdateTreeHard(new.id, hierarchyDetectTree(new.id));

    IF NEW.assigner_user_id IS NOT NULL THEN
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'on', true); -- function has direct call protection
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, null, NEW.twin_class_id, null, NEW.twin_status_id, null, NEW.owner_business_account_id);
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'off', true);
    END IF;

    IF NEW.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
    END IF;

    -- Update twin_class twin counter
    IF NEW.twin_class_id IS NOT NULL THEN
        PERFORM update_twin_class_twin_counter(NEW.twin_class_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function user_group_involved_user_add(p_user_id uuid, p_group_id uuid, p_domain_id uuid, p_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
DECLARE
BEGIN
    PERFORM set_config('app.user_group_map_auto', 'on', true);
    insert into user_group_map (id,
                                user_group_id,
                                user_group_type_id,
                                domain_id,
                                business_account_id,
                                user_id,
                                involves_count,
                                added_manually,
                                added_at,
                                added_by_user_id)
    VALUES (uuid_generate_v7_custom(),
            p_group_id,
            null, --will be filled by before insert trigger
            p_domain_id,
            p_business_account_id,
            p_user_id,
            1,
            false,
            now(),
            '00000000-0000-0000-0000-000000000000')
    on conflict (user_group_id, domain_id, business_account_id, user_id) do update set involves_count = user_group_map.involves_count + 1;
    PERFORM set_config('app.user_group_map_auto', 'off', true);
END;
$$;

create or replace function user_group_involved_user_remove(p_user_id uuid, p_group_id uuid, p_domain_id uuid, p_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
DECLARE
BEGIN
    update user_group_map set involves_count = involves_count - 1
    where
        user_id = p_user_id and
        user_group_id = p_group_id and
        ((p_domain_id IS NULL AND domain_id IS NULL) OR domain_id = p_domain_id) and
        ((p_business_account_id IS NULL AND business_account_id IS NULL) OR business_account_id = p_business_account_id);

END;
$$;



create or replace function user_group_involve_by_assignee_propagation(
    new_assigner_user_id uuid,
    old_assigner_user_id uuid,

    p_new_twin_class_id uuid,
    p_old_twin_class_id uuid,

    p_new_twin_status_id uuid,
    p_old_twin_status_id uuid,

    p_owner_business_account_id uuid
) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    new_user_group_id uuid;
    new_domain_id uuid;

    old_user_group_id uuid;
    old_domain_id uuid;

    propagation_changed boolean;
BEGIN
    IF current_setting('app.user_group_involve_by_assignee_propagation_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

    propagation_changed :=
            p_new_twin_class_id IS DISTINCT FROM p_old_twin_class_id
                OR
            p_new_twin_status_id IS DISTINCT FROM p_old_twin_status_id;

    ----------------------------------------------------------------
    -- 1. Resolve NEW propagation only if needed
    ----------------------------------------------------------------
    IF new_assigner_user_id IS NOT NULL OR propagation_changed THEN
        select a.user_group_id, tc.domain_id
        into new_user_group_id, new_domain_id
        from user_group_involve_assignee a
                 join twin_class tc on tc.id = a.propagation_by_twin_class_id
        where a.propagation_by_twin_class_id = p_new_twin_class_id
          and (a.propagation_by_twin_status_id = p_new_twin_status_id
            or a.propagation_by_twin_status_id is null)
        limit 1;
    END IF;

    ----------------------------------------------------------------
    -- 2. Resolve OLD propagation only if it changed
    ----------------------------------------------------------------
    IF propagation_changed THEN
        select a.user_group_id, tc.domain_id
        into old_user_group_id, old_domain_id
        from user_group_involve_assignee a
                 join twin_class tc on tc.id = a.propagation_by_twin_class_id
        where a.propagation_by_twin_class_id = p_old_twin_class_id
          and (a.propagation_by_twin_status_id = p_old_twin_status_id
            or a.propagation_by_twin_status_id is null)
        limit 1;
    ELSE
        old_user_group_id := new_user_group_id;
        old_domain_id := new_domain_id;
    END IF;

    ----------------------------------------------------------------
    -- 3. ADD new assigner
    ----------------------------------------------------------------
    IF new_assigner_user_id IS NOT NULL
        AND new_user_group_id IS NOT NULL
        AND new_assigner_user_id IS DISTINCT FROM old_assigner_user_id
    THEN
        perform user_group_involved_user_add(
                new_assigner_user_id,
                new_user_group_id,
                new_domain_id,
                p_owner_business_account_id
                );
    END IF;

    ----------------------------------------------------------------
    -- 4. REMOVE old assigner
    ----------------------------------------------------------------
    IF old_assigner_user_id IS NOT NULL
        AND old_user_group_id IS NOT NULL
        AND (
           old_assigner_user_id IS DISTINCT FROM new_assigner_user_id
               OR propagation_changed
           )
    THEN
        perform user_group_involved_user_remove(
                    old_assigner_user_id,
                    old_user_group_id,
                    old_domain_id,
                    p_owner_business_account_id
                    );

    END IF;

END;
$$;

------------------------------------------------------------------------------
create or replace function user_group_involve_existed_twins_add(
    p_user_group_id uuid,
    p_twin_class_id uuid,
    p_twin_status_id uuid
)
    returns void
    language plpgsql
as
$$
begin
    IF current_setting('app.user_group_involve_existed_twins_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

    PERFORM set_config('app.user_group_map_auto', 'on', true);
    insert into user_group_map (id, user_group_id, user_group_type_id, domain_id, business_account_id, user_id, involves_count, added_manually, added_at, added_by_user_id)
    select
        uuid_generate_v7_custom(),
        p_user_group_id,
        null, -- триггер заполнит
        t.domain_id,
        t.owner_business_account_id,
        t.assigner_user_id,
        t.twins_count,
        false,
        now(),
        '00000000-0000-0000-0000-000000000000'
    from (
             select t.assigner_user_id, t.owner_business_account_id, tc.domain_id, count(*) as twins_count
             from twin t
                      join twin_class tc on tc.id = t.twin_class_id
             where t.assigner_user_id is not null
               and t.twin_class_id = p_twin_class_id
               and (p_twin_status_id is null or t.twin_status_id = p_twin_status_id)
             group by t.assigner_user_id, t.owner_business_account_id, tc.domain_id
         ) t
    on conflict (user_group_id, domain_id, business_account_id, user_id)
        do update set involves_count = user_group_map.involves_count + excluded.involves_count;
    PERFORM set_config('app.user_group_map_auto', 'off', true);
end;
$$;

create or replace function user_group_involve_existed_twins_delete(
    p_user_group_id uuid,
    p_twin_class_id uuid,
    p_twin_status_id uuid
)
    returns void
    language plpgsql
as
$$
begin
    IF current_setting('app.user_group_involve_existed_twins_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;
    update user_group_map ugm
    set involves_count = involves_count - x.twins_count
    from (
             select  t.assigner_user_id, t.owner_business_account_id, tc.domain_id, count(*) as twins_count
             from twin t
                      join twin_class tc on tc.id = t.twin_class_id
             where t.twin_class_id = p_twin_class_id
               and (p_twin_status_id is null or t.twin_status_id = p_twin_status_id)
             group by t.assigner_user_id, t.owner_business_account_id, tc.domain_id
         ) x
    where ugm.user_group_id = p_user_group_id
      and ugm.user_id = x.assigner_user_id
      and ugm.domain_id = x.domain_id
      and ((ugm.business_account_id is null and x.owner_business_account_id is null)
        or ugm.business_account_id = x.owner_business_account_id);
end;
$$;

create or replace function user_group_involve_reinit()
    returns void
    language plpgsql
as
$$
declare
    r record;
begin
    update user_group_map
    set involves_count = 0
    where involves_count <> 0;

    PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'on', true); -- function has direct call protection
    for r in
        select user_group_id,
               propagation_by_twin_class_id,
               propagation_by_twin_status_id
        from user_group_involve_assignee
        loop
            perform user_group_involve_existed_twins_add(
                    r.user_group_id,
                    r.propagation_by_twin_class_id,
                    r.propagation_by_twin_status_id
                    );
        end loop;
    PERFORM set_config('app.user_group_involve_existed_twins_trigger', 'off', true);
end;
$$;


drop table if exists permission_grant_assignee_propagation;
UPDATE permission SET key = 'USER_GROUP_INVOLVE_ASSIGNEE_CREATE' WHERE id = '00000000-0000-0004-0020-000000000002';
UPDATE permission SET key = 'USER_GROUP_INVOLVE_ASSIGNEE_MANAGE' WHERE id = '00000000-0000-0004-0020-000000000001';
UPDATE permission SET key = 'USER_GROUP_INVOLVE_ASSIGNEE_UPDATE' WHERE id = '00000000-0000-0004-0020-000000000004';
UPDATE permission SET key = 'USER_GROUP_INVOLVE_ASSIGNEE_DELETE' WHERE id = '00000000-0000-0004-0020-000000000005';
UPDATE permission SET key = 'USER_GROUP_INVOLVE_ASSIGNEE_VIEW' WHERE id = '00000000-0000-0004-0020-000000000003';
