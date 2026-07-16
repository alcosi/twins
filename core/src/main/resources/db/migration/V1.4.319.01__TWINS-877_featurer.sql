alter table public.featurer_param_type
    alter column regexp type varchar(1024) using regexp;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2363::integer, 23::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2364::integer, 23::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2365::integer, 23::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
