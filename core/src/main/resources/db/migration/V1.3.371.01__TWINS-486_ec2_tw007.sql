insert into face_component (id, face_component_type_id, name, description)
values ('TW007', 'TWIDGET', 'Change twin class widget', null)
on conflict do nothing;
