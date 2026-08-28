-- TWINS-890: add flavor_data_list_id to twin_class (full parity with marker_data_list_id),
-- including the inherited_* columns populated by PostgreSQL triggers.

alter table twin_class
    add column if not exists flavor_data_list_id                        uuid references data_list on update cascade,
    add column if not exists inherited_flavor_data_list_id              uuid references data_list on update cascade on delete restrict,
    add column if not exists inherited_flavor_data_list_twin_class_id   uuid references twin_class on update cascade on delete restrict;

create index if not exists twin_class_flavor_data_list_id_index
    on twin_class (flavor_data_list_id);

create index if not exists twin_class_inherited_flavor_data_list_id_index
    on twin_class (inherited_flavor_data_list_id);

create index if not exists twin_class_inherited_flavor_data_list_twin_class_id_index
    on twin_class (inherited_flavor_data_list_twin_class_id);


create or replace function twin_class_update_inherited_flavor_data_list(old twin_class, new twin_class) returns void
    language plpgsql
as
$$
begin
    if new.flavor_data_list_id is null then
        update twin_class
        set inherited_flavor_data_list_id            = old.inherited_flavor_data_list_id,
            inherited_flavor_data_list_twin_class_id = old.inherited_flavor_data_list_twin_class_id
        where inherited_flavor_data_list_twin_class_id = old.id
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    elsif old.flavor_data_list_id is null then
        update twin_class
        set inherited_flavor_data_list_id            = new.flavor_data_list_id,
            inherited_flavor_data_list_twin_class_id = new.id
        where ((inherited_flavor_data_list_twin_class_id = old.inherited_flavor_data_list_twin_class_id) or
               (old.inherited_flavor_data_list_twin_class_id is null and
                inherited_flavor_data_list_twin_class_id is null))
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    else
        update twin_class
        set inherited_flavor_data_list_id = new.flavor_data_list_id
        where twin_class.inherited_flavor_data_list_twin_class_id = old.id
          and id != new.id;
    end if;
end;
$$;


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
    parent_flavor_data_list_id                      uuid;
    parent_inherited_marker_data_list_id            uuid;
    parent_inherited_marker_data_list_twin_class_id uuid;
    parent_inherited_tag_data_list_id               uuid;
    parent_inherited_tag_data_list_twin_class_id    uuid;
    parent_inherited_flavor_data_list_id            uuid;
    parent_inherited_flavor_data_list_twin_class_id uuid;
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
           flavor_data_list_id,
           inherited_bread_crumbs_face_id,
           inherited_bread_crumbs_twin_class_id,
           inherited_page_face_id,
           inherited_page_twin_class_id,
           inherited_marker_data_list_id,
           inherited_marker_data_list_twin_class_id,
           inherited_tag_data_list_id,
           inherited_tag_data_list_twin_class_id,
           inherited_flavor_data_list_id,
           inherited_flavor_data_list_twin_class_id
    into
        parent_id,
        parent_bread_crumbs_face_id,
        parent_page_face_id,
        parent_marker_data_list_id,
        parent_tag_data_list_id,
        parent_flavor_data_list_id,
        parent_inherited_bread_crumbs_face_id,
        parent_inherited_bread_crumbs_twin_class_id,
        parent_inherited_page_face_id,
        parent_inherited_page_twin_class_id,
        parent_inherited_marker_data_list_id,
        parent_inherited_marker_data_list_twin_class_id,
        parent_inherited_tag_data_list_id,
        parent_inherited_tag_data_list_twin_class_id,
        parent_inherited_flavor_data_list_id,
        parent_inherited_flavor_data_list_twin_class_id
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

    if parent_flavor_data_list_id is not null then
        new.inherited_flavor_data_list_id := parent_flavor_data_list_id;
        new.inherited_flavor_data_list_twin_class_id := parent_id;
    else
        new.inherited_flavor_data_list_id := parent_inherited_flavor_data_list_id;
        new.inherited_flavor_data_list_twin_class_id := parent_inherited_flavor_data_list_twin_class_id;
    end if;

    return new;
