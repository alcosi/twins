alter table twin_class
    add column if not exists inherited_bread_crumbs_twin_class_id uuid references twin_class on update cascade on delete restrict,
    add column if not exists inherited_page_twin_class_id         uuid references twin_class on update cascade on delete restrict;

create index if not exists twin_class_inherited_page_twin_class_id_idx
    on twin_class (inherited_page_twin_class_id);

create index if not exists twin_class_inherited_bread_crumbs_twin_class_id_idx
    on twin_class (inherited_bread_crumbs_twin_class_id);

create or replace function twin_class_update_inherited_bread_crumbs_face_id()
    returns trigger as
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

    return new;
end;
$$ language plpgsql;

create or replace function twin_class_update_inherited_page_face_id()
    returns trigger as
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

    return new;
end;
$$ language plpgsql;

create or replace trigger twin_class_set_inherited_face_on_insert_trigger
    after insert
    on twin_class
    for each row
execute function twin_class_set_inherited_face_on_insert();

update twin_class t
set inherited_page_twin_class_id = parent.id
from (select distinct on (child.id) child.id as child_id,
                                    parent.id
      from twin_class child
               left join twin_class parent on (
          child.extends_hierarchy_tree <@ parent.extends_hierarchy_tree
              and parent.id != child.id
              and parent.page_face_id is not null
          )
      order by child.id, nlevel(parent.extends_hierarchy_tree) desc) parent
where t.id = parent.child_id
  and (t.inherited_page_twin_class_id is distinct from parent.id);

update twin_class t
set inherited_bread_crumbs_twin_class_id = parent.id
from (select distinct on (child.id) child.id as child_id,
                                    parent.id
      from twin_class child
               left join twin_class parent on (
          child.extends_hierarchy_tree <@ parent.extends_hierarchy_tree
              and parent.id != child.id
              and parent.bread_crumbs_face_id is not null
          )
      order by child.id, nlevel(parent.extends_hierarchy_tree) desc) parent
where t.id = parent.child_id
  and (t.inherited_bread_crumbs_twin_class_id is distinct from parent.id);

update twin_class
set page_face_id = page_face_id
where page_face_id is not null;

update twin_class
set bread_crumbs_face_id = bread_crumbs_face_id
where bread_crumbs_face_id is not null;