package org.twins.core.domain;

import org.twins.core.service.EntityDuplicateService;

import java.util.*;

/**
 * Registry of in-flight duplicate operations, propagated through the whole duplicate cascade.
 * <p>
 * Each {@link EntityDuplicateService} registers its collected duplicates here, keyed by
 * {@link DuplicateKey} (entity class + originalId + newParentId). Cross-entity FK targets are
 * reserved via {@link EntityDuplicateService#lookupOrCollect} — this both deduplicates
 * (same key → same entry) and reserves the new UUID up-front so other duplicates can reference
 * the not-yet-saved entity before commit.
 * <p>
 * Lifecycle: created at the top-level entry point ({@code EntityDuplicateService.duplicate(Collection)})
 * and threaded through {@code collect} → {@code collectDuplicatesTree} → child {@code collect} →
 * ... → {@code commit}.
 * <p>
 * Dedup invariant: one {@link DuplicateKey} → one {@link EntityDuplicate}, no exceptions. Two
 * duplicates pointing at the same {@code (class, originalId, newParentId)} triple will share the
 * same new entity.
 */
public class EntityDuplicateCollector {

    /**
     * Identity key for a duplicate operation. {@code newParentId} is part of the key so that
     * duplicating the same original into two different target parents yields two separate
     * duplicates (not one shared) — this is what makes scope-aware remapping work.
     */
    public record DuplicateKey(Class<?> clazz, UUID originalId, UUID newParentId) {
        @Override
        public String toString() {
            return clazz.getSimpleName() + "[original=" + originalId + ", newParent=" + newParentId + "]";
        }
    }

    private final Map<DuplicateKey, EntityDuplicate<?, ?>> entries = new LinkedHashMap<>();
    private final Map<Class<?>, EntityDuplicateService<?, ?, ?>> services = new LinkedHashMap<>();

    /**
     * Registers the service responsible for {@code clazz}. Idempotent. Each subclass calls this
     * at the start of its {@code collect} so that other services can later resolve the owning
     * service for cascading via {@link EntityDuplicateService#lookupOrCollect}.
     */
    public void registerService(Class<?> clazz, EntityDuplicateService<?, ?, ?> service) {
        services.putIfAbsent(clazz, service);
    }

    public EntityDuplicateService<?, ?, ?> getService(Class<?> clazz) {
        return services.get(clazz);
    }

    /**
     * Classes that have at least one registered entry — i.e. classes that actually participate
     * in this operation (used for topological sort of commits).
     */
    public Set<Class<?>> getParticipatingClasses() {
        var result = new LinkedHashSet<Class<?>>();
        for (var key : entries.keySet()) {
            result.add(key.clazz());
        }
        return result;
    }

    public boolean has(DuplicateKey key) {
        return entries.containsKey(key);
    }

    public EntityDuplicate<?, ?> getEntry(DuplicateKey key) {
        return entries.get(key);
    }

    /**
     * Registers a fresh duplicate entry under {@code key} and reserves a new UUID for the
     * future newEntity. The reserved id is stored both on the entry and on the duplicate
     * ({@link EntityDuplicate#setNewEntityId}) so subclasses can read it inside
     * {@code createNewEntity} via {@code new EntityClass().setId(duplicate.getReservedNewId())}.
     */
    public EntityDuplicate<?, ?> register(DuplicateKey key, EntityDuplicate<?, ?> duplicate) {
        var reserved = UUID.randomUUID(); //todo delegate
        duplicate.setNewEntityId(reserved);
        entries.put(key, duplicate);
        return duplicate;
    }

    /**
     * All entries of {@code clazz} whose newEntity has been built (i.e. collect phase finished
     * for them). Used by both {@code commitClass} (to find what to save) and {@code afterCommit}.
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> getBuiltEntities(Class<E> clazz) {
        var result = new ArrayList<E>();
        for (var e : entries.entrySet()) {
            if (!e.getKey().clazz().equals(clazz)) {
                continue;
            }
            var newEntity = e.getValue().getNewEntity();
            if (newEntity != null) {
                result.add((E) newEntity);
            }
        }
        return result;
    }

    /**
     * All duplicates of {@code clazz} whose newEntity has been built, in registration order.
     */
    @SuppressWarnings("unchecked")
    public <D extends EntityDuplicate<?, ?>> List<D> getBuiltDuplicates(Class<?> clazz) {
        var result = new ArrayList<D>();
        for (var e : entries.entrySet()) {
            if (!e.getKey().clazz().equals(clazz)) {
                continue;
            }
            var dup = e.getValue();
            if (dup.getNewEntity() != null) {
                result.add((D) dup);
            }
        }
        return result;
    }
}
