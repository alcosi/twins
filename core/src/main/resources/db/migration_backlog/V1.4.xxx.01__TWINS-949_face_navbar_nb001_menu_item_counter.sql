-- TWINS-949: optional counter badge for navbar menu items.
-- The badge count is produced by executing the saved twin search referenced here.

alter table face_navbar_nb001_menu_item
    add column if not exists counter_twin_search_id uuid;

alter table face_navbar_nb001_menu_item drop constraint if exists face_nb001_menu_item_counter_twin_search_id_fk;
alter table face_navbar_nb001_menu_item
    add constraint face_nb001_menu_item_counter_twin_search_id_fk
        foreign key (counter_twin_search_id) references twin_search on update cascade on delete restrict;

create index if not exists face_navbar_nb001_menu_item_counter_twin_search_id_idx
    on face_navbar_nb001_menu_item(counter_twin_search_id);
