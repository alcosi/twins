-- Table: twin_class_field_action
CREATE TABLE IF NOT EXISTS twin_class_field_action (
    id character varying(15) NOT NULL,
    CONSTRAINT twin_class_field_action_pk PRIMARY KEY (id)
);

-- Table: twin_class_field_action_validation_rule
CREATE TABLE IF NOT EXISTS twin_class_field_action_validation_rule (
    id uuid NOT NULL,
    twin_class_field_id uuid NOT NULL REFERENCES twin_class_field(id) ON UPDATE CASCADE ON DELETE CASCADE,
    twin_class_field_action_id character varying(15) NOT NULL REFERENCES twin_class_field_action(id) ON UPDATE CASCADE,
    "order" integer DEFAULT 1,
    active boolean DEFAULT true NOT NULL,
    twin_validator_set_id uuid REFERENCES twin_validator_set(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT twin_class_field_action_validation_rule_pk PRIMARY KEY (id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS twin_class_field_action_validation_rule_field_idx
    ON twin_class_field_action_validation_rule (twin_class_field_id);

CREATE INDEX IF NOT EXISTS twin_class_field_action_validation_rule_action_idx
    ON twin_class_field_action_validation_rule (twin_class_field_action_id);

CREATE UNIQUE INDEX IF NOT EXISTS twin_class_field_action_validation_rule_order_uniq
    ON twin_class_field_action_validation_rule (twin_class_field_id, twin_class_field_action_id, "order");

-- Insert EDIT action
INSERT INTO twin_class_field_action (id)
VALUES ('EDIT')
ON CONFLICT (id) DO NOTHING;

INSERT INTO twin_class_field_action (id)
VALUES ('VIEW')
ON CONFLICT (id) DO NOTHING;

