DROP FUNCTION IF EXISTS public.permission_get_roles(uuid, uuid);


create function permission_get_roles(permissionschemaid uuid, permissionid uuid)
    returns TABLE(space_role_id uuid)
    immutable
    language sql
as
$$
SELECT pssr.space_role_id
FROM permission_grant_space_role pssr
WHERE pssr.permission_schema_id = permissionSchemaId
  AND pssr.permission_id = permissionId;
$$;
