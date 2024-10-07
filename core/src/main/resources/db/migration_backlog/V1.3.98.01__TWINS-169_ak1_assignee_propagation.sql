alter table public.permission_schema_assignee_propagation add if not exists in_space_only boolean default false;


DROP FUNCTION IF EXISTS public.permission_check_assignee_involver(UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permission_check_assignee_involver(UUID, UUID, UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permission_check_assignee_propagation(UUID, UUID, UUID, UUID, UUID);

CREATE OR REPLACE FUNCTION permission_check_assignee_propagation(permissionSchemaId UUID, permissionId UUID, businessAccountId UUID, spaceId UUID, userId UUID)
    RETURNS BOOLEAN AS
$$
DECLARE
    twinClassId UUID;
    twinStatusId UUID;
    inSpaceOnly BOOLEAN;
BEGIN
    -- check rights by twin_class_id
    SELECT propagation_by_twin_class_id, in_space_only INTO twinClassId, inSpaceOnly FROM public.permission_schema_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_class_id IS NOT NULL
    LIMIT 1;

    -- check rights by twin_status_id
    SELECT propagation_by_twin_status_id, in_space_only INTO twinStatusId, inSpaceOnly FROM public.permission_schema_assignee_propagation
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
$$ LANGUAGE plpgsql IMMUTABLE;

DROP FUNCTION IF EXISTS public.permission_check(UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);
create or replace function permission_check(domainId uuid, businessAccountId uuid, spaceId uuid, permissionId uuid, userId uuid, userGroupIdList uuid[], twinClassId uuid, isAssignee boolean DEFAULT false, isCreator boolean DEFAULT false)
    returns boolean as
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
    permissionSchemaId := permissionDetectSchema(domainId, businessAccountId, spaceId);

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check direct user or user group permissions
    IF permissionCheckBySchema(permissionSchemaId, permissionId, userId, userGroupIdList) THEN
        RETURN TRUE;
    END IF;

    IF isAssignee THEN
        roles := array_append(roles, 'assignee');
    END IF;

    IF isCreator THEN
        roles := array_append(roles, 'creator');
    END IF;

    SELECT * INTO isSpaceAssignee, isSpaceCreator FROM permissionCheckSpaceAssigneeAndCreator(spaceId, userId);

    IF isSpaceAssignee THEN
        roles := array_append(roles, 'space_assignee');
    END IF;

    IF isSpaceCreator THEN
        roles := array_append(roles, 'space_creator');
    END IF;

    -- Check twin-role permissions
    IF permissionCheckTwinRole(permissionSchemaId, permissionId, roles, twinClassId) THEN
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
    RETURN permissionCheckSpaceRolePermissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$ language plpgsql immutable


