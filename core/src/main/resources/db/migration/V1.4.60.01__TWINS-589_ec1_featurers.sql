update featurer
set name = 'StoragerAlcosiFileHandler'
where id=2906;

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (2907, 29, 'org.twins.core.featurer.storager.filehandler.StoragerAlcosiFileHandlerV2', 'StoragerAlcosiFileHandlerV2', 'Service to save or delete files via file-handler service (api with multipart)', false)
on conflict do nothing;
