INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3207, 32, 'org.twins.core.featurer.classfield.finder.FieldFinderByClassFromParam', 'fields by current twin class', '', false) on conflict do nothing;

update storage
set storager_params= storager_params || hstore('fileSizeLimit', '-1')
where id = '00000000-0000-0000-0013-000000000002';