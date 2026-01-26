alter table twin_class
    add if not exists segment boolean default false not null;

alter table twin_class
    add if not exists has_segments boolean default false not null;

alter table twin_class_field
    add if not exists system boolean default false not null;

create index if not exists twin_class_segment_index
    on twin_class (segment);

create index if not exists twin_class_has_segments_index
    on twin_class (has_segments);


create index if not exists twin_class_field_system_index
    on twin_class_field (system);

CREATE OR REPLACE FUNCTION twin_class_has_segments_check(head_id uuid) returns void
    LANGUAGE plpgsql
AS $$
BEGIN
    IF head_id IS NOT NULL THEN
        UPDATE twin_class
        SET has_segments = (
            SELECT EXISTS (
                SELECT 1
                FROM twin_class child
                WHERE child.head_twin_class_id = head_id
                  AND child.segment = TRUE
            )
        )
        WHERE id = head_id;
    END IF;
END;
$$;

create or replace function twin_class_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- Remove i18n and translations for deleted twin_class
    perform twin_class_on_delete_i18n_and_translations_delete(old);
    perform twin_class_has_segments_check(old.head_twin_class_id);
    return old;
end;
$$;

create or replace function twin_class_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- Call tree update on insert when extends_twin_class_id is set
    perform hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    perform twin_class_has_segments_check(new.head_twin_class_id);
    return new;
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



