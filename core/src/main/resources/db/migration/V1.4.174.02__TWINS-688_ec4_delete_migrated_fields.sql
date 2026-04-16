delete
from twin_field_simple
where twin_class_field_id in (select id from twin_class_field where field_typer_featurer_id=1317);
