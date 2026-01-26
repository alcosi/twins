insert into featurer(id, featurer_type_id, class, name, description)
values (3904, 39, '', '', '')
on conflict (id) do update set name=excluded.name,
                               description=excluded.description;
