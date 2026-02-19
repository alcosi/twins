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

    if selected_group_type = 'systemScopeDomainManage' then
        if selected_group_domain is not null then
            raise exception 'Cannot add user to group % because group domain is not null and domain id is %', p_group_id, p_domain_id;
        end if;
        if p_domain_id is null then
            raise exception 'Cannot add user to group % because domain is null', p_group_id;
        end if;
        if p_business_account_id is not null then
            raise exception 'Cannot add user to group % because ba is not null. ba id= %', p_group_id, p_business_account_id;
        end;
        if selected_group_business_account is not null then
            raise exception 'Cannot add user to group % because group ba is not null. ba id= %', p_group_id, selected_group_business_account;
        end;
        if p_add_to_group then
            insert into user_group_map_type3 (id,user_group_id, domain_id, user_id, added_at, added_by_user_id, auto_involved)
            VALUES (uuid_generate_v7_custom(), p_group_id, p_domain_id, p_user_id, now(), '00000000-0000-0000-0000-000000000000', true) on conflict (user_id, business_account_id, user_group_id) do nothing;
        else
            delete from user_group_map_type3 where auto_involved and user_id = p_user_id and domain_id = p_domain_id and user_group_id = p_group_id;
        end if;
    end if;


    if selected_group_type = 'domainScopeBusinessAccountManage' then
        if selected_group_domain <> p_domain_id then
            raise exception 'Cannot add user to group % because it is domain scope and domain id is %', p_group_id, p_domain_id;
        end if;
        if p_business_account_id is null then
            raise exception 'Cannot add user to group % because ba is null.', p_group_id;
        end;
        if selected_group_business_account is not null then
            raise exception 'Cannot add user to group % because group ba is not null. ba id= %', p_group_id, selected_group_business_account;
        end;
        if p_add_to_group then
            insert into user_group_map_type2 (id,user_group_id, business_account_id, user_id, added_at, added_by_user_id, involves_counter)
            VALUES (uuid_generate_v7_custom(), p_group_id, p_business_account_id, p_user_id, now(), '00000000-0000-0000-0000-000000000000', true) on conflict (user_id, business_account_id, user_group_id) do nothing;
        else
            delete from user_group_map_type2 where auto_involved and user_id = p_user_id and business_account_id = p_business_account_id and user_group_id = p_group_id;
        end if;
    end if;

    if selected_group_type = 'systemScopeDomainManage' then
        if selected_group_domain is not null then
            raise exception 'Cannot add user to group % because group domain is not null and domain id is %', p_group_id, p_domain_id;
        end if;
        if p_domain_id is null then
            raise exception 'Cannot add user to group % because domain is null', p_group_id;
        end if;
        if p_business_account_id is not null then
            raise exception 'Cannot add user to group % because ba is not null. ba id= %', p_group_id, p_business_account_id;
        end;
        if selected_group_business_account is not null then
            raise exception 'Cannot add user to group % because group ba is not null. ba id= %', p_group_id, selected_group_business_account;
        end;
        if p_add_to_group then
            insert into user_group_map_type3 (id,user_group_id, domain_id, user_id, added_at, added_by_user_id, auto_involved)
            VALUES (uuid_generate_v7_custom(), p_group_id, p_domain_id, p_user_id, now(), '00000000-0000-0000-0000-000000000000', true) on conflict (user_id, business_account_id, user_group_id) do nothing;
        else
            delete from user_group_map_type3 where auto_involved and user_id = p_user_id and domain_id = p_domain_id and user_group_id = p_group_id;
        end if;
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
