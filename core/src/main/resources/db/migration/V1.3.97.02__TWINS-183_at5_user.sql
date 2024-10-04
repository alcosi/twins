-- fix bug with twin.name is null in file V1.3.96.03__TWINS-167_at2_twin_class_field.sql
DROP FUNCTION IF EXISTS user_insert_or_update() CASCADE;

CREATE OR REPLACE FUNCTION user_insert_or_update()
    RETURNS TRIGGER AS $$
BEGIN
    -- Inserting a new record into twin when adding a record to user
    IF TG_OP = 'INSERT' THEN
        INSERT INTO twin (id, twin_class_id, twin_status_id, name, created_by_user_id, assigner_user_id, created_at)
        VALUES (
                   NEW.id,
                   '00000000-0000-0000-0001-000000000001',
                   '00000000-0000-0000-0003-000000000001',
                   COALESCE(NEW.name, ''),
                   '00000000-0000-0000-0000-000000000000',
                   NEW.id,
                   NEW.created_at
               )
        ON CONFLICT (id) DO NOTHING;

        -- Updating name field in twin when updating a record in user
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE twin
        SET name = COALESCE(NEW.name, '')
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_user_insert_or_update
    AFTER INSERT OR UPDATE ON "user"
    FOR EACH ROW
EXECUTE FUNCTION user_insert_or_update();
