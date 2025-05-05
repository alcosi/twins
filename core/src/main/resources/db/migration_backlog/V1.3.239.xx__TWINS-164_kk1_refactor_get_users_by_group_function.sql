DROP FUNCTION IF EXISTS get_users_by_groups(UUID, UUID, UUID);

CREATE OR REPLACE FUNCTION is_user_in_group(
    p_user_id UUID,
    p_user_group_id UUID,
    p_domain_id UUID DEFAULT NULL,
    p_business_account_id UUID DEFAULT NULL
)
    RETURNS BOOLEAN
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN EXISTS (SELECT 1
                   FROM user_group_map_type1
                   WHERE user_group_id = p_user_group_id
                     AND user_id = p_user_id) OR
           EXISTS (SELECT 1
                   FROM user_group_map_type2
                   WHERE user_group_id = p_user_group_id
                     AND user_id = p_user_id
                     AND (p_business_account_id IS NULL OR
                          business_account_id = p_business_account_id)) OR
           EXISTS (SELECT 1
                   FROM user_group_map_type3
                   WHERE user_group_id = p_user_group_id
                     AND user_id = p_user_id
                     AND (p_domain_id IS NULL OR domain_id = p_domain_id));
END;
$$;