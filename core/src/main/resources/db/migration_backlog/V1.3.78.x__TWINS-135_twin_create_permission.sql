DROP FUNCTION IF EXISTS public.detect_twin_class_schema(UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.detect_twin_class_schema_id(UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.detect_create_permission_id(UUID, UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.detect_create_permission_id(UUID, UUID, UUID);

CREATE OR REPLACE FUNCTION detect_twin_class_schema_id(
    IN head_twin_uuid UUID,
    IN business_account_uuid UUID,
    IN domain_uuid UUID
)
    RETURNS UUID AS $$
DECLARE
    twin_class_schema_uuid UUID;
BEGIN
    -- check if head_twin_id not NULL, try to find twin_class_schema_id in head-twin space
    IF head_twin_uuid IS NOT NULL THEN
        SELECT twin_class_schema_id into twin_class_schema_uuid from space where twin_id = head_twin_uuid
        LIMIT 1;
    END IF;

    -- if schema_id not found by head_twin_id, check in domain_business_account
    IF twin_class_schema_uuid IS NULL AND business_account_uuid IS NOT NULL AND domain_uuid IS NOT NULL THEN
        SELECT twin_class_schema_id into twin_class_schema_uuid
        FROM domain_business_account
        where domain_id = domain_uuid
        AND business_account_id = business_account_uuid
        LIMIT 1;
    END IF;

    -- if schema not found in first two cases, find it in domain
    IF twin_class_schema_uuid IS NULL THEN
        SELECT d.twin_class_schema_id INTO twin_class_schema_uuid
        FROM domain d WHERE d.id = domain_uuid LIMIT 1;
    END IF;

    -- return create permission id
    RETURN twin_class_schema_uuid;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


CREATE OR REPLACE FUNCTION detect_create_permission_id(
    IN head_twin_uuid UUID,
    IN business_account_uuid UUID,
    IN domain_uuid UUID,
    IN twin_class_uuid UUID
)
    RETURNS UUID AS $$
DECLARE
    twin_class_schema_uuid UUID;
    create_permission_uuid UUID;
BEGIN
    twin_class_schema_uuid := detect_twin_class_schema_id(head_twin_uuid, business_account_uuid, domain_uuid);

    IF twin_class_schema_uuid IS NOT NULL THEN
        SELECT tcsm.create_permission_id INTO create_permission_uuid
        FROM twin_class_schema_map tcsm
        WHERE tcsm.twin_class_schema_id = twin_class_schema_uuid and tcsm.twin_class_id = twin_class_uuid
        LIMIT 1;
    END IF;

    -- return create permission id
    RETURN create_permission_uuid;
END;
$$ LANGUAGE plpgsql IMMUTABLE;



alter table public.twin_class_schema_map add if not exists create_permission_id uuid;
alter table public.twin_class_schema_map drop constraint if exists twin_class_schema_map_create_permission_id_fk;

ALTER TABLE ONLY public.twin_class_schema_map ADD CONSTRAINT twin_class_schema_map_create_permission_id_fk
    FOREIGN KEY (create_permission_id) REFERENCES public.permission(id) on update no action on delete no action;

create index if not exists twin_class_schema_map_create_permission_id_index on public.twin_class_schema_map (create_permission_id);

alter table public.twin_class_schema_map drop constraint twin_class_schema_map_twin_class_id_fk;
alter table public.twin_class_schema_map add constraint twin_class_schema_map_twin_class_id_fk foreign key (twin_class_id) references public.twin_class on update no action on delete no action;
alter table public.twin_class_schema_map drop constraint twin_class_schema_map_twin_class_schema_id_fk;
alter table public.twin_class_schema_map add constraint twin_class_schema_map_twin_class_schema_id_fk foreign key (twin_class_schema_id) references public.twin_class_schema on update no action on delete no action;

