package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.domain.EntityDuplicate;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.EntityDuplicateCollector.DuplicateKey;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.Function;

/**
 * Two-phase duplicate engine: collect → commit.
 *
 * <ul>
 *   <li><b>collect</b> ({@link #collect}) — builds new entities in memory, reserves newIds,
 *       persists i18n rows, and cascades through {@link #collectDuplicatesTree} so the whole
 *       tree of duplicates lands in {@link EntityDuplicateCollector} before any business entity
 *       is saved. Cross-entity FK targets are reserved via
 *       {@link #lookupOrCollect(E, UUID, EntityDuplicateCollector)}.</li>
 *   <li><b>commit</b> (private {@code commit}) — saves per class in topological order declared
 *       by {@link #commitAfter()}, then runs {@link #afterCommit} hooks in the same order.
 *       Single outer transaction; rollback is atomic.</li>
 * </ul>
 *
 * Dedup invariant (enforced by ctx keying on {@code (class, originalId, newParentId)}):
 * the same target parent + the same source entity always yields one shared duplicate.
 *
 * @param <D> duplicate descriptor type
 * @param <E> duplicated entity type
 * @param <P> parent entity type — {@code Void} for top-level entities
 */
@Slf4j
public abstract class EntityDuplicateService<D extends EntityDuplicate<E, P>, E, P> {

    protected abstract EntitySecureFindServiceImpl<E> entityService();

    /**
     * Parent entity service — used to bulk-load {@code newParentEntity} by id during collect.
     * Return {@code null} for top-level entities ({@code P == Void}).
     */
    protected abstract EntitySecureFindServiceImpl<P> entityParentService();

    /**
     * Builds a fresh new entity from {@code duplicate.originalEntity}. May consult {@code ctx}
     * via {@link #lookupOrCollect(E, UUID, EntityDuplicateCollector)} to reserve cross-entity FK
     * targets. The reserved id of any reserved target is available from the returned entry; this
     * method is expected to set it on the corresponding FK field.
     * <p>
     * Implementations should also call {@code newEntity.setId(duplicate.getReservedNewId())}
     * — the id is reserved by the engine before this method runs.
     */
    protected abstract E createNewEntity(D duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException;

    protected abstract ErrorCode getKeyDuplicatedErrorCode();

    protected abstract void duplicateI18nFields(E src, E dst) throws ServiceException;

    /** Factory for fresh {@link D} instances — used by {@link #collectViaParentMap} and {@link #lookupOrCollect}. */
    protected abstract D createNewDuplicate();

    /** Loads children into source parents prior to iteration in {@link #collectViaParentMap}. */
    protected abstract void loadFor(Collection<P> parents);

    /** Extracts the child entity kit from a source parent. */
    protected abstract Kit<E, UUID> extractorChildren(P parent);

    /** Extracts the UUID from a destination parent. */
    protected abstract UUID extractParentId(P parent);

    /** Sets the parent reference (both id and entity) on a freshly built new entity. */
    protected abstract void setNewParentEntity(E newEntity, P parentEntity);

    /**
     * Entity class used as ctx key and as the discriminator for topological sort. Always
     * override — there is no sensible default.
     */
    protected abstract Class<E> getEntityClass();

    /**
     * Classes that must be fully committed before this service's entities can be committed.
     * Compile-time declaration; typically parent entity class plus any cross-entity FK target
     * classes that this service reserves via {@link #lookupOrCollect}. Default: empty (top-level).
     */
    protected Set<Class<?>> commitAfter() {
        return Set.of();
    }

    /**
     * Hook called after this service's own duplicates have been built and registered, but before
     * any commit. Override to:
     * <ul>
     *   <li>cascade child duplication via {@code childService.collectViaParentMap(ctx, parentMap)}</li>
     * </ul>
     * Default: no-op.
     */
    protected void collectDuplicatesTree(Collection<D> duplicates, EntityDuplicateCollector ctx) throws ServiceException {
    }

    /**
     * Post-commit hook for side effects like hierarchy refresh. Receives only the duplicates
     * that this service just saved (not the whole ctx). Default: no-op.
     */
    protected void afterCommit(Collection<D> duplicates, Collection<E> saved, EntityDuplicateCollector ctx) throws ServiceException {
    }

    // === Public entry points ===

    @Transactional(rollbackFor = Throwable.class)
    public E duplicate(D duplicate) throws ServiceException {
        var ret = duplicate(Collections.singletonList(duplicate));
        return ret.isEmpty() ? null : ret.iterator().next();
    }

    /**
     * Top-level entry point. Creates a fresh {@link EntityDuplicateCollector}, collects the whole
     * duplicate tree starting from {@code duplicates}, commits in topological order, runs
     * afterCommit hooks, and returns the new top-level entities (other participants are
     * accessible through the now-discarded ctx — callers needing them should re-resolve from db).
     */
    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        var ctx = new EntityDuplicateCollector();
        collect(ctx, duplicates);
        commit(ctx);
        return ctx.getBuiltEntities(getEntityClass());
    }

