INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1352::integer, 13::integer, '', '', '', DEFAULT) on conflict do nothing;

ALTER TABLE twin ADD COLUMN IF NOT EXISTS type_option_id uuid REFERENCES data_list_option(id);

CREATE INDEX IF NOT EXISTS idx_twin_type_option_id ON twin(type_option_id);

DROP FUNCTION IF EXISTS twin_field_decimal_calc_sum_by_link_with_twin_type(uuid[],uuid[],jsonb,boolean,uuid[],uuid[],boolean,boolean);

CREATE OR REPLACE FUNCTION twin_field_decimal_calc_sum_by_link_with_twin_type(
    p_twin_ids uuid[],
    p_link_ids uuid[],
    field_id_by_twin_type jsonb,
    p_src_else_dst boolean,
    p_linked_twin_in_status_ids uuid[] DEFAULT null,
    p_linked_twin_of_class_ids uuid[] DEFAULT null,
    p_status_exclude boolean DEFAULT false,
    p_skip_if_not_found boolean DEFAULT true
)
RETURNS TABLE (
    twin_id uuid,
    calc numeric
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_twin_id uuid;
    v_linked_twin_id uuid;
    v_type_option_id uuid;
    v_field_id uuid;
    v_field_value numeric;
    v_total numeric;
BEGIN
    -- For each twin
    FOREACH v_twin_id IN ARRAY p_twin_ids
    LOOP
        v_total := 0;

        -- Find linked twins by link
FOR v_linked_twin_id IN
SELECT CASE
           WHEN p_src_else_dst THEN tl.dst_twin_id
           ELSE tl.src_twin_id
           END
FROM twin_link tl
WHERE tl.link_id = ANY(p_link_ids)
  AND (tl.src_twin_id = v_twin_id OR tl.dst_twin_id = v_twin_id)
  AND CASE
          WHEN p_src_else_dst THEN tl.src_twin_id = v_twin_id
          ELSE tl.dst_twin_id = v_twin_id
          END = true
    LOOP
-- Get type_option_id from linked twin
SELECT t.type_option_id
INTO v_type_option_id
FROM twin t
WHERE t.id = v_linked_twin_id;

IF v_type_option_id IS NULL THEN
                CONTINUE;
END IF;

            -- Filter by status if specified
            IF p_linked_twin_in_status_ids IS NOT NULL AND array_length(p_linked_twin_in_status_ids, 1) > 0 THEN
                IF (SELECT t.twin_status_id FROM twin t WHERE t.id = v_linked_twin_id) IS NULL THEN
                    CONTINUE;
END IF;

                IF p_status_exclude THEN
                    IF (SELECT t.twin_status_id FROM twin t WHERE t.id = v_linked_twin_id) = ANY(p_linked_twin_in_status_ids) THEN
                        CONTINUE;
END IF;
ELSE
                    IF (SELECT t.twin_status_id FROM twin t WHERE t.id = v_linked_twin_id) <> ALL(p_linked_twin_in_status_ids) THEN
                        CONTINUE;
END IF;
END IF;
END IF;

            -- Filter by class if specified
            IF p_linked_twin_of_class_ids IS NOT NULL AND array_length(p_linked_twin_of_class_ids, 1) > 0 THEN
                IF (SELECT t.twin_class_id FROM twin t WHERE t.id = v_linked_twin_id) IS NULL THEN
                    CONTINUE;
END IF;
                IF (SELECT t.twin_class_id FROM twin t WHERE t.id = v_linked_twin_id) <> ALL(p_linked_twin_of_class_ids) THEN
                    CONTINUE;
END IF;
END IF;

            -- Find fieldId by type_option_id from JSON map
BEGIN
SELECT (field_id_by_twin_type ->> v_type_option_id::text)::uuid
INTO v_field_id;
EXCEPTION WHEN OTHERS THEN
                IF p_skip_if_not_found THEN
                    CONTINUE;
ELSE
                    RAISE EXCEPTION 'Type option ID % not found in fieldIdBySelectorValue map', v_type_option_id;
END IF;
END;

            IF v_field_id IS NULL THEN
                IF p_skip_if_not_found THEN
                    CONTINUE;
ELSE
                    RAISE EXCEPTION 'Type option ID % not found in fieldIdBySelectorValue map', v_type_option_id;
END IF;
END IF;

            -- Get field value
BEGIN
SELECT tfd.value
INTO v_field_value
FROM twin_field_decimal tfd
WHERE tfd.twin_id = v_linked_twin_id
  AND tfd.twin_class_field_id = v_field_id
    LIMIT 1;

IF v_field_value IS NOT NULL THEN
                    v_total := v_total + COALESCE(v_field_value, 0);
END IF;
EXCEPTION WHEN OTHERS THEN
                NULL; -- Skip if error reading field value
END;
END LOOP;

        -- Return result for this twin
RETURN QUERY SELECT v_twin_id::uuid, v_total::numeric;
END LOOP;

    RETURN;
END;
$$;
