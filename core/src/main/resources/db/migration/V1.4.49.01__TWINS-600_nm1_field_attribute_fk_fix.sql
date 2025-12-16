alter table twin_field_attribute
    drop constraint if exists twin_field_attribute_twin_id_fkey;

alter table twin_field_attribute
    add foreign key (twin_id) references twin
        on update cascade on delete cascade;

alter table twin_field_attribute
    drop constraint if exists twin_field_attribute_twin_class_field_id_fkey;

alter table twin_field_attribute
    add foreign key (twin_class_field_id) references twin_class_field
        on update cascade on delete cascade;

alter table twin_field_attribute
    drop constraint if exists fk_twin_field_attribute_class_attr;

alter table twin_field_attribute
    add constraint fk_twin_field_attribute_class_attr
        foreign key (twin_class_field_attribute_id) references twin_class_field_attribute
            on update cascade on delete cascade;

