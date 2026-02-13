package org.twins.core.service;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationRepository;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.comment.TwinCommentRepository;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.PostponedTriggers;
import org.twins.core.domain.TwinChangesApplyResult;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.trigger.TwinTriggerTaskService;
import org.twins.core.service.twin.TwinChangeTaskService;

import java.util.*;

import static org.twins.core.domain.TwinChangesCollector.TwinInvalidate;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinChangesService {

    private final TwinRepository twinRepository;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository;
    private final TwinFieldDataListRepository twinFieldDataListRepository;
    private final TwinLinkRepository twinLinkRepository;
    private final TwinFieldUserRepository twinFieldUserRepository;
    private final TwinMarkerRepository twinMarkerRepository;
    private final TwinTagRepository twinTagRepository;
    private final TwinAttachmentRepository twinAttachmentRepository;
    private final TwinFieldI18nRepository twinFieldI18nRepository;
    private final TwinFieldBooleanRepository twinFieldBooleanRepository;
    private final TwinFieldTwinClassListRepository twinFieldTwinClassListRepository;
    private final TwinAttachmentModificationRepository twinAttachmentModificationRepository;
    private final TwinFieldAttributeRepository twinFieldAttributeRepository;
    private final TwinCommentRepository twinCommentRepository;
    private final SpaceRoleUserRepository spaceRoleUserRepository;
    private final EntitySmartService entitySmartService;
    private final HistoryService historyService;
    private final TwinChangeTaskService twinChangeTaskService;
    private final TwinTriggerTaskService twinTriggerTaskService;

    @Transactional(rollbackFor = Throwable.class)
    public TwinChangesApplyResult applyChanges(TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinChangesApplyResult changesApplyResult = new TwinChangesApplyResult();
        if (!twinChangesCollector.hasChanges() &&
            twinChangesCollector.getPostponedChanges().isEmpty() &&
            twinChangesCollector.getPostponedTriggers().isEmpty())
            return changesApplyResult;
        //we have to flush new twins save because of "Not-null property references a transient value - transient instance must be saved before current operation" in other related entities
        saveEntitiesAndFlush(twinChangesCollector, TwinEntity.class, twinRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldSimpleEntity.class, twinFieldSimpleRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldSimpleNonIndexedEntity.class, twinFieldSimpleNonIndexedRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldDataListEntity.class, twinFieldDataListRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldUserEntity.class, twinFieldUserRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinLinkEntity.class, twinLinkRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinMarkerEntity.class, twinMarkerRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinTagEntity.class, twinTagRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinAttachmentEntity.class, twinAttachmentRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinAttachmentModificationEntity.class, twinAttachmentModificationRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldI18nEntity.class, twinFieldI18nRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldBooleanEntity.class, twinFieldBooleanRepository, changesApplyResult);
        saveEntities(twinChangesCollector, SpaceRoleUserEntity.class, spaceRoleUserRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldTwinClassEntity.class, twinFieldTwinClassListRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldAttributeEntity.class, twinFieldAttributeRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinCommentEntity.class, twinCommentRepository, changesApplyResult);

        if (!twinChangesCollector.getSaveEntityMap().isEmpty())
            for (Map.Entry<Class<?>, Map<Object, ChangesHelper>> classChanges : twinChangesCollector.getSaveEntityMap().entrySet()) {
                log.warn("Unsupported entity class[{}] for saving", classChanges.getKey().getSimpleName());
            }

        deleteEntities(twinChangesCollector, TwinEntity.class, twinRepository);
        deleteEntities(twinChangesCollector, TwinFieldDataListEntity.class, twinFieldDataListRepository);
        deleteEntities(twinChangesCollector, TwinFieldSimpleEntity.class, twinFieldSimpleRepository);
        deleteEntities(twinChangesCollector, TwinFieldSimpleNonIndexedEntity.class, twinFieldSimpleNonIndexedRepository);
        deleteEntities(twinChangesCollector, TwinFieldUserEntity.class, twinFieldUserRepository);
        deleteEntities(twinChangesCollector, TwinLinkEntity.class, twinLinkRepository);
        deleteEntities(twinChangesCollector, TwinMarkerEntity.class, twinMarkerRepository);
        deleteEntities(twinChangesCollector, TwinTagEntity.class, twinTagRepository);
        deleteEntities(twinChangesCollector, TwinAttachmentEntity.class, twinAttachmentRepository);
        deleteEntities(twinChangesCollector, TwinFieldI18nEntity.class, twinFieldI18nRepository);
        deleteEntities(twinChangesCollector, TwinAttachmentModificationEntity.class, twinAttachmentModificationRepository);
        deleteEntities(twinChangesCollector, TwinFieldBooleanEntity.class, twinFieldBooleanRepository);
        deleteEntities(twinChangesCollector, SpaceRoleUserEntity.class, spaceRoleUserRepository);
        deleteEntities(twinChangesCollector, TwinFieldTwinClassEntity.class, twinFieldTwinClassListRepository);
        deleteEntities(twinChangesCollector, TwinFieldAttributeEntity.class, twinFieldAttributeRepository);
        deleteEntities(twinChangesCollector, TwinCommentEntity.class, twinCommentRepository);

        if (!twinChangesCollector.getDeleteEntityMap().isEmpty())
            for (Map.Entry<Class<?>, Set<Object>> classChanges : twinChangesCollector.getDeleteEntityMap().entrySet()) {
                log.warn("Unsupported entity class[{}] for deletion", classChanges.getKey().getSimpleName());
            }
        savePostponedChanges(twinChangesCollector);
        savePostponedTriggers(twinChangesCollector.getPostponedTriggers());
        invalidate(twinChangesCollector.getInvalidationMap());
        historyService.saveHistory(twinChangesCollector.getHistoryCollector());
        twinChangesCollector.clear();
        return changesApplyResult;
    }

    private void savePostponedChanges(TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinChangesCollector.getPostponedChanges().isEmpty())
            return;
        List<TwinChangeTaskEntity> changeTaskList = new ArrayList<>();
        for (var entry : twinChangesCollector.getPostponedChanges().entrySet()) {
            changeTaskList.add(new TwinChangeTaskEntity()
                    .setTwinId(entry.getKey())
                    .setTwinFactoryId(entry.getValue().getLeft())
                    .setTwinFactorylauncher(entry.getValue().getRight())
                    .setStatusId(TwinChangeTaskStatus.NEED_START));
        }
        twinChangeTaskService.addTasks(changeTaskList);
    }

    public void savePostponedTriggers(PostponedTriggers postponedTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(postponedTriggers))
            return;
        List<TwinTriggerTaskEntity> triggerTaskList = new ArrayList<>();
        for (PostponedTriggers.PostponedTrigger postponedTrigger : postponedTriggers) {
            triggerTaskList.add(new TwinTriggerTaskEntity()
                    .setTwinId(postponedTrigger.twinId())
                    .setTwinTriggerId(postponedTrigger.triggerId())
                    .setPreviousTwinStatusId(postponedTrigger.statusId())
                    .setStatusId(TwinTriggerTaskStatus.NEED_START));
        }
        log.info("Saving {} postponed triggers", triggerTaskList.size());
        twinTriggerTaskService.addTasks(triggerTaskList);
    }

    private void invalidate(Map<Object, Set<TwinChangesCollector.TwinInvalidate>> invalidationMap) {
        for (var entry : invalidationMap.entrySet()) {
            if (entry.getKey() instanceof TwinEntity twinEntity) {
                for (TwinChangesCollector.TwinInvalidate invalidation : entry.getValue()) {
                    switch (invalidation) {
                        case tagsKit -> twinEntity.setTwinTagKit(null);
                        case markersKit -> twinEntity.setTwinMarkerKit(null);
                        case twinFieldSimpleKit -> twinEntity.setTwinFieldSimpleKit(null);
                        case twinFieldSimpleNonIndexedKit -> twinEntity.setTwinFieldSimpleNonIndexedKit(null);
                        case twinFieldUserKit -> twinEntity.setTwinFieldUserKit(null);
                        case twinFieldDatalistKit -> twinEntity.setTwinFieldDatalistKit(null);
                        case twinLinks -> twinEntity.setTwinLinks(null);
                        case fieldValuesKit -> twinEntity.setFieldValuesKit(null);
                        case twinAttachments -> twinEntity.setAttachmentKit(null);
                        case twinFieldI18nKit -> twinEntity.setTwinFieldI18nKit(null);
                        case twinFieldBooleanKit -> twinEntity.setTwinFieldBooleanKit(null);
                        case twinFieldTwinClassKit -> twinEntity.setTwinFieldTwinClassKit(null);
                        case twinFieldAttributeKit -> twinEntity.setTwinFieldAttributeKit(null);
                    }
                }
            } else if (entry.getKey() instanceof TwinAttachmentEntity twinAttachmentEntity) {
                for (TwinInvalidate invalidation : entry.getValue()) {
                    if (invalidation == TwinInvalidate.twinAttachmentModifications) {
                        twinAttachmentEntity.setModifications(null);
                    }
                }
            }

        }
    }

    // in some cases we need to story history only, and all entities changes will be stored in other way (for best performance)
    public void saveHistoryOnly(TwinChangesCollector twinChangesCollector) throws ServiceException {
        historyService.saveHistory(twinChangesCollector.getHistoryCollector());
    }

    private <T> void saveEntities(TwinChangesCollector twinChangesCollector, Class<T> entityClass, CrudRepository<T, UUID> repository, TwinChangesApplyResult changesApplyResult) throws ServiceException {
        Map<Object, ChangesHelper> entities = twinChangesCollector.getSaveEntityMap().get(entityClass);
        if (entities != null) {
            changesApplyResult.put(entityClass, entitySmartService.saveAllAndLogChanges((Map) entities, repository));
            twinChangesCollector.getSaveEntityMap().remove(entityClass);
        }
    }

    private <T> void saveEntitiesAndFlush(TwinChangesCollector twinChangesCollector, Class<T> entityClass, JpaRepository<T, UUID> repository, TwinChangesApplyResult changesApplyResult) throws ServiceException {
        Map<Object, ChangesHelper> entities = twinChangesCollector.getSaveEntityMap().get(entityClass);
        if (entities != null) {
            changesApplyResult.put(entityClass, entitySmartService.saveAllAndFlushAndLogChanges((Map) entities, repository));
            twinChangesCollector.getSaveEntityMap().remove(entityClass);
        }
    }

    private <T> void deleteEntities(TwinChangesCollector twinChangesCollector, Class<T> entityClass, CrudRepository<T, UUID> repository) {
        Set<T> entities = (Set<T>) twinChangesCollector.getDeleteEntityMap().get(entityClass);
        if (entities != null) {
            entitySmartService.deleteAllEntitiesAndLog(entities, repository);
            twinChangesCollector.getDeleteEntityMap().remove(entityClass);
        }
    }
}
