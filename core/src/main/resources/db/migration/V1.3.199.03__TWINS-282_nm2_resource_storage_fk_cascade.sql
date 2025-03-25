alter table public.resource
    drop constraint resource_storage_id_fkey;

alter table public.resource
    add foreign key (storage_id) references public.storage
        on update cascade;


INSERT INTO public.storage (id, domain_id, storager_featurer_id, storager_params, description, created_at, updated_at) VALUES ('00000000-0000-0000-0007-000000000002', null, 2903, 'selfHostDomainBaseUri => /, supportedMimeTypes => "*/ico,*/icns,*/ico,*/svg,*/svg+xml,*/webp,*/png,*/gif,*/jpeg,*/jpg,*/jpeg-lossless", fileSizeLimit => 1000000', 'External url storage', '2025-02-07 10:02:47.844931', '2025-02-07 10:02:47.844931');
