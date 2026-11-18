-- TWINS-943: explicit sibling ordering for navbar menu items.
-- target_page_face_id becomes nullable: menu items now point to FACE_PAGE twins via target_twin_id.

alter table face_navbar_nb001_menu_item
    add column if not exists "order" integer not null default 0;

create index if not exists idx_face_navbar_nb001_menu_item_sibling_order
    on face_navbar_nb001_menu_item (face_id, parent_face_navbar_nb001_menu_item_id, "order");

alter table face_navbar_nb001_menu_item
    alter column target_page_face_id drop not null;
