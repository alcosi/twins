-- One-shot SUPPLY_PORTION conservation data fix (ticket TWINS-XXX placeholder).
-- Brings portion twins to the invariant issued = used + defective + remaining, where
-- issued is the source of truth and the consumption fields are clamped on over-consumed
-- portions (used + defective > issued): defective is reduced to 0 first, then used,
-- until used + defective = issued — so remaining never goes negative:
--   overflow   = greatest(used + defective - issued, 0)
--   defective -= least(defective, overflow)   -- reduce defective first, floor 0
--   used      -= overflow - min(defective, overflow)  -- then used
--   remaining  = issued - used - defective       (>= 0 by construction)
-- Dependent materialized fields are refreshed in the same pass so the UI stays consistent:
--   remainingPriceTask / remainingPriceProjectAndWarehouse := remaining * price   (2 decimals, like typer 1355)
--   usedPrice / defectivePrice / spentPrice                := consumption * price (2 decimals)
--   spentQuantity := used + defective
--   usefulQuantity := used + defective + remaining  (= issued)
-- Rules:
--   * Fields/flavor are resolved by twin_class.key = 'SUPPLY_PORTION', field key and 'Task portion' option title —
--     design-time UUIDs are not guaranteed to match between environments.
--   * Twins with NULL flavor are skipped (unknown flavor -> unknown field pair).
--   * used/defective/remaining rows are only updated when they already exist (a clamp can only decrease
--     a value, and an absent row means 0); missing remaining/price/useful/spent rows are inserted
--     only for non-zero values (rows with 0 are not created).
--   * The plan is computed once into a temp table (ON COMMIT DROP) so every target field is derived
--     from the same numbers.

CREATE TEMP TABLE portion_conservation_fix_plan ON COMMIT DROP AS
WITH portionClass AS (
    SELECT tc.id AS class_id, tc.flavor_data_list_id
    FROM twin_class tc
    WHERE tc.key = 'SUPPLY_PORTION'
), taskFlavor AS (
    SELECT dlo.id
    FROM data_list_option dlo
    JOIN portionClass pc ON dlo.data_list_id = pc.flavor_data_list_id
    WHERE dlo.option = 'Task portion'
), fixFields AS (
    SELECT f.id, f.key
    FROM twin_class_field f
    JOIN portionClass pc ON f.twin_class_id = pc.class_id
    WHERE f.key IN ('issuedQuantity', 'issuedQuantityProjectAndWarehouse',
                    'usedQuantity', 'defectiveQuantity',
                    'remainingQuantityTask', 'remainingQuantityProjectAndWarehouse',
                    'usefulQuantity', 'spentQuantity',
                    'price', 'usedPrice', 'defectivePrice', 'spentPrice',
                    'remainingPriceTask', 'remainingPriceProjectAndWarehouse')
), portionValues AS (
    SELECT
        t.id AS twin_id,
        t.flavor_data_list_option_id AS flavor,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'issuedQuantity'), 0) AS issued_task,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'issuedQuantityProjectAndWarehouse'), 0) AS issued_pw
    FROM twin t
    JOIN portionClass pc ON pc.class_id = t.twin_class_id
    LEFT JOIN twin_field_decimal v ON v.twin_id = t.id
    LEFT JOIN fixFields ff ON ff.id = v.twin_class_field_id
    WHERE t.flavor_data_list_option_id IS NOT NULL
    GROUP BY t.id, t.flavor_data_list_option_id
), portionRest AS (
    SELECT
        t.id AS twin_id,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'usedQuantity'), 0) AS used_quantity,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'defectiveQuantity'), 0) AS defective_quantity,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'remainingQuantityTask'), 0) AS remaining_quantity_task,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'remainingQuantityProjectAndWarehouse'), 0) AS remaining_quantity_project_and_warehouse,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'usefulQuantity'), 0) AS useful_quantity,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'spentQuantity'), 0) AS spent_quantity,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'price'), 0) AS price,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'usedPrice'), 0) AS used_price,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'defectivePrice'), 0) AS defective_price,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'spentPrice'), 0) AS spent_price,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'remainingPriceTask'), 0) AS remaining_price_task,
        coalesce(max(v.value) FILTER (WHERE ff.key = 'remainingPriceProjectAndWarehouse'), 0) AS remaining_price_project_and_warehouse
    FROM twin t
    JOIN portionClass pc ON pc.class_id = t.twin_class_id
    LEFT JOIN twin_field_decimal v ON v.twin_id = t.id
    LEFT JOIN fixFields ff ON ff.id = v.twin_class_field_id
    WHERE t.flavor_data_list_option_id IS NOT NULL
    GROUP BY t.id
)
SELECT
    pv.twin_id,
    pv.flavor = (SELECT id FROM taskFlavor LIMIT 1) AS is_task,
    CASE WHEN pv.flavor = (SELECT id FROM taskFlavor LIMIT 1) THEN pv.issued_task ELSE pv.issued_pw END AS issued,
    pr.used_quantity AS used_old,
    pr.defective_quantity AS defective_old,
    -- clamp over-consumption: defective to 0 first, then used, until used + defective = issued
    pr.defective_quantity - least(pr.defective_quantity, greatest(pr.used_quantity + pr.defective_quantity - CASE WHEN pv.flavor = (SELECT id FROM taskFlavor LIMIT 1) THEN pv.issued_task ELSE pv.issued_pw END, 0)) AS defective_new,
    pr.used_quantity - greatest(greatest(pr.used_quantity + pr.defective_quantity - CASE WHEN pv.flavor = (SELECT id FROM taskFlavor LIMIT 1) THEN pv.issued_task ELSE pv.issued_pw END, 0) - pr.defective_quantity, 0) AS used_new,
    pr.price,
    pr.remaining_quantity_task,
    pr.remaining_quantity_project_and_warehouse,
    pr.remaining_price_task,
    pr.remaining_price_project_and_warehouse,
    pr.used_price,
    pr.defective_price,
    pr.spent_price,
    pr.useful_quantity,
    pr.spent_quantity
