package org.twins.core.dao.annotation;

import org.twins.core.domain.DomainConfig;

import java.util.UUID;

/**
 * Universal {@code domain_id} autofill for domain-scoped config rows (TWINS-719).
 *
 * Fills the {@code domainId} of a {@link DomainConfig} entity, but only:
 * <ul>
 *   <li>on insert — decided by {@code Persistable.isNew()} (the entity's newRow flag is flipped
 *       off at {@code @PostLoad}/{@code @PostPersist}), so already-loaded rows being updated are
 *       never touched (a system row must not turn into a domain-owned one on update). A null-id
 *       check would misfire: create paths do pre-assign ids (UuidUtils.generate, duplicate
 *       reserved ids);</li>
 *   <li>when no explicit value is set — import / duplicate / rollback paths provide their own.</li>
 * </ul>
 * System rows are not created through services, so a null {@code domainId} on insert means
 * "current user's domain". Entities not implementing {@link DomainConfig} (not versioned yet)
 * are skipped silently — their versioning wave adds the columns, the fields and the interface
 * together.
 */
public final class DomainIdFiller {
    public static <T> T fillOnInsert(T entity, UUID domainId) {
        if (entity == null || domainId == null)
            return entity;
        if (!(entity instanceof DomainConfig domainConfig))
            return entity;
        if (!domainConfig.isNew())
            return entity;  // update — never touch
        if (domainConfig.getDomainId() != null)
            return entity;  // explicit value wins
        domainConfig.setDomainId(domainId);
        return entity;
    }

    public static <T> void fillOnInsert(Iterable<T> entities, UUID domainId) {
        if (entities == null)
            return;
        for (T entity : entities)
            fillOnInsert(entity, domainId);
    }
}
