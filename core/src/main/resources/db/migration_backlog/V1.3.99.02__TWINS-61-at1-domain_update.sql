-- fixing cyclic dependency in V1.3.74.02__TWINS-61-at2-domain_update.sql
alter table domain
    alter column ancestor_twin_class_id drop not null;