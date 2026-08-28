INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1623, 16, '', '', '', false)
ON CONFLICT ON CONSTRAINT featurer_pk DO NOTHING;
