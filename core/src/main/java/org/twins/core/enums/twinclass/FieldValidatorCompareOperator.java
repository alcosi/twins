package org.twins.core.enums.twinclass;

/**
 * Comparison operators of the "Date compare" field validator (featurer 5601).
 * Configured via the {@code compareOperator} param in {@code twin_class_field_validator.field_validator_params},
 * defaults to {@link #ge}. Typical use: plannedEnd ge plannedStart.
 */
public enum FieldValidatorCompareOperator {
    // >=  (greater than or equal to)
    ge,
    // >   (strictly greater than)
    gt,
    // <=  (less than or equal to)
    le,
    // <   (strictly less than)
    lt,
    // =   (equal to)
    eq
}
