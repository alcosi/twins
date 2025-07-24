create table if not exists public.twin_class_search
(
    id                  uuid      not null primary key,
    domain_id           uuid references public.domain (id) on delete cascade on update cascade,
    name         varchar   not null
    );

create table if not exists public.twin_class_search_predicate
(
    id                  uuid      not null primary key,
    twin_class_search_id           uuid references public.twin_class_search (id) on delete cascade on update cascade,
    class_finder_featurer_id int4    not null references public.featurer (id) on delete cascade on update cascade,
    class_finder_params      hstore    not null             default ''::hstore
    );

create index if not exists twin_class_search_predicate_index1
    on twin_class_search_predicate (class_finder_featurer_id);

create index if not exists twin_class_search_predicate_index2
    on twin_class_search_predicate (twin_class_search_id);


INSERT INTO public.featurer_type VALUES (39, 'ClassFinder', 'Finds classes according to given configuration') on conflict do nothing ;

INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3901, 39, 'org.twins.core.featurer.classfinder.ClassFinderGiven', 'Given twin class id and extends depth', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3102, 39, 'org.twins.core.featurer.classfinder.ClassFinderGivenSet', 'Given twin class id set', '', false) on conflict (id) do nothing;

INSERT INTO public.featurer_param VALUES (3901, false, 1, 'twinClassId', 'twinClassId', '', 'UUID:TWINS:TWIN_CLASS_ID',false,'','');
INSERT INTO public.featurer_param VALUES (3901, false, 2, 'extendsDepth', 'extendsDepth', '', 'INT',false,'','');
INSERT INTO public.featurer_param VALUES (3902, false, 1, 'twinClassIdSet', 'twinClassIdSet', '', 'UUID_SET:TWINS:TWIN_CLASS_ID',false,'','');
