-- 1. Drop old triggers
drop trigger if exists hierarchy_twin_class_extends_update_tree_trigger on twin_class;
drop trigger if exists hierarchy_twin_class_head_update_tree_trigger on twin_class;
drop trigger if exists hierarchyrecalculatetrigger on twin_class;
drop trigger if exists permissions_on_twin_class_update on twin_class;
drop trigger if exists twin_class_on_delete_i18n_and_translations_delete on twin_class;
drop trigger if exists twin_class_set_inherited_face_on_insert_trigger on twin_class;
drop trigger if exists twin_class_update_inherited_bread_crumbs_face_id_trigger on twin_class;
drop trigger if exists twin_class_update_inherited_page_face_id_trigger on twin_class;
drop trigger if exists twin_class_after_insert_wrapper_trigger on twin_class;
drop trigger if exists twin_class_after_update_wrapper_trigger on twin_class;
drop trigger if exists twin_class_after_delete_wrapper_trigger on twin_class;

-- Drop old function
drop function if exists hierarchyrecalculateforclasstwins();

-- Create renamed function
create or replace function twin_class_hierarchy_recalculate()
    returns trigger
    language plpgsql
as
$$
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


-- 2. Insert wrapper function
create or replace function twin_class_after_insert_wrapper()
    returns trigger as $$
begin
    -- Call tree update on insert when extends_twin_class_id is set
    perform hierarchy_twin_class_extends_process_tree_update();

    -- Set inherited face data for new twin_class
    perform twin_class_set_inherited_face_on_insert();

    return new;
end;
$$ language plpgsql;

-- 3. Update wrapper function
create or replace function twin_class_after_update_wrapper()
    returns trigger as $$
begin
    -- Update tree if extends_twin_class_id changed
    if new.extends_twin_class_id is distinct from old.extends_twin_class_id then
        perform hierarchy_twin_class_extends_process_tree_update();
    end if;

    -- Update tree if head_twin_class_id changed
    if new.head_twin_class_id is distinct from old.head_twin_class_id then
        perform hierarchy_twin_class_head_process_tree_update();
    end if;

    -- Recalculate hierarchy if schema space fields changed
    if (new.permission_schema_space is distinct from old.permission_schema_space)
        or (new.twinflow_schema_space is distinct from old.twinflow_schema_space)
        or (new.twin_class_schema_space is distinct from old.twin_class_schema_space)
        or (new.alias_space is distinct from old.alias_space) then
        perform twin_class_hierarchy_recalculate();
    end if;

    -- Auto update permissions if key changed
    if new.key is distinct from old.key then
        perform permissions_autoupdate_on_twin_class_update();
    end if;

    -- Update inherited breadcrumbs face id if changed
    if new.bread_crumbs_face_id is distinct from old.bread_crumbs_face_id then
        perform twin_class_update_inherited_bread_crumbs_face_id();
    end if;

    -- Update inherited page face id if changed
    if new.page_face_id is distinct from old.page_face_id then
        perform twin_class_update_inherited_page_face_id();
    end if;

    return new;
end;
$$ language plpgsql;

-- 4. Delete wrapper function
create or replace function twin_class_after_delete_wrapper()
    returns trigger as $$
begin
    -- Remove i18n and translations for deleted twin_class
    perform twin_class_on_delete_i18n_and_translations_delete();
    return old;
end;
$$ language plpgsql;

-- 5. Create new wrapper triggers
create trigger twin_class_after_insert_wrapper_trigger
    after insert on twin_class
    for each row
execute procedure twin_class_after_insert_wrapper();

create trigger twin_class_after_update_wrapper_trigger
    after update on twin_class
    for each row
execute procedure twin_class_after_update_wrapper();

create trigger twin_class_after_delete_wrapper_trigger
    after delete on twin_class
    for each row
execute procedure twin_class_after_delete_wrapper();
