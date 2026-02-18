insert into twin_field_decimal (id, twin_id, twin_class_field_id, value)
select tfs.id, tfs.twin_id, tfs.twin_class_field_id, tfs.value::decimal
from twin_field_simple tfs
where tfs.twin_class_field_id in (select id from twin_class_field where field_typer_featurer_id=1317)
    and tfs.value is not null and tfs.value!=''
on conflict do nothing;
