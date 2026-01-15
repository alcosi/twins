BEGIN;

-- 1. add column twin_factory_id

ALTER TABLE twin_factory_condition_set
    ADD COLUMN IF NOT EXISTS twin_factory_id uuid;

-- 2. Collect all condition_set -> factory

DROP TABLE IF EXISTS tmp_condition_set_usage;

CREATE TEMP TABLE tmp_condition_set_usage AS

-- pipeline
SELECT DISTINCT
    p.twin_factory_condition_set_id AS condition_set_id,
    p.twin_factory_id               AS twin_factory_id
FROM twin_factory_pipeline p
WHERE p.twin_factory_condition_set_id IS NOT NULL

UNION ALL

-- pipeline step
SELECT DISTINCT
    ps.twin_factory_condition_set_id,
    p.twin_factory_id
FROM twin_factory_pipeline_step ps
         JOIN twin_factory_pipeline p
              ON p.id = ps.twin_factory_pipeline_id
WHERE ps.twin_factory_condition_set_id IS NOT NULL

UNION ALL

-- branch
SELECT DISTINCT
    b.twin_factory_condition_set_id,
    b.twin_factory_id
FROM twin_factory_branch b
WHERE b.twin_factory_condition_set_id IS NOT NULL

UNION ALL

-- eraser
SELECT DISTINCT
    e.twin_factory_condition_set_id,
    e.twin_factory_id
FROM twin_factory_eraser e
WHERE e.twin_factory_condition_set_id IS NOT NULL

UNION ALL

-- multiplier filter
SELECT DISTINCT
    mf.twin_factory_condition_set_id,
    m.twin_factory_id
FROM twin_factory_multiplier_filter mf
         JOIN twin_factory_multiplier m
              ON m.id = mf.twin_factory_multiplier_id
WHERE mf.twin_factory_condition_set_id IS NOT NULL;

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

INSERT INTO twin_factory_condition_set (
    id,
    name,
    description,
    domain_id,
    created_by_user_id,
    created_at,
    updated_at,
    twin_factory_id
)
SELECT
    new_id,
    name,
    description,
    domain_id,
    created_by_user_id,
    created_at,
    updated_at,
    twin_factory_id
FROM tmp_condition_set_clone;


-- 7. Relink ALL links

-- pipeline
UPDATE twin_factory_pipeline p
SET twin_factory_condition_set_id = c.new_id
FROM tmp_condition_set_clone c
WHERE p.twin_factory_condition_set_id = c.old_id
  AND p.twin_factory_id = c.twin_factory_id;

-- pipeline step
UPDATE twin_factory_pipeline_step ps
SET twin_factory_condition_set_id = c.new_id
FROM tmp_condition_set_clone c,
     twin_factory_pipeline p
WHERE ps.twin_factory_condition_set_id = c.old_id
  AND p.id = ps.twin_factory_pipeline_id
  AND p.twin_factory_id = c.twin_factory_id;

-- branch
UPDATE twin_factory_branch b
SET twin_factory_condition_set_id = c.new_id
FROM tmp_condition_set_clone c
WHERE b.twin_factory_condition_set_id = c.old_id
  AND b.twin_factory_id = c.twin_factory_id;

-- eraser
UPDATE twin_factory_eraser e
SET twin_factory_condition_set_id = c.new_id
FROM tmp_condition_set_clone c
WHERE e.twin_factory_condition_set_id = c.old_id
  AND e.twin_factory_id = c.twin_factory_id;

-- multiplier filter
UPDATE twin_factory_multiplier_filter mf
SET twin_factory_condition_set_id = c.new_id
FROM tmp_condition_set_clone c,
     twin_factory_multiplier m
WHERE mf.twin_factory_condition_set_id = c.old_id
  AND m.id = mf.twin_factory_multiplier_id
  AND m.twin_factory_id = c.twin_factory_id;

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
