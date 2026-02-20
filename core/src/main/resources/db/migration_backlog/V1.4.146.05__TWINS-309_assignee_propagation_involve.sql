drop index if exists idx_user_group_by_assignee_propagation_granted_by_user_id;
drop index if exists idx_user_group_by_assignee_propagation_user_group_id;
drop index if exists idx_user_group_by_assignee_propagation_permission_schema_id;
drop index if exists idx_user_group_by_assignee_propagation_twin_class_id;
drop index if exists idx_user_group_by_assignee_propagation_twin_status_id;

create table if not exists user_group_by_assignee_propagation
(
    id                            uuid not null
        constraint user_group_by_assignee_propagation_pk
            primary key,
    permission_schema_id          uuid not null
        constraint user_group_by_assignee_propagation_permission_schema_id_fkey
            references permission_schema
            on update cascade on delete cascade,
    user_group_id                 uuid not null
        constraint user_group_by_assignee_propagation_user_group_id_fkey
            references user_group
            on update cascade on delete cascade,
    propagation_by_twin_class_id  uuid not null
        constraint user_group_by_assignee_propagation_twin_class_id_fkey
            references twin_class
            on update cascade on delete cascade,
    propagation_by_twin_status_id uuid
        constraint user_group_by_assignee_propagation_twin_status_id_fkey
            references twin_status
            on update cascade on delete cascade,
    created_by_user_id            uuid not null
        constraint user_group_by_assignee_propagation_granted_by_user_id_fkey
            references "user"
            on update cascade on delete cascade,
    created_at                    timestamp default CURRENT_TIMESTAMP
);

create index idx_user_group_by_assignee_propagation_granted_by_user_id
    on user_group_by_assignee_propagation (created_by_user_id);

create index idx_user_group_by_assignee_propagation_user_group_id
    on user_group_by_assignee_propagation (user_group_id);

create index idx_user_group_by_assignee_propagation_permission_schema_id
    on user_group_by_assignee_propagation (permission_schema_id);

create index idx_user_group_by_assignee_propagation_twin_class_id
    on user_group_by_assignee_propagation (propagation_by_twin_class_id);

create index idx_user_group_by_assignee_propagation_twin_status_id
    on user_group_by_assignee_propagation (propagation_by_twin_status_id);


create or replace function user_group_by_assignee_propagation_validate(
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

create or replace function user_group_by_assignee_propag_before_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform user_group_by_assignee_propagation_validate(new.user_group_id,new.propagation_by_twin_class_id);
    return new;
end;
$$;

create or replace function user_group_by_assignee_propag_before_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    if new.user_group_id is distinct from old.user_group_id
           or new.propagation_by_twin_class_id is distinct from old.propagation_by_twin_class_id then
        perform user_group_by_assignee_propagation_validate(new.user_group_id,new.propagation_by_twin_class_id);
    end if;
    return new;
end;
$$;

drop trigger if exists user_group_by_assignee_propag_before_insert_wrapper_trigger on user_group;
create trigger user_group_by_assignee_propag_before_insert_wrapper_trigger
    before insert
    on user_group_by_assignee_propagation
    for each row
execute procedure user_group_by_assignee_propag_before_insert_wrapper();
drop trigger if exists user_group_by_assignee_propag_before_update_wrapper_trigger on user_group;
create trigger user_group_by_assignee_propag_before_update_wrapper_trigger
    before update
    on user_group_by_assignee_propagation
    for each row
execute procedure user_group_by_assignee_propag_before_update_wrapper();


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
        perform user_group_involve_by_assignee_propagation(null, old.assigner_user_id, old.twin_class_id, old.twin_status_id, old.owner_business_account_id);
    END IF;

    return old;
end;
$$;

create or replace function twin_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN

    --todo react on tc and status change
    IF NEW.assigner_user_id IS DISTINCT FROM OLD.assigner_user_id THEN
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, old.assigner_user_id,NEW.twin_class_id, NEW.twin_status_id, NEW.owner_business_account_id);
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
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, null, NEW.twin_class_id, NEW.twin_status_id, NEW.owner_business_account_id);
    END IF;

    IF NEW.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function user_group_add_or_remove_group(p_user_id uuid, p_group_id uuid, p_domain_id uuid, p_business_account_id uuid, p_add_to_group boolean) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    selected_group_type varchar := null;
    selected_group_domain uuid := null;
    selected_group_business_account uuid := null;
