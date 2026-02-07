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
     active                   boolean DEFAULT true
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
