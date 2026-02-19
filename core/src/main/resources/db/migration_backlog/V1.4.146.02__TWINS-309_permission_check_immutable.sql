CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE OR REPLACE FUNCTION uuid_generate_v7_custom()
    RETURNS uuid
    LANGUAGE plpgsql
AS $$
DECLARE
    unix_ms bigint;
    rand_bytes bytea;
    uuid_bytes bytea;
BEGIN
    -- 48-bit unix timestamp in milliseconds
    unix_ms := floor(extract(epoch from clock_timestamp()) * 1000);

    -- 10 random bytes (80 bits)
    rand_bytes := gen_random_bytes(10);

    -- Compose UUID (16 bytes total)
    uuid_bytes :=
            -- 6 bytes timestamp
        substring(int8send(unix_ms) from 3 for 6)
            ||
            -- version (4 bits) + first 4 bits random
        set_byte(substring(rand_bytes from 1 for 1), 0,
                 (get_byte(rand_bytes, 0) & 15) | 112)  -- 0111xxxx
            ||
            -- remaining 9 random bytes
        substring(rand_bytes from 2);

    -- Set variant (10xxxxxx)
    uuid_bytes :=
            set_byte(uuid_bytes, 8,
                     (get_byte(uuid_bytes, 8) & 63) | 128);

    RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$;

DROP FUNCTION IF EXISTS permission_check(uuid,uuid,uuid,uuid,uuid,uuid,uuid[],uuid,boolean,boolean);
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

DROP FUNCTION IF EXISTS permission_check(uuid,uuid,uuid,uuid,uuid, uuid[],uuid,boolean,boolean);
create or replace function permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean
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

    -- Detect permission schema
    permissionSchemaId := permission_detect_schema(domainId, businessAccountId, spaceId);

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check direct user or user group permissions
    IF permission_check_by_group_or_user(permissionSchemaId, permissionId, userId, userGroupIdList, domainId, businessAccountId) THEN
        RETURN TRUE;
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



create or replace function permission_check_by_group_or_user(permissionschemaid uuid, permissionidforuse uuid, userid uuid, usergroupidlist uuid[], domainid uuid, businessaccountid uuid) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    userPermissionExists INT;
    groupPermissionExists INT;
BEGIN
    SELECT COUNT(id) INTO userPermissionExists
    FROM permission_grant_user
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionIdForUse
      AND user_id = userId
      AND domain_id = domainId
      AND (business_account_id = businessAccountId OR business_account_id IS NULL);

    IF userPermissionExists > 0 THEN
        RETURN TRUE;
    END IF;

    -- Check group permissions (unchanged)
    SELECT COUNT(id) INTO groupPermissionExists
    FROM permission_grant_user_group
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionIdForUse
      AND user_group_id = ANY(userGroupIdList);

    RETURN groupPermissionExists > 0;
END;
$$;

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
          AND (
            (granted_to_assignee AND 'assignee' = ANY(roles)) OR
            (granted_to_creator AND 'creator' = ANY(roles)) OR
            (granted_to_space_assignee AND 'space_assignee' = ANY(roles)) OR
            (granted_to_space_creator AND 'space_creator' = ANY(roles))
            )
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
    userAssignedToRoleExists BOOLEAN;
    groupAssignedToRoleExists BOOLEAN;
BEGIN
    -- Check if any space role assigned to the user has the given permission
    SELECT EXISTS (
        SELECT 1
        FROM space_role_user sru
        WHERE sru.twin_id = spaceId
          AND sru.user_id = userId
          AND sru.space_role_id IN (SELECT space_role_id FROM permission_get_roles(permissionSchemaId, permissionId))
    ) INTO userAssignedToRoleExists;

    IF userAssignedToRoleExists THEN
        RETURN TRUE;
    END IF;

    -- Check if any space role assigned to the user's group has the given permission
    SELECT EXISTS (
        SELECT 1
        FROM space_role_user_group srug
        WHERE srug.twin_id = spaceId
          AND srug.user_group_id = ANY (userGroupIdList)
          AND srug.space_role_id IN (SELECT space_role_id FROM permission_get_roles(permissionSchemaId, permissionId))
    ) INTO groupAssignedToRoleExists;

    RETURN groupAssignedToRoleExists;
END;
$$;
