create table if not exists public.storage
(
    id                  uuid      not null primary key default gen_random_uuid(),
    domain_id           uuid references public.domain (id) on delete cascade on update cascade,
    storager_featurer_id bigint    not null references public.featurer (id) on delete cascade on update cascade,
    storager_params      hstore    not null             default ''::hstore,
    description         varchar   not null,
    created_at          timestamp not null             default current_timestamp,
    updated_at          timestamp not null             default current_timestamp
);



create table if not exists public.resource
(
    id                  uuid      not null primary key default gen_random_uuid(),
    domain_id           uuid references public.domain (id) on delete cascade on update cascade,
    storage_id          uuid      not null references public.storage (id),
    storage_file_key    varchar   not null,
    original_file_name  varchar   not null             default '',
    uploaded_by_user_id uuid references public."user" (id),
    size_in_bytes       bigint    not null             default 0,
    created_at          timestamp not null             default current_timestamp
);
COMMENT ON COLUMN public.resource.storage_file_key IS 'Represents key/path to resource. Like local storage path, S3 key or external URI';

alter table public.domain
    add column if not exists icon_dark_resource_id uuid references public.resource on update cascade;

alter table public.domain
    add column if not exists icon_light_resource_id uuid references public.resource on update cascade;

alter table public.domain
    add column if not exists resources_storage_id uuid references public.storage on update cascade;
COMMENT ON COLUMN public.domain.resources_storage_id IS 'Storage params that are used to create new resources';

alter table public.domain
    add column if not exists attachments_storage_id uuid references public.storage on update cascade;
COMMENT ON COLUMN public.domain.attachments_storage_id IS 'Storage params that are used to create new attachments';





insert into public.storage(id, storager_featurer_id, storager_params, description)
values ('00000000-0000-0000-0007-000000000001', 2901,
        hstore(ARRAY [
            'selfHostDomainBaseUri', '/',
            'fileSizeLimit', '1000000',
            'supportedMimeTypes','*/ico,*/icns,*/ico,*/svg,*/svg+xml,*/webp,*/png,*/gif,*/jpeg,*/jpg,*/jpeg-lossless',
            'baseLocalPath','/opt/resource/{domainId}/{businessAccountId}',
            'downloadExternalFileConnectionTimeout','60000'
            ]
        ),
        'Domain icon/logo local storage resource')
on conflict (id) do update set storager_params=excluded.storager_params;

update public.domain
set attachments_storage_id='00000000-0000-0000-0007-000000000001'::uuid
where attachments_storage_id is null;

update public.domain
set resources_storage_id='00000000-0000-0000-0007-000000000001'::uuid
where resources_storage_id is null;
