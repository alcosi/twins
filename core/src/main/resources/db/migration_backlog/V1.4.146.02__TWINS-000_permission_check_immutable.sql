drop index if exists idx_user_group_by_assignee_propagation_granted_by_user_id;
drop index if exists idx_user_group_by_assignee_propagation_user_group_id;
drop index if exists idx_user_group_by_assignee_propagation_permission_schema_id;
drop index if exists idx_user_group_by_assignee_propagation_twin_class_id;
drop index if exists idx_user_group_by_assignee_propagation_twin_status_id;

create table user_group_by_assignee_propagation
(
    id                            uuid not null
        constraint user_group_by_assignee_propagation_pk
            primary key,
    permission_schema_id          uuid not null
        constraint user_group_by_assignee_propagation_permission_schema_id_fkey
            references permission_schema,
    user_group_id                 uuid not null
        constraint user_group_by_assignee_propagation_permission_id_fkey
            references permission
            on delete cascade,
    propagation_by_twin_class_id  uuid not null
        constraint user_group_by_assignee_propagation_twin_class_id_fkey
            references twin_class,
    propagation_by_twin_status_id uuid
        constraint user_group_by_assignee_propagation_twin_status_id_fkey
            references twin_status,
    created_by_user_id            uuid not null
        constraint user_group_by_assignee_propagation_granted_by_user_id_fkey
            references "user",
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
    IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
        RAISE NOTICE 'Process update for: %', new.id;
        PERFORM hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));

        IF NEW.assigner_user_id IS DISTINCT FROM OLD.assigner_user_id AND NEW.assigner_user_id IS NOT NULL THEN
            PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, old.assigner_user_id,NEW.twin_class_id, NEW.twin_status_id, NEW.owner_business_account_id);
        END IF;

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

