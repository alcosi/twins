create or replace function twin_class_before_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_set_inherited_face_on_insert(old, new);

    return new;
end;
$$;

create or replace trigger twin_class_before_insert_wrapper_trigger
    before insert
    on twin_class
    for each row
execute procedure twin_class_before_insert_wrapper();

create or replace function twin_class_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- Call tree update on insert when extends_twin_class_id is set
    perform hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);

    return new;
end;
$$;
