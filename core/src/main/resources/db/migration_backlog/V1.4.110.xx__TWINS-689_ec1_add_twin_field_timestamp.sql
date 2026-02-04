CREATE TABLE IF NOT EXISTS twin_field_timestamp
(
    id                  UUID PRIMARY KEY,
    twin_id             UUID NOT NULL REFERENCES twin (id) ON UPDATE CASCADE ON DELETE CASCADE,
    twin_class_field_id UUID NOT NULL REFERENCES twin_class_field (id) ON UPDATE CASCADE ON DELETE CASCADE,
    value               TIMESTAMP
);

CREATE INDEX IF NOT EXISTS twin_field_timestamp_twin_class_field_id_value_index
    ON twin_field_timestamp (twin_class_field_id, value);
CREATE UNIQUE INDEX IF NOT EXISTS twin_field_timestamp_twin_class_field_id_twin_id_uindex
    ON twin_field_timestamp (twin_id, twin_class_field_id);

update featurer
set class = 'org.twins.core.featurer.fieldtyper.FieldTyperTimestamp', name = 'Timestamp', description = 'Timestamp field with dedicated table storage'
where id=1302;

insert into twin_field_timestamp(id, twin_id, twin_class_field_id, value)
select id, twin_id, twin_class_field_id,  NULLIF(value, '')::timestamp
from twin_field_simple
where twin_class_field_id in (select id from twin_class_field where field_typer_featurer_id=1302)
on conflict do nothing;
