alter table if exists data_list_option
    add column if not exists custom boolean not null default false;
