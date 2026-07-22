-- On-fly sum of children decimal fields of linked twins (field typer 1357).
-- Path: context twin --link--> linked twin (e.g. portion) --head--> children (e.g. transfer) → sum fields.
-- Featurer stub: class/name/description are filled from @Featurer at app startup.

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1357, 13, '', '', '', false)
ON CONFLICT DO NOTHING;

CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_sum_children_of_linked(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    linked_status_exclude boolean,
    link_ids uuid[],
    children_of_twin_class_ids uuid[],
    children_in_twin_status_ids uuid[],
    children_status_exclude boolean,
    class_field_ids uuid[]
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
                END AS linked_to_id,
                CASE
                    WHEN src_else_dst = true THEN tl.src_twin_id
                    ELSE tl.dst_twin_id
                END AS linked_from_id
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
                    linked_status_exclude
                  )
              AND check_twin_class_filter(
                    t.twin_class_id,
                    linked_twin_of_class_ids
                  )
        ),
        filtered_children AS (
            SELECT
                flt.linked_to_id,
                child.id AS child_twin_id
            FROM filtered_linked_twins flt
            INNER JOIN twin child ON child.head_twin_id = flt.linked_from_id
            WHERE check_twin_status_filter(
                    child.twin_status_id,
                    children_in_twin_status_ids,
                    children_status_exclude
                  )
              AND check_twin_class_filter(
                    child.twin_class_id,
                    children_of_twin_class_ids
                  )
        ),
        field_values AS (
            SELECT
                fc.linked_to_id,
                COALESCE(tfd.value, 0) AS field_val
            FROM filtered_children fc
            LEFT JOIN twin_field_decimal tfd
                ON tfd.twin_id = fc.child_twin_id
               AND tfd.twin_class_field_id = ANY(class_field_ids)
        )
        SELECT
            fv.linked_to_id AS result_linked_to_twin_id,
            COALESCE(SUM(fv.field_val), 0) AS total
        FROM field_values fv
        GROUP BY fv.linked_to_id;
END;
$$;
