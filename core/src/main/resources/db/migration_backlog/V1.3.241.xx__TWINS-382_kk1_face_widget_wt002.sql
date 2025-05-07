CREATE TABLE IF NOT EXISTS face_widget_wt002
(
    face_id          UUID PRIMARY KEY REFERENCES face (id),
    key              VARCHAR NOT NULL,
    style_attributes HSTORE
);

CREATE TABLE IF NOT EXISTS face_widget_wt002_button
(
    id                              UUID PRIMARY KEY,
    face_id                         UUID    NOT NULL REFERENCES face (id),
    key                             VARCHAR NOT NULL,
    label_i18n_id                   UUID REFERENCES i18n (id),
    icon_resource_id                UUID REFERENCES resource (id),
    style_attributes                HSTORE,
    extends_hierarchy_twin_class_id UUID REFERENCES twin_class (id),
    extends_hierarchy_depth         INTEGER
);