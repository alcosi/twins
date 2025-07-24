create table if not exists twin_statistic
(
    id                    uuid    not null
        constraint twin_statistic_pk
        primary key,
    domain_id             uuid
        constraint twin_statistic_domain_id_fk
        references domain
        on update cascade on delete cascade,
    statister_featurer_id integer not null
        constraint twin_statistic_featurer_id_fk
        references featurer
        on update restrict on delete cascade,
    statister_params      hstore
);

create index if not exists twin_statistic_statister_domain_id_index
    on twin_statistic (domain_id);

create index if not exists twin_statistic_statister_featurer_id_index
    on twin_statistic (statister_featurer_id);
