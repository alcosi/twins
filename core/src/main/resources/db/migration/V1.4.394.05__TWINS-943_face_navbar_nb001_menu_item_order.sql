-- TWINS-943: explicit sibling ordering for navbar menu items.
-- target_page_face_id becomes nullable: menu items now point to FACE_PAGE twins via target_twin_id.

alter table face_navbar_nb001_menu_item
    add column if not exists "order" integer not null default 0;

-- Backfill: leaving every row at order=0 would silently switch all existing domains to
-- key-alphabetical menu order (main had no ORDER BY at all, so users see physical insertion
-- order). Approximate that visible order by the physical row position (ctid) inside each
-- sibling group; gaps of 10 leave room for manual reordering. Best effort: rows moved by
-- updates may shift within their group, but group membership is always preserved.
with ranked as (
    select id,
           (row_number() over (partition by face_id, parent_face_navbar_nb001_menu_item_id order by ctid) - 1) * 10 as new_order
    from face_navbar_nb001_menu_item
)
update face_navbar_nb001_menu_item mi
set "order" = r.new_order
from ranked r
where r.id = mi.id and mi."order" = 0;

create index if not exists idx_face_navbar_nb001_menu_item_sibling_order
    on face_navbar_nb001_menu_item (face_id, parent_face_navbar_nb001_menu_item_id, "order");

alter table face_navbar_nb001_menu_item
    alter column target_page_face_id drop not null;
