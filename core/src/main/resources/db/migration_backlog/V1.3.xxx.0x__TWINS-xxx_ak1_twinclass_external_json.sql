ALTER TABLE twin_class ADD COLUMN external_json JSONB;
-- CREATE INDEX idx_twin_class_field_external_json ON twin_class USING GIN (external_json);
-- CREATE INDEX idx_twin_class_field_external_json_keys ON twin_class USING GIN (external_json jsonb_path_ops);
