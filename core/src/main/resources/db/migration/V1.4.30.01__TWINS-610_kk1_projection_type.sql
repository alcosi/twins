CREATE TABLE IF NOT EXISTS projection_type_group (
    id UUID PRIMARY KEY,
    domain_id UUID NOT NULL REFERENCES domain(id) ON DELETE CASCADE ON UPDATE CASCADE,
    key VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS projection_type (
    id UUID PRIMARY KEY,
    domain_id UUID NOT NULL REFERENCES domain(id) ON DELETE CASCADE ON UPDATE CASCADE,
    key VARCHAR NOT NULL,
    name VARCHAR,
    projection_type_group_id UUID NOT NULL REFERENCES projection_type_group(id) ON DELETE CASCADE ON UPDATE CASCADE,
    membership_twin_class_id UUID NOT NULL REFERENCES twin_class(id) ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE data_list_option_projection DROP COLUMN IF EXISTS data_list_projection_id;

ALTER TABLE data_list_option_projection ADD COLUMN IF NOT EXISTS projection_type_id UUID REFERENCES projection_type(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE projection ADD COLUMN IF NOT EXISTS projection_type_id UUID REFERENCES projection_type(id) ON DELETE CASCADE ON UPDATE CASCADE;

DROP TABLE IF EXISTS data_list_projection;