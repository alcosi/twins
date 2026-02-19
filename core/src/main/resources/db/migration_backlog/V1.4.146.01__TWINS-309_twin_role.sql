ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_creator BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_assignee BOOLEAN DEFAULT FALSE;
ALTER TABLE permission_grant_twin_role ADD COLUMN IF NOT EXISTS granted_to_space_creator BOOLEAN DEFAULT FALSE;

UPDATE permission_grant_twin_role SET granted_to_assignee = TRUE WHERE twin_role_id = 'assignee';
UPDATE permission_grant_twin_role SET granted_to_creator = TRUE WHERE twin_role_id = 'creator';
UPDATE permission_grant_twin_role SET granted_to_space_assignee = TRUE WHERE twin_role_id = 'space_assignee';
UPDATE permission_grant_twin_role SET granted_to_space_creator = TRUE WHERE twin_role_id = 'space_creator';

ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_creator SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_assignee SET NOT NULL;
ALTER TABLE permission_grant_twin_role ALTER COLUMN granted_to_space_creator SET NOT NULL;

ALTER TABLE permission_grant_twin_role DROP COLUMN IF EXISTS twin_role_id;

drop index if exists idx_permission_schema_twin_role_twinclass_schema_and_perm_id;
create unique index idx_permission_schema_twin_role_twinclass_schema_and_perm_id
    on permission_grant_twin_role (twin_class_id, permission_schema_id, permission_id);



