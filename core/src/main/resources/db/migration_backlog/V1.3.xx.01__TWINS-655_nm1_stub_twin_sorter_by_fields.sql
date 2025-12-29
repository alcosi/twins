-- Boolean (1306) -> Boolean fields sorter 4103
UPDATE twin_class_field SET twin_sorter_featurer_id = 4103 WHERE twin_sorter_featurer_id IS NULL AND field_typer_featurer_id IN (1306);
--text
UPDATE twin_class_field SET twin_sorter_featurer_id = 4104 WHERE twin_sorter_featurer_id IS NULL AND field_typer_featurer_id IN (1301);
-- num
UPDATE twin_class_field SET twin_sorter_featurer_id = 4105 WHERE twin_sorter_featurer_id IS NULL AND field_typer_featurer_id IN (1317);
-- date
UPDATE twin_class_field SET twin_sorter_featurer_id = 4102 WHERE twin_sorter_featurer_id IS NULL AND field_typer_featurer_id IN (1302);
-- select with multiple=false
UPDATE twin_class_field SET twin_sorter_featurer_id = 4106 WHERE twin_sorter_featurer_id IS NULL AND field_typer_featurer_id = 1305 AND (twin_sorter_params->'multiple' = 'false' OR twin_sorter_params->'multiple' IS NULL);
-- other
UPDATE twin_class_field SET twin_sorter_featurer_id = 4101 WHERE twin_sorter_featurer_id IS NULL;

alter table twin_class_field
    alter column twin_sorter_featurer_id set not null;



