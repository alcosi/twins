alter table public.twin_factory_eraser
    alter column twin_factory_eraser_action type varchar(20) using twin_factory_eraser_action::varchar(20);
