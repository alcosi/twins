alter table twin_class add if not exists head_hierarchy_tree ltree;
alter table twin_class add if not exists extends_hierarchy_tree ltree;

drop table if exists public.twin_class_child_map;
drop table if exists public.twin_class_extends_map;

DROP TRIGGER IF EXISTS hierarchy_twin_class_head_update_tree_trigger ON public.twin_class;
DROP TRIGGER IF EXISTS hierarchy_twin_class_extends_update_tree_trigger ON public.twin_class;

DROP FUNCTION IF EXISTS public.hierarchy_twin_class_head_detect_tree(UUID);
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_head_update_tree_hard(UUID, TEXT);
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_head_update_tree_soft(UUID, TEXT);
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_head_process_tree_update();
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_extends_detect_tree(UUID);
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_extends_update_tree_hard(UUID, TEXT);
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_extends_update_tree_soft(UUID, TEXT);
DROP FUNCTION IF EXISTS public.hierarchy_twin_class_extends_process_tree_update();



--
-- EXTENDS SECTION
--
CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_extends_detect_tree(twin_class_id UUID)
    RETURNS TEXT AS $$
DECLARE
    hierarchy                             TEXT   := '';
    current_id                            UUID   := twin_class_id;
    parent_id                             UUID;
    visited_ids                           UUID[] := ARRAY [twin_class_id];
BEGIN
    RAISE NOTICE 'Detected extends hier. for twin class id: %', twin_class_id;
    -- return value init
    hierarchy := '';

    -- cycle for build hierarchy form twin-in to twin-root
    LOOP
        -- get parent_id and shema flags and check space_schema_id is present for twin-in
        SELECT tc.extends_twin_class_id INTO parent_id
        FROM public.twin_class tc WHERE tc.id = current_id;

        -- check for cycle
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %', twin_class_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN hierarchy;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_extends_update_tree_hard(twin_class_id UUID, detected_hierarchy TEXT)
    RETURNS VOID AS $$
DECLARE
    hierarchy_to_use TEXT;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_extends_detect_tree(twin_class_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', hierarchy_to_use;

    -- update hier. and schemas for twin-class-in
    UPDATE public.twin_class SET extends_hierarchy_tree = text2ltree(hierarchy_to_use) WHERE id = twin_class_id;

    -- update hier. and schemas for twin-class-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin_class
        WHERE extends_twin_class_id = twin_class_id
        UNION ALL
        SELECT tc.id, d.depth + 1
        FROM public.twin_class tc INNER JOIN descendants d ON tc.extends_twin_class_id = d.id
        WHERE d.depth < 10
    )
    UPDATE public.twin_class tc
    SET extends_hierarchy_tree = text2ltree(public.hierarchy_twin_class_extends_detect_tree(tc.id))
    WHERE tc.id IN (SELECT id FROM descendants);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_extends_update_tree_soft(twin_class_id UUID, detected_hierarchy TEXT)
    RETURNS VOID AS $$
DECLARE
    hierarchy_to_use TEXT;
    old_hierarchy TEXT;
    new_hierarchy TEXT;
