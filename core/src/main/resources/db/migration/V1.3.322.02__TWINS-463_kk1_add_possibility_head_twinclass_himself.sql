create or replace function public.hierarchy_twin_class_head_detect_tree(twin_class_id uuid) returns text
    language plpgsql
as
$$
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
        -- get parent_id and schema flags and check space_schema_id is present for twin-in
SELECT tc.head_twin_class_id INTO parent_id
FROM public.twin_class tc WHERE tc.id = current_id;

-- check for cycle
IF parent_id = ANY (visited_ids) THEN
            RAISE NOTICE 'Return detected hier. for: %', hierarchy;
            hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
RETURN hierarchy;
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
$$;

