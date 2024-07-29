package org.twins.core.domain.draft;

import lombok.Getter;
import lombok.Setter;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class DraftCollector {
    final DraftEntity draftEntity;
    Map<Class<?>, Set<Object>> saveEntityMap = new HashMap<>();
    @Setter
    boolean onceFlushed = false;

    public DraftCollector(DraftEntity draftEntity) {
        this.draftEntity = draftEntity;
    }

    public DraftCollector add(Object entity) throws ServiceException {
        if (draftEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_NOT_STARTED, "current draft was not started");
        if (!isWritable())
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_NOT_WRITABLE, "current draft is already not writable");
        saveEntityMap.computeIfAbsent(entity.getClass(), k -> new HashSet<>());
        saveEntityMap.get(entity.getClass()).add(entity);
        return this;
    }

    public boolean hasChanges() {
        return !saveEntityMap.isEmpty();
    }

    public boolean isWritable() {
        return draftEntity != null && draftEntity.getStatus() == DraftEntity.Status.UNDER_CONSTRUCTION;
    }
}
