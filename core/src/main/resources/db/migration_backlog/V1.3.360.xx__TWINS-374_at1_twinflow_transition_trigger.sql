CREATE OR REPLACE FUNCTION transition_trigger_set_order()
    RETURNS TRIGGER AS $$
BEGIN
SELECT INTO NEW."order" COALESCE(MAX("order"), 0) + 1
FROM twinflow_transition_trigger
WHERE twinflow_transition_id = NEW.twinflow_transition_id;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER before_insert_order
    BEFORE INSERT ON twinflow_transition_trigger
    FOR EACH ROW
    EXECUTE FUNCTION transition_trigger_set_order();