BEGIN
    select ug.user_group_type_id, ug.domain_id, ug.business_account_id into selected_group_type, selected_group_domain, selected_group_business_account from user_group ug where ug.id = p_group_id;

    if p_add_to_group then

        insert into user_group_map (id,
                                    user_group_id,
                                    user_group_type_id,
                                    domain_id,
                                    business_account_id,
                                    user_id,
                                    involves_counter,
                                    added_manually,
                                    added_at,
                                    added_by_user_id)
        VALUES (uuid_generate_v7_custom(),
                p_group_id,
                null, --will be filled by before insert trigger
                p_domain_id,
                p_business_account_id,
                p_user_id,
                -9999, -- flag for trigger to  set counter to 1 and added_manualy false
                false,
                now(),
                '00000000-0000-0000-0000-000000000000')
        on conflict (user_group_id, domain_id, business_account_id, user_id) do update set involves_counter = involves_counter + 1;

    else
        --todo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        delete from user_group_map_type2 where auto_involved and user_id = p_user_id and business_account_id = p_business_account_id and user_group_id = p_group_id;

    end if;

END;
$$;

create or replace function user_group_involve_by_assignee_propagation(new_assigner_user_id uuid, old_assigner_user_id uuid, p_twin_class_id uuid, p_twin_status_id uuid, p_owner_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    selected_user_group_id UUID := null;
    selected_domain_id UUID := null;
    selected_status_id UUID := null;
BEGIN
    if p_owner_business_account_id is not null then

        select a.user_group_id, d.id, a.propagation_by_twin_status_id into selected_user_group_id, selected_domain_id, selected_status_id from user_group_by_assignee_propagation a
                                                                                                                                                   join twin_class tc on tc.id = a.propagation_by_twin_class_id
                                                                                                                                                   join domain d on d.id = tc.domain_id
                                                                                                                                                   join domain_business_account db on db.domain_id = d.id and db.business_account_id = p_owner_business_account_id
        where
            a.propagation_by_twin_class_id = p_twin_class_id and
            (a.propagation_by_twin_status_id = p_twin_status_id or a.propagation_by_twin_status_id is null) and
            a.permission_schema_id = COALESCE(db.permission_schema_id, d.permission_schema_id);
    end if;
    if p_owner_business_account_id is null then
        select a.user_group_id, d.id into selected_user_group_id, selected_domain_id from user_group_by_assignee_propagation a
                                                                                              join twin_class tc on tc.id = a.propagation_by_twin_class_id
                                                                                              join domain d on d.id = tc.domain_id
        where
            a.propagation_by_twin_class_id = p_twin_class_id and
            (a.propagation_by_twin_status_id = p_twin_status_id or a.propagation_by_twin_status_id is null) and
            a.permission_schema_id = d.permission_schema_id;
    end if;

    if selected_user_group_id is null then
        RETURN;
    end if;


    if new_assigner_user_id is not null then
        perform user_group_add_or_remove_group(new_assigner_user_id, selected_user_group_id, selected_domain_id, p_owner_business_account_id, true);
    end if;

    if old_assigner_user_id is not null then
        IF NOT EXISTS (SELECT 1 FROM twin
                       WHERE assigner_user_id = old_assigner_user_id
                         AND twin_class_id = p_twin_class_id
                         AND (selected_status_id IS NOT NULL AND twin_status_id = selected_status_id)
                         AND (p_owner_business_account_id IS NULL OR owner_business_account_id = p_owner_business_account_id)
                       LIMIT 1) THEN
            perform user_group_add_or_remove_group(old_assigner_user_id, selected_user_group_id, selected_domain_id, p_owner_business_account_id, false);
        END IF;
    end if;
END;
$$;
drop table if exists permission_grant_assignee_propagation;
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_CREATE' WHERE id = '00000000-0000-0004-0020-000000000002';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_MANAGE' WHERE id = '00000000-0000-0004-0020-000000000001';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_UPDATE' WHERE id = '00000000-0000-0004-0020-000000000004';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_DELETE' WHERE id = '00000000-0000-0004-0020-000000000005';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_VIEW' WHERE id = '00000000-0000-0004-0020-000000000003';
