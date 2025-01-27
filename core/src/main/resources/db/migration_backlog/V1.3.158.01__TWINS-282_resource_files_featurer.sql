INSERT INTO public.featurer_type (id, name, description)
VALUES (29, 'ResourceStoragerService', 'Services for resource(file) uploading')
on conflict (id) do nothing;
--Local controller
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2901, 29, 'org.twins.core.featurer.resource.LocalStorageControllerFileService',
        'LocalStorageControllerFileService',
        'Service to save files in local file system and return them in controller after that')
on conflict (id) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2901, false, 1, 'selfHostDomainBaseUri', 'selfHostDomainBaseUri',
        'external URI/domain of twins application to create resource links', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2901, false, 2, 'fileSizeLimit', 'fileSizeLimit', 'Limit of file size', 'INT')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2901, false, 3, 'supportedMimeTypes', 'supportedMimeTypes', 'List of supported mime types', 'WORD_LIST')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2901, false, 4, 'baseLocalPath', 'baseLocalPath', 'Base local path of directory where to save files', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2901, false, 5, 'relativeFileUri', 'relativeFileUri', 'Relative uri of controller to provide files', 'STRING')
on conflict (featurer_id,key) do nothing;
--Local static resource
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2902, 29, 'org.twins.core.featurer.resource.LocalStorageStaticFileService', 'LocalStorageStaticFileService',
        'Service to save files in local file system and return them them as nginx static resource after that')
on conflict (id) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2902, false, 1, 'selfHostDomainBaseUri', 'selfHostDomainBaseUri',
        'external URI/domain of twins application to create resource links', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2902, false, 2, 'fileSizeLimit', 'fileSizeLimit', 'Limit of file size', 'INT')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2902, false, 3, 'supportedMimeTypes', 'supportedMimeTypes', 'List of supported mime types', 'WORD_LIST')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2902, false, 4, 'baseLocalPath', 'baseLocalPath', 'Base local path of directory where to save files', 'STRING')
on conflict (featurer_id,key) do nothing;
--External Uri
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2903, 29, 'org.twins.core.featurer.resource.ExternalUriStorageFileService',
        'ExternalUriStorageFileService',
        'Service to keep and work with external uri')
on conflict (id) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 1, 'selfHostDomainBaseUri', 'selfHostDomainBaseUri',
        'external URI/domain of twins application to create resource links', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 2, 'fileSizeLimit', 'fileSizeLimit', 'Limit of file size', 'INT')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 3, 'supportedMimeTypes', 'supportedMimeTypes', 'List of supported mime types', 'WORD_LIST')
on conflict (featurer_id,key) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 4, 'connectionTimeout', 'connectionTimeout', 'Connection timeout when getting file', 'INT')
on conflict (featurer_id,key) do nothing;
--S3
insert into public.featurer(id, featurer_type_id, class, name, description)
values (2904, 29, 'org.twins.core.featurer.resource.S3StorageFileControllerService', 'S3StorageFileControllerService',
        'Service to save files to S3 and serve in controller')
on conflict (id) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 1, 'selfHostDomainBaseUri', 'selfHostDomainBaseUri',
        'external URI/domain of twins application to create resource links', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 2, 'fileSizeLimit', 'fileSizeLimit', 'Limit of file size', 'INT')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 3, 'supportedMimeTypes', 'supportedMimeTypes', 'List of supported mime types', 'WORD_LIST')
on conflict (featurer_id,key) do nothing;


INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 4, 's3Uri', 's3Uri', 'Uri to work with s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 5, 's3Region', 's3Region', 'Region config for s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 6, 's3Bucket', 's3Bucket', 'Bucket of s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 7, 's3AccessKey', 's3AccessKey', 'Access key for s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 8, 's3SecretKey', 's3SecretKey', 'Secret key for s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 9, 'basePath', 'basePath', 'Base path of directory(key) where to save files', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2904, false, 10, 'relativeFileUri', 'relativeFileUri', 'Relative uri of controller to provide files', 'STRING')
on conflict (featurer_id,key) do nothing;