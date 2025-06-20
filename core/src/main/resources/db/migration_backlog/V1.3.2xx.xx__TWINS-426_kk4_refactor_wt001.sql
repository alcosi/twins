ALTER TABLE face_widget_wt001
    ADD COLUMN IF NOT EXISTS modal_face_id UUID REFERENCES face(id);

CREATE INDEX IF NOT EXISTS idx_face_widget_wt001_modal_face_id
    ON face_widget_wt001 (modal_face_id);