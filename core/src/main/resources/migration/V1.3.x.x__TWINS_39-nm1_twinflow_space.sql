CREATE OR REPLACE FUNCTION twinflowDetectSchemaByBusinessAccount(domainId UUID, businessAccountId UUID)
    RETURNS UUID AS
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT twinflow_schema_id
        INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId
          AND business_account_id = businessAccountId;
    END IF;
    RETURN schemaId;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION twinflowDetectSchemaByDomain(domainId UUID)
    RETURNS UUID AS
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in domain
    IF domainId IS NOT NULL THEN
        SELECT twinflow_schema_id INTO schemaId FROM domain WHERE id = domainId;
    END IF;
    RETURN schemaId;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION twinflowDetectSchema(domainId UUID, businessAccountId UUID, spaceId UUID)
    RETURNS UUID AS
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in space
    IF spaceId IS NOT NULL THEN
        SELECT twinflow_schema_id INTO schemaId FROM space WHERE twin_id = spaceId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- twin in BA
    schemaId := twinflowDetectSchemaByBusinessAccount(domainId, businessAccountId);
    IF schemaId IS NOT NULL THEN
        RETURN schemaId;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    RETURN twinflowDetectSchemaByDomain(domainId);
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION twinflowDetectHierarchical(twinflowSchemaId UUID, twinClassId UUID)
    RETURNS UUID AS
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

        SELECT twinflow_id INTO detectedTwinflow FROM twinflow_schema_map
        WHERE twin_class_id = currentTwinClassId AND twinflow_schema_id = twinflowSchemaId;
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
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION twinflowDetect(domainId UUID, businessAccountId UUID, spaceId UUID, twinClassId UUID)
    RETURNS UUID AS
$$
DECLARE
    twinflowSchemaId UUID;
BEGIN
    IF twinClassId IS NULL THEN
        RETURN NULL;
    END IF;

    -- Detect twinflow schema
    twinflowSchemaId := twinflowDetectSchema(domainId, businessAccountId, spaceId);
    IF twinflowSchemaId IS NULL THEN
        RETURN NULL;
    END IF;

    RETURN twinflowDetectHierarchical(twinflowSchemaId, twinClassId);
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;





