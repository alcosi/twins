alter table public.featurer_param_type
    alter column id type varchar(100) using id;

alter table public.featurer_param
    alter column featurer_param_type_id type varchar(100) using featurer_param_type_id;
