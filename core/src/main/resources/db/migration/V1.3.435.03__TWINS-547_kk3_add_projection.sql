CREATE TABLE IF NOT EXISTS projection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    src_twin_pointer_id UUID NOT NULL REFERENCES twin_pointer(id),
    src_twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id),
    dst_twin_class_id UUID NOT NULL REFERENCES twin_class(id),
    dst_twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id),
    field_projector_featurer_id INTEGER NOT NULL REFERENCES featurer(id),
    field_projector_params HSTORE
);

CREATE INDEX IF NOT EXISTS projection_src_twin_pointer_id_idx ON projection(src_twin_pointer_id);
CREATE INDEX IF NOT EXISTS projection_src_twin_class_field_id_idx ON projection(src_twin_class_field_id);
CREATE INDEX IF NOT EXISTS projection_dst_twin_class_id_idx ON projection(dst_twin_class_id);
CREATE INDEX IF NOT EXISTS projection_dst_twin_class_field_id_idx ON projection(dst_twin_class_field_id);
CREATE INDEX IF NOT EXISTS projection_field_projector_featurer_id_idx ON projection(field_projector_featurer_id);