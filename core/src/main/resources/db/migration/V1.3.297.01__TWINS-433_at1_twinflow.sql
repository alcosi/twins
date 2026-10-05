alter table twinflow
    add if not exists initial_twin_factory_id uuid;

alter table twinflow
    drop constraint if exists twinflow_initial_factory_id_fk;

alter table twinflow
    add constraint twinflow_initial_factory_id_fk
        foreign key (initial_twin_factory_id) references twin_factory;

-- featurer
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2335, 23, '', '', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2336, 23, '', '', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2337, 23, '', '', '', false) on conflict on constraint featurer_pk do nothing ;
