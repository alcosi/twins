alter table twin_class
    add column if not exists inherited_from_twin_class_id uuid references twin_class on update cascade on delete restrict;

create index if not exists twin_class_inherited_from_twin_class_id_idx
    on twin_class (inherited_from_twin_class_id);

create or replace function twin_class_update_inherited_bread_crumbs_face_id()
    returns trigger as
$$
begin
        IF NEW.bread_crumbs_face_id IS NULL THEN
            UPDATE twin_class
            SET
                inherited_bread_crumbs_face_id = OLD.inherited_bread_crumbs_face_id,
                inherited_from_twin_class_id = OLD.inherited_from_twin_class_id
            WHERE
                inherited_from_twin_class_id = OLD.id
              AND id != NEW.id  -- exclude self
              AND extends_hierarchy_tree <@ OLD.extends_hierarchy_tree; -- only children in hierarchy

        ELSIF OLD.bread_crumbs_face_id IS NULL THEN
            UPDATE twin_class
            SET
                inherited_bread_crumbs_face_id = NEW.bread_crumbs_face_id,
                inherited_from_twin_class_id = NEW.id
            WHERE
                inherited_from_twin_class_id = OLD.inherited_from_twin_class_id
              AND id != NEW.id
              AND extends_hierarchy_tree <@ OLD.extends_hierarchy_tree; -- only children in hierarchy

        ELSE
            UPDATE twin_class
            SET
                inherited_bread_crumbs_face_id = NEW.bread_crumbs_face_id
            WHERE
                inherited_from_twin_class_id = OLD.id
              AND id != NEW.id;
        END IF;
    RETURN NEW;
end;
$$ language plpgsql;

CREATE OR REPLACE FUNCTION twin_class_update_inherited_page_face_id()
    RETURNS TRIGGER AS $$
BEGIN
        IF NEW.page_face_id IS NULL THEN
            UPDATE twin_class
            SET
                inherited_page_face_id = OLD.inherited_page_face_id,
                inherited_from_twin_class_id = OLD.inherited_from_twin_class_id
            WHERE
                inherited_from_twin_class_id = OLD.id
              AND id != NEW.id  -- exclude self
              AND extends_hierarchy_tree <@ OLD.extends_hierarchy_tree; -- only children in hierarchy

        ELSIF OLD.page_face_id IS NULL THEN
            UPDATE twin_class
            SET
                inherited_page_face_id = NEW.page_face_id,
                inherited_from_twin_class_id = NEW.id
            WHERE
                inherited_from_twin_class_id = OLD.inherited_from_twin_class_id
              AND id != NEW.id
              AND extends_hierarchy_tree <@ OLD.extends_hierarchy_tree; -- only children in hierarchy

        ELSE
            UPDATE twin_class
            SET
                inherited_page_face_id = NEW.page_face_id
            WHERE
                inherited_from_twin_class_id = OLD.id
              AND id != NEW.id;
        END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

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
    parent_inherited_from_twin_class_id uuid;
begin
    select
        bread_crumbs_face_id,
        page_face_id,
        inherited_bread_crumbs_face_id,
        inherited_page_face_id,
        inherited_from_twin_class_id
    into
        parent_bread_crumbs_face_id,
        parent_page_face_id,
        parent_inherited_bread_crumbs_face_id,
        parent_inherited_page_face_id,
        parent_inherited_from_twin_class_id
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

    new.inherited_from_twin_class_id := parent_inherited_from_twin_class_id;

    return new;
end;
$$ language plpgsql;

create or replace trigger twin_class_set_inherited_face_on_insert_trigger
    before insert
    on twin_class
    for each row
execute function twin_class_set_inherited_face_on_insert();


UPDATE twin_class t
SET inherited_from_twin_class_id = parent.id
FROM (
         SELECT DISTINCT ON (child.id)
             child.id AS child_id,
             parent.id
         FROM twin_class child
                  LEFT JOIN twin_class parent ON (
             child.extends_hierarchy_tree <@ parent.extends_hierarchy_tree
                 AND parent.id != child.id
                 AND parent.page_face_id IS NOT NULL
             )
         ORDER BY child.id, nlevel(parent.extends_hierarchy_tree) DESC
     ) parent
WHERE t.id = parent.child_id
  AND (t.inherited_from_twin_class_id IS DISTINCT FROM parent.id);

update twin_class
set page_face_id = page_face_id
where page_face_id is not null;

update twin_class
set bread_crumbs_face_id = bread_crumbs_face_id
where bread_crumbs_face_id is not null;
