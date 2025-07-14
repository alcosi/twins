INSERT INTO public.storage (id, storager_featurer_id, storager_params, description)
VALUES ('00000000-0000-0000-0013-000000000001', 2905,
        'relativeFileUri => public/attachment/{id}/v1,s3Uri => http://127.0.0.1:9000, s3SecretKey => 12345678q!, s3AccessKey => name, s3Bucket => bucket',
        'Twin attachment  s3 storager')
on conflict do nothing;
INSERT INTO public.storage (id, storager_featurer_id, storager_params, description)
VALUES ('00000000-0000-0000-0013-000000000002', 2903,
        'relativeFileUri => public/attachment/{id}/v1, selfHostDomainBaseUri => /, supportedMimeTypes => "*/ico,*/icns,*/ico,*/svg,*/svg+xml,*/webp,*/png,*/gif,*/jpeg,*/jpg,*/jpeg-lossless", downloadExternalFileConnectionTimeout => 60000, fileSizeLimit => 1000000',
        'Twin attachment  external link storager')
on conflict do nothing;


alter table public.twin_attachment
    add column if not exists storage_id uuid references public.storage on update cascade default '00000000-0000-0000-0013-000000000002' not null;
COMMENT ON COLUMN public.twin_attachment.storage_id IS 'Storage params that are used to create new attachments';


update domain
set attachments_storage_id='00000000-0000-0000-0013-000000000001'
where true;