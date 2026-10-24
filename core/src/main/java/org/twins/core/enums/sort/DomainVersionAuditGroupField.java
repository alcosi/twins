package org.twins.core.enums.sort;

/**
 * Group fields for {@code domain_version_audit} count. Only direct, low-cardinality entity fields.
 * domainId is intentionally NOT groupable — it is a security-only field, never exposed to the client.
 */
public enum DomainVersionAuditGroupField {
    domainVersionId,
    tableName,
    operation,
    domainOperationStatus,
    actorUserId
}
