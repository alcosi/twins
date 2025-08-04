DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'twinflow'
                     AND column_name = 'on_update_twin_factory_id') THEN
            ALTER TABLE twinflow
                RENAME COLUMN on_update_twin_factory_id TO before_update_twin_factory_id;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'twinflow'
                     AND column_name = 'on_create_twin_factory_id') THEN
            ALTER TABLE twinflow
                RENAME COLUMN on_create_twin_factory_id TO before_create_twin_factory_id;
        END IF;
    END
$$;


ALTER TABLE twinflow
    ADD COLUMN IF NOT EXISTS before_sketch_twin_factory_id uuid
        REFERENCES twin_factory
            ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE twinflow
    ADD COLUMN IF NOT EXISTS after_create_twin_factory_id uuid
        REFERENCES twin_factory
            ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE twinflow
    ADD COLUMN IF NOT EXISTS after_update_twin_factory_id uuid
        REFERENCES twin_factory
            ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE twinflow
    ADD COLUMN IF NOT EXISTS after_sketch_twin_factory_id uuid
        REFERENCES twin_factory
            ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE twinflow
    DROP CONSTRAINT IF EXISTS twinflow_on_create_twin_factory_id_fk;

ALTER TABLE twinflow
    DROP CONSTRAINT IF EXISTS twinflow_on_update_twin_factory_id_fkey;


DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_name = 'twinflow_before_create_twin_factory_id_fk') THEN
            ALTER TABLE twinflow
                ADD CONSTRAINT twinflow_before_create_twin_factory_id_fk
                    FOREIGN KEY (before_create_twin_factory_id)
                        REFERENCES twin_factory
                        ON UPDATE CASCADE ON DELETE RESTRICT;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_name = 'twinflow_before_update_twin_factory_id_fk') THEN
            ALTER TABLE twinflow
                ADD CONSTRAINT twinflow_before_update_twin_factory_id_fk
                    FOREIGN KEY (before_update_twin_factory_id)
                        REFERENCES twin_factory
                        ON UPDATE CASCADE ON DELETE RESTRICT;
        END IF;
    END
$$;

create table if not exists twin_change_task_status
(
    id varchar(20) not null
        constraint twin_change_task_status_pk
            primary key
);

insert into twin_change_task_status values ('NEED_START') on conflict do nothing;
insert into twin_change_task_status values ('IN_PROGRESS') on conflict do nothing;
insert into twin_change_task_status values ('DONE') on conflict do nothing;
insert into twin_change_task_status values ('FAILED') on conflict do nothing;

create table if not exists twin_factory_launcher
(
    id varchar(20) not null
        constraint twin_factory_launcher_pk
            primary key
);

insert into twin_factory_launcher values ('transition') on conflict do nothing ;
insert into twin_factory_launcher values ('targetDeletion') on conflict do nothing ;
insert into twin_factory_launcher values ('cascadeDeletion') on conflict do nothing ;
insert into twin_factory_launcher values ('beforeTwinCreate') on conflict do nothing ;
insert into twin_factory_launcher values ('beforeTwinUpdate') on conflict do nothing ;
insert into twin_factory_launcher values ('beforeTwinSketch') on conflict do nothing ;
insert into twin_factory_launcher values ('afterTwinCreate') on conflict do nothing ;
insert into twin_factory_launcher values ('afterTwinUpdate') on conflict do nothing ;
insert into twin_factory_launcher values ('afterTwinSketch') on conflict do nothing ;

create table if not exists twin_change_task
(
    id                          uuid
        constraint twin_change_task_pk
            primary key,
    twin_id               uuid                                not null
        constraint twin_change_task_twin_id_fk
            references twin,
    request_id               uuid                                not null,
    twin_factory_id             uuid                                not null
        constraint twin_change_task_twin_factory_id_fk
            references twin_factory
            on update cascade on delete cascade,
    twin_factory_launcher_id      varchar(20)                         not null
        constraint twin_change_task_twin_factory_launcher_id_fk
            references twin_factory_launcher
            on update cascade on delete restrict,
    created_by_user_id              uuid                                not null
        constraint twin_change_task_created_by_user_id_fk
            references "user",
    business_account_id             uuid
        constraint twin_change_task_business_account_id_fk
            references business_account,
    twin_change_task_status_id      varchar(20)                         not null
        constraint twin_change_task_twin_change_task_status_id_fk
            references twin_change_task_status
            on update cascade on delete restrict,
    twin_change_task_status_details varchar,
    created_at                      timestamp default current_timestamp not null,
    done_at                         timestamp
);

create index if not exists twin_change_task_input_twin_id_index
    on twin_change_task (twin_id);

create index if not exists twin_change_task_twin_factory_id_index
    on twin_change_task (twin_factory_id);

create index if not exists twin_change_task_created_by_user_id_index
    on twin_change_task (created_by_user_id);

create index if not exists twin_change_task_business_account_id_index
    on twin_change_task (business_account_id);

create index if not exists twin_change_task_twin_change_task_status_id_index
    on twin_change_task (twin_change_task_status_id);

create unique index if not exists twin_change_task_twin_id_request_id_uindex
    on twin_change_task (twin_id, request_id);

alter table twin_change_task
    add constraint twin_change_task_pk_2
        unique (twin_id, request_id);


alter table twinflow_transition
    add if not exists after_perform_twin_factory_id uuid
        constraint twinflow_transition_after_perform_twin_factory_id_fk
            references public.twin_factory
            on update cascade on delete restrict;

create index if not exists twinflow_transition_after_perform_twin_factory_id_index
    on twinflow_transition (after_perform_twin_factory_id);



