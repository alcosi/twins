alter table public.search
    add column if not exists children_twin_search_id uuid;

alter table public.search
    drop constraint if exists search_children_search_id_fk;

alter table public.search
    add constraint search_children_search_id_fk
        foreign key (children_twin_search_id) references public.search (id);