    /**
     * Convenience entry point: duplicates children of {@code sourceParent} into matching slots
     * on {@code destParent}. Builds the duplicate list via {@link #collectViaParentMap} inside
     * a fresh ctx, commits, and returns the new top-level entities.
     */
    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicateFor(Map<P, P> parentMap) throws ServiceException {
        if (parentMap == null || parentMap.isEmpty()) {
            return Collections.emptyList();
        }
        var ctx = new EntityDuplicateCollector();
        ctx.registerService(getEntityClass(), this);
        collectViaParentMap(ctx, parentMap);
        commit(ctx);
        return ctx.getBuiltEntities(getEntityClass());
    }

    // === Collect phase ===

    /**
     * Collects {@code duplicates}: validates keys, loads originals (+ parents if applicable),
     * builds new entities (reserving newIds), persists i18n, then triggers
     * {@link #collectDuplicatesTree} for cascade.
     * <p>
     * Idempotent per duplicate: if a duplicate's {@code newEntity} is already set (e.g. it was
     * built by an earlier {@link #lookupOrCollect} cascade from another service), it is skipped.
     * Dedup at ctx level ensures the same {@code (class, originalId, newParentId)} never
     * produces two new entities.
     * <p>
     * Note: collect is <b>not strictly read-only</b> — {@code duplicateI18nFields} persists I18n
     * rows (see {@code I18nService.duplicateI18n}). The whole operation runs inside one outer
     * {@code @Transactional}, so rollback stays atomic.
     */
    public void collect(EntityDuplicateCollector duplicateCollector, Collection<D> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return;
        }
        duplicateCollector.registerService(getEntityClass(), this);
        validateKeyUniqueness(duplicates);
        loadOriginalEntities(duplicates);
        var originalEntities = duplicates.stream().map(EntityDuplicate::getOriginalEntity).toList();
        loadRequiredRelations(originalEntities);
        if (entityParentService() != null) {
            loadNewParentEntities(duplicates);
        }
        for (var duplicate : duplicates) {
            if (duplicate.getNewEntity() != null) {
                // already built by an earlier collect cycle (typical when this collect was
                // invoked from another service's lookupOrCollect)
                continue;
            }
            UUID newParentId = duplicate.getNewParentEntityId();
            if (entityParentService() != null && newParentId == null) {
                throw new ServiceException(getKeyDuplicatedErrorCode(),
                        "newParentEntityId is required for " + getEntityClass().getSimpleName()
                                + " (originalId=" + duplicate.getOriginalEntityId() + ")");
            }
            var key = new DuplicateKey(getEntityClass(), duplicate.getOriginalEntityId(), newParentId);
            var registeredDuplicate = duplicateCollector.getEntry(key);
            if (registeredDuplicate != null) {
                continue; //skipping
            }
            duplicateCollector.register(key, duplicate);
            var newEntity = createNewEntity(duplicate, duplicateCollector);
            if (duplicate.getNewParentEntity() != null) {
                setNewParentEntity(newEntity, duplicate.getNewParentEntity());
            }
            duplicateI18nFields(duplicate.getOriginalEntity(), newEntity);
            duplicate.setNewEntity(newEntity);
        }
        collectDuplicatesTree(duplicates, duplicateCollector);
    }

    protected void loadRequiredRelations(List<E> originalEntities) throws ServiceException {
        //override in child if some extra data load
    }

    /**
     * Public helper for cascade-collect from another service's {@link #collectDuplicatesTree}.
     * Builds a {@code D} per child of each source parent pointing at the destination parent,
     * then delegates to {@link #collect}. Does NOT commit — the caller's outer
     * {@code duplicate(Collection)} owns the commit.
     */
    public void collectViaParentMap(EntityDuplicateCollector duplicateCollector, Map<P, P> parentMap) throws ServiceException {
        if (parentMap == null || parentMap.isEmpty()) {
            return;
        }
        loadFor(parentMap.keySet());
        Function<E, UUID> childIdExtractor = entityService().entityGetIdFunction();
        List<D> duplicates = new ArrayList<>();
        for (var entry : parentMap.entrySet()) {
            P destinationParent = entry.getValue();
            UUID destinationParentId = extractParentId(destinationParent);
            Kit<E, UUID> children = extractorChildren(entry.getKey());
            if (KitUtils.isEmpty(children)) {
                continue;
            }
            for (E child : children) {
                D newDuplicate = createNewDuplicate();
                newDuplicate.setOriginalEntity(child);
                newDuplicate.setOriginalEntityId(childIdExtractor.apply(child));
                newDuplicate.setNewParentEntity(destinationParent);
                newDuplicate.setNewParentEntityId(destinationParentId);
                duplicates.add(newDuplicate);
            }
        }
        collect(duplicateCollector, duplicates);
    }

    /**
     * Reserve-or-reuse entry for an entity managed by <b>this</b> service. Idempotent on
     * {@code (getEntityClass(), originalId, newParentId)}.
     * <p>
     * If already present in ctx: returns the existing entry (deduplication — same key always
     * reuses the same new entity).
     * <p>
     * If absent: builds a fresh {@link D} pointing at the original + new parent, registers it
     * (reserving a new UUID), and recursively invokes {@link #collect} so the FK target itself
     * gets built (its own {@code createNewEntity}, {@code setNewParentEntity}, i18n, cascade).
     * <p>
     * Caller (typically {@code createNewEntity} of another service) should use
     * {@code entry.getReservedNewId()} to set the FK field on its own new entity.
     */
    public UUID lookupOrCollect(E original, UUID newParentId, EntityDuplicateCollector ctx) throws ServiceException {
        UUID originalId = entityService().entityGetIdFunction().apply(original);
        var key = new DuplicateKey(getEntityClass(), originalId, newParentId);
        var existing = ctx.getEntry(key);
        if (existing != null) {
            return existing.getNewEntityId();
        }
        D duplicate = createNewDuplicate();
        duplicate
                .setOriginalEntity(original)
                .setOriginalEntityId(originalId)
                .setNewParentEntityId(newParentId);
        ctx.registerService(getEntityClass(), this);
        ctx.register(key, duplicate);
        collect(ctx, List.of(duplicate));
        return ctx.getEntry(key).getNewEntityId();
    }

    // === Commit phase ===

    /**
     * Two-pass commit: first {@code commitClass} per class in topological order
     * (so FK targets land in db before referencers), then {@code afterCommit} hooks in the
     * same order. Single outer transaction.
     */
    private void commit(EntityDuplicateCollector ctx) throws ServiceException {
        var orderedClasses = topoSortCommitOrder(ctx);
        for (var clazz : orderedClasses) {
            var svc = ctx.getService(clazz);
            if (svc != null) {
                svc.commitClass(ctx);
            }
        }
        for (var clazz : orderedClasses) {
            var svc = ctx.getService(clazz);
            if (svc != null) {
                svc.runAfterCommit(ctx);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void commitClass(EntityDuplicateCollector ctx) throws ServiceException {
        var dupsToCommit = ctx.<D>getBuiltDuplicates(getEntityClass());
        if (dupsToCommit.isEmpty()) {
            return;
        }
        var toSave = dupsToCommit.stream()
                .map(EntityDuplicate::getNewEntity)
                .toList();
        entityService().saveSafe(toSave);
    }

    @SuppressWarnings("unchecked")
    private void runAfterCommit(EntityDuplicateCollector ctx) throws ServiceException {
        var dups = ctx.<D>getBuiltDuplicates(getEntityClass());
        if (dups.isEmpty()) {
            return;
        }
        var saved = ctx.<E>getBuiltEntities(getEntityClass());
        afterCommit((Collection<D>) dups, (Collection<E>) saved, ctx);
    }

    /**
     * Topological sort of participating classes using {@link #commitAfter()} as the dependency
     * graph. Kahn's algorithm. Throws {@link ServiceException} ({@link ErrorCodeTwins#CYCLIC_DEPENDENCY})
     * on cycle.
     * <p>
     * Classes declared in {@code commitAfter()} but not participating in this operation are
     * skipped — they don't constrain anything since they have no entities to commit.
     */
    private List<Class<?>> topoSortCommitOrder(EntityDuplicateCollector ctx) throws ServiceException {
        var classes = ctx.getParticipatingClasses();
        var participating = new HashSet<>(classes);
        Map<Class<?>, Integer> inDegree = new java.util.HashMap<>();
        Map<Class<?>, List<Class<?>>> dependents = new java.util.HashMap<>();
        for (var c : classes) {
            inDegree.put(c, 0);
            dependents.put(c, new ArrayList<>());
        }
        for (var c : classes) {
            var svc = ctx.getService(c);
            if (svc == null) {
                continue;
            }
            for (var dep : svc.commitAfter()) {
                if (!participating.contains(dep)) {
                    continue;
                }
                dependents.get(dep).add(c);
                inDegree.merge(c, 1, Integer::sum);
            }
        }
        var queue = new ArrayDeque<Class<?>>();
        for (var e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }
        var sorted = new ArrayList<Class<?>>();
        while (!queue.isEmpty()) {
            var c = queue.poll();
            sorted.add(c);
            for (var dependent : dependents.get(c)) {
                var newDeg = inDegree.merge(dependent, -1, Integer::sum);
                if (newDeg == 0) {
                    queue.add(dependent);
                }
            }
        }
        if (sorted.size() != classes.size()) {
            var remaining = new ArrayList<>(classes);
            remaining.removeAll(sorted);
            throw new ServiceException(ErrorCodeTwins.CYCLIC_DEPENDENCY,
                    "Cyclic commitAfter() dependency among duplicate services. Unresolved: " + remaining);
        }
        return sorted;
    }

    // === Load / validate helpers ===

    protected void validateKeyUniqueness(Collection<D> duplicates) throws ServiceException {
        var newKeys = new HashSet<String>();
        for (var duplicate : duplicates) {
            String key = duplicate.getNewKey();
            if (key == null) {
                continue;
            }
            if (!newKeys.add(key)) {
                throw new ServiceException(getKeyDuplicatedErrorCode(),
                        "key[" + key + "] is duplicated in request");
            }
        }
    }

    protected void loadOriginalEntities(Collection<D> duplicates) throws ServiceException {
        entityService().load(duplicates,
                EntityDuplicate::getOriginalEntityId,
                EntityDuplicate::getOriginalEntity,
                EntityDuplicate::setOriginalEntity);
    }

    private void loadNewParentEntities(Collection<D> duplicates) throws ServiceException {
        entityParentService().load(duplicates,
                EntityDuplicate::getNewParentEntityId,
                EntityDuplicate::getNewParentEntity,
                EntityDuplicate::setNewParentEntity);
    }
}
