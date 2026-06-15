INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1352::integer, 13::integer, '', '', '', DEFAULT) on conflict do nothing;

ALTER TABLE twin ADD COLUMN IF NOT EXISTS type_option_id uuid REFERENCES data_list_option(id);

CREATE INDEX IF NOT EXISTS idx_twin_type_option_id ON twin(type_option_id);

DROP FUNCTION IF EXISTS twin_field_decimal_calc_sum_by_link_with_twin_type(uuid[],uuid[],jsonb,boolean,uuid[],uuid[],boolean,boolean);

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_by_link_with_twin_type(
    p_twin_ids uuid[],
    p_link_ids uuid[],
    field_id_by_twin_type jsonb,
    p_src_else_dst boolean,
    p_linked_twin_in_status_ids uuid[] DEFAULT NULL,
    p_linked_twin_of_class_ids uuid[] DEFAULT NULL,
    p_status_exclude boolean DEFAULT false,
    p_skip_if_not_found boolean DEFAULT true
)
RETURNS TABLE (twin_id uuid, calc numeric)
LANGUAGE plpgsql
AS $$
DECLARE
    v_missing_types TEXT[];
BEGIN
    -- Validation for strict mode
    IF NOT p_skip_if_not_found THEN
        SELECT INTO v_missing_types
        ARRAY_AGG(DISTINCT t.type_option_id::text)
        FROM twin_link tl
        INNER JOIN twin t ON (
            CASE WHEN p_src_else_dst THEN tl.dst_twin_id ELSE tl.src_twin_id END = t.id
        )
        WHERE (p_link_ids IS NULL OR array_length(p_link_ids, 1) IS NULL OR tl.link_id = ANY(p_link_ids))
          AND (tl.src_twin_id = ANY(p_twin_ids) OR tl.dst_twin_id = ANY(p_twin_ids))
          AND CASE
                  WHEN p_src_else_dst THEN tl.src_twin_id = ANY(p_twin_ids)
                  ELSE tl.dst_twin_id = ANY(p_twin_ids)
              END = true
          AND t.type_option_id IS NOT NULL
          AND (p_linked_twin_in_status_ids IS NULL
              OR array_length(p_linked_twin_in_status_ids, 1) = 0
              OR p_linked_twin_in_status_ids = '{}'
              OR (NOT p_status_exclude AND t.twin_status_id = ANY(p_linked_twin_in_status_ids))
              OR (p_status_exclude AND t.twin_status_id != ALL(p_linked_twin_in_status_ids)))
          AND (p_linked_twin_of_class_ids IS NULL
              OR array_length(p_linked_twin_of_class_ids, 1) = 0
              OR p_linked_twin_of_class_ids = '{}'
              OR t.twin_class_id = ANY(p_linked_twin_of_class_ids))
          AND (field_id_by_twin_type ->> t.type_option_id::text IS NULL
              OR (field_id_by_twin_type ->> t.type_option_id::text) ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' IS FALSE);

        IF v_missing_types IS NOT NULL AND array_length(v_missing_types, 1) > 0 THEN
            RAISE EXCEPTION 'Type options not found in fieldIdByTwinTypeId map or invalid UUID: %',
                array_to_string(v_missing_types, ', ');
        END IF;
    END IF;

    RETURN QUERY
    -- Main optimization: unpack JSON once in CTE
    WITH field_map AS (
        SELECT
            key::uuid AS type_option_id,
            value::uuid AS field_id
        FROM jsonb_each_text(field_id_by_twin_type)
    ),
    linked_twins AS (
        SELECT
            CASE WHEN p_src_else_dst THEN tl.dst_twin_id ELSE tl.src_twin_id END AS linked_to_id,
            CASE WHEN p_src_else_dst THEN tl.src_twin_id ELSE tl.dst_twin_id END AS linked_from_id
        FROM twin_link tl
        WHERE (p_link_ids IS NULL OR array_length(p_link_ids, 1) IS NULL OR tl.link_id = ANY(p_link_ids))
          AND (tl.src_twin_id = ANY(p_twin_ids) OR tl.dst_twin_id = ANY(p_twin_ids))
          AND CASE
                  WHEN p_src_else_dst THEN tl.src_twin_id = ANY(p_twin_ids)
                  ELSE tl.dst_twin_id = ANY(p_twin_ids)
              END = true
    ),
    filtered_twins AS (
        SELECT
            lt.linked_to_id,
            lt.linked_from_id,
            t.type_option_id,
            fm.field_id
        FROM linked_twins lt
        INNER JOIN twin t ON t.id = lt.linked_to_id
        -- JOIN instead of repeated JSON lookups
        LEFT JOIN field_map fm ON fm.type_option_id = t.type_option_id
        WHERE t.type_option_id IS NOT NULL
          AND (p_linked_twin_in_status_ids IS NULL
              OR array_length(p_linked_twin_in_status_ids, 1) = 0
              OR p_linked_twin_in_status_ids = '{}'
              OR (NOT p_status_exclude AND t.twin_status_id = ANY(p_linked_twin_in_status_ids))
              OR (p_status_exclude AND t.twin_status_id != ALL(p_linked_twin_in_status_ids)))
          AND (p_linked_twin_of_class_ids IS NULL
              OR array_length(p_linked_twin_of_class_ids, 1) = 0
              OR p_linked_twin_of_class_ids = '{}'
              OR t.twin_class_id = ANY(p_linked_twin_of_class_ids))
          AND (p_skip_if_not_found OR fm.field_id IS NOT NULL)
    ),
    field_values AS (
        SELECT
            ft.linked_from_id,
            COALESCE(tfd.value, 0) AS field_val
        FROM filtered_twins ft
        LEFT JOIN twin_field_decimal tfd
            ON tfd.twin_id = ft.linked_to_id
           AND tfd.twin_class_field_id = ft.field_id
        WHERE ft.field_id IS NOT NULL
    )
    SELECT
        req.twin_id,
        COALESCE(SUM(fv.field_val), 0)::numeric
    FROM unnest(p_twin_ids) AS req(twin_id)
    LEFT JOIN field_values fv ON fv.linked_from_id = req.twin_id
    GROUP BY req.twin_id;
END;
$$;
