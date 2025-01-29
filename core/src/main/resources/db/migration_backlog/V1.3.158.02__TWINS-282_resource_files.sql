create table if not exists public.storage
(
    id                  uuid      not null primary key default gen_random_uuid(),
    domain_id           uuid references public.domain (id) on delete cascade on update cascade,
    storage_featurer_id bigint    not null references public.featurer (id) on delete cascade on update cascade,
    storage_params      hstore    not null             default ''::hstore,
    description         varchar   not null,
    created_at          timestamp not null             default current_timestamp,
    updated_at          timestamp not null             default current_timestamp
);

insert into public.storage(id, storage_featurer_id, storage_params, description)
values ('0194a1cd-fc94-7c0b-9884-e3d45d2bebf3', 2901,
        hstore(ARRAY [
            'selfHostDomainBaseUri', '/',
            'fileSizeLimit', '1000000',
            'supportedMimeTypes','*/ico,*/icns,*/ico,*/svg,*/svg+xml,*/webp,*/png,*/gif,*/jpeg,*/jpg,*/jpeg-lossless',
            'baseLocalPath','/opt/resource/',
            'relativeFileUri','public/resource/{id}/v1',
            'downloadExternalFileConnectionTimeout','60000'
            ]
        ),
        'Domain icon/logo local storage resource')
on conflict (id) do update set storage_params=excluded.storage_params;

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
    add column icon_dark_resource_id uuid references public.resource on update cascade;

alter table public.domain
    add column icon_light_resource_id uuid references public.resource on update cascade;

alter table public.domain
    add column resources_storage_id uuid references public.resource_storage on update cascade;
COMMENT ON COLUMN public.domain.resources_storage_id IS 'Storage params that are used to create new resources';

alter table public.domain
    add column attachments_storage_id uuid references public.resource_storage on update cascade;
COMMENT ON COLUMN public.domain.attachments_storage_id IS 'Storage params that are used to create new attachments';


update public.domain
set attachments_storage_id='0194a1cd-fc94-7c0b-9884-e3d45d2bebf3'::uuid
where attachments_storage_id is null;

update public.domain
set resources_storage_id='0194a1cd-fc94-7c0b-9884-e3d45d2bebf3'::uuid
where resources_storage_id is null;


