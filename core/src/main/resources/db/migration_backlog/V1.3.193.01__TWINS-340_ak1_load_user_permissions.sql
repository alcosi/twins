CREATE OR REPLACE FUNCTION permissions_load_for_user(
    p_permissionSchemaId UUID,
    p_userId UUID,
    p_userGroupIds UUID[]
) RETURNS TABLE(permission_id UUID) AS $$
BEGIN
    RETURN QUERY
        SELECT DISTINCT pgu.permission_id
        FROM permission_grant_user pgu
        WHERE pgu.permission_schema_id = p_permissionSchemaId
          AND pgu.user_id = p_userId

        UNION

        SELECT DISTINCT pgg.permission_id
        FROM permission_grant_global pgg
        WHERE pgg.user_group_id = ANY(p_userGroupIds)

        UNION

        SELECT DISTINCT pgug.permission_id
        FROM permission_grant_user_group pgug
        WHERE pgug.permission_schema_id = p_permissionSchemaId
          AND pgug.user_group_id = ANY(p_userGroupIds)

        UNION

        SELECT DISTINCT pgap.permission_id
        FROM permission_grant_assignee_propagation pgap
                 JOIN twin t ON t.assigner_user_id = p_userId
        WHERE (t.twin_class_id = pgap.propagation_by_twin_class_id AND pgap.propagation_by_twin_status_id is null)
           or (t.twin_status_id = pgap.propagation_by_twin_status_id AND pgap.propagation_by_twin_status_id is not null);

END $$ LANGUAGE plpgsql;
