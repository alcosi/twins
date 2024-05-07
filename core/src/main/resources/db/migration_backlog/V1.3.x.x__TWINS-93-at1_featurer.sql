-- Head Hunter Featurer
-- insert new featurer type
INSERT INTO public.featurer_type (id, name, description) VALUES (26, 'HeadHunter', null) on conflict on constraint featurer_type_pk do update set name = excluded.name, description = excluded.description;
-- insert new featurers
INSERT INTO public.featurer (id, featurer_type_id, class, name, description) VALUES (2601, 26, 'org.twins.core.featurer.twinclass.validator.HeadHunterImpl', 'HeadHunterImpl', 'Returns all twins of given class (no other filters)') on conflict on constraint featurer_pk do update set featurer_type_id = excluded.featurer_type_id, class = excluded.class, name = excluded.name, description = excluded.description;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description) VALUES (2602, 26, 'org.twins.core.featurer.twinclass.validator.HeadHunterByStatus', 'HeadHunterByStatus', 'Returns all twins of given class and include/exclude given statuses list') on conflict on constraint featurer_pk do update set featurer_type_id = excluded.featurer_type_id, class = excluded.class, name = excluded.name, description = excluded.description;
-- create new column into twin_class
alter table twin_class
    add if not exists head_hunter_featurer_id integer
        constraint twin_class_featurer_id_fk
            references featurer
                on update cascade;
alter table twin_class add if not exists head_hunter_featurer_params hstore;
