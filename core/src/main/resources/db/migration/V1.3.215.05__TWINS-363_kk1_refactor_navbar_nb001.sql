DO $$
    BEGIN
        IF EXISTS (SELECT FROM information_schema.tables
                   WHERE table_name = 'face_navbar_nb001_menu_items')
            AND NOT EXISTS (SELECT FROM information_schema.tables
                            WHERE table_name = 'face_navbar_nb001_menu_item') THEN
            EXECUTE 'ALTER TABLE face_navbar_nb001_menu_items RENAME TO face_navbar_nb001_menu_item';
        END IF;
    END $$;


DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM information_schema.columns
                       WHERE table_name = 'face_navbar_nb001_menu_item'
                         AND column_name = 'parent_face_navbar_nb001_menu_item_id') THEN
            EXECUTE 'ALTER TABLE face_navbar_nb001_menu_item ADD COLUMN parent_face_navbar_nb001_menu_item_id UUID NULL';
        END IF;
    END $$;


DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM information_schema.table_constraints
                       WHERE constraint_name = 'fk_menu_item_parent'
                         AND table_name = 'face_navbar_nb001_menu_item') THEN
            EXECUTE 'ALTER TABLE face_navbar_nb001_menu_item
                ADD CONSTRAINT fk_menu_item_parent
                FOREIGN KEY (parent_face_navbar_nb001_menu_item_id)
                REFERENCES face_navbar_nb001_menu_item(id)';
        END IF;
    END $$;


DO $$
    BEGIN
        IF EXISTS (SELECT FROM information_schema.tables
                   WHERE table_name = 'face_navbar_nb001_status')
            AND NOT EXISTS (SELECT FROM information_schema.tables
                            WHERE table_name = 'face_navbar_nb001_menu_item_status') THEN
            EXECUTE 'ALTER TABLE face_navbar_nb001_status RENAME TO face_navbar_nb001_menu_item_status';
        END IF;
    END $$;