alter table twin_class
    add column if not exists inherited_marker_data_list_id            uuid references data_list on update cascade on delete no action,
    add column if not exists inherited_marker_data_list_twin_class_id uuid references twin_class on update cascade on delete no action,
    add column if not exists inherited_tag_data_list_id               uuid references data_list on update cascade on delete no action,
    add column if not exists inherited_tag_data_list_twin_class_id    uuid references twin_class on update cascade on delete no action;

create index if not exists twin_class_inherited_marker_data_list_id_index
    on twin_class (inherited_marker_data_list_id);

create index if not exists twin_class_inherited_marker_data_list_twin_class_id_index
    on twin_class (inherited_marker_data_list_twin_class_id);

create index if not exists twin_class_inherited_tag_data_list_id_index
    on twin_class (inherited_tag_data_list_id);

create index if not exists twin_class_inherited_tag_data_list_twin_class_id_index
    on twin_class (inherited_tag_data_list_twin_class_id);


create or replace function twin_class_set_inherited_fields_on_insert(new twin_class) returns twin_class
    language plpgsql
as
$$
declare
    parent_id                                       uuid;
    parent_bread_crumbs_face_id                     uuid;
    parent_page_face_id                             uuid;
    parent_marker_data_list_id                      uuid;
    parent_tag_data_list_id                         uuid;
    parent_inherited_marker_data_list_id            uuid;
    parent_inherited_marker_data_list_twin_class_id uuid;
    parent_inherited_tag_data_list_id               uuid;
    parent_inherited_tag_data_list_twin_class_id    uuid;
    parent_inherited_bread_crumbs_face_id           uuid;
    parent_inherited_bread_crumbs_twin_class_id     uuid;
    parent_inherited_page_face_id                   uuid;
    parent_inherited_page_twin_class_id             uuid;
begin
    select id,
           bread_crumbs_face_id,
           page_face_id,
           marker_data_list_id,
           tag_data_list_id,
           inherited_bread_crumbs_face_id,
           inherited_bread_crumbs_twin_class_id,
           inherited_page_face_id,
           inherited_page_twin_class_id,
           inherited_marker_data_list_id,
           inherited_marker_data_list_twin_class_id,
           inherited_tag_data_list_id,
           inherited_tag_data_list_twin_class_id
    into
        parent_id,
        parent_bread_crumbs_face_id,
        parent_page_face_id,
        parent_marker_data_list_id,
        parent_tag_data_list_id,
        parent_inherited_bread_crumbs_face_id,
        parent_inherited_bread_crumbs_twin_class_id,
        parent_inherited_page_face_id,
        parent_inherited_page_twin_class_id,
        parent_inherited_marker_data_list_id,
        parent_inherited_marker_data_list_twin_class_id,
        parent_inherited_tag_data_list_id,
        parent_inherited_tag_data_list_twin_class_id
    from twin_class
    where id = new.extends_twin_class_id;

    if parent_bread_crumbs_face_id is not null then
        new.inherited_bread_crumbs_face_id := parent_bread_crumbs_face_id;
        new.inherited_bread_crumbs_twin_class_id := parent_id;
    else
        new.inherited_bread_crumbs_face_id := parent_inherited_bread_crumbs_face_id;
        new.inherited_bread_crumbs_twin_class_id := parent_inherited_bread_crumbs_twin_class_id;
    end if;

    if parent_page_face_id is not null then
        new.inherited_page_face_id := parent_page_face_id;
        new.inherited_page_twin_class_id := parent_id;
    else
        new.inherited_page_face_id := parent_inherited_page_face_id;
        new.inherited_page_twin_class_id := parent_inherited_page_twin_class_id;
    end if;

    if parent_marker_data_list_id is not null then
        new.inherited_marker_data_list_id := parent_marker_data_list_id;
        new.inherited_marker_data_list_twin_class_id := parent_id;
    else
        new.inherited_marker_data_list_id := parent_inherited_marker_data_list_id;
        new.inherited_marker_data_list_twin_class_id := parent_inherited_marker_data_list_twin_class_id;
    end if;

    if parent_tag_data_list_id is not null then
        new.inherited_tag_data_list_id := parent_tag_data_list_id;
        new.inherited_tag_data_list_twin_class_id := parent_id;
    else
        new.inherited_tag_data_list_id := parent_inherited_tag_data_list_id;
        new.inherited_tag_data_list_twin_class_id := parent_inherited_tag_data_list_twin_class_id;
    end if;

    return new;
