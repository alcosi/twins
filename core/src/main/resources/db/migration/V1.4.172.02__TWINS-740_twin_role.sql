drop view if exists permission_schema_twin_role_lazy;

ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_creator BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_creator BOOLEAN DEFAULT FALSE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'permission_grant_twin_role' AND column_name = 'twin_role_id') THEN
        UPDATE permission_grant_twin_role SET granted_to_assignee = TRUE WHERE twin_role_id = 'assignee';
        UPDATE permission_grant_twin_role SET granted_to_creator = TRUE WHERE twin_role_id = 'creator';
        UPDATE permission_grant_twin_role SET granted_to_space_assignee = TRUE WHERE twin_role_id = 'space_assignee';
        UPDATE permission_grant_twin_role SET granted_to_space_creator = TRUE WHERE twin_role_id = 'space_creator';
    END IF;
END $$;

ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_creator SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_creator SET NOT NULL;


drop index if exists idx_permission_schema_twin_role_twinclass_schema_and_perm_id;



create or replace function permission_check_twin_role(permissionschemaid uuid, permissionid uuid, roles character varying[], twinclassid uuid) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    hasPermission BOOLEAN := FALSE;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM permission_grant_twin_role
        WHERE permission_schema_id = permissionSchemaId
          AND permission_id = permissionId
          AND twin_class_id = twinClassId
          AND (
            (granted_to_assignee AND 'assignee' = ANY(roles)) OR
            (granted_to_creator AND 'creator' = ANY(roles)) OR
            (granted_to_space_assignee AND 'space_assignee' = ANY(roles)) OR
            (granted_to_space_creator AND 'space_creator' = ANY(roles))
            )
    ) INTO hasPermission;

    RETURN hasPermission;
END;
$$;


DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'permission_grant_twin_role' AND column_name = 'twin_role_id') THEN
        CREATE TEMP TABLE permission_grant_twin_role_collapsed AS
        SELECT
            uuid_generate_v7_custom() as id,
            permission_schema_id,
            permission_id,
            twin_class_id,
            bool_or(granted_to_assignee) as granted_to_assignee,
            bool_or(granted_to_creator) as granted_to_creator,
            bool_or(granted_to_space_assignee) as granted_to_space_assignee,
            bool_or(granted_to_space_creator) as granted_to_space_creator,
            min(granted_by_user_id::text)::uuid as granted_by_user_id,
            min(granted_at) as granted_at
        FROM permission_grant_twin_role
        GROUP BY permission_schema_id, permission_id, twin_class_id;

        DELETE FROM permission_grant_twin_role;
        ALTER TABLE permission_grant_twin_role DROP COLUMN IF EXISTS twin_role_id;

        INSERT INTO permission_grant_twin_role (
            id,
            permission_schema_id,
            permission_id,
            twin_class_id,
            granted_to_assignee,
            granted_to_creator,
            granted_to_space_assignee,
            granted_to_space_creator,
            granted_by_user_id,
            granted_at
        )
        SELECT * FROM permission_grant_twin_role_collapsed;

        -- 4. Удаляем временную таблицу
        DROP TABLE permission_grant_twin_role_collapsed;
    END IF;
END $$;

drop index if exists idx_permission_schema_twin_role_twinclass_schema_and_perm_id;
create unique index idx_permission_schema_twin_role_twinclass_schema_and_perm_id
    on permission_grant_twin_role (twin_class_id, permission_schema_id, permission_id);
