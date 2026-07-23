-- TWINS: optional assignee_user_id filter for calc-sum-by-link field typers (1345 / 1354).
-- When assignee_user_id is set: sum only linked twins with assigner_user_id = that UUID.
-- When assignee_user_id is null: include all linked twins (default / no ApiUser).

DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_sum_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    class_field_ids uuid[],
    link_ids uuid[],
    status_exclude boolean
);

DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_sum_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    class_field_ids uuid[],
    link_ids uuid[],
    status_exclude boolean,
    match_assignee boolean
);

CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_sum_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    class_field_ids uuid[],
    link_ids uuid[],
    status_exclude boolean DEFAULT false,
    assignee_user_id uuid DEFAULT NULL
)
RETURNS TABLE(result_linked_to_twin_id uuid, total numeric)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        WITH linked_twins AS (
            SELECT
                CASE
                    WHEN src_else_dst = true THEN tl.dst_twin_id
                    ELSE tl.src_twin_id
                    END as linked_to_id,
                CASE
                    WHEN src_else_dst = true THEN tl.src_twin_id
                    ELSE tl.dst_twin_id
                    END as linked_from_id
            FROM twin_link tl
            WHERE (
                      (src_else_dst = true AND tl.dst_twin_id = ANY(linked_to_twin_ids))
                          OR (src_else_dst = false AND tl.src_twin_id = ANY(linked_to_twin_ids))
                  )
              AND (link_ids IS NULL OR tl.link_id = ANY(link_ids))
        ),
             filtered_linked_twins AS (
                 SELECT
                     lt.linked_to_id,
                     lt.linked_from_id
                 FROM linked_twins lt
                          INNER JOIN twin t ON t.id = lt.linked_from_id
                 WHERE check_twin_status_filter(
                         t.twin_status_id,
                         linked_from_in_twin_status_ids,
                         status_exclude
                       )
                   AND check_twin_class_filter(
                         t.twin_class_id,
                         linked_twin_of_class_ids
                       )
                   AND (
                         assignee_user_id IS NULL
                         OR t.assigner_user_id = assignee_user_id
                       )
             ),
             field_values AS (
                 SELECT
                     flt.linked_to_id,
                     flt.linked_from_id,
                     COALESCE(tfd.value, 0) as field_val
                 FROM filtered_linked_twins flt
                          LEFT JOIN twin_field_decimal tfd ON tfd.twin_id = flt.linked_from_id
                      AND tfd.twin_class_field_id = ANY(class_field_ids)
             )
        SELECT
            fv.linked_to_id as result_linked_to_twin_id,
            COALESCE(SUM(fv.field_val), 0) as total
        FROM field_values fv
        GROUP BY fv.linked_to_id;
END;
$$;

DROP FUNCTION IF EXISTS twin_field_decimal_calc_sum_by_link_with_twin_flavor(uuid[],uuid[],jsonb,boolean,uuid[],uuid[],boolean,boolean);
DROP FUNCTION IF EXISTS twin_field_decimal_calc_sum_by_link_with_twin_flavor(uuid[],uuid[],jsonb,boolean,uuid[],uuid[],boolean,boolean,boolean);

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_by_link_with_twin_flavor(
    p_twin_ids uuid[],
    p_link_ids uuid[],
    field_id_by_twin_flavor jsonb,
    p_src_else_dst boolean,
    p_linked_twin_in_status_ids uuid[] DEFAULT NULL,
    p_linked_twin_of_class_ids uuid[] DEFAULT NULL,
    p_status_exclude boolean DEFAULT false,
    p_skip_if_not_found boolean DEFAULT true,
    p_assignee_user_id uuid DEFAULT NULL
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
        ARRAY_AGG(DISTINCT t.flavor_data_list_option_id::text)
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
          AND t.flavor_data_list_option_id IS NOT NULL
          AND (p_linked_twin_in_status_ids IS NULL
              OR array_length(p_linked_twin_in_status_ids, 1) = 0
              OR p_linked_twin_in_status_ids = '{}'
              OR (NOT p_status_exclude AND t.twin_status_id = ANY(p_linked_twin_in_status_ids))
              OR (p_status_exclude AND t.twin_status_id != ALL(p_linked_twin_in_status_ids)))
          AND (p_linked_twin_of_class_ids IS NULL
              OR array_length(p_linked_twin_of_class_ids, 1) = 0
              OR p_linked_twin_of_class_ids = '{}'
              OR t.twin_class_id = ANY(p_linked_twin_of_class_ids))
          AND (
                p_assignee_user_id IS NULL
                OR t.assigner_user_id = p_assignee_user_id
              )
          AND (field_id_by_twin_flavor ->> t.flavor_data_list_option_id::text IS NULL
              OR (field_id_by_twin_flavor ->> t.flavor_data_list_option_id::text) ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' IS FALSE);

        IF v_missing_types IS NOT NULL AND array_length(v_missing_types, 1) > 0 THEN
            RAISE EXCEPTION 'Type options not found in fieldIdByTwinTypeId map or invalid UUID: %',
                array_to_string(v_missing_types, ', ');
        END IF;
    END IF;

    RETURN QUERY
    WITH field_map AS (
        SELECT
            key::uuid AS flavor_data_list_option_id,
            value::uuid AS field_id
        FROM jsonb_each_text(field_id_by_twin_flavor)
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
            t.flavor_data_list_option_id,
            fm.field_id
        FROM linked_twins lt
        INNER JOIN twin t ON t.id = lt.linked_to_id
        LEFT JOIN field_map fm ON fm.flavor_data_list_option_id = t.flavor_data_list_option_id
        WHERE t.flavor_data_list_option_id IS NOT NULL
          AND (p_linked_twin_in_status_ids IS NULL
              OR array_length(p_linked_twin_in_status_ids, 1) = 0
              OR p_linked_twin_in_status_ids = '{}'
              OR (NOT p_status_exclude AND t.twin_status_id = ANY(p_linked_twin_in_status_ids))
              OR (p_status_exclude AND t.twin_status_id != ALL(p_linked_twin_in_status_ids)))
          AND (p_linked_twin_of_class_ids IS NULL
              OR array_length(p_linked_twin_of_class_ids, 1) = 0
              OR p_linked_twin_of_class_ids = '{}'
              OR t.twin_class_id = ANY(p_linked_twin_of_class_ids))
          AND (
                p_assignee_user_id IS NULL
                OR t.assigner_user_id = p_assignee_user_id
              )
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
