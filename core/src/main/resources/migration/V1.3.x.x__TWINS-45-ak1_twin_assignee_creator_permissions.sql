DROP FUNCTION IF EXISTS public.hierarchyDetectTree(UUID);

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