end;
$$;

create or replace function twin_class_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    new := twin_class_set_inherited_fields_on_insert(new);

    return new;
end;
$$;

drop function if exists twin_class_set_inherited_face_on_insert(twin_class);


create or replace function twin_class_update_inherited_marker_data_list(old twin_class, new twin_class) returns void
    language plpgsql
as
$$
begin
    if new.marker_data_list_id is null then
        update twin_class
        set inherited_marker_data_list_id            = old.inherited_marker_data_list_id,
            inherited_marker_data_list_twin_class_id = old.inherited_marker_data_list_twin_class_id
        where inherited_marker_data_list_twin_class_id = old.id
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    elsif old.marker_data_list_id is null then
        update twin_class
        set inherited_marker_data_list_id            = new.marker_data_list_id,
            inherited_marker_data_list_twin_class_id = new.id
        where ((inherited_marker_data_list_twin_class_id = old.inherited_marker_data_list_twin_class_id) or
               (old.inherited_marker_data_list_twin_class_id is null and
                inherited_marker_data_list_twin_class_id is null))
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    else
        update twin_class
        set inherited_marker_data_list_id = new.marker_data_list_id
        where twin_class.inherited_marker_data_list_twin_class_id = old.id
          and id != new.id;
    end if;
end;
$$;

create or replace function twin_class_update_inherited_tag_data_list(old twin_class, new twin_class) returns void
    language plpgsql
as
$$
begin
    if new.tag_data_list_id is null then
        update twin_class
        set inherited_tag_data_list_id            = old.inherited_tag_data_list_id,
            inherited_tag_data_list_twin_class_id = old.inherited_tag_data_list_twin_class_id
        where inherited_tag_data_list_twin_class_id = old.id
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    elsif old.tag_data_list_id is null then
        update twin_class
        set inherited_tag_data_list_id            = new.tag_data_list_id,
            inherited_tag_data_list_twin_class_id = new.id
        where ((inherited_tag_data_list_twin_class_id = old.inherited_tag_data_list_twin_class_id) or
               (old.inherited_tag_data_list_twin_class_id is null and inherited_tag_data_list_twin_class_id is null))
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    else
        update twin_class
        set inherited_tag_data_list_id = new.tag_data_list_id
        where twin_class.inherited_tag_data_list_twin_class_id = old.id
          and id != new.id;
    end if;
end;
$$;

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

    -- Update tree and has_segments if head_twin_class_id changed
    if new.head_twin_class_id is distinct from old.head_twin_class_id then
        perform hierarchy_twin_class_head_process_tree_update(old, new, TG_OP);
        perform twin_class_has_segments_check(old.head_twin_class_id);
        perform twin_class_has_segments_check(new.head_twin_class_id);
    end if;

    -- Update has_segments if segment changed
    if new.segment is distinct from old.segment then
        perform twin_class_has_segments_check(new.head_twin_class_id);
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

    -- Update inherited bread_crumbs_face_id if changed
    if new.bread_crumbs_face_id is distinct from old.bread_crumbs_face_id then
        perform twin_class_update_inherited_bread_crumbs_face_id(old, new);
    end if;

    -- Update inherited page_face_id if changed
    if new.page_face_id is distinct from old.page_face_id then
        perform twin_class_update_inherited_page_face_id(old, new);
    end if;

    -- Update inherited marker_data_list_id id if changed
    if new.marker_data_list_id is distinct from old.marker_data_list_id then
        perform twin_class_update_inherited_marker_data_list(old, new);
    end if;

    -- Update inherited tag_data_list_id id if changed
    if new.tag_data_list_id is distinct from old.tag_data_list_id then
        perform twin_class_update_inherited_tag_data_list(old, new);
    end if;

    return new;
end;
$$;
