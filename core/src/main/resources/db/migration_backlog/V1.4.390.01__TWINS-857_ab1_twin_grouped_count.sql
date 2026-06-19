-- TWINS-857: Twin grouped COUNT by dynamic fields — TwinCounter featurer type (group/count by
-- dynamic twin class fields, on top of the existing basic-field grouping).
-- Mirrors twin sorter (type 41). Type 54 is already Factory Processor, so counters use type 58.
-- Featurer class/name/description are filled from @Featurer at app startup.
--   5801 stub, 5802 decimal, 5803 text, 5804 date, 5805 boolean, 5806 datalist

INSERT INTO featurer_type (id, name, description)
VALUES (58, 'Twin Search Counter', 'Group/count twin search')
ON CONFLICT (id) DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5801, 58, '', '', '', false),
       (5802, 58, '', '', '', false),
       (5803, 58, '', '', '', false),
       (5804, 58, '', '', '', false),
       (5805, 58, '', '', '', false),
       (5806, 58, '', '', '', false)
ON CONFLICT (id) DO NOTHING;

ALTER TABLE IF EXISTS twin_class_field
    ADD COLUMN IF NOT EXISTS twin_counter_featurer_id int,
    ADD COLUMN IF NOT EXISTS twin_counter_params hstore;

UPDATE twin_class_field SET twin_counter_featurer_id = NULL;
-- Boolean (1306) -> 5805
UPDATE twin_class_field SET twin_counter_featurer_id = 5805 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1306);
-- text (1301) -> 5803
UPDATE twin_class_field SET twin_counter_featurer_id = 5803 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1301);
-- decimal (1317) -> 5802
UPDATE twin_class_field SET twin_counter_featurer_id = 5802 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1317);
-- date (1302) -> 5804
UPDATE twin_class_field SET twin_counter_featurer_id = 5804 WHERE twin_counter_featurer_id IS NULL AND field_typer_featurer_id IN (1302);
-- other -> stub 5801
UPDATE twin_class_field SET twin_counter_featurer_id = 5801 WHERE twin_counter_featurer_id IS NULL;
-- single-select datalist (1305) -> 5806
UPDATE twin_class_field SET twin_counter_featurer_id = 5806 WHERE field_typer_featurer_id = 1305 AND (twin_counter_params->'multiple' = 'false' OR twin_counter_params->'multiple' IS NULL);
