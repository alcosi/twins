alter table public.projection
    alter column projection_type_id set not null;

alter table public.data_list_option_projection
    alter column projection_type_id set not null;