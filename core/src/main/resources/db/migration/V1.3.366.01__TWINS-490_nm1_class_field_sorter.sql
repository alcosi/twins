INSERT INTO featurer_type (id, name, description)
VALUES (40, 'FieldSorter', 'Twin class field sorter')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

insert into featurer(id, featurer_type_id, class, name, description)
values (4001, 40, '', '', '')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

--Local controller
insert into featurer(id, featurer_type_id, class, name, description)
values (4002, 40, '', '', '')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

alter table twin_class_field_search
    add if not exists force_sorting boolean not null default false;

alter table twin_class_field_search
    add if not exists field_sorter_featurer_id integer default 4001 not null
        constraint twin_class_field_search_featurer_id_fk
            references featurer;

alter table twin_class_field_search
    add if not exists field_sorter_params hstore;

create index if not exists twin_class_field_search_field_sorter_featurer_id_index
    on twin_class_field_search (field_sorter_featurer_id);