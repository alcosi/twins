-- TWINS-949: drop deprecated target_page_face_id from navbar menu items.
-- Menu items now point to FACE_PAGE twins via target_twin_id; the client no longer reads this field.
-- Dropping the column also drops its FK constraints (page_face_id_fk, target_page_face_id_fk)
-- and indexes (idx_face_nb001_menu_items_target_page_face_id[_2]) automatically.

alter table face_nb001_menu_item
    drop column if exists target_page_face_id;
