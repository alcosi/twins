package org.twins.core.domain;

import java.util.UUID;
import java.util.Objects;

/**
 * Key wrapper for entities in EntitiesChangesCollector.
 * Uses UUID id for equals/hashCode to avoid issues when entity fields change.
 */
public record EntityKey(UUID id, Object entity) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityKey entityKey = (EntityKey) o;
        if (id == null) {
            // For new entities (id == null), compare by reference
            return entity == entityKey.entity;
        }
        return Objects.equals(id, entityKey.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            // For new entities (id == null), use identityHashCode
            return System.identityHashCode(entity);
        }
        return Objects.hash(id);
    }
}
