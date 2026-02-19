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

    --     IF isAssignee THEN
--         roles := array_append(roles, 'assignee');
--     END IF;
--
--     IF isCreator THEN
--         roles := array_append(roles, 'creator');
--     END IF;
--
--     SELECT * INTO isSpaceAssignee, isSpaceCreator FROM permission_check_space_assignee_and_creator(spaceId, userId);

--     IF isSpaceAssignee THEN
--         roles := array_append(roles, 'space_assignee');
--     END IF;

--     IF isSpaceCreator THEN
--         roles := array_append(roles, 'space_creator');
--     END IF;

    -- Check twin-role permissions
--     IF permission_check_twin_role(permissionSchemaId, permissionId, roles, twinClassId) THEN
--         RETURN TRUE;
--     END IF;

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



DROP FUNCTION IF EXISTS permission_check_by_group_or_user(uuid, uuid, uuid, uuid[], uuid, uuid);
DROP FUNCTION IF EXISTS permission_check_space_assignee_and_creator(uuid, uuid, uuid, uuid[], uuid, uuid);


ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_creator BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_creator BOOLEAN DEFAULT FALSE;

UPDATE permission_grant_twin_role SET granted_to_assignee = TRUE WHERE twin_role_id = 'assignee';
UPDATE permission_grant_twin_role SET granted_to_creator = TRUE WHERE twin_role_id = 'creator';
UPDATE permission_grant_twin_role SET granted_to_space_assignee = TRUE WHERE twin_role_id = 'space_assignee';
UPDATE permission_grant_twin_role SET granted_to_space_creator = TRUE WHERE twin_role_id = 'space_creator';

ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_creator SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_creator SET NOT NULL;

ALTER TABLE permission_grant_twin_role DROP COLUMN IF EXISTS twin_role_id;

drop index if exists idx_permission_schema_twin_role_twinclass_schema_and_perm_id;
create unique index idx_permission_schema_twin_role_twinclass_schema_and_perm_id
    on permission_grant_twin_role (twin_class_id, permission_schema_id, permission_id);

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

DROP FUNCTION IF EXISTS permission_check_assignee_propagation(uuid, uuid, uuid, uuid, uuid);

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
