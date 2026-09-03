-- LVL-9372: configurable backend validators for twin class field values.
-- Validators are attached to a twin class field and executed during twin create/update field validation
-- (FieldTyper.validate -> TwinService.validateFieldsOnCreate/OnUpdate), failures are reported
-- via the existing invalidTwinFieldErrors mechanism.
-- Featurer type 56 "FieldValidator":
--   5601 "Date compare" — compares the field date with another date field of the same twin
--   5602 "Duration equals date diff" — duration == (end date - start date) in whole days
--   5603 "Date compare with parent" — compares the field date with a date field of the head twin
-- Featurer stub: class/name/description are filled from @Featurer at app startup.

INSERT INTO featurer_type (id, name)
VALUES (56, 'FieldValidator')
ON CONFLICT (id) DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5601, 56, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5602, 56, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5603, 56, '', '', '', false)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS twin_class_field_validator
(
    id                          uuid     NOT NULL
        CONSTRAINT twin_class_field_validator_pk PRIMARY KEY,
    twin_class_field_id         uuid     NOT NULL
        CONSTRAINT twin_class_field_validator_twin_class_field_id_fk
            REFERENCES twin_class_field ON UPDATE CASCADE ON DELETE CASCADE,
    field_validator_featurer_id integer  NOT NULL
        CONSTRAINT twin_class_field_validator_featurer_id_fk
            REFERENCES featurer ON UPDATE CASCADE ON DELETE CASCADE,
    field_validator_params      hstore,
    be_validation_error_i18n_id uuid
        CONSTRAINT twin_class_field_validator_be_validation_error_i18n_id_fk
            REFERENCES i18n ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_twin_class_field_validator_twin_class_field_id
    ON twin_class_field_validator (twin_class_field_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_validator_field_validator_featurer_id
    ON twin_class_field_validator (field_validator_featurer_id);
CREATE INDEX IF NOT EXISTS idx_twin_class_field_validator_be_validation_error_i18n_id
    ON twin_class_field_validator (be_validation_error_i18n_id);

