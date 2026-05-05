-- Add twins_count_quota field to twin_class_schema_map for quota management
ALTER TABLE twin_class_schema_map
    ADD COLUMN IF NOT EXISTS twins_count_quota INTEGER;

-- Function to detect max twin count by hierarchy (Space -> BA -> Domain)
CREATE OR REPLACE FUNCTION twins_quota_get(
    twinClassSchemaSpaceId uuid,
    domainid uuid,
    businessaccountid uuid,
    twinclassid uuid
) RETURNS INTEGER AS $$
DECLARE
    twinsCountQuota INTEGER;
BEGIN
    -- 1. Check space schema first (if exists)
    IF twinClassSchemaSpaceId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO twinsCountQuota
        FROM space s
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = s.twin_class_schema_id
        WHERE s.twin_id = twinClassSchemaSpaceId
          AND tcsmap.twin_class_id = twinClassId;

        IF twinsCountQuota IS NOT NULL THEN
            RETURN twinsCountQuota;
        END IF;
    END IF;

    -- 2. Check business account schema (if exists)
    IF businessAccountId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO twinsCountQuota
        FROM domain_business_account dba
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = dba.twin_class_schema_id
        WHERE dba.domain_id = domainId
          AND dba.business_account_id = businessAccountId
          AND tcsmap.twin_class_id = twinClassId;

        IF twinsCountQuota IS NOT NULL THEN
            RETURN twinsCountQuota;
        END IF;
    END IF;

    -- 3. Check domain schema (if not found in Space or BA)
    IF domainId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO twinsCountQuota
        FROM domain d
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = d.twin_class_schema_id
        WHERE d.id = domainId
          AND tcsmap.twin_class_id = twinClassId;

        IF twinsCountQuota IS NOT NULL THEN
            RETURN twinsCountQuota;
        END IF;
    END IF;

    RETURN NULL; -- No quota limit
END;
$$ LANGUAGE plpgsql IMMUTABLE;

