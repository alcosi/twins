alter table twin_class
    drop column if exists inherited_marker_data_list_id,
    drop column if exists inherited_marker_data_list_twin_class_id,
    drop column if exists inherited_tag_data_list_id,
    drop column if exists inherited_tag_data_list_twin_class_id,
    add column if not exists inherited_marker_data_list_id            uuid references data_list on update cascade on delete restrict,
    add column if not exists inherited_marker_data_list_twin_class_id uuid references twin_class on update cascade on delete restrict,
    add column if not exists inherited_tag_data_list_id               uuid references data_list on update cascade on delete restrict,
    add column if not exists inherited_tag_data_list_twin_class_id    uuid references twin_class on update cascade on delete restrict;

create index if not exists twin_class_inherited_marker_data_list_id_index
    on twin_class (inherited_marker_data_list_id);

create index if not exists twin_class_inherited_marker_data_list_twin_class_id_index
    on twin_class (inherited_marker_data_list_twin_class_id);

create index if not exists twin_class_inherited_tag_data_list_id_index
    on twin_class (inherited_tag_data_list_id);

create index if not exists twin_class_inherited_tag_data_list_twin_class_id_index
    on twin_class (inherited_tag_data_list_twin_class_id);


select id, marker_data_list_id, tag_data_list_id
into temp table data
from twin_class
where marker_data_list_id is not null or tag_data_list_id is not null;

update twin_class
set marker_data_list_id = null, tag_data_list_id = null
where marker_data_list_id is not null or tag_data_list_id is not null;

update twin_class
set marker_data_list_id = data.marker_data_list_id, tag_data_list_id = data.tag_data_list_id
from data
where twin_class.id = data.id;

drop table if exists data;
