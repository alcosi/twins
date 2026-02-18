CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE OR REPLACE FUNCTION uuid_generate_v7_custom()
    RETURNS uuid
    LANGUAGE plpgsql
AS $$
DECLARE
    unix_ms bigint;
    rand_bytes bytea;
    uuid_bytes bytea;
BEGIN
    -- 48-bit unix timestamp in milliseconds
    unix_ms := floor(extract(epoch from clock_timestamp()) * 1000);

    -- 10 random bytes (80 bits)
    rand_bytes := gen_random_bytes(10);

    -- Compose UUID (16 bytes total)
    uuid_bytes :=
            -- 6 bytes timestamp
        substring(int8send(unix_ms) from 3 for 6)
            ||
            -- version (4 bits) + first 4 bits random
        set_byte(substring(rand_bytes from 1 for 1), 0,
                 (get_byte(rand_bytes, 0) & 15) | 112)  -- 0111xxxx
            ||
            -- remaining 9 random bytes
        substring(rand_bytes from 2);

    -- Set variant (10xxxxxx)
    uuid_bytes :=
            set_byte(uuid_bytes, 8,
                     (get_byte(uuid_bytes, 8) & 63) | 128);

    RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$;


drop index if exists idx_user_group_by_assignee_propagation_granted_by_user_id;
drop index if exists idx_user_group_by_assignee_propagation_user_group_id;
drop index if exists idx_user_group_by_assignee_propagation_permission_schema_id;
drop index if exists idx_user_group_by_assignee_propagation_twin_class_id;
drop index if exists idx_user_group_by_assignee_propagation_twin_status_id;

create table if not exists user_group_by_assignee_propagation
(
    id                            uuid not null
        constraint user_group_by_assignee_propagation_pk
            primary key,
    permission_schema_id          uuid not null
        constraint user_group_by_assignee_propagation_permission_schema_id_fkey
            references permission_schema
            on update cascade on delete cascade,
    user_group_id                 uuid not null
        constraint user_group_by_assignee_propagation_user_group_id_fkey
            references user_group
            on update cascade on delete cascade,
    propagation_by_twin_class_id  uuid not null
        constraint user_group_by_assignee_propagation_twin_class_id_fkey
            references twin_class
            on update cascade on delete cascade,
    propagation_by_twin_status_id uuid
        constraint user_group_by_assignee_propagation_twin_status_id_fkey
            references twin_status
            on update cascade on delete cascade,
    created_by_user_id            uuid not null
        constraint user_group_by_assignee_propagation_granted_by_user_id_fkey
            references "user"
            on update cascade on delete cascade,
    created_at                    timestamp default CURRENT_TIMESTAMP
);

create index idx_user_group_by_assignee_propagation_granted_by_user_id
    on user_group_by_assignee_propagation (created_by_user_id);

create index idx_user_group_by_assignee_propagation_user_group_id
    on user_group_by_assignee_propagation (user_group_id);

create index idx_user_group_by_assignee_propagation_permission_schema_id
    on user_group_by_assignee_propagation (permission_schema_id);

create index idx_user_group_by_assignee_propagation_twin_class_id
    on user_group_by_assignee_propagation (propagation_by_twin_class_id);

create index idx_user_group_by_assignee_propagation_twin_status_id
    on user_group_by_assignee_propagation (propagation_by_twin_status_id);



drop trigger if exists twin_after_delete_wrapper_trigger on twin;
-- auto-generated definition
create trigger twin_after_delete_wrapper_trigger
    after delete
    on twin
    for each row
execute procedure twin_after_delete_wrapper();

create or replace function twin_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    IF old.assigner_user_id IS NOT NULL THEN
        perform user_group_involve_by_assignee_propagation(null, old.assigner_user_id, old.twin_class_id, old.twin_status_id, old.owner_business_account_id);
    END IF;

    return old;
end;
$$;

