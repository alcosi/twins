ALTER TABLE projection
DROP CONSTRAINT IF EXISTS projection_fields_unique;

ALTER TABLE projection
    ADD CONSTRAINT projection_fields_unique
        UNIQUE (src_twin_class_field_id, dst_twin_class_id, dst_twin_class_field_id, projection_type_id);