CREATE TABLE IF NOT EXISTS twin_class_dynamic_marker (
    id UUID PRIMARY KEY,
    twin_class_id UUID NOT NULL REFERENCES twin_class(id),
    twin_validator_set_id UUID NOT NULL REFERENCES twin_validator_set(id),
    marker_data_list_option_id UUID NOT NULL REFERENCES data_list_option(id)
);

CREATE INDEX IF NOT EXISTS idx_twin_class_dynamic_marker_twin_class_id
    ON twin_class_dynamic_marker(twin_class_id);

CREATE INDEX IF NOT EXISTS idx_twin_class_dynamic_marker_twin_validator_set_id
    ON twin_class_dynamic_marker(twin_validator_set_id);

CREATE INDEX IF NOT EXISTS idx_twin_class_dynamic_marker_marker_data_list_option_id
    ON twin_class_dynamic_marker(marker_data_list_option_id);

ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS has_dynamic_markers BOOLEAN NOT NULL DEFAULT FALSE;


CREATE OR REPLACE FUNCTION update_twin_class_has_dynamic_markers()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
UPDATE twin_class
SET has_dynamic_markers = TRUE
WHERE id = NEW.twin_class_id;
RETURN NEW;

ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.twin_class_id != NEW.twin_class_id THEN
UPDATE twin_class
SET has_dynamic_markers = EXISTS (
    SELECT 1 FROM twin_class_dynamic_marker
    WHERE twin_class_id = OLD.twin_class_id
)
WHERE id = OLD.twin_class_id;

UPDATE twin_class
SET has_dynamic_markers = TRUE
WHERE id = NEW.twin_class_id;
END IF;
RETURN NEW;

ELSIF TG_OP = 'DELETE' THEN
UPDATE twin_class
SET has_dynamic_markers = EXISTS (
    SELECT 1 FROM twin_class_dynamic_marker
    WHERE twin_class_id = OLD.twin_class_id
)
WHERE id = OLD.twin_class_id;
RETURN OLD;
END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER twin_class_dynamic_marker_after_insert
    AFTER INSERT ON twin_class_dynamic_marker
    FOR EACH ROW
    EXECUTE FUNCTION update_twin_class_has_dynamic_markers();

CREATE TRIGGER twin_class_dynamic_marker_after_update
    AFTER UPDATE ON twin_class_dynamic_marker
    FOR EACH ROW
    EXECUTE FUNCTION update_twin_class_has_dynamic_markers();

CREATE TRIGGER twin_class_dynamic_marker_after_delete
    AFTER DELETE ON twin_class_dynamic_marker
    FOR EACH ROW
    EXECUTE FUNCTION update_twin_class_has_dynamic_markers();
