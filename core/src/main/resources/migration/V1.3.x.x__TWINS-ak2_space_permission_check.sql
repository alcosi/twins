DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'twin_class' AND column_name = 'space') THEN
            ALTER TABLE twin_class RENAME COLUMN space TO permission_schema_space;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_class'::regclass AND attname = 'twinflow_schema_space' AND attnum > 0 AND NOT attisdropped) THEN
            ALTER TABLE twin_class ADD COLUMN twinflow_schema_space BOOLEAN DEFAULT FALSE;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_class'::regclass AND attname = 'twin_class_schema_space' AND attnum > 0 AND NOT attisdropped) THEN
            ALTER TABLE twin_class ADD COLUMN twin_class_schema_space BOOLEAN DEFAULT FALSE;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_class'::regclass AND attname = 'alias_space' AND attnum > 0 AND NOT attisdropped) THEN
            ALTER TABLE twin_class ADD COLUMN alias_space BOOLEAN DEFAULT FALSE;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_class'::regclass AND attname = 'view_permission_id' AND attnum > 0 AND NOT attisdropped) THEN
            ALTER TABLE twin_class ADD COLUMN view_permission_id UUID;
        END IF;

        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'twin' AND column_name = 'view_permission_id') THEN
            ALTER TABLE twin ADD COLUMN view_permission_id UUID;
--              ALTER TABLE twin ADD CONSTRAINT fk_twin_view_permission_id FOREIGN KEY (view_permission_id) REFERENCES space(twin_id);
        END IF;

        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'twin' AND column_name = 'permission_schema_space_id') THEN
            ALTER TABLE twin ADD COLUMN permission_schema_space_id UUID;
            ALTER TABLE twin ADD CONSTRAINT fk_twin_permission_schema_space_id FOREIGN KEY (permission_schema_space_id) REFERENCES space(twin_id);
        END IF;

        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'twin' AND column_name = 'twinflow_schema_space_id') THEN
            ALTER TABLE twin ADD COLUMN twinflow_schema_space_id UUID;
            ALTER TABLE twin ADD CONSTRAINT fk_twin_twinflow_schema_space_id FOREIGN KEY (twinflow_schema_space_id) REFERENCES space(twin_id);
        END IF;

        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'twin' AND column_name = 'twin_class_schema_space_id') THEN
            ALTER TABLE twin ADD COLUMN twin_class_schema_space_id UUID;
            ALTER TABLE twin ADD CONSTRAINT fk_twin_twin_class_schema_space_id FOREIGN KEY (twin_class_schema_space_id) REFERENCES space(twin_id);
        END IF;

        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'twin' AND column_name = 'alias_space_id') THEN
            ALTER TABLE twin ADD COLUMN alias_space_id UUID;
            ALTER TABLE twin ADD CONSTRAINT fk_twin_alias_space_id FOREIGN KEY (alias_space_id) REFERENCES space(twin_id);
        END IF;
    END $$;
ALTER TABLE twin_class ALTER COLUMN permission_schema_space SET DEFAULT FALSE;

