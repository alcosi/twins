alter table twin_class
    add if not exists segment boolean default false not null;

alter table twin_class
    add if not exists has_segments boolean default false not null;

-- TODO add trigger to set has_segments flag

alter table twin_class_field
    add if not exists system boolean default false not null;

create index if not exists twin_class_segment_index
    on twin_class (segment);

create index if not exists twin_class_has_segments_index
    on twin_class (has_segments);


create index if not exists twin_class_field_system_index
    on twin_class_field (system);
