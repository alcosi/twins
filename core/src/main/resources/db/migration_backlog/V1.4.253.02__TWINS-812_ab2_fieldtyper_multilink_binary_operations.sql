-- Add link_ids parameter to binary operations by link (divisions, multiplications, subtractions)
-- Backward compatible: if link_ids is NULL, returns all links

-- Drop old versions without link_ids parameter to avoid signature conflicts
DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_two_field_op_by_link(
    p_linked_to_twin_ids uuid[],
    p_src_else_dst boolean,
    p_linked_from_in_twin_status_ids uuid[],
    p_linked_twin_of_class_ids uuid[],
    p_first_twin_class_field_id uuid,
    p_second_twin_class_field_id uuid,
    p_status_exclude boolean,
    p_operation_type text,
    p_throw_on_division_by_zero boolean
);

DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_sum_of_divisions_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    first_twin_class_field_id uuid,
    second_twin_class_field_id uuid,
    status_exclude boolean,
    throw_on_division_by_zero boolean
);

DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_sum_of_multiplications_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    first_twin_class_field_id uuid,
    second_twin_class_field_id uuid,
    status_exclude boolean
);

DROP FUNCTION IF EXISTS public.twin_field_decimal_calc_sum_of_subtractions_by_link(
    linked_to_twin_ids uuid[],
    src_else_dst boolean,
    linked_from_in_twin_status_ids uuid[],
    linked_twin_of_class_ids uuid[],
    first_twin_class_field_id uuid,
    second_twin_class_field_id uuid,
    status_exclude boolean
);

-- Create new version with link_ids parameter
CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_two_field_op_by_link(
    p_linked_to_twin_ids UUID[],
    p_src_else_dst BOOLEAN,
    p_linked_from_in_twin_status_ids UUID[],
    p_linked_twin_of_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
    p_link_ids UUID[],
    p_status_exclude BOOLEAN DEFAULT false,
    p_operation_type TEXT DEFAULT 'division',
    p_throw_on_division_by_zero boolean DEFAULT true
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        WITH linked_twins AS (
            SELECT
                CASE
                    WHEN p_src_else_dst = true THEN tl.dst_twin_id
                    ELSE tl.src_twin_id
                    END as linked_to_id,
                CASE
                    WHEN p_src_else_dst = true THEN tl.src_twin_id
                    ELSE tl.dst_twin_id
                    END as linked_from_id
            FROM twin_link tl
            WHERE (
                      (p_src_else_dst = true AND tl.dst_twin_id = ANY(p_linked_to_twin_ids))
                          OR (p_src_else_dst = false AND tl.src_twin_id = ANY(p_linked_to_twin_ids))
                  )
              AND (p_link_ids IS NULL OR tl.link_id = ANY(p_link_ids))
        ),
             filtered_linked_twins AS (
                 SELECT
                     lt.linked_to_id,
                     lt.linked_from_id
                 FROM linked_twins lt
                          INNER JOIN twin t ON t.id = lt.linked_from_id
                 WHERE check_twin_status_filter(
                         t.twin_status_id,
                         p_linked_from_in_twin_status_ids,
                         p_status_exclude
                       )
                   AND check_twin_class_filter(
                         t.twin_class_id,
                         p_linked_twin_of_class_ids
                       )
             ),
             field_values AS (
                 SELECT
                     flt.linked_to_id,
                     flt.linked_from_id,
                     MAX(CASE WHEN tfd.twin_class_field_id = p_first_twin_class_field_id
                                  THEN tfd.value
                         END) as first_val,
                     MAX(CASE WHEN tfd.twin_class_field_id = p_second_twin_class_field_id
                                  THEN tfd.value
                         END) as second_val
                 FROM filtered_linked_twins flt
                          LEFT JOIN twin_field_decimal tfd ON tfd.twin_id = flt.linked_from_id
                     AND tfd.twin_class_field_id IN (p_first_twin_class_field_id, p_second_twin_class_field_id)
                 GROUP BY flt.linked_to_id, flt.linked_from_id
             ),
             operations AS (
                 SELECT
                     linked_to_id,
                     CASE p_operation_type
                         WHEN 'division' THEN
                             CASE
                                 WHEN (second_val IS NOT NULL AND second_val != 0) OR p_throw_on_division_by_zero = true
                                     THEN COALESCE(first_val, 0) / second_val
                                 ELSE 0
                                 END
                         WHEN 'multiplication' THEN
                             COALESCE(first_val, 0) * COALESCE(second_val, 1)
                         WHEN 'subtraction' THEN
                             COALESCE(first_val, 0) - COALESCE(second_val, 0)
                         ELSE 0
                         END as operation_result
                 FROM field_values
                 WHERE (p_operation_type = 'division' AND first_val IS NOT NULL AND second_val IS NOT NULL)
                    OR (p_operation_type = 'multiplication' AND (first_val IS NOT NULL OR second_val IS NOT NULL))
                    OR (p_operation_type = 'subtraction' AND (first_val IS NOT NULL OR second_val IS NOT NULL))
             )
        SELECT
            o.linked_to_id,
            COALESCE(SUM(o.operation_result), 0) as total
        FROM operations o
        GROUP BY o.linked_to_id;
END;
$$;

CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_sum_of_divisions_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
    link_ids UUID[],
    status_exclude BOOLEAN DEFAULT false,
    throw_on_division_by_zero boolean DEFAULT true
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_decimal_calc_two_field_op_by_link(
                linked_to_twin_ids,
                src_else_dst,
                linked_from_in_twin_status_ids,
                linked_twin_of_class_ids,
                first_twin_class_field_id,
                second_twin_class_field_id,
                link_ids,
                status_exclude,
                'division',
                throw_on_division_by_zero
                      );
END;
$$;

CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_sum_of_multiplications_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
    link_ids UUID[],
    status_exclude BOOLEAN DEFAULT false
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_decimal_calc_two_field_op_by_link(
                linked_to_twin_ids,
                src_else_dst,
                linked_from_in_twin_status_ids,
                linked_twin_of_class_ids,
                first_twin_class_field_id,
                second_twin_class_field_id,
                link_ids,
                status_exclude,
                'multiplication'
                      );
END;
$$;

CREATE OR REPLACE FUNCTION public.twin_field_decimal_calc_sum_of_subtractions_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
    link_ids UUID[],
    status_exclude BOOLEAN DEFAULT false
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_decimal_calc_two_field_op_by_link(
                linked_to_twin_ids,
                src_else_dst,
                linked_from_in_twin_status_ids,
                linked_twin_of_class_ids,
                first_twin_class_field_id,
                second_twin_class_field_id,
                link_ids,
                status_exclude,
                'subtraction'
                      );
END;
$$;
