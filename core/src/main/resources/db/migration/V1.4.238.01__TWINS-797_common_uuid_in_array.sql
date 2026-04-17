CREATE OR REPLACE FUNCTION uuid_in_array(uuid_val uuid, uuid_array uuid[]) RETURNS boolean AS
$$
SELECT uuid_val = ANY (uuid_array);
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE FUNCTION uuid_in_array(uuid_val uuid, uuid_array text) RETURNS boolean AS
$$
SELECT uuid_val = ANY (uuid_array::uuid[]);
$$ LANGUAGE sql IMMUTABLE;