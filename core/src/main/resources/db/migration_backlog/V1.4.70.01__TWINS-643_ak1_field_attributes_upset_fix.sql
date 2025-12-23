INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('attachmentCreateOnCreate', 'Attachment was added', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('fieldCreatedOnCreate', 'Field ''${field.name}'' was set with ''${toValue}''', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
INSERT INTO history_type (id, snapshot_message_template, history_type_status_id) VALUES ('linkCreatedOnCreate', 'Link ''${link.name}'' on ''${dstTwin.name}'' created', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;

insert into featurer(id, featurer_type_id, class, name, description) values (4907, 49, '', '', '') on conflict (id) do nothing;
