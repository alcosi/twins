
-- create table if not exists twin_pointer
-- (
--     id                     varchar    not null
--         constraint fk_twin_pointer_id
--             primary key
-- );
--
-- insert into twin_pointer values ('current_twin');
-- insert into twin_pointer values ('current_twin.head_twin');
-- insert into twin_pointer values ('current_twin.linked_twin');

create table if not exists face_twidget_tw003
(
    face_id                     uuid    not null
        constraint face_twidget_tw003_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid
        constraint face_twidget_tw003_label_i18n_id_fk
            references i18n
            on update cascade,
    label_value_inline boolean default false
);

create table if not exists face_twidget_tw003_row
(
    id                          uuid    not null
        constraint face_twidget_tw003_row_pk
            primary key,
    face_id                     uuid    not null
        constraint face_twidget_tw003_row_face_id_fk
            references face
            on update cascade on delete cascade,
    "column"                     integer default 1 not null,
    "row"                     integer default 1 not null,
    label_i18n_id               uuid not null
        constraint face_twidget_tw003_accordion_item_label_i18n_id_fk
            references i18n
            on update cascade
);

INSERT INTO face_component (id, face_component_type_id, name, description) VALUES ('TW003', 'TWIDGET', 'Twins fields panel', null);

