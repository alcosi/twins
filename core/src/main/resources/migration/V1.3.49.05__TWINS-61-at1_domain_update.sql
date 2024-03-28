alter table domain
    add if not exists ancestor_twin_class_id uuid;

alter table domain drop constraint if exists domain_twin_class_id_fk;

alter table domain
    add constraint domain_twin_class_id_fk
        foreign key (ancestor_twin_class_id) references twin_class;
