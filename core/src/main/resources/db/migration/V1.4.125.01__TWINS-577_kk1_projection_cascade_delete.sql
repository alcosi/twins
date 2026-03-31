ALTER TABLE projection
DROP CONSTRAINT IF EXISTS projection_src_twin_class_field_id_fkey;

ALTER TABLE projection
    ADD CONSTRAINT projection_src_twin_class_field_id_fkey
        FOREIGN KEY (src_twin_class_field_id)
            REFERENCES twin_class_field(id)
            ON DELETE CASCADE;

ALTER TABLE projection
DROP CONSTRAINT IF EXISTS projection_dst_twin_class_field_id_fkey;

ALTER TABLE projection
    ADD CONSTRAINT projection_dst_twin_class_field_id_fkey
        FOREIGN KEY (dst_twin_class_field_id)
            REFERENCES twin_class_field(id)
            ON DELETE CASCADE;

ALTER TABLE projection
DROP CONSTRAINT IF EXISTS projection_dst_twin_class_id_fkey;

ALTER TABLE projection
    ADD CONSTRAINT projection_dst_twin_class_id_fkey
        FOREIGN KEY (dst_twin_class_id)
            REFERENCES twin_class(id)
            ON DELETE CASCADE;

ALTER TABLE twin_class_field
DROP CONSTRAINT IF EXISTS twin_class_field_twin_class_id_fkey;

ALTER TABLE twin_class_field
    ADD CONSTRAINT twin_class_field_twin_class_id_fkey
        FOREIGN KEY (twin_class_id)
            REFERENCES twin_class(id)
            ON DELETE CASCADE;