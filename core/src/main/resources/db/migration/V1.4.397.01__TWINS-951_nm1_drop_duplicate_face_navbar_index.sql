-- TWINS-951: drop accidental duplicate index on face_navbar_nb001_menu_item(target_page_face_id).
-- V1.4.97.01 created the same index twice (idx_face_navbar_nb001_menu_items_target_page_face_id
-- and ..._2); the original name is kept, the _2 copy is dropped.
DROP INDEX IF EXISTS idx_face_navbar_nb001_menu_items_target_page_face_id_2;
