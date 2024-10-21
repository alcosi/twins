package org.twins.core.domain.draft;

import lombok.Getter;
import lombok.Setter;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.draft.*;
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
        updateCounters(entity);
        return this;
    }

    private void updateCounters(Object entity) {
        if (entity instanceof DraftTwinEraseEntity draftTwinEraseEntity && draftTwinEraseEntity.isEraseReady()) { //we will increment counter if erase ready
            if (draftTwinEraseEntity.getStatus() == DraftTwinEraseEntity.Status.DETECTED_STATUS_CHANGE_ERASE)
                draftEntity.incrementTwinEraseIrrevocable();
            else
                draftEntity.incrementTwinEraseByStatus();
        } else if (entity instanceof DraftTwinPersistEntity draftTwinPersistEntity) {
            if (draftTwinPersistEntity.isCreateElseUpdate())
                draftEntity.incrementTwinPersistCreate();
            else
                draftEntity.incrementTwinPersistUpdate();
        } else if (entity instanceof DraftTwinLinkEntity twinLinkEntity) {
            switch (twinLinkEntity.getCud()) {
                case CREATE -> draftEntity.incrementTwinLinkCreate();
                case DELETE -> draftEntity.incrementTwinLinkDelete();
                case UPDATE -> draftEntity.incrementTwinLinkUpdate();
            }
        } else if (entity instanceof DraftTwinAttachmentEntity twinAttachmentEntity) {
            switch (twinAttachmentEntity.getCud()) {
                case CREATE -> draftEntity.incrementTwinAttachmentCreate();
                case DELETE -> draftEntity.incrementTwinAttachmentDelete();
                case UPDATE -> draftEntity.incrementTwinAttachmentUpdate();
            }
        } else if (entity instanceof DraftTwinTagEntity twinTagEntity) {
            if (twinTagEntity.isCreateElseDelete())
                draftEntity.incrementTwinTagCreate();
            else
                draftEntity.incrementTwinTagDelete();
        } else if (entity instanceof DraftTwinMarkerEntity twinMarkerEntity) {
            if (twinMarkerEntity.isCreateElseDelete())
                draftEntity.incrementTwinMarkerCreate();
            else
                draftEntity.incrementTwinMarkerDelete();
        } else if (entity instanceof DraftTwinFieldSimpleEntity twinFieldSimpleEntity) {
            switch (twinFieldSimpleEntity.getCud()) {
                case CREATE -> draftEntity.incrementTwinFieldSimpleCreate();
                case DELETE -> draftEntity.incrementTwinFieldSimpleDelete();
                case UPDATE -> draftEntity.incrementTwinFieldSimpleUpdate();
            }
        } else if (entity instanceof DraftTwinFieldDataListEntity twinFieldDataListEntity) {
            switch (twinFieldDataListEntity.getCud()) {
                case CREATE -> draftEntity.incrementTwinFieldDataListCreate();
                case DELETE -> draftEntity.incrementTwinFieldDataListDelete();
                case UPDATE -> draftEntity.incrementTwinFieldDataListUpdate();
            }
        } else if (entity instanceof DraftTwinFieldUserEntity twinFieldUserEntity) {
            switch (twinFieldUserEntity.getCud()) {
                case CREATE -> draftEntity.incrementTwinFieldUserCreate();
                case DELETE -> draftEntity.incrementTwinFieldUserDelete();
                case UPDATE -> draftEntity.incrementTwinFieldUserUpdate();
            }
        }
    }

    public UUID getDraftId() {
        return draftEntity.getId();
    }

    public boolean hasChanges() {
        return !draftEntitiesMap.isEmpty();
    }

    public boolean isWritable() {
        return draftEntity != null && (draftEntity.getStatus() == DraftEntity.Status.UNDER_CONSTRUCTION || draftEntity.getStatus() == DraftEntity.Status.LOCKED);
    }

    public void clear() {
        draftEntitiesMap.clear();
        historyCollector.clear();
    }
}
