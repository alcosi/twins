alter table public.twin_class add column if not exists create_permission_id uuid;
alter table public.twin_class add column if not exists edit_permission_id uuid;
alter table public.twin_class add column if not exists delete_permission_id uuid;

alter table public.twin_class drop constraint if exists fk_twinclass_create_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_edit_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_delete_permission_id;

ALTER TABLE public.twin_class
    ADD CONSTRAINT fk_twinclass_create_permission_id FOREIGN KEY (create_permission_id) REFERENCES permission (id),
    ADD CONSTRAINT fk_twinclass_delete_permission_id FOREIGN KEY (delete_permission_id) REFERENCES permission (id),
    ADD CONSTRAINT fk_twinclass_edit_permission_id FOREIGN KEY (edit_permission_id) REFERENCES permission (id);

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'twin_class_schema_map'
              AND column_name = 'create_permission_id'
        ) THEN
            UPDATE twin_class
            SET create_permission_id = tcm.create_permission_id
            FROM twin_class_schema_map tcm
                     JOIN twin_class_schema tcs ON tcs.id = tcm.twin_class_schema_id
            WHERE twin_class.id = tcm.twin_class_id and twin_class.domain_id = tcs.domain_id;

            ALTER TABLE public.twin_class_schema_map DROP COLUMN create_permission_id;
        END IF;
    END $$;


DROP FUNCTION IF EXISTS public.create_permission_id_detect(uuid, uuid, uuid, uuid);
DROP FUNCTION IF EXISTS public.twin_class_schema_id_detect(uuid, uuid, uuid);
DROP FUNCTION IF EXISTS public.twin_class_create_permission_id_detect(uuid);
DROP FUNCTION IF EXISTS public.twin_class_delete_permission_id_detect(uuid);
DROP FUNCTION IF EXISTS public.twin_class_edit_permission_id_detect(uuid);
