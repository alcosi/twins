insert into twin_field_decimal (id, twin_id, twin_class_field_id, value)
select tfs.id, tfs.twin_id, tfs.twin_class_field_id, tfs.value::decimal
from twin_field_simple tfs
where tfs.twin_class_field_id in (select id from twin_class_field where field_typer_featurer_id=1317)
on conflict do nothing;

update twin_class_field
set field_typer_featurer_id=1351
where field_typer_featurer_id=1317;

delete
from twin_field_simple
where twin_class_field_id in (select id from twin_class_field where field_typer_featurer_id=1317);
