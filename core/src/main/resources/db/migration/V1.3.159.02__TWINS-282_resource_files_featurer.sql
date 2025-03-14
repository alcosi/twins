INSERT INTO public.featurer_type (id, name, description)
VALUES (29, 'Storager', 'Services for resource(file) uploading')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

--Local controller
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2901, 29, 'org.twins.core.featurer.storager.StoragerLocalStaticController',
        'StoragerLocalStaticController',
        'Service to save files in local file system and return their URL like as ''$selfHostDomainBaseUri''+''public/resource/{id}/v1''')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;


insert into public.featurer(id, featurer_type_id, class, name, description)
values (2902, 29, 'org.twins.core.featurer.storager.StoragerLocalDynamicController',
        'StoragerLocalDynamicController',
        'Service to save files in local file system and return their URL as ''$selfHostDomainBaseUri''+''$relativeFileUri''')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;



--External Uri
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2903, 29, 'org.twins.core.featurer.storager.StoragerExternalUri',
        'StoragerExternalUri',
        'Service to keep and work with external uri')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;


--S3
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2904, 29, 'org.twins.core.featurer.storager.StoragerS3StaticController', 'StoragerS3StaticController',
        'Service to save files to S3 and return their URL as ''$selfHostDomainBaseUri''+''public/resource/{id}/v1''')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

insert into public.featurer(id, featurer_type_id, class, name, description)
values (2905, 29, 'org.twins.core.featurer.storager.StoragerS3StaticController', 'StoragerS3StaticController',
        'Service to save files to S3 and return their URL as ''$selfHostDomainBaseUri''+''$relativeFileUri''')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;


