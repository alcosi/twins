alter table twin_status_transition_trigger
    add column if not exists
    async boolean not null default true;

CREATE TABLE twin_status_transition_trigger_task_status if not exists
(
    id
    varchar
(
    40
) NOT NULL,
    CONSTRAINT twin_status_transition_trigger_task_status_pk PRIMARY KEY
(
    id
)
    );


insert into twin_status_transition_trigger_task_status
values ('NEED_START') on conflict do nothing;
insert into twin_status_transition_trigger_task_status
values ('IN_PROGRESS') on conflict do nothing;
insert into twin_status_transition_trigger_task_status
values ('DONE') on conflict do nothing;
insert into twin_status_transition_trigger_task_status
values ('FAILED') on conflict do nothing;


create table twin_status_transition_trigger_task if not exists
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
    twin_status_transition_trigger_id
    uuid
    NOT
    NULL,
    src_twin_status_id
    uuid
    NULL,
    dst_twin_status_id
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
    twin_status_transition_trigger_task_status_id
    varchar
(
    20
) NOT NULL,
    twin_status_transition_trigger_task_status_details varchar NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    done_at timestamp NULL,
    CONSTRAINT twin_status_transition_trigger_task_pk PRIMARY KEY
(
    id
),
    CONSTRAINT twin_status_transition_trigger_task_pk_2 UNIQUE
(
    twin_id,
    request_id
),
    CONSTRAINT twin_status_transition_trigger_task_business_account_id_fk FOREIGN KEY
(
    business_account_id
) REFERENCES business_account
(
    id
),
    CONSTRAINT twin_status_transition_trigger_task_created_by_user_id_fk FOREIGN KEY
(
    created_by_user_id
) REFERENCES "user"
(
    id
),
    CONSTRAINT twin_status_transition_trigger_task_status_id_fk FOREIGN KEY
(
    twin_status_transition_trigger_task_status_id
) REFERENCES twin_status_transition_trigger_task_status
(
    id
) ON DELETE RESTRICT
  ON UPDATE CASCADE,
    CONSTRAINT twin_status_transition_trigger_task_trigger_id_fk FOREIGN KEY
(
    twin_status_transition_trigger_id
) REFERENCES twin_status_transition_trigger
(
    id
)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
    CONSTRAINT twin_status_transition_trigger_twin_id_fk FOREIGN KEY
(
    twin_id
) REFERENCES twin
(
    id
),
    CONSTRAINT twin_status_transition_trigger_src_twin_status_id_fk FOREIGN KEY
(
    src_twin_status_id
) REFERENCES twin_status
(
    id
),
    CONSTRAINT twin_status_transition_trigger_dst_twin_status_id_fk FOREIGN KEY
(
    dst_twin_status_id
) REFERENCES twin_status
(
    id
)

    );

CREATE INDEX if not exists twin_status_transition_trigger_task_business_account_id_index ON twin_status_transition_trigger_task USING btree (business_account_id);
CREATE INDEX if not exists twin_status_transition_trigger_task_created_by_user_id_index ON twin_status_transition_trigger_task USING btree (created_by_user_id);
CREATE INDEX if not exists twin_status_transition_trigger_task_input_twin_id_index ON twin_status_transition_trigger_task USING btree (twin_id);
CREATE INDEX if not exists twin_status_transition_trigger_task_twin_status_id_index ON twin_status_transition_trigger_task USING btree (twin_status_transition_trigger_task_status_id);
CREATE UNIQUE INDEX if not exists twin_status_transition_trigger_task_twin_id_request_id_uindex ON twin_status_transition_trigger_task USING btree (twin_id, request_id);


