package org.twins.core.service;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.domain.EntityDuplicate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * @param <D> duplicate descriptor type
 * @param <E> duplicated entity type
 * @param <P> parent entity type — meaningful only for services invoked via {@link #duplicateFor(Map)};
 *            use {@link Void} for top-level entities that have no parent
 */
public abstract class EntityDuplicateService<D extends EntityDuplicate<E>, E, P> {

    protected abstract EntitySecureFindServiceImpl<E> entityService();

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

    protected void afterSave(Collection<D> duplicates, Collection<E> saved) throws ServiceException {
    }

    @Transactional(rollbackFor = Throwable.class)
    public E duplicate(D duplicate) throws ServiceException {
        var ret = duplicate(Collections.singletonList(duplicate));
        return ret.isEmpty() ? null : ret.iterator().next();
    }

    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        validateKeyUniqueness(duplicates);
        loadOriginalEntities(duplicates);
        var entitiesToSave = new ArrayList<E>();
        for (var duplicate : duplicates) {
            var original = duplicate.getOriginalEntity();
            var newEntity = createNewEntity(duplicate);
            if (duplicate.getDuplicateParentEntityId() != null) {
                setNewParentEntityId(newEntity, duplicate.getDuplicateParentEntityId());
            }
            duplicateI18nFields(original, newEntity);
            duplicate.setNewEntity(newEntity);
            entitiesToSave.add(newEntity);
        }
        var saved = StreamSupport.stream(entityService().saveSafe(entitiesToSave).spliterator(), false).toList();
        afterSave(duplicates, saved);
        return saved;
    }

    /**
     * Bulk duplicate child entities of one or more source parents into matching destination parents.
     * <p>
     * For each entry {@code (sourceParent -> destinationParent)}:
     * <ol>
     *     <li>loads children into all source parents (once, via {@link #loadFor(Collection)});</li>
     *     <li>extracts children from the source parent via {@link #extractorChildren(Object)};</li>
     *     <li>builds a {@link D} per child pointing at the destination parent via {@link #createNewDuplicate()};</li>
     * </ol>
     * then delegates to {@link #duplicate(Collection)} for the actual save.
     */
    @Transactional(rollbackFor = Throwable.class)
    public Collection<E> duplicateFor(Map<P, P> parentMap) throws ServiceException {
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
                newDuplicate.setDuplicateParentEntityId(destinationParentId);
                duplicates.add(newDuplicate);
            }
        }
        return duplicate(duplicates);
    }

    protected abstract void setNewParentEntityId(E newEntity, UUID duplicateParentEntityId);

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

    protected void loadDuplicateParent(Collection<D> duplicates) throws ServiceException {
        entityService().load(duplicates,
                EntityDuplicate::getOriginalEntityId,
                EntityDuplicate::getOriginalEntity,
                EntityDuplicate::setOriginalEntity);
    }
}
