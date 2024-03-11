CREATE TABLE IF NOT EXISTS twin_role
(
    id VARCHAR PRIMARY KEY
);
INSERT INTO twin_role (id)
VALUES ('assignee'),
       ('creator'),
       ('watcher')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS permission_schema_twin_role
(
    id                   UUID NOT NULL CONSTRAINT permission_schema_twin_role_pk PRIMARY KEY,
    permission_schema_id UUID REFERENCES permission_schema (id),
    permission_id        UUID REFERENCES permission (id),
    twin_class_id        UUID REFERENCES twin_class (id),
    twin_role_id         VARCHAR REFERENCES twin_role (id),
    granted_by_user_id   UUID REFERENCES "user" (id),
    granted_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- GRANT SELECT, INSERT, UPDATE, DELETE ON permission_schema_twin_role TO elp_user; ---TODO?????

DROP FUNCTION IF EXISTS public.hierarchyDetectTree(UUID);

DROP FUNCTION IF EXISTS public.permissionCheckAssigneAndCreator(UUID, UUID, BOOLEAN, BOOLEAN, UUID);
DROP FUNCTION IF EXISTS public.permissionCheckAssigneeAndCreator(UUID, UUID, BOOLEAN, BOOLEAN, UUID);

DROP FUNCTION IF EXISTS public.permissionCheckSpaceRolePermissions(UUID, UUID, UUID, UUID, UUID[]);

DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID[]);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID, UUID[]);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID[], UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID, UUID[], UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);

CREATE OR REPLACE FUNCTION public.hierarchyDetectTree(twin_id UUID)
    RETURNS TABLE
            (
                hierarchy                  TEXT,
                permission_schema_space_id UUID,
                twinflow_schema_space_id   UUID,
                twin_class_schema_space_id UUID,
                alias_space_id             UUID
            )
AS
$$
DECLARE
    current_id                            UUID   := twin_id;
    parent_id                             UUID;
    visited_ids                           UUID[] := ARRAY [twin_id];
    local_permission_schema_space_enabled BOOLEAN;
    local_twinflow_schema_space_enabled   BOOLEAN;
    local_twin_class_schema_space_enabled BOOLEAN;
    local_alias_space_enabled             BOOLEAN;

BEGIN
    RAISE NOTICE 'Detected hier. for id: %', twin_id;
    -- return values init
    hierarchy := '';
    permission_schema_space_id := NULL;
    twinflow_schema_space_id := NULL;
    twin_class_schema_space_id := NULL;
    alias_space_id := NULL;

    -- cycle for build hierarchy form twin-in to twin-root
    LOOP
        -- get parent_id and shema flags and check space_schema_id is present for twin-in
        SELECT t.head_twin_id,
               tc.permission_schema_space,
               tc.twinflow_schema_space,
               tc.twin_class_schema_space,
               tc.alias_space
        INTO parent_id,
            local_permission_schema_space_enabled,
            local_twinflow_schema_space_enabled,
            local_twin_class_schema_space_enabled,
            local_alias_space_enabled
        FROM public.twin t LEFT JOIN public.twin_class tc ON t.twin_class_id = tc.id WHERE t.id = current_id;

        -- check for cycle
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_id %', twin_id;
        END IF;

        -- update schema ids, if it is enabled on class and return value not null
        IF permission_schema_space_id IS NULL AND local_permission_schema_space_enabled IS TRUE THEN permission_schema_space_id := current_id;
        END IF;
        IF twinflow_schema_space_id IS NULL AND local_twinflow_schema_space_enabled IS TRUE THEN twinflow_schema_space_id := current_id;
        END IF;
        IF twin_class_schema_space_id IS NULL AND local_twin_class_schema_space_enabled IS TRUE THEN twin_class_schema_space_id := current_id;
        END IF;
        IF alias_space_id IS NULL AND local_alias_space_enabled IS TRUE THEN alias_space_id := current_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy :=
                replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    IF permission_schema_space_id IS NULL THEN permission_schema_space_id := twin_id;
    END IF;
    IF twinflow_schema_space_id IS NULL THEN twinflow_schema_space_id := twin_id;
    END IF;
    IF twin_class_schema_space_id IS NULL THEN twin_class_schema_space_id := twin_id;
    END IF;
    IF alias_space_id IS NULL THEN alias_space_id := twin_id;
    END IF;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN QUERY SELECT hierarchy, permission_schema_space_id, twinflow_schema_space_id, twin_class_schema_space_id, alias_space_id;
