alter table public.twin_factory_eraser
    alter column twin_factory_condition_invert set not null;

alter table public.twin_factory_eraser
    alter column active set not null;