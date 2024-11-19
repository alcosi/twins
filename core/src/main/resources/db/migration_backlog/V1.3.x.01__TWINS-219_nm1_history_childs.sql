create table if not exists history_type_config_twin_class_child
(
    id                        uuid    not null
        constraint history_type_config_twin_class_child_pk
            primary key,
    twin_class_id             uuid    not null
        constraint history_type_config_twin_class_child_id_fk
            references twin_class,
    child_twin_class_id             uuid    not null
        constraint history_type_config_twin_class_child_child_twin_class_id_fk
            references twin_class,
    child_history_type_id           varchar not null
        constraint history_type_config_twin_class_child_child_history_type_id_fk
            references history_type,
    history_type_status_id    varchar not null
        constraint history_type_config_twin_class_child_status_id_fk
            references history_type_status
            on update cascade,
    snapshot_message_template text,
    message_template_i18n_id  uuid
        constraint history_type_config_twin_class_child_i18n_id_fk
            references i18n
);
