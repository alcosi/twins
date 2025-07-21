create table if not exists twin_statistic
(
    id                    uuid    not null
        constraint twin_statistic_pk
            primary key,
    domain_id             uuid
        constraint twin_statistic_domain_id_fk
            references domain,
    statister_featurer_id integer not null
        constraint twin_statistic_featurer_id_fk
            references featurer,
    statister_params      hstore
);
