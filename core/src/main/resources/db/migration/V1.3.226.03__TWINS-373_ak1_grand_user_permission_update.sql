ALTER TABLE public.permission_grant_user
    DROP CONSTRAINT IF EXISTS permission_grant_user_domain_id_fk;
ALTER TABLE public.permission_grant_user
    DROP CONSTRAINT IF EXISTS permission_grant_user_business_account_id_fk;

-- domain

ALTER TABLE public.permission_grant_user
    ADD COLUMN IF NOT EXISTS domain_id uuid;

UPDATE public.permission_grant_user pgu SET domain_id = du.domain_id
FROM public.domain_user du WHERE pgu.user_id = du.user_id;

ALTER TABLE public.permission_grant_user
    ALTER COLUMN domain_id SET NOT NULL;

ALTER TABLE ONLY public.permission_grant_user
    ADD CONSTRAINT permission_grant_user_domain_id_fk
        FOREIGN KEY (domain_id) REFERENCES public.domain(id);

CREATE INDEX IF NOT EXISTS idx_permission_grant_user_domain_id
    ON public.permission_grant_user (domain_id);


-- BA

ALTER TABLE public.permission_grant_user
    ADD COLUMN IF NOT EXISTS business_account_id uuid;

ALTER TABLE ONLY public.permission_grant_user
    ADD CONSTRAINT permission_grant_user_business_account_id_fk
        FOREIGN KEY (business_account_id) REFERENCES public.business_account(id);

CREATE INDEX IF NOT EXISTS idx_permission_grant_user_business_account_id
    ON public.permission_grant_user (business_account_id);

DROP FUNCTION IF EXISTS public.permission_check_by_grant_group_or_user(uuid, uuid, uuid, uuid[]);
DROP FUNCTION IF EXISTS public.permission_check_by_grant_group_or_user(uuid, uuid, uuid, uuid[], uuid, uuid);

CREATE OR REPLACE FUNCTION permission_check_by_group_or_user(
    permissionSchemaId uuid,
    permissionIdForUse uuid,
    userId uuid,
    userGroupIdList uuid[],
    domainId uuid,
    businessAccountId uuid
) RETURNS BOOLEAN AS $$
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
$$ IMMUTABLE LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS public.permission_check(uuid, uuid, uuid, uuid, uuid, uuid[], uuid, boolean, boolean);
create OR REPLACE function permission_check(domainId uuid, businessAccountId uuid, spaceId uuid, permissionId uuid, userId uuid, userGroupIdList uuid[], twinClassId uuid, isAssignee boolean DEFAULT false, isCreator boolean DEFAULT false) returns boolean as $$

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
$$ immutable language plpgsql;

