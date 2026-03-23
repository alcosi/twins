WITH recalculated AS (
    SELECT
        t.id,
        d.hierarchy,
        d.permission_schema_space_id,
        d.twinflow_schema_space_id,
        d.twin_class_schema_space_id,
        d.alias_space_id,
        d.permission_schema_id
    FROM twin t
             CROSS JOIN LATERAL hierarchydetecttree(t.id) d
),
mismatch AS (
    SELECT
        r.id,
        r.hierarchy,
        r.permission_schema_space_id,
        r.twinflow_schema_space_id,
        r.twin_class_schema_space_id,
        r.alias_space_id,
        r.permission_schema_id
    FROM recalculated r
             JOIN twin t ON t.id = r.id
    WHERE t.hierarchy_tree IS DISTINCT FROM text2ltree(r.hierarchy)
       OR t.permission_schema_space_id IS DISTINCT FROM r.permission_schema_space_id
       OR t.twinflow_schema_space_id IS DISTINCT FROM r.twinflow_schema_space_id
       OR t.twin_class_schema_space_id IS DISTINCT FROM r.twin_class_schema_space_id
       OR t.alias_space_id IS DISTINCT FROM r.alias_space_id
       OR t.permission_schema_id IS DISTINCT FROM r.permission_schema_id
)
UPDATE twin t
SET hierarchy_tree = text2ltree(m.hierarchy),
    permission_schema_space_id = m.permission_schema_space_id,
    twinflow_schema_space_id = m.twinflow_schema_space_id,
    twin_class_schema_space_id = m.twin_class_schema_space_id,
    alias_space_id = m.alias_space_id,
    permission_schema_id = m.permission_schema_id
FROM mismatch m
WHERE t.id = m.id;
