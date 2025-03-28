CREATE OR REPLACE FUNCTION get_users_by_groups(
    p_user_group_ids UUID[],
    p_domain_id UUID DEFAULT NULL,
    p_business_account_id UUID DEFAULT NULL
)
    RETURNS TABLE (
                      user_id UUID
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT DISTINCT ugm1.user_id
        FROM user_group_map_type1 ugm1
                 JOIN "user" u ON ugm1.user_id = u.id
        WHERE ugm1.user_group_id = ANY(p_user_group_ids)
          AND (p_domain_id IS NULL OR u.domain_id = p_domain_id)
          AND (p_business_account_id IS NULL OR u.business_account_id = p_business_account_id)

        UNION

        SELECT DISTINCT ugm2.user_id
        FROM user_group_map_type2 ugm2
                 JOIN "user" u ON ugm2.user_id = u.id
        WHERE ugm2.user_group_id = ANY(p_user_group_ids)
          AND (p_domain_id IS NULL OR u.domain_id = p_domain_id)
          AND (p_business_account_id IS NULL OR u.business_account_id = p_business_account_id)

        UNION

        SELECT DISTINCT ugm3.user_id
        FROM user_group_map_type3 ugm3
                 JOIN "user" u ON ugm3.user_id = u.id
        WHERE ugm3.user_group_id = ANY(p_user_group_ids)
          AND (p_domain_id IS NULL OR u.domain_id = p_domain_id)
          AND (p_business_account_id IS NULL OR u.business_account_id = p_business_account_id);
END;
$$;