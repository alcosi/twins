create table if not exists face_layout_container
(
    id          varchar(40) not null
        constraint face_layout_container_pk
            primary key,
    description varchar(255)
);

insert into face_layout_container values ('FLEX', 'Flex Container') on conflict do nothing ;
insert into face_layout_container values ('GRID', 'Grid Container') on conflict do nothing ;

alter table face_page_pg001
    add column if not exists face_layout_container_id varchar(40)
        constraint face_page_pg001_face_layout_container_id_fk
            references public.face_layout_container (id)
            on update cascade;
update face_page_pg001 set face_layout_container_id = 'FLEX' where face_layout_container_id is null;
alter table face_page_pg001
    alter column face_layout_container_id set not null;
alter table face_page_pg001
    add column if not exists face_layout_container_attributes hstore;
alter table face_page_pg001
    drop column if exists face_page_pg001_layout_id;
drop table if exists face_page_pg001_layout;
alter table face_page_pg001_widget
    add column if not exists layout_container_item_attributes hstore;
-- alter table face_page_pg001_widget
--     drop column if exists row;
-- alter table face_page_pg001_widget
--     drop column if exists "column";

alter table face_page_pg002_tab
    add column if not exists face_layout_container_id varchar(40)
        constraint face_page_pg002_tab_face_layout_container_id_fk
            references public.face_layout_container (id)
            on update cascade;
update face_page_pg002_tab set face_layout_container_id = 'FLEX' where face_layout_container_id is null;
alter table face_page_pg002_tab
    alter column face_layout_container_id set not null;
alter table face_page_pg002_tab
    add column if not exists face_layout_container_attributes hstore;
alter table face_page_pg002_tab
    drop column if exists face_page_pg002_tab_layout_id;
drop table if exists face_page_pg002_tab_layout;
alter table face_page_pg002_widget
    add column if not exists layout_container_item_attributes hstore;
-- alter table face_page_pg002_widget
--     drop column if exists row;
-- alter table face_page_pg002_widget
--     drop column if exists "column";

