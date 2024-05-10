create table if not exists twin_starred
(
    id         uuid
        constraint twin_starred_pk
            primary key,
    user_id    uuid not null
        constraint twin_starred_user_id_fk
            references "user",
    twin_id    uuid not null
        constraint twin_starred_twin_id_fk
            references twin,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);