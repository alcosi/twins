insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (1334, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperTwinClassListField', 'Twin class list field', 'Field typer for twin class list field', false)
on conflict do nothing;


create table if not exists twin_field_twin_class
(
    id                  UUID primary key,
    twin_id             UUID not null references twin (id) on update cascade on delete cascade,
    twin_class_field_id UUID not null references twin_class_field (id) on update cascade on delete cascade,
    twin_class_id       UUID not null references twin_class (id) on update cascade on delete cascade
);

create index if not exists twin_field_twin_class_list_twin_class_field_id_idx
    on twin_field_twin_class (twin_class_field_id);

create index if not exists twin_field_twin_class_list_twin_class_id_idx
    on twin_field_twin_class (twin_class_id);

create index if not exists twin_field_twin_class_list_twin_id_idx
    on twin_field_twin_class (twin_id);
