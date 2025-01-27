

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
    storage_featurer_id uuid      not null references public.featurer (id) on delete cascade on update cascade,
    storage_params      hstore    not null             default ''::hstore,
    description         varchar   not null,
    created_at          timestamp not null             default current_timestamp,
    updated_at          timestamp not null             default current_timestamp
);

create or replace trigger update_column_updated_at_trigger
    before update
    on public.resource_storage
    for each row
execute procedure public.update_column_updated_at_trigger();

insert into public.resource_storage(id, storage_featurer_id, storage_params, description)
values ('0194a1cd-fc94-7c0b-9884-e3d45d2bebf3', 2901,
        hstore(ARRAY [
            'selfHostDomainBaseUri', '/',
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
    created_at          timestamp not null             default current_timestamp
);
COMMENT ON COLUMN public.resource.storage_file_key IS 'Represents key/path to resource. Like local storage path, S3 key or external URI';

alter table public.domain
    add column icon_dark_resource_id uuid references public.resource on delete cascade on update cascade;

alter table public.domain
    add column icon_light_resource_id uuid references public.resource on delete cascade on update cascade;

alter table public.domain
    add column resources_storage_id uuid references public.resource_storage on delete cascade on update cascade;
COMMENT ON COLUMN public.domain.resources_storage_id IS 'Storage params that are used to create new resources';

alter table public.domain
    add column attachments_storage_id uuid references public.resource_storage on delete cascade on update cascade;
COMMENT ON COLUMN public.domain.attachments_storage_id IS 'Storage params that are used to create new attachments';
