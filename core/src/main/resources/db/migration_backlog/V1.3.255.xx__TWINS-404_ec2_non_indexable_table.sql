create table if not exists twin_field_simple_non_indexed (
    id uuid not null,
    twin_id uuid not null,
    twin_class_field_id uuid not null,
    value text,

    constraint twin_field_simple_non_indexed_pk
        primary key (id),
    constraint twin_field_simple_non_indexed_twin_id_fk
        foreign key (twin_id) references twin(id) on update cascade on delete cascade,
    constraint twin_field_simple_non_indexed_twin_class_field_id_fk
        foreign key (twin_class_field_id) references twin_class_field(id) on update cascade on delete cascade
)
