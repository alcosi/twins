ALTER TABLE face_navbar_nb001_menu_item
    ADD COLUMN IF NOT EXISTS "order" integer NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_item_sibling_order
    ON face_navbar_nb001_menu_item (face_id, parent_face_navbar_nb001_menu_item_id, "order");

ALTER TABLE face_navbar_nb001_menu_item
    ALTER COLUMN target_page_face_id DROP NOT NULL;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2729, 27, '', '', '', false)
    ON CONFLICT DO NOTHING;

