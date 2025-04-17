CREATE TABLE IF NOT EXISTS face_widget_wt001_column
(
    id                  UUID PRIMARY KEY,
    face_id             UUID    NOT NULL,
    twin_class_field_id UUID    NOT NULL,
    "order"             INTEGER NOT NULL,
    label_i18n_id       UUID,
    show_by_default     BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_face_widget_wt001_column_face
    FOREIGN KEY (face_id) REFERENCES face (id),
    CONSTRAINT fk_face_widget_wt001_column_twin_class_field
    FOREIGN KEY (twin_class_field_id) REFERENCES twin_class_field (id),
    CONSTRAINT fk_face_widget_wt001_column_label_i18n
    FOREIGN KEY (label_i18n_id) REFERENCES i18n (id)
    );

CREATE INDEX IF NOT EXISTS idx_face_widget_wt001_column_face_id
    ON face_widget_wt001_column (face_id);

CREATE INDEX IF NOT EXISTS idx_face_widget_wt001_column_twin_class_field_id
    ON face_widget_wt001_column (twin_class_field_id);

CREATE INDEX IF NOT EXISTS idx_face_widget_wt001_column_label_i18n_id
    ON face_widget_wt001_column (label_i18n_id);

ALTER TABLE face_widget_wt001
DROP COLUMN IF EXISTS show_columns;