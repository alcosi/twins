DROP FUNCTION IF EXISTS public.detect_twin_class_schema(UUID, UUID, UUID);
CREATE OR REPLACE FUNCTION detect_twin_class_schema(
    IN head_twin_uuid UUID,
    IN business_account_uuid UUID,
    IN domain_uuid UUID
)
    RETURNS UUID AS $$
DECLARE
    twin_class_schema_id UUID;
BEGIN
    -- check if head_twin_id not NULL, try to find t-class schema
    IF head_twin_uuid IS NOT NULL THEN
        SELECT tcs.id INTO twin_class_schema_id
        FROM twin_class_schema tcs
                 JOIN twin ON twin.id = head_twin_uuid
                 JOIN space ON space.twin_id = twin.id
        WHERE space.twin_class_schema_id = tcs.id
        LIMIT 1;
    END IF;

    -- if not found by head_twin_id, check in domain_business_account
    IF twin_class_schema_id IS NULL AND business_account_uuid IS NOT NULL AND domain_uuid IS NOT NULL THEN
        SELECT tcs.id INTO twin_class_schema_id
        FROM twin_class_schema tcs
                 JOIN domain_business_account dba
                     ON dba.domain_id = domain_uuid
                            AND dba.business_account_id = business_account_uuid
        WHERE tcs.id = dba.twin_class_schema_id
        LIMIT 1;
    END IF;

    -- if chema not found in first two cases, find it in domain
    IF twin_class_schema_id IS NULL THEN
        SELECT d.twin_class_schema_id INTO twin_class_schema_id
        FROM domain d WHERE d.id = domain_uuid LIMIT 1;
    END IF;

    -- return schema id
    RETURN twin_class_schema_id;
END;
$$ LANGUAGE plpgsql;


alter table public.twin_class_schema_map add if not exists create_permission_id uuid;

alter table public.twin_class_schema_map drop constraint if exists twin_class_schema_map_create_permission_id_fk;

ALTER TABLE ONLY public.twin_class_schema_map
    ADD CONSTRAINT twin_class_schema_map_create_permission_id_fk FOREIGN KEY (create_permission_id) REFERENCES public.permission(id);

create index if not exists twin_class_schema_map_create_permission_id_index on public.twin_class_schema_map (create_permission_id);
