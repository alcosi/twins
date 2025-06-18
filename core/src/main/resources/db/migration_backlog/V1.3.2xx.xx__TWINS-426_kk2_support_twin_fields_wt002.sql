ALTER TABLE face_widget_wt002_button
    ADD COLUMN IF NOT EXISTS head_pointer_featurer_id INTEGER,
    ADD COLUMN IF NOT EXISTS head_pointer_params HSTORE,
    ADD COLUMN IF NOT EXISTS field_finder_featurer_id INTEGER,
    ADD COLUMN IF NOT EXISTS field_finder_params HSTORE;

ALTER TABLE face_widget_wt002_button
    ADD CONSTRAINT fk_face_widget_wt002_button_head_pointer_featurer
        FOREIGN KEY (head_pointer_featurer_id)
            REFERENCES featurer(id);

ALTER TABLE face_widget_wt002_button
    ADD CONSTRAINT fk_face_widget_wt002_button_field_finder_featurer
        FOREIGN KEY (field_finder_featurer_id)
            REFERENCES featurer(id);