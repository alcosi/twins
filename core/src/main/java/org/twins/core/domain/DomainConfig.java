package org.twins.core.domain;

import java.util.UUID;

/**
 * Marker for domain-scoped config entities (TWINS-719 domain versioning): their rows carry
 * {@code domain_id} (owner domain, NULL = system/shared row) and {@code domain_version_id}
 * (last-touch version stamp, maintained by DB triggers — Hibernate never writes it).
 * Used by DomainIdFiller for universal domain_id autofill on insert.
 */
public interface DomainConfig {
    UUID getDomainId();

    // for Accessor(chain = true)
    DomainConfig setDomainId(UUID domainId);

    UUID getDomainVersionId();
}
