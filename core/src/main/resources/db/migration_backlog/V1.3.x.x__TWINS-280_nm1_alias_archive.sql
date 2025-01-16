alter table public.twin_alias
    add if not exists archived boolean default false not null;


