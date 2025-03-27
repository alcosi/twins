drop table if exists face_widget_wt003;
drop table if exists face_widget_wt004_accordion_item;
drop table if exists face_widget_wt004;

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

create table if not exists face_twidget_tw001
(
    face_id                     uuid    not null
        constraint face_twidget_tw001_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid
        constraint face_twidget_tw001_label_i18n_id_fk
            references i18n
            on update cascade,
    images_twin_class_field_id               uuid
        constraint face_twidget_tw001_images_twin_class_field_id_fk
            references twin_class_field
            on update cascade
);

create table if not exists face_twidget_tw002
(
    face_id                     uuid    not null
        constraint face_twidget_tw002_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid
        constraint face_twidget_tw002_label_i18n_id_fk
            references i18n
            on update cascade,
    i18n_twin_class_field_id               uuid not null
        constraint face_twidget_tw002_i18n_twin_class_field_id_fk
            references twin_class_field
            on update cascade
);

create table if not exists face_twidget_tw002_accordion_item
(
    id                          uuid    not null
        constraint face_twidget_tw002_accordion_item_pk
            primary key,
    face_id                     uuid    not null
        constraint face_twidget_tw002_accordion_item_face_id_fk
            references face
            on update cascade on delete cascade,
    locale                     varchar(2) not null
        constraint face_twidget_tw002_accordion_item_locale_id_fk
            references i18n_locale on update cascade,
    label_i18n_id               uuid not null
        constraint face_twidget_tw002_accordion_item_label_i18n_id_fk
            references i18n
            on update cascade
);

INSERT INTO face_component_type (id, name, description) VALUES ('TWIDGET', 'Twin widget', null) on conflict do nothing ;
UPDATE face_component SET id = 'TW001', face_component_type_id = 'TWIDGET' WHERE id LIKE 'WT003' and name like 'Twins image gallery';
UPDATE face_component SET id = 'TW002', face_component_type_id = 'TWIDGET' WHERE id LIKE 'WT004' and name like 'Twins i18n field accordion';
