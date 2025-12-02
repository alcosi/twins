alter table public.twin_class_field_condition
    alter column base_twin_class_field_id drop not null,
    alter column condition_evaluator_featurer_id drop not null;


CREATE OR REPLACE FUNCTION validate_twin_class_field_condition()
RETURNS TRIGGER AS $$
BEGIN
IF NEW.logic_operator_id = 'LEAF' THEN
    IF NEW.base_twin_class_field_id IS NULL THEN
            RAISE EXCEPTION 'base_twin_class_field_id is required when logic_operator_id = LEAF';
    END IF;

    IF NEW.condition_evaluator_featurer_id IS NULL THEN
            RAISE EXCEPTION 'condition_evaluator_featurer_id is required when logic_operator_id = LEAF';
    END IF;
ELSE
    IF NEW.base_twin_class_field_id IS NOT NULL THEN
            RAISE EXCEPTION 'base_twin_class_field_id must be NULL when logic_operator_id != LEAF';
    END IF;

    IF NEW.condition_evaluator_featurer_id IS NOT NULL THEN
            RAISE EXCEPTION 'condition_evaluator_featurer_id must be NULL when logic_operator_id != LEAF';
    END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_twin_class_field_condition_insert
    BEFORE INSERT ON twin_class_field_condition
    FOR EACH ROW
    EXECUTE FUNCTION validate_twin_class_field_condition();

CREATE TRIGGER validate_twin_class_field_condition_update
    BEFORE UPDATE ON twin_class_field_condition
    FOR EACH ROW
    EXECUTE FUNCTION validate_twin_class_field_condition();