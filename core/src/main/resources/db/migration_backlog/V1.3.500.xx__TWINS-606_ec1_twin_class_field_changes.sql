alter table if exists twin_class_field
    add column if not exists projection_field boolean not null default false,
    add column if not exists has_projected_fields boolean not null default false;


create or replace function projection_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    update twin_class_field
        set has_projected_fields=true
    where id=new.src_twin_class_field_id;

    update twin_class_field
        set projection_field=true
    where id=new.dst_twin_class_field_id;

    return old;
end;
$$;

create or replace trigger projection_after_insert_wrapper_trigger
    after insert
    on projection
    for each row
    execute procedure projection_after_insert_wrapper();


create or replace function projection_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    update twin_class_field
    set has_projected_fields=true
    where id=new.src_twin_class_field_id;

    update twin_class_field
    set projection_field=true
    where id=new.dst_twin_class_field_id;

    return new;
end;
$$;

create or replace trigger projection_after_update_wrapper_trigger
    after update
    on projection
    for each row
execute procedure projection_after_update_wrapper();


-- to make triggers work
update projection
    set id = id;
