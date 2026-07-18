-- Universal trigger for parent status change when all children have the same status
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES
(1520, 15, '', '', '', false)
on conflict do nothing;
