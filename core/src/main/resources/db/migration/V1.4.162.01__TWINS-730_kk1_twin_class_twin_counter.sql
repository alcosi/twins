-- TWINS-730: twin_class.twin_counter column and triggers

-- Add twin_counter column to twin_class table
ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS twin_counter int NOT NULL DEFAULT 0;

-- Update twin counter for one twin_class
CREATE OR REPLACE FUNCTION update_twin_class_twin_counter(p_twin_class_id UUID)
    RETURNS VOID AS
$$
DECLARE
    v_twin_count INT;
BEGIN
    IF p_twin_class_id IS NULL THEN
        RETURN;
    END IF;

    SELECT COUNT(*)
    INTO v_twin_count
    FROM twin
    WHERE twin_class_id = p_twin_class_id;

    UPDATE twin_class
    SET twin_counter = COALESCE(v_twin_count, 0)
    WHERE id = p_twin_class_id;
END;
$$ LANGUAGE plpgsql;

-- Initialize all twin_class twin counters
CREATE OR REPLACE FUNCTION initialize_twin_class_twin_counters()
    RETURNS VOID AS
$$
BEGIN
    WITH twin_counts AS (
        SELECT twin_class_id, COUNT(*) AS cnt
        FROM twin
        WHERE twin_class_id IS NOT NULL
        GROUP BY twin_class_id
    )
    UPDATE twin_class tc
    SET twin_counter = twin_counts.cnt
    FROM twin_counts
    WHERE tc.id = twin_counts.twin_class_id;
END;
$$ LANGUAGE plpgsql;

-- Update twin_after_insert_wrapper: add twin_class counter update
CREATE OR REPLACE FUNCTION twin_after_insert_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    RAISE NOTICE 'Process insert for: %', new.id;
    PERFORM hierarchyUpdateTreeHard(new.id, hierarchyDetectTree(new.id));

    IF NEW.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
    END IF;

    -- Update twin_class twin counter
    IF NEW.twin_class_id IS NOT NULL THEN
        PERFORM update_twin_class_twin_counter(NEW.twin_class_id);
    END IF;

    RETURN NEW;
END;
$$;

-- Update twin_after_update_wrapper: add twin_class counter update when twin_class_id changes
CREATE OR REPLACE FUNCTION twin_after_update_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
        RAISE NOTICE 'Process update for: %', new.id;
        PERFORM hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));

        IF OLD.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
        END IF;
        IF NEW.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
        END IF;
    END IF;

    -- Update twin_class twin counters if twin_class_id changed
    IF OLD.twin_class_id IS DISTINCT FROM NEW.twin_class_id THEN
        IF OLD.twin_class_id IS NOT NULL THEN
            PERFORM update_twin_class_twin_counter(OLD.twin_class_id);
        END IF;
        IF NEW.twin_class_id IS NOT NULL THEN
            PERFORM update_twin_class_twin_counter(NEW.twin_class_id);
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

-- Update twin_after_delete_wrapper: add twin_class counter update
CREATE OR REPLACE FUNCTION twin_after_delete_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF OLD.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
    END IF;

    -- Update twin_class twin counter
    IF OLD.twin_class_id IS NOT NULL THEN
        PERFORM update_twin_class_twin_counter(OLD.twin_class_id);
    END IF;

    RETURN OLD;
END;
$$;

-- Backfill existing data
SELECT initialize_twin_class_twin_counters();