create or replace function twin_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN

    --todo react on tc and status change
    IF NEW.assigner_user_id IS DISTINCT FROM OLD.assigner_user_id THEN
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, old.assigner_user_id,NEW.twin_class_id, NEW.twin_status_id, NEW.owner_business_account_id);
    END IF;

    IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
        RAISE NOTICE 'Process update for: %', new.id;
        PERFORM hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));

        IF OLD.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
        END IF;
        IF NEW.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

create or replace function twin_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    RAISE NOTICE 'Process insert for: %', new.id;
    PERFORM hierarchyUpdateTreeHard(new.id, hierarchyDetectTree(new.id));

    IF NEW.assigner_user_id IS NOT NULL THEN
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, null, NEW.twin_class_id, NEW.twin_status_id, NEW.owner_business_account_id);
    END IF;

    IF NEW.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
    END IF;

    RETURN NEW;
END;
$$;


alter table user_group_map_type2 add if not exists auto_involved boolean default false not null;

create or replace function user_group_add_or_remove_group(p_user_id uuid, p_group_id uuid, p_domain_id uuid, p_business_account_id uuid, p_add_to_group boolean) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    selected_user_group_id UUID := null;
BEGIN

    if p_add_to_group then
        -- TODO support type1 and type3 groups
        insert into user_group_map_type2 (id,user_group_id, business_account_id, user_id, added_at, added_by_user_id, auto_involved)
        VALUES (uuid_generate_v7_custom(), p_group_id, p_business_account_id, p_user_id, now(), '00000000-0000-0000-0000-000000000000', true) on conflict (user_id, business_account_id, user_group_id) do nothing;
    else
        delete from user_group_map_type2 where auto_involved and user_id = p_user_id and business_account_id = p_business_account_id and user_group_id = p_group_id;
    end if;
END;
$$;

