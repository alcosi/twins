alter table twin_class
    add column if not exists inherited_bread_crumbs_face_id uuid references face on update cascade on delete restrict,
    add column if not exists inherited_page_face_id uuid references face on update cascade on delete restrict;

create index if not exists twin_class_inherited_bread_crumbs_face_id_idx
    on twin_class (inherited_bread_crumbs_face_id);

create index if not exists twin_class_inherited_page_face_id_idx
    on twin_class (inherited_page_face_id);


create or replace function twin_class_update_inherited_bread_crumbs_face_id()
    returns trigger as
$$
begin
    update twin_class
    set inherited_bread_crumbs_face_id = new.bread_crumbs_face_id
    where extends_hierarchy_tree <@ new.extends_hierarchy_tree
      and id != new.id;

    return new;
end;
$$ language plpgsql;

create or replace function twin_class_update_inherited_page_face_id()
    returns trigger as
$$
begin
    update twin_class
    set inherited_page_face_id = new.page_face_id
    where extends_hierarchy_tree <@ new.extends_hierarchy_tree
      and id != new.id;

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
begin
    select inherited_bread_crumbs_face_id, inherited_page_face_id
    into new.inherited_bread_crumbs_face_id, new.inherited_page_face_id
    from twin_class
    where id = new.extends_twin_class_id;

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
