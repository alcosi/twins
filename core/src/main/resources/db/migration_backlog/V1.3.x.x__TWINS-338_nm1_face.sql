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
    face_component_id varchar(5) not null
        constraint face_face_component_id_fk
            references face_component
            on update cascade on delete restrict,
    name              varchar,
    description       varchar,
    created_at             timestamp default CURRENT_TIMESTAMP,
    created_by_user_id     uuid
        constraint face_created_by_user_id_fk
            references "user"
            on update cascade
);

--  insert into face values ('00000000-0000-0000-0008-000000000001', null, 'NB001', 'Create me', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;

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
    description_i18n_id               uuid
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
            on update cascade on delete restrict
);

create table if not exists face_page_pg001_widget
(
    id                          uuid    not null
        constraint face_page_pg001_widget_pk
            primary key,
    face_id                     uuid    not null
        constraint face_page_pg001_widget_face_id_fk
        references face
            on update cascade on delete restrict,
    widget_order                     integer not null default 0,
    widget_face_id               uuid    not null
        constraint face_page_pg001_widget_widget_face_id_fk
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
            references twin_class
            on update cascade,
    search_id               uuid
        constraint face_widget_wt001_search_id_fk
            references search
            on update cascade,
    hide_columns                varchar[]
);

insert into i18n_type values ('faceElement', 'Common type for all faces') on conflict do nothing;


-- configuring WNR simple navbar example
insert into face values ('b929c624-368c-4a5f-84b7-0522f3257e3b', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'NB001', 'WNR navigation bar', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('d425b8a6-9855-4baa-ae29-99b6b0bfb446', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Projects page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('f0e1c3b8-d88f-46a3-b013-e52a7cdff34f', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Tasks page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('61f47cfe-5ea3-44f1-b007-effa920fceb9', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Tools page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('0663cb2f-0a0e-437b-a81d-a2f74df04295', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Supplies page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('07abc204-e1f4-45ff-8585-1379fa34ba3d', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All projects table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('040bdc99-3288-40b3-892f-65a470cd0aa8', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All tasks table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('b3a5c11c-3f07-42cc-bb04-be1da3291439', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All tools table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('11407415-838d-4bea-b837-d62cc5017045', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All supplies table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face_navbar_nb001 values ('b929c624-368c-4a5f-84b7-0522f3257e3b', 'primary') on conflict do nothing;
insert into i18n values ('77a4eee9-41cf-4747-adb9-088d9d88c953', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('88cdaaeb-5f10-4f69-9bff-d284824bb142', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('6c4ec589-423d-4e36-959d-6ac4b21a2b77', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('7b7c59b3-2fcc-470e-a1a1-81d78560074e', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('77a4eee9-41cf-4747-adb9-088d9d88c953', 'en', 'Projects') on conflict do nothing ;
insert into i18n_translation values ('88cdaaeb-5f10-4f69-9bff-d284824bb142', 'en', 'Tasks') on conflict do nothing ;
insert into i18n_translation values ('6c4ec589-423d-4e36-959d-6ac4b21a2b77', 'en', 'Tools') on conflict do nothing ;
insert into i18n_translation values ('7b7c59b3-2fcc-470e-a1a1-81d78560074e', 'en', 'Supplies') on conflict do nothing ;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, face_navbar_nb001_status_id, target_page_face_id) values ('ea954367-2e2b-4b36-ac37-0afc54e9d6fc','b929c624-368c-4a5f-84b7-0522f3257e3b', 'projects', '77a4eee9-41cf-4747-adb9-088d9d88c953', 'ACTIVE', 'd425b8a6-9855-4baa-ae29-99b6b0bfb446') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, face_navbar_nb001_status_id, target_page_face_id) values ('f30e3369-6f35-45c2-a333-94ac510b39e2','b929c624-368c-4a5f-84b7-0522f3257e3b', 'tasks',    '88cdaaeb-5f10-4f69-9bff-d284824bb142', 'ACTIVE', 'f0e1c3b8-d88f-46a3-b013-e52a7cdff34f') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, face_navbar_nb001_status_id, target_page_face_id) values ('b53cde28-d5b1-4dad-83a5-8572d0bd8293','b929c624-368c-4a5f-84b7-0522f3257e3b', 'tools',    '6c4ec589-423d-4e36-959d-6ac4b21a2b77', 'ACTIVE', '61f47cfe-5ea3-44f1-b007-effa920fceb9') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, face_navbar_nb001_status_id, target_page_face_id) values ('c5ab6edb-dda3-4d0f-a9cd-a512bc849f7d','b929c624-368c-4a5f-84b7-0522f3257e3b', 'supplies', '7b7c59b3-2fcc-470e-a1a1-81d78560074e', 'ACTIVE', '0663cb2f-0a0e-437b-a81d-a2f74df04295') on conflict do nothing;
insert into face_page_pg001 (face_id) values ('d425b8a6-9855-4baa-ae29-99b6b0bfb446') on conflict do nothing;
insert into face_page_pg001 (face_id) values ('f0e1c3b8-d88f-46a3-b013-e52a7cdff34f') on conflict do nothing;
insert into face_page_pg001 (face_id) values ('61f47cfe-5ea3-44f1-b007-effa920fceb9') on conflict do nothing;
insert into face_page_pg001 (face_id) values ('0663cb2f-0a0e-437b-a81d-a2f74df04295') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, widget_order, widget_face_id) values ('b7b67c9c-b810-485d-9edc-dac500140243', 'd425b8a6-9855-4baa-ae29-99b6b0bfb446', 1, '07abc204-e1f4-45ff-8585-1379fa34ba3d') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, widget_order, widget_face_id) values ('a713d757-a180-4f94-bbac-c100c1f6123f', 'f0e1c3b8-d88f-46a3-b013-e52a7cdff34f', 1, '040bdc99-3288-40b3-892f-65a470cd0aa8') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, widget_order, widget_face_id) values ('a9be080f-4fc1-42e0-8ccc-eee603cce971', '61f47cfe-5ea3-44f1-b007-effa920fceb9', 1, 'b3a5c11c-3f07-42cc-bb04-be1da3291439') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, widget_order, widget_face_id) values ('93662b84-b488-44e4-9f3c-25737080509d', '0663cb2f-0a0e-437b-a81d-a2f74df04295', 1, '11407415-838d-4bea-b837-d62cc5017045') on conflict do nothing;
update domain set navbar_face_id = 'b929c624-368c-4a5f-84b7-0522f3257e3b' where id = 'f67ad556-dd27-4871-9a00-16fb0e8a4102' and domain.navbar_face_id is null;
