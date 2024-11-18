DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'permission_schema_user') THEN
            EXECUTE 'ALTER TABLE public.permission_schema_user RENAME TO permission_grant_user';
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'permission_schema_assignee_propagation') THEN
            EXECUTE 'alter table public.permission_schema_assignee_propagation rename to permission_grant_assignee_propagation';
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'permission_schema_space_roles') THEN
            EXECUTE 'alter table public.permission_schema_space_roles rename to permission_grant_space_role';
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'permission_schema_twin_role') THEN
            EXECUTE 'alter table public.permission_schema_twin_role rename to permission_grant_twin_role';
        END IF;
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'permission_schema_user_group') THEN
            EXECUTE 'alter table public.permission_schema_user_group rename to permission_grant_user_group';
        END IF;
    END;
$$;


DROP TRIGGER IF EXISTS tiers_tier_update_trigger ON public.tier;

DROP FUNCTION IF EXISTS public.permission_check(uuid, uuid, uuid, uuid, uuid, uuid[], uuid, boolean, boolean);
DROP FUNCTION IF EXISTS public.permissioncheckbyschema(uuid, uuid, uuid, uuid[]);
DROP FUNCTION IF EXISTS public.permission_check_by_grant_group_or_user(uuid, uuid, uuid, uuid[]);
DROP FUNCTION IF EXISTS public.permissioncheckspacerolepermissions(uuid, uuid, uuid, uuid, uuid[]);
DROP FUNCTION IF EXISTS public.permissiongetroles(uuid, uuid);
DROP FUNCTION IF EXISTS public.permission_get_roles(uuid, uuid);
DROP FUNCTION IF EXISTS public.permission_check_space_role_permissions(uuid, uuid, uuid, uuid, uuid[]);
DROP FUNCTION IF EXISTS public.permissioncheckspaceassigneeandcreator(uuid, uuid);
DROP FUNCTION IF EXISTS public.permission_check_space_assignee_and_creator(uuid, uuid);
DROP FUNCTION IF EXISTS public.permission_check_assignee_propagation(uuid, uuid, uuid, uuid, uuid);
DROP FUNCTION IF EXISTS public.permissionchecktwinrole(uuid, uuid, character varying[], uuid);
DROP FUNCTION IF EXISTS public.permission_check_twin_role(uuid, uuid, character varying[], uuid);
DROP FUNCTION IF EXISTS public.permissiondetectschema(uuid, uuid, uuid);
DROP FUNCTION IF EXISTS public.permission_detect_schema(uuid, uuid, uuid);



create OR REPLACE function permission_check_by_grant_group_or_user(permissionschemaid uuid, permissionidforuse uuid, userid uuid, usergroupidlist uuid[]) returns boolean as $$
DECLARE
    userPermissionExists INT;
    groupPermissionExists INT;
BEGIN

    SELECT COUNT(id) INTO userPermissionExists
    FROM permission_grant_user
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionIdForUse
      AND user_id = userId;

    IF userPermissionExists > 0 THEN
        RETURN TRUE;
    END IF;

    SELECT COUNT(id) INTO groupPermissionExists
    FROM permission_grant_user_group
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionIdForUse
      AND user_group_id = ANY(userGroupIdList);

    RETURN groupPermissionExists > 0;
END;
$$ immutable language plpgsql;


create OR REPLACE function permission_check_space_assignee_and_creator(spaceid uuid, userid uuid) returns space_permissions as $$
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
$$ immutable language plpgsql;


create OR REPLACE function permission_check_assignee_propagation(permissionschemaid uuid, permissionid uuid, businessaccountid uuid, spaceid uuid, userid uuid) returns boolean as $$
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
$$ immutable language plpgsql;

create OR REPLACE function permission_get_roles(permissionschemaid uuid, permissionid uuid) returns TABLE(space_role_id uuid) as $$
BEGIN
    SELECT pssr.space_role_id
    FROM permission_grant_space_role pssr
    WHERE pssr.permission_schema_id = permissionSchemaId
      AND pssr.permission_id = permissionId;
END;
$$ immutable language plpgsql;

create OR REPLACE function permission_check_space_role_permissions(permissionschemaid uuid, permissionid uuid, spaceid uuid, userid uuid, usergroupidlist uuid[]) returns boolean as $$
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
$$ immutable language plpgsql;

create OR REPLACE function permission_check_twin_role(permissionschemaid uuid, permissionid uuid, roles character varying[], twinclassid uuid) returns boolean as $$
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
$$ immutable language plpgsql;

create OR REPLACE function permission_detect_schema(domainid uuid, businessaccountid uuid, spaceid uuid) returns uuid as $$
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
$$ immutable language plpgsql;


create OR REPLACE function permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean as $$
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
    IF permission_check_by_grant_group_or_user(permissionSchemaId, permissionId, userId, userGroupIdList) THEN
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
$$ immutable language plpgsql;
