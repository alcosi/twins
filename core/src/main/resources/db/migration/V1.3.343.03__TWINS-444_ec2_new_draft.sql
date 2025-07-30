create table if not exists draft_twin_field_twin_class
(
    id                               uuid    not null primary key,
    draft_id                         uuid    not null references draft on update cascade on delete cascade,
    time_in_millis                   bigint  not null,
    cud_id                           varchar not null references cud on update cascade,
    twin_field_twin_class_id         uuid,
    twin_id                          uuid    not null,
    twin_class_field_id              uuid    not null,
    twin_class_id                    uuid    not null references twin_class on update cascade
);

create index if not exists draft_twin_field_twin_class_draft_id_index
    on draft_twin_field_simple_non_indexed (draft_id);

create index if not exists draft_twin_field_twin_class_twin_id_index
    on draft_twin_field_simple_non_indexed (twin_id);
