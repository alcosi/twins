create table if not exists face_component_type
(
    id          varchar not null
        constraint face_component_type_pk
            primary key,
    name        varchar not null,
    description varchar
);

insert into face_component_type values ('NAVBAR', 'Navigation bar') on conflict do nothing ;
insert into face_component_type values ('PAGE', 'Page') on conflict do nothing ;
insert into face_component_type values ('WIDGET', 'Widget') on conflict do nothing ;

create table if not exists face_component
(
    id                     varchar(5) not null
        constraint face_component_pk
            primary key,
    face_component_type_id varchar    not null
        constraint face_component_face_component_type_id_fk
            references face_component_type
            on update cascade on delete restrict,
    name                   varchar    not null,
    description            varchar
);

insert into face_component values ('NB001', 'NAVBAR', 'Simple navigation bar')  on conflict do nothing ;
insert into face_component values ('PG001', 'PAGE', 'Simple one column page')  on conflict do nothing ;
insert into face_component values ('WT001', 'WIDGET', 'Twins table with of specific class')  on conflict do nothing ;

create table if not exists face
(
    id                uuid       not null
        constraint face_pk
            primary key,
    domain_id                   uuid
        constraint face_domain_id_fk
            references domain
            on update cascade,
    face_component_id varchar(4) not null
        constraint face_face_component_id_fk
            references face_component_type
            on update cascade on delete restrict,
    name              varchar,
    description       varchar,
    created_at             timestamp default CURRENT_TIMESTAMP,
    created_by_user_id     uuid
        constraint face_created_by_user_id_fk
            references "user"
            on update cascade
);

alter table domain
    add if not exists navbar_face_id uuid
        constraint domain_navbar_face_id_fk
            references face
            on update cascade on delete restrict;



create table if not exists face_navbar_nb001_status
(
    id varchar not null
        constraint face_navbar_nb001_status_pk
            primary key
);

insert into face_navbar_nb001_status values ('ACTIVE') on conflict do nothing ;
insert into face_navbar_nb001_status values ('DISABLED')  on conflict do nothing ;
insert into face_navbar_nb001_status values ('HIDDEN')  on conflict do nothing ;

create table if not exists face_navbar_nb001
(
    face_id                     uuid    not null
        primary key
        constraint face_navbar_nb001_face_id_fk
            references face
            on update cascade on delete restrict,
    skin                     varchar not null
);

create table if not exists face_navbar_nb001_menu_items
(
    id                          uuid    not null
        constraint face_navbar_nb001_menu_items_pk
            primary key,
    face_id                     uuid    not null
        constraint face_navbar_nb001_menu_items_face_id_fk
            references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid not null
        constraint face_navbar_nb001_menu_items_label_i18n_id_fk
            references i18n
            on update cascade,
    description_i18n_id               uuid not null
        constraint face_navbar_nb001_menu_items_description_i18n_id_fk
            references i18n
            on update cascade,
    icon_dark_resource_id                  uuid
        references resource
            on update cascade,
    icon_light_resource_id                 uuid
        references resource
            on update cascade,
    face_navbar_nb001_status_id varchar not null
        constraint face_navbar_nb001_menu_items_face_navbar_nb001_status_id_fk
            references face_navbar_nb001_status
            on update cascade on delete restrict,
    target_page_face_id               uuid    not null
        constraint face_navbar_nb001_menu_items_page_face_id_fk
            references face
            on update cascade on delete restrict
);

create table if not exists face_page_pg001
(
    face_id                     uuid    not null
        constraint face_page_pg001_face_id_fk
            primary key
            references face
            on update cascade on delete restrict,
    widget_order                     integer not null default 0,
    widget_face_id               uuid    not null
        constraint face_page_pg001_widget_face_id_fk
            references face
            on update cascade on delete restrict
);

create table if not exists face_widget_wt001
(
    face_id                     uuid    not null
        constraint face_widget_wt001_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    key                     varchar not null,
    label_i18n_id               uuid not null
        constraint face_widget_wt001_label_i18n_id_fk
            references i18n
            on update cascade,
    twin_class_id               uuid not null
        constraint face_widget_wt001_twin_class_id_fk
            references i18n
            on update cascade,
    hide_columns                varchar[]
);
