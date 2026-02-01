CREATE OR REPLACE FUNCTION safe_cast_to_numeric(value TEXT)
    RETURNS NUMERIC AS $$
BEGIN
    RETURN CASE
               WHEN value IS NULL THEN NULL::NUMERIC
               WHEN value ~ '^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$'
                   THEN CAST(value AS NUMERIC)
               ELSE NULL::NUMERIC
        END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION check_twin_status_filter(
    twin_status_id UUID,
    status_ids UUID[],
    exclude BOOLEAN
) RETURNS BOOLEAN AS $$
BEGIN
    IF status_ids IS NULL OR array_length(status_ids, 1) = 0 OR status_ids = '{}' THEN
        RETURN TRUE;
    END IF;

    IF exclude = false THEN
        RETURN twin_status_id = ANY(status_ids);
    ELSE
        RETURN twin_status_id != ALL(status_ids);
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION check_twin_class_filter(
    twin_class_id UUID,
    class_ids UUID[]
) RETURNS BOOLEAN AS $$
BEGIN
    IF class_ids IS NULL OR array_length(class_ids, 1) = 0 OR class_ids = '{}' THEN
        RETURN TRUE;
    END IF;

    RETURN twin_class_id = ANY(class_ids);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

DROP FUNCTION IF EXISTS twin_field_calc_sum_by_head(
    UUID[], UUID[], UUID[], BOOLEAN, UUID[]
);

CREATE OR REPLACE FUNCTION twin_field_calc_sum_by_head(
    p_head_twin_ids UUID[],
    p_twin_class_field_ids UUID[],
    p_children_in_twin_status_ids UUID[],
    p_exclude_status BOOLEAN,
    p_children_of_twin_class_ids UUID[]
)
    RETURNS TABLE(head_twin_id UUID, total NUMERIC) AS $$
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
                     COALESCE(safe_cast_to_numeric(tfs.value), 0) as field_val
                 FROM filtered_children fc
                          LEFT JOIN twin_field_simple tfs ON tfs.twin_id = fc.child_twin_id
                     AND tfs.twin_class_field_id = ANY(p_twin_class_field_ids)
             )
        SELECT
            fv.head_twin_id,
            COALESCE(SUM(fv.field_val), 0)::NUMERIC as total
        FROM field_values fv
        GROUP BY fv.head_twin_id;
END;
$$ LANGUAGE plpgsql;

-- Удаляем старую функцию
DROP FUNCTION IF EXISTS twin_field_calc_two_field_op_by_head(
    UUID[], UUID[], UUID[], UUID, UUID, BOOLEAN, TEXT, BOOLEAN
);

CREATE OR REPLACE FUNCTION twin_field_calc_two_field_op_by_head(
    p_head_twin_ids UUID[],
    p_children_in_twin_status_ids UUID[],
    p_children_of_twin_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
    p_exclude_status BOOLEAN DEFAULT false,
    p_operation_type TEXT DEFAULT 'division',
    p_throw_on_division_by_zero BOOLEAN DEFAULT true
)
    RETURNS TABLE (
                      head_twin_id UUID,
                      total NUMERIC
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
                     MAX(CASE WHEN tfs.twin_class_field_id = p_first_twin_class_field_id
                                  THEN safe_cast_to_numeric(tfs.value)
                         END) as first_val,
                     MAX(CASE WHEN tfs.twin_class_field_id = p_second_twin_class_field_id
                                  THEN safe_cast_to_numeric(tfs.value)
                         END) as second_val
                 FROM filtered_children fc
                          LEFT JOIN twin_field_simple tfs ON tfs.twin_id = fc.child_twin_id
                     AND tfs.twin_class_field_id IN (p_first_twin_class_field_id, p_second_twin_class_field_id)
                 GROUP BY fc.head_twin_id, fc.child_twin_id
             ),
             operations AS (
                 SELECT
                     fv.head_twin_id,
                     CASE p_operation_type
                         WHEN 'division' THEN
                             CASE
                                 WHEN (fv.second_val IS NOT NULL AND fv.second_val != 0) OR p_throw_on_division_by_zero = true
                                     THEN COALESCE(fv.first_val, 0) / fv.second_val
                                 ELSE 0
                                 END
                         WHEN 'multiplication' THEN
                             COALESCE(fv.first_val, 0) * COALESCE(fv.second_val, 1)
                         WHEN 'subtraction' THEN
                             COALESCE(fv.first_val, 0) - COALESCE(fv.second_val, 0)
                         ELSE 0
                         END as operation_result
                 FROM field_values fv
                 WHERE (p_operation_type = 'division' AND fv.first_val IS NOT NULL AND fv.second_val IS NOT NULL)
                    OR (p_operation_type = 'multiplication' AND (fv.first_val IS NOT NULL OR fv.second_val IS NOT NULL))
                    OR (p_operation_type = 'subtraction' AND (fv.first_val IS NOT NULL OR fv.second_val IS NOT NULL))
             )
        SELECT
            o.head_twin_id,
            COALESCE(SUM(o.operation_result), 0)::NUMERIC as total
        FROM operations o
        GROUP BY o.head_twin_id;
END;
$$;

-- Удаляем старую функцию
DROP FUNCTION IF EXISTS twin_field_calc_sum_by_link(
    UUID[], BOOLEAN, UUID[], UUID[], UUID[], BOOLEAN
);

