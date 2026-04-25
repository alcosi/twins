-- Add max_twin_count field to twin_class_schema_map for quota management
ALTER TABLE public.twin_class_schema_map
    ADD COLUMN IF NOT EXISTS max_twin_count INTEGER;

-- Function to detect max twin count by hierarchy (BA -> Domain)
CREATE OR REPLACE FUNCTION twinclass_detect_max_count(
    domainid uuid,
    businessaccountid uuid,
    twinclassid uuid
) RETURNS INTEGER AS $$
DECLARE
    maxCount INTEGER;
BEGIN
    -- 1. Check business account schema first (if exists)
    IF businessAccountId IS NOT NULL THEN
        SELECT tcsmap.max_twin_count INTO maxCount
        FROM domain_business_account dba
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = dba.twin_class_schema_id
        WHERE dba.domain_id = domainId
          AND dba.business_account_id = businessAccountId
          AND tcsmap.twin_class_id = twinClassId;

        IF maxCount IS NOT NULL THEN
            RETURN maxCount;
        END IF;
    END IF;

    -- 2. Check domain schema (if not found in BA)
    IF domainId IS NOT NULL THEN
        SELECT tcsmap.max_twin_count INTO maxCount
        FROM domain d
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = d.twin_class_schema_id
        WHERE d.id = domainId
          AND tcsmap.twin_class_id = twinClassId;

        IF maxCount IS NOT NULL THEN
            RETURN maxCount;
        END IF;
    END IF;

    RETURN NULL; -- No quota limit
END;
$$ LANGUAGE plpgsql IMMUTABLE;
