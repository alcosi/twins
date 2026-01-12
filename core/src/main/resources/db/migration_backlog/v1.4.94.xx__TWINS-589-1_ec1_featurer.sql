-- for the first version of fh storager
update featurer
set deprecated = true
where id=2906;

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (2908, 29, 'org.twins.core.featurer.storager.filehandler.StoragerAlcosiFileHandlerV3', 'StoragerAlcosiFileHandlerV3', 'Service to save or delete files via file-handler service (async api with multipart)', false)
on conflict do nothing;
