package org.twins.core.domain.draft;

import lombok.Getter;
import lombok.Setter;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.draft.*;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;

@Getter
public class DraftCollector {
    final DraftEntity draftEntity;
    Map<Class<?>, Set<Object>> saveEntityMap = new HashMap<>();
    @Setter
    boolean onceFlushed = false;
    @Getter
    DraftCounters counters = new DraftCounters();

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
        if (entity instanceof DraftTwinEraseEntity)
            counters.incrementTwinErase();
        else if (entity instanceof DraftTwinPersistEntity)
            counters.incrementTwinPersist();
        else if (entity instanceof DraftTwinLinkEntity twinLinkEntity) {
            switch (twinLinkEntity.getCud()) {
                case CREATE -> counters.incrementTwinLinkCreate();
                case DELETE -> counters.incrementTwinLinkDelete();
                case UPDATE -> counters.incrementTwinLinkUpdate();
            }
        } else if (entity instanceof DraftTwinAttachmentEntity twinAttachmentEntity) {
            switch (twinAttachmentEntity.getCud()) {
                case CREATE -> counters.incrementTwinAttachmentCreate();
                case DELETE -> counters.incrementTwinAttachmentDelete();
                case UPDATE -> counters.incrementTwinAttachmentUpdate();
            }
        } else if (entity instanceof DraftTwinTagEntity twinTagEntity) {
            if (twinTagEntity.isCreateElseDelete())
                counters.incrementTwinTagCreate();
            else
                counters.incrementTwinTagDelete();
        } else if (entity instanceof DraftTwinMarkerEntity twinMarkerEntity) {
            if (twinMarkerEntity.isCreateElseDelete())
                counters.incrementTwinMarkerCreate();
            else
                counters.incrementTwinMarkerDelete();
        } else if (entity instanceof DraftTwinFieldSimpleEntity twinFieldSimpleEntity) {
            switch (twinFieldSimpleEntity.getCud()) {
                case CREATE -> counters.incrementTwinTwinFieldSimpleCreate();
                case DELETE -> counters.incrementTwinTwinFieldSimpleDelete();
                case UPDATE -> counters.incrementTwinTwinFieldSimpleUpdate();
            }
        } else if (entity instanceof DraftTwinFieldDataListEntity twinFieldSimpleEntity) {
            switch (twinFieldSimpleEntity.getCud()) {
                case CREATE -> counters.incrementTwinTwinFieldSimpleCreate();
                case DELETE -> counters.incrementTwinTwinFieldSimpleDelete();
                case UPDATE -> counters.incrementTwinTwinFieldSimpleUpdate();
            }
        } else if (entity instanceof DraftTwinFieldUserEntity twinFieldUserEntity) {
            switch (twinFieldUserEntity.getCud()) {
                case CREATE -> counters.incrementTwinTwinFieldUserCreate();
                case DELETE -> counters.incrementTwinTwinFieldUserDelete();
                case UPDATE -> counters.incrementTwinTwinFieldUserUpdate();
            }
        }

        return this;
    }

    public UUID getDraftId() {
        return draftEntity.getId();
    }

    public boolean hasChanges() {
        return !saveEntityMap.isEmpty();
    }

    public boolean isWritable() {
        return draftEntity != null && (draftEntity.getStatus() == DraftEntity.Status.UNDER_CONSTRUCTION || draftEntity.getStatus() == DraftEntity.Status.LOCKED);
    }
}
