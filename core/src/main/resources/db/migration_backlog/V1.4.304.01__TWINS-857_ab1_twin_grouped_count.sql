-- TWINS-857: Twin grouped COUNT by dynamic fields — TwinCounter featurer type (group/count by
-- dynamic twin class fields, on top of the existing basic-field grouping).
-- Mirrors V1.3.402.01 + V1.3.431.01 (twin sorter). Counter ids use the 54xx range (TYPE_54).

-- 1. New featurer type
INSERT INTO featurer_type (id, name, description)
VALUES (54, 'Twin Search Counter', 'Group/count twin search')
ON CONFLICT (id) DO NOTHING;

-- 2. Register counter featurers (class resolved by @Featurer id)
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5401, 54, 'org.twins.core.featurer.twin.counter.TwinCounterStub',         'No grouping',     '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5402, 54, 'org.twins.core.featurer.twin.counter.TwinCounterNumberField',  'Group by number', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5403, 54, 'org.twins.core.featurer.twin.counter.TwinCounterTextField',    'Group by text',   '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5404, 54, 'org.twins.core.featurer.twin.counter.TwinCounterDateField',    'Group by date',   '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5405, 54, 'org.twins.core.featurer.twin.counter.TwinCounterBooleanField', 'Group by boolean','', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5406, 54, 'org.twins.core.featurer.twin.counter.TwinCounterDataListField','Group by dataList','', false) ON CONFLICT (id) DO NOTHING;

-- 3. Per-field counter config (mirror twin_sorter_* columns)
ALTER TABLE IF EXISTS twin_class_field
    ADD COLUMN IF NOT EXISTS twin_counter_featurer_id int,
    ADD COLUMN IF NOT EXISTS twin_counter_params hstore;

-- Backfill default counter per field typer (mirror V1.3.431.01 sorter backfill)
UPDATE twin_class_field SET twin_counter_featurer_id = NULL;
-- Boolean (1306) -> 5405
UPDATE twin_class_field SET twin_counter_featurer_id = 5405 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1306);
-- text (1301) -> 5403
UPDATE twin_class_field SET twin_counter_featurer_id = 5403 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1301);
-- number (1317) -> 5402
UPDATE twin_class_field SET twin_counter_featurer_id = 5402 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1317);
-- date (1302) -> 5404
UPDATE twin_class_field SET twin_counter_featurer_id = 5404 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1302);
-- other -> stub 5401
UPDATE twin_class_field SET twin_counter_featurer_id = 5401 WHERE twin_counter_featurer_id IS NULL;
-- single-select datalist (1305) -> 5406
UPDATE twin_class_field SET twin_counter_featurer_id = 5406 WHERE field_typer_featurer_id = 1305 AND (twin_counter_params->'multiple' = 'false' OR twin_counter_params->'multiple' IS NULL);
