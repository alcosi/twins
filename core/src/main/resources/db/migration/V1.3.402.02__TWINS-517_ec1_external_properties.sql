alter table twin_class
    add column if not exists external_properties hstore;
