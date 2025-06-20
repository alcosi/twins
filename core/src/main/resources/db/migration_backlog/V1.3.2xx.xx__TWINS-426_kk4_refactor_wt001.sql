ALTER TABLE face_widget_wt001
    ADD COLUMN IF NOT EXISTS modal_face_id UUID;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'face_widget_modal_face_id_fk'
                         AND conrelid = 'face_widget_wt001'::regclass) THEN
            ALTER TABLE face_widget_wt001
                ADD CONSTRAINT face_widget_modal_face_id_fk
                    FOREIGN KEY (modal_face_id)
                        REFERENCES face (id);
        END IF;
    END;
$$;

CREATE INDEX IF NOT EXISTS idx_face_widget_wt001_modal_face_id
    ON face_widget_wt001 (modal_face_id);