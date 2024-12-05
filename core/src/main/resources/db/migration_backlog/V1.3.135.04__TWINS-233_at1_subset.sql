create table if not exists data_list_subset
(
    id uuid not null
        constraint data_list_subset_pk
            primary key,
    data_list_id uuid not null
        constraint data_list_subset_data_list_id_fk
            references data_list
            on update cascade on delete cascade,
    name varchar(100) not null,
    description varchar(255),
    key varchar(100) not null
);

create table if not exists data_list_subset_option
(
    data_list_subset_id uuid not null
        constraint data_list_subset_option_data_list_subset_id_fk
            references data_list_subset
            on update cascade on delete cascade,
    data_list_option_id uuid not null
        constraint data_list_subset_option_data_list_option_id_fk
            references data_list_option
            on update cascade on delete cascade,
    constraint data_list_subset_option_uq
        unique (data_list_subset_id, data_list_option_id)
);
