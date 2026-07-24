-- TWINS-893: pluggable Recomputer featurer.
-- New parallel tables (old twin_class_field_recompute_* left untouched). Recomputer config lives once
-- per subscriber (pointer + field); on_field/on_action rules reference it via FK. recomputer_featurer_id
-- is NOT NULL — every subscriber must declare its Recomputer (default 5501 = RecomputerByFieldTyper).

-- 1. Subscriber table: one row per (pointer, field) + recomputer config
CREATE TABLE IF NOT EXISTS twin_recompute_subscriber
(
    id                             uuid            NOT NULL
        CONSTRAINT twin_recompute_subscriber_pk PRIMARY KEY,
    domain_id                      uuid            NOT NULL
        CONSTRAINT twin_recompute_subscriber_domain_id_fk
            REFERENCES domain ON UPDATE CASCADE ON DELETE CASCADE,
    subscriber_twin_pointer_id     uuid            NOT NULL
        CONSTRAINT twin_recompute_subscriber_sub_twin_pointer_id_fk
            REFERENCES twin_pointer ON UPDATE CASCADE ON DELETE CASCADE,
    subscriber_twin_class_field_id uuid            NOT NULL
        CONSTRAINT twin_recompute_subscriber_sub_twin_class_field_id_fk
            REFERENCES twin_class_field ON UPDATE CASCADE ON DELETE CASCADE,
    recomputer_featurer_id         integer         NOT NULL
        CONSTRAINT twin_recompute_subscriber_recomputer_featurer_id_fk
            REFERENCES featurer ON UPDATE CASCADE ON DELETE CASCADE,
    recomputer_params              hstore,
    CONSTRAINT twin_recompute_subscriber_pointer_field_uk
        UNIQUE (subscriber_twin_pointer_id, subscriber_twin_class_field_id)
);

CREATE INDEX IF NOT EXISTS ix_twin_recompute_subscriber_domain_id
    ON twin_recompute_subscriber (domain_id);
CREATE INDEX IF NOT EXISTS ix_twin_recompute_subscriber_sub_field_id
    ON twin_recompute_subscriber (subscriber_twin_class_field_id);

-- 2. OnField rules: subscriber + publisher field + async
CREATE TABLE IF NOT EXISTS twin_recompute_on_field
(
    id                            uuid            NOT NULL
        CONSTRAINT twin_recompute_on_field_pk PRIMARY KEY,
    recompute_subscriber_id       uuid            NOT NULL
        CONSTRAINT twin_recompute_on_field_recompute_subscriber_id_fk
            REFERENCES twin_recompute_subscriber ON UPDATE CASCADE ON DELETE CASCADE,
    publisher_twin_class_field_id uuid            NOT NULL
        CONSTRAINT twin_recompute_on_field_pub_twin_class_field_id_fk
            REFERENCES twin_class_field ON UPDATE CASCADE ON DELETE CASCADE,
    async                         boolean         NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS ix_twin_recompute_on_field_subscriber_id
    ON twin_recompute_on_field (recompute_subscriber_id);
CREATE INDEX IF NOT EXISTS ix_twin_recompute_on_field_pub_field_id
    ON twin_recompute_on_field (publisher_twin_class_field_id);

-- 3. OnAction rules: subscriber + publisher class + action + async
CREATE TABLE IF NOT EXISTS twin_recompute_on_action
(
    id                       uuid            NOT NULL
        CONSTRAINT twin_recompute_on_action_pk PRIMARY KEY,
    recompute_subscriber_id  uuid            NOT NULL
        CONSTRAINT twin_recompute_on_action_recompute_subscriber_id_fk
            REFERENCES twin_recompute_subscriber ON UPDATE CASCADE ON DELETE CASCADE,
    publisher_twin_class_id  uuid            NOT NULL
        CONSTRAINT twin_recompute_on_action_pub_twin_class_id_fk
            REFERENCES twin_class ON UPDATE CASCADE ON DELETE CASCADE,
    publisher_twin_action_id varchar         NOT NULL
        CONSTRAINT twin_recompute_on_action_pub_twin_action_id_fk
            REFERENCES twin_action ON UPDATE CASCADE ON DELETE CASCADE,
    async                    boolean         NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS ix_twin_recompute_on_action_subscriber_id
    ON twin_recompute_on_action (recompute_subscriber_id);
CREATE INDEX IF NOT EXISTS ix_twin_recompute_on_action_pub_class_id
    ON twin_recompute_on_action (publisher_twin_class_id);
CREATE INDEX IF NOT EXISTS ix_twin_recompute_on_action_pub_action_id
    ON twin_recompute_on_action (publisher_twin_action_id);

-- 4. Pre-seed default recomputer featurer (must exist before subscriber backfill references it as FK)
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5501, 55, '', '', '', false)
ON CONFLICT ON CONSTRAINT featurer_pk DO NOTHING;

-- 5. Backfill subscriber rows from old tables (no-op on empty DBs). recomputer = 5501 (default RecomputerByFieldTyper).
INSERT INTO twin_recompute_subscriber (id, domain_id, subscriber_twin_pointer_id, subscriber_twin_class_field_id, recomputer_featurer_id)
SELECT DISTINCT ON (subscriber_twin_pointer_id, subscriber_twin_class_field_id)
       gen_random_uuid(), domain_id, subscriber_twin_pointer_id, subscriber_twin_class_field_id, 5501
FROM (
    SELECT domain_id, subscriber_twin_pointer_id, subscriber_twin_class_field_id FROM twin_class_field_recompute_on_field
    UNION
    SELECT domain_id, subscriber_twin_pointer_id, subscriber_twin_class_field_id FROM twin_class_field_recompute_on_action
) u
ON CONFLICT (subscriber_twin_pointer_id, subscriber_twin_class_field_id) DO NOTHING;

-- 6. Backfill on_field rules (keep original id), resolving subscriber FK
INSERT INTO twin_recompute_on_field (id, recompute_subscriber_id, publisher_twin_class_field_id, async)
SELECT o.id, s.id, o.publisher_twin_class_field_id, o.async
FROM twin_class_field_recompute_on_field o
    JOIN twin_recompute_subscriber s
        ON s.subscriber_twin_pointer_id = o.subscriber_twin_pointer_id
       AND s.subscriber_twin_class_field_id = o.subscriber_twin_class_field_id
ON CONFLICT (id) DO NOTHING;

-- 7. Backfill on_action rules (keep original id), resolving subscriber FK
INSERT INTO twin_recompute_on_action (id, recompute_subscriber_id, publisher_twin_class_id, publisher_twin_action_id, async)
SELECT o.id, s.id, o.publisher_twin_class_id, o.publisher_twin_action_id, o.async
FROM twin_class_field_recompute_on_action o
    JOIN twin_recompute_subscriber s
        ON s.subscriber_twin_pointer_id = o.subscriber_twin_pointer_id
       AND s.subscriber_twin_class_field_id = o.subscriber_twin_class_field_id
ON CONFLICT (id) DO NOTHING;
