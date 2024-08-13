DROP FUNCTION IF EXISTS public.detect_twin_class_schema(UUID, UUID, UUID);
DROP FUNCTION IF EXISTS public.detect_create_permission_id(UUID, UUID, UUID, UUID);
CREATE OR REPLACE FUNCTION detect_create_permission_id(
    IN head_twin_uuid UUID,
    IN business_account_uuid UUID,
    IN domain_uuid UUID,
    IN twin_class_uuid UUID
)
    RETURNS UUID AS $$
DECLARE
    create_permission_id UUID;
BEGIN
    -- check if head_twin_id not NULL, try to find create permission in head-twin space class schema map
    IF head_twin_uuid IS NOT NULL THEN
        SELECT tcsm.create_permission_id INTO create_permission_id
        FROM twin_class_schema_map tcsm
            JOIN space ON space.twin_id = head_twin_uuid
        WHERE space.twin_class_schema_id = tcsm.twin_class_schema_id and  tcsm.twin_class_id = twin_class_uuid
        LIMIT 1;
    END IF;

    -- if not found by head_twin_id, check in domain_business_account
    IF create_permission_id IS NULL AND business_account_uuid IS NOT NULL AND domain_uuid IS NOT NULL THEN
        SELECT tcsm.create_permission_id INTO create_permission_id
        FROM twin_class_schema_map tcsm
                 JOIN domain_business_account dba
                     ON dba.domain_id = domain_uuid AND dba.business_account_id = business_account_uuid
        WHERE tcsm.id = dba.twin_class_schema_id and tcsm.twin_class_id = twin_class_uuid
        LIMIT 1;
    END IF;

    -- if chema not found in first two cases, find it in domain
    IF create_permission_id IS NULL THEN
        SELECT tcsm.create_permission_id INTO create_permission_id
        FROM twin_class_schema_map tcsm
        JOIN domain d on d.id = domain_uuid
        WHERE tcsm.id = d.twin_class_schema_id and tcsm.twin_class_id = twin_class_uuid
        LIMIT 1;
    END IF;

    -- return schema id
    RETURN create_permission_id;
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

