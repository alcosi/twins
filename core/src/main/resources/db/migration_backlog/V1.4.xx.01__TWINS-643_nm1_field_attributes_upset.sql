CREATE OR REPLACE FUNCTION twin_field_attribute_upsert_trigger()
    RETURNS TRIGGER AS $$
DECLARE
    is_uniq boolean;
    existing_id uuid;
BEGIN
    -- Fetch the uniq flag from the related twin_class_field_attribute
    SELECT uniq INTO is_uniq
    FROM twin_class_field_attribute
    WHERE id = NEW.twin_class_field_attribute_id;

    -- If uniq is false or null, proceed with normal insert
    IF is_uniq IS NULL OR NOT is_uniq THEN
        RETURN NEW;
    END IF;

    -- Acquire an advisory lock based on the unique combination to handle concurrency
    PERFORM pg_advisory_xact_lock(hashtext(concat(NEW.twin_id::text, NEW.twin_class_field_id::text, NEW.twin_class_field_attribute_id::text))::bigint);

    -- Check for existing record with the same unique combination
    SELECT id INTO existing_id
    FROM twin_field_attribute
    WHERE twin_id = NEW.twin_id
      AND twin_class_field_id = NEW.twin_class_field_id
      AND twin_class_field_attribute_id = NEW.twin_class_field_attribute_id
    LIMIT 1;

    -- If no existing record, proceed with insert
    IF existing_id IS NULL THEN
        RETURN NEW;
    END IF;

    -- Update the existing record
    UPDATE twin_field_attribute
    SET note_msg = NEW.note_msg,
        note_msg_context = NEW.note_msg_context,
        changed_at = CURRENT_TIMESTAMP
    WHERE id = existing_id;

    -- Skip the insert
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER twin_field_attribute_insert_trigger
    BEFORE INSERT ON twin_field_attribute
    FOR EACH ROW
EXECUTE FUNCTION twin_field_attribute_upsert_trigger();
