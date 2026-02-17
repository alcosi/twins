CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_by_head(
    head_twin_ids UUID[],
    twin_class_field_ids UUID[],
    children_in_twin_status_ids UUID[],
    exclude_status boolean,
    children_of_twin_class_ids UUID[]
)
    RETURNS TABLE(head_twin_id UUID, total DECIMAL) AS $$
BEGIN
    RETURN QUERY
        WITH filtered_children AS (
            SELECT
                t.head_twin_id,
                t.id as child_twin_id
            FROM twin t
            WHERE t.head_twin_id = ANY(head_twin_ids)
              AND check_twin_status_filter(
                    t.twin_status_id,
                    children_in_twin_status_ids,
                    exclude_status
                  )
              AND check_twin_class_filter(
                    t.twin_class_id,
                    children_of_twin_class_ids
                  )
        ),
             field_values AS (
                 SELECT
                     fc.head_twin_id,
                     fc.child_twin_id,
                     COALESCE(tfd.value, 0) as field_val
                 FROM filtered_children fc
                          LEFT JOIN twin_field_decimal tfd ON tfd.twin_id = fc.child_twin_id
                     AND tfd.twin_class_field_id = ANY(twin_class_field_ids)
             )
        SELECT
            fv.head_twin_id,
            COALESCE(SUM(fv.field_val), 0) as total
        FROM field_values fv
        GROUP BY fv.head_twin_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_two_field_op_by_head(
    p_head_twin_ids UUID[],
    p_children_in_twin_status_ids UUID[],
    p_children_of_twin_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
    p_exclude_status boolean DEFAULT false,
    p_operation_type TEXT DEFAULT 'division',
    p_throw_on_division_by_zero boolean DEFAULT true
)
    RETURNS TABLE (
                      result_head_twin_id UUID,
                      result_total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        WITH filtered_children AS (
            SELECT
                t.head_twin_id,
                t.id as child_twin_id
            FROM twin t
            WHERE t.head_twin_id = ANY(p_head_twin_ids)
              AND check_twin_status_filter(
                    t.twin_status_id,
                    p_children_in_twin_status_ids,
                    p_exclude_status
                  )
              AND check_twin_class_filter(
                    t.twin_class_id,
                    p_children_of_twin_class_ids
                  )
        ),
             field_values AS (
                 SELECT
                     fc.head_twin_id,
                     fc.child_twin_id,
                     MAX(CASE WHEN tfd.twin_class_field_id = p_first_twin_class_field_id
                                  THEN tfd.value
                         END) as first_val,
                     MAX(CASE WHEN tfd.twin_class_field_id = p_second_twin_class_field_id
                                  THEN tfd.value
                         END) as second_val
                 FROM filtered_children fc
                          LEFT JOIN twin_field_decimal tfd ON tfd.twin_id = fc.child_twin_id
                     AND tfd.twin_class_field_id IN (p_first_twin_class_field_id, p_second_twin_class_field_id)
                 GROUP BY fc.head_twin_id, fc.child_twin_id
             ),
             operations AS (
                 SELECT
                     head_twin_id,
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
            o.head_twin_id as result_head_twin_id,
            COALESCE(SUM(o.operation_result), 0) as result_total
        FROM operations o
        GROUP BY o.head_twin_id;
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_of_divisions_by_head(
    head_twin_ids UUID[],
    children_in_twin_status_ids UUID[],
    children_of_twin_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
    exclude_status boolean DEFAULT false,
    throw_on_division_by_zero boolean DEFAULT true
)
    RETURNS TABLE (
                      head_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_decimal_calc_two_field_op_by_head(
                head_twin_ids,
                children_in_twin_status_ids,
                children_of_twin_class_ids,
                first_twin_class_field_id,
                second_twin_class_field_id,
                exclude_status,
                'division',
                throw_on_division_by_zero
                      );
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_of_multiplications_by_head(
    head_twin_ids UUID[],
    children_in_twin_status_ids UUID[],
    children_of_twin_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
    exclude_status boolean DEFAULT false
)
    RETURNS TABLE (
                      head_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_decimal_calc_two_field_op_by_head(
                head_twin_ids,
                children_in_twin_status_ids,
                children_of_twin_class_ids,
                first_twin_class_field_id,
                second_twin_class_field_id,
                exclude_status,
                'multiplication'
                      );
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_of_subtractions_by_head(
    head_twin_ids UUID[],
    children_in_twin_status_ids UUID[],
    children_of_twin_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
    exclude_status boolean DEFAULT false
)
    RETURNS TABLE (
                      head_twin_id UUID,
                      total DECIMAL
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_decimal_calc_two_field_op_by_head(
                head_twin_ids,
                children_in_twin_status_ids,
                children_of_twin_class_ids,
                first_twin_class_field_id,
                second_twin_class_field_id,
                exclude_status,
                'subtraction'
                      );
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    class_field_ids UUID[],
    status_exclude BOOLEAN DEFAULT false
)
    RETURNS TABLE (
                      result_linked_to_twin_id UUID,
                      total DECIMAL
                  )
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
            fv.linked_to_id,
            COALESCE(SUM(fv.field_val), 0) as total
        FROM field_values fv
        GROUP BY fv.linked_to_id;
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_two_field_op_by_link(
    p_linked_to_twin_ids UUID[],
    p_src_else_dst BOOLEAN,
    p_linked_from_in_twin_status_ids UUID[],
    p_linked_twin_of_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
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

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_of_divisions_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
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
                status_exclude,
                'division',
                throw_on_division_by_zero
                      );
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_of_multiplications_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
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
                status_exclude,
                'multiplication'
                      );
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_of_subtractions_by_link(
    linked_to_twin_ids UUID[],
    src_else_dst BOOLEAN,
    linked_from_in_twin_status_ids UUID[],
    linked_twin_of_class_ids UUID[],
    first_twin_class_field_id UUID,
    second_twin_class_field_id UUID,
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
                status_exclude,
                'subtraction'
                      );
END;
$$;
