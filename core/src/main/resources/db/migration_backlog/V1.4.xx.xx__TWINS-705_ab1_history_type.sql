-- Add history type for space role user added on create
INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('spaceRoleUserAddedOnCreate', 'Users was added', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
