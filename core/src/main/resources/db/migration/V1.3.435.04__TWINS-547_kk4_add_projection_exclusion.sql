CREATE TABLE IF NOT EXISTS projection_exclusion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    twin_id UUID NOT NULL REFERENCES twin(id),
    twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id)
    );

CREATE INDEX IF NOT EXISTS projection_exclusion_twin_id_idx ON projection_exclusion(twin_id);
CREATE INDEX IF NOT EXISTS projection_exclusion_twin_class_field_id_idx ON projection_exclusion(twin_class_field_id);