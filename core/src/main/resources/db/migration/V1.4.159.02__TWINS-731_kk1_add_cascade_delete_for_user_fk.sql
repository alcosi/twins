-- Drop existing foreign key constraints on permission_grant_user.user_id
ALTER TABLE permission_grant_user
    DROP CONSTRAINT IF EXISTS permission_schema_user_user_id_fk;
ALTER TABLE permission_grant_user
    DROP CONSTRAINT IF EXISTS permission_grant_user_user_id_fk;

-- Re-add the constraint with ON DELETE CASCADE
ALTER TABLE permission_grant_user
    ADD CONSTRAINT permission_grant_user_user_id_fk
        FOREIGN KEY (user_id) REFERENCES public."user"(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;

-- Drop existing foreign key constraint on twin.assigner_user_id
ALTER TABLE twin
    DROP CONSTRAINT IF EXISTS twin_assigner_user_id_fk;

-- Re-add the constraint with ON DELETE SET NULL
ALTER TABLE twin
    DROP CONSTRAINT IF EXISTS twin_assigner_user_id_fk;
ALTER TABLE twin
    ADD CONSTRAINT twin_assigner_user_id_fk
        FOREIGN KEY (assigner_user_id) REFERENCES public."user"(id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;

-- Drop existing foreign key constraint on twin.created_by_user_id
ALTER TABLE twin
    DROP CONSTRAINT IF EXISTS twin_created_by_user_id_fk;
ALTER TABLE twin
    ADD CONSTRAINT twin_created_by_user_id_fk
        FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id)
            ON UPDATE CASCADE
            ON DELETE CASCADE;