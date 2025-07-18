insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (1334, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperTwinClassIdField', 'Twin class id field', 'Field typer for twin class id field', false)
on conflict do nothing;

create table if not exists twin_field_twin_class
(
    id                  UUID primary key,
    twin_id             UUID not null references twin (id) on update cascade on delete cascade,
    twin_class_field_id UUID not null references  twin_class_field (id) on update cascade on delete cascade,
    value               UUID
);

CREATE INDEX IF NOT EXISTS twin_field_boolean_twin_class_field_id_value_index ON twin_field_boolean (twin_class_field_id, value);
CREATE UNIQUE INDEX IF NOT EXISTS twin_field_boolean_twin_class_field_id_twin_id_uindex ON twin_field_boolean (twin_id, twin_class_field_id);

