-- Add twins_count_quota field to twin_class_schema_map for quota management
ALTER TABLE twin_class_schema_map
    ADD COLUMN IF NOT EXISTS twins_count_quota INTEGER;

-- Function to detect max twin count by hierarchy (Space -> BA -> Domain)
CREATE OR REPLACE FUNCTION quota_get_limit(
    twinClassSchemaSpaceId uuid,
    domainid uuid,
    businessaccountid uuid,
    twinclassid uuid
) RETURNS INTEGER AS $$
DECLARE
    maxCount INTEGER;
BEGIN
    -- 1. Check space schema first (if exists)
    IF twinClassSchemaSpaceId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO maxCount
        FROM space s
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = s.twin_class_schema_id
        WHERE s.twin_id = twinClassSchemaSpaceId
          AND tcsmap.twin_class_id = twinClassId;

        IF maxCount IS NOT NULL THEN
            RETURN maxCount;
        END IF;
    END IF;

    -- 2. Check business account schema (if exists)
    IF businessAccountId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO maxCount
        FROM domain_business_account dba
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = dba.twin_class_schema_id
        WHERE dba.domain_id = domainId
          AND dba.business_account_id = businessAccountId
          AND tcsmap.twin_class_id = twinClassId;

        IF maxCount IS NOT NULL THEN
            RETURN maxCount;
        END IF;
    END IF;

    -- 3. Check domain schema (if not found in Space or BA)
    IF domainId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO maxCount
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

-- Function to count current twins by hierarchy (Space -> BA -> Domain)
-- Returns count for the effective level where quota is defined
CREATE OR REPLACE FUNCTION quota_count_twins(
    twinClassSchemaSpaceId uuid,
    businessaccountid uuid,
    twinclassid uuid
) RETURNS BIGINT AS $$
DECLARE
    currentCount BIGINT;
BEGIN
    -- 1. Check if space has quota defined
    IF twinClassSchemaSpaceId IS NOT NULL THEN
        SELECT tcsmap.twins_count_quota INTO currentCount
        FROM space s
        JOIN twin_class_schema_map tcsmap ON tcsmap.twin_class_schema_id = s.twin_class_schema_id
        WHERE s.twin_id = twinClassSchemaSpaceId
          AND tcsmap.twin_class_id = twinClassId
          AND tcsmap.twins_count_quota IS NOT NULL;

        IF currentCount IS NOT NULL THEN
            -- Space has quota, count twins in this space
            SELECT COUNT(*) INTO currentCount
            FROM twin t
            WHERE t.owner_business_account_id = businessAccountId
              AND t.twin_class_id = twinClassId
              AND t.twin_class_schema_space_id = twinClassSchemaSpaceId;
            RETURN currentCount;
        END IF;
    END IF;

    -- 2. No space quota (or no space), count twins without space (for BA/Domain quota)
    SELECT COUNT(*) INTO currentCount
    FROM twin t
    WHERE t.owner_business_account_id = businessAccountId
      AND t.twin_class_id = twinClassId
      AND t.twin_class_schema_space_id IS NULL;
    RETURN currentCount;
END;
$$ LANGUAGE plpgsql STABLE;

