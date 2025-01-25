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
    storage_featurer_id uuid      not null references public.featurer (id),
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

create table if not exists public.resource
(
    id                  uuid        not null primary key default gen_random_uuid(),
    domain_id           uuid references public.domain (id),
    business_account_id uuid references public.business_account (id),
    uploaded_by_user_id uuid references public."user" (id),
    original_file_name  varchar     not null             default '',
    size_in_bytes       bigint      not null             default 0,
    md5_hash            varchar(32) not null,
    storage_file_key    varchar     not null,
    storage_id          uuid        not null references public.resource_storage (id),
    added_at            timestamp   not null             default current_timestamp
);
COMMENT ON COLUMN public.resource.storage_file_key IS 'Represents key/path to resource. Like local storage path, S3 key or external URI';


--domain table

-- new icon_dark_resource_id - FK to resource
-- table
--     new
-- icon_light_resource_id - FK to resource
-- table
--     new
-- resources_storage_id - FK to storage
-- table
--     new
-- attachments_storage_id - FK to storage
-- table