alter table public.twin_factory_condition_set
    add column  if not exists domain_id uuid;

alter table twin_factory_condition_set
    drop constraint if exists twin_factory_condition_set_domain_id_fk;

alter table twin_factory_condition_set
    add constraint twin_factory_condition_set_domain_id_fk
        foreign key (domain_id) references public.domain (id);
