-- add new field
alter table twinflow_transition
    add if not exists message_i18n_id uuid
        constraint twinflow_transition_i18n_id_fk
            references i18n
            on update cascade on delete cascade;


INSERT INTO i18n_type (id, name) VALUES ('twinflowTransitionMessage', 'Twinflow transition message');
