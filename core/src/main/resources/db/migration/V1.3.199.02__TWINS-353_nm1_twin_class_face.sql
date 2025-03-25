
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
    label_i18n_id               uuid
        constraint face_widget_wt003_label_i18n_id_fk
            references i18n
            on update cascade,
    images_twin_class_field_id               uuid
        constraint face_widget_wt003_images_twin_class_field_id_fk
            references twin_class_field
            on update cascade
);

create table if not exists face_widget_wt004
(
    face_id                     uuid    not null
        constraint face_widget_wt004_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid
        constraint face_widget_wt004_label_i18n_id_fk
            references i18n
            on update cascade,
    i18n_twin_class_field_id               uuid not null
        constraint face_widget_wt004_i18n_twin_class_field_id_fk
            references twin_class_field
            on update cascade
);

create table if not exists face_widget_wt004_accordion_item
(
    id                          uuid    not null
        constraint face_widget_wt004_accordion_item_pk
            primary key,
    face_id                     uuid    not null
        constraint face_widget_wt004_accordion_item_face_id_fk
            references face
            on update cascade on delete cascade,
    locale                     varchar(2) not null
        constraint face_widget_wt004_accordion_item_locale_id_fk
            references i18n_locale on update cascade,
    label_i18n_id               uuid not null
        constraint face_widget_wt004_accordion_item_label_i18n_id_fk
            references i18n
            on update cascade
);

insert into face_component values ('WT003', 'WIDGET', 'Twins image gallery')  on conflict do nothing ;
insert into face_component values ('WT004', 'WIDGET', 'Twins i18n field accordion')  on conflict do nothing ;

alter table public.face_navbar_nb001_menu_items
    drop constraint face_navbar_nb001_menu_items_face_id_fk;

alter table public.face_navbar_nb001_menu_items
    add constraint face_navbar_nb001_menu_items_face_id_fk
        foreign key (face_id) references public.face
            on update cascade on delete cascade;