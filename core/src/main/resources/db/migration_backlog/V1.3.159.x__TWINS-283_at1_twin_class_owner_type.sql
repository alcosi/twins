alter table public.twin_class_owner_type
    add if not exists name_i18n_id uuid;

alter table public.twin_class_owner_type
    add if not exists description_i18n_id uuid;

create table if not exists domain_type_twin_class_owner_type
(
    domain_id uuid not null
    constraint domain_type_twin_class_owner_type_domain_id_fk
    references domain,
    twin_class_owner_type_id varchar not null
    constraint domain_type_twin_class_owner_type_tco_type_id_fk
    references twin_class_owner_type,
    constraint domain_type_twin_class_owner_type_pk
    primary key (domain_id, twin_class_owner_type_id)
    );

alter table domain_type_twin_class_owner_type owner to gateway80lvl;
