CREATE TABLE if not exists public.permission_schema_assignee_propagation (
                                                               id uuid NOT NULL,
                                                               permission_schema_id uuid,
                                                               permission_id uuid,
                                                               propagation_by_twin_class_id uuid,
                                                               propagation_by_twin_status_id uuid,
                                                               granted_by_user_id uuid,
                                                               granted_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

alter table public.permission_schema_assignee_propagation drop constraint if exists permission_schema_assignee_propag_pk;
alter table public.permission_schema_assignee_propagation drop constraint if exists permission_schema_assignee_propag_granted_by_user_id_fkey;
alter table public.permission_schema_assignee_propagation drop constraint if exists permission_schema_assignee_propag_permission_id_fkey;
alter table public.permission_schema_assignee_propagation drop constraint if exists permission_schema_assignee_propag_permission_schema_id_fkey;
alter table public.permission_schema_assignee_propagation drop constraint if exists permission_schema_assignee_propag_twin_class_id_fkey;
alter table public.permission_schema_assignee_propagation drop constraint if exists permission_schema_assignee_propag_twin_status_id_fkey;

ALTER TABLE ONLY public.permission_schema_assignee_propagation ADD CONSTRAINT permission_schema_assignee_propag_pk PRIMARY KEY (id);
ALTER TABLE ONLY public.permission_schema_assignee_propagation ADD CONSTRAINT permission_schema_assignee_propag_granted_by_user_id_fkey FOREIGN KEY (granted_by_user_id) REFERENCES public."user"(id);
ALTER TABLE ONLY public.permission_schema_assignee_propagation ADD CONSTRAINT permission_schema_assignee_propag_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permission(id);
ALTER TABLE ONLY public.permission_schema_assignee_propagation ADD CONSTRAINT permission_schema_assignee_propag_permission_schema_id_fkey FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id);
ALTER TABLE ONLY public.permission_schema_assignee_propagation ADD CONSTRAINT permission_schema_assignee_propag_twin_class_id_fkey FOREIGN KEY (propagation_by_twin_class_id) REFERENCES public.twin_class(id);
ALTER TABLE ONLY public.permission_schema_assignee_propagation ADD CONSTRAINT permission_schema_assignee_propag_twin_status_id_fkey FOREIGN KEY (propagation_by_twin_status_id) REFERENCES public.twin_status(id);

DROP FUNCTION IF EXISTS public.permission_check_assignee_involver(UUID, UUID, UUID);

CREATE OR REPLACE FUNCTION permission_check_assignee_involver(permissionSchemaId UUID, permissionId UUID, userId UUID)
    RETURNS BOOLEAN AS
$$
DECLARE
    twinClassId UUID;
    twinStatusId UUID;
BEGIN
    -- check rights by twin_class_id
    SELECT propagation_by_twin_class_id INTO twinClassId FROM public.permission_schema_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_class_id IS NOT NULL
    LIMIT 1;

    -- check rights by twin_status_id
    SELECT propagation_by_twin_status_id INTO twinStatusId FROM public.permission_schema_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_status_id IS NOT NULL
    LIMIT 1;

    IF twinStatusId IS NOT NULL AND twinClassId IS NOT NULL THEN
        -- if twin_status_id and twin_class_id exists, check twin exists with assignee current user
        PERFORM 1 FROM public.twin
        WHERE assigner_user_id = userId AND twin_class_id = twinClassId AND twin_status_id = twinStatusId LIMIT 1;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;

    IF twinClassId IS NOT NULL THEN
        -- if twin_class_id exists, check twin exists with assignee current user
        PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_class_id = twinClassId LIMIT 1;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;


    IF twinStatusId IS NOT NULL THEN
        -- if twin_status_id exists, check twin exists with assignee current user
        PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_status_id = twinStatusId LIMIT 1;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;


    RETURN FALSE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS public.permissionCheck(UUID, UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS public.permission_check(UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);
DROP FUNCTION IF EXISTS public.permission_check(UUID, UUID, UUID, UUID, UUID, UUID, UUID[], UUID, BOOLEAN, BOOLEAN);

create function permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionidtwin uuid, permissionidtwinclass uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false)
    returns boolean as
$$
DECLARE
    permissionIdForUse UUID := permissionIdTwin;
BEGIN
    IF permissionIdForUse IS NULL THEN
        permissionIdForUse = permissionIdTwinClass;
    END IF;
    RETURN permission_check(domainId, businessAccountId, spaceId, permissionIdForUse, userId, userGroupIdList, twinClassId, isAssignee, isCreator);
END;
$$ language plpgsql immutable;

create or replace function permission_check(domainId uuid, businessAccountId uuid, spaceId uuid, permissionId uuid, userId uuid, userGroupIdList uuid[], twinClassId uuid, isAssignee boolean DEFAULT false, isCreator boolean DEFAULT false)
    returns boolean as
$$
DECLARE
    permissionSchemaId        UUID;
    roles                     VARCHAR[] := '{}';
    isSpaceAssignee           BOOLEAN DEFAULT FALSE;
    isSpaceCreator            BOOLEAN DEFAULT FALSE;
BEGIN
    --- PERMISSION IS ABSENT
    IF permissionId IS NULL THEN
        RETURN TRUE;
    END IF;

    --- DENY_ALL permission
    IF permissionId = '00000000-0000-0000-0004-000000000001' THEN
        RETURN FALSE;
    END IF;

    -- Detect permission schema
    permissionSchemaId := permissionDetectSchema(domainId, businessAccountId, spaceId);

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check direct user or user group permissions
    IF permissionCheckBySchema(permissionSchemaId, permissionId, userId, userGroupIdList) THEN
        RETURN TRUE;
    END IF;

    IF isAssignee THEN
        roles := array_append(roles, 'assignee');
    END IF;

    IF isCreator THEN
        roles := array_append(roles, 'creator');
    END IF;

    SELECT * INTO isSpaceAssignee, isSpaceCreator FROM permissionCheckSpaceAssigneeAndCreator(spaceId, userId);

    IF isSpaceAssignee THEN
        roles := array_append(roles, 'space_assignee');
    END IF;

    IF isSpaceCreator THEN
        roles := array_append(roles, 'space_creator');
    END IF;

    -- Check twin-role permissions
    IF permissionCheckTwinRole(permissionSchemaId, permissionId, roles, twinClassId) THEN
        RETURN TRUE;
    END IF;

    -- check propagation
    IF permission_check_assignee_involver(permissionSchemaId, permissionId, userId) THEN
        RETURN TRUE;
    END IF;

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check space-role and space-role-group permissions
    RETURN permissionCheckSpaceRolePermissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$ language plpgsql immutable


