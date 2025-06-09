-- change index name
alter index if exists field_id_value_uindex rename to twin_field_simple_twin_class_field_id_value_uindex;

-- change index for twin_field_user
drop index if exists twin_field_user_twin_class_field_id_index;

drop index if exists twin_field_user_twin_class_field_id_user_id_index;

create index twin_field_user_twin_class_field_id_user_id_index
    on twin_field_user (twin_class_field_id, user_id);
