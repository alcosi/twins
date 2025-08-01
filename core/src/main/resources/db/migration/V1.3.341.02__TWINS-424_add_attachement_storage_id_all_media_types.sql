update storage
set storager_params= storager_params || hstore('supportedMimeTypes', '*/*')
where id = '00000000-0000-0000-0013-000000000002';
