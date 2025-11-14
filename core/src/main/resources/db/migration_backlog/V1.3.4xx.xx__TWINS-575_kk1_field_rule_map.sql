CREATE TABLE IF NOT EXISTS twin_class_field_rule_map (
     id UUID PRIMARY KEY,
     twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id) ON DELETE CASCADE ON UPDATE CASCADE,
     twin_class_field_rule_id UUID NOT NULL REFERENCES twin_class_field_rule(id) ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE twin_class_field_rule DROP COLUMN IF EXISTS twin_class_field_id;

CREATE TABLE IF NOT EXISTS logic_operator
(
    id  varchar(10) PRIMARY KEY
);

insert into logic_operator (id) values ('AND'), ('OR'), ('LEAF') on conflict do nothing;

ALTER TABLE twin_class_field_condition
    ADD COLUMN IF NOT EXISTS parent_twin_class_field_condition_id UUID REFERENCES twin_class_field_condition(id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD COLUMN IF NOT EXISTS logic_operator_id VARCHAR(10) NOT NULL REFERENCES logic_operator(id) ON DELETE CASCADE ON UPDATE CASCADE DEFAULT 'LEAF',
    DROP COLUMN IF EXISTS group_no;