BEGIN
    SELECT extends_hierarchy_tree::text INTO old_hierarchy FROM public.twin_class WHERE id = twin_class_id;

    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_extends_detect_tree(twin_class_id);
    END IF;

    new_hierarchy := hierarchy_to_use;

    RAISE NOTICE 'NEW: %', new_hierarchy;
    RAISE NOTICE 'OLD: %', old_hierarchy;

    UPDATE public.twin_class
    SET extends_hierarchy_tree = text2ltree(replace(twin_class.extends_hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE extends_hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_extends_process_tree_update()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.extends_twin_class_id IS DISTINCT FROM NEW.extends_twin_class_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.extends_twin_class_id IS DISTINCT FROM NEW.extends_twin_class_id THEN
            PERFORM public.hierarchy_twin_class_extends_update_tree_soft(NEW.id, public.hierarchy_twin_class_extends_detect_tree(NEW.id));
        ELSE
            PERFORM public.hierarchy_twin_class_extends_update_tree_soft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.hierarchy_twin_class_extends_update_tree_hard(NEW.id, public.hierarchy_twin_class_extends_detect_tree(NEW.id));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER hierarchy_twin_class_extends_update_tree_trigger
    AFTER INSERT OR UPDATE OF extends_twin_class_id ON public.twin_class
    FOR EACH ROW
EXECUTE FUNCTION public.hierarchy_twin_class_extends_process_tree_update();



--
-- HEAD SECTION
--

CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_head_detect_tree(twin_class_id UUID)
    RETURNS TEXT AS $$
DECLARE
    hierarchy                             TEXT   := '';
    current_id                            UUID   := twin_class_id;
    parent_id                             UUID;
    visited_ids                           UUID[] := ARRAY [twin_class_id];
BEGIN
    RAISE NOTICE 'Detected head hier. for twin class id: %', twin_class_id;
    -- return value init
    hierarchy := '';

    -- cycle for build hierarchy form twin-in to twin-root
    LOOP
        -- get parent_id and shema flags and check space_schema_id is present for twin-in
        SELECT tc.head_twin_class_id INTO parent_id
        FROM public.twin_class tc WHERE tc.id = current_id;

        -- check for cycle
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %', twin_class_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN hierarchy;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_head_update_tree_hard(twin_class_id UUID, detected_hierarchy TEXT)
    RETURNS VOID AS $$
DECLARE
    hierarchy_to_use TEXT;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_head_detect_tree(twin_class_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', hierarchy_to_use;

    -- update hier. and schemas for twin-class-in
    UPDATE public.twin_class SET head_hierarchy_tree = text2ltree(hierarchy_to_use) WHERE id = twin_class_id;

    -- update hier. and schemas for twin-class-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin_class
        WHERE head_twin_class_id = twin_class_id
        UNION ALL
        SELECT tc.id, d.depth + 1
        FROM public.twin_class tc INNER JOIN descendants d ON tc.head_twin_class_id = d.id
        WHERE d.depth < 10
    )
    UPDATE public.twin_class tc
    SET head_hierarchy_tree = text2ltree(public.hierarchy_twin_class_head_detect_tree(tc.id))
    WHERE tc.id IN (SELECT id FROM descendants);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_head_update_tree_soft(twin_class_id UUID, detected_hierarchy TEXT)
    RETURNS VOID AS $$
DECLARE
    hierarchy_to_use TEXT;
    old_hierarchy TEXT;
    new_hierarchy TEXT;
BEGIN
    SELECT head_hierarchy_tree::text INTO old_hierarchy FROM public.twin_class WHERE id = twin_class_id;

    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_head_detect_tree(twin_class_id);
    END IF;

    new_hierarchy := hierarchy_to_use;

    RAISE NOTICE 'NEW: %', new_hierarchy;
    RAISE NOTICE 'OLD: %', old_hierarchy;

    UPDATE public.twin_class
    SET head_hierarchy_tree = text2ltree(replace(twin_class.head_hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE head_hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.hierarchy_twin_class_head_process_tree_update()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.head_twin_class_id IS DISTINCT FROM NEW.head_twin_class_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.head_twin_class_id IS DISTINCT FROM NEW.head_twin_class_id THEN
            PERFORM public.hierarchy_twin_class_head_update_tree_soft(NEW.id, public.hierarchy_twin_class_head_detect_tree(NEW.id));
        ELSE
            PERFORM public.hierarchy_twin_class_head_update_tree_soft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.hierarchy_twin_class_head_update_tree_hard(NEW.id, public.hierarchy_twin_class_head_detect_tree(NEW.id));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER hierarchy_twin_class_head_update_tree_trigger
    AFTER INSERT OR UPDATE OF head_twin_class_id ON public.twin_class
    FOR EACH ROW
EXECUTE FUNCTION public.hierarchy_twin_class_head_process_tree_update();

DO
$$
    DECLARE
        root_twin_class RECORD;
    BEGIN
        FOR root_twin_class IN SELECT id FROM public.twin_class WHERE twin_class.head_twin_class_id IS NULL
            LOOP
                PERFORM public.hierarchy_twin_class_head_update_tree_hard(root_twin_class.id, NULL);
            END LOOP;
    END
$$;

DO
$$
    DECLARE
        root_twin_class RECORD;
    BEGIN
        FOR root_twin_class IN SELECT id FROM public.twin_class WHERE twin_class.extends_twin_class_id IS NULL
            LOOP
                PERFORM public.hierarchy_twin_class_extends_update_tree_hard(root_twin_class.id, NULL);
            END LOOP;
    END
$$;
