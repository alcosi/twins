package org.twins.core.enums.domain;

/**
 * Long-running operation currently executed against a domain (TWINS-719): mirrored on
 * {@code domain.operation_status} (the domain lock/state) and stamped into
 * {@code domain_version_audit.domain_operation_status} so every change record shows which
 * operation produced it. IDLE means normal member activity.
 */
public enum DomainOperationStatus {
    IDLE,
    IMPORTING,
    RELEASING,
    ROLLBACK
}
