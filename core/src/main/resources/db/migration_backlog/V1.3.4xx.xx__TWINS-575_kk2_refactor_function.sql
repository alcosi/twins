CREATE OR REPLACE FUNCTION twin_class_field_is_dependent_field_check(field_id uuid)
    RETURNS void
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF field_id IS NOT NULL THEN
        UPDATE twin_class_field
        SET dependent_field = EXISTS (SELECT 1
                                      FROM twin_class_field_rule_map map
                                      WHERE map.twin_class_field_id = field_id)
        WHERE id = field_id;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION twin_class_field_rule_map_after_change()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        PERFORM twin_class_field_is_dependent_field_check(NEW.twin_class_field_id);
    END IF;

    IF (TG_OP = 'DELETE' OR TG_OP = 'UPDATE') THEN
        PERFORM twin_class_field_is_dependent_field_check(OLD.twin_class_field_id);
    END IF;

    RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE TRIGGER twin_class_field_rule_map_after_insert
    AFTER INSERT
    ON twin_class_field_rule_map
    FOR EACH ROW
EXECUTE FUNCTION twin_class_field_rule_map_after_change();

CREATE TRIGGER twin_class_field_rule_map_after_update
    AFTER UPDATE
    ON twin_class_field_rule_map
    FOR EACH ROW
EXECUTE FUNCTION twin_class_field_rule_map_after_change();

CREATE TRIGGER twin_class_field_rule_map_after_delete
    AFTER DELETE
    ON twin_class_field_rule_map
    FOR EACH ROW
EXECUTE FUNCTION twin_class_field_rule_map_after_change();

DROP FUNCTION twin_class_field_rule_after_insert_wrapper() CASCADE;
DROP FUNCTION twin_class_field_rule_after_update_wrapper() CASCADE;
DROP FUNCTION twin_class_field_rule_after_delete_wrapper() CASCADE;