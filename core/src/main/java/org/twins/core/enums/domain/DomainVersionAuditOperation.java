package org.twins.core.enums.domain;

/**
 * Admin-facing change vocabulary of {@code domain_version_audit.operation} (TWINS-719). Soft vs
 * physical delete is internal mechanics, derived from the row's group history (first=INSERT +
 * last=DELETE = born and deleted inside the window), never from a separate operation value.
 * RESTORE = the row returned to a previous state and left the window.
 */
public enum DomainVersionAuditOperation {
    INSERT,
    UPDATE,
    DELETE,
    RESTORE
}
