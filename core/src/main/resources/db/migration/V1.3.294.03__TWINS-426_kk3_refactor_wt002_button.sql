INSERT INTO public.face_component (id, face_component_type_id, name, description)
VALUES ('WT002', 'WIDGET', 'Create twin button widget', null)
on conflict (id) do nothing;


ALTER TABLE face_widget_wt002_button
    DROP COLUMN IF EXISTS twin_class_id,
    DROP COLUMN IF EXISTS extends_depth;


ALTER TABLE face_widget_wt002_button
    ADD COLUMN IF NOT EXISTS modal_face_id UUID;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'fk_face_widget_modal_face'
                         AND conrelid = 'face_widget_wt002_button'::regclass) THEN
            ALTER TABLE face_widget_wt002_button
                ADD CONSTRAINT fk_face_widget_modal_face
                    FOREIGN KEY (modal_face_id)
                        REFERENCES face (id);
        END IF;
    END;
$$;

CREATE INDEX IF NOT EXISTS idx_face_widget_wt002_button_modal_face_id
    ON face_widget_wt002_button (modal_face_id);