alter table public.twin_class_owner_type
    add if not exists name_i18n_id uuid;

alter table public.twin_class_owner_type
    add if not exists description_i18n_id uuid;

create table if not exists domain_type_twin_class_owner_type
(
    domain_type_id varchar not null
        constraint domain_type_twin_class_owner_type_domain_type_id_fk
            references domain_type,
    twin_class_owner_type_id varchar not null
        constraint domain_type_twin_class_owner_type_twin_class_owner_type_id_fk
            references twin_class_owner_type,
        constraint domain_type_twin_class_owner_type_pk
    primary key (domain_type_id, twin_class_owner_type_id)
);

alter table domain_type_twin_class_owner_type owner to gateway80lvl;

INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('basic', 'businessAccount') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('basic', 'user') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('b2b', 'domain') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('b2b', 'domainBusinessAccount') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('b2b', 'domainUser') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;

