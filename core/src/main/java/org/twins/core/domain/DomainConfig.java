package org.twins.core.domain;

import java.util.UUID;

/**
 * Marker for domain-scoped config entities (TWINS-719 domain versioning): their rows carry
 * {@code domain_id} (owner domain, NULL = system/shared row) and {@code domain_version_id}
 * (last-touch version stamp, maintained by DB triggers — Hibernate never writes it).
 * Used by DomainIdFiller for universal domain_id autofill on insert.
 *
 * {@link #isNew()} is redeclared abstract: implementors MUST override the id-null default from
 * {@link Identifiable} with a real insert flag ({@code @Transient boolean newRow = true} flipped
 * off at {@code @PostLoad}/{@code @PostPersist}) — create paths may pre-assign ids before save
 * (UuidUtils.generate, duplicate reserved ids), so id nullness is not a reliable discriminator.
 */
public interface DomainConfig extends Identifiable<UUID> {
    UUID getDomainId();

    // for Accessor(chain = true)
    DomainConfig setDomainId(UUID domainId);

    UUID getDomainVersionId();

    @Override
    boolean isNew();
}
