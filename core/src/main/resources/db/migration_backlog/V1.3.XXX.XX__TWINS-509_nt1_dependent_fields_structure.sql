CREATE TABLE IF NOT EXISTS twin_class_field_rule
(
    id
    UUID
    PRIMARY
    KEY,
    dependent_twin_class_field_id
    UUID
    NOT
    NULL
    REFERENCES
    twin_class_field
(
    id
) ON DELETE CASCADE,
    target_element VARCHAR
(
    16
) NOT NULL DEFAULT 'value',
    target_param_key VARCHAR NULL,
    dependent_overwritten_value VARCHAR NULL,
    rule_priority INT NULL);

CREATE TABLE IF NOT EXISTS twin_class_field_condition
(
    id
    UUID
    PRIMARY
    KEY,
    twin_class_field_rule_id
    UUID
    NOT
    NULL
    REFERENCES
    twin_class_field_rule
(
    id
) ON DELETE CASCADE,
    base_twin_class_field_id UUID NOT NULL REFERENCES twin_class_field
(
    id
)
  ON DELETE CASCADE,
    condition_order INT NULL,
    group_no INT NULL,
    condition_operator VARCHAR
(
    32
) NOT NULL,
    cmp_value VARCHAR NULL,
    cmp_params hstore NULL,
    evaluated_element VARCHAR
(
    16
) NOT NULL DEFAULT 'value',
    evaluated_param_key VARCHAR NULL);

-- todo - dictionaries for condition_operator and evaluated_element