create or replace function twin_class_update_extends_hierarchy_tree() returns trigger
    language plpgsql
as
$$
declare
    parent twin_class%rowtype;
begin
    if new.extends_hierarchy_tree is distinct from old.extends_hierarchy_tree then
        new.extends_twin_class_id := replace(subpath(new.extends_hierarchy_tree, -2, 1)::text, '_', '-')::uuid;

        select *
        into parent
        from twin_class
        where id=new.extends_twin_class_id;

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
    end if;

    return new;
end;
$$;

create or replace trigger twin_class_update_extends_hierarchy_tree_trigger
    before update of extends_hierarchy_tree
    on twin_class
    for each row
execute function twin_class_update_extends_hierarchy_tree();
