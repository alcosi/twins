-- change name for index for table twin_field_simple
alter index if exists twin_field_simple_twin_id_twin_class_field_id_uindex rename to twin_id_twin_class_field_id_uindex;

-- change index for twin_field_user
drop index if exists twin_field_user_twin_class_field_id_index;

drop index if exists twin_class_field_id_user_id_index;

create index twin_class_field_id_user_id_index
    on twin_field_user (twin_class_field_id, user_id);
