package org.twins.core.dao.annotation;

import org.twins.core.domain.DomainConfig;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

/**
 * Universal {@code domain_id} autofill for domain-scoped config rows (TWINS-719).
 *
 * Fills the {@code domainId} of a {@link DomainConfig} entity, but only:
 * <ul>
 *   <li>on insert — the entity id is still unassigned (ids are generated at persist time),
 *       so already-loaded rows being updated are never touched (a system row must not turn
 *       into a domain-owned one on update);</li>
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
        if (!(entity instanceof Identifiable<?> identifiable) || identifiable.getId() != null)
            return entity;  // not an insert (or identity not detectable — safer to skip)
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
