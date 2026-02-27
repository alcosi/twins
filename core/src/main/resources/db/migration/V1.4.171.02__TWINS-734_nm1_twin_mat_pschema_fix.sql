create or replace function hierarchydetecttree(p_twin_id uuid)
    returns TABLE(
                     hierarchy text,
                     permission_schema_space_id uuid,
                     twinflow_schema_space_id uuid,
                     twin_class_schema_space_id uuid,
                     alias_space_id uuid,
                     permission_schema_id uuid,
                     twinflow_schema_id uuid,
                     twin_class_schema_id uuid
                 )
    language plpgsql
as
$$
DECLARE
    current_id  UUID   := p_twin_id;
    parent_id   UUID;
    visited_ids UUID[] := ARRAY[p_twin_id];
    local_permission_schema_space_enabled BOOLEAN;
    local_twinflow_schema_space_enabled   BOOLEAN;
    local_twin_class_schema_space_enabled BOOLEAN;
    local_alias_space_enabled             BOOLEAN;

    -- локальные переменные для финального SELECT, чтобы убрать ambiguity
    v_permission_schema_space_id uuid;
    v_twinflow_schema_space_id uuid;
    v_twin_class_schema_space_id uuid;
BEGIN
    RAISE NOTICE 'Detected hier. for id: %', p_twin_id;

    -- init return values
    hierarchy := '';
    permission_schema_space_id := NULL;
    twinflow_schema_space_id := NULL;
    twin_class_schema_space_id := NULL;
    alias_space_id := NULL;
    permission_schema_id := NULL;
    twinflow_schema_id := NULL;
    twin_class_schema_id := NULL;

    LOOP
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
        FROM twin t
                 LEFT JOIN twin_class tc ON t.twin_class_id = tc.id
        WHERE t.id = current_id;

        -- cycle protection
        IF parent_id = ANY (visited_ids) THEN
            RAISE EXCEPTION 'Cycle detected in hierarchy for twin_id %', p_twin_id;
        END IF;

        -- detect first space in hierarchy
        IF permission_schema_space_id IS NULL AND local_permission_schema_space_enabled THEN
            permission_schema_space_id := current_id;
        END IF;
        IF twinflow_schema_space_id IS NULL AND local_twinflow_schema_space_enabled THEN
            twinflow_schema_space_id := current_id;
        END IF;
        IF twin_class_schema_space_id IS NULL AND local_twin_class_schema_space_enabled THEN
            twin_class_schema_space_id := current_id;
        END IF;
        IF alias_space_id IS NULL AND local_alias_space_enabled THEN
            alias_space_id := current_id;
        END IF;

        -- build ltree-compatible hierarchy
        hierarchy := replace(current_id::text, '-', '_') || CASE WHEN hierarchy = '' THEN '' ELSE '.' END || hierarchy;

        EXIT WHEN parent_id IS NULL;

        visited_ids := array_append(visited_ids, parent_id);
        current_id := parent_id;
    END LOOP;

    -- присваиваем локальные переменные для финального SELECT, чтобы убрать ambiguity
    v_permission_schema_space_id := permission_schema_space_id;
    v_twinflow_schema_space_id := twinflow_schema_space_id;
    v_twin_class_schema_space_id := twin_class_schema_space_id;

    -- single query to resolve schemas from computed spaces
    SELECT
        COALESCE(sp.permission_schema_id, dba.permission_schema_id, d.permission_schema_id, '00000000-0000-0000-0012-000000000001'::uuid),
        COALESCE(stf.twinflow_schema_id, dba.twinflow_schema_id, d.twinflow_schema_id, '00000000-0000-0000-0013-000000000001'::uuid),
        COALESCE(stc.twin_class_schema_id, dba.twin_class_schema_id, d.twin_class_schema_id, '00000000-0000-0000-0014-000000000001'::uuid)
    INTO
        permission_schema_id,
        twinflow_schema_id,
        twin_class_schema_id
    FROM twin t
             JOIN twin_class tc ON tc.id = t.twin_class_id
             LEFT JOIN space sp ON sp.twin_id = v_permission_schema_space_id
             LEFT JOIN space stf ON stf.twin_id = v_twinflow_schema_space_id
             LEFT JOIN space stc ON stc.twin_id = v_twin_class_schema_space_id
             LEFT JOIN domain d ON d.id = tc.domain_id
             LEFT JOIN domain_business_account dba
                       ON dba.domain_id = tc.domain_id
                           AND dba.business_account_id IS NOT DISTINCT FROM t.owner_business_account_id
    WHERE t.id = p_twin_id;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;

    RETURN QUERY
        SELECT
            hierarchy,
            permission_schema_space_id,
            twinflow_schema_space_id,
            twin_class_schema_space_id,
            alias_space_id,
            permission_schema_id,
            twinflow_schema_id,
            twin_class_schema_id;
END;
$$;

