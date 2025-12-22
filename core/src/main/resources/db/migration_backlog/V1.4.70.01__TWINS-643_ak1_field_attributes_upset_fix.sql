alter table history
    add if not exists create_else_update boolean;

INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('attachmentCreateOnCreate', 'Attachment was added', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('fieldCreatedOnCreate', 'Field ''${field.name}'' was set with ''${toValue}''', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('linkCreatedOnCreate', 'Link ''${link.name}'' on ''${dstTwin.name}'' created', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
