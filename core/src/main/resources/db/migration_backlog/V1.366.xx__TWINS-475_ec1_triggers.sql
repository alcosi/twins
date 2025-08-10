create or replace function twin_class_update_inherited_bread_crumbs_face_id()
    returns trigger as
$$
declare
    value         uuid;
    current_level int;
    depth     int;
    updated_count int;
    blocked_nodes ltree[];
    prev_blocked_nodes ltree[];
begin
    -- filling in the variable that we will set for the children of this class
    if new.bread_crumbs_face_id is not null then
        value := new.bread_crumbs_face_id;
    else
        value := new.inherited_bread_crumbs_face_id;
    end if;

    -- filling the current level of class and depth vars
    current_level := nlevel(new.extends_hierarchy_tree);

    select max(nlevel(extends_hierarchy_tree))
    into depth
    from twin_class
    where extends_hierarchy_tree <@ new.extends_hierarchy_tree;

    -- exit if max_level is null because it means that this element don't have any children
    if depth is null then
        return new;
    end if;

    -- initializing value to avoid it becoming null after array_cat
    blocked_nodes := array[]::ltree[];

    -- cycle through the levels of the tree (like bfs)
    for level_to_update in (current_level + 1)..depth loop
        -- updating classes on this level including ones that has bread_crumbs_face_id is not null
        -- and excluding ones that are children of classes with bread_crumbs_face_id is not null
            update twin_class
            set inherited_bread_crumbs_face_id = value
            where extends_hierarchy_tree <@ new.extends_hierarchy_tree
              and nlevel(extends_hierarchy_tree) = level_to_update
              and not exists (
                select 1
                from unnest(blocked_nodes) as blocked_node
                where extends_hierarchy_tree <@ blocked_node
            );

            -- getting the number of updated classes on this level
            get diagnostics updated_count = row_count;

            -- saving blocked_nodes from prev level and adding blocked_nodes from this level
            prev_blocked_nodes := blocked_nodes;
            select array_cat(blocked_nodes, array_agg(extends_hierarchy_tree))
            into blocked_nodes
            from twin_class
            where extends_hierarchy_tree <@ new.extends_hierarchy_tree
              and nlevel(extends_hierarchy_tree) = level_to_update
              and bread_crumbs_face_id is not null;

            -- exit from cycle if we updated nothing on this level
            -- and blocked_nodes from prev and this levels are equal
            if updated_count = 0 and prev_blocked_nodes = blocked_nodes then
                exit;
            end if;
        end loop;

    return new;
end;
$$ language plpgsql;

create or replace function twin_class_update_inherited_page_face_id()
    returns trigger as
$$
declare
    value         uuid;
    current_level int;
    depth     int;
    updated_count int;
    blocked_nodes ltree[];
    prev_blocked_nodes ltree[];
begin
    -- filling in the variable that we will set for the children of this class
    if new.page_face_id is not null then
        value := new.page_face_id;
    else
        value := new.inherited_page_face_id;
    end if;

    -- filling the current level of class and depth vars
    current_level := nlevel(new.extends_hierarchy_tree);

    select max(nlevel(extends_hierarchy_tree))
    into depth
    from twin_class
    where extends_hierarchy_tree <@ new.extends_hierarchy_tree;

    -- exit if max_level is null because it means that this element don't have any children
    if depth is null then
        return new;
    end if;

    -- initializing value to avoid it becoming null after array_cat
    blocked_nodes := array[]::ltree[];

    -- cycle through the levels of the tree (like bfs)
    for level_to_update in (current_level + 1)..depth loop
        -- updating classes on this level including ones that has page_face_id is not null
        -- and excluding ones that are children of classes with page_face_id is not null
            update twin_class
            set inherited_page_face_id = value
            where extends_hierarchy_tree <@ new.extends_hierarchy_tree
              and nlevel(extends_hierarchy_tree) = level_to_update
              and not exists (
                select 1
                from unnest(blocked_nodes) as blocked_node
                where extends_hierarchy_tree <@ blocked_node
            );

            -- getting the number of updated classes on this level
            get diagnostics updated_count = row_count;

            -- saving blocked_nodes from prev level and adding blocked_nodes from this level
            prev_blocked_nodes := blocked_nodes;
            select array_cat(blocked_nodes, array_agg(extends_hierarchy_tree))
            into blocked_nodes
            from twin_class
            where extends_hierarchy_tree <@ new.extends_hierarchy_tree
              and nlevel(extends_hierarchy_tree) = level_to_update
              and page_face_id is not null;

            -- exit from cycle if we updated nothing on this level
            -- and blocked_nodes from prev and this levels are equal
            if updated_count = 0 and prev_blocked_nodes = blocked_nodes then
                exit;
            end if;
        end loop;

    return new;
end;
$$ language plpgsql;

create or replace trigger twin_class_update_inherited_bread_crumbs_face_id_trigger
    after update of bread_crumbs_face_id
    on twin_class
    for each row
execute function twin_class_update_inherited_bread_crumbs_face_id();

create or replace trigger twin_class_update_inherited_page_face_id_trigger
    after update of page_face_id
    on twin_class
    for each row
execute function twin_class_update_inherited_page_face_id();


create or replace function twin_class_set_inherited_face_on_insert()
    returns trigger as
$$
declare
    parent_bread_crumbs_face_id uuid;
    parent_page_face_id uuid;
    parent_inherited_bread_crumbs_face_id uuid;
    parent_inherited_page_face_id uuid;
begin
    select
        bread_crumbs_face_id,
        page_face_id,
        inherited_bread_crumbs_face_id,
        inherited_page_face_id
    into
        parent_bread_crumbs_face_id,
        parent_page_face_id,
        parent_inherited_bread_crumbs_face_id,
        parent_inherited_page_face_id
    from twin_class
    where id = new.extends_twin_class_id;

    if parent_bread_crumbs_face_id is not null then
        new.inherited_bread_crumbs_face_id := parent_bread_crumbs_face_id;
    else
        new.inherited_bread_crumbs_face_id := parent_inherited_bread_crumbs_face_id;
    end if;

    if parent_page_face_id is not null then
        new.inherited_page_face_id := parent_page_face_id;
    else
        new.inherited_page_face_id := parent_inherited_page_face_id;
    end if;

    return new;
end;
$$ language plpgsql;

create or replace trigger twin_class_set_inherited_face_on_insert_trigger
    before insert
    on twin_class
    for each row
execute function twin_class_set_inherited_face_on_insert();


update twin_class
set page_face_id = page_face_id
where page_face_id is not null;

update twin_class
set bread_crumbs_face_id = bread_crumbs_face_id
where bread_crumbs_face_id is not null;
