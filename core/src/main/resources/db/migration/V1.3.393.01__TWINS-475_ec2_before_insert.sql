create or replace function twin_class_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    new := twin_class_set_inherited_face_on_insert(new);

    return new;
end;
$$;

create or replace function twin_class_set_inherited_face_on_insert(new twin_class) returns twin_class
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

    return new;
end;
$$;
