alter table twin_class_field
    add column if not exists external_properties hstore;
