CREATE OR REPLACE FUNCTION twin_class_field_rule_map_after_insert_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    PERFORM twin_class_field_is_dependent_field_check(NEW.twin_class_field_id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_rule_map_after_update_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    PERFORM twin_class_field_is_dependent_field_check(NEW.twin_class_field_id);
    PERFORM twin_class_field_is_dependent_field_check(OLD.twin_class_field_id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_field_rule_map_after_delete_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    PERFORM twin_class_field_is_dependent_field_check(OLD.twin_class_field_id);
RETURN OLD;
END;
$$ LANGUAGE plpgsql;

drop function if exists public.twin_class_field_rule_map_after_change();