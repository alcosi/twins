create or replace function twin_class_update_inherited_page_face_id(
    old twin_class,
    new twin_class
) returns void
    language plpgsql
as
$$
begin
    if new.page_face_id is null then
        update twin_class
        set inherited_page_face_id       = old.inherited_page_face_id,
            inherited_page_twin_class_id = old.inherited_page_twin_class_id
        where inherited_page_twin_class_id = old.id
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    elsif old.page_face_id is null then
        update twin_class
        set inherited_page_face_id       = new.page_face_id,
            inherited_page_twin_class_id = new.id
        where ((inherited_page_twin_class_id = old.inherited_page_twin_class_id) or
               (old.inherited_page_twin_class_id is null and inherited_page_twin_class_id is null))
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    else
        update twin_class
        set inherited_page_face_id = new.page_face_id
        where inherited_page_twin_class_id = old.id
          and id != new.id;
    end if;
end;
$$;

create or replace function twin_class_update_inherited_bread_crumbs_face_id(
    old twin_class,
    new twin_class
) returns void
    language plpgsql
as
$$
begin
    if new.bread_crumbs_face_id is null then
        update twin_class
        set inherited_bread_crumbs_face_id       = old.inherited_bread_crumbs_face_id,
            inherited_bread_crumbs_twin_class_id = old.inherited_bread_crumbs_twin_class_id
        where inherited_bread_crumbs_twin_class_id = old.id
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    elsif old.bread_crumbs_face_id is null then
        update twin_class
        set inherited_bread_crumbs_face_id       = new.bread_crumbs_face_id,
            inherited_bread_crumbs_twin_class_id = new.id
        where ((inherited_bread_crumbs_twin_class_id = old.inherited_bread_crumbs_twin_class_id) or
               (old.inherited_bread_crumbs_twin_class_id is null and inherited_bread_crumbs_twin_class_id is null))
          and id != new.id
          and extends_hierarchy_tree <@ old.extends_hierarchy_tree;

    else
        update twin_class
        set inherited_bread_crumbs_face_id = new.bread_crumbs_face_id
        where inherited_bread_crumbs_twin_class_id = old.id
          and id != new.id;
    end if;
end;
$$;


create or replace function permissions_autoupdate_on_twin_class_update(
    old twin_class,
    new twin_class
)
    returns void
    language plpgsql
as
$$
DECLARE
    old_twin_class_key  TEXT;
    new_twin_class_key  TEXT;
    permission_id       UUID;
    perm_group_id       UUID;
    i18n_id_name        UUID;
    i18n_id_description UUID;
    perm                VARCHAR;
    perm_arr            VARCHAR[] := ARRAY ['CREATE','EDIT','DELETE','VIEW'];
BEGIN
    IF (NEW.domain_id IS NULL) THEN
        return;
    end if;
    IF (OLD.key = NEW.key) THEN
        RETURN;
    END IF;

    old_twin_class_key := OLD.key;
    new_twin_class_key := NEW.key;

    SELECT id INTO perm_group_id FROM permission_group WHERE twin_class_id = NEW.id AND domain_id = NEW.domain_id;

    UPDATE permission_group SET key  = new_twin_class_key || '_PERMISSIONS', name = lower(replace(new_twin_class_key, '_', ' ') || ' permissions') WHERE twin_class_id = NEW.id AND domain_id = NEW.domain_id;

    FOREACH perm SLICE 0 IN ARRAY perm_arr
        LOOP
            SELECT id, name_i18n_id, description_i18n_id INTO permission_id, i18n_id_name, i18n_id_description FROM permission WHERE key = old_twin_class_key || '_' || perm and permission_group_id = perm_group_id;
            UPDATE i18n_translation SET translation = lower(replace(new_twin_class_key, '_', ' ')) || ' ' || lower(perm) || ' permission' WHERE i18n_id = i18n_id_name AND locale = 'en';
            UPDATE i18n_translation SET translation = lower(replace(new_twin_class_key, '_', ' ')) || ' ' || lower(perm) || ' permission' WHERE i18n_id = i18n_id_description AND locale = 'en';
            UPDATE permission SET key = new_twin_class_key || '_' || perm WHERE id = permission_id;
        END LOOP;
END;
$$;


create or replace function hierarchy_twin_class_head_process_tree_update(
    old twin_class,
    new twin_class,
    tg_op text

) returns void
    language plpgsql
as
$$
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
END;
$$;

create or replace function hierarchy_twin_class_extends_process_tree_update(
    old twin_class,
    new twin_class,
    tg_op text
) returns void
    language plpgsql
as
$$
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
END;
$$;


-- Create pure function without trigger signature
create or replace function twin_class_hierarchy_recalculate(
    old twin_class,
    new twin_class
)
    returns void
    language plpgsql
as $$
begin
    if old.permission_schema_space is distinct from new.permission_schema_space
        or old.twinflow_schema_space is distinct from new.twinflow_schema_space
        or old.twin_class_schema_space is distinct from new.twin_class_schema_space
        or old.alias_space is distinct from new.alias_space then
        perform public.hierarchyUpdateTreeHard(t.id, null)
        from public.twin t
        where t.twin_class_id = new.id;
    end if;
end;
$$;

-- Update wrapper function to call the pure logic function
create or replace function twin_class_after_update_wrapper()
    returns trigger
    language plpgsql
as $$
begin
    -- Update tree if extends_twin_class_id changed
    if new.extends_twin_class_id is distinct from old.extends_twin_class_id then
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

create or replace function twin_class_on_delete_i18n_and_translations_delete(
    old twin_class
) returns void
    language plpgsql
as
$$
BEGIN
    BEGIN
        DELETE FROM i18n WHERE id = OLD.name_i18n_id;
    EXCEPTION
        WHEN others THEN
            RAISE NOTICE 'error: %', SQLERRM;
    END;

    BEGIN
        DELETE FROM i18n WHERE id = OLD.description_i18n_id;
    EXCEPTION
        WHEN others THEN
            RAISE NOTICE 'error: %', SQLERRM;
    END;
END;
$$;

create or replace function twin_class_after_delete_wrapper()
    returns trigger as $$
begin
    -- Remove i18n and translations for deleted twin_class
    perform twin_class_on_delete_i18n_and_translations_delete(old);
    return old;
end;
$$ language plpgsql;


create or replace function twin_class_set_inherited_face_on_insert(
    old twin_class,
    new twin_class
) returns void
    language plpgsql
as
$$
declare
    parent_id                                   uuid;
    parent_bread_crumbs_face_id                 uuid;
    parent_page_face_id                         uuid;
    parent_inherited_bread_crumbs_face_id       uuid;
    parent_inherited_page_face_id               uuid;
    parent_inherited_page_twin_class_id         uuid;
    parent_inherited_bread_crumbs_twin_class_id uuid;
begin
    select id,
           bread_crumbs_face_id,
           page_face_id,
           inherited_bread_crumbs_face_id,
           inherited_page_face_id,
           inherited_page_twin_class_id,
           inherited_bread_crumbs_face_id
    into
        parent_id,
        parent_bread_crumbs_face_id,
        parent_page_face_id,
        parent_inherited_bread_crumbs_face_id,
        parent_inherited_page_face_id,
        parent_inherited_page_twin_class_id,
        parent_inherited_bread_crumbs_twin_class_id
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

end;
$$;


create or replace function twin_class_after_insert_wrapper()
    returns trigger as $$
begin
    -- Call tree update on insert when extends_twin_class_id is set
    perform hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);

    -- Set inherited face data for new twin_class
    perform twin_class_set_inherited_face_on_insert(old, new);

    return new;
end;
$$ language plpgsql;

