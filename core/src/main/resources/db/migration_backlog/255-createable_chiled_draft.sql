create or replace function permission_twin_create_check(domainid uuid, businessaccountid uuid, permissionid uuid,
                                                        userid uuid, usergroupidlist uuid[],
                                                        space2newtwinclass2permission text[],
                                                        isassignee boolean DEFAULT false,
                                                        iscreator boolean DEFAULT false)
    RETURNS TABLE
            (
                space_id             UUID,
                create_twin_class_id UUID,
                has_permission       BOOLEAN
            )
    language plpgsql
as
$$
DECLARE
    roles           VARCHAR[] := '{}';
    isSpaceAssignee BOOLEAN DEFAULT FALSE;
    isSpaceCreator  BOOLEAN DEFAULT FALSE;
    checker         INTEGER default 0;
BEGIN
    --- PERMISSION IS ABSENT
    IF permissionId IS NULL THEN
        RETURN TRUE;
    END IF;

    --- DENY_ALL permission
    IF permissionId = '00000000-0000-0000-0004-000000000001' THEN
        RETURN FALSE;
    END IF;

    CREATE UNLOGGED TABLE temp_table
    (
        space_id             UUID,
        create_twin_class_id UUID,
        permission_id        UUID,
        permission_schema_id UUID,
        is_space_assignee    BOOLEAN DEFAULT null,
        is_space_creator     BOOLEAN DEFAULT null,
        has_permission       BOOLEAN DEFAULT false
    );

    INSERT INTO temp_table (space_id, create_twin_class_id, permission_id)
    SELECT split_part(elem, '.', 1)::UUID AS uuid1,
           split_part(elem, '.', 2)::UUID AS uuid2,
           split_part(elem, '.', 3)::UUID AS uuid3
    FROM unnest(space2newtwinclass2permission) AS elem;

    UPDATE temp_table tt
    SET permission_schema_id = sp.permission_schema_id,
        is_space_creator     = (tw.created_by_user_id = userid),
        is_space_assignee    = (tw.assigner_user_id = userid)
    FROM space sp,
         twin tw
    WHERE sp.twin_id = tw.id
      AND sp.twin_id = tt.space_id;

    SELECT count(*) INTO checker FROM temp_table WHERE permission_schema_id IS NULL;
    -- Exit if no permission schema found
    IF checker = array_length(space2newtwinclass2permission, 1) THEN
        RETURN QUERY SELECT space_id, create_twin_class_id, permission_id FROM temp_table;
    END IF;

    -- Check direct user grant permissions
    UPDATE temp_table tt
    SET has_permission = true
    FROM permission_grant_user pgu
    WHERE tt.permission_schema_id = pgu.permission_schema_id
      AND tt.permission_id = pgu.permission_id
      AND pgu.user_id = userid
      AND tt.has_permission = false;

    SELECT count(*) INTO checker FROM temp_table WHERE has_permission = false;
    -- Exit if all items has permissions
    IF checker = 0 THEN
        RETURN QUERY SELECT space_id, create_twin_class_id, permission_id FROM temp_table;
    END IF;

    -- Check user_group grant permissions
    UPDATE temp_table tt
    SET has_permission = true
    FROM permission_grant_user_group pgug
    WHERE tt.permission_schema_id = pgug.permission_schema_id
      AND tt.permission_id = pgug.permission_id
      AND pgug.user_group_id = ANY(userGroupIdList)
      AND tt.has_permission = false;

    SELECT count(*) INTO checker FROM temp_table WHERE has_permission = false;
    -- Exit if all items has permissions
    IF checker = 0 THEN
        RETURN QUERY SELECT space_id, create_twin_class_id, permission_id FROM temp_table;
    END IF;

    UPDATE temp_table tt
    SET has_permission = true
    FROM permission_grant_twin_role pgtr
    WHERE tt.permission_schema_id = pgtr.permission_schema_id
      AND tt.permission_id = pgtr.permission_id
      AND tt.create_twin_class_id = pgtr.twin_class_id
      AND ((tt.is_space_assignee = true AND pgtr.twin_role_id = 'space_assignee') OR (tt.is_space_creator = true AND pgtr.twin_role_id = 'space_creator'))
      AND tt.has_permission = false;

    SELECT count(*) INTO checker FROM temp_table WHERE has_permission = false;
    -- Exit if all items has permissions
    IF checker = 0 THEN
        RETURN QUERY SELECT space_id, create_twin_class_id, permission_id FROM temp_table;
    END IF;

    -- check propagation
    IF permission_check_assignee_propagation(permissionSchemaId, permissionId, businessAccountId, spaceId, userId) THEN
        RETURN TRUE;
    END IF;

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check space-role and space-role-group permissions
    RETURN permission_check_space_role_permissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$;

alter function permission_check(uuid, uuid, uuid, uuid, uuid, uuid[], uuid, boolean, boolean) owner to gateway80lvl;

