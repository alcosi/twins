CREATE OR REPLACE FUNCTION update_quota_on_twin_deletion()
    RETURNS TRIGGER AS $$
DECLARE
var_business_account_id UUID;
    var_total_size BIGINT;
    var_total_count INTEGER;
BEGIN
    var_business_account_id := OLD.owner_business_account_id;

SELECT
    COALESCE(SUM(size), 0),
    COALESCE(COUNT(*), 0)
INTO var_total_size, var_total_count
FROM twin_attachment
WHERE twin_id = OLD.id;

IF var_total_count > 0 THEN
UPDATE domain_business_account
SET
    attachments_storage_used_count = attachments_storage_used_count - var_total_count,
    attachments_storage_used_size = attachments_storage_used_size - var_total_size
WHERE business_account_id = var_business_account_id;
END IF;

RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_quota_on_twin_deletion ON twin;
CREATE TRIGGER trigger_update_quota_on_twin_deletion
    BEFORE DELETE ON twin
    FOR EACH ROW
    EXECUTE FUNCTION update_quota_on_twin_deletion();