DROP TRIGGER IF EXISTS updateHierarchyTreeTrigger ON public.twin;
DROP TRIGGER IF EXISTS hierarchyUpdateTreeTrigger ON public.twin;
DROP TRIGGER IF EXISTS trigger_recalculateHierarchy ON public.twin_class;
DROP TRIGGER IF EXISTS hierarchyRecalculateTrigger ON public.twin_class;
DROP FUNCTION IF EXISTS public.ltree_check(ltree, text);
DROP FUNCTION IF EXISTS public.hierarchyCheck(ltree, text);
DROP FUNCTION IF EXISTS public.detectHierarchyTree(UUID);
DROP FUNCTION IF EXISTS public.detectHierarchyTree(UUID);
DROP FUNCTION IF EXISTS public.hierarchyDetectTree(UUID);
DROP FUNCTION IF EXISTS public.permissionCheckBySchema(UUID, UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permissionCheckBySchema(UUID, UUID, UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permissionGetRoles(UUID, UUID);
DROP FUNCTION IF EXISTS public.permissionDetectSchema(UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID[]);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID, UUID[]);
DROP FUNCTION IF EXISTS public.updateHierarchyTreeHard(UUID, TEXT);
DROP FUNCTION IF EXISTS public.updateHierarchyTreeSoft(UUID, TEXT);
DROP FUNCTION IF EXISTS public.updateHierarchyTreeHard(UUID, RECORD);
DROP FUNCTION IF EXISTS public.hierarchyUpdateTreeHard(UUID, RECORD);
DROP FUNCTION IF EXISTS public.updateHierarchyTreeSoft(UUID, RECORD);
DROP FUNCTION IF EXISTS public.hierarchyUpdateTreeSoft(UUID, RECORD);
DROP FUNCTION IF EXISTS public.processHierarchyTreeUpdate();
DROP FUNCTION IF EXISTS public.hierarchyProcessTreeUpdate();
DROP FUNCTION IF EXISTS public.recalculateHierarchyForClassTwins();
DROP FUNCTION IF EXISTS public.hierarchyRecalculateForClassTwins();

CREATE OR REPLACE FUNCTION public.hierarchyDetectTree(twin_id UUID)
    RETURNS TABLE(
                     hierarchy TEXT,
                     permission_schema_space_id UUID,
                     twinflow_schema_space_id UUID,
                     twin_class_schema_space_id UUID,
                     alias_space_id UUID
                 ) AS $$
DECLARE
    current_id UUID := twin_id;
    parent_id UUID;
    visited_ids UUID[] := ARRAY[twin_id];
    local_permission_schema_space_id UUID;
    local_twinflow_schema_space_id UUID;
    local_twin_class_schema_space_id UUID;
    local_alias_space_id UUID;
    local_permission_schema_space BOOLEAN;
    local_twinflow_schema_space BOOLEAN;
    local_twin_class_schema_space BOOLEAN;
    local_alias_space BOOLEAN;
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
        -- get parent_id and shema flags for twin-in
        SELECT t.head_twin_id, t.permission_schema_space_id, t.twinflow_schema_space_id, t.twin_class_schema_space_id, t.alias_space_id,
               tc.permission_schema_space, tc.twinflow_schema_space, tc.twin_class_schema_space, tc.alias_space
        INTO parent_id,
            local_permission_schema_space_id, local_twinflow_schema_space_id, local_twin_class_schema_space_id, local_alias_space_id,
            local_permission_schema_space, local_twinflow_schema_space, local_twin_class_schema_space, local_alias_space
        FROM public.twin t LEFT JOIN public.twin_class tc ON t.twin_class_id = tc.id WHERE t.id = current_id;

        -- check for cycle
        IF parent_id = ANY(visited_ids) THEN
            RAISE EXCEPTION 'Cycle detected in hierarchy for twin_id %', twin_id;
        END IF;

        -- update schema ids, if it is enabled on class and return value not null
        IF permission_schema_space_id IS NULL AND local_permission_schema_space IS TRUE
            THEN permission_schema_space_id := local_permission_schema_space_id;
        END IF;
        IF twinflow_schema_space_id IS NULL AND local_twinflow_schema_space IS TRUE
            THEN twinflow_schema_space_id := local_twinflow_schema_space_id;
        END IF;
        IF twin_class_schema_space_id IS NULL AND local_twin_class_schema_space IS TRUE
            THEN twin_class_schema_space_id := local_twin_class_schema_space_id;
        END IF;
        IF alias_space_id IS NULL AND local_alias_space IS TRUE
            THEN alias_space_id := local_alias_space_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN QUERY SELECT hierarchy, permission_schema_space_id, twinflow_schema_space_id, twin_class_schema_space_id, alias_space_id;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchyUpdateTreeHard(twin_id UUID, detect_data RECORD)
    RETURNS VOID AS $$
DECLARE
    data_to_use RECORD;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detect_data IS NOT NULL THEN
        data_to_use := detect_data;
    ELSE
        data_to_use := public.hierarchyDetectTree(twin_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', data_to_use.hierarchy;

    -- update hier. for twin-in
    UPDATE public.twin
    SET hierarchy_tree = text2ltree(data_to_use.hierarchy),
        permission_schema_space_id = data_to_use.permission_schema_space_id,
        twinflow_schema_space_id = data_to_use.twinflow_schema_space_id,
        twin_class_schema_space_id = data_to_use.twin_class_schema_space_id,
        alias_space_id = data_to_use.alias_space_id
    WHERE id = twin_id;

    -- update hier. for twin-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin
        WHERE head_twin_id = twin_id
        UNION ALL
        SELECT t.id, d.depth + 1
        FROM public.twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
        WHERE d.depth < 10
    ), updated_data AS (
        SELECT dt.id, (public.hierarchyDetectTree(dt.id)).* -- use function and expand result
        FROM descendants dt
    )
    UPDATE public.twin t
    SET hierarchy_tree = text2ltree(ud.hierarchy),
        permission_schema_space_id = ud.permission_schema_space_id,
        twinflow_schema_space_id = ud.twinflow_schema_space_id,
        twin_class_schema_space_id = ud.twin_class_schema_space_id,
        alias_space_id = ud.alias_space_id
    FROM updated_data ud
    WHERE t.id = ud.id;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchyUpdateTreeSoft(twin_id UUID, detect_data RECORD)
    RETURNS VOID AS $$
DECLARE
    data_to_use RECORD;
    old_hierarchy TEXT;
    new_hierarchy TEXT;
BEGIN
    RAISE NOTICE 'DATA: %', detect_data;

    SELECT hierarchy_tree::text INTO old_hierarchy FROM public.twin WHERE id = twin_id;

    -- if hier. in params - use it. if not - detect it and use.
    IF detect_data IS NOT NULL THEN
        data_to_use := detect_data;
    ELSE
        data_to_use := public.hierarchyDetectTree(twin_id);
    END IF;

    new_hierarchy := data_to_use.hierarchy;

    RAISE NOTICE 'NEW: %', new_hierarchy;
    RAISE NOTICE 'OLD: %', old_hierarchy;

    UPDATE public.twin
    SET hierarchy_tree = text2ltree(replace(hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchyProcessTreeUpdate()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
            PERFORM public.hierarchyUpdateTreeSoft(NEW.id, public.hierarchyDetectTree(NEW.id));
        ELSE
            PERFORM public.hierarchyUpdateTreeSoft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.hierarchyUpdateTreeHard(NEW.id, public.hierarchyDetectTree(NEW.id));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER hierarchyUpdateTreeTrigger
    AFTER INSERT OR UPDATE OF head_twin_id ON public.twin
    FOR EACH ROW
EXECUTE FUNCTION public.hierarchyProcessTreeUpdate();



CREATE OR REPLACE FUNCTION hierarchyCheck(hierarchy_tree ltree, ltree_value text)
    RETURNS boolean AS $$
BEGIN
    RETURN hierarchy_tree ~ ltree_value::lquery;
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;



-- TWIN-CLASS SECTION------------------------------------------------------------------------------------
-- if space field in twin class changed trigger update... function for all twin-descendants of this class
CREATE OR REPLACE FUNCTION public.hierarchyRecalculateForClassTwins()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.permission_schema_space IS DISTINCT FROM NEW.permission_schema_space OR
       OLD.twinflow_schema_space IS DISTINCT FROM NEW.twinflow_schema_space OR
       OLD.twin_class_schema_space IS DISTINCT FROM NEW.twin_class_schema_space OR
       OLD.alias_space IS DISTINCT FROM NEW.alias_space
    THEN
        PERFORM public.hierarchyUpdateTreeHard(t.id, NULL)
        FROM public.twin t
        WHERE t.twin_class_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER hierarchyRecalculateTrigger
    AFTER UPDATE OF permission_schema_space, twinflow_schema_space, twin_class_schema_space, alias_space ON public.twin_class
    FOR EACH ROW
EXECUTE FUNCTION public.hierarchyRecalculateForClassTwins();

-------------------------------------------------------------------------
-- call update... function for all root-twins, to update all twin's hier.
DO $$
    DECLARE
        root_twin RECORD;
    BEGIN
        FOR root_twin IN SELECT id FROM public.twin WHERE head_twin_id IS NULL LOOP
                PERFORM public.hierarchyUpdateTreeHard(root_twin.id, NULL);
            END LOOP;
    END $$;







CREATE OR REPLACE FUNCTION permissionCheckBySchema(permissionSchemaId UUID, permissionIdTwin UUID, permissionIdTwinClass UUID, userId UUID, userGroupIdList UUID[])
    RETURNS BOOLEAN AS $$
DECLARE
    userPermissionExists INT;
    groupPermissionExists INT;
    permissionId_for_use UUID := permissionIdTwinClass;
BEGIN
    IF permissionId_for_use IS NULL THEN
        permissionId_for_use = permissionIdTwin;
    END IF;

    SELECT COUNT(id) INTO userPermissionExists
    FROM permission_schema_user
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionId_for_use
      AND user_id = userId;

    IF userPermissionExists > 0 THEN
        RETURN TRUE;
    END IF;

    SELECT COUNT(id) INTO groupPermissionExists
    FROM permission_schema_user_group
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionId
      AND user_group_id = ANY(userGroupIdList);

    RETURN groupPermissionExists > 0;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


CREATE OR REPLACE FUNCTION permissionGetRoles(permissionSchemaId UUID, permissionId UUID)
    RETURNS TABLE(space_role_id UUID) AS $$
SELECT pssr.space_role_id
FROM permission_schema_space_roles pssr
WHERE pssr.permission_schema_id = permissionSchemaId
  AND pssr.permission_id = permissionId;
$$ LANGUAGE sql IMMUTABLE;


CREATE OR REPLACE FUNCTION permissionDetectSchema(domainId UUID, businessAccountId UUID, spaceId UUID)
    RETURNS UUID AS $$
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
    IF businessAccountId IS NOT NULL AND spaceId IS NULL THEN
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
$$ LANGUAGE plpgsql IMMUTABLE;


CREATE OR REPLACE FUNCTION permissionCheck(
    domainId UUID,
    businessAccountId UUID,
    spaceId UUID,
    permissionIdTwin UUID,
    permissionIdTwinClass UUID,
    userId UUID,
    userGroupIdList UUID[]
)
    RETURNS BOOLEAN AS $$
DECLARE
    permissionSchemaId UUID;
    userAssignedToRoleExists INT;
    groupAssignedToRoleExists INT;
BEGIN
    IF permissionIdTwin IS NULL AND permissionIdTwinClass  IS NULL THEN
        RETURN TRUE;
    END IF;

    -- Detect permission schema
    permissionSchemaId := permissionDetectSchema(domainId, businessAccountId, spaceId);

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check direct user or user group permissions
    IF permissionCheckBySchema(permissionSchemaId, permissionIdTwin, permissionIdTwinClass, userId, userGroupIdList) THEN
        RETURN TRUE;
    END IF;

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check if any space role assigned to the user has the given permission
    SELECT COUNT(spru.id) INTO userAssignedToRoleExists
    FROM space_role_user spru
    WHERE spru.twin_id = spaceId
      AND spru.user_id = userId
      AND spru.space_role_id IN (SELECT space_role_id FROM permissionGetRoles(permissionSchemaId, permissionId));

    IF userAssignedToRoleExists > 0 THEN
        RETURN TRUE;
    END IF;

    -- Check if any space role assigned to the user's group has the given permission
    SELECT COUNT(sprug.id) INTO groupAssignedToRoleExists
    FROM space_role_user_group sprug
    WHERE sprug.twin_id = spaceId
      AND sprug.user_group_id = ANY(userGroupIdList)
      AND sprug.space_role_id IN (SELECT space_role_id FROM permissionGetRoles(permissionSchemaId, permissionId));

    RETURN groupAssignedToRoleExists > 0;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
