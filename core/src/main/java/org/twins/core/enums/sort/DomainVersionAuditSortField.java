package org.twins.core.enums.sort;

/**
 * Sortable fields for {@code domain_version_audit} search. seq is the strict insertion order
 * (bigserial) — the natural audit ordering.
 */
public enum DomainVersionAuditSortField {
    seq,
    changedAt,
    tableName,
    operation,
    id
}
