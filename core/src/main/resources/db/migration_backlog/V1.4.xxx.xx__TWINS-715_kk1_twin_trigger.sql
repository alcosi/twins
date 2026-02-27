CREATE TABLE IF NOT EXISTS twin_trigger (
     id                       uuid PRIMARY KEY,
     domain_id                uuid    NOT NULL
     REFERENCES domain
     ON UPDATE CASCADE
     ON DELETE CASCADE,
     twin_trigger_featurer_id integer NOT NULL
     REFERENCES featurer
     ON UPDATE CASCADE
     ON DELETE CASCADE,
     twin_trigger_param       hstore,
     active                   boolean DEFAULT true,
     name                     varchar,
     description              varchar
);

CREATE INDEX IF NOT EXISTS twin_trigger_domain_id_index
    ON twin_trigger (domain_id);

CREATE INDEX IF NOT EXISTS twin_trigger_twin_trigger_featurer_id_index
    ON twin_trigger (twin_trigger_featurer_id);

CREATE TABLE IF NOT EXISTS twin_trigger_task_status
(
    id VARCHAR(20) NOT NULL PRIMARY KEY
);

INSERT INTO twin_trigger_task_status
VALUES ('NEED_START')
ON CONFLICT (id) DO NOTHING;

INSERT INTO twin_trigger_task_status
VALUES ('IN_PROGRESS')
ON CONFLICT (id) DO NOTHING;

INSERT INTO twin_trigger_task_status
VALUES ('DONE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO twin_trigger_task_status
VALUES ('FAILED')
ON CONFLICT (id) DO NOTHING;


CREATE TABLE IF NOT EXISTS twin_trigger_task (
    id                       uuid PRIMARY KEY,
    twin_id                  uuid        NOT NULL
    REFERENCES twin
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    twin_trigger_id          uuid        NOT NULL
    REFERENCES twin_trigger
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
    previous_twin_status_id  uuid        REFERENCES twin_status
    ON UPDATE CASCADE
    ON DELETE SET NULL,
    created_by_user_id       uuid        NOT NULL
    REFERENCES "user"
    ON UPDATE CASCADE
    ON DELETE SET NULL,
    business_account_id      uuid        REFERENCES business_account
    ON UPDATE CASCADE
    ON DELETE SET NULL,
    twin_trigger_task_status_id VARCHAR(20) NOT NULL
    REFERENCES twin_trigger_task_status
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
    status_details           text,
    created_at               timestamp   DEFAULT CURRENT_TIMESTAMP,
    done_at                  timestamp
    );

CREATE INDEX IF NOT EXISTS twin_trigger_task_twin_id_index
    ON twin_trigger_task (twin_id);

CREATE INDEX IF NOT EXISTS twin_trigger_task_twin_trigger_id_index
    ON twin_trigger_task (twin_trigger_id);

CREATE INDEX IF NOT EXISTS twin_trigger_task_status_id_index
    ON twin_trigger_task (twin_trigger_task_status_id);

CREATE INDEX IF NOT EXISTS twin_trigger_task_created_by_user_id_index
    ON twin_trigger_task (created_by_user_id);

CREATE TABLE IF NOT EXISTS twin_factory_trigger (
    id                           uuid    PRIMARY KEY,
    twin_factory_id              uuid    NOT NULL
    REFERENCES twin_factory
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    input_twin_class_id          uuid    NOT NULL
    REFERENCES twin_class
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    twin_factory_condition_set_id uuid   REFERENCES twin_factory_condition_set
    ON UPDATE CASCADE
    ON DELETE SET NULL,
    twin_factory_condition_invert boolean DEFAULT false,
    active                       boolean DEFAULT true,
    description                  varchar,
    twin_trigger_id              uuid    NOT NULL
    REFERENCES twin_trigger
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
    async                        boolean DEFAULT false
);

CREATE INDEX IF NOT EXISTS twin_factory_trigger_twin_factory_id_index
    ON twin_factory_trigger (twin_factory_id);

CREATE INDEX IF NOT EXISTS twin_factory_trigger_input_twin_class_id_index
    ON twin_factory_trigger (input_twin_class_id);

CREATE INDEX IF NOT EXISTS twin_factory_trigger_twin_factory_condition_set_id_index
    ON twin_factory_trigger (twin_factory_condition_set_id);

CREATE INDEX IF NOT EXISTS twin_factory_trigger_twin_trigger_id_index
    ON twin_factory_trigger (twin_trigger_id);

ALTER TABLE twinflow_transition_trigger
    DROP CONSTRAINT IF EXISTS twinflow_transition_trigger_featurer_id_fk;

ALTER TABLE twinflow_transition_trigger
    ADD COLUMN IF NOT EXISTS async boolean DEFAULT false;

ALTER TABLE twinflow_transition_trigger
    ADD COLUMN IF NOT EXISTS twin_trigger_id uuid
    REFERENCES twin_trigger
    ON UPDATE CASCADE
    ON DELETE CASCADE;

ALTER TABLE twinflow_transition_trigger
    DROP COLUMN IF EXISTS transition_trigger_featurer_id;

ALTER TABLE twinflow_transition_trigger
    DROP COLUMN IF EXISTS transition_trigger_params;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (5010::integer, 50::integer, '', '', '', DEFAULT) on conflict do nothing;

INSERT INTO scheduler (id, domain_id, scheduler_featurer_id, cron, active, log_enabled, scheduler_params, fixed_rate, description, created_at, updated_at)
VALUES ('00000000-0000-0000-0015-00000000000a'::uuid, NULL, 5010, NULL, true, true, NULL, 2000, 'Scheduler for executing twin triggers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Step 1: Add new columns
ALTER TABLE public.twin_status_transition_trigger
    ADD COLUMN IF NOT EXISTS twin_trigger_id uuid,
    ADD COLUMN IF NOT EXISTS async boolean DEFAULT false NOT NULL;

-- Step 2: Create foreign key constraint to twin_trigger table
ALTER TABLE public.twin_status_transition_trigger
    ADD CONSTRAINT twin_status_transition_trigger_twin_trigger_id_fk
        FOREIGN KEY (twin_trigger_id) REFERENCES public.twin_trigger(id) ON UPDATE CASCADE;

-- Step 3: Create index for the new twin_trigger_id column
CREATE INDEX IF NOT EXISTS twin_status_transition_trigger_twin_trigger_id_i
    ON public.twin_status_transition_trigger(twin_trigger_id);

-- Step 4: Drop old foreign key constraint to featurer table
ALTER TABLE public.twin_status_transition_trigger
DROP CONSTRAINT IF EXISTS twin_status_transition_trigger_featurer_id_fk;

-- Step 5: Drop old index on transition_trigger_featurer_id
DROP INDEX IF EXISTS twin_status_transition_trigger_transition_trigger_featurer_id_i;

-- Step 6: Drop old columns
ALTER TABLE public.twin_status_transition_trigger
DROP COLUMN IF EXISTS transition_trigger_featurer_id,
    DROP COLUMN IF EXISTS transition_trigger_params;

