alter table twinflow
    add if not exists initial_twin_factory_id uuid;

alter table twinflow
    drop constraint if exists twinflow_initial_factory_id_fk;

alter table twinflow
    add constraint twinflow_initial_factory_id_fk
        foreign key (initial_twin_factory_id) references twin_factory;