--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4 (Ubuntu 16.4-1.pgdg22.04+2)
-- Dumped by pg_dump version 17.0 (Ubuntu 17.0-1.pgdg22.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- Name: ltree; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS ltree WITH SCHEMA public;


--
-- Name: EXTENSION ltree; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION ltree IS 'data type for hierarchical tree-like structures';


--
-- Name: access_order; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.access_order AS ENUM (
    'AllowDeny',
    'DenyAllow'
);


ALTER TYPE public.access_order OWNER TO postgres;

--
-- Name: access_rule; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.access_rule AS ENUM (
    'Allow',
    'Deny'
);


ALTER TYPE public.access_rule OWNER TO postgres;

--
-- Name: space_permissions; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.space_permissions AS (
	isspaceassignee boolean,
	isspacecreator boolean
);


ALTER TYPE public.space_permissions OWNER TO postgres;

--
-- Name: create_permission_id_detect(uuid, uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.create_permission_id_detect(head_twin_uuid uuid, business_account_uuid uuid, domain_uuid uuid, twin_class_uuid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    twin_class_schema_uuid UUID;
    create_permission_uuid UUID;
BEGIN
    twin_class_schema_uuid := twin_class_schema_id_detect(head_twin_uuid, business_account_uuid, domain_uuid);

    IF twin_class_schema_uuid IS NOT NULL THEN
        SELECT tcsm.create_permission_id INTO create_permission_uuid
        FROM twin_class_schema_map tcsm
        WHERE tcsm.twin_class_schema_id = twin_class_schema_uuid and tcsm.twin_class_id = twin_class_uuid
        LIMIT 1;
    END IF;

    -- return create permission id
    RETURN create_permission_uuid;
END;
$$;


ALTER FUNCTION public.create_permission_id_detect(head_twin_uuid uuid, business_account_uuid uuid, domain_uuid uuid, twin_class_uuid uuid) OWNER TO postgres;

--
-- Name: hierarchy_check_lquery(public.ltree, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_check_lquery(hierarchy_tree public.ltree, ltree_value text) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $$
BEGIN
    RETURN hierarchy_tree ~ ltree_value::lquery;
END;
$$;


ALTER FUNCTION public.hierarchy_check_lquery(hierarchy_tree public.ltree, ltree_value text) OWNER TO postgres;

--
-- Name: hierarchy_twin_class_extends_detect_tree(uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_extends_detect_tree(twin_class_id uuid) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
    hierarchy                             TEXT   := '';
    current_id                            UUID   := twin_class_id;
    parent_id                             UUID;
    visited_ids                           UUID[] := ARRAY [twin_class_id];
BEGIN
    RAISE NOTICE 'Detected extends hier. for twin class id: %', twin_class_id;
    -- return value init
    hierarchy := '';

    -- cycle for build hierarchy form twin-in to twin-root
    LOOP
        -- get parent_id and shema flags and check space_schema_id is present for twin-in
        SELECT tc.extends_twin_class_id INTO parent_id
        FROM public.twin_class tc WHERE tc.id = current_id;

        -- check for cycle
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %', twin_class_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN hierarchy;
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_extends_detect_tree(twin_class_id uuid) OWNER TO postgres;

--
-- Name: hierarchy_twin_class_extends_process_tree_update(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_extends_process_tree_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.extends_twin_class_id IS DISTINCT FROM NEW.extends_twin_class_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.extends_twin_class_id IS DISTINCT FROM NEW.extends_twin_class_id THEN
            PERFORM public.hierarchy_twin_class_extends_update_tree_soft(NEW.id, public.hierarchy_twin_class_extends_detect_tree(NEW.id));
        ELSE
            PERFORM public.hierarchy_twin_class_extends_update_tree_soft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.hierarchy_twin_class_extends_update_tree_hard(NEW.id, public.hierarchy_twin_class_extends_detect_tree(NEW.id));
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_extends_process_tree_update() OWNER TO postgres;

--
-- Name: hierarchy_twin_class_extends_update_tree_hard(uuid, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_extends_update_tree_hard(twin_class_id uuid, detected_hierarchy text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    hierarchy_to_use TEXT;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_extends_detect_tree(twin_class_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', hierarchy_to_use;

    -- update hier. and schemas for twin-class-in
    UPDATE public.twin_class SET extends_hierarchy_tree = text2ltree(hierarchy_to_use) WHERE id = twin_class_id;

    -- update hier. and schemas for twin-class-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin_class
        WHERE extends_twin_class_id = twin_class_id
        UNION ALL
        SELECT tc.id, d.depth + 1
        FROM public.twin_class tc INNER JOIN descendants d ON tc.extends_twin_class_id = d.id
        WHERE d.depth < 10
    )
    UPDATE public.twin_class tc
    SET extends_hierarchy_tree = text2ltree(public.hierarchy_twin_class_extends_detect_tree(tc.id))
    WHERE tc.id IN (SELECT id FROM descendants);
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_extends_update_tree_hard(twin_class_id uuid, detected_hierarchy text) OWNER TO postgres;

--
-- Name: hierarchy_twin_class_extends_update_tree_soft(uuid, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_extends_update_tree_soft(twin_class_id uuid, detected_hierarchy text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    hierarchy_to_use TEXT;
    old_hierarchy TEXT;
    new_hierarchy TEXT;
BEGIN
    SELECT extends_hierarchy_tree::text INTO old_hierarchy FROM public.twin_class WHERE id = twin_class_id;

    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_extends_detect_tree(twin_class_id);
    END IF;

    new_hierarchy := hierarchy_to_use;

    RAISE NOTICE 'NEW: %', new_hierarchy;
    RAISE NOTICE 'OLD: %', old_hierarchy;

    UPDATE public.twin_class
    SET extends_hierarchy_tree = text2ltree(replace(twin_class.extends_hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE extends_hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_extends_update_tree_soft(twin_class_id uuid, detected_hierarchy text) OWNER TO postgres;

--
-- Name: hierarchy_twin_class_head_detect_tree(uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_head_detect_tree(twin_class_id uuid) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
    hierarchy                             TEXT   := '';
    current_id                            UUID   := twin_class_id;
    parent_id                             UUID;
    visited_ids                           UUID[] := ARRAY [twin_class_id];
BEGIN
    RAISE NOTICE 'Detected head hier. for twin class id: %', twin_class_id;
    -- return value init
    hierarchy := '';

    -- cycle for build hierarchy form twin-in to twin-root
    LOOP
        -- get parent_id and shema flags and check space_schema_id is present for twin-in
        SELECT tc.head_twin_class_id INTO parent_id
        FROM public.twin_class tc WHERE tc.id = current_id;

        -- check for cycle
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %', twin_class_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN hierarchy;
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_head_detect_tree(twin_class_id uuid) OWNER TO postgres;

--
-- Name: hierarchy_twin_class_head_process_tree_update(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_head_process_tree_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.head_twin_class_id IS DISTINCT FROM NEW.head_twin_class_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.head_twin_class_id IS DISTINCT FROM NEW.head_twin_class_id THEN
            PERFORM public.hierarchy_twin_class_head_update_tree_soft(NEW.id, public.hierarchy_twin_class_head_detect_tree(NEW.id));
        ELSE
            PERFORM public.hierarchy_twin_class_head_update_tree_soft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.hierarchy_twin_class_head_update_tree_hard(NEW.id, public.hierarchy_twin_class_head_detect_tree(NEW.id));
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_head_process_tree_update() OWNER TO postgres;

--
-- Name: hierarchy_twin_class_head_update_tree_hard(uuid, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_head_update_tree_hard(twin_class_id uuid, detected_hierarchy text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    hierarchy_to_use TEXT;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_head_detect_tree(twin_class_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', hierarchy_to_use;

    -- update hier. and schemas for twin-class-in
    UPDATE public.twin_class SET head_hierarchy_tree = text2ltree(hierarchy_to_use) WHERE id = twin_class_id;

    -- update hier. and schemas for twin-class-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin_class
        WHERE head_twin_class_id = twin_class_id
        UNION ALL
        SELECT tc.id, d.depth + 1
        FROM public.twin_class tc INNER JOIN descendants d ON tc.head_twin_class_id = d.id
        WHERE d.depth < 10
    )
    UPDATE public.twin_class tc
    SET head_hierarchy_tree = text2ltree(public.hierarchy_twin_class_head_detect_tree(tc.id))
    WHERE tc.id IN (SELECT id FROM descendants);
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_head_update_tree_hard(twin_class_id uuid, detected_hierarchy text) OWNER TO postgres;

--
-- Name: hierarchy_twin_class_head_update_tree_soft(uuid, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchy_twin_class_head_update_tree_soft(twin_class_id uuid, detected_hierarchy text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    hierarchy_to_use TEXT;
    old_hierarchy TEXT;
    new_hierarchy TEXT;
BEGIN
    SELECT head_hierarchy_tree::text INTO old_hierarchy FROM public.twin_class WHERE id = twin_class_id;

    -- if hier. in params - use it. if not - detect it and use.
    IF detected_hierarchy IS NOT NULL THEN
        hierarchy_to_use := detected_hierarchy;
    ELSE
        hierarchy_to_use := public.hierarchy_twin_class_head_detect_tree(twin_class_id);
    END IF;

    new_hierarchy := hierarchy_to_use;

    RAISE NOTICE 'NEW: %', new_hierarchy;
    RAISE NOTICE 'OLD: %', old_hierarchy;

    UPDATE public.twin_class
    SET head_hierarchy_tree = text2ltree(replace(twin_class.head_hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE head_hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$;


ALTER FUNCTION public.hierarchy_twin_class_head_update_tree_soft(twin_class_id uuid, detected_hierarchy text) OWNER TO postgres;

--
-- Name: hierarchydetecttree(uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchydetecttree(twin_id uuid) RETURNS TABLE(hierarchy text, permission_schema_space_id uuid, twinflow_schema_space_id uuid, twin_class_schema_space_id uuid, alias_space_id uuid)
    LANGUAGE plpgsql
    AS $$
DECLARE
    current_id                            UUID   := twin_id;
    parent_id                             UUID;
    visited_ids                           UUID[] := ARRAY [twin_id];
    local_permission_schema_space_enabled BOOLEAN;
    local_twinflow_schema_space_enabled   BOOLEAN;
    local_twin_class_schema_space_enabled BOOLEAN;
    local_alias_space_enabled             BOOLEAN;

BEGIN
    RAISE NOTICE 'Detected hier. for id: %', twin_id;
    -- return values init
    hierarchy := '';
    permission_schema_space_id := NULL;
    twinflow_schema_space_id := NULL;
    twin_class_schema_space_id := NULL;
    alias_space_id := NULL;

    -- cycle for build hierarchy form twin-in to twin-root
    LOOP
        -- get parent_id and shema flags and check space_schema_id is present for twin-in
        SELECT t.head_twin_id,
               tc.permission_schema_space,
               tc.twinflow_schema_space,
               tc.twin_class_schema_space,
               tc.alias_space
        INTO parent_id,
            local_permission_schema_space_enabled,
            local_twinflow_schema_space_enabled,
            local_twin_class_schema_space_enabled,
            local_alias_space_enabled
        FROM public.twin t LEFT JOIN public.twin_class tc ON t.twin_class_id = tc.id WHERE t.id = current_id;

        -- check for cycle
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_id %', twin_id;
        END IF;

        -- update schema ids, if it is enabled on class and return value not null
        IF permission_schema_space_id IS NULL AND local_permission_schema_space_enabled IS TRUE THEN permission_schema_space_id := current_id;
        END IF;
        IF twinflow_schema_space_id IS NULL AND local_twinflow_schema_space_enabled IS TRUE THEN twinflow_schema_space_id := current_id;
        END IF;
        IF twin_class_schema_space_id IS NULL AND local_twin_class_schema_space_enabled IS TRUE THEN twin_class_schema_space_id := current_id;
        END IF;
        IF alias_space_id IS NULL AND local_alias_space_enabled IS TRUE THEN alias_space_id := current_id;
        END IF;

        -- replace - to _ for compatibility with ltree
        hierarchy :=
                replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        RAISE NOTICE 'Detected hier. for: %', hierarchy;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;
    RETURN QUERY SELECT hierarchy, permission_schema_space_id, twinflow_schema_space_id, twin_class_schema_space_id, alias_space_id;
END;
$$;


ALTER FUNCTION public.hierarchydetecttree(twin_id uuid) OWNER TO postgres;

--
-- Name: hierarchyprocesstreeupdate(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchyprocesstreeupdate() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
            PERFORM public.hierarchyUpdateTreeSoft(NEW.id, public.hierarchyDetectTree(NEW.id));
        ELSE
            PERFORM public.hierarchyUpdateTreeSoft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.hierarchyUpdateTreeHard(NEW.id, public.hierarchyDetectTree(NEW.id));
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.hierarchyprocesstreeupdate() OWNER TO postgres;

--
-- Name: hierarchyrecalculateforclasstwins(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchyrecalculateforclasstwins() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF OLD.permission_schema_space IS DISTINCT FROM NEW.permission_schema_space OR
       OLD.twinflow_schema_space IS DISTINCT FROM NEW.twinflow_schema_space OR
       OLD.twin_class_schema_space IS DISTINCT FROM NEW.twin_class_schema_space OR
       OLD.alias_space IS DISTINCT FROM NEW.alias_space
    THEN
        PERFORM public.hierarchyUpdateTreeHard(t.id, NULL)
        FROM public.twin t
        WHERE t.twin_class_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.hierarchyrecalculateforclasstwins() OWNER TO postgres;

--
-- Name: hierarchyupdatetreehard(uuid, record); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchyupdatetreehard(twin_id uuid, detect_data record) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    data_to_use RECORD;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detect_data IS NOT NULL THEN
        data_to_use := detect_data;
    ELSE
        data_to_use := public.hierarchyDetectTree(twin_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', data_to_use.hierarchy;

    -- update hier. and schemas for twin-in
    UPDATE public.twin
    SET hierarchy_tree = text2ltree(data_to_use.hierarchy),
        permission_schema_space_id = data_to_use.permission_schema_space_id,
        twinflow_schema_space_id = data_to_use.twinflow_schema_space_id,
        twin_class_schema_space_id = data_to_use.twin_class_schema_space_id,
        alias_space_id = data_to_use.alias_space_id
    WHERE id = twin_id;

    -- update hier. and schemas for twin-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin
        WHERE head_twin_id = twin_id
        UNION ALL
        SELECT t.id, d.depth + 1
        FROM public.twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
        WHERE d.depth < 10
    ), updated_data AS (
        SELECT dt.id, (public.hierarchyDetectTree(dt.id)).* -- use function and expand result
        FROM descendants dt
    )
    UPDATE public.twin t
    SET hierarchy_tree = text2ltree(ud.hierarchy),
        permission_schema_space_id = ud.permission_schema_space_id,
        twinflow_schema_space_id = ud.twinflow_schema_space_id,
        twin_class_schema_space_id = ud.twin_class_schema_space_id,
        alias_space_id = ud.alias_space_id
    FROM updated_data ud
    WHERE t.id = ud.id;
END;
$$;


ALTER FUNCTION public.hierarchyupdatetreehard(twin_id uuid, detect_data record) OWNER TO postgres;

--
-- Name: hierarchyupdatetreesoft(uuid, record); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hierarchyupdatetreesoft(twin_id uuid, detect_data record) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    data_to_use RECORD;
    old_hierarchy TEXT;
    new_hierarchy TEXT;
BEGIN
    RAISE NOTICE 'DATA: %', detect_data;

    SELECT hierarchy_tree::text INTO old_hierarchy FROM public.twin WHERE id = twin_id;

    -- if hier. in params - use it. if not - detect it and use.
    IF detect_data IS NOT NULL THEN
        data_to_use := detect_data;
    ELSE
        data_to_use := public.hierarchyDetectTree(twin_id);
    END IF;

    new_hierarchy := data_to_use.hierarchy;

    RAISE NOTICE 'NEW: %', new_hierarchy;
    RAISE NOTICE 'OLD: %', old_hierarchy;

    UPDATE public.twin
    SET hierarchy_tree = text2ltree(replace(hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$;


ALTER FUNCTION public.hierarchyupdatetreesoft(twin_id uuid, detect_data record) OWNER TO postgres;

--
-- Name: permission_check(uuid, uuid, uuid, uuid, uuid, uuid[], uuid, boolean, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
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
    IF permission_check_assignee_propagation(permissionSchemaId, permissionId, businessAccountId, spaceId, userId) THEN
        RETURN TRUE;
    END IF;

    -- Exit if spaceId is NULL, indicating no further hierarchy to check
    IF spaceId IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Check space-role and space-role-group permissions
    RETURN permissionCheckSpaceRolePermissions(permissionSchemaId, permissionId, spaceId, userId, userGroupIdList);

END;
$$;


ALTER FUNCTION public.permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionid uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean, iscreator boolean) OWNER TO postgres;

--
-- Name: permission_check(uuid, uuid, uuid, uuid, uuid, uuid, uuid[], uuid, boolean, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionidtwin uuid, permissionidtwinclass uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean DEFAULT false, iscreator boolean DEFAULT false) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    permissionIdForUse UUID := permissionIdTwin;
BEGIN
    IF permissionIdForUse IS NULL THEN
        permissionIdForUse = permissionIdTwinClass;
    END IF;
    RETURN permission_check(domainId, businessAccountId, spaceId, permissionIdForUse, userId, userGroupIdList, twinClassId, isAssignee, isCreator);
END;
$$;


ALTER FUNCTION public.permission_check(domainid uuid, businessaccountid uuid, spaceid uuid, permissionidtwin uuid, permissionidtwinclass uuid, userid uuid, usergroupidlist uuid[], twinclassid uuid, isassignee boolean, iscreator boolean) OWNER TO postgres;

--
-- Name: permission_check_assignee_propagation(uuid, uuid, uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permission_check_assignee_propagation(permissionschemaid uuid, permissionid uuid, businessaccountid uuid, spaceid uuid, userid uuid) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    twinClassId UUID;
    twinStatusId UUID;
    inSpaceOnly BOOLEAN;
BEGIN
    -- check rights by twin_class_id
    SELECT propagation_by_twin_class_id, in_space_only INTO twinClassId, inSpaceOnly FROM public.permission_schema_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_class_id IS NOT NULL
    LIMIT 1;

    -- check rights by twin_status_id
    SELECT propagation_by_twin_status_id, in_space_only INTO twinStatusId, inSpaceOnly FROM public.permission_schema_assignee_propagation
    WHERE permission_schema_id = permissionSchemaId AND permission_id = permissionId AND propagation_by_twin_status_id IS NOT NULL
    LIMIT 1;

    IF twinStatusId IS NOT NULL THEN
        -- if twin_status_id exists, check twin exists with assignee current user
        IF inSpaceOnly THEN
            PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_status_id = twinStatusId AND owner_business_account_id = businessAccountId AND permission_schema_space_id = spaceId LIMIT 1;
        ELSE
            PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_status_id = twinStatusId AND owner_business_account_id = businessAccountId LIMIT 1;
        END IF;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;

    IF twinClassId IS NOT NULL THEN
        -- if twin_class_id exists, check twin exists with assignee current user
        IF inSpaceOnly THEN
            PERFORM 1 FROM public.twin WHERE assigner_user_id = userId AND twin_class_id = twinClassId AND owner_business_account_id = businessAccountId AND permission_schema_space_id = spaceId LIMIT 1;
        ELSE
            PERFORM 1
            FROM public.twin WHERE assigner_user_id = userId AND twin_class_id = twinClassId AND owner_business_account_id = businessAccountId LIMIT 1;
        END IF;
        IF FOUND THEN
            RETURN TRUE;
        END IF;
    END IF;

    RETURN FALSE;
END;
$$;


ALTER FUNCTION public.permission_check_assignee_propagation(permissionschemaid uuid, permissionid uuid, businessaccountid uuid, spaceid uuid, userid uuid) OWNER TO postgres;

--
-- Name: permissioncheckbyschema(uuid, uuid, uuid, uuid[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permissioncheckbyschema(permissionschemaid uuid, permissionidforuse uuid, userid uuid, usergroupidlist uuid[]) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    userPermissionExists INT;
    groupPermissionExists INT;
BEGIN

    SELECT COUNT(id) INTO userPermissionExists
    FROM permission_schema_user
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionIdForUse
      AND user_id = userId;

    IF userPermissionExists > 0 THEN
        RETURN TRUE;
    END IF;

    SELECT COUNT(id) INTO groupPermissionExists
    FROM permission_schema_user_group
    WHERE permission_schema_id = permissionSchemaId
      AND permission_id = permissionIdForUse
      AND user_group_id = ANY(userGroupIdList);

    RETURN groupPermissionExists > 0;
END;
$$;


ALTER FUNCTION public.permissioncheckbyschema(permissionschemaid uuid, permissionidforuse uuid, userid uuid, usergroupidlist uuid[]) OWNER TO postgres;

--
-- Name: permissioncheckspaceassigneeandcreator(uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permissioncheckspaceassigneeandcreator(spaceid uuid, userid uuid) RETURNS public.space_permissions
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    result space_permissions := (FALSE, FALSE);
BEGIN
    IF spaceId IS NULL THEN
        RETURN result;
    END IF;
    SELECT
        (t.assigner_user_id = userId) AS isSpaceAssignee,
        (t.created_by_user_id = userId) AS isSpaceCreator
    INTO result FROM twin t WHERE t.id = spaceId;
    RETURN result;
END;
$$;


ALTER FUNCTION public.permissioncheckspaceassigneeandcreator(spaceid uuid, userid uuid) OWNER TO postgres;

--
-- Name: permissioncheckspacerolepermissions(uuid, uuid, uuid, uuid, uuid[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permissioncheckspacerolepermissions(permissionschemaid uuid, permissionid uuid, spaceid uuid, userid uuid, usergroupidlist uuid[]) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    userAssignedToRoleExists INT;
    groupAssignedToRoleExists INT;
BEGIN
    -- Check if any space role assigned to the user has the given permission
    SELECT COUNT(sru.id)
    INTO userAssignedToRoleExists
    FROM space_role_user sru
    WHERE sru.twin_id = spaceId
      AND sru.user_id = userId
      AND sru.space_role_id IN (SELECT space_role_id FROM permissionGetRoles(permissionSchemaId, permissionId));

    IF userAssignedToRoleExists > 0 THEN
        RETURN TRUE;
    END IF;

    -- Check if any space role assigned to the user's group has the given permission
    SELECT COUNT(srug.id)
    INTO groupAssignedToRoleExists
    FROM space_role_user_group srug
    WHERE srug.twin_id = spaceId
      AND srug.user_group_id = ANY (userGroupIdList)
      AND srug.space_role_id IN (SELECT space_role_id FROM permissionGetRoles(permissionSchemaId, permissionId));

    RETURN groupAssignedToRoleExists > 0;
END;
$$;


ALTER FUNCTION public.permissioncheckspacerolepermissions(permissionschemaid uuid, permissionid uuid, spaceid uuid, userid uuid, usergroupidlist uuid[]) OWNER TO postgres;

--
-- Name: permissionchecktwinrole(uuid, uuid, character varying[], uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permissionchecktwinrole(permissionschemaid uuid, permissionid uuid, roles character varying[], twinclassid uuid) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    hasPermission BOOLEAN := FALSE;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM permission_schema_twin_role
        WHERE permission_schema_id = permissionSchemaId
          AND permission_id = permissionId
          AND twin_class_id = twinClassId
          AND twin_role_id = ANY(roles)
    ) INTO hasPermission;

    RETURN hasPermission;
END;
$$;


ALTER FUNCTION public.permissionchecktwinrole(permissionschemaid uuid, permissionid uuid, roles character varying[], twinclassid uuid) OWNER TO postgres;

--
-- Name: permissiondetectschema(uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permissiondetectschema(domainid uuid, businessaccountid uuid, spaceid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
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


ALTER FUNCTION public.permissiondetectschema(domainid uuid, businessaccountid uuid, spaceid uuid) OWNER TO postgres;

--
-- Name: permissiongetroles(uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.permissiongetroles(permissionschemaid uuid, permissionid uuid) RETURNS TABLE(space_role_id uuid)
    LANGUAGE sql IMMUTABLE
    AS $$
SELECT pssr.space_role_id
FROM permission_schema_space_roles pssr
WHERE pssr.permission_schema_id = permissionSchemaId
  AND pssr.permission_id = permissionId;
$$;


ALTER FUNCTION public.permissiongetroles(permissionschemaid uuid, permissionid uuid) OWNER TO postgres;

--
-- Name: twin_class_schema_id_detect(uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.twin_class_schema_id_detect(head_twin_uuid uuid, business_account_uuid uuid, domain_uuid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    twin_class_schema_uuid UUID;
BEGIN
    -- check if head_twin_id not NULL, try to find twin_class_schema_id in head-twin space
    IF head_twin_uuid IS NOT NULL THEN
        SELECT twin_class_schema_id into twin_class_schema_uuid from space where twin_id = head_twin_uuid
        LIMIT 1;
    END IF;

    -- if schema_id not found by head_twin_id, check in domain_business_account
    IF twin_class_schema_uuid IS NULL AND business_account_uuid IS NOT NULL AND domain_uuid IS NOT NULL THEN
        SELECT twin_class_schema_id into twin_class_schema_uuid
        FROM domain_business_account
        where domain_id = domain_uuid
        AND business_account_id = business_account_uuid
        LIMIT 1;
    END IF;

    -- if schema not found in first two cases, find it in domain
    IF twin_class_schema_uuid IS NULL THEN
        SELECT d.twin_class_schema_id INTO twin_class_schema_uuid
        FROM domain d WHERE d.id = domain_uuid LIMIT 1;
    END IF;

    -- return create permission id
    RETURN twin_class_schema_uuid;
END;
$$;


ALTER FUNCTION public.twin_class_schema_id_detect(head_twin_uuid uuid, business_account_uuid uuid, domain_uuid uuid) OWNER TO postgres;

--
-- Name: twinflowdetect(uuid, uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.twinflowdetect(domainid uuid, businessaccountid uuid, spaceid uuid, twinclassid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    twinflowSchemaId UUID;
BEGIN
    IF twinClassId IS NULL THEN
        RETURN NULL;
    END IF;

    -- Detect twinflow schema
    twinflowSchemaId := twinflowDetectSchema(domainId, businessAccountId, spaceId);
    IF twinflowSchemaId IS NULL THEN
        RETURN NULL;
    END IF;

    RETURN twinflowDetectHierarchical(twinflowSchemaId, twinClassId);
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


ALTER FUNCTION public.twinflowdetect(domainid uuid, businessaccountid uuid, spaceid uuid, twinclassid uuid) OWNER TO postgres;

--
-- Name: twinflowdetecthierarchical(uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.twinflowdetecthierarchical(twinflowschemaid uuid, twinclassid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    detectedTwinflow UUID;
    currentTwinClassId uuid := twinClassId;
    extendsTwinClassId UUID;
    visitedClassIds UUID[] := '{}';
BEGIN
    IF twinClassId IS NULL OR twinflowSchemaId IS NULL THEN
        RETURN NULL;
    END IF;

    -- cycle loop twin class extends hierarchy from child to parent
    LOOP
        IF currentTwinClassId = ANY(visitedClassIds) THEN
            RAISE EXCEPTION 'Cycle detected in hierarchy for twin_class_id %', extendsTwinClassId;
        END IF;

        SELECT twinflow_id INTO detectedTwinflow FROM twinflow_schema_map
        WHERE twin_class_id = currentTwinClassId AND twinflow_schema_id = twinflowSchemaId;
        IF detectedTwinflow IS NOT NULL THEN
            RETURN detectedTwinflow;
        END IF;

        SELECT extends_twin_class_id INTO extendsTwinClassId FROM twin_class WHERE id = currentTwinClassId;
        EXIT WHEN extendsTwinClassId IS NULL;

        -- add currentTwinClassId to visitedClassIds before moving to the next twin_class
        visitedClassIds := array_append(visitedClassIds, currentTwinClassId);
        currentTwinClassId := extendsTwinClassId;
    END LOOP;

EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


ALTER FUNCTION public.twinflowdetecthierarchical(twinflowschemaid uuid, twinclassid uuid) OWNER TO postgres;

--
-- Name: twinflowdetectschema(uuid, uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.twinflowdetectschema(domainid uuid, businessaccountid uuid, spaceid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in space
    IF spaceId IS NOT NULL THEN
        SELECT twinflow_schema_id INTO schemaId FROM space WHERE twin_id = spaceId;
        IF FOUND THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- twin in BA
    schemaId := twinflowDetectSchemaByBusinessAccount(domainId, businessAccountId);
    IF schemaId IS NOT NULL THEN
        RETURN schemaId;
    END IF;

    -- return domain schema, if twin not in space, and not in BA
    RETURN twinflowDetectSchemaByDomain(domainId);
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


ALTER FUNCTION public.twinflowdetectschema(domainid uuid, businessaccountid uuid, spaceid uuid) OWNER TO postgres;

--
-- Name: twinflowdetectschemabybusinessaccount(uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.twinflowdetectschemabybusinessaccount(domainid uuid, businessaccountid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT twinflow_schema_id
        INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId
          AND business_account_id = businessAccountId;
    END IF;
    RETURN schemaId;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


ALTER FUNCTION public.twinflowdetectschemabybusinessaccount(domainid uuid, businessaccountid uuid) OWNER TO postgres;

--
-- Name: twinflowdetectschemabydomain(uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.twinflowdetectschemabydomain(domainid uuid) RETURNS uuid
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in domain
    IF domainId IS NOT NULL THEN
        SELECT twinflow_schema_id INTO schemaId FROM domain WHERE id = domainId;
    END IF;
    RETURN schemaId;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$;


ALTER FUNCTION public.twinflowdetectschemabydomain(domainid uuid) OWNER TO postgres;

--
-- Name: user_insert_or_update(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.user_insert_or_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Inserting a new record into twin when adding a record to user
    IF TG_OP = 'INSERT' THEN
        INSERT INTO twin (id, twin_class_id, twin_status_id, name, created_by_user_id, assigner_user_id, created_at)
        VALUES (
                   NEW.id,
                   '00000000-0000-0000-0001-000000000001',
                   '00000000-0000-0000-0003-000000000001',
                   COALESCE(NEW.name, ''),
                   '00000000-0000-0000-0000-000000000000',
                   NEW.id,
                   NEW.created_at
               )
        ON CONFLICT (id) DO NOTHING;

        -- Updating name field in twin when updating a record in user
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE twin
        SET name = COALESCE(NEW.name, '')
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.user_insert_or_update() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: business_account; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.business_account (
    id uuid NOT NULL,
    name character varying,
    owner_user_group_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.business_account OWNER TO postgres;

--
-- Name: business_account_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.business_account_user (
    id uuid NOT NULL,
    business_account_id uuid,
    user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.business_account_user OWNER TO postgres;

--
-- Name: card; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card (
    id uuid NOT NULL,
    logo character varying,
    card_layout_id uuid NOT NULL,
    key character varying NOT NULL,
    name_i18n_id uuid NOT NULL
);


ALTER TABLE public.card OWNER TO postgres;

--
-- Name: card_access; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_access (
    id uuid NOT NULL,
    twin_class_id uuid,
    "order" integer NOT NULL,
    card_id uuid NOT NULL
);


ALTER TABLE public.card_access OWNER TO postgres;

--
-- Name: card_layout; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_layout (
    id uuid NOT NULL,
    channel_id character varying(20) NOT NULL,
    key character varying(49) NOT NULL
);


ALTER TABLE public.card_layout OWNER TO postgres;

--
-- Name: card_layout_position; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_layout_position (
    id uuid NOT NULL,
    card_layout_id uuid NOT NULL,
    key character varying(30)
);


ALTER TABLE public.card_layout_position OWNER TO postgres;

--
-- Name: card_override; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_override (
    id uuid NOT NULL,
    override_card_id uuid NOT NULL,
    override_for_channel_id character varying(20) NOT NULL,
    override_eclipse boolean DEFAULT false,
    logo character varying,
    name_i18n_id uuid,
    card_layout_id uuid
);


ALTER TABLE public.card_override OWNER TO postgres;

--
-- Name: card_widget; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_widget (
    id uuid NOT NULL,
    card_id uuid NOT NULL,
    card_layout_position_id uuid NOT NULL,
    in_position_order integer DEFAULT 1,
    name character varying NOT NULL,
    color character varying,
    widget_id uuid NOT NULL,
    widget_data_grabber_params public.hstore
);


ALTER TABLE public.card_widget OWNER TO postgres;

--
-- Name: card_widget_override; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_widget_override (
    id uuid NOT NULL,
    override_card_widget_id uuid NOT NULL,
    override_for_channel_id character varying(20) NOT NULL,
    override_eclipse boolean DEFAULT false,
    card_layout_position_id uuid,
    in_position_order integer,
    name character varying,
    color character varying,
    widget_data_grabber_params public.hstore
);


ALTER TABLE public.card_widget_override OWNER TO postgres;

--
-- Name: channel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel (
    id character varying(20) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.channel OWNER TO postgres;

--
-- Name: data_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.data_list (
    id uuid NOT NULL,
    name character varying NOT NULL,
    description character varying,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    domain_id uuid NOT NULL,
    key character varying,
    attribute_1_key character varying,
    attribute_2_key character varying,
    attribute_3_key character varying,
    attribute_4_key character varying
);


ALTER TABLE public.data_list OWNER TO postgres;

--
-- Name: data_list_option; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.data_list_option (
    id uuid NOT NULL,
    data_list_id uuid NOT NULL,
    option character varying,
    option_i18n_id uuid,
    icon character varying,
    disabled boolean DEFAULT false,
    attribute_1_value character varying,
    attribute_2_value character varying,
    attribute_3_value character varying,
    attribute_4_value character varying,
    data_list_option_status_id character varying NOT NULL,
    business_account_id uuid,
    "order" smallint
);


ALTER TABLE public.data_list_option OWNER TO postgres;

--
-- Name: data_list_option_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.data_list_option_status (
    id character varying NOT NULL
);


ALTER TABLE public.data_list_option_status OWNER TO postgres;

--
-- Name: domain; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.domain (
    id uuid NOT NULL,
    key character varying(100),
    business_account_initiator_featurer_id integer,
    business_account_initiator_params public.hstore,
    description character varying(255),
    token_handler_featurer_id integer,
    token_handler_params public.hstore,
    user_group_manager_featurer_id integer,
    user_group_manager_params public.hstore,
    permission_schema_id uuid,
    twinflow_schema_id uuid,
    twin_class_schema_id uuid,
    business_account_template_twin_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    default_i18n_locale_id character varying,
    ancestor_twin_class_id uuid NOT NULL,
    domain_type_id character varying NOT NULL,
    alias_counter integer DEFAULT 0
);


ALTER TABLE public.domain OWNER TO postgres;

--
-- Name: domain_business_account; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.domain_business_account (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    business_account_id uuid NOT NULL,
    permission_schema_id uuid,
    twinflow_schema_id uuid,
    twin_class_schema_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.domain_business_account OWNER TO postgres;

--
-- Name: domain_locale; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.domain_locale (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    i18n_locale_id character varying NOT NULL,
    icon character varying,
    active boolean DEFAULT true
);


ALTER TABLE public.domain_locale OWNER TO postgres;

--
-- Name: domain_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.domain_type (
    id character varying NOT NULL,
    name character varying(255),
    description character varying(255),
    domain_initiator_featurer_id integer NOT NULL,
    domain_initiator_params public.hstore,
    default_token_handler_featurer_id integer,
    default_token_handler_params public.hstore,
    default_user_group_manager_featurer_id integer,
    default_user_group_manager_params public.hstore
);


ALTER TABLE public.domain_type OWNER TO postgres;

--
-- Name: domain_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.domain_user (
    id uuid NOT NULL,
    domain_id uuid,
    user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    i18n_locale_id character varying
);


ALTER TABLE public.domain_user OWNER TO postgres;

--
-- Name: error; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.error (
    id uuid NOT NULL,
    code_local integer,
    code_external character varying(255),
    name character varying(40),
    description character varying(255),
    client_msg_i18n_id uuid NOT NULL
);


ALTER TABLE public.error OWNER TO postgres;

--
-- Name: featurer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.featurer (
    id integer NOT NULL,
    featurer_type_id integer NOT NULL,
    class character varying NOT NULL,
    name character varying NOT NULL,
    description character varying(255),
    deprecated boolean DEFAULT false NOT NULL
);


ALTER TABLE public.featurer OWNER TO postgres;

--
-- Name: featurer_injection; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.featurer_injection (
    id uuid NOT NULL,
    injector_featurer_id integer NOT NULL,
    injector_params public.hstore,
    description character varying(255)
);


ALTER TABLE public.featurer_injection OWNER TO postgres;

--
-- Name: featurer_param; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.featurer_param (
    featurer_id integer NOT NULL,
    injectable boolean,
    "order" integer NOT NULL,
    key character varying(40) NOT NULL,
    name character varying(40) NOT NULL,
    description character varying(255),
    featurer_param_type_id character varying(40)
);


ALTER TABLE public.featurer_param OWNER TO postgres;

--
-- Name: featurer_param_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.featurer_param_type (
    id character varying(40) NOT NULL,
    regexp character varying(255) NOT NULL,
    example character varying(255) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.featurer_param_type OWNER TO postgres;

--
-- Name: featurer_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.featurer_type (
    id integer NOT NULL,
    name character varying(40) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.featurer_type OWNER TO postgres;

--
-- Name: history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actor_user_id uuid NOT NULL,
    history_type_id character varying NOT NULL,
    twin_class_field_id uuid,
    context jsonb,
    snapshot_message text,
    history_batch_id uuid
);


ALTER TABLE public.history OWNER TO postgres;

--
-- Name: history_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history_type (
    id character varying NOT NULL,
    snapshot_message_template text,
    history_type_status_id character varying
);


ALTER TABLE public.history_type OWNER TO postgres;

--
-- Name: history_type_config_domain; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history_type_config_domain (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    history_type_id character varying NOT NULL,
    history_type_status_id character varying NOT NULL,
    snapshot_message_template text,
    message_template_i18n_id uuid
);


ALTER TABLE public.history_type_config_domain OWNER TO postgres;

--
-- Name: history_type_config_twin_class; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history_type_config_twin_class (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    history_type_id character varying NOT NULL,
    history_type_status_id character varying NOT NULL,
    snapshot_message_template text,
    message_template_i18n_id uuid
);


ALTER TABLE public.history_type_config_twin_class OWNER TO postgres;

--
-- Name: history_type_config_twin_class_field; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history_type_config_twin_class_field (
    id uuid NOT NULL,
    twin_class_field_id uuid NOT NULL,
    history_type_id character varying NOT NULL,
    history_type_status_id character varying NOT NULL,
    snapshot_message_template text,
    message_template_i18n_id uuid
);


ALTER TABLE public.history_type_config_twin_class_field OWNER TO postgres;

--
-- Name: history_type_domain_template; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history_type_domain_template (
    id uuid NOT NULL,
    history_type_id character varying NOT NULL,
    domain_id uuid NOT NULL,
    snapshot_message_template text,
    history_type_status_id character varying NOT NULL
);


ALTER TABLE public.history_type_domain_template OWNER TO postgres;

--
-- Name: history_type_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.history_type_status (
    id character varying NOT NULL,
    description character varying
);


ALTER TABLE public.history_type_status OWNER TO postgres;

--
-- Name: i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.i18n (
    id uuid NOT NULL,
    name character varying(255) DEFAULT NULL::character varying,
    key character varying(255),
    i18n_type_id character varying NOT NULL
);


ALTER TABLE public.i18n OWNER TO postgres;

--
-- Name: i18n_locale; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.i18n_locale (
    locale character varying(2) NOT NULL,
    name character varying(20) DEFAULT ''::character varying NOT NULL,
    active boolean DEFAULT true NOT NULL,
    native_name character varying,
    icon character varying
);


ALTER TABLE public.i18n_locale OWNER TO postgres;

--
-- Name: i18n_translation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.i18n_translation (
    i18n_id uuid NOT NULL,
    locale character varying(2) NOT NULL,
    translation text,
    usage_counter integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.i18n_translation OWNER TO postgres;

--
-- Name: i18n_translation_bin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.i18n_translation_bin (
    i18n_id uuid NOT NULL,
    locale character varying(2) DEFAULT 'en'::character varying,
    translation bytea
);


ALTER TABLE public.i18n_translation_bin OWNER TO postgres;

--
-- Name: i18n_translation_style; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.i18n_translation_style (
    id uuid NOT NULL,
    i18n_id uuid NOT NULL,
    locale character varying(2) DEFAULT NULL::character varying,
    start_index integer NOT NULL,
    end_index integer NOT NULL,
    color character varying(30),
    size character varying(10),
    link character varying(255)
);


ALTER TABLE public.i18n_translation_style OWNER TO postgres;

--
-- Name: i18n_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.i18n_type (
    id character varying NOT NULL,
    name character varying(255)
);


ALTER TABLE public.i18n_type OWNER TO postgres;

--
-- Name: i18n_type_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.i18n_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.i18n_type_id_seq OWNER TO postgres;

--
-- Name: i18n_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.i18n_type_id_seq OWNED BY public.i18n_type.id;


--
-- Name: link; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    src_twin_class_id uuid NOT NULL,
    dst_twin_class_id uuid NOT NULL,
    forward_name_i18n_id uuid NOT NULL,
    backward_name_i18n_id uuid NOT NULL,
    link_type_id character varying NOT NULL,
    created_by_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    link_strength_id character varying(255) DEFAULT 'OPTIONAL'::character varying
);


ALTER TABLE public.link OWNER TO postgres;

--
-- Name: twin_class; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_class (
    id uuid NOT NULL,
    domain_id uuid,
    key character varying(100),
    permission_schema_space boolean DEFAULT false,
    abstract boolean DEFAULT false,
    head_twin_class_id uuid,
    extends_twin_class_id uuid,
    name_i18n_id uuid,
    description_i18n_id uuid,
    logo character varying,
    created_by_user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    twin_class_owner_type_id character varying,
    domain_alias_counter integer DEFAULT 0,
    marker_data_list_id uuid,
    tag_data_list_id uuid,
    twinflow_schema_space boolean DEFAULT false,
    twin_class_schema_space boolean DEFAULT false,
    alias_space boolean DEFAULT false,
    view_permission_id uuid,
    head_hierarchy_tree public.ltree,
    extends_hierarchy_tree public.ltree,
    head_hunter_featurer_id integer DEFAULT 2601,
    head_hunter_featurer_params public.hstore
);


ALTER TABLE public.twin_class OWNER TO postgres;

--
-- Name: link_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.link_lazy AS
 SELECT l.id,
    l.domain_id,
    l.src_twin_class_id,
    l.dst_twin_class_id,
    l.forward_name_i18n_id,
    l.backward_name_i18n_id,
    l.link_type_id,
    l.created_by_user_id,
    l.created_at,
    l.link_strength_id,
    tc.key AS fk_src_twin_class_key,
    tc2.key AS fk_dst_twin_class_key
   FROM ((public.link l
     LEFT JOIN public.twin_class tc ON ((l.src_twin_class_id = tc.id)))
     LEFT JOIN public.twin_class tc2 ON ((l.dst_twin_class_id = tc2.id)));


ALTER VIEW public.link_lazy OWNER TO postgres;

--
-- Name: link_strength; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link_strength (
    id character varying(255) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.link_strength OWNER TO postgres;

--
-- Name: link_tree; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link_tree (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    name character varying NOT NULL,
    root_twin_class_id uuid NOT NULL,
    created_by_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.link_tree OWNER TO postgres;

--
-- Name: link_tree_node; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link_tree_node (
    id uuid NOT NULL,
    link_tree_id uuid NOT NULL,
    depth integer DEFAULT 1 NOT NULL,
    link_id uuid NOT NULL
);


ALTER TABLE public.link_tree_node OWNER TO postgres;

--
-- Name: link_trigger; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link_trigger (
    id uuid NOT NULL,
    link_id uuid,
    "order" integer DEFAULT 1 NOT NULL,
    link_trigger_featurer_id integer NOT NULL,
    link_trigger_params public.hstore
);


ALTER TABLE public.link_trigger OWNER TO postgres;

--
-- Name: link_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link_type (
    id character varying NOT NULL
);


ALTER TABLE public.link_type OWNER TO postgres;

--
-- Name: link_validator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.link_validator (
    id uuid NOT NULL,
    link_id uuid NOT NULL,
    "order" integer DEFAULT 1,
    link_validator_featurer_id integer NOT NULL,
    link_validator_params public.hstore
);


ALTER TABLE public.link_validator OWNER TO postgres;

--
-- Name: permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission (
    id uuid NOT NULL,
    key character varying(100),
    permission_group_id uuid NOT NULL,
    name character varying(100),
    description character varying(255)
);


ALTER TABLE public.permission OWNER TO postgres;

--
-- Name: permission_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_group (
    id uuid NOT NULL,
    domain_id uuid,
    twin_class_id uuid,
    key character varying(100),
    name character varying,
    description character varying
);


ALTER TABLE public.permission_group OWNER TO postgres;

--
-- Name: permission_schema; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_schema (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    business_account_id uuid,
    name character varying NOT NULL,
    description character varying,
    created_by_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.permission_schema OWNER TO postgres;

--
-- Name: permission_schema_assignee_propagation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_schema_assignee_propagation (
    id uuid NOT NULL,
    permission_schema_id uuid,
    permission_id uuid,
    propagation_by_twin_class_id uuid,
    propagation_by_twin_status_id uuid,
    granted_by_user_id uuid,
    granted_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    in_space_only boolean DEFAULT false
);


ALTER TABLE public.permission_schema_assignee_propagation OWNER TO postgres;

--
-- Name: permission_schema_space_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_schema_space_roles (
    id uuid NOT NULL,
    permission_schema_id uuid NOT NULL,
    permission_id uuid NOT NULL,
    space_role_id uuid NOT NULL,
    granted_by_user_id uuid NOT NULL,
    granted_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.permission_schema_space_roles OWNER TO postgres;

--
-- Name: permission_schema_twin_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_schema_twin_role (
    id uuid NOT NULL,
    permission_schema_id uuid,
    permission_id uuid,
    twin_class_id uuid,
    twin_role_id character varying,
    granted_by_user_id uuid,
    granted_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.permission_schema_twin_role OWNER TO postgres;

--
-- Name: permission_schema_twin_role_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.permission_schema_twin_role_lazy AS
 SELECT pstr.id,
    pstr.permission_schema_id,
    ps.name AS fk_permission_schema_name,
    pstr.permission_id,
    p.key AS fk_permission_key,
    pstr.twin_class_id,
    tc.key AS fk_twin_class_key,
    pstr.twin_role_id,
    pstr.granted_by_user_id,
    pstr.granted_at
   FROM (((public.permission_schema_twin_role pstr
     LEFT JOIN public.permission_schema ps ON ((pstr.permission_schema_id = ps.id)))
     LEFT JOIN public.permission p ON ((pstr.permission_id = p.id)))
     LEFT JOIN public.twin_class tc ON ((pstr.twin_class_id = tc.id)));


ALTER VIEW public.permission_schema_twin_role_lazy OWNER TO postgres;

--
-- Name: permission_schema_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_schema_user (
    id uuid NOT NULL,
    permission_schema_id uuid NOT NULL,
    permission_id uuid NOT NULL,
    user_id uuid NOT NULL,
    granted_by_user_id uuid NOT NULL,
    granted_at timestamp without time zone
);


ALTER TABLE public.permission_schema_user OWNER TO postgres;

--
-- Name: permission_schema_user_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permission_schema_user_group (
    id uuid NOT NULL,
    permission_schema_id uuid NOT NULL,
    permission_id uuid NOT NULL,
    user_group_id uuid NOT NULL,
    granted_by_user_id uuid NOT NULL,
    granted_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.permission_schema_user_group OWNER TO postgres;

--
-- Name: user_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group (
    id uuid NOT NULL,
    domain_id uuid,
    business_account_id uuid,
    name character varying(100) NOT NULL,
    description character varying(255),
    user_group_type_id character varying NOT NULL
);


ALTER TABLE public.user_group OWNER TO postgres;

--
-- Name: permission_schema_user_group_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.permission_schema_user_group_lazy AS
 SELECT psug.id,
    psug.permission_schema_id,
    ps.name AS fk_permission_schema_name,
    psug.permission_id,
    p.key AS fk_permission_key,
    psug.user_group_id,
    ug.name AS fk_user_group_name,
    psug.granted_by_user_id,
    psug.granted_at
   FROM (((public.permission_schema_user_group psug
     LEFT JOIN public.permission_schema ps ON ((psug.permission_schema_id = ps.id)))
     LEFT JOIN public.permission p ON ((psug.permission_id = p.id)))
     LEFT JOIN public.user_group ug ON ((psug.user_group_id = ug.id)));


ALTER VIEW public.permission_schema_user_group_lazy OWNER TO postgres;

--
-- Name: search; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search (
    id uuid NOT NULL,
    name character varying,
    search_alias_id uuid,
    permission_id uuid,
    description character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    head_twin_search_id uuid
);


ALTER TABLE public.search OWNER TO postgres;

--
-- Name: search_alias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search_alias (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    alias character varying NOT NULL,
    search_detector_featurer_id integer DEFAULT 2801 NOT NULL,
    search_detector_params public.hstore
);


ALTER TABLE public.search_alias OWNER TO postgres;

--
-- Name: search_field; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search_field (
    id character varying NOT NULL
);


ALTER TABLE public.search_field OWNER TO postgres;

--
-- Name: search_predicate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search_predicate (
    id uuid NOT NULL,
    search_id uuid,
    search_field_id character varying,
    search_criteria_builder_featurer_id integer NOT NULL,
    search_criteria_builder_params public.hstore,
    exclude boolean DEFAULT false,
    description character varying(255)
);


ALTER TABLE public.search_predicate OWNER TO postgres;

--
-- Name: space; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.space (
    twin_id uuid NOT NULL,
    key character varying(100) NOT NULL,
    permission_schema_id uuid,
    twinflow_schema_id uuid,
    twin_class_schema_id uuid,
    alias_counter integer DEFAULT 0,
    domain_alias_counter integer DEFAULT 0,
    business_account_alias_counter integer DEFAULT 0
);


ALTER TABLE public.space OWNER TO postgres;

--
-- Name: space_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.space_role (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    business_account_id uuid,
    key character varying,
    name_i18n_id uuid NOT NULL,
    description_i18n_id uuid
);


ALTER TABLE public.space_role OWNER TO postgres;

--
-- Name: space_role_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.space_role_user (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    space_role_id uuid NOT NULL,
    user_id uuid NOT NULL,
    created_by_user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.space_role_user OWNER TO postgres;

--
-- Name: space_role_user_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.space_role_user_group (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    space_role_id uuid NOT NULL,
    user_group_id uuid NOT NULL,
    created_by_user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.space_role_user_group OWNER TO postgres;

--
-- Name: touch; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.touch (
    id character varying(50) NOT NULL
);


ALTER TABLE public.touch OWNER TO postgres;

--
-- Name: twin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    head_twin_id uuid,
    external_id character varying(100),
    twin_status_id uuid NOT NULL,
    name character varying(256) NOT NULL,
    description text,
    created_by_user_id uuid NOT NULL,
    assigner_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    owner_business_account_id uuid,
    owner_user_id uuid,
    hierarchy_tree public.ltree,
    view_permission_id uuid,
    permission_schema_space_id uuid,
    twinflow_schema_space_id uuid,
    twin_class_schema_space_id uuid,
    alias_space_id uuid
);


ALTER TABLE public.twin OWNER TO postgres;

--
-- Name: twin_action; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_action (
    id character varying(255) NOT NULL
);


ALTER TABLE public.twin_action OWNER TO postgres;

--
-- Name: twin_action_permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_action_permission (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    twin_action_id character varying(255) NOT NULL,
    permission_id uuid NOT NULL
);


ALTER TABLE public.twin_action_permission OWNER TO postgres;

--
-- Name: twin_action_validator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_action_validator (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    twin_action_id character varying(255) NOT NULL,
    "order" integer DEFAULT 1,
    twin_validator_featurer_id integer NOT NULL,
    twin_validator_params public.hstore,
    invert boolean DEFAULT false NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.twin_action_validator OWNER TO postgres;

--
-- Name: twin_alias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_alias (
    id uuid NOT NULL,
    alias_value character varying(255) NOT NULL,
    twin_alias_type_id character varying(1) NOT NULL,
    twin_id uuid,
    user_id uuid,
    domain_id uuid,
    business_account_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twin_alias OWNER TO postgres;

--
-- Name: twin_alias_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_alias_type (
    alias_type_id character(1) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.twin_alias_type OWNER TO postgres;

--
-- Name: twin_attachment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_attachment (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    twinflow_transition_id uuid,
    storage_link character varying(255) NOT NULL,
    view_permission_id uuid,
    created_by_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    external_id character varying,
    title character varying,
    description character varying,
    twin_comment_id uuid,
    twin_class_field_id uuid
);


ALTER TABLE public.twin_attachment OWNER TO postgres;

--
-- Name: twin_business_account_alias_counter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_business_account_alias_counter (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    business_account_id uuid NOT NULL,
    alias_counter integer NOT NULL
);


ALTER TABLE public.twin_business_account_alias_counter OWNER TO postgres;

--
-- Name: twin_class_field; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_class_field (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    key character varying(100) NOT NULL,
    name_i18n_id uuid,
    description_i18n_id uuid,
    field_typer_featurer_id integer NOT NULL,
    field_typer_params public.hstore,
    view_permission_id uuid,
    edit_permission_id uuid,
    required boolean DEFAULT false
);


ALTER TABLE public.twin_class_field OWNER TO postgres;

--
-- Name: twin_class_field_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twin_class_field_lazy AS
 SELECT tcf.id,
    tcf.twin_class_id,
    tcf.key,
    tcf.name_i18n_id,
    tcf.description_i18n_id,
    tcf.field_typer_featurer_id,
    tcf.field_typer_params,
    tcf.view_permission_id,
    tcf.edit_permission_id,
    tcf.required,
    tc.key AS fk_twin_class_key,
    fr.name AS fk_field_typer_name
   FROM ((public.twin_class_field tcf
     LEFT JOIN public.twin_class tc ON ((tcf.twin_class_id = tc.id)))
     LEFT JOIN public.featurer fr ON ((tcf.field_typer_featurer_id = fr.id)));


ALTER VIEW public.twin_class_field_lazy OWNER TO postgres;

--
-- Name: twin_class_owner_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_class_owner_type (
    id character varying NOT NULL
);


ALTER TABLE public.twin_class_owner_type OWNER TO postgres;

--
-- Name: twin_class_schema; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_class_schema (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    name character varying NOT NULL,
    description character varying,
    created_by_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twin_class_schema OWNER TO postgres;

--
-- Name: twin_class_schema_map; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_class_schema_map (
    id uuid NOT NULL,
    twin_class_schema_id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    create_permission_id uuid
);


ALTER TABLE public.twin_class_schema_map OWNER TO postgres;

--
-- Name: twin_comment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_comment (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    text text NOT NULL,
    created_by_user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    changed_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twin_comment OWNER TO postgres;

--
-- Name: twin_comment_action; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_comment_action (
    id character varying(20) NOT NULL
);


ALTER TABLE public.twin_comment_action OWNER TO postgres;

--
-- Name: twin_comment_action_alien_permission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_comment_action_alien_permission (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    twin_comment_action_id character varying(20) NOT NULL,
    permission_id uuid NOT NULL
);


ALTER TABLE public.twin_comment_action_alien_permission OWNER TO postgres;

--
-- Name: twin_comment_action_alien_validator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_comment_action_alien_validator (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    twin_comment_action_id character varying(20) NOT NULL,
    "order" integer DEFAULT 1,
    twin_validator_featurer_id integer NOT NULL,
    twin_validator_params public.hstore,
    invert boolean DEFAULT false NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.twin_comment_action_alien_validator OWNER TO postgres;

--
-- Name: twin_comment_action_self; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_comment_action_self (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    restrict_twin_comment_action_id character varying(20) NOT NULL
);


ALTER TABLE public.twin_comment_action_self OWNER TO postgres;

--
-- Name: twin_factory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory (
    id uuid NOT NULL,
    key character varying NOT NULL,
    domain_id uuid,
    name_i18n_id uuid,
    description_i18n_id uuid
);


ALTER TABLE public.twin_factory OWNER TO postgres;

--
-- Name: twin_factory_condition; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory_condition (
    id uuid NOT NULL,
    twin_factory_condition_set_id uuid NOT NULL,
    conditioner_featurer_id integer NOT NULL,
    conditioner_params public.hstore,
    invert boolean DEFAULT false,
    active boolean DEFAULT true,
    description character varying
);


ALTER TABLE public.twin_factory_condition OWNER TO postgres;

--
-- Name: twin_factory_condition_set; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory_condition_set (
    id uuid NOT NULL,
    name character varying,
    description character varying
);


ALTER TABLE public.twin_factory_condition_set OWNER TO postgres;

--
-- Name: twin_factory_multiplier; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory_multiplier (
    id uuid NOT NULL,
    twin_factory_id uuid NOT NULL,
    input_twin_class_id uuid NOT NULL,
    multiplier_featurer_id integer NOT NULL,
    multiplier_params public.hstore,
    comment character varying
);


ALTER TABLE public.twin_factory_multiplier OWNER TO postgres;

--
-- Name: twin_factory_multiplier_filter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory_multiplier_filter (
    id uuid NOT NULL,
    twin_factory_multiplier_id uuid,
    twin_factory_condition_set_id uuid,
    twin_factory_condition_invert boolean,
    active boolean,
    comment character varying(255)
);


ALTER TABLE public.twin_factory_multiplier_filter OWNER TO postgres;

--
-- Name: twin_factory_pipeline; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory_pipeline (
    id uuid NOT NULL,
    twin_factory_id uuid NOT NULL,
    input_twin_class_id uuid NOT NULL,
    twin_factory_condition_set_id uuid,
    twin_factory_condition_invert boolean DEFAULT false,
    active boolean DEFAULT true,
    output_twin_status_id uuid,
    template_twin_id uuid,
    next_twin_factory_id uuid,
    description character varying
);


ALTER TABLE public.twin_factory_pipeline OWNER TO postgres;

--
-- Name: twin_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_status (
    id uuid NOT NULL,
    twins_class_id uuid NOT NULL,
    name_i18n_id uuid,
    description_i18n_id uuid,
    logo character varying,
    background_color character varying,
    key character varying,
    font_color character varying
);


ALTER TABLE public.twin_status OWNER TO postgres;

--
-- Name: twin_factory_pipeline_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twin_factory_pipeline_lazy AS
 SELECT tw_fact_pipe.id,
    tw_fact_pipe.twin_factory_id,
    tw_fact_pipe.input_twin_class_id,
    tw_class.key AS fk_twin_factory_pipeline_class_key,
    tw_fact_pipe.twin_factory_condition_set_id,
    tf_condition_set.name AS fk_twin_factory_condition_set_name,
    tw_fact_pipe.twin_factory_condition_invert,
    tw_fact_pipe.active,
    tw_fact_pipe.next_twin_factory_id,
    tw_fact_pipe.template_twin_id,
    tw_fact_pipe.description,
    tw_fact_pipe.output_twin_status_id,
    tw_fact.key AS fk_twin_factory_key,
    next_tw_factory.key AS fk_nex_twin_factory_key
   FROM (((((public.twin_factory tw_fact
     JOIN public.twin_factory_pipeline tw_fact_pipe ON ((tw_fact_pipe.twin_factory_id = tw_fact.id)))
     JOIN public.twin_class tw_class ON ((tw_fact_pipe.input_twin_class_id = tw_class.id)))
     LEFT JOIN public.twin_factory next_tw_factory ON ((tw_fact_pipe.next_twin_factory_id = next_tw_factory.id)))
     LEFT JOIN public.twin_factory_condition_set tf_condition_set ON ((tw_fact_pipe.twin_factory_condition_set_id = tf_condition_set.id)))
     LEFT JOIN public.twin_status tw_stat ON ((tw_fact_pipe.output_twin_status_id = tw_stat.id)));


ALTER VIEW public.twin_factory_pipeline_lazy OWNER TO postgres;

--
-- Name: twin_factory_pipeline_step; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_factory_pipeline_step (
    id uuid NOT NULL,
    twin_factory_pipeline_id uuid NOT NULL,
    "order" integer DEFAULT 0 NOT NULL,
    twin_factory_condition_set_id uuid,
    twin_factory_condition_invert boolean DEFAULT false,
    active boolean DEFAULT true,
    filler_featurer_id integer NOT NULL,
    filler_params public.hstore,
    comment character varying,
    optional boolean DEFAULT false
);


ALTER TABLE public.twin_factory_pipeline_step OWNER TO postgres;

--
-- Name: twin_factory_pipeline_step_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twin_factory_pipeline_step_lazy AS
 SELECT tw_fact_pipe_step.id,
    tw_fact_pipe_step.twin_factory_pipeline_id,
    tw_fact_pipe.description AS fk_twin_factory_pipeline_comment,
    tf_condition_set2.name AS fk_twin_factory_pipeline_condition_set_name,
    tw_class.key AS fk_twin_factory_pipeline_class_key,
    tw_fact_pipe_step.twin_factory_condition_set_id,
    tf_condition_set.name AS fk_twin_factory_pipeline_step_condition_set_name,
    tw_fact_pipe_step.twin_factory_condition_invert,
    tw_fact_pipe_step."order",
    tw_fact_pipe_step.active,
    tw_fact_pipe_step.optional,
    tw_fact_pipe_step.filler_featurer_id,
    tw_fact_pipe_step.filler_params,
    tw_fact_pipe_step.comment,
    tw_fact.key AS fk_twin_factory_key
   FROM ((((((public.twin_factory tw_fact
     JOIN public.twin_factory_pipeline tw_fact_pipe ON ((tw_fact_pipe.twin_factory_id = tw_fact.id)))
     JOIN public.twin_class tw_class ON ((tw_fact_pipe.input_twin_class_id = tw_class.id)))
     JOIN public.twin_factory_pipeline_step tw_fact_pipe_step ON ((tw_fact_pipe.id = tw_fact_pipe_step.twin_factory_pipeline_id)))
     LEFT JOIN public.twin_factory next_tw_factory ON ((tw_fact_pipe.next_twin_factory_id = next_tw_factory.id)))
     LEFT JOIN public.twin_factory_condition_set tf_condition_set ON ((tw_fact_pipe_step.twin_factory_condition_set_id = tf_condition_set.id)))
     LEFT JOIN public.twin_factory_condition_set tf_condition_set2 ON ((tw_fact_pipe.twin_factory_condition_set_id = tf_condition_set2.id)));


ALTER VIEW public.twin_factory_pipeline_step_lazy OWNER TO postgres;

--
-- Name: twin_field_data_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_field_data_list (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    twin_class_field_id uuid NOT NULL,
    data_list_option_id uuid NOT NULL
);


ALTER TABLE public.twin_field_data_list OWNER TO postgres;

--
-- Name: twin_field_simple; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_field_simple (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    twin_class_field_id uuid NOT NULL,
    value text
);


ALTER TABLE public.twin_field_simple OWNER TO postgres;

--
-- Name: twin_field_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_field_user (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    twin_class_field_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.twin_field_user OWNER TO postgres;

--
-- Name: twin_link; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_link (
    id uuid NOT NULL,
    src_twin_id uuid NOT NULL,
    dst_twin_id uuid NOT NULL,
    link_id uuid NOT NULL,
    created_by_user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twin_link OWNER TO postgres;

--
-- Name: twin_marker; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_marker (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    marker_data_list_option_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twin_marker OWNER TO postgres;

--
-- Name: twin_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_role (
    id character varying NOT NULL
);


ALTER TABLE public.twin_role OWNER TO postgres;

--
-- Name: twin_status_group; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_status_group (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    key character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying
);


ALTER TABLE public.twin_status_group OWNER TO postgres;

--
-- Name: twin_status_group_map; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_status_group_map (
    id uuid NOT NULL,
    twin_status_id uuid NOT NULL,
    twin_status_group_id uuid NOT NULL
);


ALTER TABLE public.twin_status_group_map OWNER TO postgres;

--
-- Name: twin_status_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twin_status_lazy AS
 SELECT ts.id,
    ts.key,
    ts.twins_class_id,
    ts.name_i18n_id,
    ts.description_i18n_id,
    ts.logo,
    ts.background_color AS color,
    tc.key AS fk_twin_class_key,
    i1.translation AS fk_name_i18n_translation
   FROM ((public.twin_status ts
     LEFT JOIN public.twin_class tc ON ((ts.twins_class_id = tc.id)))
     LEFT JOIN public.i18n_translation i1 ON ((ts.name_i18n_id = i1.i18n_id)));


ALTER VIEW public.twin_status_lazy OWNER TO postgres;

--
-- Name: twin_status_transition_trigger; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_status_transition_trigger (
    id uuid NOT NULL,
    twin_status_id uuid,
    twin_status_transition_type_id character varying,
    "order" integer DEFAULT 1 NOT NULL,
    transition_trigger_featurer_id integer NOT NULL,
    transition_trigger_params public.hstore,
    active boolean DEFAULT true
);


ALTER TABLE public.twin_status_transition_trigger OWNER TO postgres;

--
-- Name: twin_status_transition_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_status_transition_type (
    id character varying NOT NULL
);


ALTER TABLE public.twin_status_transition_type OWNER TO postgres;

--
-- Name: twin_tag; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_tag (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    tag_data_list_option_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twin_tag OWNER TO postgres;

--
-- Name: twin_touch; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_touch (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    touch_id character varying NOT NULL,
    user_id uuid NOT NULL,
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public.twin_touch OWNER TO postgres;

--
-- Name: twin_work; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twin_work (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    logged_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    author_user_id uuid NOT NULL,
    minutes_spent integer NOT NULL
);


ALTER TABLE public.twin_work OWNER TO postgres;

--
-- Name: twinflow; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow (
    id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    created_by_user_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    initial_twin_status_id uuid NOT NULL,
    name_i18n_id uuid,
    description_i18n_id uuid
);


ALTER TABLE public.twinflow OWNER TO postgres;

--
-- Name: twinflow_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twinflow_lazy AS
 SELECT tf.id,
    tf.twin_class_id,
    tf.created_by_user_id,
    tf.created_at,
    tf.initial_twin_status_id,
    tc.key AS fk_twin_class_key,
    i1.translation AS fk_status_name_i18n_translation
   FROM public.twinflow tf,
    public.twin_class tc,
    public.twin_status ts,
    public.i18n_translation i1
  WHERE ((tf.twin_class_id = tc.id) AND (tf.initial_twin_status_id = ts.id) AND (ts.name_i18n_id = i1.i18n_id));


ALTER VIEW public.twinflow_lazy OWNER TO postgres;

--
-- Name: twinflow_schema; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow_schema (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    business_account_id uuid,
    name character varying NOT NULL,
    descrption character varying,
    created_by_user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.twinflow_schema OWNER TO postgres;

--
-- Name: twinflow_schema_map; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow_schema_map (
    id uuid NOT NULL,
    twinflow_schema_id uuid NOT NULL,
    twin_class_id uuid NOT NULL,
    twinflow_id uuid
);


ALTER TABLE public.twinflow_schema_map OWNER TO postgres;

--
-- Name: twinflow_schema_map_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twinflow_schema_map_lazy AS
 SELECT tfsm.id,
    tfsm.twinflow_schema_id,
    tfsm.twin_class_id,
    tfsm.twinflow_id,
    tfs.name AS fk_twinflow_schema_name,
    tc.key AS fk_twin_class_key
   FROM public.twinflow tf,
    public.twin_class tc,
    public.twinflow_schema tfs,
    public.twinflow_schema_map tfsm
  WHERE ((tfsm.twin_class_id = tc.id) AND (tfsm.twinflow_id = tf.id) AND (tfsm.twinflow_schema_id = tfs.id));


ALTER VIEW public.twinflow_schema_map_lazy OWNER TO postgres;

--
-- Name: twinflow_transition; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow_transition (
    id uuid NOT NULL,
    twinflow_id uuid NOT NULL,
    name_i18n_id uuid NOT NULL,
    src_twin_status_id uuid,
    dst_twin_status_id uuid NOT NULL,
    screen_id uuid,
    permission_id uuid,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id uuid,
    allow_comment boolean DEFAULT false,
    allow_attachments boolean DEFAULT false,
    allow_links boolean DEFAULT false,
    inbuilt_twin_factory_id uuid,
    drafting_twin_factory_id uuid,
    twinflow_transition_alias_id uuid,
    description_i18n_id uuid
);


ALTER TABLE public.twinflow_transition OWNER TO postgres;

--
-- Name: twinflow_transition_alias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow_transition_alias (
    id uuid NOT NULL,
    domain_id uuid NOT NULL,
    alias character varying NOT NULL
);


ALTER TABLE public.twinflow_transition_alias OWNER TO postgres;

--
-- Name: twinflow_transition_lazy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.twinflow_transition_lazy AS
 SELECT tft.id,
    tft.twinflow_id,
    tc.key AS fk_twinflow_twinclass_key,
    tft.name_i18n_id,
    tft.src_twin_status_id,
    tft.dst_twin_status_id,
    tft.screen_id,
    tft.permission_id,
    psn.key,
    tft.created_at,
    tft.created_by_user_id,
    tft.allow_comment,
    tft.allow_attachments,
    tft.allow_links,
    tft.inbuilt_twin_factory_id,
    tft.drafting_twin_factory_id,
    tfta.alias AS twinflow_transition_alias,
    tc2.key AS fk_src_status_twinclass_key,
    tc3.key AS fk_dst_status_twinclass_key,
    ts1.key AS fk_src_status_name,
    ts2.key AS fk_dst_status_name
   FROM ((((((((public.twinflow_transition tft
     LEFT JOIN public.twinflow tf ON ((tft.twinflow_id = tf.id)))
     LEFT JOIN public.twin_class tc ON ((tf.twin_class_id = tc.id)))
     LEFT JOIN public.permission psn ON ((tft.permission_id = psn.id)))
     LEFT JOIN public.twin_status ts2 ON ((tft.dst_twin_status_id = ts2.id)))
     LEFT JOIN public.twin_class tc3 ON ((ts2.twins_class_id = tc3.id)))
     LEFT JOIN public.twin_status ts1 ON ((tft.src_twin_status_id = ts1.id)))
     LEFT JOIN public.twin_class tc2 ON ((ts1.twins_class_id = tc2.id)))
     LEFT JOIN public.twinflow_transition_alias tfta ON ((tft.twinflow_transition_alias_id = tfta.id)));


ALTER VIEW public.twinflow_transition_lazy OWNER TO postgres;

--
-- Name: twinflow_transition_trigger; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow_transition_trigger (
    id uuid NOT NULL,
    twinflow_transition_id uuid,
    "order" integer DEFAULT 1 NOT NULL,
    transition_trigger_featurer_id integer NOT NULL,
    transition_trigger_params public.hstore,
    active boolean DEFAULT true
);


ALTER TABLE public.twinflow_transition_trigger OWNER TO postgres;

--
-- Name: twinflow_transition_validator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twinflow_transition_validator (
    id uuid NOT NULL,
    twinflow_transition_id uuid NOT NULL,
    "order" integer DEFAULT 1,
    twin_validator_featurer_id integer NOT NULL,
    twin_validator_params public.hstore,
    invert boolean DEFAULT false,
    active boolean DEFAULT true
);


ALTER TABLE public.twinflow_transition_validator OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."user" (
    id uuid NOT NULL,
    name character varying,
    email character varying,
    avatar character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    user_status_id character varying DEFAULT 'ACTIVE'::character varying NOT NULL
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- Name: user_group_map; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group_map (
    id uuid NOT NULL,
    user_group_id uuid NOT NULL,
    business_account_id uuid,
    user_id uuid NOT NULL,
    added_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    added_by_user_id uuid
);


ALTER TABLE public.user_group_map OWNER TO postgres;

--
-- Name: user_group_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_group_type (
    id character varying NOT NULL,
    name character varying(255),
    slugger_featurer_id integer NOT NULL,
    slugger_params public.hstore
);


ALTER TABLE public.user_group_type OWNER TO postgres;

--
-- Name: user_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_status (
    id character varying(50) NOT NULL
);


ALTER TABLE public.user_status OWNER TO postgres;

--
-- Name: widget; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.widget (
    id uuid NOT NULL,
    key character varying,
    name character varying NOT NULL,
    description character varying,
    widget_data_grabber_featurer_id integer NOT NULL,
    widget_accessor_featurer_id integer,
    widget_accessor_params public.hstore
);


ALTER TABLE public.widget OWNER TO postgres;

--
-- Name: i18n_type id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_type ALTER COLUMN id SET DEFAULT nextval('public.i18n_type_id_seq'::regclass);


--
-- Data for Name: business_account; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: business_account_user; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card_access; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card_layout; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card_layout_position; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card_override; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card_widget; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: card_widget_override; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: channel; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.channel VALUES ('WEB', NULL);


--
-- Data for Name: data_list; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: data_list_option; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: data_list_option_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.data_list_option_status VALUES ('active');
INSERT INTO public.data_list_option_status VALUES ('disabled');
INSERT INTO public.data_list_option_status VALUES ('hidden');


--
-- Data for Name: domain; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: domain_business_account; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: domain_locale; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: domain_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.domain_type VALUES ('basic', 'Basic', 'Single level domain. With no business account', 2501, NULL, 1901, NULL, 2101, NULL);
INSERT INTO public.domain_type VALUES ('b2b', 'B2B', 'Double level domain. With business accounts support', 2502, NULL, 1901, NULL, 2101, NULL);


--
-- Data for Name: domain_user; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: error; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: featurer; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.featurer VALUES (1001, 10, 'org.cambium.featurer.injectors.InjectorImpl', 'InjectorImpl', '', false);
INSERT INTO public.featurer VALUES (1301, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperTextField', 'FieldTyperTextField', '', false);
INSERT INTO public.featurer VALUES (1302, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperDateScroll', 'FieldTyperDateScroll', '', false);
INSERT INTO public.featurer VALUES (1303, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperUrl', 'FieldTyperUrl', '', false);
INSERT INTO public.featurer VALUES (1304, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperColorPicker', 'FieldTyperColorPicker', '', false);
INSERT INTO public.featurer VALUES (1306, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperCheckbox', 'FieldTyperCheckbox', '', false);
INSERT INTO public.featurer VALUES (1305, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperSelect', 'FieldTyperSelect', '', false);
INSERT INTO public.featurer VALUES (1401, 14, 'org.twins.core.featurer.widget.accessor.WidgetAccessorAllowAny', 'WidgetAccessorAllowAny', '', false);
INSERT INTO public.featurer VALUES (1402, 14, 'org.twins.core.featurer.widget.accessor.WidgetAccessorAllowForKeys', 'WidgetAccessorAllowForKeys', '', false);
INSERT INTO public.featurer VALUES (1403, 14, 'org.twins.core.featurer.widget.accessor.WidgetAccessorAllowForSpace', 'WidgetAccessorAllowForSpace', '', false);
INSERT INTO public.featurer VALUES (1404, 14, 'org.twins.core.featurer.widget.accessor.WidgetAccessorDenyForKeys', 'WidgetAccessorDenyForKeys', '', false);
INSERT INTO public.featurer VALUES (1101, 11, 'org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiatorFromParams', 'BusinessAccountInitiatorFromParams', '', false);
INSERT INTO public.featurer VALUES (1901, 19, 'org.twins.core.featurer.tokenhandler.TokenHandlerStub', 'TokenHandlerStub', '', false);
INSERT INTO public.featurer VALUES (1308, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInBusinessAccount', 'FieldTyperBusinessAccountSharedSelect', '', false);
INSERT INTO public.featurer VALUES (1307, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInDomain', 'FieldTyperSharedSelectInDomain', '', false);
INSERT INTO public.featurer VALUES (1309, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInHead', 'FieldTyperSharedSelectInHead', '', false);
INSERT INTO public.featurer VALUES (2002, 20, 'org.twins.core.featurer.usergroup.slugger.SluggerDomainScopeBusinessAccountManage', 'SluggerDomainScopeBusinessAccountManage', '', false);
INSERT INTO public.featurer VALUES (2001, 20, 'org.twins.core.featurer.usergroup.slugger.SluggerDomainScopeDomainManage', 'SluggerDomainScopeDomainManage', '', false);
INSERT INTO public.featurer VALUES (2003, 20, 'org.twins.core.featurer.usergroup.slugger.SluggerBusinessAccountScopeBusinessAccountManage', 'SluggerBusinessAccountScopeBusinessAccountManage', '', false);
INSERT INTO public.featurer VALUES (2101, 21, 'org.twins.core.featurer.usergroup.manager.UserGroupManagerImpl', 'UserGroupManagerImpl', '', false);
INSERT INTO public.featurer VALUES (2102, 21, 'org.twins.core.featurer.usergroup.manager.UserGroupManagerSingleGroup', 'UserGroupManagerSingleGroup', '', false);
INSERT INTO public.featurer VALUES (1501, 15, 'org.twins.core.featurer.transition.trigger.TransitionTriggerDuplicateTwin', 'TransitionTriggerDuplicateTwin', '', false);
INSERT INTO public.featurer VALUES (1102, 11, 'org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiatorFromParamsPostCreate', 'BusinessAccountInitiatorFromParams', '', false);
INSERT INTO public.featurer VALUES (1310, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperLink', 'FieldTyperLink', '', false);
INSERT INTO public.featurer VALUES (2308, 23, 'org.twins.core.featurer.factory.filler.FillerBackwardLinksAsContextTwin', 'FillerBackwardLinksAsContextTwin', '', false);
INSERT INTO public.featurer VALUES (2304, 23, 'org.twins.core.featurer.factory.filler.FillerBackwardLinksFromContextTwinAll', 'FillerBackwardLinksFromContextTwinAll', '', false);
INSERT INTO public.featurer VALUES (2310, 23, 'org.twins.core.featurer.factory.filler.FillerFieldsFromContextAll', 'FillerFieldsFromContextAll', '', false);
INSERT INTO public.featurer VALUES (2309, 23, 'org.twins.core.featurer.factory.filler.FillerFieldsFromTemplateTwinAll', 'FillerFieldsFromTemplateTwinAll', '', false);
INSERT INTO public.featurer VALUES (2305, 23, 'org.twins.core.featurer.factory.filler.FillerForwardLinksFromContextTwin', 'FillerForwardLinksFromContextTwin', '', false);
INSERT INTO public.featurer VALUES (2303, 23, 'org.twins.core.featurer.factory.filler.FillerForwardLinksFromContextTwinAll', 'FillerForwardLinksFromContextTwinAll', '', false);
INSERT INTO public.featurer VALUES (2307, 23, 'org.twins.core.featurer.factory.filler.FillerForwardLinksFromTemplateTwinAll', 'FillerForwardLinksFromTemplateTwinAll', '', false);
INSERT INTO public.featurer VALUES (2301, 23, 'org.twins.core.featurer.factory.filler.FillerHeadAsContextTwin', 'FillerHeadAsContextTwin', '', false);
INSERT INTO public.featurer VALUES (2302, 23, 'org.twins.core.featurer.factory.filler.FillerHeadFromContextTwinHead', 'FillerHeadFromContextTwinHead', '', false);
INSERT INTO public.featurer VALUES (2306, 23, 'org.twins.core.featurer.factory.filler.FillerHeadFromTemplateTwinHead', 'FillerHeadFromTemplateTwinHead', '', false);
INSERT INTO public.featurer VALUES (2201, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierAggregate', 'MultiplierAggregate', 'Only one output twin, even for multiple input.  Output class from params', false);
INSERT INTO public.featurer VALUES (2202, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolated', 'MultiplierIsolated', 'New output twin for each input. Output class from params', false);
INSERT INTO public.featurer VALUES (2203, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedOnContextField', 'MultiplierIsolatedOnContextField', 'New output twin for each input. Output class is selected by checking if given twinClassField is present in context', false);
INSERT INTO public.featurer VALUES (1311, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperUser', 'FieldTyperUser', '', false);
INSERT INTO public.featurer VALUES (2204, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedShiftHead', 'MultiplierIsolatedShiftHead', 'Output twin for each input. Output twin will be loaded from head', false);
INSERT INTO public.featurer VALUES (2401, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinFieldValueEquals', 'ConditionerContextTwinFieldValueEquals', '', false);
INSERT INTO public.featurer VALUES (2314, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinField', 'FillerBasicsAssigneeFromContextTwinField', '', false);
INSERT INTO public.featurer VALUES (2312, 23, 'org.twins.core.featurer.factory.filler.FillerFieldFromContextField', 'FillerFieldFromContextField', '', false);
INSERT INTO public.featurer VALUES (2311, 23, 'org.twins.core.featurer.factory.filler.FillerFieldFromContextTwinField', 'FillerFieldFromContextTwinField', '', false);
INSERT INTO public.featurer VALUES (2315, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextField', 'FillerBasicsAssigneeFromContextField', '', false);
INSERT INTO public.featurer VALUES (2402, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextFieldValueEquals', 'ConditionerContextFieldValueEquals', '', false);
INSERT INTO public.featurer VALUES (2316, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinAssignee', 'FillerBasicsAssigneeFromContextTwinAssignee', '', false);
INSERT INTO public.featurer VALUES (2313, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinCreatedBy', 'FillerBasicsAssigneeFromContextTwinCreatedBy', '', false);
INSERT INTO public.featurer VALUES (2317, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeNull', 'FillerBasicsAssigneeNull', '', false);
INSERT INTO public.featurer VALUES (2403, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerApiUserHasPermission', 'ConditionerApiUserHasPermission', '', false);
INSERT INTO public.featurer VALUES (2405, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinMarkerEquals', 'ConditionerContextTwinMarkerEquals', '', false);
INSERT INTO public.featurer VALUES (2318, 23, 'org.twins.core.featurer.factory.filler.FillerMarkerAdd', 'FillerMarkerAdd', '', false);
INSERT INTO public.featurer VALUES (2319, 23, 'org.twins.core.featurer.factory.filler.FillerMarkerDelete', 'FillerMarkerDelete', '', false);
INSERT INTO public.featurer VALUES (2327, 23, 'org.twins.core.featurer.factory.filler.FillerTwinBasicFieldsFromContextBasics', 'FillerTwinBasicFieldsFromContextBasics', '', false);
INSERT INTO public.featurer VALUES (2205, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedRelativesByHead', 'MultiplierIsolatedRelativesByHead', 'Output list of twin relatives for each input. Output twin list will be loaded by head and filtered by statusIds', false);
INSERT INTO public.featurer VALUES (2320, 23, 'org.twins.core.featurer.factory.filler.FillerAttachmentCUDFromContext', 'FillerAttachmentCUDFromContext', '', false);
INSERT INTO public.featurer VALUES (2321, 23, 'org.twins.core.featurer.factory.filler.FillerFieldMathDifferenceFromContextField', 'FillerFieldMathDifferenceFromContextField', '', false);
INSERT INTO public.featurer VALUES (2411, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinInstanceOf', 'ConditionerContextTwinInstanceOf', '', false);
INSERT INTO public.featurer VALUES (2412, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinOfClass', 'ConditionerContextTwinOfClass', '', false);
INSERT INTO public.featurer VALUES (2207, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCopy', 'MultiplierIsolatedCopy', 'New output twin for each input. Output class will be taken from input twin.', false);
INSERT INTO public.featurer VALUES (2206, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedOnContextFieldList', 'MultiplierIsolatedOnContextFieldList', 'New output twin for each input. Output class is selected by checking if fields in context (in loop). Order is important.If field is present then output twin class will be selected from this field class, otherwise loop will continue', false);
INSERT INTO public.featurer VALUES (2413, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinInstanceOf', 'ConditionerFactoryItemTwinInstanceOf', '', false);
INSERT INTO public.featurer VALUES (2414, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinOfClass', 'ConditionerFactoryItemTwinOfClass', '', false);
INSERT INTO public.featurer VALUES (2409, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinCreateOperation', 'ConditionerTwinCreateOperation', '', false);
INSERT INTO public.featurer VALUES (2004, 20, 'org.twins.core.featurer.usergroup.slugger.SluggerDomainAndBusinessAccountScopeBusinessAccountManage', 'SluggerDomainAndBusinessAccountScopeBusinessAccountManage', '', false);
INSERT INTO public.featurer VALUES (2410, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinUpdateOperation', 'ConditionerTwinUpdateOperation', '', false);
INSERT INTO public.featurer VALUES (1317, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperNumeric', 'FieldTyperNumeric', 'Numeric field', false);
INSERT INTO public.featurer VALUES (2325, 23, 'org.twins.core.featurer.factory.filler.FillerForwardLinkFromContextTwinLinkDstTwinHead', 'FillerForwardLinkFromContextTwinLinkDstTwinHead', 'Finds link in context twin. Get dst twin for this link. Get head of this dst twin. Create new link of given type from current twin pointing to this head', false);
INSERT INTO public.featurer VALUES (2407, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinHasChildren', 'ConditionerFactoryItemTwinHasChildren', '', false);
INSERT INTO public.featurer VALUES (2406, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinIsInFactoryInputList', 'ConditionerFactoryItemTwinIsInFactoryInputList', '', false);
INSERT INTO public.featurer VALUES (1313, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperCalcChildrenFieldV2', 'FieldTyperCalcChildrenFieldV2', 'Save sum of child.fields.values on serializeValue, and return saved total from database
', false);
INSERT INTO public.featurer VALUES (1312, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperCalcChildrenFieldV1', 'FieldTyperCalcChildrenFieldV1', 'Get sum of child.fields.values on fly', false);
INSERT INTO public.featurer VALUES (2322, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextFieldTwinAssignee', 'FillerBasicsAssigneeFromContextFieldTwinAssignee', 'If value of context field is an id of other twin (link) we will get assignee from that twin', false);
INSERT INTO public.featurer VALUES (1314, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperCountChildrenTwinsV1', 'FieldTyperCountChildrenTwinsV1', 'Get count of child-twins by child-status(inc/exc) on fly', false);
INSERT INTO public.featurer VALUES (1315, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperCountChildrenTwinsV2', 'FieldTyperCountChildrenTwinsV2', 'Save count of child-twin by child-status(exl/inc) on serializeValue, and return saved total from database', false);
INSERT INTO public.featurer VALUES (2324, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContext', 'FillerBasicsAssigneeFromContext', '', false);
INSERT INTO public.featurer VALUES (2323, 23, 'org.twins.core.featurer.factory.filler.FillerFieldFromContext', 'FillerFieldFromContext', '', false);
INSERT INTO public.featurer VALUES (2417, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinInstanceOfDeep', 'ConditionerContextTwinInstanceOfDeep', '', false);
INSERT INTO public.featurer VALUES (2416, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinOfClassDeep', 'ConditionerContextTwinOfClassDeep', '', false);
INSERT INTO public.featurer VALUES (2415, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextValueEquals', 'ConditionerContextValueEquals', '', false);
INSERT INTO public.featurer VALUES (1316, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperAttachment', 'FieldTyperAttachment', 'Allow the field to have an attachment', false);
INSERT INTO public.featurer VALUES (2418, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextValueExists', 'ConditionerContextValueExists', '', false);
INSERT INTO public.featurer VALUES (1103, 11, 'org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiatorFromDomain', 'BusinessAccountInitiatorFromParams', '', false);
INSERT INTO public.featurer VALUES (2326, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinOfClassAssignee', 'FillerBasicsAssigneeFromContextTwinOfClassAssignee', '', false);
INSERT INTO public.featurer VALUES (1606, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinHasLink', 'TwinValidatorTwinHasLink', '', false);
INSERT INTO public.featurer VALUES (2701, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderConfiguredId', 'SearchCriteriaBuilderConfiguredId', '', false);
INSERT INTO public.featurer VALUES (2702, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderCurrentUser', 'SearchCriteriaBuilderCurrentUser', '', false);
INSERT INTO public.featurer VALUES (2703, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderLink', 'SearchCriteriaBuilderLink', '', false);
INSERT INTO public.featurer VALUES (2705, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderParamLinkDst', 'SearchCriteriaBuilderParamLinkDst', '', false);
INSERT INTO public.featurer VALUES (2421, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerOutputTwinFieldValueEquals', 'ConditionerContextTwinFieldValueEquals', '', false);
INSERT INTO public.featurer VALUES (2208, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedByLink', 'MultiplierIsolatedByLink', 'Output list of twin relatives for each input. Output twin list will be loaded by link and filtered by statusIds', false);
INSERT INTO public.featurer VALUES (2601, 26, 'org.twins.core.featurer.twinclass.HeadHunterImpl', 'HeadHunterImpl', '', false);
INSERT INTO public.featurer VALUES (2209, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCreate', 'MultiplierIsolatedCreate', 'New output twin for each input. Output class in param.', false);
INSERT INTO public.featurer VALUES (2422, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinBasicFieldValueEqualsContextBasics', 'ConditionerContextTwinBasicFieldValueEqualsContextBasics', '', false);
INSERT INTO public.featurer VALUES (2419, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryContextTwinOfClassDeep', 'ConditionerFactoryContextTwinOfClassDeep', '', false);
INSERT INTO public.featurer VALUES (2420, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryContextTwinOfClass', 'ConditionerFactoryContextTwinOfClass', '', false);
INSERT INTO public.featurer VALUES (1502, 15, 'org.twins.core.featurer.transition.trigger.TransitionTriggerClearCurrentUserTouch', 'TransitionTriggerClearCurrentUserTouch', '', false);
INSERT INTO public.featurer VALUES (1503, 15, 'org.twins.core.featurer.transition.trigger.TransitionTriggerClearAllUsersTouch', 'TransitionTriggerClearAllUsersTouch', '', false);
INSERT INTO public.featurer VALUES (2404, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsAssignee', 'ConditionerApiUserIsAssignee', '', false);
INSERT INTO public.featurer VALUES (2423, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsMemberOfGroup', 'ConditionerApiUserIsMemberOfGroup', '', false);
INSERT INTO public.featurer VALUES (2424, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerHeadTwinFieldValueEquals', 'ConditionerHeadTwinFieldValueEquals', '', false);
INSERT INTO public.featurer VALUES (2425, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee', 'ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee', '', false);
INSERT INTO public.featurer VALUES (2328, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromContextTwinFieldTwinAssignee', 'FillerBasicsAssigneeFromContextTwinFieldTwinAssignee', 'If value of context twin field is an id of other twin (link) we will get assignee from that twin', false);
INSERT INTO public.featurer VALUES (2802, 28, 'org.twins.core.featurer.search.detector.SearchDetectorConcat', 'SearchDetectorConcat', '', false);
INSERT INTO public.featurer VALUES (1607, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorApiUserIsMemberOfGroup', 'TwinValidatorApiUserIsMemberOfGroup', '', false);
INSERT INTO public.featurer VALUES (1318, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperAssigneeEmail', 'FieldTyperAssigneeEmail', 'Return the mail field to the user', false);
INSERT INTO public.featurer VALUES (1319, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperAssigneeAvatar', 'FieldTyperAssigneeAvatar', 'Return the avatar field to the user', false);
INSERT INTO public.featurer VALUES (1601, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinAssigneeToCurrentUser', 'TransitionValidatorTwinAssigneeToCurrentUser', '', false);
INSERT INTO public.featurer VALUES (1602, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinCreatedByCurrentUser', 'TransitionValidatorTwinCreatedByCurrentUser', '', false);
INSERT INTO public.featurer VALUES (1603, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinMarkerExist', 'TransitionValidatorTwinMarkerExist', '', false);
INSERT INTO public.featurer VALUES (1604, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinHasChildrenInStatuses', 'TransitionValidatorTwinHasChildrenInStatuses', '', false);
INSERT INTO public.featurer VALUES (1605, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinAllChildrenInStatuses', 'TransitionValidatorTwinAllChildrenInStatuses', '', false);
INSERT INTO public.featurer VALUES (1608, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorCountOfTwinsSameTwinClassGTEValue', 'TwinValidatorCountOfTwinsSameTwinClassGTEValue', 'Count twins with twin-class same to input twin and compare with GTEvalue(great or equals)', false);
INSERT INTO public.featurer VALUES (2329, 23, 'org.twins.core.featurer.factory.filler.FillerBasicsAssigneeFromOutputTwinHeadAssignee', 'FillerBasicsAssigneeFromHeadTwinAssignee', 'Fill the assignee from own head twin assignee', false);
INSERT INTO public.featurer VALUES (2408, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinHasChildrenButNotFromFactoryInput', 'ConditionerHasChildrenButNotFromFactoryInput', '', true);
INSERT INTO public.featurer VALUES (2426, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerHeadTwinFieldExistsAndValueFilled', 'ConditionerHeadTwinFieldExistsAndValueFilled', 'Check head twin has field and its value not empty', false);
INSERT INTO public.featurer VALUES (2501, 25, 'org.twins.core.featurer.domain.initiator.DomainInitiatorBasic', 'DomainInitiatorBasic', '', false);
INSERT INTO public.featurer VALUES (2502, 25, 'org.twins.core.featurer.domain.initiator.DomainInitiatorB2B', 'DomainInitiatorB2B', '', false);
INSERT INTO public.featurer VALUES (2602, 26, 'org.twins.core.featurer.twinclass.HeadHunterByStatus', 'HeadHunterByStatus', '', false);
INSERT INTO public.featurer VALUES (2704, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderParam', 'SearchCriteriaBuilderParam', '', false);
INSERT INTO public.featurer VALUES (2801, 28, 'org.twins.core.featurer.search.detector.SearchDetectorPreferProtected', 'SearchDetectorPreferProtected', '', false);


--
-- Data for Name: featurer_injection; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: featurer_param; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.featurer_param VALUES (1101, false, 1, 'permissionSchemaId', 'permissionSchemaId', '', 'UUID:TWINS_PERMISSION_SCHEMA_ID');
INSERT INTO public.featurer_param VALUES (1101, false, 1, 'twinClassSchemaId', 'twinClassSchemaId', '', 'UUID:TWINS_TWIN_CLASS_SCHEMA_ID');
INSERT INTO public.featurer_param VALUES (1101, false, 1, 'twinflowSchemaId', 'twinflowSchemaId', '', 'UUID:TWINS_TWINFLOW_SCHEMA_ID');
INSERT INTO public.featurer_param VALUES (1102, false, 1, 'businessAccountTemplateTwinId', 'businessAccountTemplateTwinId', '', 'UUID:TWINS_TWIN_ID');
INSERT INTO public.featurer_param VALUES (1102, false, 1, 'permissionSchemaId', 'permissionSchemaId', '', 'UUID:TWINS_PERMISSION_SCHEMA_ID');
INSERT INTO public.featurer_param VALUES (1102, false, 1, 'twinClassSchemaId', 'twinClassSchemaId', '', 'UUID:TWINS_TWIN_CLASS_SCHEMA_ID');
INSERT INTO public.featurer_param VALUES (1102, false, 1, 'twinflowSchemaId', 'twinflowSchemaId', '', 'UUID:TWINS_TWINFLOW_SCHEMA_ID');
INSERT INTO public.featurer_param VALUES (2403, false, 1, 'permissionId', 'permissionId', '', 'UUID:TWINS_PERMISSION_ID');
INSERT INTO public.featurer_param VALUES (2423, false, 1, 'userGroupIds', 'userGroupIds', '', 'UUID_SET:TWINS_USER_GROUP_ID');
INSERT INTO public.featurer_param VALUES (2402, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2402, false, 1, 'value', 'value', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2422, false, 1, 'field', 'field', 'Basic field to check', 'STRING:TWINS_TWIN_BASIC_FIELD');
INSERT INTO public.featurer_param VALUES (2401, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2401, false, 1, 'value', 'value', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2411, false, 1, 'instanceOfTwinClassId', 'instanceOfTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2417, false, 1, 'instanceOfTwinClassId', 'instanceOfTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2405, false, 1, 'markerId', 'markerId', '', 'UUID:TWINS_MARKER_ID');
INSERT INTO public.featurer_param VALUES (2412, false, 1, 'ofTwinClassId', 'ofTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2416, false, 1, 'ofTwinClassId', 'ofTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2415, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2415, false, 1, 'value', 'value', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2418, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2420, false, 1, 'ofTwinClassId', 'ofTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2419, false, 1, 'ofTwinClassId', 'ofTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2425, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2407, false, 1, 'statusIds', 'statusIds', '', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (2407, false, 1, 'excludeFactoryInput', 'excludeFactoryInput', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2408, false, 1, 'statusIds', 'statusIds', '', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (2413, false, 1, 'instanceOfTwinClassId', 'instanceOfTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2414, false, 1, 'ofTwinClassId', 'ofTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2426, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2424, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2424, false, 1, 'value', 'value', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2421, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2421, false, 1, 'value', 'value', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2308, false, 1, 'uniqForSrcRelink', 'uniqForSrcRelink', 'If true, then OneToOne and ManyToOne links will be relinked to new twin (if some other twin was already linked)', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2304, false, 1, 'uniqForSrcRelink', 'uniqForSrcRelink', 'If true, then XToOne links will be relinked to new twin', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2324, false, 1, 'assigneeField', 'assigneeField', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2315, false, 1, 'assigneeField', 'assigneeField', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2322, false, 1, 'linkField', 'linkField', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2314, false, 1, 'assigneeField', 'assigneeField', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2328, false, 1, 'linkField', 'linkField', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2326, false, 1, 'twinClassId', 'twinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2323, false, 1, 'srcTwinClassFieldId', 'srcTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2323, false, 1, 'dstTwinClassFieldId', 'dstTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2312, false, 1, 'srcTwinClassFieldId', 'srcTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2312, false, 1, 'dstTwinClassFieldId', 'dstTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2311, false, 1, 'srcTwinClassFieldId', 'srcTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2311, false, 1, 'dstTwinClassFieldId', 'dstTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2321, false, 1, 'minuendTwinClassFieldId', 'minuendTwinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2321, false, 1, 'subtrahendTwinClassFieldId', 'subtrahendTwinClassFieldId', 'Value from this field will be ', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2321, false, 1, 'allowNegativeResult', 'allowNegativeResult', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2325, false, 1, 'headHunterLink', 'headHunterLink', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (2325, false, 1, 'newLinksId', 'newLinksId', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (2305, false, 1, 'linksIds', 'linksIds', '', 'UUID_SET:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (2318, false, 1, 'markerId', 'markerId', '', 'UUID:TWINS_MARKER_ID');
INSERT INTO public.featurer_param VALUES (2319, false, 1, 'markerId', 'markerId', '', 'UUID:TWINS_MARKER_ID');
INSERT INTO public.featurer_param VALUES (2327, false, 1, 'fields', 'fields', 'List of basic fields to fill', 'WORD_LIST:TWINS_TWIN_BASIC_FIELD');
INSERT INTO public.featurer_param VALUES (2201, false, 1, 'outputTwinClassId', 'outputTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2202, false, 1, 'outputTwinClassId', 'outputTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2208, false, 1, 'linkId', 'linkId', 'Link from sought twin to factory input twin', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (2208, false, 1, 'statusIds', 'statusIds', 'Statuses of src(fwd) linked twin. If empty - twins with any status will be found', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (2208, false, 1, 'excludeStatuses', 'excludeStatues', 'Exclude(true)/Include(false) child Twin.Status.IDs from query result', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2207, false, 1, 'copyHead', 'copyHead', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2203, false, 1, 'outputTwinClassIdFromContextField', 'outputTwinClassIdFromContextField', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2203, false, 1, 'elseOutputTwinClassId', 'elseOutputTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (2206, false, 1, 'contextFieldIdList', 'contextFieldList', '', 'UUID_SET:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (2205, false, 1, 'statusIds', 'statusIds', '', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1316, false, 1, 'minCount', 'minCount', 'Min count of attachments to field', 'INT');
INSERT INTO public.featurer_param VALUES (1316, false, 1, 'maxCount', 'maxCount', 'Max count of attachments to field', 'INT');
INSERT INTO public.featurer_param VALUES (1316, false, 1, 'fileSizeMbLimit', 'fileSizeMbLimit', 'Max size per file for attachment', 'INT');
INSERT INTO public.featurer_param VALUES (1316, false, 1, 'fileExtensionList', 'fileExtensionList', 'Allowed extensions for attachment(ex: jpg,jpeg,png)', 'STRING');
INSERT INTO public.featurer_param VALUES (1316, false, 1, 'fileNameRegexp', 'fileNameRegexp', 'File name must match this pattern', 'STRING');
INSERT INTO public.featurer_param VALUES (1312, false, 1, 'childrenTwinClassFieldId', 'childrenTwinClassFieldId', 'Twin.Class.Field Id of child twin fields', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (1312, false, 1, 'childrenTwinStatusIdList', 'childrenTwinStatusIdList', 'Twin.Status.IDs of child twin', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1312, false, 1, 'exclude', 'exclude', 'Exclude(true)/Include(false) child-field''s Twin.Status.IDs from query result', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1313, false, 1, 'childrenTwinClassFieldId', 'childrenTwinClassFieldId', 'Twin.Class.Field Id of child twin fields', 'UUID:TWINS_TWIN_CLASS_FIELD_ID');
INSERT INTO public.featurer_param VALUES (1313, false, 1, 'childrenTwinStatusIdList', 'childrenTwinStatusIdList', 'Twin.Status.IDs of child twin', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1313, false, 1, 'exclude', 'exclude', 'Exclude(true)/Include(false) child-field''s Twin.Status.IDs from query result', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1306, false, 1, 'listUUID', 'listUUID', '', 'UUID:TWINS_DATA_LIST_ID');
INSERT INTO public.featurer_param VALUES (1306, false, 1, 'inline', 'inline', 'If true, then values will be on one row', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1314, false, 1, 'childrenTwinStatusIdList', 'childrenTwinStatusIdList', 'Twin.Status.IDs of child twin', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1314, false, 1, 'exclude', 'exclude', 'Exclude(true)/Include(false) child-field''s Twin.Status.IDs from query result', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1315, false, 1, 'childrenTwinStatusIdList', 'childrenTwinStatusIdList', 'Twin.Status.IDs of child twin', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1315, false, 1, 'exclude', 'exclude', 'Exclude(true)/Include(false) child-field''s Twin.Status.IDs from query result', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1302, false, 1, 'pattern', 'pattern', 'pattern for date value', 'STRING');
INSERT INTO public.featurer_param VALUES (1310, false, 1, 'linkUUID', 'linkUUID', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (1310, false, 1, 'longListThreshold', 'longListThreshold', 'If options count is bigger then given threshold longList type will be used', 'INT');
INSERT INTO public.featurer_param VALUES (1317, false, 1, 'min', 'min', 'Min possible value', 'DOUBLE');
INSERT INTO public.featurer_param VALUES (1317, false, 1, 'max', 'max', 'Max possible value', 'DOUBLE');
INSERT INTO public.featurer_param VALUES (1317, false, 1, 'step', 'step', 'Step of value change', 'DOUBLE');
INSERT INTO public.featurer_param VALUES (1317, false, 1, 'thousandSeparator', 'thousandSeparator', 'Thousand separator. Must not be equal to decimal separator.', 'STRING');
INSERT INTO public.featurer_param VALUES (1317, false, 1, 'decimalSeparator', 'decimalSeparator', 'Decimal separator. Must not be equal to thousand separator.', 'STRING');
INSERT INTO public.featurer_param VALUES (1317, false, 1, 'decimalPlaces', 'decimalPlaces', 'Number of decimal places.', 'INT');
INSERT INTO public.featurer_param VALUES (1305, false, 1, 'multiple', 'multiple', 'If true, then multiple select available', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1305, false, 1, 'supportCustom', 'supportCustom', 'If true, then user can enter custom value', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1305, false, 1, 'longListThreshold', 'longListThreshold', 'If options count is bigger then given threshold longList type will be used', 'INT');
INSERT INTO public.featurer_param VALUES (1305, false, 1, 'listUUID', 'listUUID', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (1308, false, 1, 'listUUID', 'listUUID', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (1307, false, 1, 'listUUID', 'listUUID', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (1309, false, 1, 'listUUID', 'listUUID', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (1301, false, 1, 'regexp', 'regexp', '', 'STRING');
INSERT INTO public.featurer_param VALUES (1311, false, 1, 'userFilterUUID', 'userFilterUUID', '', 'UUID');
INSERT INTO public.featurer_param VALUES (1311, false, 1, 'multiple', 'multiple', 'If true, then multiple select available', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1311, false, 1, 'longListThreshold', 'longListThreshold', 'If options count is bigger then given threshold longList type will be used', 'INT');
INSERT INTO public.featurer_param VALUES (2701, false, 1, 'entityId', 'entityId', '', 'UUID');
INSERT INTO public.featurer_param VALUES (2703, false, 1, 'linkId', 'linkId', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (2703, false, 1, 'dstTwinId', 'dstTwinId', '', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (2703, false, 1, 'anyOfList', 'anyOfList', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2704, false, 1, 'paramKey', 'paramKey', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2704, false, 1, 'required', 'required', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2705, false, 1, 'paramKey', 'paramKey', '', 'STRING');
INSERT INTO public.featurer_param VALUES (2705, false, 1, 'linkId', 'linkId', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (2705, false, 1, 'required', 'required', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2705, false, 1, 'anyOfList', 'anyOfList', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1503, false, 1, 'touchId', 'touchId', '', 'STRING:TWINS_TWIN_TOUCH_ID');
INSERT INTO public.featurer_param VALUES (1502, false, 1, 'touchId', 'touchId', '', 'STRING:TWINS_TWIN_TOUCH_ID');
INSERT INTO public.featurer_param VALUES (1501, false, 1, 'twinId', 'twinId', '', 'UUID:TWINS_TWIN_ID');
INSERT INTO public.featurer_param VALUES (1607, false, 1, 'userGroupIds', 'userGroupIds', '', 'UUID_SET:TWINS_USER_GROUP_ID');
INSERT INTO public.featurer_param VALUES (1608, false, 1, 'GTEvalue', 'GTEvalue', 'count of twins must be great or equals to this value', 'INT');
INSERT INTO public.featurer_param VALUES (1605, false, 1, 'childrenTwinClassId', 'childrenTwinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (1605, false, 1, 'childrenTwinStatusId', 'childrenTwinStatusId', '', 'UUID:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1604, false, 1, 'statusIds', 'statusIds', '', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (1606, false, 1, 'linkId', 'linkId', '', 'UUID:TWINS_LINK_ID');
INSERT INTO public.featurer_param VALUES (1603, false, 1, 'markerId', 'markerId', '', 'UUID:TWINS_MARKER_ID');
INSERT INTO public.featurer_param VALUES (2602, false, 1, 'statusIds', 'statusIds', '', 'UUID_SET:TWINS_TWIN_STATUS_ID');
INSERT INTO public.featurer_param VALUES (2602, false, 1, 'excludeStatusInput', 'excludeStatusInput', '', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (2102, false, 1, 'allowEmpty', 'allowEmpty', 'If true, then user can be out of any group', 'BOOLEAN');
INSERT INTO public.featurer_param VALUES (1402, false, 1, 'twinClassIdList', 'twinClassIdList', '', 'UUID_SET:TWINS_TWIN_CLASS_ID');
INSERT INTO public.featurer_param VALUES (1404, false, 1, 'twinClassIdList', 'twinClassIdList', '', 'UUID_SET:TWINS_TWIN_CLASS_ID');


--
-- Data for Name: featurer_param_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.featurer_param_type VALUES ('STRING', '.*', 'Hello world!', 'any string');
INSERT INTO public.featurer_param_type VALUES ('BOOLEAN', '^true$|^false$', 'true', 'true or false value');
INSERT INTO public.featurer_param_type VALUES ('INT', '^-?\d+$', '108', 'any integer number');
INSERT INTO public.featurer_param_type VALUES ('WORD_LIST', '.*', 'Hello world!', 'words splited by comma');
INSERT INTO public.featurer_param_type VALUES ('DOUBLE', '^-?\d+(\.\d+)?$', '108.84', 'any number');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_PERMISSION_SCHEMA_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_TWIN_CLASS_SCHEMA_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_TWINFLOW_SCHEMA_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_TWIN_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_PERMISSION_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID_SET:TWINS_USER_GROUP_ID', '.*', '9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_TWIN_CLASS_FIELD_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('STRING:TWINS_TWIN_BASIC_FIELD', '.*', 'name', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_TWIN_CLASS_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_MARKER_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID_SET:TWINS_TWIN_STATUS_ID', '.*', '9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_LINK_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID_SET:TWINS_LINK_ID', '.*', '9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('WORD_LIST:TWINS_TWIN_BASIC_FIELD', '.*', '9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID_SET:TWINS_TWIN_CLASS_FIELD_ID', '.*', '9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_DATA_LIST_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('STRING:TWINS_TWIN_TOUCH_ID', '.*', 'name', 'WATCHED');
INSERT INTO public.featurer_param_type VALUES ('UUID:TWINS_TWIN_STATUS_ID', '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}', '9a3f6075-f175-41cd-a804-934201ec969c', '');
INSERT INTO public.featurer_param_type VALUES ('UUID_SET:TWINS_TWIN_CLASS_ID', '.*', '9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c', '');


--
-- Data for Name: featurer_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.featurer_type VALUES (13, 'FieldTyper', 'Customize format of twin class field');
INSERT INTO public.featurer_type VALUES (10, 'injectors', 'Customize inject of featurer params');
INSERT INTO public.featurer_type VALUES (14, 'WidgetAccessor', 'Checks if widget is suitable for class');
INSERT INTO public.featurer_type VALUES (11, 'BusinessAccountInitiator', '');
INSERT INTO public.featurer_type VALUES (19, 'TokenHandler', '');
INSERT INTO public.featurer_type VALUES (20, 'Slugger', '');
INSERT INTO public.featurer_type VALUES (21, 'UserGroupManager', '');
INSERT INTO public.featurer_type VALUES (15, 'TransitionTrigger', '');
INSERT INTO public.featurer_type VALUES (23, 'Filler', '');
INSERT INTO public.featurer_type VALUES (22, 'Multiplier', '');
INSERT INTO public.featurer_type VALUES (24, 'Conditioner', '');
INSERT INTO public.featurer_type VALUES (25, 'DomainInitiator', '');
INSERT INTO public.featurer_type VALUES (27, 'SearchCriteriaBuilder', '');
INSERT INTO public.featurer_type VALUES (16, 'TwinValidator', '');
INSERT INTO public.featurer_type VALUES (26, 'HeadHunter', 'Getting valid head twin class by some class');
INSERT INTO public.featurer_type VALUES (28, 'SearchBatcher', 'Encapsulate logic to select searches from one alias into one batch');


--
-- Data for Name: history; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: history_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.history_type VALUES ('linkCreated', 'Link ''${link.name}'' on ''${dstTwin.name}'' created', 'softEnabled');
INSERT INTO public.history_type VALUES ('linkUpdated', 'Link ''${link.name}'' was updated from ''${fromTwin.name}'' to ''${toTwin.name}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('createdByChanged', 'CreatedBy was changed from ''${fromUser.name}'' to ''${toUser.name}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('fieldCreated', 'Field ''${field.name}'' was set with ''${toValue}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('fieldDeleted', 'Field ''${field.name}'' was truncated', 'softEnabled');
INSERT INTO public.history_type VALUES ('nameChanged', 'Name was changed from ''${fromValue}'' to ''${toValue}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('assigneeChanged', 'Assignee was changed from ''${fromUser.name}'' to ''${toUser.name}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('headChanged', 'Head twin was changed', 'softEnabled');
INSERT INTO public.history_type VALUES ('markerChanged', NULL, 'softEnabled');
INSERT INTO public.history_type VALUES ('twinDeleted', NULL, 'softEnabled');
INSERT INTO public.history_type VALUES ('statusChanged', 'Status was changed from ''${fromStatus.name}'' to ''${toStatus.name}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('twinCreated', 'New ''${twin.class.name}'' created', 'softEnabled');
INSERT INTO public.history_type VALUES ('fieldChanged', 'Field ''${field.name}'' was changed from ''${fromValue}'' to ''${toValue}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('attachmentUpdate', 'Attachment was updated', 'softEnabled');
INSERT INTO public.history_type VALUES ('tagChanged', NULL, 'softEnabled');
INSERT INTO public.history_type VALUES ('attachmentCreate', 'Attachment was added', 'softEnabled');
INSERT INTO public.history_type VALUES ('attachmentDelete', 'Attachment was deleted', 'softEnabled');
INSERT INTO public.history_type VALUES ('linkDeleted', 'Link ''${link.name}'' on ''${dstTwin.name}'' deleted', 'softEnabled');
INSERT INTO public.history_type VALUES ('descriptionChanged', 'Description was changed from ''${fromValue}'' to ''${toValue}''', 'softEnabled');
INSERT INTO public.history_type VALUES ('linkCreate', NULL, NULL);
INSERT INTO public.history_type VALUES ('linkUpdate', NULL, NULL);
INSERT INTO public.history_type VALUES ('linkDelete', NULL, NULL);


--
-- Data for Name: history_type_config_domain; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: history_type_config_twin_class; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: history_type_config_twin_class_field; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: history_type_domain_template; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: history_type_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.history_type_status VALUES ('hardDisabled', 'Type is disabled. All lower config will be ignored');
INSERT INTO public.history_type_status VALUES ('softDisabled', 'Type likely is disabled. Type can be enabled by lower level config');
INSERT INTO public.history_type_status VALUES ('softEnabled', 'Type likely is enabled. Type can be disabled by lower level config');
INSERT INTO public.history_type_status VALUES ('hardEnabled', 'Type is enabled. All lower config will be ignored');


--
-- Data for Name: i18n; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.i18n VALUES ('00000000-0000-0000-1111-000000000001', NULL, NULL, 'twinClassFieldName');
INSERT INTO public.i18n VALUES ('00000000-0000-0000-1111-000000000002', NULL, NULL, 'twinClassFieldName');


--
-- Data for Name: i18n_locale; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.i18n_locale VALUES ('en', 'English', true, NULL, NULL);


--
-- Data for Name: i18n_translation; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.i18n_translation VALUES ('00000000-0000-0000-1111-000000000001', 'en', 'Email', 0);
INSERT INTO public.i18n_translation VALUES ('00000000-0000-0000-1111-000000000002', 'en', 'Avatar', 0);


--
-- Data for Name: i18n_translation_bin; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: i18n_translation_style; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: i18n_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.i18n_type VALUES ('twinClassName', 'Twin class name');
INSERT INTO public.i18n_type VALUES ('twinStatusName', 'Twin status name');
INSERT INTO public.i18n_type VALUES ('twinStatusDescription', 'Twin status description');
INSERT INTO public.i18n_type VALUES ('twinClassFieldName', 'Twin class field name');
INSERT INTO public.i18n_type VALUES ('twinClassFieldDescription', 'Twin class field description');
INSERT INTO public.i18n_type VALUES ('twinClassDescription', 'Twin class description');
INSERT INTO public.i18n_type VALUES ('cardName', 'Twin card name');
INSERT INTO public.i18n_type VALUES ('unknown', 'Unknown');
INSERT INTO public.i18n_type VALUES ('dataListOptionValue', 'Data list option value');
INSERT INTO public.i18n_type VALUES ('linkForwardName', 'Twin link forward name');
INSERT INTO public.i18n_type VALUES ('linkBackwardName', 'Twin link backward name');
INSERT INTO public.i18n_type VALUES ('twinflowTransitionName', 'Twinflow transition name');
INSERT INTO public.i18n_type VALUES ('spaceRoleName', 'Space role name');
INSERT INTO public.i18n_type VALUES ('twinflowName', 'Twinflow name');
INSERT INTO public.i18n_type VALUES ('twinflowDescription', 'Twinflow description');
INSERT INTO public.i18n_type VALUES ('twinflowTransitionDescription', 'Twinflow transition description');
INSERT INTO public.i18n_type VALUES ('error', 'Error');


--
-- Data for Name: link; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: link_strength; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.link_strength VALUES ('MANDATORY', 'Twin of src_twin_class_id can`t be created without such link. Cascade deletion will occur.');
INSERT INTO public.link_strength VALUES ('OPTIONAL', 'Twin of src_twin_class_id can be created without such link. If link exists cascade deletion will not occur. ');
INSERT INTO public.link_strength VALUES ('OPTIONAL_BUT_DELETE_CASCADE', 'Link is optional, but if exist cascade deletion will occur');


--
-- Data for Name: link_tree; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: link_tree_node; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: link_trigger; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: link_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.link_type VALUES ('OneToOne');
INSERT INTO public.link_type VALUES ('ManyToMany');
INSERT INTO public.link_type VALUES ('ManyToOne');


--
-- Data for Name: link_validator; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: permission; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.permission VALUES ('8f9d0ec6-47e5-4d19-b5f6-b0c11d3f057c', 'SPACE_BROWSE', '00000000-0000-0000-0005-000000000001', NULL, NULL);
INSERT INTO public.permission VALUES ('00000000-0000-0000-0004-000000000001', 'DENY_ALL', '00000000-0000-0000-0005-000000000001', NULL, NULL);
INSERT INTO public.permission VALUES ('00000000-0000-0000-0004-000000000002', 'TWIN_CLASS_MANAGE', '00000000-0000-0000-0005-000000000001', NULL, NULL);
INSERT INTO public.permission VALUES ('00000000-0000-0000-0004-000000000003', 'TRANSITION_MANAGE', '00000000-0000-0000-0005-000000000001', NULL, NULL);


--
-- Data for Name: permission_group; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.permission_group VALUES ('00000000-0000-0000-0005-000000000001', NULL, NULL, 'TWINS_GLOBAL_PERMISSIONS', 'Twins global permissions', NULL);


--
-- Data for Name: permission_schema; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: permission_schema_assignee_propagation; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: permission_schema_space_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: permission_schema_twin_role; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: permission_schema_user; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: permission_schema_user_group; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: search; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: search_alias; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: search_field; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.search_field VALUES ('assigneeUserId');
INSERT INTO public.search_field VALUES ('createdByUserId');
INSERT INTO public.search_field VALUES ('headTwinId');
INSERT INTO public.search_field VALUES ('statusId');
INSERT INTO public.search_field VALUES ('linkId');
INSERT INTO public.search_field VALUES ('twinClassId');
INSERT INTO public.search_field VALUES ('twinNameLike');
INSERT INTO public.search_field VALUES ('twinId');
INSERT INTO public.search_field VALUES ('tagDataListOptionId');
INSERT INTO public.search_field VALUES ('markerDataListOptionId');
INSERT INTO public.search_field VALUES ('hierarchyTreeContainsId');


--
-- Data for Name: search_predicate; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: space; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: space_role; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: space_role_user; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: space_role_user_group; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: touch; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.touch VALUES ('WATCHED');
INSERT INTO public.touch VALUES ('STARRED');
INSERT INTO public.touch VALUES ('REVIEWED');


--
-- Data for Name: twin; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin VALUES ('00000000-0000-0000-0002-000000000001', '00000000-0000-0000-0001-000000000001', NULL, NULL, '00000000-0000-0000-0003-000000000001', 'User', NULL, '00000000-0000-0000-0000-000000000000', NULL, '2024-10-16 15:27:07.407411', NULL, NULL, '00000000_0000_0000_0002_000000000001', NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.twin VALUES ('00000000-0000-0000-0002-000000000003', '00000000-0000-0000-0001-000000000003', NULL, NULL, '00000000-0000-0000-0003-000000000003', 'Business account', NULL, '00000000-0000-0000-0000-000000000000', NULL, '2024-10-16 15:27:07.443014', NULL, NULL, '00000000_0000_0000_0002_000000000003', NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: twin_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_action VALUES ('EDIT');
INSERT INTO public.twin_action VALUES ('DELETE');
INSERT INTO public.twin_action VALUES ('COMMENT');
INSERT INTO public.twin_action VALUES ('MOVE');
INSERT INTO public.twin_action VALUES ('WATCH');
INSERT INTO public.twin_action VALUES ('TIME_TRACK');
INSERT INTO public.twin_action VALUES ('ATTACHMENT_ADD');
INSERT INTO public.twin_action VALUES ('ATTACHMENT_DELETE');
INSERT INTO public.twin_action VALUES ('HISTORY_VIEW');


--
-- Data for Name: twin_action_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_action_validator; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_alias; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_alias_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_alias_type VALUES ('D', 'Domain key is uniq, so given type of alias is also uniq even between all registered domains');
INSERT INTO public.twin_alias_type VALUES ('C', 'Twin class key is uniq only in given domain. That is why such kind of alias can be duplicated between different domains.');
INSERT INTO public.twin_alias_type VALUES ('B', 'This alias is differ from domain class alias because it is uniq only inside BA inside some domain.');
INSERT INTO public.twin_alias_type VALUES ('S', 'Space for owner type domain');
INSERT INTO public.twin_alias_type VALUES ('T', 'Space for owner type domainBusinessAccount');
INSERT INTO public.twin_alias_type VALUES ('K', 'Space for owner type domainUser');


--
-- Data for Name: twin_attachment; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_business_account_alias_counter; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_class; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_class VALUES ('00000000-0000-0000-0001-000000000001', NULL, 'USER', false, false, NULL, NULL, NULL, NULL, NULL, '00000000-0000-0000-0000-000000000000', '2023-11-09 13:54:48.521717', 'system', 0, NULL, NULL, false, false, false, NULL, '00000000_0000_0000_0001_000000000001', '00000000_0000_0000_0001_000000000001', 2601, NULL);
INSERT INTO public.twin_class VALUES ('00000000-0000-0000-0001-000000000003', NULL, 'BUSINESS_ACCOUNT', false, false, NULL, NULL, NULL, NULL, NULL, '00000000-0000-0000-0000-000000000000', '2023-11-09 13:54:49.625046', 'system', 0, NULL, NULL, false, false, false, NULL, '00000000_0000_0000_0001_000000000003', '00000000_0000_0000_0001_000000000003', 2601, NULL);


--
-- Data for Name: twin_class_field; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_class_field VALUES ('00000000-0000-0000-0011-000000000001', '00000000-0000-0000-0001-000000000001', 'email', '00000000-0000-0000-1111-000000000001', NULL, 1318, NULL, NULL, NULL, false);
INSERT INTO public.twin_class_field VALUES ('00000000-0000-0000-0011-000000000002', '00000000-0000-0000-0001-000000000001', 'avatar', '00000000-0000-0000-1111-000000000002', NULL, 1319, NULL, NULL, NULL, false);


--
-- Data for Name: twin_class_owner_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_class_owner_type VALUES ('domainUser');
INSERT INTO public.twin_class_owner_type VALUES ('domainBusinessAccount');
INSERT INTO public.twin_class_owner_type VALUES ('domain');
INSERT INTO public.twin_class_owner_type VALUES ('user');
INSERT INTO public.twin_class_owner_type VALUES ('businessAccount');
INSERT INTO public.twin_class_owner_type VALUES ('system');


--
-- Data for Name: twin_class_schema; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_class_schema_map; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_comment; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_comment_action; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_comment_action VALUES ('EDIT');
INSERT INTO public.twin_comment_action VALUES ('DELETE');
INSERT INTO public.twin_comment_action VALUES ('PIN');
INSERT INTO public.twin_comment_action VALUES ('UNPIN');
INSERT INTO public.twin_comment_action VALUES ('VOTE');
INSERT INTO public.twin_comment_action VALUES ('REACT');
INSERT INTO public.twin_comment_action VALUES ('HIDE');
INSERT INTO public.twin_comment_action VALUES ('UNHIDE');


--
-- Data for Name: twin_comment_action_alien_permission; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_comment_action_alien_validator; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_comment_action_self; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory_condition; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory_condition_set; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory_multiplier; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory_multiplier_filter; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory_pipeline; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_factory_pipeline_step; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_field_data_list; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_field_simple; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_field_user; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_link; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_marker; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_role; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_role VALUES ('assignee');
INSERT INTO public.twin_role VALUES ('creator');
INSERT INTO public.twin_role VALUES ('space_assignee');
INSERT INTO public.twin_role VALUES ('space_creator');


--
-- Data for Name: twin_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_status VALUES ('00000000-0000-0000-0003-000000000001', '00000000-0000-0000-0001-000000000001', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.twin_status VALUES ('00000000-0000-0000-0003-000000000003', '00000000-0000-0000-0001-000000000003', NULL, NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: twin_status_group; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_status_group_map; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_status_transition_trigger; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_status_transition_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.twin_status_transition_type VALUES ('incoming');
INSERT INTO public.twin_status_transition_type VALUES ('outgoing');


--
-- Data for Name: twin_tag; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_touch; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twin_work; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow_schema; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow_schema_map; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow_transition; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow_transition_alias; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow_transition_trigger; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: twinflow_transition_validator; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public."user" VALUES ('00000000-0000-0000-0000-000000000000', 'SYSTEM', NULL, NULL, '2023-10-18 08:24:50.52663', 'ACTIVE');


--
-- Data for Name: user_group; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: user_group_map; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: user_group_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.user_group_type VALUES ('businessAccountScopeBusinessAccountManage', NULL, 2003, NULL);
INSERT INTO public.user_group_type VALUES ('domainScopeDomainManage', NULL, 2001, NULL);
INSERT INTO public.user_group_type VALUES ('domainScopeBusinessAccountManage', NULL, 2002, NULL);
INSERT INTO public.user_group_type VALUES ('domainAndBusinessAccountScopeBusinessAccountManage', NULL, 2004, NULL);


--
-- Data for Name: user_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.user_status VALUES ('ACTIVE');
INSERT INTO public.user_status VALUES ('DELETED');
INSERT INTO public.user_status VALUES ('BLOCKED');


--
-- Data for Name: widget; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.widget VALUES ('8627b5f4-714a-4f6e-ab58-e76e90249842', 'attachments', 'Twin attachments widget', NULL, 1301, 1401, NULL);
INSERT INTO public.widget VALUES ('d502e185-a339-41a2-9e48-75c5047f48a1', 'rich_text_panel', 'Richtext panel ', NULL, 1301, 1401, NULL);
INSERT INTO public.widget VALUES ('bd1f4a13-13bc-4d44-b05b-33ca05450291', 'work_log', 'Twin Work log widget', NULL, 1301, 1401, NULL);
INSERT INTO public.widget VALUES ('ec18375d-19ee-4016-985d-bacc5aa5185d', 'comments', 'Twin comments widget', NULL, 1301, 1401, NULL);
INSERT INTO public.widget VALUES ('bc887127-0394-4f06-ae16-f0e20573707d', 'twinlinks_table', 'Linked twins table', NULL, 1301, 1401, NULL);
INSERT INTO public.widget VALUES ('4245e338-3c09-4390-8a03-435d1da4e311', 'description_list', 'Fields description list widget', NULL, 1301, 1401, NULL);
INSERT INTO public.widget VALUES ('ddb81f2a-8e2a-4ae0-886d-e3ec93a861dd', 'history', 'Twin history widget', NULL, 1301, 1401, NULL);


--
-- Name: i18n_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.i18n_type_id_seq', 1, false);


--
-- Name: business_account business_account_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.business_account
    ADD CONSTRAINT business_account_pk PRIMARY KEY (id);


--
-- Name: business_account_user business_account_user_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.business_account_user
    ADD CONSTRAINT business_account_user_pk PRIMARY KEY (id);


--
-- Name: card_access card_access_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_access
    ADD CONSTRAINT card_access_pk PRIMARY KEY (id);


--
-- Name: card_layout card_layout_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_layout
    ADD CONSTRAINT card_layout_pk PRIMARY KEY (id);


--
-- Name: card_layout_position card_layout_position_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_layout_position
    ADD CONSTRAINT card_layout_position_pk PRIMARY KEY (id);


--
-- Name: card_override card_override_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_override
    ADD CONSTRAINT card_override_pk PRIMARY KEY (id);


--
-- Name: card card_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT card_pk PRIMARY KEY (id);


--
-- Name: card_widget_override card_widget_override_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget_override
    ADD CONSTRAINT card_widget_override_pk PRIMARY KEY (id);


--
-- Name: card_widget card_widget_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget
    ADD CONSTRAINT card_widget_pk PRIMARY KEY (id);


--
-- Name: channel channel_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel
    ADD CONSTRAINT channel_pk PRIMARY KEY (id);


--
-- Name: data_list_option data_list_option_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list_option
    ADD CONSTRAINT data_list_option_pk PRIMARY KEY (id);


--
-- Name: data_list_option_status data_list_option_status_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list_option_status
    ADD CONSTRAINT data_list_option_status_pk PRIMARY KEY (id);


--
-- Name: data_list data_list_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list
    ADD CONSTRAINT data_list_pk PRIMARY KEY (id);


--
-- Name: domain_business_account domain_business_account_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_business_account
    ADD CONSTRAINT domain_business_account_pk PRIMARY KEY (id);


--
-- Name: domain_locale domain_id_locale_id_uk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_locale
    ADD CONSTRAINT domain_id_locale_id_uk UNIQUE (domain_id, i18n_locale_id);


--
-- Name: domain_locale domain_locale_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_locale
    ADD CONSTRAINT domain_locale_pk PRIMARY KEY (id);


--
-- Name: domain domain_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_pk PRIMARY KEY (id);


--
-- Name: domain_type domain_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_type
    ADD CONSTRAINT domain_type_pkey PRIMARY KEY (id);


--
-- Name: domain_user domain_user_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_user
    ADD CONSTRAINT domain_user_pk PRIMARY KEY (id);


--
-- Name: error error_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.error
    ADD CONSTRAINT error_pk PRIMARY KEY (id);


--
-- Name: featurer_injection featurer_injections_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_injection
    ADD CONSTRAINT featurer_injections_pk PRIMARY KEY (id);


--
-- Name: featurer_param featurer_param_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_param
    ADD CONSTRAINT featurer_param_pk PRIMARY KEY (featurer_id, key);


--
-- Name: featurer_param_type featurer_param_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_param_type
    ADD CONSTRAINT featurer_param_type_pk PRIMARY KEY (id);


--
-- Name: featurer featurer_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer
    ADD CONSTRAINT featurer_pk PRIMARY KEY (id);


--
-- Name: featurer_type featurer_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_type
    ADD CONSTRAINT featurer_type_pk PRIMARY KEY (id);


--
-- Name: history history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_pk PRIMARY KEY (id);


--
-- Name: history_type_config_domain history_type_config_domain_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_domain
    ADD CONSTRAINT history_type_config_domain_pk PRIMARY KEY (id);


--
-- Name: history_type_config_twin_class_field history_type_config_twin_class_field_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class_field
    ADD CONSTRAINT history_type_config_twin_class_field_pk PRIMARY KEY (id);


--
-- Name: history_type_config_twin_class history_type_config_twin_class_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class
    ADD CONSTRAINT history_type_config_twin_class_pk PRIMARY KEY (id);


--
-- Name: history_type_domain_template history_type_domain_template_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_domain_template
    ADD CONSTRAINT history_type_domain_template_pk PRIMARY KEY (id);


--
-- Name: history_type history_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type
    ADD CONSTRAINT history_type_pkey PRIMARY KEY (id);


--
-- Name: history_type_status history_type_status_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_status
    ADD CONSTRAINT history_type_status_pk PRIMARY KEY (id);


--
-- Name: i18n_locale i18n_locale_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_locale
    ADD CONSTRAINT i18n_locale_pk PRIMARY KEY (locale);


--
-- Name: i18n i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n
    ADD CONSTRAINT i18n_pkey PRIMARY KEY (id);


--
-- Name: i18n_translation_bin i18n_translation_bin_uq; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_translation_bin
    ADD CONSTRAINT i18n_translation_bin_uq UNIQUE (i18n_id, locale);


--
-- Name: i18n_translation i18n_translation_uq; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_translation
    ADD CONSTRAINT i18n_translation_uq UNIQUE (i18n_id, locale);


--
-- Name: i18n_translation_style i18n_translations_styles_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_translation_style
    ADD CONSTRAINT i18n_translations_styles_pk PRIMARY KEY (id);


--
-- Name: i18n_type i18n_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_type
    ADD CONSTRAINT i18n_type_pk PRIMARY KEY (id);


--
-- Name: twin_link link_map_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_link
    ADD CONSTRAINT link_map_pk PRIMARY KEY (id);


--
-- Name: link link_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_pk PRIMARY KEY (id);


--
-- Name: link_strength link_strength_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_strength
    ADD CONSTRAINT link_strength_pk PRIMARY KEY (id);


--
-- Name: link_tree_node link_tree_node_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree_node
    ADD CONSTRAINT link_tree_node_pk PRIMARY KEY (id);


--
-- Name: link_tree link_tree_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree
    ADD CONSTRAINT link_tree_pk PRIMARY KEY (id);


--
-- Name: link_trigger link_trigger_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_trigger
    ADD CONSTRAINT link_trigger_pk PRIMARY KEY (id);


--
-- Name: link_type link_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_type
    ADD CONSTRAINT link_type_pk PRIMARY KEY (id);


--
-- Name: link_validator link_validator_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_validator
    ADD CONSTRAINT link_validator_pk PRIMARY KEY (id);


--
-- Name: permission_group permission_group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_group
    ADD CONSTRAINT permission_group_pk PRIMARY KEY (id);


--
-- Name: permission permission_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_pk PRIMARY KEY (id);


--
-- Name: permission_schema_assignee_propagation permission_schema_assignee_propag_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_pk PRIMARY KEY (id);


--
-- Name: permission_schema permission_schema_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema
    ADD CONSTRAINT permission_schema_pk PRIMARY KEY (id);


--
-- Name: permission_schema_space_roles permission_schema_space_roles_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_space_roles
    ADD CONSTRAINT permission_schema_space_roles_pk PRIMARY KEY (id);


--
-- Name: permission_schema_twin_role permission_schema_twin_role_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_twin_role
    ADD CONSTRAINT permission_schema_twin_role_pk PRIMARY KEY (id);


--
-- Name: permission_schema_user_group permission_schema_user_group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user_group
    ADD CONSTRAINT permission_schema_user_group_pk PRIMARY KEY (id);


--
-- Name: permission_schema_user permission_schema_user_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user
    ADD CONSTRAINT permission_schema_user_pk PRIMARY KEY (id);


--
-- Name: search_alias search_alias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_alias
    ADD CONSTRAINT search_alias_pkey PRIMARY KEY (id);


--
-- Name: search_field search_by_user_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_field
    ADD CONSTRAINT search_by_user_type_pk PRIMARY KEY (id);


--
-- Name: search search_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search
    ADD CONSTRAINT search_pk PRIMARY KEY (id);


--
-- Name: search_predicate search_predicate_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_predicate
    ADD CONSTRAINT search_predicate_pk PRIMARY KEY (id);


--
-- Name: space space_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space
    ADD CONSTRAINT space_pk PRIMARY KEY (twin_id);


--
-- Name: space_role space_role_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role
    ADD CONSTRAINT space_role_pk PRIMARY KEY (id);


--
-- Name: space_role_user_group space_role_user_group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user_group
    ADD CONSTRAINT space_role_user_group_pk PRIMARY KEY (id);


--
-- Name: space_role_user space_role_user_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user
    ADD CONSTRAINT space_role_user_pk PRIMARY KEY (id);


--
-- Name: touch touch_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.touch
    ADD CONSTRAINT touch_pk PRIMARY KEY (id);


--
-- Name: twin_action_permission twin_action_permission_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_permission
    ADD CONSTRAINT twin_action_permission_pk PRIMARY KEY (id);


--
-- Name: twin_action twin_action_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action
    ADD CONSTRAINT twin_action_pk PRIMARY KEY (id);


--
-- Name: twin_action_validator twin_action_validator_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_validator
    ADD CONSTRAINT twin_action_validator_pk PRIMARY KEY (id);


--
-- Name: twin_alias twin_alias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias
    ADD CONSTRAINT twin_alias_pkey PRIMARY KEY (id);


--
-- Name: twin_alias_type twin_alias_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias_type
    ADD CONSTRAINT twin_alias_type_pk PRIMARY KEY (alias_type_id);


--
-- Name: twin_attachment twin_attachment_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT twin_attachment_pk PRIMARY KEY (id);


--
-- Name: twin_business_account_alias_counter twin_business_account_alias_counter_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_business_account_alias_counter
    ADD CONSTRAINT twin_business_account_alias_counter_pk PRIMARY KEY (id);


--
-- Name: twin_business_account_alias_counter twin_business_account_alias_counter_uniq; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_business_account_alias_counter
    ADD CONSTRAINT twin_business_account_alias_counter_uniq UNIQUE (twin_class_id, business_account_id);


--
-- Name: twin_class_field twin_class_field_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_pk PRIMARY KEY (id);


--
-- Name: twin_class_owner_type twin_class_owner_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_owner_type
    ADD CONSTRAINT twin_class_owner_pk PRIMARY KEY (id);


--
-- Name: twin_class twin_class_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_pk PRIMARY KEY (id);


--
-- Name: twin_class_schema_map twin_class_schema_map_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema_map
    ADD CONSTRAINT twin_class_schema_map_pk PRIMARY KEY (id);


--
-- Name: twin_class_schema twin_class_schema_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema
    ADD CONSTRAINT twin_class_schema_pk PRIMARY KEY (id);


--
-- Name: twin_comment_action_alien_permission twin_comment_action_alien_permission_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_permission
    ADD CONSTRAINT twin_comment_action_alien_permission_pk PRIMARY KEY (id);


--
-- Name: twin_comment_action_alien_validator twin_comment_action_alien_validator_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_validator
    ADD CONSTRAINT twin_comment_action_alien_validator_pk PRIMARY KEY (id);


--
-- Name: twin_comment_action twin_comment_action_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action
    ADD CONSTRAINT twin_comment_action_pk PRIMARY KEY (id);


--
-- Name: twin_comment_action_self twin_comment_action_self_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_self
    ADD CONSTRAINT twin_comment_action_self_pk PRIMARY KEY (id);


--
-- Name: twin_comment twin_comment_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment
    ADD CONSTRAINT twin_comment_pk PRIMARY KEY (id);


--
-- Name: twin_factory_condition twin_factory_condition_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_condition
    ADD CONSTRAINT twin_factory_condition_pk PRIMARY KEY (id);


--
-- Name: twin_factory_condition_set twin_factory_condition_set_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_condition_set
    ADD CONSTRAINT twin_factory_condition_set_pk PRIMARY KEY (id);


--
-- Name: twin_factory_multiplier_filter twin_factory_multiplier_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_pkey PRIMARY KEY (id);


--
-- Name: twin_factory_multiplier twin_factory_multiplier_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier
    ADD CONSTRAINT twin_factory_multiplier_pk PRIMARY KEY (id);


--
-- Name: twin_factory_pipeline twin_factory_pipeline_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_pk PRIMARY KEY (id);


--
-- Name: twin_factory_pipeline_step twin_factory_pipeline_step_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline_step
    ADD CONSTRAINT twin_factory_pipeline_step_pk PRIMARY KEY (id);


--
-- Name: twin_factory twin_factory_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory
    ADD CONSTRAINT twin_factory_pk PRIMARY KEY (id);


--
-- Name: twin_field_data_list twin_field_data_list_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_data_list
    ADD CONSTRAINT twin_field_data_list_pk PRIMARY KEY (id);


--
-- Name: twin_field_simple twin_field_simple_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_simple
    ADD CONSTRAINT twin_field_simple_pk PRIMARY KEY (id);


--
-- Name: twin_field_user twin_field_user_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_user
    ADD CONSTRAINT twin_field_user_pk PRIMARY KEY (id);


--
-- Name: twin_marker twin_marker_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_marker
    ADD CONSTRAINT twin_marker_pk PRIMARY KEY (id);


--
-- Name: twin twin_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_pk PRIMARY KEY (id);


--
-- Name: twin_role twin_role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_role
    ADD CONSTRAINT twin_role_pkey PRIMARY KEY (id);


--
-- Name: twin_status_group_map twin_status_group_map_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_group_map
    ADD CONSTRAINT twin_status_group_map_pk PRIMARY KEY (id);


--
-- Name: twin_status_group twin_status_group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_group
    ADD CONSTRAINT twin_status_group_pk PRIMARY KEY (id);


--
-- Name: twin_status twin_status_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status
    ADD CONSTRAINT twin_status_pk PRIMARY KEY (id);


--
-- Name: twin_status_transition_trigger twin_status_transition_trigger_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_transition_trigger
    ADD CONSTRAINT twin_status_transition_trigger_pk PRIMARY KEY (id);


--
-- Name: twin_status_transition_type twin_status_transition_type_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_transition_type
    ADD CONSTRAINT twin_status_transition_type_pk PRIMARY KEY (id);


--
-- Name: twin_tag twin_tag_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_tag
    ADD CONSTRAINT twin_tag_pk PRIMARY KEY (id);


--
-- Name: twin_touch twin_touch_uq; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_touch
    ADD CONSTRAINT twin_touch_uq UNIQUE (twin_id, touch_id, user_id);


--
-- Name: twin_work twin_work_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_work
    ADD CONSTRAINT twin_work_pk PRIMARY KEY (id);


--
-- Name: twinflow twinflow_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow
    ADD CONSTRAINT twinflow_pk PRIMARY KEY (id);


--
-- Name: twinflow_schema_map twinflow_schema_map_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema_map
    ADD CONSTRAINT twinflow_schema_map_pk PRIMARY KEY (id);


--
-- Name: twinflow_schema twinflow_schema_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema
    ADD CONSTRAINT twinflow_schema_pk PRIMARY KEY (id);


--
-- Name: twinflow_transition_alias twinflow_transition_group_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_alias
    ADD CONSTRAINT twinflow_transition_group_pkey PRIMARY KEY (id);


--
-- Name: twinflow_transition twinflow_transition_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_pk PRIMARY KEY (id);


--
-- Name: twinflow_transition_trigger twinflow_transition_trigger_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_trigger
    ADD CONSTRAINT twinflow_transition_trigger_pk PRIMARY KEY (id);


--
-- Name: twinflow_transition twinflow_transition_uniq; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_uniq UNIQUE NULLS NOT DISTINCT (twinflow_id, src_twin_status_id, twinflow_transition_alias_id);


--
-- Name: twinflow_transition_validator twinflow_transition_validator_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_validator
    ADD CONSTRAINT twinflow_transition_validator_pk PRIMARY KEY (id);


--
-- Name: user_group_map user_group_map_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_map
    ADD CONSTRAINT user_group_map_pkey PRIMARY KEY (id);


--
-- Name: user_group user_group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group
    ADD CONSTRAINT user_group_pk PRIMARY KEY (id);


--
-- Name: user_group_type user_group_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_type
    ADD CONSTRAINT user_group_type_pkey PRIMARY KEY (id);


--
-- Name: user user_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pk PRIMARY KEY (id);


--
-- Name: user_status user_status_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_status
    ADD CONSTRAINT user_status_pk PRIMARY KEY (id);


--
-- Name: widget widget_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT widget_pk PRIMARY KEY (id);


--
-- Name: business_account_user_business_account_id_user_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX business_account_user_business_account_id_user_id_uindex ON public.business_account_user USING btree (business_account_id, user_id);


--
-- Name: card_access_twin_class_id_order_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX card_access_twin_class_id_order_uindex ON public.card_access USING btree (twin_class_id, "order");


--
-- Name: card_layout_channel_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX card_layout_channel_key_uindex ON public.card_layout USING btree (channel_id, key);


--
-- Name: card_layout_position_card_layout_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX card_layout_position_card_layout_id_key_uindex ON public.card_layout_position USING btree (card_layout_id, key);


--
-- Name: card_widget_card_id_card_layout_position_id_in_position_ord; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX card_widget_card_id_card_layout_position_id_in_position_ord ON public.card_widget USING btree (card_id, card_layout_position_id, in_position_order);


--
-- Name: data_list_domain_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX data_list_domain_id_key_uindex ON public.data_list USING btree (domain_id, key);


--
-- Name: data_list_option_data_list_id_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX data_list_option_data_list_id_business_account_id_index ON public.data_list_option USING btree (data_list_id, business_account_id);


--
-- Name: domain_business_account_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX domain_business_account_business_account_id_index ON public.domain_business_account USING btree (business_account_id);


--
-- Name: domain_business_account_domain_id_business_account_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX domain_business_account_domain_id_business_account_id_uindex ON public.domain_business_account USING btree (domain_id, business_account_id);


--
-- Name: domain_type_default_token_handler_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX domain_type_default_token_handler_featurer_id_index ON public.domain_type USING btree (default_token_handler_featurer_id);


--
-- Name: domain_type_default_user_group_manager_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX domain_type_default_user_group_manager_featurer_id_index ON public.domain_type USING btree (default_user_group_manager_featurer_id);


--
-- Name: domain_type_domain_initiator_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX domain_type_domain_initiator_featurer_id_index ON public.domain_type USING btree (domain_initiator_featurer_id);


--
-- Name: domain_user_domain_id_user_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX domain_user_domain_id_user_id_uindex ON public.domain_user USING btree (domain_id, user_id);


--
-- Name: domain_user_user_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX domain_user_user_id_index ON public.domain_user USING btree (user_id);


--
-- Name: error__uniq; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX error__uniq ON public.error USING btree (code_local, code_external);


--
-- Name: history_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX history_twin_id_index ON public.history USING btree (twin_id);


--
-- Name: i18n_key_uniq; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX i18n_key_uniq ON public.i18n USING btree (key);


--
-- Name: idx_hierarchy_tree; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_hierarchy_tree ON public.twin USING gist (hierarchy_tree);


--
-- Name: idx_twin_marker_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_twin_marker_unique ON public.twin_marker USING btree (twin_id, marker_data_list_option_id);


--
-- Name: idx_twin_tag_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_twin_tag_unique ON public.twin_tag USING btree (twin_id, tag_data_list_option_id);


--
-- Name: link_domain_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX link_domain_id_index ON public.link USING btree (domain_id);


--
-- Name: link_dst_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX link_dst_twin_class_id_index ON public.link USING btree (dst_twin_class_id);


--
-- Name: link_src_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX link_src_twin_class_id_index ON public.link USING btree (src_twin_class_id);


--
-- Name: link_trigger_link_id_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX link_trigger_link_id_order ON public.link_trigger USING btree (link_id, "order");


--
-- Name: link_validator_link_id_order_uind; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX link_validator_link_id_order_uind ON public.link_validator USING btree (link_id, "order");


--
-- Name: permission_group_domain_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX permission_group_domain_id_key_uindex ON public.permission_group USING btree (domain_id, key);


--
-- Name: permission_group_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX permission_group_twin_class_id_index ON public.permission_group USING btree (twin_class_id);


--
-- Name: permission_permission_group_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX permission_permission_group_id_key_uindex ON public.permission USING btree (permission_group_id, key);


--
-- Name: permission_schema_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX permission_schema_business_account_id_index ON public.permission_schema USING btree (business_account_id);


--
-- Name: permission_schema_domain_id_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX permission_schema_domain_id_business_account_id_index ON public.permission_schema USING btree (domain_id, business_account_id);


--
-- Name: permission_schema_space_roles_permission_schema_id_permission_i; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX permission_schema_space_roles_permission_schema_id_permission_i ON public.permission_schema_space_roles USING btree (permission_schema_id, permission_id, space_role_id);


--
-- Name: permission_schema_user_group_permission_schema_id_permission_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX permission_schema_user_group_permission_schema_id_permission_id ON public.permission_schema_user_group USING btree (permission_schema_id, permission_id, user_group_id);


--
-- Name: permission_schema_user_permission_schema_id_permission_id_user_; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX permission_schema_user_permission_schema_id_permission_id_user_ ON public.permission_schema_user USING btree (permission_schema_id, permission_id, user_id);


--
-- Name: search_alias_domain_id_alias_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX search_alias_domain_id_alias_uindex ON public.search_alias USING btree (domain_id, alias);


--
-- Name: search_predicate_search_field_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX search_predicate_search_field_id_index ON public.search_predicate USING btree (search_field_id);


--
-- Name: search_predicate_search_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX search_predicate_search_id_index ON public.search_predicate USING btree (search_id);


--
-- Name: space_role_twin_class_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX space_role_twin_class_id_key_uindex ON public.space_role USING btree (twin_class_id, key);


--
-- Name: space_role_user_group_twin_id_space_role_id_user_group_id_uinde; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX space_role_user_group_twin_id_space_role_id_user_group_id_uinde ON public.space_role_user_group USING btree (twin_id, space_role_id, user_group_id);


--
-- Name: space_role_user_twin_id_space_role_id_user_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX space_role_user_twin_id_space_role_id_user_id_uindex ON public.space_role_user USING btree (twin_id, space_role_id, user_id);


--
-- Name: twin_action_permission_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_action_permission_twin_class_id_index ON public.twin_action_permission USING btree (twin_class_id);


--
-- Name: twin_action_validator_twin_action_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_action_validator_twin_action_id_index ON public.twin_action_validator USING btree (twin_action_id);


--
-- Name: twin_action_validator_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_action_validator_twin_class_id_index ON public.twin_action_validator USING btree (twin_class_id);


--
-- Name: twin_alias_b_k; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_alias_b_k ON public.twin_alias USING btree (domain_id, business_account_id, alias_value) WHERE ((twin_alias_type_id)::text = ANY ((ARRAY['B'::character varying, 'K'::character varying])::text[]));


--
-- Name: twin_alias_d_c_s; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_alias_d_c_s ON public.twin_alias USING btree (domain_id, alias_value) WHERE ((twin_alias_type_id)::text = ANY ((ARRAY['D'::character varying, 'C'::character varying, 'S'::character varying])::text[]));


--
-- Name: twin_alias_t; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_alias_t ON public.twin_alias USING btree (domain_id, user_id, alias_value) WHERE ((twin_alias_type_id)::text = 'T'::text);


--
-- Name: twin_assigner_user_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_assigner_user_id_index ON public.twin USING btree (assigner_user_id);


--
-- Name: twin_attachment_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_attachment_twin_id_index ON public.twin_attachment USING btree (twin_id);


--
-- Name: twin_attachment_twinflow_transition_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_attachment_twinflow_transition_id_index ON public.twin_attachment USING btree (twinflow_transition_id);


--
-- Name: twin_class_action_permission_twin_action_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_action_permission_twin_action_id_index ON public.twin_action_permission USING btree (twin_action_id);


--
-- Name: twin_class_action_validator_order_uniq; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_class_action_validator_order_uniq ON public.twin_action_validator USING btree (twin_class_id, twin_action_id, "order");


--
-- Name: twin_class_action_validator_twin_validator_featurer_id_; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_action_validator_twin_validator_featurer_id_ ON public.twin_action_validator USING btree (twin_validator_featurer_id);


--
-- Name: twin_class_domain_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_class_domain_id_key_uindex ON public.twin_class USING btree (domain_id, key);


--
-- Name: twin_class_extends_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_extends_twin_class_id_index ON public.twin_class USING btree (extends_twin_class_id);


--
-- Name: twin_class_field_edit_permission_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_field_edit_permission_id_index ON public.twin_class_field USING btree (edit_permission_id);


--
-- Name: twin_class_field_field_typer_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_field_field_typer_featurer_id_index ON public.twin_class_field USING btree (field_typer_featurer_id);


--
-- Name: twin_class_field_twin_class_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_class_field_twin_class_id_key_uindex ON public.twin_class_field USING btree (twin_class_id, key);


--
-- Name: twin_class_field_view_permission_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_field_view_permission_id_index ON public.twin_class_field USING btree (view_permission_id);


--
-- Name: twin_class_head_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_head_twin_class_id_index ON public.twin_class USING btree (head_twin_class_id);


--
-- Name: twin_class_marker_data_list_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_marker_data_list_id_index ON public.twin_class USING btree (marker_data_list_id);


--
-- Name: twin_class_schema_domain_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_schema_domain_id_index ON public.twin_class_schema USING btree (domain_id);


--
-- Name: twin_class_schema_map_create_permission_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_schema_map_create_permission_id_index ON public.twin_class_schema_map USING btree (create_permission_id);


--
-- Name: twin_class_schema_map_twin_class_schema_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_schema_map_twin_class_schema_id_index ON public.twin_class_schema_map USING btree (twin_class_schema_id);


--
-- Name: twin_class_tag_data_list_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_tag_data_list_id_index ON public.twin_class USING btree (tag_data_list_id);


--
-- Name: twin_class_twin_class_owner_type_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_class_twin_class_owner_type_id_index ON public.twin_class USING btree (twin_class_owner_type_id);


--
-- Name: twin_comment_action_alien_permission_twin_class_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_action_alien_permission_twin_class_id_idx ON public.twin_comment_action_alien_permission USING btree (twin_class_id);


--
-- Name: twin_comment_action_alien_permission_twin_comment_action_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_action_alien_permission_twin_comment_action_id_idx ON public.twin_comment_action_alien_permission USING btree (twin_comment_action_id);


--
-- Name: twin_comment_action_alien_valid_twin_validator_featurer_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_action_alien_valid_twin_validator_featurer_id_idx ON public.twin_comment_action_alien_validator USING btree (twin_validator_featurer_id);


--
-- Name: twin_comment_action_alien_validator_order_uniq; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_comment_action_alien_validator_order_uniq ON public.twin_comment_action_alien_validator USING btree (twin_class_id, twin_comment_action_id, "order");


--
-- Name: twin_comment_action_alien_validator_twin_class_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_action_alien_validator_twin_class_id_idx ON public.twin_comment_action_alien_validator USING btree (twin_class_id);


--
-- Name: twin_comment_action_alien_validator_twin_comment_action_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_action_alien_validator_twin_comment_action_id_idx ON public.twin_comment_action_alien_validator USING btree (twin_comment_action_id);


--
-- Name: twin_comment_action_self_twin_class_id_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_action_self_twin_class_id_idx ON public.twin_comment_action_self USING btree (twin_class_id);


--
-- Name: twin_comment_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_comment_twin_id_index ON public.twin_comment USING btree (twin_id);


--
-- Name: twin_created_by_user_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_created_by_user_id_index ON public.twin USING btree (created_by_user_id);


--
-- Name: twin_factory_condition_conditioner_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_condition_conditioner_featurer_id_index ON public.twin_factory_condition USING btree (conditioner_featurer_id);


--
-- Name: twin_factory_condition_twin_factory_condition_set_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_condition_twin_factory_condition_set_id_index ON public.twin_factory_condition USING btree (twin_factory_condition_set_id);


--
-- Name: twin_factory_domain_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_domain_id_index ON public.twin_factory USING btree (domain_id);


--
-- Name: twin_factory_domain_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_factory_domain_id_key_uindex ON public.twin_factory USING btree (domain_id, key);


--
-- Name: twin_factory_multiplier_filter_condition_set_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_multiplier_filter_condition_set_id_index ON public.twin_factory_multiplier_filter USING btree (twin_factory_condition_set_id);


--
-- Name: twin_factory_multiplier_filter_multiplier_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_multiplier_filter_multiplier_id_index ON public.twin_factory_multiplier_filter USING btree (twin_factory_multiplier_id);


--
-- Name: twin_factory_multiplier_input_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_multiplier_input_twin_class_id_index ON public.twin_factory_multiplier USING btree (input_twin_class_id);


--
-- Name: twin_factory_multiplier_multiplier_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_multiplier_multiplier_featurer_id_index ON public.twin_factory_multiplier USING btree (multiplier_featurer_id);


--
-- Name: twin_factory_multiplier_twin_factory_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_multiplier_twin_factory_id_index ON public.twin_factory_multiplier USING btree (twin_factory_id);


--
-- Name: twin_factory_pipeline_input_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_input_twin_class_id_index ON public.twin_factory_pipeline USING btree (input_twin_class_id);


--
-- Name: twin_factory_pipeline_next_twin_factory_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_next_twin_factory_id_index ON public.twin_factory_pipeline USING btree (next_twin_factory_id);


--
-- Name: twin_factory_pipeline_step_filler_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_step_filler_featurer_id_index ON public.twin_factory_pipeline_step USING btree (filler_featurer_id);


--
-- Name: twin_factory_pipeline_step_twin_factory_condition_set_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_step_twin_factory_condition_set_id_index ON public.twin_factory_pipeline_step USING btree (twin_factory_condition_set_id);


--
-- Name: twin_factory_pipeline_step_twin_factory_pipeline_id_order_uinde; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_factory_pipeline_step_twin_factory_pipeline_id_order_uinde ON public.twin_factory_pipeline_step USING btree (twin_factory_pipeline_id, "order");


--
-- Name: twin_factory_pipeline_template_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_template_twin_id_index ON public.twin_factory_pipeline USING btree (template_twin_id);


--
-- Name: twin_factory_pipeline_twin_factory_condition_set_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_twin_factory_condition_set_id_index ON public.twin_factory_pipeline USING btree (twin_factory_condition_set_id);


--
-- Name: twin_factory_pipeline_twin_factory_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_factory_pipeline_twin_factory_id_index ON public.twin_factory_pipeline USING btree (twin_factory_id);


--
-- Name: twin_field_data_list_data_list_option_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_field_data_list_data_list_option_id_index ON public.twin_field_data_list USING btree (data_list_option_id);


--
-- Name: twin_field_data_list_twin_class_field_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_field_data_list_twin_class_field_id_index ON public.twin_field_data_list USING btree (twin_class_field_id);


--
-- Name: twin_field_data_list_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_field_data_list_twin_id_index ON public.twin_field_data_list USING btree (twin_id);


--
-- Name: twin_field_simple_twin_id_twin_class_field_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_field_simple_twin_id_twin_class_field_id_uindex ON public.twin_field_simple USING btree (twin_id, twin_class_field_id);


--
-- Name: twin_field_user_twin_class_field_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_field_user_twin_class_field_id_index ON public.twin_field_user USING btree (twin_class_field_id);


--
-- Name: twin_field_user_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_field_user_twin_id_index ON public.twin_field_user USING btree (twin_id);


--
-- Name: twin_head_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_head_twin_id_index ON public.twin USING btree (head_twin_id);


--
-- Name: twin_link_dst_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_link_dst_twin_id_index ON public.twin_link USING btree (dst_twin_id);


--
-- Name: twin_link_link_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_link_link_id_index ON public.twin_link USING btree (link_id);


--
-- Name: twin_link_src_twin_id_dst_twin_id_link_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_link_src_twin_id_dst_twin_id_link_id_uindex ON public.twin_link USING btree (src_twin_id, dst_twin_id, link_id);


--
-- Name: twin_link_src_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_link_src_twin_id_index ON public.twin_link USING btree (src_twin_id);


--
-- Name: twin_marker_marker_data_list_option_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_marker_marker_data_list_option_id_index ON public.twin_marker USING btree (marker_data_list_option_id);


--
-- Name: twin_marker_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_marker_twin_id_index ON public.twin_marker USING btree (twin_id);


--
-- Name: twin_owner_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_owner_business_account_id_index ON public.twin USING btree (owner_business_account_id);


--
-- Name: twin_owner_user_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_owner_user_id_index ON public.twin USING btree (owner_user_id);


--
-- Name: twin_status_group_domain_id_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_status_group_domain_id_key_uindex ON public.twin_status_group USING btree (domain_id, key);


--
-- Name: twin_status_group_map_twin_status_id_twin_status_group_id_uinde; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_status_group_map_twin_status_id_twin_status_group_id_uinde ON public.twin_status_group_map USING btree (twin_status_group_id, twin_status_id);


--
-- Name: twin_status_transition_trigger_transition_trigger_featurer_id_i; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_status_transition_trigger_transition_trigger_featurer_id_i ON public.twin_status_transition_trigger USING btree (transition_trigger_featurer_id);


--
-- Name: twin_status_transition_trigger_twin_status_transition_id_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twin_status_transition_trigger_twin_status_transition_id_order ON public.twin_status_transition_trigger USING btree (twin_status_id, "order");


--
-- Name: twin_status_twins_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_status_twins_class_id_index ON public.twin_status USING btree (twins_class_id);


--
-- Name: twin_tag_tag_data_list_option_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_tag_tag_data_list_option_id_index ON public.twin_tag USING btree (tag_data_list_option_id);


--
-- Name: twin_tag_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_tag_twin_id_index ON public.twin_tag USING btree (twin_id);


--
-- Name: twin_twin_class_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_twin_class_id_index ON public.twin USING btree (twin_class_id);


--
-- Name: twin_twin_status_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_twin_status_id_index ON public.twin USING btree (twin_status_id);


--
-- Name: twin_work_author_user_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_work_author_user_id_index ON public.twin_work USING btree (author_user_id);


--
-- Name: twin_work_twin_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twin_work_twin_id_index ON public.twin_work USING btree (twin_id);


--
-- Name: twinflow_schema_domain_id_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_schema_domain_id_business_account_id_index ON public.twinflow_schema USING btree (domain_id, business_account_id);


--
-- Name: twinflow_schema_map_twinflow_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_schema_map_twinflow_id_index ON public.twinflow_schema_map USING btree (twinflow_id);


--
-- Name: twinflow_schema_map_twinflow_schema_id_twin_class_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twinflow_schema_map_twinflow_schema_id_twin_class_id_uindex ON public.twinflow_schema_map USING btree (twinflow_schema_id, twin_class_id);


--
-- Name: twinflow_transition_alias_domain_id_alias_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twinflow_transition_alias_domain_id_alias_uindex ON public.twinflow_transition_alias USING btree (domain_id, alias);


--
-- Name: twinflow_transition_alias_domain_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_alias_domain_id_index ON public.twinflow_transition_alias USING btree (domain_id);


--
-- Name: twinflow_transition_dst_twin_status_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_dst_twin_status_id_index ON public.twinflow_transition USING btree (dst_twin_status_id);


--
-- Name: twinflow_transition_inbuilt_twin_factory_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_inbuilt_twin_factory_id_index ON public.twinflow_transition USING btree (inbuilt_twin_factory_id);


--
-- Name: twinflow_transition_permission_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_permission_id_index ON public.twinflow_transition USING btree (permission_id);


--
-- Name: twinflow_transition_trigger_transition_trigger_featurer_id_inde; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_trigger_transition_trigger_featurer_id_inde ON public.twinflow_transition_trigger USING btree (transition_trigger_featurer_id);


--
-- Name: twinflow_transition_trigger_twinflow_transition_id_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twinflow_transition_trigger_twinflow_transition_id_order ON public.twinflow_transition_trigger USING btree (twinflow_transition_id, "order");


--
-- Name: twinflow_transition_twinflow_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_twinflow_id_index ON public.twinflow_transition USING btree (twinflow_id);


--
-- Name: twinflow_transition_validator_transition_validator_featurer_id_; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX twinflow_transition_validator_transition_validator_featurer_id_ ON public.twinflow_transition_validator USING btree (twin_validator_featurer_id);


--
-- Name: twinflow_transition_validator_twinflow_transition_id_order_uind; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX twinflow_transition_validator_twinflow_transition_id_order_uind ON public.twinflow_transition_validator USING btree (twinflow_transition_id, "order");


--
-- Name: user_group_domain_id_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_group_domain_id_business_account_id_index ON public.user_group USING btree (domain_id, business_account_id);


--
-- Name: user_group_map_user_group_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_group_map_user_group_id_index ON public.user_group_map USING btree (user_group_id);


--
-- Name: user_group_map_user_id_business_account_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_group_map_user_id_business_account_id_index ON public.user_group_map USING btree (user_id, business_account_id);


--
-- Name: user_group_type_slugger_featurer_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_group_type_slugger_featurer_id_index ON public.user_group_type USING btree (slugger_featurer_id);


--
-- Name: user_group_user_group_type_id_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX user_group_user_group_type_id_index ON public.user_group USING btree (user_group_type_id);


--
-- Name: widget_key_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX widget_key_uindex ON public.widget USING btree (key);


--
-- Name: user after_user_insert_or_update; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER after_user_insert_or_update AFTER INSERT OR UPDATE ON public."user" FOR EACH ROW EXECUTE FUNCTION public.user_insert_or_update();


--
-- Name: twin_class hierarchy_twin_class_extends_update_tree_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER hierarchy_twin_class_extends_update_tree_trigger AFTER INSERT OR UPDATE OF extends_twin_class_id ON public.twin_class FOR EACH ROW EXECUTE FUNCTION public.hierarchy_twin_class_extends_process_tree_update();


--
-- Name: twin_class hierarchy_twin_class_head_update_tree_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER hierarchy_twin_class_head_update_tree_trigger AFTER INSERT OR UPDATE OF head_twin_class_id ON public.twin_class FOR EACH ROW EXECUTE FUNCTION public.hierarchy_twin_class_head_process_tree_update();


--
-- Name: twin_class hierarchyrecalculatetrigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER hierarchyrecalculatetrigger AFTER UPDATE OF permission_schema_space, twinflow_schema_space, twin_class_schema_space, alias_space ON public.twin_class FOR EACH ROW EXECUTE FUNCTION public.hierarchyrecalculateforclasstwins();


--
-- Name: twin hierarchyupdatetreetrigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER hierarchyupdatetreetrigger AFTER INSERT OR UPDATE OF head_twin_id ON public.twin FOR EACH ROW EXECUTE FUNCTION public.hierarchyprocesstreeupdate();


--
-- Name: business_account_user business_account_user_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.business_account_user
    ADD CONSTRAINT business_account_user_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: business_account business_account_user_group_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.business_account
    ADD CONSTRAINT business_account_user_group_id_fk FOREIGN KEY (owner_user_group_id) REFERENCES public.user_group(id) ON UPDATE CASCADE;


--
-- Name: business_account_user business_account_user_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.business_account_user
    ADD CONSTRAINT business_account_user_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: card_access card_access_card_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_access
    ADD CONSTRAINT card_access_card_id_fk FOREIGN KEY (card_id) REFERENCES public.card(id) ON UPDATE CASCADE;


--
-- Name: card_access card_access_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_access
    ADD CONSTRAINT card_access_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: card card_card_layout_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT card_card_layout_id_fk FOREIGN KEY (card_layout_id) REFERENCES public.card_layout(id) ON UPDATE CASCADE;


--
-- Name: card card_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT card_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: card_layout card_layout_channel_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_layout
    ADD CONSTRAINT card_layout_channel_id_fk FOREIGN KEY (channel_id) REFERENCES public.channel(id) ON UPDATE CASCADE;


--
-- Name: card_layout_position card_layout_position_card_layout_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_layout_position
    ADD CONSTRAINT card_layout_position_card_layout_id_fk FOREIGN KEY (card_layout_id) REFERENCES public.card_layout(id) ON UPDATE CASCADE;


--
-- Name: card_override card_override_card_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_override
    ADD CONSTRAINT card_override_card_id_fk FOREIGN KEY (override_card_id) REFERENCES public.card(id) ON UPDATE CASCADE;


--
-- Name: card_override card_override_card_layout_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_override
    ADD CONSTRAINT card_override_card_layout_id_fk FOREIGN KEY (card_layout_id) REFERENCES public.card_layout(id) ON UPDATE CASCADE;


--
-- Name: card_override card_override_channel_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_override
    ADD CONSTRAINT card_override_channel_id_fk FOREIGN KEY (override_for_channel_id) REFERENCES public.channel(id) ON UPDATE CASCADE;


--
-- Name: card_override card_override_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_override
    ADD CONSTRAINT card_override_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: card_widget card_widget_card_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget
    ADD CONSTRAINT card_widget_card_id_fk FOREIGN KEY (card_id) REFERENCES public.card(id) ON UPDATE CASCADE;


--
-- Name: card_widget card_widget_card_layout_position_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget
    ADD CONSTRAINT card_widget_card_layout_position_id_fk FOREIGN KEY (card_layout_position_id) REFERENCES public.card_layout_position(id);


--
-- Name: card_widget_override card_widget_override_card_layout_position_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget_override
    ADD CONSTRAINT card_widget_override_card_layout_position_id_fk FOREIGN KEY (card_layout_position_id) REFERENCES public.card_layout_position(id) ON UPDATE CASCADE;


--
-- Name: card_widget_override card_widget_override_card_widget_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget_override
    ADD CONSTRAINT card_widget_override_card_widget_id_fk FOREIGN KEY (override_card_widget_id) REFERENCES public.card_widget(id) ON UPDATE CASCADE;


--
-- Name: card_widget_override card_widget_override_channel_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget_override
    ADD CONSTRAINT card_widget_override_channel_id_fk FOREIGN KEY (override_for_channel_id) REFERENCES public.channel(id) ON UPDATE CASCADE;


--
-- Name: card_widget card_widget_widget_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_widget
    ADD CONSTRAINT card_widget_widget_id_fk FOREIGN KEY (widget_id) REFERENCES public.widget(id) ON UPDATE CASCADE;


--
-- Name: data_list data_list_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list
    ADD CONSTRAINT data_list_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: data_list_option data_list_option_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list_option
    ADD CONSTRAINT data_list_option_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: data_list_option data_list_option_data_list_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list_option
    ADD CONSTRAINT data_list_option_data_list_id_fk FOREIGN KEY (data_list_id) REFERENCES public.data_list(id) ON UPDATE CASCADE;


--
-- Name: data_list_option data_list_option_data_list_option_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list_option
    ADD CONSTRAINT data_list_option_data_list_option_status_id_fk FOREIGN KEY (data_list_option_status_id) REFERENCES public.data_list_option_status(id) ON UPDATE CASCADE;


--
-- Name: data_list_option data_list_option_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.data_list_option
    ADD CONSTRAINT data_list_option_i18n_id_fk FOREIGN KEY (option_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: domain_type default_domain_token_handler_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_type
    ADD CONSTRAINT default_domain_token_handler_featurer_id_fk FOREIGN KEY (default_token_handler_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: domain domain_ba_initiator_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_ba_initiator_featurer_id_fk FOREIGN KEY (business_account_initiator_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: domain_business_account domain_business_account_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_business_account
    ADD CONSTRAINT domain_business_account_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: domain_business_account domain_business_account_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_business_account
    ADD CONSTRAINT domain_business_account_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: domain_business_account domain_business_account_permission_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_business_account
    ADD CONSTRAINT domain_business_account_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id) ON UPDATE CASCADE;


--
-- Name: domain_business_account domain_business_account_twin_class_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_business_account
    ADD CONSTRAINT domain_business_account_twin_class_schema_id_fk FOREIGN KEY (twin_class_schema_id) REFERENCES public.twin_class_schema(id) ON UPDATE CASCADE;


--
-- Name: domain_business_account domain_business_account_twinflow_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_business_account
    ADD CONSTRAINT domain_business_account_twinflow_schema_id_fk FOREIGN KEY (twinflow_schema_id) REFERENCES public.twinflow_schema(id) ON UPDATE CASCADE;


--
-- Name: domain domain_default_i18n_locale_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_default_i18n_locale_id_fk FOREIGN KEY (default_i18n_locale_id) REFERENCES public.i18n_locale(locale);


--
-- Name: domain domain_domain_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_domain_type_id_fk FOREIGN KEY (domain_type_id) REFERENCES public.domain_type(id) ON UPDATE CASCADE;


--
-- Name: domain_type domain_initiator_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_type
    ADD CONSTRAINT domain_initiator_featurer_id_fk FOREIGN KEY (domain_initiator_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: domain_locale domain_locale_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_locale
    ADD CONSTRAINT domain_locale_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id);


--
-- Name: domain_locale domain_locale_i18n_locale_locale_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_locale
    ADD CONSTRAINT domain_locale_i18n_locale_locale_fk FOREIGN KEY (i18n_locale_id) REFERENCES public.i18n_locale(locale);


--
-- Name: domain domain_permission_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id) ON UPDATE CASCADE;


--
-- Name: domain domain_token_checker_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_token_checker_featurer_id_fk FOREIGN KEY (token_handler_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: domain domain_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_twin_class_id_fk FOREIGN KEY (ancestor_twin_class_id) REFERENCES public.twin_class(id);


--
-- Name: domain domain_twin_class_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_twin_class_schema_id_fk FOREIGN KEY (twin_class_schema_id) REFERENCES public.twin_class_schema(id) ON UPDATE CASCADE;


--
-- Name: domain domain_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_twin_id_fk FOREIGN KEY (business_account_template_twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE;


--
-- Name: domain domain_twinflow_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_twinflow_schema_id_fk FOREIGN KEY (twinflow_schema_id) REFERENCES public.twinflow_schema(id) ON UPDATE CASCADE;


--
-- Name: domain_user domain_user_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_user
    ADD CONSTRAINT domain_user_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: domain domain_user_group_manager_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_user_group_manager_featurer_id_fk FOREIGN KEY (user_group_manager_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: domain_type domain_user_group_manager_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_type
    ADD CONSTRAINT domain_user_group_manager_featurer_id_fk FOREIGN KEY (default_user_group_manager_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: domain_user domain_user_i18n_locale_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_user
    ADD CONSTRAINT domain_user_i18n_locale_id_fk FOREIGN KEY (i18n_locale_id) REFERENCES public.i18n_locale(locale);


--
-- Name: domain_user domain_user_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.domain_user
    ADD CONSTRAINT domain_user_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: error error__client_msg_i18n_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.error
    ADD CONSTRAINT error__client_msg_i18n_id FOREIGN KEY (client_msg_i18n_id) REFERENCES public.i18n(id);


--
-- Name: featurer featurer_featurer_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer
    ADD CONSTRAINT featurer_featurer_type_id_fk FOREIGN KEY (featurer_type_id) REFERENCES public.featurer_type(id) ON UPDATE CASCADE;


--
-- Name: featurer_injection featurer_injections_injector_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_injection
    ADD CONSTRAINT featurer_injections_injector_featurer_id_fk FOREIGN KEY (injector_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: featurer_param featurer_param_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_param
    ADD CONSTRAINT featurer_param_featurer_id_fk FOREIGN KEY (featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: featurer_param featurer_param_featurer_param_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.featurer_param
    ADD CONSTRAINT featurer_param_featurer_param_type_id_fk FOREIGN KEY (featurer_param_type_id) REFERENCES public.featurer_param_type(id) ON UPDATE CASCADE;


--
-- Name: twin_attachment fk_attachment_fieldclass; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT fk_attachment_fieldclass FOREIGN KEY (twin_class_field_id) REFERENCES public.twin_class_field(id);


--
-- Name: twin fk_twin_alias_space_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT fk_twin_alias_space_id FOREIGN KEY (alias_space_id) REFERENCES public.twin(id);


--
-- Name: twin fk_twin_permission_schema_space_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT fk_twin_permission_schema_space_id FOREIGN KEY (permission_schema_space_id) REFERENCES public.twin(id);


--
-- Name: twin fk_twin_twin_class_schema_space_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT fk_twin_twin_class_schema_space_id FOREIGN KEY (twin_class_schema_space_id) REFERENCES public.twin(id);


--
-- Name: twin fk_twin_twinflow_schema_space_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT fk_twin_twinflow_schema_space_id FOREIGN KEY (twinflow_schema_space_id) REFERENCES public.twin(id);


--
-- Name: twin fk_twin_view_permission_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT fk_twin_view_permission_id FOREIGN KEY (view_permission_id) REFERENCES public.permission(id);


--
-- Name: twin_class fk_twinclass_view_permission_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT fk_twinclass_view_permission_id FOREIGN KEY (view_permission_id) REFERENCES public.permission(id);


--
-- Name: history history_history_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_history_type_id_fk FOREIGN KEY (history_type_id) REFERENCES public.history_type(id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: history history_twin_class_field_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES public.twin_class_field(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: history history_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: history_type_config_domain history_type_config_domain_history_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_domain
    ADD CONSTRAINT history_type_config_domain_history_type_id_fk FOREIGN KEY (history_type_id) REFERENCES public.history_type(id);


--
-- Name: history_type_config_domain history_type_config_domain_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_domain
    ADD CONSTRAINT history_type_config_domain_i18n_id_fk FOREIGN KEY (message_template_i18n_id) REFERENCES public.i18n(id);


--
-- Name: history_type_config_domain history_type_config_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_domain
    ADD CONSTRAINT history_type_config_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id);


--
-- Name: history_type_config_domain history_type_config_domain_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_domain
    ADD CONSTRAINT history_type_config_domain_status_id_fk FOREIGN KEY (history_type_status_id) REFERENCES public.history_type_status(id) ON UPDATE CASCADE;


--
-- Name: history_type_config_twin_class_field history_type_config_twin_class_field_field_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class_field
    ADD CONSTRAINT history_type_config_twin_class_field_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES public.twin_class_field(id);


--
-- Name: history_type_config_twin_class_field history_type_config_twin_class_field_history_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class_field
    ADD CONSTRAINT history_type_config_twin_class_field_history_type_id_fk FOREIGN KEY (history_type_id) REFERENCES public.history_type(id);


--
-- Name: history_type_config_twin_class_field history_type_config_twin_class_field_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class_field
    ADD CONSTRAINT history_type_config_twin_class_field_i18n_id_fk FOREIGN KEY (message_template_i18n_id) REFERENCES public.i18n(id);


--
-- Name: history_type_config_twin_class_field history_type_config_twin_class_field_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class_field
    ADD CONSTRAINT history_type_config_twin_class_field_status_id_fk FOREIGN KEY (history_type_status_id) REFERENCES public.history_type_status(id) ON UPDATE CASCADE;


--
-- Name: history_type_config_twin_class history_type_config_twin_class_history_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class
    ADD CONSTRAINT history_type_config_twin_class_history_type_id_fk FOREIGN KEY (history_type_id) REFERENCES public.history_type(id);


--
-- Name: history_type_config_twin_class history_type_config_twin_class_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class
    ADD CONSTRAINT history_type_config_twin_class_i18n_id_fk FOREIGN KEY (message_template_i18n_id) REFERENCES public.i18n(id);


--
-- Name: history_type_config_twin_class history_type_config_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class
    ADD CONSTRAINT history_type_config_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id);


--
-- Name: history_type_config_twin_class history_type_config_twin_class_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_config_twin_class
    ADD CONSTRAINT history_type_config_twin_class_status_id_fk FOREIGN KEY (history_type_status_id) REFERENCES public.history_type_status(id) ON UPDATE CASCADE;


--
-- Name: history_type_domain_template history_type_domain_template_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_domain_template
    ADD CONSTRAINT history_type_domain_template_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id);


--
-- Name: history_type_domain_template history_type_domain_template_history_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_domain_template
    ADD CONSTRAINT history_type_domain_template_history_type_id_fk FOREIGN KEY (history_type_id) REFERENCES public.history_type(id);


--
-- Name: history_type_domain_template history_type_domain_template_history_type_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type_domain_template
    ADD CONSTRAINT history_type_domain_template_history_type_status_id_fk FOREIGN KEY (history_type_status_id) REFERENCES public.history_type_status(id) ON UPDATE CASCADE;


--
-- Name: history_type history_type_history_type_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history_type
    ADD CONSTRAINT history_type_history_type_status_id_fk FOREIGN KEY (history_type_status_id) REFERENCES public.history_type_status(id) ON UPDATE CASCADE;


--
-- Name: history history_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_user_id_fk FOREIGN KEY (actor_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: i18n i18n_i18n_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n
    ADD CONSTRAINT i18n_i18n_type_id_fk FOREIGN KEY (i18n_type_id) REFERENCES public.i18n_type(id) ON UPDATE CASCADE;


--
-- Name: i18n_translation_bin i18n_translation_bin_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_translation_bin
    ADD CONSTRAINT i18n_translation_bin_i18n_id_fk FOREIGN KEY (i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: i18n_translation i18n_translation_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_translation
    ADD CONSTRAINT i18n_translation_i18n_id_fk FOREIGN KEY (i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: i18n_translation_style i18n_translations_styles_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.i18n_translation_style
    ADD CONSTRAINT i18n_translations_styles_i18n_id_fk FOREIGN KEY (i18n_id) REFERENCES public.i18n(id);


--
-- Name: link link_backward_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_backward_name_i18n_id_fk FOREIGN KEY (backward_name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: link link_backward_twin_class__fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_backward_twin_class__fk FOREIGN KEY (dst_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: link link_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: link link_forward_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_forward_name_i18n_id_fk FOREIGN KEY (forward_name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: link link_forward_twin_class__fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_forward_twin_class__fk FOREIGN KEY (src_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: link link_link_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_link_type_id_fk FOREIGN KEY (link_type_id) REFERENCES public.link_type(id) ON UPDATE CASCADE;


--
-- Name: link link_strength_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link
    ADD CONSTRAINT link_strength_id_fk FOREIGN KEY (link_strength_id) REFERENCES public.link_strength(id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: link_tree link_tree_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree
    ADD CONSTRAINT link_tree_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: link_tree_node link_tree_node_link_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree_node
    ADD CONSTRAINT link_tree_node_link_id_fk FOREIGN KEY (link_id) REFERENCES public.link(id) ON UPDATE CASCADE;


--
-- Name: link_tree_node link_tree_node_link_tree_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree_node
    ADD CONSTRAINT link_tree_node_link_tree_id_fk FOREIGN KEY (link_tree_id) REFERENCES public.link_tree(id) ON UPDATE CASCADE;


--
-- Name: link_tree link_tree_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree
    ADD CONSTRAINT link_tree_twin_class_id_fk FOREIGN KEY (root_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: link_tree link_tree_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_tree
    ADD CONSTRAINT link_tree_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: link_trigger link_trigger_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_trigger
    ADD CONSTRAINT link_trigger_featurer_id_fk FOREIGN KEY (link_trigger_featurer_id) REFERENCES public.featurer(id);


--
-- Name: link_trigger link_trigger_link_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_trigger
    ADD CONSTRAINT link_trigger_link_id_fk FOREIGN KEY (link_id) REFERENCES public.link(id) ON UPDATE CASCADE;


--
-- Name: link_validator link_validator_featurer_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_validator
    ADD CONSTRAINT link_validator_featurer_id FOREIGN KEY (link_validator_featurer_id) REFERENCES public.featurer(id);


--
-- Name: link_validator link_validator_link_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.link_validator
    ADD CONSTRAINT link_validator_link_id_fk FOREIGN KEY (link_id) REFERENCES public.link(id) ON UPDATE CASCADE;


--
-- Name: permission_group permission_group_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_group
    ADD CONSTRAINT permission_group_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: permission_group permission_group_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_group
    ADD CONSTRAINT permission_group_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: permission permission_permission_group_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission
    ADD CONSTRAINT permission_permission_group_id_fk FOREIGN KEY (permission_group_id) REFERENCES public.permission_group(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_assignee_propagation permission_schema_assignee_propag_granted_by_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_granted_by_user_id_fkey FOREIGN KEY (granted_by_user_id) REFERENCES public."user"(id);


--
-- Name: permission_schema_assignee_propagation permission_schema_assignee_propag_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permission(id);


--
-- Name: permission_schema_assignee_propagation permission_schema_assignee_propag_permission_schema_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_permission_schema_id_fkey FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id);


--
-- Name: permission_schema_assignee_propagation permission_schema_assignee_propag_twin_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_twin_class_id_fkey FOREIGN KEY (propagation_by_twin_class_id) REFERENCES public.twin_class(id);


--
-- Name: permission_schema_assignee_propagation permission_schema_assignee_propag_twin_status_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_assignee_propagation
    ADD CONSTRAINT permission_schema_assignee_propag_twin_status_id_fkey FOREIGN KEY (propagation_by_twin_status_id) REFERENCES public.twin_status(id);


--
-- Name: permission_schema permission_schema_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema
    ADD CONSTRAINT permission_schema_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE;


--
-- Name: permission_schema permission_schema_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema
    ADD CONSTRAINT permission_schema_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_space_roles permission_schema_space_roles_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_space_roles
    ADD CONSTRAINT permission_schema_space_roles_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_space_roles permission_schema_space_roles_permission_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_space_roles
    ADD CONSTRAINT permission_schema_space_roles_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_space_roles permission_schema_space_roles_space_role_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_space_roles
    ADD CONSTRAINT permission_schema_space_roles_space_role_id_fk FOREIGN KEY (space_role_id) REFERENCES public.space_role(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_space_roles permission_schema_space_roles_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_space_roles
    ADD CONSTRAINT permission_schema_space_roles_user_id_fk FOREIGN KEY (granted_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_twin_role permission_schema_twin_role_granted_by_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_twin_role
    ADD CONSTRAINT permission_schema_twin_role_granted_by_user_id_fkey FOREIGN KEY (granted_by_user_id) REFERENCES public."user"(id);


--
-- Name: permission_schema_twin_role permission_schema_twin_role_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_twin_role
    ADD CONSTRAINT permission_schema_twin_role_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permission(id);


--
-- Name: permission_schema_twin_role permission_schema_twin_role_permission_schema_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_twin_role
    ADD CONSTRAINT permission_schema_twin_role_permission_schema_id_fkey FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id);


--
-- Name: permission_schema_twin_role permission_schema_twin_role_twin_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_twin_role
    ADD CONSTRAINT permission_schema_twin_role_twin_class_id_fkey FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id);


--
-- Name: permission_schema_twin_role permission_schema_twin_role_twin_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_twin_role
    ADD CONSTRAINT permission_schema_twin_role_twin_role_id_fkey FOREIGN KEY (twin_role_id) REFERENCES public.twin_role(id);


--
-- Name: permission_schema_user permission_schema_user_granted_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user
    ADD CONSTRAINT permission_schema_user_granted_user_id_fk FOREIGN KEY (granted_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user_group permission_schema_user_group_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user_group
    ADD CONSTRAINT permission_schema_user_group_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user_group permission_schema_user_group_permission_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user_group
    ADD CONSTRAINT permission_schema_user_group_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user_group permission_schema_user_group_user_group_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user_group
    ADD CONSTRAINT permission_schema_user_group_user_group_id_fk FOREIGN KEY (user_group_id) REFERENCES public.user_group(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user_group permission_schema_user_group_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user_group
    ADD CONSTRAINT permission_schema_user_group_user_id_fk FOREIGN KEY (granted_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: permission_schema permission_schema_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema
    ADD CONSTRAINT permission_schema_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user permission_schema_user_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user
    ADD CONSTRAINT permission_schema_user_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user permission_schema_user_permission_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user
    ADD CONSTRAINT permission_schema_user_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id) ON UPDATE CASCADE;


--
-- Name: permission_schema_user permission_schema_user_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permission_schema_user
    ADD CONSTRAINT permission_schema_user_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: search_alias search_alias_detector_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_alias
    ADD CONSTRAINT search_alias_detector_featurer_id_fk FOREIGN KEY (search_detector_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: search_alias search_alias_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_alias
    ADD CONSTRAINT search_alias_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: search search_head_search_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search
    ADD CONSTRAINT search_head_search_id_fk FOREIGN KEY (head_twin_search_id) REFERENCES public.search(id);


--
-- Name: search search_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search
    ADD CONSTRAINT search_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: search_predicate search_predicate_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_predicate
    ADD CONSTRAINT search_predicate_featurer_id_fk FOREIGN KEY (search_criteria_builder_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: search_predicate search_predicate_search_field_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_predicate
    ADD CONSTRAINT search_predicate_search_field_id_fk FOREIGN KEY (search_field_id) REFERENCES public.search_field(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: search_predicate search_predicate_search_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_predicate
    ADD CONSTRAINT search_predicate_search_id_fk FOREIGN KEY (search_id) REFERENCES public.search(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: search search_search_alias_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search
    ADD CONSTRAINT search_search_alias_id_fk FOREIGN KEY (search_alias_id) REFERENCES public.search_alias(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: space space_permission_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space
    ADD CONSTRAINT space_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id) ON UPDATE CASCADE;


--
-- Name: space_role space_role_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role
    ADD CONSTRAINT space_role_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE;


--
-- Name: space_role space_role_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role
    ADD CONSTRAINT space_role_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: space_role space_role_i18n_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role
    ADD CONSTRAINT space_role_i18n_id_fk_2 FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: space_role space_role_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role
    ADD CONSTRAINT space_role_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: space_role_user_group space_role_user_group_space_role_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user_group
    ADD CONSTRAINT space_role_user_group_space_role_id_fk FOREIGN KEY (space_role_id) REFERENCES public.space_role(id) ON UPDATE CASCADE;


--
-- Name: space_role_user_group space_role_user_group_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user_group
    ADD CONSTRAINT space_role_user_group_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: space_role_user_group space_role_user_group_user_group_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user_group
    ADD CONSTRAINT space_role_user_group_user_group_id_fk FOREIGN KEY (user_group_id) REFERENCES public.user_group(id) ON UPDATE CASCADE;


--
-- Name: space_role_user_group space_role_user_group_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user_group
    ADD CONSTRAINT space_role_user_group_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: space_role_user space_role_user_space_role_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user
    ADD CONSTRAINT space_role_user_space_role_id_fk FOREIGN KEY (space_role_id) REFERENCES public.space_role(id) ON UPDATE CASCADE;


--
-- Name: space_role_user space_role_user_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user
    ADD CONSTRAINT space_role_user_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: space_role_user space_role_user_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user
    ADD CONSTRAINT space_role_user_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: space_role_user space_role_user_user_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space_role_user
    ADD CONSTRAINT space_role_user_user_id_fk_2 FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: space space_twin_class_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space
    ADD CONSTRAINT space_twin_class_schema_id_fk FOREIGN KEY (twin_class_schema_id) REFERENCES public.twin_class_schema(id) ON UPDATE CASCADE;


--
-- Name: space space_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space
    ADD CONSTRAINT space_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: space space_twinflow_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.space
    ADD CONSTRAINT space_twinflow_schema_id_fk FOREIGN KEY (twinflow_schema_id) REFERENCES public.twinflow_schema(id) ON UPDATE CASCADE;


--
-- Name: twin_action_permission twin_action_permission_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_permission
    ADD CONSTRAINT twin_action_permission_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: twin_action_permission twin_action_permission_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_permission
    ADD CONSTRAINT twin_action_permission_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin_action_validator twin_action_validator_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_validator
    ADD CONSTRAINT twin_action_validator_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin_alias twin_alias_business_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias
    ADD CONSTRAINT twin_alias_business_account_id_fkey FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON DELETE CASCADE;


--
-- Name: twin_alias twin_alias_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias
    ADD CONSTRAINT twin_alias_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON DELETE CASCADE;


--
-- Name: twin_alias twin_alias_twin_alias_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias
    ADD CONSTRAINT twin_alias_twin_alias_type_id_fkey FOREIGN KEY (twin_alias_type_id) REFERENCES public.twin_alias_type(alias_type_id);


--
-- Name: twin_alias twin_alias_twin_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias
    ADD CONSTRAINT twin_alias_twin_id_fkey FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON DELETE CASCADE;


--
-- Name: twin_alias twin_alias_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_alias
    ADD CONSTRAINT twin_alias_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: twin twin_assigner_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_assigner_user_id_fk FOREIGN KEY (assigner_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin_attachment twin_attachment_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT twin_attachment_permission_id_fk FOREIGN KEY (view_permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: twin_attachment twin_attachment_twin_comment_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT twin_attachment_twin_comment_id_fk FOREIGN KEY (twin_comment_id) REFERENCES public.twin_comment(id) ON DELETE CASCADE;


--
-- Name: twin_attachment twin_attachment_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT twin_attachment_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_attachment twin_attachment_twinflow_transition_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT twin_attachment_twinflow_transition_id_fk FOREIGN KEY (twinflow_transition_id) REFERENCES public.twinflow_transition(id) ON UPDATE CASCADE;


--
-- Name: twin_attachment twin_attachment_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_attachment
    ADD CONSTRAINT twin_attachment_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin_business_account_alias_counter twin_business_account_alias_counter_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_business_account_alias_counter
    ADD CONSTRAINT twin_business_account_alias_counter_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_business_account_alias_counter twin_business_account_alias_counter_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_business_account_alias_counter
    ADD CONSTRAINT twin_business_account_alias_counter_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_action_permission twin_class_action_permission_twin_action_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_permission
    ADD CONSTRAINT twin_class_action_permission_twin_action_id_fk FOREIGN KEY (twin_action_id) REFERENCES public.twin_action(id) ON UPDATE CASCADE;


--
-- Name: twin_action_validator twin_class_action_validator_featurer_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_validator
    ADD CONSTRAINT twin_class_action_validator_featurer_id_fk_2 FOREIGN KEY (twin_validator_featurer_id) REFERENCES public.featurer(id);


--
-- Name: twin_action_validator twin_class_action_validator_twin_action_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_action_validator
    ADD CONSTRAINT twin_class_action_validator_twin_action_id_fk FOREIGN KEY (twin_action_id) REFERENCES public.twin_action(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_description_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_extends_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_extends_twin_class_id_fk FOREIGN KEY (extends_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_featurer_id_fk FOREIGN KEY (head_hunter_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: twin_class_field twin_class_field_description_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twin_class_field twin_class_field_edit_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_edit_permission_id_fk FOREIGN KEY (edit_permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: twin_class_field twin_class_field_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_featurer_id_fk FOREIGN KEY (field_typer_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: twin_class_field twin_class_field_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twin_class_field twin_class_field_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_class_field twin_class_field_view_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_field
    ADD CONSTRAINT twin_class_field_view_permission_id_fk FOREIGN KEY (view_permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_head_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_head_twin_class_id_fk FOREIGN KEY (head_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_marker_data_list_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_marker_data_list_id_fk FOREIGN KEY (marker_data_list_id) REFERENCES public.data_list(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twin_class_schema twin_class_schema_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema
    ADD CONSTRAINT twin_class_schema_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: twin_class_schema_map twin_class_schema_map_create_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema_map
    ADD CONSTRAINT twin_class_schema_map_create_permission_id_fk FOREIGN KEY (create_permission_id) REFERENCES public.permission(id);


--
-- Name: twin_class_schema_map twin_class_schema_map_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema_map
    ADD CONSTRAINT twin_class_schema_map_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id);


--
-- Name: twin_class_schema_map twin_class_schema_map_twin_class_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema_map
    ADD CONSTRAINT twin_class_schema_map_twin_class_schema_id_fk FOREIGN KEY (twin_class_schema_id) REFERENCES public.twin_class_schema(id);


--
-- Name: twin_class_schema twin_class_schema_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class_schema
    ADD CONSTRAINT twin_class_schema_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_tag_data_list_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_tag_data_list_id_fk FOREIGN KEY (tag_data_list_id) REFERENCES public.data_list(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_twin_class_owner_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_twin_class_owner_id_fk FOREIGN KEY (twin_class_owner_type_id) REFERENCES public.twin_class_owner_type(id) ON UPDATE CASCADE;


--
-- Name: twin_class twin_class_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_class
    ADD CONSTRAINT twin_class_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin_comment_action_alien_permission twin_comment_action_alien_permission_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_permission
    ADD CONSTRAINT twin_comment_action_alien_permission_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: twin_comment_action_alien_permission twin_comment_action_alien_permission_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_permission
    ADD CONSTRAINT twin_comment_action_alien_permission_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin_comment_action_alien_permission twin_comment_action_alien_permission_twin_comment_action_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_permission
    ADD CONSTRAINT twin_comment_action_alien_permission_twin_comment_action_id_fk FOREIGN KEY (twin_comment_action_id) REFERENCES public.twin_comment_action(id) ON UPDATE CASCADE;


--
-- Name: twin_comment_action_alien_validator twin_comment_action_alien_validator_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_validator
    ADD CONSTRAINT twin_comment_action_alien_validator_featurer_id_fk FOREIGN KEY (twin_validator_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: twin_comment_action_alien_validator twin_comment_action_alien_validator_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_alien_validator
    ADD CONSTRAINT twin_comment_action_alien_validator_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin_comment_action_self twin_comment_action_self_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_self
    ADD CONSTRAINT twin_comment_action_self_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id);


--
-- Name: twin_comment_action_self twin_comment_action_self_twin_comment_action_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment_action_self
    ADD CONSTRAINT twin_comment_action_self_twin_comment_action_id_fk FOREIGN KEY (restrict_twin_comment_action_id) REFERENCES public.twin_comment_action(id);


--
-- Name: twin_comment twin_comment_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment
    ADD CONSTRAINT twin_comment_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_comment twin_comment_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_comment
    ADD CONSTRAINT twin_comment_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin twin_created_by_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_created_by_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin_factory_condition twin_factory_condition_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_condition
    ADD CONSTRAINT twin_factory_condition_featurer_id_fk FOREIGN KEY (conditioner_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: twin_factory_condition twin_factory_condition_twin_factory_condition_set_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_condition
    ADD CONSTRAINT twin_factory_condition_twin_factory_condition_set_id_fk FOREIGN KEY (twin_factory_condition_set_id) REFERENCES public.twin_factory_condition_set(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory twin_factory_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory
    ADD CONSTRAINT twin_factory_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory twin_factory_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory
    ADD CONSTRAINT twin_factory_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory twin_factory_i18n_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory
    ADD CONSTRAINT twin_factory_i18n_id_fk_2 FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_multiplier twin_factory_multiplier_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier
    ADD CONSTRAINT twin_factory_multiplier_featurer_id_fk FOREIGN KEY (multiplier_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_multiplier_filter twin_factory_multiplier_filter_twin_factory_condition_set_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_twin_factory_condition_set_id_fk FOREIGN KEY (twin_factory_condition_set_id) REFERENCES public.twin_factory_condition_set(id);


--
-- Name: twin_factory_multiplier_filter twin_factory_multiplier_filter_twin_factory_multiplier_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_twin_factory_multiplier_id_fk FOREIGN KEY (twin_factory_multiplier_id) REFERENCES public.twin_factory_multiplier(id);


--
-- Name: twin_factory_multiplier twin_factory_multiplier_input_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier
    ADD CONSTRAINT twin_factory_multiplier_input_twin_class_id_fk FOREIGN KEY (input_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_multiplier twin_factory_multiplier_twin_factory_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_multiplier
    ADD CONSTRAINT twin_factory_multiplier_twin_factory_id_fk FOREIGN KEY (twin_factory_id) REFERENCES public.twin_factory(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_pipeline twin_factory_pipeline_input_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_input_twin_class_id_fk FOREIGN KEY (input_twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_pipeline twin_factory_pipeline_output_twin_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_output_twin_status_id_fk FOREIGN KEY (output_twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: twin_factory_pipeline_step twin_factory_pipeline_step_filler_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline_step
    ADD CONSTRAINT twin_factory_pipeline_step_filler_featurer_id_fk FOREIGN KEY (filler_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_pipeline_step twin_factory_pipeline_step_twin_factory_condition_set_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline_step
    ADD CONSTRAINT twin_factory_pipeline_step_twin_factory_condition_set_id_fk FOREIGN KEY (twin_factory_condition_set_id) REFERENCES public.twin_factory_condition_set(id) ON UPDATE CASCADE;


--
-- Name: twin_factory_pipeline_step twin_factory_pipeline_step_twin_factory_pipeline_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline_step
    ADD CONSTRAINT twin_factory_pipeline_step_twin_factory_pipeline_id_fk FOREIGN KEY (twin_factory_pipeline_id) REFERENCES public.twin_factory_pipeline(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_pipeline twin_factory_pipeline_twin_factory_condition_set_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_twin_factory_condition_set_id_fk FOREIGN KEY (twin_factory_condition_set_id) REFERENCES public.twin_factory_condition_set(id) ON UPDATE CASCADE;


--
-- Name: twin_factory_pipeline twin_factory_pipeline_twin_factory_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_twin_factory_id_fk FOREIGN KEY (twin_factory_id) REFERENCES public.twin_factory(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_pipeline twin_factory_pipeline_twin_factory_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_twin_factory_id_fk_2 FOREIGN KEY (next_twin_factory_id) REFERENCES public.twin_factory(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_factory_pipeline twin_factory_pipeline_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_factory_pipeline
    ADD CONSTRAINT twin_factory_pipeline_twin_id_fk FOREIGN KEY (template_twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE;


--
-- Name: twin_field_data_list twin_field_data_list_data_list_option_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_data_list
    ADD CONSTRAINT twin_field_data_list_data_list_option_id_fk FOREIGN KEY (data_list_option_id) REFERENCES public.data_list_option(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_data_list twin_field_data_list_twin_class_field_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_data_list
    ADD CONSTRAINT twin_field_data_list_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES public.twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_data_list twin_field_data_list_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_data_list
    ADD CONSTRAINT twin_field_data_list_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_simple twin_field_simple_twin_class_field_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_simple
    ADD CONSTRAINT twin_field_simple_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES public.twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_simple twin_field_simple_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_simple
    ADD CONSTRAINT twin_field_simple_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_user twin_field_user_twin_class_field_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_user
    ADD CONSTRAINT twin_field_user_twin_class_field_id_fk FOREIGN KEY (twin_class_field_id) REFERENCES public.twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_user twin_field_user_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_user
    ADD CONSTRAINT twin_field_user_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_field_user twin_field_user_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_field_user
    ADD CONSTRAINT twin_field_user_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin twin_head_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_head_twin_id_fk FOREIGN KEY (head_twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_link twin_link_dst_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_link
    ADD CONSTRAINT twin_link_dst_twin_id_fk FOREIGN KEY (dst_twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_link twin_link_link_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_link
    ADD CONSTRAINT twin_link_link_id_fk FOREIGN KEY (link_id) REFERENCES public.link(id) ON UPDATE CASCADE;


--
-- Name: twin_link twin_link_src_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_link
    ADD CONSTRAINT twin_link_src_twin_id_fk FOREIGN KEY (src_twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_link twin_link_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_link
    ADD CONSTRAINT twin_link_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twin_marker twin_marker_market_data_list_option_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_marker
    ADD CONSTRAINT twin_marker_market_data_list_option_id_fk FOREIGN KEY (marker_data_list_option_id) REFERENCES public.data_list_option(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_marker twin_marker_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_marker
    ADD CONSTRAINT twin_marker_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin twin_owner_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_owner_business_account_id_fk FOREIGN KEY (owner_business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_status twin_status_description_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status
    ADD CONSTRAINT twin_status_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twin_status_group twin_status_group_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_group
    ADD CONSTRAINT twin_status_group_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: twin_status_group_map twin_status_group_map_twin_status_group_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_group_map
    ADD CONSTRAINT twin_status_group_map_twin_status_group_id_fk FOREIGN KEY (twin_status_group_id) REFERENCES public.twin_status_group(id) ON UPDATE CASCADE;


--
-- Name: twin_status_group_map twin_status_group_map_twin_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_group_map
    ADD CONSTRAINT twin_status_group_map_twin_status_id_fk FOREIGN KEY (twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE;


--
-- Name: twin_status twin_status_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status
    ADD CONSTRAINT twin_status_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twin_status_transition_trigger twin_status_transition_trigger_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_transition_trigger
    ADD CONSTRAINT twin_status_transition_trigger_featurer_id_fk FOREIGN KEY (transition_trigger_featurer_id) REFERENCES public.featurer(id);


--
-- Name: twin_status_transition_trigger twin_status_transition_trigger_twin_status_transition_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_transition_trigger
    ADD CONSTRAINT twin_status_transition_trigger_twin_status_transition_id_fk FOREIGN KEY (twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE;


--
-- Name: twin_status_transition_trigger twin_status_transition_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status_transition_trigger
    ADD CONSTRAINT twin_status_transition_type_id_fk FOREIGN KEY (twin_status_transition_type_id) REFERENCES public.twin_status_transition_type(id) ON UPDATE CASCADE;


--
-- Name: twin_status twin_status_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_status
    ADD CONSTRAINT twin_status_twin_class_id_fk FOREIGN KEY (twins_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_tag twin_tag_tag_data_list_option_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_tag
    ADD CONSTRAINT twin_tag_tag_data_list_option_id_fk FOREIGN KEY (tag_data_list_option_id) REFERENCES public.data_list_option(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_tag twin_tag_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_tag
    ADD CONSTRAINT twin_tag_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_touch twin_touch_touch_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_touch
    ADD CONSTRAINT twin_touch_touch_id_fk FOREIGN KEY (touch_id) REFERENCES public.touch(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_touch twin_touch_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_touch
    ADD CONSTRAINT twin_touch_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_touch twin_touch_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_touch
    ADD CONSTRAINT twin_touch_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- Name: twin twin_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twin twin_twin_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_twin_status_id_fk FOREIGN KEY (twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE;


--
-- Name: twin twin_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin
    ADD CONSTRAINT twin_user_id_fk FOREIGN KEY (owner_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twin_work twin_work_twin_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_work
    ADD CONSTRAINT twin_work_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id) ON UPDATE CASCADE;


--
-- Name: twin_work twin_work_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twin_work
    ADD CONSTRAINT twin_work_user_id_fk FOREIGN KEY (author_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twinflow twinflow_description_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow
    ADD CONSTRAINT twinflow_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twinflow twinflow_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow
    ADD CONSTRAINT twinflow_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twinflow_schema twinflow_schema_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema
    ADD CONSTRAINT twinflow_schema_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE;


--
-- Name: twinflow_schema twinflow_schema_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema
    ADD CONSTRAINT twinflow_schema_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: twinflow_schema_map twinflow_schema_map_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema_map
    ADD CONSTRAINT twinflow_schema_map_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twinflow_schema_map twinflow_schema_map_twinflow_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema_map
    ADD CONSTRAINT twinflow_schema_map_twinflow_id_fk FOREIGN KEY (twinflow_id) REFERENCES public.twinflow(id) ON UPDATE CASCADE;


--
-- Name: twinflow_schema_map twinflow_schema_map_twinflow_schema_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema_map
    ADD CONSTRAINT twinflow_schema_map_twinflow_schema_id_fk FOREIGN KEY (twinflow_schema_id) REFERENCES public.twinflow_schema(id) ON UPDATE CASCADE;


--
-- Name: twinflow_schema twinflow_schema_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_schema
    ADD CONSTRAINT twinflow_schema_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition_alias twinflow_transition_alias_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_alias
    ADD CONSTRAINT twinflow_transition_alias_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_created_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_created_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_description_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_dst_twin_status__fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_dst_twin_status__fk FOREIGN KEY (dst_twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_name_i18n_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_permission_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_permission_id_fk FOREIGN KEY (permission_id) REFERENCES public.permission(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_src_twin_status__fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_src_twin_status__fk FOREIGN KEY (src_twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition_trigger twinflow_transition_trigger_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_trigger
    ADD CONSTRAINT twinflow_transition_trigger_featurer_id_fk FOREIGN KEY (transition_trigger_featurer_id) REFERENCES public.featurer(id);


--
-- Name: twinflow_transition_trigger twinflow_transition_trigger_twinflow_transition_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_trigger
    ADD CONSTRAINT twinflow_transition_trigger_twinflow_transition_id_fk FOREIGN KEY (twinflow_transition_id) REFERENCES public.twinflow_transition(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_twin_factory_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_twin_factory_id_fk FOREIGN KEY (inbuilt_twin_factory_id) REFERENCES public.twin_factory(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_twin_factory_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_twin_factory_id_fk_2 FOREIGN KEY (drafting_twin_factory_id) REFERENCES public.twin_factory(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_twinflow_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_twinflow_id_fk FOREIGN KEY (twinflow_id) REFERENCES public.twinflow(id) ON UPDATE CASCADE;


--
-- Name: twinflow_transition twinflow_transition_twinflow_transition_alias_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition
    ADD CONSTRAINT twinflow_transition_twinflow_transition_alias_id_fk FOREIGN KEY (twinflow_transition_alias_id) REFERENCES public.twinflow_transition_alias(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: twinflow_transition_validator twinflow_transition_validator_featurer_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_validator
    ADD CONSTRAINT twinflow_transition_validator_featurer_id_fk_2 FOREIGN KEY (twin_validator_featurer_id) REFERENCES public.featurer(id);


--
-- Name: twinflow_transition_validator twinflow_transition_validator_twinflow_transition_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow_transition_validator
    ADD CONSTRAINT twinflow_transition_validator_twinflow_transition_id_fk FOREIGN KEY (twinflow_transition_id) REFERENCES public.twinflow_transition(id) ON UPDATE CASCADE;


--
-- Name: twinflow twinflow_twin_class_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow
    ADD CONSTRAINT twinflow_twin_class_id_fk FOREIGN KEY (twin_class_id) REFERENCES public.twin_class(id) ON UPDATE CASCADE;


--
-- Name: twinflow twinflow_twin_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow
    ADD CONSTRAINT twinflow_twin_status_id_fk FOREIGN KEY (initial_twin_status_id) REFERENCES public.twin_status(id) ON UPDATE CASCADE;


--
-- Name: twinflow twinflow_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twinflow
    ADD CONSTRAINT twinflow_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: user_group user_group_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group
    ADD CONSTRAINT user_group_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE;


--
-- Name: user_group user_group_domain_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group
    ADD CONSTRAINT user_group_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id) ON UPDATE CASCADE;


--
-- Name: user_group_map user_group_map_added_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_map
    ADD CONSTRAINT user_group_map_added_user_id_fk FOREIGN KEY (added_by_user_id) REFERENCES public."user"(id) ON UPDATE CASCADE;


--
-- Name: user_group_map user_group_map_business_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_map
    ADD CONSTRAINT user_group_map_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: user_group_map user_group_map_user_group_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_map
    ADD CONSTRAINT user_group_map_user_group_id_fk FOREIGN KEY (user_group_id) REFERENCES public.user_group(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: user_group_map user_group_map_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_map
    ADD CONSTRAINT user_group_map_user_id_fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: user_group_type user_group_type_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group_type
    ADD CONSTRAINT user_group_type_featurer_id_fk FOREIGN KEY (slugger_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: user_group user_group_user_group_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_group
    ADD CONSTRAINT user_group_user_group_type_id_fk FOREIGN KEY (user_group_type_id) REFERENCES public.user_group_type(id) ON UPDATE CASCADE;


--
-- Name: user user_user_status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_user_status_id_fk FOREIGN KEY (user_status_id) REFERENCES public.user_status(id);


--
-- Name: widget widget_featurer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT widget_featurer_id_fk FOREIGN KEY (widget_data_grabber_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- Name: widget widget_featurer_id_fk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.widget
    ADD CONSTRAINT widget_featurer_id_fk_2 FOREIGN KEY (widget_accessor_featurer_id) REFERENCES public.featurer(id) ON UPDATE CASCADE;


--
-- PostgreSQL database dump complete
--

