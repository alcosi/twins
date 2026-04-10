create or replace function twinflowdetecthierarchical(twinflowschemaid uuid, twinclassid uuid)
    returns uuid
    immutable
    language plpgsql
as
$$
DECLARE
detectedTwinflow UUID;
    currentTwinClassId uuid := twinClassId;
    extendsTwinClassId UUID;
    visitedClassIds UUID[] := '{}';
BEGIN
    IF twinClassId IS NULL OR twinflowSchemaId IS NULL THEN
        RETURN NULL;
END IF;

    -- cycle loop twin class extends hierarchy from child to parent
    LOOP
IF currentTwinClassId = ANY(visitedClassIds) THEN
            RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %',
                extendsTwinClassId;
END IF;

        -- 1. Try with specific schema first
SELECT twinflow_id INTO detectedTwinflow FROM twinflow_schema_map
WHERE twin_class_id = currentTwinClassId AND twinflow_schema_id = twinflowSchemaId;

IF detectedTwinflow IS NOT NULL THEN
            RETURN detectedTwinflow;
END IF;

        -- 2. Fallback to global schema (domain_id IS NULL)
SELECT tsm.twinflow_id INTO detectedTwinflow
FROM twinflow_schema_map tsm
         JOIN twinflow_schema ts ON ts.id = tsm.twinflow_schema_id
WHERE tsm.twin_class_id = currentTwinClassId AND ts.domain_id IS NULL;

IF detectedTwinflow IS NOT NULL THEN
            RETURN detectedTwinflow;
END IF;

SELECT extends_twin_class_id INTO extendsTwinClassId FROM twin_class WHERE id =
                                                                           currentTwinClassId;
EXIT WHEN extendsTwinClassId IS NULL;

        visitedClassIds := array_append(visitedClassIds, currentTwinClassId);
        currentTwinClassId := extendsTwinClassId;
END LOOP;

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


UPDATE public.twin_status SET key = 'error'::varchar WHERE id = '00000000-0000-0000-0003-000000000008'::uuid;
UPDATE public.twin_status SET key = 'completed'::varchar WHERE id = '00000000-0000-0000-0003-000000000007'::uuid;
UPDATE public.twin_status SET key = 'in_progress'::varchar WHERE id = '00000000-0000-0000-0003-000000000006'::uuid;

UPDATE public.twin_class SET extends_twin_class_id = '00000000-0000-0000-0001-000000000004'::uuid WHERE id = '00000000-0000-0000-0001-000000000006'::uuid;

UPDATE public.twin_class SET extends_twin_class_id = '00000000-0000-0000-0001-000000000006'::uuid WHERE id = '00000000-0000-0000-0001-000000000007'::uuid;

UPDATE public.twinflow SET initial_sketch_twin_status_id = '00000001-0000-0000-0000-000000000001'::uuid WHERE id = '00000000-0000-0000-0019-000000000001'::uuid;