create or replace function permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionSchemaId uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean
    stable
    language plpgsql
as
$$

DECLARE
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

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        permissionSchemaId := permission_schema_detect(spaceid, businessAccountId, twinclassid);
        IF permissionSchemaId IS NULL THEN
            RETURN FALSE;
        END IF;
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
