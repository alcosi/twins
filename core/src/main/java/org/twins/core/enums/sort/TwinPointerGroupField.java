package org.twins.core.enums.sort;

/**
 * Group fields for {@code twin_pointer} count. Only direct, low-cardinality entity fields.
 * domainId is intentionally NOT groupable — it is a security-only field, never exposed to the client.
 */
public enum TwinPointerGroupField {
    twinClassId,
    pointerFeaturerId,
    createdByUserId,
    optional
}
