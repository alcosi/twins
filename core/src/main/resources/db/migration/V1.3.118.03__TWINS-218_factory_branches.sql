create table if not exists twin_factory_branch
(
    id                            uuid not null
        constraint twin_factory_branch_pk
            primary key,
    twin_factory_id               uuid not null
        constraint twin_factory_branch_twin_factory_id_fk
            references twin_factory
            on update cascade on delete cascade,

    twin_factory_condition_set_id uuid
        constraint twin_factory_branch_twin_factory_condition_set_id_fk
            references twin_factory_condition_set
            on update cascade,
    twin_factory_condition_invert boolean default false,
    active                        boolean default true,
    next_twin_factory_id          uuid
        constraint twin_factory_branch_twin_factory_id_fk_2
            references twin_factory
            on update cascade on delete cascade,
    description                   varchar
);


create index if not exists twin_factory_branch_next_twin_factory_id_index
    on twin_factory_branch (next_twin_factory_id);


create index if not exists twin_factory_branch_twin_factory_condition_set_id_index
    on twin_factory_branch (twin_factory_condition_set_id);

create index if not exists twin_factory_branch_twin_factory_id_index
    on twin_factory_branch (twin_factory_id);


alter table public.twin_factory_pipeline
    add if not exists next_twin_factory_limit_scope boolean default true not null;

