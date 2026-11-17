package org.twins.core.enums.domain;

/**
 * Lifecycle of a {@code domain_version} row (TWINS-719): OPEN — the mutable draft window collecting
 * configuration changes; RELEASED — an immutable snapshot (release or import). At most one OPEN
 * window per domain (enforced by a partial unique index).
 */
public enum DomainVersionStatus {
    OPEN,
    RELEASED
}
