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

create table if not exists twin_factory_task_status
(
    id varchar(20) not null
        constraint twin_factory_task_status_pk
            primary key
);

create table if not exists twin_factory_task
(
    id                          uuid
        constraint twin_factory_task_pk
            primary key,
    twin_factory_id             uuid                                not null
        constraint twin_factory_task_twin_factory_id_fk
            references twin_factory
            on update cascade on delete cascade,
    input_twin_id               uuid                                not null
        constraint twin_factory_task_twin_id_fk
            references twin,
    created_by_user_id                     uuid                                not null
        constraint twin_factory_task_created_by_user_id_fk
            references "user",
    business_account_id                     uuid                                not null
        constraint twin_factory_task_business_account_id_fk
            references business_account,
    twin_factory_task_status_id varchar(20)                         not null
        constraint twin_factory_task_twin_factory_task_status_id_fk
            references twin_factory_task_status
            on update cascade on delete restrict,
    twin_factory_task_status_details varchar,
    created_at                  timestamp default current_timestamp not null,
    done_at                     timestamp
);

create index if not exists twin_factory_task_input_twin_id_index
    on twin_factory_task (input_twin_id);

create index if not exists twin_factory_task_twin_factory_id_index
    on twin_factory_task (twin_factory_id);

create index if not exists twin_factory_task_created_by_user_id_index
    on twin_factory_task (created_by_user_id);

create index if not exists twin_factory_task_business_account_id_index
    on twin_factory_task (business_account_id);

create index if not exists twin_factory_task_twin_factory_task_status_id_index
    on twin_factory_task (twin_factory_task_status_id);




