CREATE TABLE IF NOT EXISTS twin_field_timestamp
(
    id                  UUID PRIMARY KEY,
    twin_id             UUID NOT NULL REFERENCES twin (id) ON UPDATE CASCADE ON DELETE CASCADE,
    twin_class_field_id UUID NOT NULL REFERENCES twin_class_field (id) ON UPDATE CASCADE ON DELETE CASCADE,
    value               TIMESTAMP
);

CREATE INDEX IF NOT EXISTS twin_field_timestamp_twin_class_field_id_value_index
    ON twin_field_timestamp (twin_class_field_id, value);
CREATE UNIQUE INDEX IF NOT EXISTS twin_field_timestamp_twin_class_field_id_twin_id_uindex
    ON twin_field_timestamp (twin_id, twin_class_field_id);

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1349, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperTimestamp','Timestamp', 'Timestamp field with dedicated table storage', false)
ON CONFLICT DO NOTHING;
