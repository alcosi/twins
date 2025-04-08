ALTER TABLE face_navbar_nb001_menu_items ADD COLUMN IF NOT EXISTS permission_id UUID;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_menu_items_permission'
        ) THEN
            ALTER TABLE face_navbar_nb001_menu_items
                ADD CONSTRAINT fk_menu_items_permission
                    FOREIGN KEY (permission_id) REFERENCES permission(id);
        END IF;
    END $$;