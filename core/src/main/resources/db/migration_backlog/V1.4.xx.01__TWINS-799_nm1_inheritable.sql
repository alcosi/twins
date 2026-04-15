alter table twin_class_field
    add if not exists inheritable boolean default true not null;

alter table twin_status
    add if not exists inheritable boolean default true not null;

alter table link
    add if not exists inheritable boolean default true not null;

alter table twin_class_dynamic_marker
    add if not exists inheritable boolean default true not null;

alter table twinflow
    add if not exists inheritable boolean default true not null;