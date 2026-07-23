alter table twin_factory_pipeline
    drop constraint twin_factory_pipeline_twin_factory_id_fk_2;

alter table twin_factory_pipeline
    add constraint twin_factory_pipeline_twin_factory_id_fk_2
        foreign key (next_twin_factory_id) references twin_factory
            on update cascade on delete restrict;
