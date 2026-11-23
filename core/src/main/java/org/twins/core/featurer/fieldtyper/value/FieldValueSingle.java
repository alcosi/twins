package org.twins.core.featurer.fieldtyper.value;

/**
 * A field value reducible to a single hashable scalar: the scalar itself for simple/date values, the
 * entity id for entity-carrying values. Only single-valued FieldValues implement it — a collection
 * (FieldValueCollection) has no single scalar by definition. Used where values must be indexed or
 * compared as keys (e.g. twin group keys: TwinGroupLevel.extractKeys converts a multi-value field
 * into per-key single values) — deliberately an interface, NOT equals/hashCode on FieldValue
 * (whose equality is identity by design).
 */
public interface FieldValueSingle {
    /**
     * The value as a single hashable scalar token. Null when the value is undefined or cleared.
     */
    Object getScalarValue();
}
