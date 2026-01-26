alter table twinflow_transition
    drop column if exists after_perform_twin_factory_id;


alter table twin_factory_pipeline
    add if not exists after_commit_twin_factory_id uuid
        constraint twin_factory_pipeline_after_commit_twin_factory_id_fk
            references twin_factory
            on update cascade on delete restrict;

create index if not exists twin_factory_pipeline_after_commit_twin_factory_id_index
    on twin_factory_pipeline (after_commit_twin_factory_id);