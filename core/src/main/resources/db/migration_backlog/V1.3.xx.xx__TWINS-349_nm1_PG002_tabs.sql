create table if not exists face_page_pg002_tab_layout
(
    id          varchar(40) not null
        constraint face_page_pg002_tab_layout_pk
            primary key,
    description varchar(255)
);

INSERT INTO face_page_pg002_tab_layout (id, description) VALUES ('ONE_COLUMN', 'Single column layout') on conflict do nothing;
INSERT INTO face_page_pg002_tab_layout (id, description) VALUES ('TWO_COLUMNS', 'Two columns layout') on conflict do nothing;
INSERT INTO face_page_pg002_tab_layout (id, description) VALUES ('THREE_COLUMNS', 'Three columns layout') on conflict do nothing;


create table if not exists face_page_pg002_tab
(
    id             uuid                 not null
        constraint face_page_pg002_tab_pk
            primary key,
    face_id                   uuid        not null
        constraint face_page_pg002_tab_face_id_fk
        references face
            on update cascade on delete cascade ,
    face_page_pg002_tab_layout_id varchar(40) not null
        constraint face_page_pg002_tab_face_page_pg002_tab_layout_id_fk
            references face_page_pg002_tab_layout
            on update cascade on delete restrict,
    icon_resource_id                  uuid
        constraint face_page_pg002_tab_icon_resource_id_fk
            references resource
            on update cascade,
    title_i18n_id             uuid
        constraint face_page_pg002_tab_title_i18n_id_fk
            references i18n
            on update cascade,
    active         boolean default true not null
);

create table if not exists face_page_pg002_layout
(
    id          varchar(40) not null
        constraint face_page_pg002_layout_pk
            primary key,
    description varchar(255)
);

INSERT INTO face_page_pg002_layout (id, description) VALUES ('TOP', 'Tabs on top layout') on conflict do nothing;
INSERT INTO face_page_pg002_layout (id, description) VALUES ('BOTTOM', 'Tabs on bottom layout') on conflict do nothing;
INSERT INTO face_page_pg002_layout (id, description) VALUES ('LEFT', 'Tabs on left layout') on conflict do nothing;
INSERT INTO face_page_pg002_layout (id, description) VALUES ('RIGHT', 'Tabs on right layout') on conflict do nothing;


create table if not exists face_page_pg002
(
    face_id                   uuid        not null
        constraint face_page_pg002_face_id_fk
            primary key
        references face
            on update cascade on delete cascade ,
    title_i18n_id             uuid
        constraint face_page_pg002_title_i18n_id_fk
            references i18n
            on update cascade,
    face_page_pg002_layout_id varchar(40) not null
        constraint face_page_pg002_face_page_pg002_layout_id_fk
            references face_page_pg002_layout
            on update cascade on delete restrict
);



create table if not exists face_page_pg002_widget
(
    id             uuid                 not null
        constraint face_page_pg002_widget_pk
            primary key,
    face_page_pg002_tab_id        uuid                 not null
        constraint face_page_pg002_widget_face_page_pg002_tab_id_fk
            references face_page_pg002_tab
            on update cascade on delete cascade,
    widget_face_id uuid                 not null
        constraint face_page_pg002_widget_widget_face_id_fk
            references face
            on update cascade on delete restrict,
    "column"       integer default 1    not null,
    row            integer default 1    not null,
    active         boolean default true not null
);

insert into face_component values ('PG002', 'PAGE', 'Tabbed page')  on conflict do nothing ;