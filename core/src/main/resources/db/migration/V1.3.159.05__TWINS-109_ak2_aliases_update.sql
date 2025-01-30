DO $$
    DECLARE
        v_twin_id UUID;
        v_twin_class_id UUID;
        v_domain_alias_counter INT;
        v_alias_value TEXT;
    BEGIN
        FOR v_twin_id, v_twin_class_id IN
            SELECT te.id, te.twin_class_id
            FROM twin te
            WHERE EXISTS (
                SELECT 1
                FROM twin_alias ta
                WHERE ta.twin_id = te.id
                  AND ta.twin_alias_type_id = 'B'
                  AND NOT EXISTS (
                    SELECT 1
                    FROM twin_alias ta_c
                    WHERE ta_c.twin_id = ta.twin_id
                      AND ta_c.twin_alias_type_id = 'C'
                )
            )
            LOOP
                SELECT domain_alias_counter
                INTO v_domain_alias_counter
                FROM twin_class
                WHERE id = v_twin_class_id;

                UPDATE twin_class
                SET domain_alias_counter = v_domain_alias_counter + 1
                WHERE id = v_twin_class_id;

                SELECT CONCAT(UPPER(tc.key), '-C',
                              ROW_NUMBER() OVER (PARTITION BY tc.id ORDER BY ta.twin_id) + v_domain_alias_counter)
                INTO v_alias_value
                FROM twin_alias ta
                         JOIN twin te ON ta.twin_id = te.id
                         JOIN twin_class tc ON te.twin_class_id = tc.id
                WHERE ta.twin_id = v_twin_id
                  AND ta.twin_alias_type_id = 'B'
                LIMIT 1;

                INSERT INTO twin_alias (id, twin_id, twin_alias_type_id, alias_value, created_at, domain_id)
                VALUES (
                           gen_random_uuid(),
                           v_twin_id,
                           'C',
                           v_alias_value,
                           NOW(),
                           (SELECT domain_id FROM twin_class WHERE id = v_twin_class_id)
                       );
            END LOOP;
    END $$;