create or replace function user_group_involve_by_assignee_propagation(new_assigner_user_id uuid, old_assigner_user_id uuid, p_twin_class_id uuid, p_twin_status_id uuid, p_owner_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    selected_user_group_id UUID := null;
    selected_domain_id UUID := null;
    selected_status_id UUID := null;
BEGIN
    if p_owner_business_account_id is not null then

        select a.user_group_id, d.id, a.propagation_by_twin_status_id into selected_user_group_id, selected_domain_id, selected_status_id from user_group_by_assignee_propagation a
                                                                                              join twin_class tc on tc.id = a.propagation_by_twin_class_id
                                                                                              join domain d on d.id = tc.domain_id
                                                                                              join domain_business_account db on db.domain_id = d.id and db.business_account_id = p_owner_business_account_id
        where
            a.propagation_by_twin_class_id = p_twin_class_id and
            (a.propagation_by_twin_status_id = p_twin_status_id or a.propagation_by_twin_status_id is null) and
            a.permission_schema_id = COALESCE(db.permission_schema_id, d.permission_schema_id);
    end if;
    if p_owner_business_account_id is null then
        select a.user_group_id, d.id into selected_user_group_id, selected_domain_id from user_group_by_assignee_propagation a
                                                                                              join twin_class tc on tc.id = a.propagation_by_twin_class_id
                                                                                              join domain d on d.id = tc.domain_id
        where
            a.propagation_by_twin_class_id = p_twin_class_id and
            (a.propagation_by_twin_status_id = p_twin_status_id or a.propagation_by_twin_status_id is null) and
            a.permission_schema_id = d.permission_schema_id;
    end if;

    if selected_user_group_id is null then
        RETURN;
    end if;


    if new_assigner_user_id is not null then
        perform user_group_add_or_remove_group(new_assigner_user_id, selected_user_group_id, selected_domain_id, p_owner_business_account_id, true);
    end if;

    if old_assigner_user_id is not null then
        IF NOT EXISTS (SELECT 1 FROM twin
                       WHERE assigner_user_id = old_assigner_user_id
                         AND twin_class_id = p_twin_class_id
                         AND (selected_status_id IS NOT NULL AND twin_status_id = selected_status_id)
                         AND (p_owner_business_account_id IS NULL OR owner_business_account_id = p_owner_business_account_id)
                         LIMIT 1) THEN
            perform user_group_add_or_remove_group(old_assigner_user_id, selected_user_group_id, selected_domain_id, p_owner_business_account_id, false);
        END IF;
    end if;
END;
$$;




DROP FUNCTION IF EXISTS permission_check(uuid,uuid,uuid,uuid,uuid,uuid,uuid[],uuid,boolean,boolean);
create or replace function permission_check(permissionSchemaId uuid, businessaccountid uuid, spaceid uuid, permissionidtwin uuid, permissionidtwinclass uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    permissionIdForUse UUID := permissionIdTwin;
BEGIN
    IF permissionIdForUse IS NULL THEN
        permissionIdForUse = permissionIdTwinClass;
    END IF;
    RETURN permission_check(permissionSchemaId, businessAccountId, spaceId, permissionIdForUse, userId, userGroupIdList, twinClassId, isAssignee, isCreator);
END;
$$;


DROP FUNCTION IF EXISTS permission_check(uuid,uuid,uuid,uuid,uuid, uuid[],uuid,boolean,boolean);
create or replace function permission_check(domainId uuid, businessaccountid uuid, spaceid uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) returns boolean
    volatile
    language plpgsql
as
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

--     Detect permission schema
    permissionSchemaId := permission_detect_schema(domainId, businessAccountId, spaceId);

    -- Exit if no permission schema found
    IF permissionSchemaId IS NULL THEN
        RETURN FALSE;
    END IF;

--     IF isAssignee THEN
--         roles := array_append(roles, 'assignee');
--     END IF;
--
--     IF isCreator THEN
--         roles := array_append(roles, 'creator');
--     END IF;
--
--     SELECT * INTO isSpaceAssignee, isSpaceCreator FROM permission_check_space_assignee_and_creator(spaceId, userId);

--     IF isSpaceAssignee THEN
--         roles := array_append(roles, 'space_assignee');
--     END IF;

--     IF isSpaceCreator THEN
--         roles := array_append(roles, 'space_creator');
--     END IF;

    -- Check twin-role permissions
--     IF permission_check_twin_role(permissionSchemaId, permissionId, roles, twinClassId) THEN
--         RETURN TRUE;
--     END IF;

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check space-role and space-role-group permissions
    RETURN permission_check_space_role_permissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$;

create or replace function permission_detect_schema(domainid uuid, businessaccountid uuid) returns uuid
    volatile
    language plpgsql
as
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT permission_schema_id INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId AND business_account_id = businessAccountId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    SELECT permission_schema_id INTO schemaId FROM domain WHERE id = domainId;
    RETURN schemaId;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$;


create or replace function permission_detect_schema(domainid uuid, businessaccountid uuid, spaceid uuid) returns uuid
    volatile
    language plpgsql
as
$$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in space
    IF spaceId IS NOT NULL THEN
        SELECT permission_schema_id INTO schemaId FROM space WHERE twin_id = spaceId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT permission_schema_id INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId AND business_account_id = businessAccountId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    SELECT permission_schema_id INTO schemaId FROM domain WHERE id = domainId;
    RETURN schemaId;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$;



DROP FUNCTION IF EXISTS permission_check_by_group_or_user(uuid, uuid, uuid, uuid[], uuid, uuid);
DROP FUNCTION IF EXISTS permission_check_space_assignee_and_creator(uuid, uuid, uuid, uuid[], uuid, uuid);


ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_creator BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_creator BOOLEAN DEFAULT FALSE;

UPDATE permission_grant_twin_role SET granted_to_assignee = TRUE WHERE twin_role_id = 'assignee';
UPDATE permission_grant_twin_role SET granted_to_creator = TRUE WHERE twin_role_id = 'creator';
UPDATE permission_grant_twin_role SET granted_to_space_assignee = TRUE WHERE twin_role_id = 'space_assignee';
UPDATE permission_grant_twin_role SET granted_to_space_creator = TRUE WHERE twin_role_id = 'space_creator';

ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_creator SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_creator SET NOT NULL;

ALTER TABLE permission_grant_twin_role DROP COLUMN IF EXISTS twin_role_id;

drop index if exists idx_permission_schema_twin_role_twinclass_schema_and_perm_id;
create unique index idx_permission_schema_twin_role_twinclass_schema_and_perm_id
    on permission_grant_twin_role (twin_class_id, permission_schema_id, permission_id);

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

DROP FUNCTION IF EXISTS permission_check_assignee_propagation(uuid, uuid, uuid, uuid, uuid);

create or replace function permission_check_space_role_permissions(permissionschemaid uuid, permissionid uuid, spaceid uuid, userid uuid, usergroupidlist uuid[]) returns boolean
    volatile
    language plpgsql
as
$$
DECLARE
    userAssignedToRoleExists BOOLEAN;
    groupAssignedToRoleExists BOOLEAN;
BEGIN
    -- Check if any space role assigned to the user has the given permission
    SELECT EXISTS (
        SELECT 1
        FROM space_role_user sru
        WHERE sru.twin_id = spaceId
          AND sru.user_id = userId
          AND sru.space_role_id IN (SELECT space_role_id FROM permission_get_roles(permissionSchemaId, permissionId))
    ) INTO userAssignedToRoleExists;

    IF userAssignedToRoleExists THEN
        RETURN TRUE;
    END IF;

    -- Check if any space role assigned to the user's group has the given permission
    SELECT EXISTS (
        SELECT 1
        FROM space_role_user_group srug
        WHERE srug.twin_id = spaceId
          AND srug.user_group_id = ANY (userGroupIdList)
          AND srug.space_role_id IN (SELECT space_role_id FROM permission_get_roles(permissionSchemaId, permissionId))
    ) INTO groupAssignedToRoleExists;

    RETURN groupAssignedToRoleExists;
END;
$$;

drop table if exists permission_grant_assignee_propagation;

UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_CREATE' WHERE id = '00000000-0000-0004-0020-000000000002';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_MANAGE' WHERE id = '00000000-0000-0004-0020-000000000001';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_UPDATE' WHERE id = '00000000-0000-0004-0020-000000000004';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_DELETE' WHERE id = '00000000-0000-0004-0020-000000000005';
UPDATE permission SET key = 'USER_GROUP_BY_ASSIGNEE_PROPAGATION_VIEW' WHERE id = '00000000-0000-0004-0020-000000000003';

-- INSERT INTO user_group_by_assignee_propagation (id, permission_schema_id, user_group_id, propagation_by_twin_class_id, propagation_by_twin_status_id, created_by_user_id, created_at) VALUES ('0050f1ad-e160-4719-9382-df99a5abcc4a', 'af143656-9899-4e1f-8683-48795cdefeac', '6173ff08-7c2b-4302-9fff-c576f9d3c2d8', '7c027b60-0f6c-445c-9889-8ee3855d2c59', null, '00000000-0000-0000-0000-000000000000', '2026-02-16 15:09:30.000000') on conflict do nothing;
-- INSERT INTO user_group_by_assignee_propagation (id, permission_schema_id, user_group_id, propagation_by_twin_class_id, propagation_by_twin_status_id, created_by_user_id, created_at) VALUES ('0b5ffd00-d5ca-4fc1-996b-a92eb71613c6', '343db5da-c45c-4f48-b876-b488e2818d5e', '6173ff08-7c2b-4302-9fff-c576f9d3c2d8', '7c027b60-0f6c-445c-9889-8ee3855d2c59', null, '00000000-0000-0000-0000-000000000000', '2026-02-16 15:09:30.000000') on conflict do nothing;
-- select user_group_involve_by_assignee_propagation(t.assigner_user_id, NULL, t.twin_class_id, NULL, t.owner_business_account_id) from twin t where t.twin_class_id='7c027b60-0f6c-445c-9889-8ee3855d2c59' and t.assigner_user_id is not null;
-- INSERT INTO permission_grant_user_group (id, permission_schema_id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('019c667a-b95e-7366-ada0-756d9fcf3db1', 'af143656-9899-4e1f-8683-48795cdefeac', 'a62c04f4-6f5a-497c-aa71-3065e3529d29', '6173ff08-7c2b-4302-9fff-c576f9d3c2d8', '00000000-0000-0000-0000-000000000000', '2026-02-16 15:42:14.000000') on conflict do nothing ;
-- INSERT INTO permission_grant_user_group (id, permission_schema_id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('019c667b-2264-71e2-8d1d-cb89588ce101', '343db5da-c45c-4f48-b876-b488e2818d5e', 'a62c04f4-6f5a-497c-aa71-3065e3529d29', '6173ff08-7c2b-4302-9fff-c576f9d3c2d8', '00000000-0000-0000-0000-000000000000', '2026-02-16 15:42:14.000000') on conflict do nothing ;

create table space_permission_user
(
    twin_id            uuid not null
        constraint space_permission_user_twin_id_fk
            references twin
            on update cascade on delete cascade,
    permission_id      uuid not null
        constraint space_permission_user_permission_id_fk
            references permission
            on update cascade on delete cascade,
    user_id            uuid not null
        constraint space_permission_user_user_id_fk
            references "user"
            on update cascade on delete cascade,
    grants_count            int not null default 0,
        constraint space_permission_user_pk
        primary key (twin_id, permission_id, user_id)
);

drop index if exists idx_space_permission_user_grants_count;
create index idx_space_permission_user_grants_count
    on space_permission_user (grants_count);






user_group_map_type2
id
user_group_id
business_account_id
user_id                         space_permission_user_update_by_user_group_map_type2_insert()
added_at                        space_permission_user_update_by_user_group_map_type2_delete()
added_by_user_id
auto_involved

permission_grant_space_role

id
permission_schema_id+
permission_id+                  space_permission_user_update_by_permiss_grant_space_role_insert()
space_role_id+                  space_permission_user_update_by_permiss_grant_space_role_delete()
granted_by_user_id
granted_at

space_role_user_group

id
+twin_id
+space_role_id                  space_permission_user_update_by_space_role_user_group_insert()
+user_group_id                  space_permission_user_update_by_space_role_user_group_delete()
created_by_user_id
created_at

space_role_user

id
+twin_id                        space_permission_user_update_by_space_role_user_insert()
+space_role_id                  space_permission_user_update_by_space_role_user_delete()
+user_id
created_by_user_id
created_at

-- todo triggers space_role_user - CUD -> wrapper function
            insert/update/delete space_permission_user values (gen_random_uuid(), :twinId, :permissionId, :userId, :createdByUserId, now());


-- create or replace function space_permission_user_update_by_user_group_map_type2_insert(p_new_user_group_id uuid, p_new_user_id uuid, p_new_business_account_id uuid) returns void
--     volatile
--     language plpgsql
-- as
-- $$
-- DECLARE
--     selected_user_group_id UUID := null;
-- BEGIN
--
--     if p_new_user_group_id is not null then
--         if p_old_user_group_id is not null then
--             -- update
--         else
--             insert into space_permission_user (twin_id, permission_id, user_id, grants_count)
--             select t.id, pgsr.permission_id, p_new_user_id  from space_role_user_group srug
--                          join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id
--                          join twin t on t.owner_business_account_id = p_new_business_account_id and
--                                         t.id = srug.twin_id
--             where srug.user_group_id = p_new_user_group_id
--             on conflict do update set grants_count = grants_count + 1;
--         end if;
--
--     else
--         delete from space_permission_user_group spug
--             using space_role_user_group srug, space s
--         where spug.permission_id = p_old_permission_id and
--             spug.user_group_id = srug.user_group_id and
--             spug.twin_id = srug.twin_id and
--             spug.twin_id = s.twin_id and s.permission_schema_id = p_old_permission_schema_id and
--             spug.space_role_id = p_old_space_role_id and
--             srug.space_role_id = p_new_space_role_id;
--     end if;
-- END;
-- $$;


create or replace function space_permission_user_update_by_user_group_map_type2_insert(p_new_user_group_id uuid, p_new_user_id uuid, p_new_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
  insert into space_permission_user (twin_id, permission_id, user_id, grants_count)
  select t.id, pgsr.permission_id, p_new_user_id  from space_role_user_group srug
         join space s on s.twin_id = srug.id
         join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
         join twin t on t.owner_business_account_id = p_new_business_account_id and t.id = srug.twin_id
  where srug.user_group_id = p_new_user_group_id
  on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function space_permission_user_update_by_user_group_map_type2_delete(p_old_user_group_id uuid, p_old_user_id uuid, p_old_business_account_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update space_permission_user set grants_count = grants_count - 1
    from space_role_user_group srug
      join space s on s.twin_id = srug.id
      join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
      join twin t on t.owner_business_account_id = p_old_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_old_user_group_id;
END;
$$;

-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function space_permission_user_update_by_permiss_grant_space_role_insert(p_new_permission_schema_id uuid, p_new_permission_schema_id uuid, p_new_space_role_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    --todo
  insert into space_permission_user (twin_id, permission_id, user_id, grants_count)
  select t.id, pgsr.permission_id, p_new_user_id  from space_role_user_group srug
         join space s on s.twin_id = srug.id
         join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
         join twin t on t.owner_business_account_id = p_new_business_account_id and t.id = srug.twin_id
  where srug.user_group_id = p_new_user_group_id
  on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function space_permission_user_update_by_permiss_grant_space_role_delete(p_old_permission_schema_id uuid, p_old_permission_schema_id uuid, p_old_space_role_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update space_permission_user set grants_count = grants_count - 1
    from space_role_user_group srug
      join space s on s.twin_id = srug.id
      join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
      join twin t on t.owner_business_account_id = p_old_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_old_user_group_id;
END;
$$;
-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function space_permission_user_update_by_space_role_user_group_insert(p_new_twin_id uuid, p_new_space_role_id uuid, p_new_user_group_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    --todo
    insert into space_permission_user (twin_id, permission_id, user_id, grants_count)
    select t.id, pgsr.permission_id, p_new_user_id  from space_role_user_group srug
                                                             join space s on s.twin_id = srug.id
                                                             join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
                                                             join twin t on t.owner_business_account_id = p_new_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_new_user_group_id
    on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function space_permission_user_update_by_space_role_user_group_delete(p_old_twin_id uuid, p_old_space_role_id uuid, p_old_user_group_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update space_permission_user set grants_count = grants_count - 1
    from space_role_user_group srug
             join space s on s.twin_id = srug.id
             join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
             join twin t on t.owner_business_account_id = p_old_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_old_user_group_id;
END;
$$;
-----------------------------------------------------------------
-----------------------------------------------------------------
create or replace function space_permission_user_update_by_space_role_user_insert(p_new_twin_id uuid, p_new_space_role_id uuid, p_new_user_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    --todo
    insert into space_permission_user (twin_id, permission_id, user_id, grants_count)
    select t.id, pgsr.permission_id, p_new_user_id  from space_role_user_group srug
                                                             join space s on s.twin_id = srug.id
                                                             join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
                                                             join twin t on t.owner_business_account_id = p_new_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_new_user_group_id
    on conflict do update set grants_count = grants_count + 1;
END;
$$;

create or replace function space_permission_user_update_by_space_role_user_delete(p_old_twin_id uuid, p_old_space_role_id uuid, p_old_user_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    update space_permission_user set grants_count = grants_count - 1
    from space_role_user_group srug
             join space s on s.twin_id = srug.id
             join permission_grant_space_role pgsr on pgsr.space_role_id = srug.space_role_id and pgsr.permission_schema_id = s.permission_schema_id
             join twin t on t.owner_business_account_id = p_old_business_account_id and t.id = srug.twin_id
    where srug.user_group_id = p_old_user_group_id;
END;
$$;
-----------------------------------------------------------------


create or replace function permission_grant_space_role_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_permiss_grant_space_role_insert(NEW.permission_schema_id, NEW.permission_id, NEW.space_role_id);
    RETURN NEW;
END;
$$;

create or replace function permission_grant_space_role_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS DISTINCT FROM OLD.permission_schema_id OR
       NEW.permission_id IS DISTINCT FROM OLD.permission_id OR
       NEW.space_role_id IS DISTINCT FROM OLD.space_role_id
       THEN
        PERFORM space_permission_user_update_by_permiss_grant_space_role_insert(NEW.permission_schema_id, NEW.permission_id, NEW.space_role_id);
        PERFORM space_permission_user_update_by_permiss_grant_space_role_delete(OLD.permission_schema_id, OLD.permission_id, OLD.space_role_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function permission_grant_space_role_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
        PERFORM space_permission_user_update_by_permiss_grant_space_role_delete(OLD.permission_schema_id, OLD.permission_id, OLD.space_role_id);
    RETURN OLD;
END;
$$;
------------------------------------------------------------------------------

create or replace function space_role_user_group_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_space_role_user_group_insert(NEW.twin_id, NEW.space_role_id, NEW.user_group_id);
    RETURN NEW;
END;
$$;

create or replace function space_role_user_group_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.twin_id IS DISTINCT FROM OLD.twin_id OR
       NEW.user_group_id IS DISTINCT FROM OLD.user_group_id OR
       NEW.space_role_id IS DISTINCT FROM OLD.space_role_id
    THEN
        PERFORM space_permission_user_update_by_space_role_user_group_insert(NEW.twin_id, NEW.space_role_id, NEW.user_group_id);
        PERFORM space_permission_user_update_by_space_role_user_group_delete(OLD.twin_id, OLD.space_role_id, OLD.user_group_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function space_role_user_group_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_space_role_user_group_delete(OLD.twin_id, OLD.space_role_id, OLD.user_group_id);
    RETURN OLD;
END;
$$;
------------------------------------------------------------------------------
create or replace function space_role_user_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_space_role_user_insert(NEW.twin_id, NEW.space_role_id, NEW.user_id);
    RETURN NEW;
END;
$$;

create or replace function space_role_user_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.twin_id IS DISTINCT FROM OLD.twin_id OR
       NEW.user_id IS DISTINCT FROM OLD.user_id OR
       NEW.space_role_id IS DISTINCT FROM OLD.space_role_id
    THEN
        PERFORM space_permission_user_update_by_space_role_user_insert(NEW.twin_id, NEW.space_role_id, NEW.user_id);
        PERFORM space_permission_user_update_by_space_role_user_delete(OLD.twin_id, OLD.space_role_id, OLD.user_id);
    END IF;

    RETURN NEW;
END;
$$;

create or replace function space_role_user_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_space_role_user_delete(OLD.twin_id, OLD.space_role_id, OLD.user_id);
    RETURN OLD;
END;
$$;
------------------------------------------------------------------------------
create or replace function user_group_map_type2_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_user_group_map_type2_insert(NEW.user_id, NEW.user_group_id, NEW.business_account_id);
    RETURN NEW;
END;
$$;


create or replace function user_group_map_type2_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.user_id IS DISTINCT FROM OLD.user_id OR
       NEW.user_group_id IS DISTINCT FROM OLD.user_group_id
    THEN
        PERFORM space_permission_user_update_by_user_group_map_type2_insert(NEW.user_id, NEW.user_group_id, NEW.business_account_id);
        PERFORM space_permission_user_update_by_user_group_map_type2_delete(OLD.user_id, OLD.user_group_id, OLD.business_account_id);
    END IF;
    RETURN NEW;
END;
$$;

create or replace function user_group_map_type2_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM space_permission_user_update_by_user_group_map_type2_delete(OLD.user_id, OLD.user_group_id, OLD.business_account_id);
    RETURN OLD;
END;
$$;

create or replace function user_group_map_type2_before_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.business_account_id IS DISTINCT FROM OLD.business_account_id THEN
        RAISE EXCEPTION 'It is forbidden to change the [business_account_id] field for table [user_group_map_type2]';
    END IF;
    RETURN NEW;
END;
$$;

----------------------------------------------------------------------------------
drop trigger if exists permission_grant_space_role_after_delete_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_delete_wrapper_trigger
    after delete
    on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_delete_wrapper();
drop trigger if exists permission_grant_space_role_after_insert_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_insert_wrapper_trigger
    after delete
    on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_insert_wrapper();
drop trigger if exists permission_grant_space_role_after_update_wrapper_trigger on permission_grant_space_role;
create trigger permission_grant_space_role_after_update_wrapper_trigger
    after delete
    on permission_grant_space_role
    for each row
execute procedure permission_grant_space_role_after_update_wrapper();
--------------------------------------------------
drop trigger if exists space_role_user_group_after_delete_wrapper_trigger on space_role_user_group;
create trigger space_role_user_group_after_delete_wrapper_trigger
    after delete
    on space_role_user_group
    for each row
execute procedure space_role_user_group_after_delete_wrapper();
drop trigger if exists space_role_user_group_after_insert_wrapper_trigger on space_role_user_group;
create trigger space_role_user_group_after_insert_wrapper_trigger
    after delete
    on space_role_user_group
    for each row
execute procedure space_role_user_group_after_insert_wrapper();
drop trigger if exists space_role_user_group_after_update_wrapper_trigger on space_role_user_group;
create trigger space_role_user_group_after_update_wrapper_trigger
    after delete
    on space_role_user_group
    for each row
execute procedure space_role_user_group_after_update_wrapper();
-----------------------------------------------------------------
drop trigger if exists space_role_user_after_delete_wrapper_trigger on space_role_user;
create trigger space_role_user_after_delete_wrapper_trigger
    after delete
    on space_role_user
    for each row
execute procedure space_role_user_after_delete_wrapper();
drop trigger if exists space_role_user_after_insert_wrapper_trigger on space_role_user;
create trigger space_role_user_after_insert_wrapper_trigger
    after delete
    on space_role_user
    for each row
execute procedure space_role_user_after_insert_wrapper();
drop trigger if exists space_role_user_after_update_wrapper_trigger on space_role_user;
create trigger space_role_user_after_update_wrapper_trigger
    after delete
    on space_role_user
    for each row
execute procedure space_role_user_after_update_wrapper();
-----------------------------------------------------------------
drop trigger if exists user_group_map_type2_after_delete_wrapper_trigger on user_group_map_type2;
create trigger user_group_map_type2_after_delete_wrapper_trigger
    after delete
    on user_group_map_type2
    for each row
execute procedure user_group_map_type2_after_delete_wrapper();
drop trigger if exists user_group_map_type2_after_insert_wrapper_trigger on user_group_map_type2;
create trigger user_group_map_type2_after_insert_wrapper_trigger
    after delete
    on user_group_map_type2
    for each row
execute procedure user_group_map_type2_after_insert_wrapper();
drop trigger if exists user_group_map_type2_after_update_wrapper_trigger on user_group_map_type2;
create trigger user_group_map_type2_after_update_wrapper_trigger
    after delete
    on user_group_map_type2
    for each row
execute procedure user_group_map_type2_after_update_wrapper();
drop trigger if exists user_group_map_type2_before_update_wrapper_trigger on user_group_map_type2;
create trigger user_group_map_type2_before_update_wrapper_trigger
    after delete
    on user_group_map_type2
    for each row
execute procedure user_group_map_type2_before_update_wrapper();
