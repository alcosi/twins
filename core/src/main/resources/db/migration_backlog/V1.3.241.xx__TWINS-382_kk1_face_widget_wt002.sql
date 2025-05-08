CREATE TABLE IF NOT EXISTS face_widget_wt002
(
    face_id          UUID PRIMARY KEY REFERENCES face (id) ON DELETE CASCADE ON UPDATE CASCADE,
    key              VARCHAR NOT NULL,
    style_classes    VARCHAR
    );

CREATE TABLE IF NOT EXISTS face_widget_wt002_button
(
    id                              UUID PRIMARY KEY,
    face_id                         UUID NOT NULL REFERENCES face (id) ON DELETE CASCADE ON UPDATE CASCADE,
    key                             VARCHAR NOT NULL,
    label_i18n_id                   UUID REFERENCES i18n (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    icon_resource_id                UUID REFERENCES resource (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    style_classes                   VARCHAR,
    extends_hierarchy_twin_class_id UUID NOT NULL REFERENCES twin_class (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    extends_hierarchy_depth         INTEGER NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_face_widget_wt002_button_face_id ON face_widget_wt002_button (face_id);
CREATE INDEX IF NOT EXISTS idx_face_widget_wt002_button_label_i18n_id ON face_widget_wt002_button (label_i18n_id);
CREATE INDEX IF NOT EXISTS idx_face_widget_wt002_button_icon_resource_id ON face_widget_wt002_button (icon_resource_id);
CREATE INDEX IF NOT EXISTS idx_face_widget_wt002_button_twin_class_id ON face_widget_wt002_button (extends_hierarchy_twin_class_id);