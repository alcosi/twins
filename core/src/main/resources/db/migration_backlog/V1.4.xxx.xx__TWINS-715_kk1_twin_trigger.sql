-- Create twin_trigger table
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

-- Create twin_trigger_task_status table
CREATE TABLE IF NOT EXISTS twin_trigger_task_status (
    id VARCHAR(20) NOT NULL PRIMARY KEY
);

INSERT INTO twin_trigger_task_status VALUES ('NEED_START') ON CONFLICT (id) DO NOTHING;
INSERT INTO twin_trigger_task_status VALUES ('IN_PROGRESS') ON CONFLICT (id) DO NOTHING;
INSERT INTO twin_trigger_task_status VALUES ('DONE') ON CONFLICT (id) DO NOTHING;
INSERT INTO twin_trigger_task_status VALUES ('FAILED') ON CONFLICT (id) DO NOTHING;

-- Create twin_trigger_task table
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

-- Create twin_factory_trigger table
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

-- Migrate twinflow_transition_trigger data
-- Add new columns
ALTER TABLE twinflow_transition_trigger
    ADD COLUMN IF NOT EXISTS twin_trigger_id uuid,
    ADD COLUMN IF NOT EXISTS async boolean DEFAULT true;

-- Drop old foreign key constraint
ALTER TABLE twinflow_transition_trigger
    DROP CONSTRAINT IF EXISTS twinflow_transition_trigger_featurer_id_fk;

-- Migrate data: create twin_trigger records for each unique featurer+params combination
INSERT INTO twin_trigger (id, domain_id, twin_trigger_featurer_id, twin_trigger_param, active)
SELECT
    uuid_generate_v5('00000000-0000-0000-0000-000000000000'::uuid,
        tclass.domain_id::text || '|' || ttt.transition_trigger_featurer_id::text || '|' || COALESCE(ttt.transition_trigger_params::text, '')),
    tclass.domain_id,
    ttt.transition_trigger_featurer_id,
    ttt.transition_trigger_params,
    true
FROM (SELECT DISTINCT transition_trigger_featurer_id, transition_trigger_params, twinflow_transition_id
      FROM twinflow_transition_trigger
      WHERE transition_trigger_featurer_id IS NOT NULL) AS ttt
JOIN twinflow_transition tt ON tt.id = ttt.twinflow_transition_id
JOIN twinflow tf ON tf.id = tt.twinflow_id
JOIN twin_class tclass ON tclass.id = tf.twin_class_id
ON CONFLICT DO NOTHING;

-- Update twinflow_transition_trigger with new twin_trigger_id
UPDATE twinflow_transition_trigger ttt
SET twin_trigger_id = tr.id
FROM twin_trigger tr
WHERE ttt.transition_trigger_featurer_id = tr.twin_trigger_featurer_id
  AND COALESCE(ttt.transition_trigger_params::text, '') = COALESCE(tr.twin_trigger_param::text, '');

-- Add foreign key constraint to twin_trigger
ALTER TABLE twinflow_transition_trigger
    ADD CONSTRAINT twinflow_transition_trigger_twin_trigger_id_fk
    FOREIGN KEY (twin_trigger_id) REFERENCES twin_trigger(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS twinflow_transition_trigger_twin_trigger_id_index
    ON twinflow_transition_trigger (twin_trigger_id);

-- Drop old columns
ALTER TABLE twinflow_transition_trigger
    DROP COLUMN IF EXISTS transition_trigger_featurer_id,
    DROP COLUMN IF EXISTS transition_trigger_params;

-- Rename twin_status_transition_trigger to twin_status_trigger
ALTER TABLE twin_status_transition_trigger
    DROP CONSTRAINT IF EXISTS twin_status_transition_trigger_featurer_id_fk;

ALTER TABLE twin_status_transition_trigger
    DROP CONSTRAINT IF EXISTS twin_status_transition_type_id_fk;

DROP INDEX IF EXISTS idx_twin_status_transition_type_id;

-- Add new columns (async before twin_trigger_id)
ALTER TABLE twin_status_transition_trigger

    ADD COLUMN IF NOT EXISTS twin_trigger_id uuid,
    ADD COLUMN IF NOT EXISTS async boolean DEFAULT true;

-- Add boolean incoming_else_outgoing column
ALTER TABLE twin_status_transition_trigger
    ADD COLUMN IF NOT EXISTS incoming_else_outgoing boolean;

-- Migrate data from twin_status_transition_type_id to incoming_else_outgoing (incoming=true, outgoing=false)
UPDATE twin_status_transition_trigger
SET incoming_else_outgoing = (twin_status_transition_type_id = 'incoming');

-- Drop twin_status_transition_type table
DROP TABLE IF EXISTS twin_status_transition_type;

-- Drop old column twin_status_transition_type_id
ALTER TABLE twin_status_transition_trigger
    DROP COLUMN IF EXISTS twin_status_transition_type_id;

-- Migrate data: create twin_trigger records for twin_status_transition_trigger
INSERT INTO twin_trigger (id, domain_id, twin_trigger_featurer_id, twin_trigger_param, active)
SELECT
    uuid_generate_v5('00000000-0000-0000-0000-000000000000'::uuid,
        tc.domain_id::text || '|' || tstt.transition_trigger_featurer_id::text || '|' || COALESCE(tstt.transition_trigger_params::text, '')),
    tc.domain_id,
    tstt.transition_trigger_featurer_id,
    tstt.transition_trigger_params,
    COALESCE(tstt.active, true)
FROM (SELECT DISTINCT transition_trigger_featurer_id, transition_trigger_params, twin_status_id, active
      FROM twin_status_transition_trigger
      WHERE transition_trigger_featurer_id IS NOT NULL) AS tstt
JOIN twin_status ts ON ts.id = tstt.twin_status_id
JOIN twin_class tc ON tc.id = ts.twins_class_id
ON CONFLICT DO NOTHING;

-- Update twin_status_transition_trigger with new twin_trigger_id
UPDATE twin_status_transition_trigger tstt
SET twin_trigger_id = tr.id
FROM twin_trigger tr
WHERE tstt.transition_trigger_featurer_id = tr.twin_trigger_featurer_id
  AND COALESCE(tstt.transition_trigger_params::text, '') = COALESCE(tr.twin_trigger_param::text, '');

-- Add foreign key constraint to twin_trigger
ALTER TABLE twin_status_transition_trigger
    ADD CONSTRAINT twin_status_transition_trigger_twin_trigger_id_fk
    FOREIGN KEY (twin_trigger_id) REFERENCES twin_trigger(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS twin_status_transition_trigger_twin_trigger_id_index
    ON twin_status_transition_trigger (twin_trigger_id);

-- Drop old columns
ALTER TABLE twin_status_transition_trigger
    DROP COLUMN IF EXISTS transition_trigger_featurer_id,
    DROP COLUMN IF EXISTS transition_trigger_params;

-- Rename table
ALTER TABLE twin_status_transition_trigger RENAME TO twin_status_trigger;

-- Add scheduler for executing twin triggers
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5010::integer, 50::integer, '', '', '', DEFAULT)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler (id, domain_id, scheduler_featurer_id, cron, active, log_enabled, scheduler_params, fixed_rate, description, created_at, updated_at)
VALUES ('00000000-0000-0000-0015-00000000000a'::uuid, NULL, 5010, NULL, true, true, NULL, 2000, 'Scheduler for executing twin triggers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
