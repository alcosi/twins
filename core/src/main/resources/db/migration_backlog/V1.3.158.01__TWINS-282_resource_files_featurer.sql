INSERT INTO public.featurer_type (id, name, description)
VALUES (29, 'Storager', 'Services for resource(file) uploading')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;

--Local controller
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2901, 29, 'org.twins.core.featurer.storager.StoragerLocal',
        'StoragerLocal',
        'Service to save files in local file system and return them in controller after that')
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
values (2904, 29, 'org.twins.core.featurer.storager.StoragerS3', 'StoragerS3',
        'Service to save files to S3 and serve in controller')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;
