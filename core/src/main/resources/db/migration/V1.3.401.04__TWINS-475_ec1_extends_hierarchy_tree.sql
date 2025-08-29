create or replace function twin_class_update_extends_hierarchy_tree(old twin_class, new twin_class) returns twin_class
    language plpgsql
as
$$
declare
    parent twin_class%rowtype;
begin
    new.extends_twin_class_id := replace(subpath(new.extends_hierarchy_tree, -2, 1)::text, '_', '-')::uuid;

    select *
    into parent
    from twin_class
    where id = new.extends_twin_class_id;

    if parent.page_face_id is not null then
        new.inherited_page_face_id := parent.page_face_id;
        new.inherited_page_twin_class_id := parent.id;
    else
        new.inherited_page_face_id := parent.inherited_page_face_id;
        new.inherited_page_twin_class_id := parent.inherited_page_twin_class_id;
    end if;

    if parent.bread_crumbs_face_id is not null then
        new.inherited_bread_crumbs_face_id := parent.bread_crumbs_face_id;
        new.inherited_bread_crumbs_twin_class_id := parent.id;
    else
        new.inherited_bread_crumbs_face_id := parent.inherited_bread_crumbs_face_id;
        new.inherited_bread_crumbs_twin_class_id := parent.inherited_bread_crumbs_twin_class_id;
    end if;

    return new;
end;
$$;

create or replace function twin_class_before_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    if new.extends_hierarchy_tree is distinct from old.extends_hierarchy_tree and old.extends_hierarchy_tree is not null then
        new := twin_class_update_extends_hierarchy_tree(old, new);
    end if;

    return new;
end;
$$;

create or replace trigger twin_class_before_update_wrapper_trigger
    before update
    on twin_class
    for each row
execute procedure twin_class_before_update_wrapper();

-- logic change for extends_twin_class_id
create or replace function twin_class_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- Update tree if extends_twin_class_id changed
    if new.extends_twin_class_id is distinct from old.extends_twin_class_id
        -- we need to update tree only in case if extends_twin_class_id was updated and tree wasn't
        and new.extends_hierarchy_tree is not distinct from old.extends_hierarchy_tree then
        perform hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    end if;

    -- Update tree if head_twin_class_id changed
    if new.head_twin_class_id is distinct from old.head_twin_class_id then
        perform hierarchy_twin_class_head_process_tree_update(old, new, TG_OP);
    end if;

    -- Recalculate hierarchy if schema space fields changed
    if (new.permission_schema_space is distinct from old.permission_schema_space)
        or (new.twinflow_schema_space is distinct from old.twinflow_schema_space)
        or (new.twin_class_schema_space is distinct from old.twin_class_schema_space)
        or (new.alias_space is distinct from old.alias_space) then
        perform twin_class_hierarchy_recalculate(old, new);
    end if;

    -- Auto update permissions if key changed
    if new.key is distinct from old.key then
        perform permissions_autoupdate_on_twin_class_update(old, new);
    end if;

    -- Update inherited breadcrumbs face id if changed
    if new.bread_crumbs_face_id is distinct from old.bread_crumbs_face_id then
        perform twin_class_update_inherited_bread_crumbs_face_id(old, new);
    end if;

    -- Update inherited page face id if changed
    if new.page_face_id is distinct from old.page_face_id then
        perform twin_class_update_inherited_page_face_id(old, new);
    end if;

    return new;
end;
$$;
