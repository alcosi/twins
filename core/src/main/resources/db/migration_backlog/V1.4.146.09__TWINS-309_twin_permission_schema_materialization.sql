ALTER TABLE twin ADD COLUMN IF NOT EXISTS permission_schema_id UUID DEFAULT null;

DROP FUNCTION IF EXISTS hierarchyupdatetreehard(uuid, record);
create OR REPLACE function hierarchyupdatetreehard(p_twin_id uuid, detect_data record) returns void
    VOLATILE
    language plpgsql
as
$$
DECLARE
    data_to_use RECORD;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detect_data IS NOT NULL THEN
        data_to_use := detect_data;
    ELSE
        data_to_use := public.hierarchyDetectTree(p_twin_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', data_to_use.hierarchy;

    -- update hier. and schemas for twin-in
    UPDATE twin t
    SET hierarchy_tree = text2ltree(data_to_use.hierarchy),
        permission_schema_space_id = data_to_use.permission_schema_space_id,
        twinflow_schema_space_id = data_to_use.twinflow_schema_space_id,
        twin_class_schema_space_id = data_to_use.twin_class_schema_space_id,
        alias_space_id = data_to_use.alias_space_id,
        permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id)
    FROM twin_class tc
        JOIN domain d ON tc.domain_id = d.id
        LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id and dbu.business_account_id is not distinct from t.owner_business_account_id
        LEFT JOIN space s ON s.twin_id is not distinct from data_to_use.permission_schema_space_id
    WHERE t.id = p_twin_id and t.twin_class_id = tc.id;

    -- update hier. and schemas for twin-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM twin
        WHERE head_twin_id = p_twin_id
        UNION ALL
        SELECT t.id, d.depth + 1
        FROM twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
        WHERE d.depth < 10
    ), updated_data AS (
        SELECT dt.id, (hierarchyDetectTree(dt.id)).* -- use function and expand result
        FROM descendants dt
    )
    UPDATE twin t
    SET hierarchy_tree = text2ltree(ud.hierarchy),
        permission_schema_space_id = ud.permission_schema_space_id,
        twinflow_schema_space_id = ud.twinflow_schema_space_id,
        twin_class_schema_space_id = ud.twin_class_schema_space_id,
        alias_space_id = ud.alias_space_id,
        permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id)
    FROM updated_data ud
        JOIN twin_class tc ON t.twin_class_id = tc.id
        JOIN domain d ON tc.domain_id = d.id
        LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id and dbu.business_account_id is not distinct from t.owner_business_account_id
        LEFT JOIN space s ON s.twin_id is not distinct from data_to_use.permission_schema_space_id
    WHERE t.id = ud.id;
END;
$$;

-- update all twins
UPDATE twin t
SET permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id)
FROM twin_class tc
         JOIN domain d ON tc.domain_id = d.id
         LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id
         LEFT JOIN space s ON true -- Здесь мы не можем сослаться на t, поэтому используем фильтр ниже
WHERE t.twin_class_id = tc.id
  AND (dbu.business_account_id IS NOT DISTINCT FROM t.owner_business_account_id OR dbu.business_account_id IS NULL)
  AND (s.twin_id IS NOT DISTINCT FROM t.permission_schema_space_id OR s.twin_id IS NULL);



ALTER TABLE twin alter COLUMN permission_schema_id set not null;




