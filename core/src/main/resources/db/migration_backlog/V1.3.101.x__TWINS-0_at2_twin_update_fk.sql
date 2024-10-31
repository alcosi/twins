alter table twin
drop constraint fk_twin_permission_schema_space_id;

alter table twin
    add constraint fk_twin_permission_schema_space_id
        foreign key (permission_schema_space_id) references twin
            on delete cascade;


alter table twin
drop constraint fk_twin_twinflow_schema_space_id;

alter table twin
    add constraint fk_twin_twinflow_schema_space_id
        foreign key (twinflow_schema_space_id) references twin
            on delete cascade;


alter table twin
drop constraint fk_twin_twin_class_schema_space_id;

alter table twin
    add constraint fk_twin_twin_class_schema_space_id
        foreign key (twin_class_schema_space_id) references twin
            on delete cascade;