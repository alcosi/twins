alter table public.twin_factory add column if not exists shared boolean default false;
alter table public.twin_factory add column if not exists created_by_user_id uuid;
alter table public.twin_factory add column if not exists created_at timestamp default CURRENT_TIMESTAMP;
