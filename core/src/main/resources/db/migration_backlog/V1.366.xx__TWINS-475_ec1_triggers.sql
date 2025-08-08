create or replace function twin_class_update_inherited_bread_crumbs_face_id()
    returns trigger as
$$
begin
    if new.bread_crumbs_face_id is not null then
        update twin_class
        set inherited_bread_crumbs_face_id = new.bread_crumbs_face_id
        where extends_hierarchy_tree <@ new.extends_hierarchy_tree
          and id != new.id;
    else
        update twin_class
        set inherited_bread_crumbs_face_id = new.inherited_bread_crumbs_face_id
        where extends_hierarchy_tree <@ new.extends_hierarchy_tree
          and id != new.id;
    end if;

    return new;
end;
$$ language plpgsql;

create or replace function twin_class_update_inherited_page_face_id()
    returns trigger as
$$
begin
    if new.page_face_id is not null then
        update twin_class
        set inherited_page_face_id = new.page_face_id
        where extends_hierarchy_tree <@ new.extends_hierarchy_tree
          and id != new.id;
    else
        update twin_class
        set inherited_page_face_id = new.inherited_page_face_id
        where extends_hierarchy_tree <@ new.extends_hierarchy_tree
          and id != new.id;
    end if;

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
