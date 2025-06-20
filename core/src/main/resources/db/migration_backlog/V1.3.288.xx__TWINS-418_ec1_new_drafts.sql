create table if not exists draft_twin_field_simple_non_indexed
(
    id                   uuid    not null
        constraint draft_twin_field_simple_non_indexed_pk primary key,
    draft_id             uuid    not null
        constraint draft_twin_field_simple_non_indexed_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis       bigint  not null,
    cud_id               varchar not null
        constraint draft_twin_field_simple_non_indexed_cud_id_fk
            references cud
            on update cascade,
    twin_field_simple_id uuid,
    twin_id              uuid    not null,
    twin_class_field_id  uuid    not null,
    value                text
);

create index if not exists draft_twin_field_simple_non_indexed_draft_id_index
    on draft_twin_field_simple_non_indexed (draft_id);

create index if not exists draft_twin_field_simple_non_indexed_twin_id_index
    on draft_twin_field_simple_non_indexed (twin_id);

create table if not exists draft_twin_field_boolean
(
    id                   uuid    not null
        constraint draft_twin_field_boolean_pk primary key,
    draft_id             uuid    not null
        constraint draft_twin_field_boolean_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis       bigint  not null,
    cud_id               varchar not null
        constraint draft_twin_field_boolean_cud_id_fk
            references cud
            on update cascade,
    twin_field_simple_id uuid,
    twin_id              uuid    not null,
    twin_class_field_id  uuid    not null,
    value                boolean
);

create index if not exists draft_twin_field_boolean_draft_id_index
    on draft_twin_field_boolean (draft_id);

create index if not exists draft_twin_field_boolean_twin_id_index
    on draft_twin_field_boolean (twin_id);
