alter table twin_class_field
    add if not exists inheritable boolean default true not null;

alter table twin_status
    add if not exists inheritable boolean default true not null;

alter table link
    add if not exists inheritable boolean default true not null;

alter table twin_class_dynamic_marker
    add if not exists inheritable boolean default true not null;

alter table twinflow
    add if not exists inheritable boolean default true not null;

create or replace function twinflowdetecthierarchical(twinflowschemaid uuid, twinclassid uuid) returns uuid
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
            RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %', extendsTwinClassId;
        END IF;

        IF (currentTwinClassId = twinclassid) -- first loop
        THEN
            SELECT twinflow_id INTO detectedTwinflow FROM twinflow_schema_map tsm
            WHERE tsm.twin_class_id = currentTwinClassId AND tsm.twinflow_schema_id = twinflowSchemaId;
        ELSE
            SELECT twinflow_id INTO detectedTwinflow FROM twinflow_schema_map tsm LEFT JOIN twinflow tf ON tsm.twinflow_id = tf.id
            WHERE tsm.twin_class_id = currentTwinClassId AND tsm.twinflow_schema_id = twinflowSchemaId AND tf.inheritable;
        END IF;
        IF detectedTwinflow IS NOT NULL THEN
            RETURN detectedTwinflow;
        END IF;

        SELECT extends_twin_class_id INTO extendsTwinClassId FROM twin_class WHERE id = currentTwinClassId;
        EXIT WHEN extendsTwinClassId IS NULL;

        -- add currentTwinClassId to visitedClassIds before moving to the next twin_class
        visitedClassIds := array_append(visitedClassIds, currentTwinClassId);
        currentTwinClassId := extendsTwinClassId;
    END LOOP;

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


