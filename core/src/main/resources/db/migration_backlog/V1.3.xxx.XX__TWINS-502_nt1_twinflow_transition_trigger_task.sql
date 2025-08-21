alter table twinflow_transition_trigger
    add column if not exists
    async boolean not null default true;

CREATE TABLE twinflow_transition_trigger_task_status if not exists
(
    id
    varchar
(
    40
) NOT NULL,
    CONSTRAINT twinflow_transition_trigger_task_status_pk PRIMARY KEY
(
    id
)
    );

insert into twinflow_transition_trigger_task_status
values ('NEED_START') on conflict do nothing;
insert into twinflow_transition_trigger_task_status
values ('IN_PROGRESS') on conflict do nothing;
insert into twinflow_transition_trigger_task_status
values ('DONE') on conflict do nothing;
insert into twinflow_transition_trigger_task_status
values ('FAILED') on conflict do nothing;


create table twinflow_transition_trigger_task if not exists
(
    id
    uuid
    NOT
    NULL,
    twin_id
    uuid
    NOT
    NULL,
    request_id
    uuid
    NOT
    NULL,
    twinflow_transition_trigger_id
    uuid
    NOT
    NULL,
    src_twin_status_id
    uuid
    NOT
    NULL,
    created_by_user_id
    uuid
    NOT
    NULL,
    business_account_id
    uuid
    NULL,
    twinflow_transition_trigger_task_status_id
    varchar
(
    20
) NOT NULL,
    twinflow_transition_trigger_task_status_details varchar NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    done_at timestamp NULL,
    CONSTRAINT twinflow_transition_trigger_task_pk PRIMARY KEY
(
    id
),
    CONSTRAINT twinflow_transition_trigger_task_pk_2 UNIQUE
(
    twin_id,
    request_id
),
    CONSTRAINT twinflow_transition_trigger_task_business_account_id_fk FOREIGN KEY
(
    business_account_id
) REFERENCES business_account
(
    id
),
    CONSTRAINT twinflow_transition_trigger_task_created_by_user_id_fk FOREIGN KEY
(
    created_by_user_id
) REFERENCES "user"
(
    id
),
    CONSTRAINT twinflow_transition_trigger_task_status_id_fk FOREIGN KEY
(
    twinflow_transition_trigger_task_status_id
) REFERENCES twinflow_transition_trigger_task_status
(
    id
) ON DELETE RESTRICT
  ON UPDATE CASCADE,
    CONSTRAINT twinflow_transition_trigger_task_trigger_id_fk FOREIGN KEY
(
    twinflow_transition_trigger_id
) REFERENCES twinflow_transition_trigger
(
    id
)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
    CONSTRAINT twinflow_transition_trigger_twin_id_fk FOREIGN KEY
(
    twin_id
) REFERENCES twin
(
    id
),
    CONSTRAINT twinflow_transition_trigger_src_twin_status_id_fk FOREIGN KEY
(
    src_twin_status_id
) REFERENCES twin_status
(
    id
)
    );

CREATE INDEX if not exists twinflow_transition_trigger_task_business_account_id_index ON twinflow_transition_trigger_task USING btree (business_account_id);
CREATE INDEX if not exists twinflow_transition_trigger_task_created_by_user_id_index ON twinflow_transition_trigger_task USING btree (created_by_user_id);
CREATE INDEX if not exists twinflow_transition_trigger_task_input_twin_id_index ON twinflow_transition_trigger_task USING btree (twin_id);
CREATE INDEX if not exists twinflow_transition_trigger_task_twin_status_id_index ON twinflow_transition_trigger_task USING btree (twinflow_transition_trigger_task_status_id);
CREATE UNIQUE INDEX if not exists twinflow_transition_trigger_task_twin_id_request_id_uindex ON twinflow_transition_trigger_task USING btree (twin_id, request_id);


