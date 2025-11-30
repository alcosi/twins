CREATE TABLE IF NOT EXISTS projection_type_group (
    id UUID PRIMARY KEY,
    domain_id UUID NOT NULL REFERENCES domain(id),
    key VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS projection_type (
    id UUID PRIMARY KEY,
    domain_id UUID NOT NULL REFERENCES domain(id),
    key VARCHAR NOT NULL,
    name VARCHAR,
    projection_type_group_id UUID NOT NULL REFERENCES projection_type_group(id),
    membership_twin_class_id UUID NOT NULL REFERENCES twin_class(id)
);

ALTER TABLE data_list_option_projection DROP COLUMN IF EXISTS data_list_projection_id;

ALTER TABLE data_list_option_projection ADD COLUMN IF NOT EXISTS projection_type_id UUID REFERENCES projection_type(id);

ALTER TABLE projection ADD COLUMN IF NOT EXISTS projection_type_id UUID REFERENCES projection_type(id);

DROP TABLE IF EXISTS data_list_projection;