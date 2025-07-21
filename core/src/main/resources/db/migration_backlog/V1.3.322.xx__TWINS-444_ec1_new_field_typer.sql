insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (1334, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperTwinClassListFieldld', 'Twin class list field', 'Field typer for twin class list field', false)
on conflict do nothing;


create table if not exists twin_field_twin_class_list
(
    id                  UUID primary key,
    twin_id             UUID not null references twin (id) on update cascade on delete cascade,
    twin_class_field_id UUID not null references  twin_class_field (id) on update cascade on delete cascade
);

create index if not exists twin_field_twin_class_list_twin_class_field_id_idx
    on twin_field_twin_class_list (twin_class_field_id);
create unique index if not exists twin_field_twin_class_list_twin_class_field_id_twin_id_uidx
    on twin_field_twin_class_list (twin_id, twin_class_field_id);

create table if not exists twin_class_list
(
    twin_field_twin_class_list_id UUID not null references twin_field_twin_class_list on update cascade on delete restrict,
    twin_class_id                 UUID not null references twin_class on update cascade on delete restrict
);

create index if not exists twin_class_list_twin_field_twin_class_list_id_idx
    on twin_class_list (twin_field_twin_class_list_id);

create index if not exists twin_class_list_twin_class_id_idx
    on twin_class_list (twin_class_id);
