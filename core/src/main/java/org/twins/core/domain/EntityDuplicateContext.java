package org.twins.core.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registry of old→new UUID mappings per entity class, propagated through the whole
 * duplicate cascade. Each {@code EntityDuplicateService} registers its saved entities
 * here so downstream cascades can remap foreign keys that point at other duplicated
 * entities (e.g. {@code twinFactoryConditionSetId} when duplicating a factory whose
 * pipelines reference its condition sets).
 * <p>
 * Lifecycle: created at the top-level entry point ({@code duplicate(duplicates)} /
 * {@code duplicateFor(parentMap)}) and threaded through {@code afterSave()} → child
 * {@code duplicateFor(parentMap, ctx)} → {@code duplicate(duplicates, ctx)}.
 */
public class EntityDuplicateContext {
    private final Map<Class<?>, Map<UUID, UUID>> mapping = new HashMap<>();

    public EntityDuplicateContext register(Class<?> clazz, UUID oldId, UUID newId) {
        if (clazz == null || oldId == null || newId == null) {
            return this;
        }
        mapping.computeIfAbsent(clazz, k -> new HashMap<>()).put(oldId, newId);
        return this;
    }

    /**
     * Resolves {@code oldId} to its new UUID for the given class, or {@code null} if
     * no mapping exists (e.g. the referenced entity was not part of this duplicate
     * operation — typical for cross-domain references).
     */
    public UUID resolve(Class<?> clazz, UUID oldId) {
        if (clazz == null || oldId == null) {
            return null;
        }
        Map<UUID, UUID> classMap = mapping.get(clazz);
        return classMap == null ? null : classMap.get(oldId);
    }

    /**
     * Resolves {@code oldId} or returns it unchanged when no mapping exists. Use for
     * FK remapping where keeping the original reference is the desired fallback.
     */
    public UUID resolveOrDefault(Class<?> clazz, UUID oldId) {
        UUID resolved = resolve(clazz, oldId);
        return resolved != null ? resolved : oldId;
    }
}