FROM portionValues pv
JOIN portionRest pr ON pr.twin_id = pv.twin_id;

-- numeric columns derived from the clamped consumption (computed here so every statement reads the same numbers)
ALTER TABLE portion_conservation_fix_plan
    ADD COLUMN remaining_new numeric,
    ADD COLUMN useful_new numeric,
    ADD COLUMN spent_quantity_new numeric,
    ADD COLUMN remaining_price_new numeric,
    ADD COLUMN used_price_new numeric,
    ADD COLUMN defective_price_new numeric,
    ADD COLUMN spent_price_new numeric;

UPDATE portion_conservation_fix_plan p
SET remaining_new = p.issued - p.used_new - p.defective_new,
    useful_new = p.issued,
    spent_quantity_new = p.used_new + p.defective_new,
    remaining_price_new = round((p.issued - p.used_new - p.defective_new) * p.price, 2),
    used_price_new = round(p.used_new * p.price, 2),
    defective_price_new = round(p.defective_new * p.price, 2),
    spent_price_new = round(p.used_new * p.price, 2) + round(p.defective_new * p.price, 2);

-- ====================================================================
-- 1) usedQuantity / defectiveQuantity: clamp on over-consumed portions (rows always exist here)
-- ====================================================================

UPDATE twin_field_decimal tfd
SET value = p.used_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'usedQuantity'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.used_new;

UPDATE twin_field_decimal tfd
SET value = p.defective_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'defectiveQuantity'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.defective_new;

-- ====================================================================
-- 2) remainingQuantityTask / remainingQuantityProjectAndWarehouse := issued - used - defective
-- ====================================================================

UPDATE twin_field_decimal tfd
SET value = p.remaining_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id
    AND f.key = CASE WHEN p.is_task THEN 'remainingQuantityTask' ELSE 'remainingQuantityProjectAndWarehouse' END
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.remaining_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.remaining_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id
    AND f.key = CASE WHEN p.is_task THEN 'remainingQuantityTask' ELSE 'remainingQuantityProjectAndWarehouse' END
WHERE p.remaining_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );

-- ====================================================================
-- 3) usefulQuantity := issued, spentQuantity := used + defective
-- ====================================================================

UPDATE twin_field_decimal tfd
SET value = p.useful_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'usefulQuantity'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.useful_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.useful_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'usefulQuantity'
WHERE p.useful_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );

UPDATE twin_field_decimal tfd
SET value = p.spent_quantity_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'spentQuantity'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.spent_quantity_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.spent_quantity_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'spentQuantity'
WHERE p.spent_quantity_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );

-- ====================================================================
-- 4) prices: remainingPrice*, usedPrice, defectivePrice, spentPrice (consumption/remaining * price, 2 decimals)
-- ====================================================================

UPDATE twin_field_decimal tfd
SET value = p.remaining_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id
    AND f.key = CASE WHEN p.is_task THEN 'remainingPriceTask' ELSE 'remainingPriceProjectAndWarehouse' END
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.remaining_price_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.remaining_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id
    AND f.key = CASE WHEN p.is_task THEN 'remainingPriceTask' ELSE 'remainingPriceProjectAndWarehouse' END
WHERE p.remaining_price_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );

UPDATE twin_field_decimal tfd
SET value = p.used_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'usedPrice'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.used_price_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.used_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'usedPrice'
WHERE p.used_price_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );

UPDATE twin_field_decimal tfd
SET value = p.defective_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'defectivePrice'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.defective_price_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.defective_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'defectivePrice'
WHERE p.defective_price_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );

UPDATE twin_field_decimal tfd
SET value = p.spent_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'spentPrice'
WHERE tfd.twin_id = p.twin_id
  AND tfd.twin_class_field_id = f.id
  AND tfd.value IS DISTINCT FROM p.spent_price_new;

INSERT INTO twin_field_decimal (id, twin_id, twin_class_field_id, value)
SELECT gen_random_uuid(), p.twin_id, f.id, p.spent_price_new
FROM portion_conservation_fix_plan p
JOIN twin_class tc ON tc.key = 'SUPPLY_PORTION'
JOIN twin_class_field f ON f.twin_class_id = tc.id AND f.key = 'spentPrice'
WHERE p.spent_price_new <> 0
  AND NOT EXISTS (
      SELECT 1 FROM twin_field_decimal tfd
      WHERE tfd.twin_id = p.twin_id AND tfd.twin_class_field_id = f.id
  );
