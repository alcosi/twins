package org.twins.core.service;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.domain.EntityDuplicate;
import org.twins.core.domain.EntityDuplicateContext;

import java.util.*;
import java.util.function.Function;

/**
 * @param <D> duplicate descriptor type
 * @param <E> duplicated entity type
 * @param <P> parent entity type — meaningful only for services invoked via {@link #duplicateFor(Map)};
 *            use {@link Void} for top-level entities that have no parent
 */
public abstract class EntityDuplicateService<D extends EntityDuplicate<E, P>, E, P> {

    protected abstract EntitySecureFindServiceImpl<E> entityService();

    protected abstract EntitySecureFindServiceImpl<P> entityParentService();

    protected abstract E createNewEntity(D duplicate) throws ServiceException;

    protected abstract ErrorCode getKeyDuplicatedErrorCode();

    protected abstract void duplicateI18nFields(E src, E dst) throws ServiceException;

    /**
     * Factory for fresh {@link D} instances — used by {@link #duplicateFor} to build one duplicate per child entity.
     */
    protected abstract D createNewDuplicate();

    /**
     * Loads children into source parents prior to iteration in {@link #duplicateFor}.
     * Implementations may be no-ops if loading is performed externally by the caller.
     */
    protected abstract void loadFor(Collection<P> parents);

    /**
     * Extracts the child entity kit from a source parent during {@link #duplicateFor}.
     * For top-level services this is never invoked.
     */
    protected abstract Kit<E, UUID> extractorChildren(P parent);

    /**
     * Extracts the UUID from a destination parent during {@link #duplicateFor}.
     * For top-level services this is never invoked.
     */
    protected abstract UUID extractParentId(P parent);

    protected abstract void setNewParentEntity(E newEntity, P parentEntity);

    /**
     * Entity class used to register old→new id mappings in {@link EntityDuplicateContext}.
     * Override when this service should participate in cross-entity reference remapping
     * (i.e. when other duplicated entities may hold FKs pointing at this one).
     * Default {@code null} — no registration, no remapping from this entity.
     */
    protected Class<E> getEntityClass() {
        return null;
    }

    /**
     * Hook to remap foreign keys on the newly-built entity so they point at the duplicated
     * copies of referenced entities instead of the originals. Invoked after
     * {@link #createNewEntity}, {@link #setNewParentEntity} and {@link #duplicateI18nFields},
     * before {@code saveSafe()}.
     * <p>
     * Default: no-op. Override to translate FKs that are "internal" to the duplication graph
     * (e.g. {@code twinFactoryConditionSetId}). Use {@link EntityDuplicateContext#resolveOrDefault(Class, UUID)}
     * to fall back to the original id when no mapping exists (e.g. the referenced entity
     * was not part of this operation — cross-domain references stay intact).
     */
    protected void remapReferences(E newEntity, EntityDuplicateContext ctx) throws ServiceException {
    }

    /**
     * Post-save hook: refresh hierarchies, build per-flag maps, delegate to child services
     * via {@code duplicateFor(parentMap, ctx)}. The same {@link EntityDuplicateContext} that flowed
     * into the outer {@link #duplicate(Collection, EntityDuplicateContext)} is propagated so child
     * cascades can remap their FKs against anything saved so far.
     */
    protected void afterSave(Collection<D> duplicates, Collection<E> saved, EntityDuplicateContext ctx) throws ServiceException {
    }

    // === Backward-compatible overloads — create a fresh context internally ===

    @Transactional(rollbackFor = Throwable.class)
    public E duplicate(D duplicate) throws ServiceException {
        var ret = duplicate(Collections.singletonList(duplicate), new EntityDuplicateContext());
        return ret.isEmpty() ? null : ret.iterator().next();
    }

    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException {
        return duplicate(duplicates, new EntityDuplicateContext());
    }

    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicateFor(Map<P, P> parentMap) throws ServiceException {
        return duplicateFor(parentMap, new EntityDuplicateContext());
    }

    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicate(Collection<D> duplicates, EntityDuplicateContext ctx) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        validateKeyUniqueness(duplicates);
        loadOriginalEntities(duplicates);
        loadNewParentEntities(duplicates);
        collectDuplicatesTree(duplicates, ctx);
        var entitiesToSave = new ArrayList<E>();
        for (var duplicate : duplicates) {
            var original = duplicate.getOriginalEntity();
            var newEntity = createNewEntity(duplicate);
            if (duplicate.getNewParentEntityId() != null) {
                setNewParentEntity(newEntity, duplicate.getNewParentEntity());
            }
            duplicateI18nFields(original, newEntity);
            remapReferences(newEntity, ctx);
            duplicate.setNewEntity(newEntity);
            entitiesToSave.add(newEntity);
        }
        var saved = StreamSupport.stream(entityService().saveSafe(entitiesToSave).spliterator(), false).toList();
        registerIds(duplicates, saved, ctx);
        afterSave(duplicates, saved, ctx);
        return saved;
    }

    protected abstract void collectDuplicatesTree(Collection<D> duplicates, EntityDuplicateContext ctx);

    /**
     * Bulk duplicate child entities of one or more source parents into matching destination parents.
     * <p>
     * For each entry {@code (sourceParent -> destinationParent)}:
     * <ol>
     *     <li>loads children into all source parents (once, via {@link #loadFor(Collection)});</li>
     *     <li>extracts children from the source parent via {@link #extractorChildren(Object)};</li>
     *     <li>builds a {@link D} per child pointing at the destination parent via {@link #createNewDuplicate()};</li>
     * </ol>
     * then delegates to {@link #duplicate(Collection, EntityDuplicateContext)} for the actual save.
     */
    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicateFor(Map<P, P> parentMap, EntityDuplicateContext ctx) throws ServiceException {
        if (parentMap == null || parentMap.isEmpty()) {
            return Collections.emptyList();
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
                // setters come from EntityDuplicate<E> and return EntityDuplicate<E>, not D — assign field-by-field
                D newDuplicate = createNewDuplicate();
                newDuplicate.setOriginalEntity(child);
                newDuplicate.setOriginalEntityId(childIdExtractor.apply(child));
                newDuplicate.setNewParentEntityId(destinationParentId);
                duplicates.add(newDuplicate);
            }
        }
        return duplicate(duplicates, ctx);
    }

    protected void validateKeyUniqueness(Collection<D> duplicates) throws ServiceException {
        var newKeys = new HashSet<String>();
        for (var duplicate : duplicates) {
            String key = duplicate.getNewKey();
            if (key == null) {
                continue;
            }
            if (!newKeys.add(key)) {
                throw new ServiceException(getKeyDuplicatedErrorCode(), "key[" + key + "] is duplicated in request");
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
        if (entityParentService() == null) {
            return;
        }
        entityParentService().load(duplicates,
                EntityDuplicate::getNewParentEntityId,
                EntityDuplicate::getNewParentEntity,
                EntityDuplicate::setNewParentEntity);
    }

    /**
     * Registers {@code originalEntityId → saved.id} into the context for every saved entity.
     * Relies on positional correspondence between {@code duplicates} and {@code saved}.
     */
    protected void registerIds(Collection<D> duplicates, Collection<E> saved, EntityDuplicateContext ctx) {
        Class<E> clazz = getEntityClass();
        if (clazz == null) {
            return;
        }
        Function<E, UUID> idFn = entityService().entityGetIdFunction();
        var it = saved.iterator();
        for (var duplicate : duplicates) {
            if (!it.hasNext()) {
                break;
            }
            E savedEntity = it.next();
            ctx.register(clazz, duplicate.getOriginalEntityId(), idFn.apply(savedEntity));
        }
    }
}
