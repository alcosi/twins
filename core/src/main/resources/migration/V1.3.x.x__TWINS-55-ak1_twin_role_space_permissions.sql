INSERT INTO twin_role (id)
VALUES ('space_assignee'), ('space_creator')
ON CONFLICT (id) DO NOTHING;

DROP FUNCTION IF EXISTS public.permissionCheckAssigneeAndCreator(UUID, UUID, BOOLEAN, BOOLEAN, UUID);
DROP FUNCTION IF EXISTS public.permissionCheckTwinRole(UUID, UUID, VARCHAR[], UUID);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS public.permissionCheckSpaceAssigneeAndCreator(UUID, UUID);
DROP TYPE IF EXISTS space_permissions CASCADE;

CREATE OR REPLACE FUNCTION permissionCheckTwinRole(
    permissionSchemaId UUID,
    permissionId UUID,
    roles VARCHAR[],
    twinClassId UUID
)
    RETURNS BOOLEAN AS
$$
DECLARE
    hasPermission BOOLEAN := FALSE;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM permission_schema_twin_role
        WHERE permission_schema_id = permissionSchemaId
          AND permission_id = permissionId
          AND twin_class_id = twinClassId
          AND twin_role_id = ANY(roles)
    ) INTO hasPermission;

    RETURN hasPermission;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


CREATE TYPE space_permissions AS (
                                     isSpaceAssignee BOOLEAN,
                                     isSpaceCreator BOOLEAN
                                 );

CREATE OR REPLACE FUNCTION permissionCheckSpaceAssigneeAndCreator(spaceId UUID, userId UUID)
    RETURNS space_permissions AS
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
$$ LANGUAGE plpgsql IMMUTABLE;



CREATE OR REPLACE FUNCTION permissionCheck(
    domainId UUID,
    businessAccountId UUID,
    spaceId UUID,
    permissionId UUID,
    userId UUID,
    userGroupIdList UUID[],
    twinClassId UUID,
    isAssignee BOOLEAN DEFAULT FALSE,
    isCreator BOOLEAN DEFAULT FALSE
)
    RETURNS BOOLEAN AS
$$
DECLARE
    permissionSchemaId        UUID;
    roles                     VARCHAR[] := '{}';
    isSpaceAssignee           BOOLEAN DEFAULT FALSE;
    isSpaceCreator            BOOLEAN DEFAULT FALSE;
BEGIN
    IF permissionId IS NULL THEN
        RETURN TRUE;
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

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check space-role and space-role-group permissions
    RETURN permissionCheckSpaceRolePermissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$ LANGUAGE plpgsql IMMUTABLE;


