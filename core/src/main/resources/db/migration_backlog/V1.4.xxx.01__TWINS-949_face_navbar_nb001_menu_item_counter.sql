-- TWINS-943: optional counter badge for navbar menu items.
-- The badge count is produced by executing the saved twin search referenced here.

alter table face_navbar_nb001_menu_item
    add column if not exists counter_twin_search_id uuid references twin_search on update cascade on delete restrict;

create index if not exists face_navbar_nb001_menu_item_counter_twin_search_id_idx
    on face_navbar_nb001_menu_item(counter_twin_search_id);
