
alter table face_page_pg001
    add column if not exists style_classes varchar(255);
alter table face_page_pg001
    drop column if exists face_page_pg001_layout_id;
drop table if exists face_page_pg001_layout;
alter table face_page_pg001_widget
    add column if not exists style_classes varchar(255);

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_page_pg001_widget'
              AND column_name = 'row'
        ) AND EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_page_pg001_widget'
              AND column_name = 'column'
        ) THEN
            EXECUTE $sql$
            UPDATE face_page_pg001_widget
            SET style_classes =
                COALESCE(style_classes || ' ', '') ||
                'deprecated-row-index-' || row || ' ' ||
                'deprecated-column-index-' || "column";
        $sql$;
        END IF;
    END
$$;

alter table face_page_pg001_widget
    drop column if exists row;
alter table face_page_pg001_widget
    drop column if exists "column";

alter table face_page_pg002
    add column if not exists style_classes varchar(255);
alter table face_page_pg002_tab
    add column if not exists style_classes varchar(255);
alter table face_page_pg002_tab
    drop column if exists face_page_pg002_tab_layout_id;
drop table if exists face_page_pg002_tab_layout;
alter table face_page_pg002_widget
    add column if not exists style_classes varchar(255);
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_page_pg002_widget'
              AND column_name = 'row'
        ) AND EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_page_pg002_widget'
              AND column_name = 'column'
        ) THEN
            EXECUTE $sql$
            UPDATE face_page_pg002_widget
            SET style_classes =
                COALESCE(style_classes || ' ', '') ||
                'deprecated-row-index-' || row || ' ' ||
                'deprecated-column-index-' || "column";
        $sql$;
        END IF;
    END
$$;
alter table face_page_pg002_widget
    drop column if exists row;
alter table face_page_pg002_widget
    drop column if exists "column";

