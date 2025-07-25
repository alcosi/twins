update twins_ons_db.public.storage
set storager_params= storager_params || hstore('supportedMimeTypes', '*/*')
where id = '00000000-0000-0000-0013-000000000002';