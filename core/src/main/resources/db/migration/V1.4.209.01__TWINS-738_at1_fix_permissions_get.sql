create or replace function permissions_get(p_permission_schema_id uuid, p_user_id uuid, p_user_group_footprint uuid)
    returns TABLE(permission_id uuid)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        SELECT DISTINCT pmg.permission_id
        FROM permission_mater_global pmg
        WHERE pmg.user_group_footprint_id = p_user_group_footprint
          AND pmg.grants_count > 0

        UNION

        SELECT DISTINCT pgu.permission_id
        FROM permission_grant_user pgu
        WHERE pgu.permission_schema_id = p_permission_schema_id
          AND pgu.user_id = p_user_id

        UNION

        SELECT DISTINCT pmug.permission_id
        FROM permission_mater_user_group pmug
        WHERE pmug.permission_schema_id = p_permission_schema_id
          AND pmug.user_group_footprint_id = p_user_group_footprint
          AND pmug.grants_count > 0;

END $$;
