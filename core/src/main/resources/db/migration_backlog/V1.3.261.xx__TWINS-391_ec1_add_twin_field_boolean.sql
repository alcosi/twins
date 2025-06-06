CREATE TABLE IF NOT EXISTS twin_field_boolean
(
    id                  UUID PRIMARY KEY,
    twin_id             UUID    NOT NULL REFERENCES twin (id) ON UPDATE CASCADE ON DELETE CASCADE,
    twin_class_field_id UUID    NOT NULL REFERENCES twin_class_field (id) ON UPDATE CASCADE ON DELETE CASCADE,
    value               BOOLEAN
);

CREATE INDEX IF NOT EXISTS idx_twin_field_boolean_field_value ON twin_field_boolean (twin_class_field_id, value);
CREATE INDEX IF NOT EXISTS idx_twin_field_boolean_field_value ON twin_field_boolean (twin_class_field_id, twin_id);
