INSERT INTO public.featurer_type (id, name, description)
VALUES (29, 'StoragerFileService', 'Services for resource(file) uploading')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

--Local controller
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2901, 29, 'org.twins.core.featurer.resource.StoragerLocalControllerFileService',
        'StoragerLocalControllerFileService',
        'Service to save files in local file system and return them in controller after that')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;


--Local static resource
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2902, 29, 'org.twins.core.featurer.resource.StoragerLocalStaticFileService', 'StoragerLocalStaticFileService',
        'Service to save files in local file system and return them them as nginx static resource after that')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;


--External Uri
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2903, 29, 'org.twins.core.featurer.resource.StoragerExternalUriFileService',
        'StoragerExternalUriFileService',
        'Service to keep and work with external uri')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;


--S3
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2904, 29, 'org.twins.core.featurer.resource.StoragerS3FileControllerService', 'StoragerS3FileControllerService',
        'Service to save files to S3 and serve in controller')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;
