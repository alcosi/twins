
CREATE OR REPLACE FUNCTION twinflowDetectSchema(domainId UUID, businessAccountId UUID, spaceId UUID)
    RETURNS UUID AS $$
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
    IF businessAccountId IS NOT NULL AND spaceId IS NULL THEN
        SELECT twinflow_schema_id INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId AND business_account_id = businessAccountId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    SELECT twinflow_schema_id INTO schemaId FROM domain WHERE id = domainId;
    RETURN schemaId;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


CREATE OR REPLACE FUNCTION twinflowDetect(domainId UUID, businessAccountId UUID, spaceId UUID, twinClassId UUID)
    RETURNS UUID AS $$
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

    
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;





