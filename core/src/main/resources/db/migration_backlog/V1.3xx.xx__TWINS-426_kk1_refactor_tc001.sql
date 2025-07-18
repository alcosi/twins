DROP TABLE IF EXISTS face_tc001;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_name = 'face_tc002'
        ) THEN
            EXECUTE 'ALTER TABLE face_tc002 RENAME TO face_tc001';
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_name = 'face_tc002_option'
        ) THEN
            EXECUTE 'ALTER TABLE face_tc002_option RENAME TO face_tc001_option';
        END IF;
    END;
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'face_tc001_option'
        AND column_name = 'face_tc002_id'
    ) THEN
        EXECUTE 'ALTER TABLE face_tc001_option RENAME COLUMN face_tc002_id TO face_tc001_id';
    END IF;
END $$;


ALTER TABLE face_tc001_option
    DROP COLUMN IF EXISTS twin_class_id,
    DROP COLUMN IF EXISTS extends_depth;

ALTER TABLE face_tc001
    ADD COLUMN IF NOT EXISTS option_select_i18n_id UUID REFERENCES i18n(id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD COLUMN IF NOT EXISTS sketch_mode BOOLEAN,
    ADD COLUMN IF NOT EXISTS single_option_silent_mode BOOLEAN;

ALTER TABLE face_tc001_option
    ADD COLUMN IF NOT EXISTS twin_class_search_id UUID;