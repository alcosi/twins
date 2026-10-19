-- Insert CREATE action for twin_class_field.
-- Separates field editability validation rules between create and update operations: in code both go
-- through the editability path (TwinField.editable), but the action validator is selected per twin —
-- CREATE when TwinEntity.createElseUpdate is true, EDIT otherwise (previous behaviour).
INSERT INTO twin_class_field_action (id)
VALUES ('CREATE')
ON CONFLICT (id) DO NOTHING;
