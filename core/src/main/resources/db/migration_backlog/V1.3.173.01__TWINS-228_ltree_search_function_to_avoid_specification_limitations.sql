-- This function, ltree_of_uuids_get_parents, extracts parent UUIDs from an ltree path by iterating up to a specified depth.
-- It generates levels based on the given depth, filters out invalid positions, and retrieves distinct parent UUIDs by extracting subpaths
-- at the computed positions. The function ensures safe execution, supports parallelism, and returns NULL for NULL input values.
create or replace function ltree_of_uuids_get_parents(ltree_root_val ltree, depth_val integer) returns setof uuid
    language sql
    immutable parallel safe
    returns null on null input
as
$$
with generated_levels as (select nlevel(ltree_root_val) - lvl as position
                          FROM generate_series(2, depth_val + 1) AS lvl),
     filter_negative as (select g.position from generated_levels g where g.position >= 0)
SELECT DISTINCT replace(ltree2text(subpath(ltree_root_val, f.position, 1)), '_',
                        '-')::uuid AS parent_id
FROM filter_negative AS f;
$$;

-- create or replace function ltree_get_parent_uuid_array(ltree_root_val ltree,depth_val integer) returns  uuid[]
--     language sql
--     immutable parallel safe
--     returns null on null input
-- as
-- $$
-- SELECT array_agg(f) from ltree_get_parent_uuid(ltree_root_val,depth_val) f
-- $$;


create or replace function twin_class_head_hierarchy_get_parent_ids(ids_val uuid[], depth_val integer) returns setof uuid
    language sql
    immutable parallel safe
    returns null on null input
as
$$
SELECT ltree_of_uuids_get_parents(c.head_hierarchy_tree, depth_val)
from twin_class c
where c.id = any (ids_val::uuid[])
$$;

create or replace function twin_class_extends_hierarchy_get_parent_ids(ids_val uuid[], depth_val integer) returns setof uuid
    language sql
    immutable parallel safe
    returns null on null input
as
$$
SELECT ltree_of_uuids_get_parents(c.extends_hierarchy_tree, depth_val)
from twin_class c
where c.id = any (ids_val::uuid[])
$$;



--hierarchy_check_lquery

--TODO uncomment if we need indexes
-- btree can be used with <, <=, =, >=, > only, so we need GIST for @>, <@, @, ~, ?

-- CREATE INDEX if not exists extends_hierarchy_tree_gist_idx ON twin_class USING GIST (extends_hierarchy_tree);
-- CREATE INDEX if not exists head_hierarchy_tree_gist_idx ON twin_class USING GIST (head_hierarchy_tree);