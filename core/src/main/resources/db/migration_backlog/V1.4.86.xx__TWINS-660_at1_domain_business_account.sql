-- function
CREATE OR REPLACE FUNCTION business_account_properties_init_on_insert(
    p_domain_business_account_id uuid,
    p_tier_id uuid
) RETURNS void AS $$
BEGIN
UPDATE domain_business_account ba
SET notification_schema_id = t.notification_schema_id
    FROM tier t
WHERE ba.id = p_domain_business_account_id
  AND t.id = p_tier_id;
END;
$$ LANGUAGE plpgsql;

   -- wrapper
CREATE OR REPLACE FUNCTION domain_business_account_after_insert_wrapper()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM business_account_properties_init_on_insert(
        NEW.id,
        NEW.tier_id
    );
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- trigger
DROP TRIGGER IF EXISTS domain_business_account_after_insert_trigger
    ON domain_business_account;

CREATE TRIGGER domain_business_account_after_insert_trigger
    AFTER INSERT ON domain_business_account
    FOR EACH ROW
    EXECUTE FUNCTION domain_business_account_after_insert_wrapper();
