alter table public.twin
    alter column external_id type varchar using external_id::varchar;