create table if not exists face_page_pg001_layout
(
    id          varchar(40) not null
        constraint face_page_pg001_layout_pk
            primary key,
    description varchar(255)
);

INSERT INTO face_page_pg001_layout (id, description) VALUES ('ONE_COLUMN', 'Single column layout') on conflict do nothing;
INSERT INTO face_page_pg001_layout (id, description) VALUES ('TWO_COLUMNS', 'Two columns layout') on conflict do nothing;
INSERT INTO face_page_pg001_layout (id, description) VALUES ('THREE_COLUMNS', 'Three columns layout') on conflict do nothing;

alter table public.face_page_pg001
    add if not exists face_page_pg001_layout_id varchar(40)
        constraint face_page_pg001_face_page_pg001_layout_id_fk
            references public.face_page_pg001_layout
            on update cascade on delete restrict;

update face_page_pg001 set face_page_pg001_layout_id = 'ONE_COLUMN' where face_page_pg001_layout_id is null;
alter table public.face_page_pg001
    alter column face_page_pg001_layout_id set not null;

alter table public.face_page_pg001_widget
    drop column if exists widget_order;

alter table public.face_page_pg001_widget
    add if not exists "column" integer default 1 not null;

alter table public.face_page_pg001_widget
    add if not exists row integer default 1 not null;

alter table public.face_page_pg001_widget
    add if not exists active boolean default true not null;