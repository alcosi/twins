package org.twins.core.domain.draft;

import lombok.Getter;
import lombok.Setter;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

import java.util.*;

@Getter
public class DraftCollector {
    final DraftEntity draftEntity;
    final Map<Class<?>, Set<Object>> draftEntitiesMap = new HashMap<>();
    final HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
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
        draftEntitiesMap.computeIfAbsent(entity.getClass(), k -> new HashSet<>());
        draftEntitiesMap.get(entity.getClass()).add(entity);
        return this;
    }

    public UUID getDraftId() {
        return draftEntity.getId();
    }

    public boolean hasChanges() {
        return !draftEntitiesMap.isEmpty();
    }

    public boolean isWritable() {
        if (draftEntity == null)
            return false;
        switch (draftEntity.getStatus()) {
            case COMMITED:
            case OUT_OF_DATE:
            case COMMIT_EXCEPTION:
            case CONSTRUCTION_EXCEPTION:
            case NORMALIZE_EXCEPTION:
            case CHECK_CONFLICTS_EXCEPTION:
            case COMMIT_IN_PROGRESS:
            case ERASE_SCOPE_COLLECT_EXCEPTION:
            case UNCOMMITED:
                return false;
            case LOCKED:
            case UNDER_CONSTRUCTION:
            case ERASE_SCOPE_COLLECT_PLANNED:
            case ERASE_SCOPE_COLLECT_IN_PROGRESS:
            case ERASE_SCOPE_COLLECT_NEED_START:
                return true;
            default:
                return false;
        }
    }

    public void clear() {
        draftEntitiesMap.clear();
        historyCollector.clear();
    }

    public DraftCounters getDraftCounters() {
        return draftEntity.getCounters();
    }
}
