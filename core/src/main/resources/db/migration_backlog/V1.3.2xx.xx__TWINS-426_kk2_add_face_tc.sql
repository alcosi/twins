CREATE TABLE IF NOT EXISTS face_tc001
(
    face_id                  UUID    NOT NULL PRIMARY KEY REFERENCES face
    ON UPDATE CASCADE ON DELETE CASCADE,
    key                      VARCHAR NOT NULL,
    class_selector_label_i18n_id           UUID    REFERENCES i18n
        ON UPDATE CASCADE ON DELETE RESTRICT,
    save_button_label_i18n_id           UUID    REFERENCES i18n
        ON UPDATE CASCADE ON DELETE RESTRICT,
    header_i18n_id           UUID    REFERENCES i18n
    ON UPDATE CASCADE ON DELETE RESTRICT,
    header_icon_resource_id  UUID    REFERENCES resource
    ON UPDATE CASCADE ON DELETE RESTRICT,
    style_classes            VARCHAR,
    twin_class_id            UUID    NOT NULL REFERENCES twin_class
    ON UPDATE CASCADE ON DELETE CASCADE,
    extends_depth            INTEGER DEFAULT 0 NOT NULL,
    head_pointer_featurer_id INTEGER REFERENCES featurer,
    head_pointer_params      HSTORE,
    field_finder_featurer_id INTEGER REFERENCES featurer NOT NULL,
    field_finder_params      HSTORE
);

CREATE INDEX IF NOT EXISTS idx_face_tc001_face_id
    ON face_tc001 (face_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_class_selector_label_i18n_id
    ON face_tc001 (class_selector_label_i18n_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_save_button_label_i18n_id
    ON face_tc001 (save_button_label_i18n_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_header_i18n_id
    ON face_tc001 (header_i18n_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_header_icon_resource_id
    ON face_tc001 (header_icon_resource_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_twin_class_id
    ON face_tc001 (twin_class_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_head_pointer_featurer_id
    ON face_tc001 (head_pointer_featurer_id);

CREATE INDEX IF NOT EXISTS idx_face_tc001_field_finder_featurer_id
    ON face_tc001 (field_finder_featurer_id);
