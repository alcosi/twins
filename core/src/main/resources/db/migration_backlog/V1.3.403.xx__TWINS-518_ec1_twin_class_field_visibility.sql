create table if not exists twin_class_field_visibility (
    id varchar(20) primary key
);

insert into twin_class_field_visibility (id) values 
    ('PUBLIC'),
    ('PRIVATE'),
    ('PLUGGABLE')
on conflict do nothing;


alter table twin_class_field 
    add column if not exists twin_class_field_visibility_id varchar(20) not null references twin_class_field_visibility default 'PUBLIC';

create index if not exists twin_class_field_twin_class_field_visibility_id_idx
    on twin_class_field (twin_class_field_visibility_id);


create table if not exists twin_class_field_plug (
    twin_class_id uuid not null references twin_class on update cascade on delete cascade,
    twin_class_field_id uuid not null references twin_class_field on update cascade on delete cascade,
    primary key (twin_class_id, twin_class_field_id)
);

create index if not exists twin_class_field_plug_twin_class_id_idx
    on twin_class_field_plug (twin_class_id);

create index if not exists twin_class_field_plug_twin_class_field_id_idx
    on twin_class_field_plug (twin_class_field_id);


alter table twin_class
    add column if not exists has_plugged_fields boolean not null default false;


create or replace function twin_class_set_has_pluggable_fields_true(twin_class_id uuid) returns void
    language plpgsql
as
$$
begin
    update twin_class
    set has_plugged_fields = true
    where id = twin_class_id;
end;
$$;

create or replace function twin_class_set_has_pluggable_fields_false(tc_id uuid) returns void
    language plpgsql
as
$$
begin
    if not exists (
        select 1 from twin_class_field_plug tp
        where twin_class_id = tc_id
    ) then
        update twin_class
        set has_plugged_fields = false
        where id = tc_id;
    end if;
end;
$$;

create or replace function twin_class_field_plug_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_set_has_pluggable_fields_true(new.twin_class_id);

    return new;
end;
$$;

create or replace function twin_class_field_plug_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_set_has_pluggable_fields_false(old.twin_class_id);

    return old;
end;
$$;

create or replace trigger twin_class_field_plug_after_insert_wrapper_trigger
    after insert on twin_class_field_plug
    for each row
execute procedure twin_class_field_plug_after_insert_wrapper();

create or replace trigger twin_class_field_plug_after_delete_wrapper_trigger
    after delete on twin_class_field_plug
    for each row
execute procedure twin_class_field_plug_after_delete_wrapper();
