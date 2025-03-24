
alter table twin_class
    add if not exists page_face_id uuid
        constraint twin_class_page_face_id_fk
            references face
            on update cascade on delete restrict;


create table if not exists face_widget_wt003
(
    face_id                     uuid    not null
        constraint face_widget_wt003_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid not null
        constraint face_widget_wt003_label_i18n_id_fk
            references i18n
            on update cascade,
    images_twin_class_field_id               uuid not null
        constraint face_widget_wt003_twin_class_id_fk
            references twin_class_field
            on update cascade
);

insert into face_component values ('WT003', 'WIDGET', 'Twins image gallery')  on conflict do nothing ;