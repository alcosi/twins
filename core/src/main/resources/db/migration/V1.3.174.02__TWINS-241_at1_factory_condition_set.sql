alter table public.twin_factory_condition_set
    add column if not exists created_by_user_id uuid;

alter table public.twin_factory_condition_set
    add column if not exists updated_at timestamp;

alter table public.twin_factory_condition_set
    add column if not exists created_at timestamp default CURRENT_TIMESTAMP;

UPDATE public.twin_factory_condition_set
SET created_by_user_id = '00000000-0000-0000-0000-000000000000'::uuid WHERE created_by_user_id is null;

UPDATE public.twin_factory_condition_set
SET created_at = '2025-01-01 00:00:00.000000'::timestamp WHERE created_at is null;

alter table public.twin_factory_condition_set
    alter column created_by_user_id set not null;

alter table public.twin_factory_condition_set
    alter column created_at set not null;

alter table public.twin_factory_condition_set
    drop constraint if exists twin_factory_condition_set_user_id_fk;

alter table public.twin_factory_condition_set
    add constraint twin_factory_condition_set_user_id_fk
        foreign key (created_by_user_id) references public."user";
