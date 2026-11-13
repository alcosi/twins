-- Add sorter block to twin_class_search preset (align with twin_class_field_search / user_search / data_list_option_search)

INSERT INTO featurer_type (id, name, description)
VALUES (58, 'ClassSorter', 'Order classes search')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (5801, 58, '', 'Unsorted', '', false)
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

alter table twin_class_search
    add if not exists force_sorting boolean not null default false;

alter table twin_class_search
    add if not exists class_sorter_featurer_id integer default 5801 not null
        constraint twin_class_search_class_sorter_featurer_id_fk
            references featurer;

alter table twin_class_search
    add if not exists class_sorter_params hstore;

create index if not exists twin_class_search_class_sorter_featurer_id_index
    on twin_class_search (class_sorter_featurer_id);
