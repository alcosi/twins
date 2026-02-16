BEGIN;

-- 1. add column twin_factory_id

ALTER TABLE twin_factory_condition_set
    ADD COLUMN IF NOT EXISTS twin_factory_id uuid;

-- 2. Collect all condition_set -> factory

DROP TABLE IF EXISTS tmp_condition_set_usage;

CREATE TEMP TABLE tmp_condition_set_usage AS
WITH all_usage AS (
    -- pipeline
    SELECT
        p.twin_factory_condition_set_id AS condition_set_id,
        p.twin_factory_id               AS twin_factory_id
    FROM twin_factory_pipeline p
    WHERE p.twin_factory_condition_set_id IS NOT NULL

    UNION ALL

    -- pipeline step
    SELECT
        ps.twin_factory_condition_set_id,
        p.twin_factory_id
    FROM twin_factory_pipeline_step ps
             JOIN twin_factory_pipeline p
                  ON p.id = ps.twin_factory_pipeline_id
    WHERE ps.twin_factory_condition_set_id IS NOT NULL

    UNION ALL

    -- branch
    SELECT
        b.twin_factory_condition_set_id,
        b.twin_factory_id
    FROM twin_factory_branch b
    WHERE b.twin_factory_condition_set_id IS NOT NULL

    UNION ALL

    -- eraser
    SELECT
        e.twin_factory_condition_set_id,
        e.twin_factory_id
    FROM twin_factory_eraser e
    WHERE e.twin_factory_condition_set_id IS NOT NULL

    UNION ALL

    -- multiplier filter
    SELECT
        mf.twin_factory_condition_set_id,
        m.twin_factory_id
    FROM twin_factory_multiplier_filter mf
             JOIN twin_factory_multiplier m
                  ON m.id = mf.twin_factory_multiplier_id
    WHERE mf.twin_factory_condition_set_id IS NOT NULL
),
unique_usage AS (
    SELECT DISTINCT condition_set_id, twin_factory_id FROM all_usage
),
counts AS (
    SELECT condition_set_id, COUNT(*) as usage_count
    FROM unique_usage
    GROUP BY condition_set_id
)
SELECT u.condition_set_id, u.twin_factory_id, c.usage_count
FROM unique_usage u
JOIN counts c ON u.condition_set_id = c.condition_set_id;

DO $$
DECLARE
    r RECORD;
    r_fact RECORD;
BEGIN
    FOR r IN SELECT DISTINCT condition_set_id FROM tmp_condition_set_usage WHERE usage_count > 1 LOOP
        RAISE NOTICE 'Found shared condition_set: id % is used by multiple factories', r.condition_set_id;
        FOR r_fact IN SELECT twin_factory_id FROM tmp_condition_set_usage WHERE condition_set_id = r.condition_set_id LOOP
            RAISE NOTICE '  Used by factory: %', r_fact.twin_factory_id;
        END LOOP;
    END LOOP;
END $$;

-- 3. Define a PRIMARY factory for each condition_set

DROP TABLE IF EXISTS tmp_condition_set_primary_factory;

CREATE TEMP TABLE tmp_condition_set_primary_factory AS
SELECT DISTINCT ON (condition_set_id)
    condition_set_id,
    twin_factory_id AS primary_factory_id
FROM tmp_condition_set_usage
ORDER BY condition_set_id, twin_factory_id;

-- 4. Set the factory for original twin_factory_condition_set

UPDATE twin_factory_condition_set cs
SET twin_factory_id = pf.primary_factory_id
FROM tmp_condition_set_primary_factory pf
WHERE cs.id = pf.condition_set_id;

-- 5. Preparing clones ONLY for other factories

DROP TABLE IF EXISTS tmp_condition_set_clone;

CREATE TEMP TABLE tmp_condition_set_clone AS
SELECT
    gen_random_uuid()          AS new_id,
    cs.id                      AS old_id,
    u.twin_factory_id,
    cs.name,
    cs.description,
    cs.domain_id,
    cs.created_by_user_id,
    cs.created_at,
    cs.updated_at
FROM tmp_condition_set_usage u
         JOIN tmp_condition_set_primary_factory pf
              ON pf.condition_set_id = u.condition_set_id
         JOIN twin_factory_condition_set cs
              ON cs.id = u.condition_set_id
WHERE u.twin_factory_id <> pf.primary_factory_id;

-- 6. Insert clones

DO $$
DECLARE
    r RECORD;
    r_cond RECORD;
