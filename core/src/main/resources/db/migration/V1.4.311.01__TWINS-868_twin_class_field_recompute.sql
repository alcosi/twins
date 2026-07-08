-- TWINS-868: Recompute system for Mater fields.
-- Registers new TwinAction.CREATE and creates three recompute metadata tables
-- described in ai/plans/field-typer-mater-listeners.md (§1.5).

INSERT INTO twin_action (id) VALUES ('CREATE') ON CONFLICT DO NOTHING;

-- ============================================================
-- twin_class_field_recompute_on_field: fires when the value of publisher_twin_class_field_id
-- changes for one or more twins. Subscriber is the twin resolved from the
-- publisher via subscriber_twin_pointer_id.
-- ============================================================
CREATE TABLE IF NOT EXISTS twin_class_field_recompute_on_field
(
    id                             uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_field_pk PRIMARY KEY,
    domain_id                      uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_field_domain_id_fk
            REFERENCES domain ON UPDATE CASCADE ON DELETE CASCADE,
    subscriber_twin_pointer_id     uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_field_sub_twin_pointer_id_fk
            REFERENCES twin_pointer ON UPDATE CASCADE ON DELETE CASCADE,
    subscriber_twin_class_field_id uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_field_sub_twin_class_field_id_fk
            REFERENCES twin_class_field ON UPDATE CASCADE ON DELETE CASCADE,
    publisher_twin_class_field_id  uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_field_pub_twin_class_field_id_fk
            REFERENCES twin_class_field ON UPDATE CASCADE ON DELETE CASCADE,
    async                          boolean         NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS tcf_recompute_on_field_sub_twin_class_field_id_index
    ON twin_class_field_recompute_on_field (subscriber_twin_class_field_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_field_pub_twin_class_field_id_idx
    ON twin_class_field_recompute_on_field (publisher_twin_class_field_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_field_sub_twin_pointer_id_idx
    ON twin_class_field_recompute_on_field (subscriber_twin_pointer_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_field_domain_id_idx
    ON twin_class_field_recompute_on_field (domain_id);

-- ============================================================
-- twin_class_field_recompute_on_action: fires when a TwinAction is performed on a twin of
-- publisher_twin_class_id. Subscriber is resolved via subscriber_twin_pointer_id.
-- ============================================================
CREATE TABLE IF NOT EXISTS twin_class_field_recompute_on_action
(
    id                             uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_action_pk PRIMARY KEY,
    domain_id                      uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_action_domain_id_fk
            REFERENCES domain ON UPDATE CASCADE ON DELETE CASCADE,
    subscriber_twin_pointer_id     uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_action_sub_twin_pointer_id_fk
            REFERENCES twin_pointer ON UPDATE CASCADE ON DELETE CASCADE,
    subscriber_twin_class_field_id uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_action_sub_twin_class_field_id_fk
            REFERENCES twin_class_field ON UPDATE CASCADE ON DELETE CASCADE,
    publisher_twin_class_id        uuid            NOT NULL
        CONSTRAINT twin_class_field_recompute_on_action_pub_twin_class_id_fk
            REFERENCES twin_class ON UPDATE CASCADE ON DELETE CASCADE,
    publisher_twin_action_id       varchar         NOT NULL
        CONSTRAINT twin_class_field_recompute_on_action_pub_twin_action_id_fk
            REFERENCES twin_action ON UPDATE CASCADE ON DELETE CASCADE,
    async                          boolean         NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS tcf__recompute_on_action_sub_twin_class_field_idx
    ON twin_class_field_recompute_on_action (subscriber_twin_class_field_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_action_pub_class_action_idx
    ON twin_class_field_recompute_on_action (publisher_twin_class_id, publisher_twin_action_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_action_sub_twin_pointer_idx
    ON twin_class_field_recompute_on_action (subscriber_twin_pointer_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_action_domain_id_idx
    ON twin_class_field_recompute_on_action (domain_id);

-- ============================================================
-- twin_class_field_recompute_on_action_validator_rule: optional validator sets checked before
-- Mater recompute fires for a twin_class_field_recompute_on_action row. Reuses the
-- twin_validator_set infrastructure. Mirrors twin_action_validator_rule pattern.
-- ============================================================
CREATE TABLE IF NOT EXISTS twin_class_field_recompute_on_action_validator_rule
(
    id                                       uuid     NOT NULL
        CONSTRAINT tcf__recompute_on_action_validator_rule_pk PRIMARY KEY,
    twin_class_field_recompute_on_action_id  uuid     NOT NULL
        CONSTRAINT tcf__recompute_on_action_validator_rule_listener_fk
            REFERENCES twin_class_field_recompute_on_action ON UPDATE CASCADE ON DELETE CASCADE,
    "order"                                  integer  DEFAULT 1,
    active                                   boolean  DEFAULT true NOT NULL,
    twin_validator_set_id                    uuid
        CONSTRAINT tcf__recompute_on_action_validator_rule_validator_set_fk
            REFERENCES twin_validator_set ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS tcf__recompute_on_action_validator_rule_order_uniq
    ON twin_class_field_recompute_on_action_validator_rule (twin_class_field_recompute_on_action_id, "order");
CREATE INDEX IF NOT EXISTS tcf__recompute_on_action_validator_rule_listener_idx
    ON twin_class_field_recompute_on_action_validator_rule (twin_class_field_recompute_on_action_id);
CREATE INDEX IF NOT EXISTS tcf__recompute_on_action_validator_rule_validator_set_idx
    ON twin_class_field_recompute_on_action_validator_rule (twin_validator_set_id);