CREATE OR REPLACE FUNCTION twin_field_calc_sum_by_link(
    p_linked_to_twin_ids UUID[],
    p_src_else_dst BOOLEAN,
    p_linked_from_in_twin_status_ids UUID[],
    p_linked_twin_of_class_ids UUID[],
    p_class_field_ids UUID[],
    p_status_exclude BOOLEAN DEFAULT false
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total NUMERIC
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
                     COALESCE(safe_cast_to_numeric(tfs.value), 0) as field_val
                 FROM filtered_linked_twins flt
                          LEFT JOIN twin_field_simple tfs ON tfs.twin_id = flt.linked_from_id
                     AND tfs.twin_class_field_id = ANY(p_class_field_ids)
             )
        SELECT
            fv.linked_to_id,
            COALESCE(SUM(fv.field_val), 0)::NUMERIC as total
        FROM field_values fv
        GROUP BY fv.linked_to_id;
END;
$$;

-- Удаляем старую функцию
DROP FUNCTION IF EXISTS twin_field_calc_two_field_op_by_link(
    UUID[], BOOLEAN, UUID[], UUID[], UUID, UUID, BOOLEAN, TEXT, BOOLEAN
);

CREATE OR REPLACE FUNCTION twin_field_calc_two_field_op_by_link(
    p_linked_to_twin_ids UUID[],
    p_src_else_dst BOOLEAN,
    p_linked_from_in_twin_status_ids UUID[],
    p_linked_twin_of_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
    p_status_exclude BOOLEAN DEFAULT false,
    p_operation_type TEXT DEFAULT 'division',
    p_throw_on_division_by_zero BOOLEAN DEFAULT true
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total NUMERIC
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
                     MAX(CASE WHEN tfs.twin_class_field_id = p_first_twin_class_field_id
                                  THEN safe_cast_to_numeric(tfs.value)
                         END) as first_val,
                     MAX(CASE WHEN tfs.twin_class_field_id = p_second_twin_class_field_id
                                  THEN safe_cast_to_numeric(tfs.value)
                         END) as second_val
                 FROM filtered_linked_twins flt
                          LEFT JOIN twin_field_simple tfs ON tfs.twin_id = flt.linked_from_id
                     AND tfs.twin_class_field_id IN (p_first_twin_class_field_id, p_second_twin_class_field_id)
                 GROUP BY flt.linked_to_id, flt.linked_from_id
             ),
             operations AS (
                 SELECT
                     fv.linked_to_id,
                     CASE p_operation_type
                         WHEN 'division' THEN
                             CASE
                                 WHEN (fv.second_val IS NOT NULL AND fv.second_val != 0) OR p_throw_on_division_by_zero = true
                                     THEN COALESCE(fv.first_val, 0) / fv.second_val
                                 ELSE 0
                                 END
                         WHEN 'multiplication' THEN
                             COALESCE(fv.first_val, 0) * COALESCE(fv.second_val, 1)
                         WHEN 'subtraction' THEN
                             COALESCE(fv.first_val, 0) - COALESCE(fv.second_val, 0)
                         ELSE 0
                         END as operation_result
                 FROM field_values fv
                 WHERE (p_operation_type = 'division' AND fv.first_val IS NOT NULL AND fv.second_val IS NOT NULL)
                    OR (p_operation_type = 'multiplication' AND (fv.first_val IS NOT NULL OR fv.second_val IS NOT NULL))
                    OR (p_operation_type = 'subtraction' AND (fv.first_val IS NOT NULL OR fv.second_val IS NOT NULL))
             )
        SELECT
            o.linked_to_id,
            COALESCE(SUM(o.operation_result), 0)::NUMERIC as total
        FROM operations o
        GROUP BY o.linked_to_id;
END;
$$;

-- Удаляем функции, которые вызывают проблемные функции
DROP FUNCTION IF EXISTS twin_field_calc_sum_of_multiplications_by_head(
    UUID[], UUID[], UUID[], UUID, UUID, BOOLEAN
);

DROP FUNCTION IF EXISTS twin_field_calc_sum_of_multiplications_by_link(
    UUID[], BOOLEAN, UUID[], UUID[], UUID, UUID, BOOLEAN
);

-- Создаем функции с правильным типом возвращаемого значения
CREATE OR REPLACE FUNCTION twin_field_calc_sum_of_multiplications_by_head(
    p_head_twin_ids UUID[],
    p_children_in_twin_status_ids UUID[],
    p_children_of_twin_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
    p_exclude_status BOOLEAN DEFAULT false
)
    RETURNS TABLE (
                      head_twin_id UUID,
                      total NUMERIC
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_calc_two_field_op_by_head(
                p_head_twin_ids,
                p_children_in_twin_status_ids,
                p_children_of_twin_class_ids,
                p_first_twin_class_field_id,
                p_second_twin_class_field_id,
                p_exclude_status,
                'multiplication'
                      );
END;
$$;

CREATE OR REPLACE FUNCTION twin_field_calc_sum_of_multiplications_by_link(
    p_linked_to_twin_ids UUID[],
    p_src_else_dst BOOLEAN,
    p_linked_from_in_twin_status_ids UUID[],
    p_linked_twin_of_class_ids UUID[],
    p_first_twin_class_field_id UUID,
    p_second_twin_class_field_id UUID,
    p_status_exclude BOOLEAN DEFAULT false
)
    RETURNS TABLE (
                      linked_to_twin_id UUID,
                      total NUMERIC
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM twin_field_calc_two_field_op_by_link(
                p_linked_to_twin_ids,
                p_src_else_dst,
                p_linked_from_in_twin_status_ids,
                p_linked_twin_of_class_ids,
                p_first_twin_class_field_id,
                p_second_twin_class_field_id,
                p_status_exclude,
                'multiplication'
                      );
END;
$$;

DROP FUNCTION IF EXISTS safe_cast_to_double(TEXT);