BEGIN
    FOR r IN SELECT * FROM tmp_condition_set_clone LOOP
        RAISE NOTICE 'Cloning condition_set: old_id %, new_id % for twin_factory_id %', r.old_id, r.new_id, r.twin_factory_id;

        INSERT INTO twin_factory_condition_set (
            id, name, description, domain_id, created_by_user_id, created_at, updated_at, twin_factory_id
        ) VALUES (
            r.new_id, r.name, r.description, r.domain_id, r.created_by_user_id, r.created_at, r.updated_at, r.twin_factory_id
        );

        -- 6.1 Clone conditions for the new sets
        FOR r_cond IN
            INSERT INTO twin_factory_condition (
                id, twin_factory_condition_set_id, conditioner_featurer_id, conditioner_params, invert, active, description
            )
            SELECT
                gen_random_uuid(),
                r.new_id,
                tc.conditioner_featurer_id,
                tc.conditioner_params,
                tc.invert,
                tc.active,
                tc.description
            FROM twin_factory_condition tc
            WHERE tc.twin_factory_condition_set_id = r.old_id
            RETURNING id
        LOOP
            RAISE NOTICE '  Cloned condition: new_id % for new condition_set_id %', r_cond.id, r.new_id;
        END LOOP;
    END LOOP;
END $$;


-- 7. Relink ALL links

DO $$
DECLARE
    r RECORD;
    updated_count INT;
BEGIN
    FOR r IN SELECT * FROM tmp_condition_set_clone LOOP
        -- pipeline
        UPDATE twin_factory_pipeline p
        SET twin_factory_condition_set_id = r.new_id
        WHERE p.twin_factory_condition_set_id = r.old_id
          AND p.twin_factory_id = r.twin_factory_id;
        GET DIAGNOSTICS updated_count = ROW_COUNT;
        IF updated_count > 0 THEN
            RAISE NOTICE 'Updated twin_factory_pipeline: set condition_set_id % (was %) for % rows', r.new_id, r.old_id, updated_count;
        END IF;

        -- pipeline step
        UPDATE twin_factory_pipeline_step ps
        SET twin_factory_condition_set_id = r.new_id
        FROM twin_factory_pipeline p
        WHERE ps.twin_factory_condition_set_id = r.old_id
          AND p.id = ps.twin_factory_pipeline_id
          AND p.twin_factory_id = r.twin_factory_id;
        GET DIAGNOSTICS updated_count = ROW_COUNT;
        IF updated_count > 0 THEN
            RAISE NOTICE 'Updated twin_factory_pipeline_step: set condition_set_id % (was %) for % rows', r.new_id, r.old_id, updated_count;
        END IF;

        -- branch
        UPDATE twin_factory_branch b
        SET twin_factory_condition_set_id = r.new_id
        WHERE b.twin_factory_condition_set_id = r.old_id
          AND b.twin_factory_id = r.twin_factory_id;
        GET DIAGNOSTICS updated_count = ROW_COUNT;
        IF updated_count > 0 THEN
            RAISE NOTICE 'Updated twin_factory_branch: set condition_set_id % (was %) for % rows', r.new_id, r.old_id, updated_count;
        END IF;

        -- eraser
        UPDATE twin_factory_eraser e
        SET twin_factory_condition_set_id = r.new_id
        WHERE e.twin_factory_condition_set_id = r.old_id
          AND e.twin_factory_id = r.twin_factory_id;
        GET DIAGNOSTICS updated_count = ROW_COUNT;
        IF updated_count > 0 THEN
            RAISE NOTICE 'Updated twin_factory_eraser: set condition_set_id % (was %) for % rows', r.new_id, r.old_id, updated_count;
        END IF;

        -- multiplier filter
        UPDATE twin_factory_multiplier_filter mf
        SET twin_factory_condition_set_id = r.new_id
        FROM twin_factory_multiplier m
        WHERE mf.twin_factory_condition_set_id = r.old_id
          AND m.id = mf.twin_factory_multiplier_id
          AND m.twin_factory_id = r.twin_factory_id;
        GET DIAGNOSTICS updated_count = ROW_COUNT;
        IF updated_count > 0 THEN
            RAISE NOTICE 'Updated twin_factory_multiplier_filter: set condition_set_id % (was %) for % rows', r.new_id, r.old_id, updated_count;
        END IF;
    END LOOP;
END $$;

-- 8. Delete old twin_factory_condition_set that are not used

DELETE FROM twin_factory_condition_set cs
WHERE cs.twin_factory_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM twin_factory_pipeline p WHERE p.twin_factory_condition_set_id = cs.id)
  AND NOT EXISTS (SELECT 1 FROM twin_factory_pipeline_step ps WHERE ps.twin_factory_condition_set_id = cs.id)
  AND NOT EXISTS (SELECT 1 FROM twin_factory_branch b WHERE b.twin_factory_condition_set_id = cs.id)
  AND NOT EXISTS (SELECT 1 FROM twin_factory_eraser e WHERE e.twin_factory_condition_set_id = cs.id)
  AND NOT EXISTS (SELECT 1 FROM twin_factory_multiplier_filter mf WHERE mf.twin_factory_condition_set_id = cs.id);

-- 9. Secure

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM twin_factory_condition_set
            WHERE twin_factory_id IS NULL
        ) THEN
            RAISE EXCEPTION
                'Migration failed: some condition sets are not linked to factory';
        END IF;
    END $$;


-- 10. Making the column not null + index

ALTER TABLE twin_factory_condition_set ALTER COLUMN twin_factory_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_twin_factory_condition_set_factory
    ON twin_factory_condition_set (twin_factory_id);

COMMIT;
