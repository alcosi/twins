INSERT INTO history_type (id, snapshot_message_template, history_type_status_id)
VALUES ('assigneeAssigned', 'Assignee was assigned to ''${toUser.name}''', 'softEnabled')
ON CONFLICT ON CONSTRAINT history_type_pkey DO NOTHING;
