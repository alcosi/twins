CREATE TABLE IF NOT EXISTS face_widget_wt003 (
    face_id UUID PRIMARY KEY REFERENCES face (id) ON DELETE CASCADE ON UPDATE CASCADE,
    level VARCHAR NOT NULL,
    message_i18n_id UUID REFERENCES i18n (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    icon_resource_id UUID REFERENCES resource (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    style_classes VARCHAR
);

CREATE INDEX IF NOT EXISTS idx_face_widget_wt003_message_i18n_id ON face_widget_wt003 (message_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_widget_wt003_icon_resource_id ON face_widget_wt003 (icon_resource_id);