package org.twins.core.service;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.domain.EntityDuplicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.StreamSupport;

public abstract class EntityDuplicateService<D extends EntityDuplicate<E>, E> {

    protected abstract EntitySecureFindServiceImpl<E> entityService();

    protected abstract E createNewEntity(D duplicate) throws ServiceException;

    protected abstract ErrorCode getKeyDuplicatedErrorCode();

    protected abstract void duplicateI18nFields(E src, E dst) throws ServiceException;

    protected void prepareDuplicates(Collection<D> duplicates) throws ServiceException {
    }

    protected void afterSave(Collection<D> duplicates, Collection<E> saved) throws ServiceException {
    }

    @Transactional
    public E duplicate(D duplicate) throws ServiceException {
        var ret = duplicate(Collections.singletonList(duplicate));
        return ret.isEmpty() ? null : ret.iterator().next();
    }

    @Transactional
    public Collection<E> duplicate(Collection<D> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        validateKeyUniqueness(duplicates);
        loadOriginalEntities(duplicates);
        prepareDuplicates(duplicates);
        var entitiesToSave = new ArrayList<E>();
        for (var duplicate : duplicates) {
            var original = duplicate.getOriginalEntity();
            var newEntity = createNewEntity(duplicate);
            duplicateI18nFields(original, newEntity);
            duplicate.setNewEntity(newEntity);
            entitiesToSave.add(newEntity);
        }
        var saved = StreamSupport.stream(entityService().saveSafe(entitiesToSave).spliterator(), false).toList();
        afterSave(duplicates, saved);
        return saved;
    }

    protected void validateKeyUniqueness(Collection<D> duplicates) throws ServiceException {
        var newKeys = new HashSet<String>();
        for (var duplicate : duplicates) {
            String key = duplicate.getNewKey();
            if (newKeys.contains(key))
                throw new ServiceException(getKeyDuplicatedErrorCode(), "key[" + key + "] is duplicated in request");
            newKeys.add(key);
        }
    }

    protected void loadOriginalEntities(Collection<D> duplicates) throws ServiceException {
        entityService().load(duplicates,
                EntityDuplicate::getOriginalEntityId,
                EntityDuplicate::getOriginalEntity,
                EntityDuplicate::setOriginalEntity);
    }
}