END;
$$ LANGUAGE plpgsql;


-- one time full recalculation of twin's hier.
DO
$$
    DECLARE
        root_twin RECORD;
    BEGIN
        FOR root_twin IN SELECT id FROM public.twin WHERE head_twin_id IS NULL
            LOOP
                PERFORM public.hierarchyUpdateTreeHard(root_twin.id, NULL);
            END LOOP;
    END
$$;


CREATE OR REPLACE FUNCTION permissionCheckAssigneeAndCreator(
    permissionSchemaId UUID,
    permissionId UUID,
    isAssignee BOOLEAN,
    isCreator BOOLEAN,
    twinClassId UUID
)
    RETURNS BOOLEAN AS
$$
DECLARE
    hasPermission BOOLEAN := FALSE;
BEGIN
    -- check assignee permission
    IF isAssignee THEN
        SELECT EXISTS (
            SELECT 1
            FROM permission_schema_twin_role
            WHERE permission_schema_id = permissionSchemaId
              AND permission_id = permissionId
              AND twin_class_id = twinClassId
              AND twin_role_id = 'assignee'
        ) INTO hasPermission;
        IF hasPermission THEN
            RETURN TRUE;
        END IF;
    END IF;

    -- check assignee permission
    IF isCreator THEN
        SELECT EXISTS (
            SELECT 1
            FROM permission_schema_twin_role
            WHERE permission_schema_id = permissionSchemaId
              AND permission_id = permissionId
              AND twin_class_id = twinClassId
              AND twin_role_id = 'creator'
        ) INTO hasPermission;
        IF hasPermission THEN
            RETURN TRUE;
        END IF;
    END IF;

    RETURN FALSE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


CREATE OR REPLACE FUNCTION permissionCheckSpaceRolePermissions(
    permissionSchemaId UUID,
    permissionId UUID,
    spaceId UUID,
    userId UUID,
    userGroupIdList UUID[]
)
    RETURNS BOOLEAN AS
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
      AND sru.space_role_id IN (SELECT space_role_id FROM permissionGetRoles(permissionSchemaId, permissionId));

    IF userAssignedToRoleExists > 0 THEN
        RETURN TRUE;
    END IF;

    -- Check if any space role assigned to the user's group has the given permission
    SELECT COUNT(srug.id)
    INTO groupAssignedToRoleExists
    FROM space_role_user_group srug
    WHERE srug.twin_id = spaceId
      AND srug.user_group_id = ANY (userGroupIdList)
      AND srug.space_role_id IN (SELECT space_role_id FROM permissionGetRoles(permissionSchemaId, permissionId));

    RETURN groupAssignedToRoleExists > 0;
END;
$$ LANGUAGE plpgsql IMMUTABLE;



CREATE OR REPLACE FUNCTION permissionCheck(
    domainId UUID,
    businessAccountId UUID,
    spaceId UUID,
    permissionIdTwin UUID,
    permissionIdTwinClass UUID,
    userId UUID,
    userGroupIdList UUID[],
    twinClassId UUID,
    isAssignee BOOLEAN DEFAULT FALSE,
    isCreator BOOLEAN DEFAULT FALSE
)
    RETURNS BOOLEAN AS
$$
DECLARE
    permissionIdForUse UUID := permissionIdTwin;
BEGIN
    IF permissionIdForUse IS NULL THEN
        permissionIdForUse = permissionIdTwinClass;
    END IF;
    RETURN permissionCheck(domainId, businessAccountId, spaceId, permissionIdForUse, userId, userGroupIdList, twinClassId, isAssignee, isCreator);
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

    -- Check assigne or creator permissions
    IF permissionCheckAssigneeAndCreator(permissionSchemaId, permissionId, isAssignee, isCreator, twinClassId) THEN
        RETURN TRUE;
    END IF;

    -- Check direct user or user group permissions
    IF permissionCheckBySchema(permissionSchemaId, permissionId, userId, userGroupIdList) THEN
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