create or replace function user_group_involve_by_assignee_propagation(new_assigner_user_id uuid, old_assigner_user_id uuid, p_twin_class_id uuid, p_twin_status_id uuid, p_owner_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    selected_user_group_id UUID := null;
BEGIN
    if new_assigner_user_id is not null then
        if p_owner_business_account_id is not null then

            select user_group_id into selected_user_group_id from user_group_by_assignee_propagation a
            join twin_class tc on tc.id = a.propagation_by_twin_status_id
            join domain d on d.id = tc.domain_id
            join domain_business_account db on db.domain_id = d.id and db.business_account_id = p_owner_business_account_id
                             where
                                 a.propagation_by_twin_class_id = p_twin_class_id and
                                 (a.propagation_by_twin_status_id = p_twin_status_id or a.propagation_by_twin_status_id is null) and
                                 a.permission_schema_id = COALESCE(db.permission_schema_id, d.permission_schema_id)
        end if;
        if p_owner_business_account_id is null then
            select user_group_id into selected_user_group_id from user_group_by_assignee_propagation a
                                                                      join twin_class tc on tc.id = a.propagation_by_twin_status_id
                                                                      join domain d on d.id = tc.domain_id
            where
                a.propagation_by_twin_class_id = p_twin_class_id and
                (a.propagation_by_twin_status_id = p_twin_status_id or a.propagation_by_twin_status_id is null) and
                a.permission_schema_id = d.permission_schema_id
        end if;

        if selected_user_group_id is not null then
--             TODO insert into groups
        end if;

    end if;

    if old_assigner_user_id is not null and new_assigner_user_id is null then

--         todo remove from grops

    end if;
END;
$$;



create or replace function permission_check(permissionSchemaId uuid, businessaccountid uuid, spaceid uuid, permissionidtwin uuid, permissionidtwinclass uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    permissionIdForUse UUID := permissionIdTwin;
BEGIN
    IF permissionIdForUse IS NULL THEN
        permissionIdForUse = permissionIdTwinClass;
    END IF;
    RETURN permission_check(permissionSchemaId, businessAccountId, spaceId, permissionIdForUse, userId, userGroupIdList, twinClassId, isAssignee, isCreator);
END;
$$;


create or replace function permission_check(domainId uuid, businessaccountid uuid, spaceid uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean
    volatile
    language plpgsql
as
$$

DECLARE
    permissionSchemaId        UUID;
    roles                     VARCHAR[] := '{}';
    isSpaceAssignee           BOOLEAN DEFAULT FALSE;
    isSpaceCreator            BOOLEAN DEFAULT FALSE;
BEGIN
    --- PERMISSION IS ABSENT
    IF permissionId IS NULL THEN
        RETURN TRUE;
    END IF;

    --- DENY_ALL permission
    IF permissionId = '00000000-0000-0000-0004-000000000001' THEN
        RETURN FALSE;
    END IF;

--     Detect permission schema
    permissionSchemaId := permission_detect_schema(domainId, businessAccountId, spaceId);

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        RETURN FALSE;
    END IF;

    IF isAssignee THEN
        roles := array_append(roles, 'assignee');
    END IF;

    IF isCreator THEN
        roles := array_append(roles, 'creator');
    END IF;

    SELECT * INTO isSpaceAssignee, isSpaceCreator FROM permission_check_space_assignee_and_creator(spaceId, userId);

    IF isSpaceAssignee THEN
        roles := array_append(roles, 'space_assignee');
    END IF;

    IF isSpaceCreator THEN
        roles := array_append(roles, 'space_creator');
    END IF;

    -- Check twin-role permissions
    IF permission_check_twin_role(permissionSchemaId, permissionId, roles, twinClassId) THEN
        RETURN TRUE;
    END IF;

    -- check propagation
    IF permission_check_assignee_propagation(permissionSchemaId, permissionId, businessAccountId, spaceId, userId) THEN
        RETURN TRUE;
    END IF;

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check space-role and space-role-group permissions
    RETURN permission_check_space_role_permissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$;

create or replace function permission_detect_schema(domainid uuid, businessaccountid uuid) returns uuid
    volatile
    language plpgsql
as
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT permission_schema_id INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId AND business_account_id = businessAccountId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    SELECT permission_schema_id INTO schemaId FROM domain WHERE id = domainId;
    RETURN schemaId;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$;


create or replace function permission_detect_schema(domainid uuid, businessaccountid uuid, spaceid uuid) returns uuid
    volatile
    language plpgsql
as
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in space
    IF spaceId IS NOT NULL THEN
        SELECT permission_schema_id INTO schemaId FROM space WHERE twin_id = spaceId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT permission_schema_id INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId AND business_account_id = businessAccountId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    SELECT permission_schema_id INTO schemaId FROM domain WHERE id = domainId;
    RETURN schemaId;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$;



DROP FUNCTION IF EXISTS public.permission_check_by_group_or_user(uuid, uuid, uuid, uuid[], uuid, uuid);


create or replace function permission_check_space_assignee_and_creator(spaceid uuid, userid uuid) returns space_permissions
    volatile
    language plpgsql
as
$$
DECLARE
    result space_permissions := (FALSE, FALSE);
BEGIN
    IF spaceId IS NULL THEN
        RETURN result;
    END IF;
    SELECT
        (t.assigner_user_id = userId) AS isSpaceAssignee,
        (t.created_by_user_id = userId) AS isSpaceCreator
    INTO result FROM twin t WHERE t.id = spaceId;
    RETURN result;
END;
$$;

create or replace function permission_check_twin_role(permissionschemaid uuid, permissionid uuid, roles character varying[], twinclassid uuid) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    hasPermission BOOLEAN := FALSE;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM permission_grant_twin_role
        WHERE permission_schema_id = permissionSchemaId
          AND permission_id = permissionId
          AND twin_class_id = twinClassId
          AND twin_role_id = ANY(roles)
    ) INTO hasPermission;

    RETURN hasPermission;
END;
$$;

create or replace function permission_check_assignee_propagation(permissionschemaid uuid, permissionid uuid, businessaccountid uuid, spaceid uuid, userid uuid) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    twinClassId UUID;
    twinStatusId UUID;
    inSpaceOnly BOOLEAN;
BEGIN
    -- check rights by twin_class_id
    SELECT propagation_by_twin_class_id, in_space_only INTO twinClassId, inSpaceOnly FROM public.permission_grant_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_class_id IS NOT NULL
    LIMIT 1;

    -- check rights by twin_status_id
    SELECT propagation_by_twin_status_id, in_space_only INTO twinStatusId, inSpaceOnly FROM public.permission_grant_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_status_id IS NOT NULL
    LIMIT 1;

    IF twinStatusId IS NOT NULL THEN
        -- if twin_status_id exists, check twin exists with assignee current user
        IF inSpaceOnly THEN
            PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_status_id = twinStatusId AND owner_business_account_id = businessAccountId AND permission_schema_space_id = spaceId LIMIT 1;
        ELSE
            PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_status_id = twinStatusId AND owner_business_account_id = businessAccountId LIMIT 1;
        END IF;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;

    IF twinClassId IS NOT NULL THEN
        -- if twin_class_id exists, check twin exists with assignee current user
        IF inSpaceOnly THEN
            PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_class_id = twinClassId AND owner_business_account_id = businessAccountId AND permission_schema_space_id = spaceId LIMIT 1;
        ELSE
            PERFORM 1
            FROM public.twin WHERE assigner_user_id = userId AND twin_class_id = twinClassId AND owner_business_account_id = businessAccountId LIMIT 1;
        END IF;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;

    RETURN FALSE;
END;
$$;


create or replace function permission_check_space_role_permissions(permissionschemaid uuid, permissionid uuid, spaceid uuid, userid uuid, usergroupidlist uuid[]) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    userAssignedToRoleExists INT;
    groupAssignedToRoleExists INT;
BEGIN
    -- Check if any space role assigned to the user has the given permission
    SELECT COUNT(sru.id)
    INTO userAssignedToRoleExists
    FROM space_role_user sru
    WHERE sru.twin_id = spaceId
      AND sru.user_id = userId
      AND sru.space_role_id IN (SELECT space_role_id FROM permission_get_roles(permissionSchemaId, permissionId));

    IF userAssignedToRoleExists > 0 THEN
        RETURN TRUE;
    END IF;

    -- Check if any space role assigned to the user's group has the given permission
    SELECT COUNT(srug.id)
    INTO groupAssignedToRoleExists
    FROM space_role_user_group srug
    WHERE srug.twin_id = spaceId
      AND srug.user_group_id = ANY (userGroupIdList)
      AND srug.space_role_id IN (SELECT space_role_id FROM permission_get_roles(permissionSchemaId, permissionId));

    RETURN groupAssignedToRoleExists > 0;
END;
$$;
