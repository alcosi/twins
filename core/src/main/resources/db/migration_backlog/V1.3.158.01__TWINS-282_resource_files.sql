INSERT INTO public.featurer_type (id, name, description)
VALUES (29, 'ResourceStoragerService', 'Services for resource(file) uploading')
on conflict (id) do nothing;

insert into public.featurer(id, featurer_type_id, class, name, description)
values (2901, 29, 'org.twins.core.featurer.resource.LocalStorageResourceService', 'LocalStorageResourceService',
        'Service to save resources (files) in local file system')
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

insert into public.featurer(id, featurer_type_id, class, name, description)
values (2902, 29, 'org.twins.core.featurer.resource.ExternalUriStorageResourceService',
        'ExternalUriStorageResourceService',
        'Service to keep and work with external uri')
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

insert into public.featurer(id, featurer_type_id, class, name, description)
values (2903, 29, 'org.twins.core.featurer.resource.S3StorageResourceService', 'S3StorageResourceService',
        'Service to save resources (files) to S3')
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
VALUES (2903, false, 4, 's3Uri', 's3Uri', 'Uri to work with s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 5, 's3Region', 's3Region', 'Region config for s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 6, 's3Bucket', 's3Bucket', 'Bucket of s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 7, 's3AccessKey', 's3AccessKey', 'Access key for s3', 'STRING')
on conflict (featurer_id,key) do nothing;

INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id)
VALUES (2903, false, 8, 's3SecretKey', 's3SecretKey', 'Secret key for s3', 'STRING')
on conflict (featurer_id,key) do nothing;

create or replace function public.update_column_updated_at_trigger() returns trigger
    language plpgsql
as
$$
BEGIN
    new.updated_at = current_timestamp;
    RETURN NEW;
END;
$$;

create table if not exists public.resource_storage
(
    id                  uuid      not null primary key default gen_random_uuid(),
    storage_featurer_id uuid      not null references public.featurer (id) on delete cascade on update cascade ,
    storage_params      hstore    not null             default ''::hstore,
    description         varchar   not null,
    added_at            timestamp not null             default current_timestamp,
    updated_at          timestamp not null             default current_timestamp
);

create or replace trigger update_column_updated_at_trigger
    before update
    on public.resource_storage
    for each row
execute procedure public.update_column_updated_at_trigger();

insert into public.resource_storage(id, storage_featurer_id, storage_params, description)
values ('0194a1cd-fc94-7c0b-9884-e3d45d2bebf3', 2901,
        hstore(ARRAY[
                'selfHostDomainBaseUri', 'http://127.0.0.1/test',
                'fileSizeLimit', '1000000',
                'supportedMimeTypes','*/ico,*/icns,*/ico,*/svg,*/svg+xml,*/webp,*/png,*/gif,*/jpeg,*/jpg,*/jpeg-lossless',
                'baseLocalPath','/opt/resources/']
        ),
        'Domain icon/logo local storage resource');

create table if not exists public.resource
(
    id                  uuid      not null primary key default gen_random_uuid(),
    domain_id           uuid references public.domain (id) on delete cascade on update cascade,
    uploaded_by_user_id uuid references public."user" (id),
    original_file_name  varchar   not null             default '',
    size_in_bytes       bigint    not null             default 0,
    storage_file_key    varchar   not null,
    storage_id          uuid      not null references public.resource_storage (id),
    added_at            timestamp not null             default current_timestamp
);

COMMENT ON COLUMN public.resource.storage_file_key IS 'Represents key/path to resource. Like local storage path, S3 key or external URI';

alter table public.domain
add column icon_dark_resource_id uuid references public.resource on delete cascade on update cascade;

alter table public.domain
    add column icon_light_resource_id uuid references public.resource on delete cascade on update cascade;

alter table public.domain
    add column config_resources_storage_id uuid references public.resource_storage on delete cascade on update cascade;

alter table public.domain
    add column config_attachments_storage_id uuid references public.resource_storage on delete cascade on update cascade;
