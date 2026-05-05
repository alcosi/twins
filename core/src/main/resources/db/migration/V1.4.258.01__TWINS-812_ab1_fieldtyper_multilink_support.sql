-- Add link_ids parameter to twin_field_decimal_calc_sum_by_link function
-- Backward compatible: if link_ids is NULL, returns all links

-- Drop old version without link_ids parameter to avoid signature conflict
DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_sum_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    class_field_ids uuid[],
    status_exclude boolean
);

CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_sum_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    class_field_ids uuid[],
    link_ids uuid[],
    status_exclude boolean DEFAULT false
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