end;
$$;


create or replace function twin_class_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    -- fn's if view_permission_id changed
    IF NEW.view_permission_id IS DISTINCT FROM OLD.view_permission_id THEN
        UPDATE twin t SET view_permission_id = NEW.view_permission_id FROM twin_class tc WHERE not t.view_permission_custom and t.twin_class_id = NEW.id;
    END IF;

    -- Update tree if extends_twin_class_id changed
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id
        -- we need to update tree only in case if extends_twin_class_id was updated and tree wasn't
        AND NEW.extends_hierarchy_tree IS NOT DISTINCT FROM OLD.extends_hierarchy_tree THEN
        PERFORM hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    END IF;

    -- Update tree and has_segments if head_twin_class_id changed
    IF NEW.head_twin_class_id IS DISTINCT FROM OLD.head_twin_class_id THEN
        PERFORM hierarchy_twin_class_head_process_tree_update(old, new, TG_OP);
        PERFORM twin_class_has_segments_check(old.head_twin_class_id);
        PERFORM twin_class_has_segments_check(new.head_twin_class_id);
    END IF;

    -- Update has_segments if segment changed
    IF NEW.segment IS DISTINCT FROM OLD.segment THEN
        PERFORM twin_class_has_segments_check(new.head_twin_class_id);
    END IF;

    -- Recalculate hierarchy if schema space fields changed
    IF (NEW.permission_schema_space IS DISTINCT FROM OLD.permission_schema_space)
        OR (NEW.twinflow_schema_space IS DISTINCT FROM OLD.twinflow_schema_space)
        OR (NEW.twin_class_schema_space IS DISTINCT FROM OLD.twin_class_schema_space)
        OR (NEW.alias_space IS DISTINCT FROM OLD.alias_space) THEN
        PERFORM twin_class_hierarchy_recalculate(old, new);
    END IF;

    -- Auto update permissions if key changed
    IF NEW.key IS DISTINCT FROM OLD.key THEN
        PERFORM permissions_autoupdate_on_twin_class_update(old, new);
    END IF;

    -- Update inherited bread_crumbs_face_id if changed
    IF NEW.bread_crumbs_face_id IS DISTINCT FROM OLD.bread_crumbs_face_id THEN
        PERFORM twin_class_update_inherited_bread_crumbs_face_id(old, new);
    END IF;

    -- Update inherited page_face_id if changed
    IF NEW.page_face_id IS DISTINCT FROM OLD.page_face_id THEN
        PERFORM twin_class_update_inherited_page_face_id(old, new);
    END IF;

    -- Update inherited marker_data_list_id id if changed
    IF NEW.marker_data_list_id IS DISTINCT FROM OLD.marker_data_list_id THEN
        PERFORM twin_class_update_inherited_marker_data_list(old, new);
    END IF;

    -- Update inherited tag_data_list_id id if changed
    IF NEW.tag_data_list_id IS DISTINCT FROM OLD.tag_data_list_id THEN
        PERFORM twin_class_update_inherited_tag_data_list(old, new);
    END IF;

    -- Update inherited flavor_data_list_id id if changed
    IF NEW.flavor_data_list_id IS DISTINCT FROM OLD.flavor_data_list_id THEN
        PERFORM twin_class_update_inherited_flavor_data_list(old, new);
    END IF;

    -- Update direct children counters if parent references changed
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id THEN
        -- Update old parent's counter
        IF OLD.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(OLD.extends_twin_class_id, 'extends');
        END IF;

        -- Update new parent's counter
        IF NEW.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(NEW.extends_twin_class_id, 'extends');
        END IF;
    END IF;

    IF NEW.head_twin_class_id IS DISTINCT FROM OLD.head_twin_class_id THEN
        -- Update old parent's counter
        IF OLD.head_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(OLD.head_twin_class_id, 'head');
        END IF;

        -- Update new parent's counter
        IF NEW.head_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(NEW.head_twin_class_id, 'head');
        END IF;
    END IF;

    RETURN NEW;
END;
$$;
