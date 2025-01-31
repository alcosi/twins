alter table public.featurer_param
    add column if not exists is_optional bool default false not null;
alter table public.featurer_param
    add column if not exists default_value varchar;
alter table public.featurer_param
    add column if not exists example_values varchar[];

