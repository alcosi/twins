CREATE TABLE IF NOT EXISTS twin_class_field_attribute (
    id UUID PRIMARY KEY,
    twin_class_id UUID REFERENCES twin_class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    key VARCHAR NOT NULL,
    note_msg_i18n_id UUID REFERENCES i18n(id) ON DELETE SET NULL ON UPDATE CASCADE,
    create_permission_id UUID REFERENCES permission(id) ON DELETE SET NULL ON UPDATE CASCADE,
    update_permission_id UUID REFERENCES permission(id) ON DELETE SET NULL ON UPDATE CASCADE,
    delete_permission_id UUID REFERENCES permission(id) ON DELETE SET NULL ON UPDATE CASCADE,
    uniq BOOLEAN DEFAULT FALSE,
    CONSTRAINT twin_class_field_attribute_class_key UNIQUE (twin_class_id, key)
);

ALTER TABLE IF EXISTS projection_exclusion RENAME TO twin_field_attribute;

ALTER TABLE IF EXISTS twin_field_attribute
    RENAME CONSTRAINT projection_exclusion_twin_class_field_id_fkey TO twin_field_attribute_twin_class_field_id_fkey;

ALTER TABLE IF EXISTS twin_field_attribute
    RENAME CONSTRAINT projection_exclusion_twin_id_fkey TO twin_field_attribute_twin_id_fkey;

ALTER TABLE twin_field_attribute
    ADD COLUMN IF NOT EXISTS twin_class_field_attribute_id UUID REFERENCES twin_class_field_attribute(id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL,
    ADD COLUMN IF NOT EXISTS note_msg TEXT,
    ADD COLUMN IF NOT EXISTS note_msg_context HSTORE,
    ADD COLUMN IF NOT EXISTS changed_at TIMESTAMP;

ALTER TABLE twin_field_attribute
    ADD CONSTRAINT fk_twin_field_attribute_class_attr
        FOREIGN KEY (twin_class_field_attribute_id) REFERENCES twin_class_field_attribute(id);

CREATE INDEX IF NOT EXISTS twin_field_attribute_twin_class_field_idx ON twin_field_attribute (twin_id, twin_class_field_id);
CREATE INDEX IF NOT EXISTS twin_field_attribute_attribute_id_idx ON twin_field_attribute (twin_class_field_attribute_id);


