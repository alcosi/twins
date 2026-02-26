CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE OR REPLACE FUNCTION uuid_generate_v7_custom()
    RETURNS uuid
    LANGUAGE plpgsql
AS $$
DECLARE
unix_ms bigint;
    rand_bytes bytea;
    uuid_bytes bytea;
BEGIN
    -- 48-bit unix timestamp in milliseconds
    unix_ms := floor(extract(epoch from clock_timestamp()) * 1000);

    -- 10 random bytes (80 bits)
    rand_bytes := gen_random_bytes(10);

    -- Compose UUID (16 bytes total)
    uuid_bytes :=
            -- 6 bytes timestamp
        substring(int8send(unix_ms) from 3 for 6)
            ||
            -- version (4 bits) + first 4 bits random
        set_byte(substring(rand_bytes from 1 for 1), 0,
                 (get_byte(rand_bytes, 0) & 15) | 112)  -- 0111xxxx
            ||
            -- remaining 9 random bytes
        substring(rand_bytes from 2);

    -- Set variant (10xxxxxx)
    uuid_bytes :=
            set_byte(uuid_bytes, 8,
                     (get_byte(uuid_bytes, 8) & 63) | 128);

RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$;
 