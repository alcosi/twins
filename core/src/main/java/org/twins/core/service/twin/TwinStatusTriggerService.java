package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTriggerRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.trigger.TwinTriggerService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class TwinStatusTriggerService extends EntitySecureFindServiceImpl<TwinStatusTriggerEntity> {
    private final TwinStatusTriggerRepository repository;
    private final TwinTriggerService twinTriggerService;
    private final TwinStatusService twinStatusService;
    private final AuthService authService;
    @Lazy
    private final TwinChangesService twinChangesService;

    @Override
    public CrudRepository<TwinStatusTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinStatusTriggerEntity, UUID> entityGetIdFunction() {
        return TwinStatusTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatusTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        loadTrigger(entity);
        if (entity.getTwinTrigger().getDomainId() != null) {
            return !entity.getTwinTrigger().getDomainId().equals(apiUser.getDomainId());
        }
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatusTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinStatusId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinStatusId is not specified");
        }
        if (entity.getIncomingElseOutgoing() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " incomingElseOutgoing is not specified");
        }
        if (entity.getTwinTriggerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinTriggerId is not specified");
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinStatusTriggerEntity> createStatusTriggers(Collection<TwinStatusTriggerEntity> statusTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(statusTriggers)) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(saveSafe(statusTriggers).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinStatusTriggerEntity> updateStatusTriggers(Collection<TwinStatusTriggerEntity> statusTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(statusTriggers)) {
            return Collections.emptyList();
        }
        ChangesHelperMulti<TwinStatusTriggerEntity> changes = new ChangesHelperMulti<>();
        List<TwinStatusTriggerEntity> allEntities = new java.util.ArrayList<>(statusTriggers.size());

        for (TwinStatusTriggerEntity trigger : statusTriggers) {
            TwinStatusTriggerEntity entity = findEntitySafe(trigger.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValue(trigger.getTwinStatusId(), entity,
                    TwinStatusTriggerEntity::getTwinStatusId, TwinStatusTriggerEntity::setTwinStatusId,
                    TwinStatusTriggerEntity.Fields.twinStatusId, changesHelper);
            updateEntityFieldByValue(trigger.getIncomingElseOutgoing(), entity,
                    TwinStatusTriggerEntity::getIncomingElseOutgoing, TwinStatusTriggerEntity::setIncomingElseOutgoing,
                    TwinStatusTriggerEntity.Fields.incomingElseOutgoing, changesHelper);
            updateEntityFieldByValue(trigger.getOrder(), entity,
                    TwinStatusTriggerEntity::getOrder, TwinStatusTriggerEntity::setOrder,
                    TwinStatusTriggerEntity.Fields.order, changesHelper);
            updateEntityFieldByValue(trigger.getTwinTriggerId(), entity,
                    TwinStatusTriggerEntity::getTwinTriggerId, TwinStatusTriggerEntity::setTwinTriggerId,
                    TwinStatusTriggerEntity.Fields.twinTriggerId, changesHelper);
            updateEntityFieldByValue(trigger.getAsync(), entity,
                    TwinStatusTriggerEntity::getAsync, TwinStatusTriggerEntity::setAsync,
                    TwinStatusTriggerEntity.Fields.async, changesHelper);
            updateEntityFieldByValue(trigger.getActive(), entity,
                    TwinStatusTriggerEntity::getActive, TwinStatusTriggerEntity::setActive,
                    TwinStatusTriggerEntity.Fields.active, changesHelper);
            changes.add(entity, changesHelper);
        }

        updateSafe(changes);
        return allEntities;
    }


    @Transactional
    public void runTwinStatusTriggers(TwinEntity twinEntity, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        runTwinStatusTriggers(twinEntity, srcStatusEntity, dstStatusEntity, twinChangesCollector);
        twinChangesService.savePostponedTriggers(twinChangesCollector.getPostponedTriggers());
    }

    public void runTwinStatusTriggers(TwinEntity twinEntity, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity, TwinChangesCollector twinChangesCollector) throws ServiceException {
        UUID srcStatusId = srcStatusEntity != null ? srcStatusEntity.getId() : null;
        UUID dstStatusId = dstStatusEntity != null ? dstStatusEntity.getId() : null;
        List<UUID> statusIds = new ArrayList<>(2);
        if (srcStatusId != null && !srcStatusId.equals(dstStatusId)) {
            statusIds.add(srcStatusId);
        }
        if (dstStatusId != null && !dstStatusId.equals(srcStatusId)) {
            statusIds.add(dstStatusId);
        }
        if (statusIds.isEmpty()) {
            return;
        }
        List<TwinStatusTriggerEntity> triggers = repository.findAllByTwinStatusIdInAndActiveTrueOrderByOrder(statusIds);
        runTriggers(twinEntity, triggers, srcStatusEntity, dstStatusEntity, twinChangesCollector);
    }

    private void runTriggers(TwinEntity twinEntity, List<TwinStatusTriggerEntity> twinStatusTriggerEntityList, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(twinStatusTriggerEntityList))
            return;
        loadTriggers(twinStatusTriggerEntityList);
        for (TwinStatusTriggerEntity twinStatusTriggerEntity : twinStatusTriggerEntityList) {
            log.info("{} will be triggered", twinStatusTriggerEntity.logDetailed());
            var twinTriggerEntity = twinStatusTriggerEntity.getTwinTrigger();
            if (Boolean.TRUE.equals(twinStatusTriggerEntity.getAsync())) {
                if (twinChangesCollector != null) {
                    UUID statusId = Boolean.TRUE.equals(twinStatusTriggerEntity.getIncomingElseOutgoing()) ? dstStatusEntity.getId() : srcStatusEntity.getId();
                    twinChangesCollector.addPostponedTrigger(twinEntity.getId(), statusId, twinStatusTriggerEntity.getTwinTriggerId());
                } else {
                    log.warn("Async trigger execution skipped (no TwinChangesCollector): {}", twinStatusTriggerEntity.easyLog(EasyLoggable.Level.NORMAL));
                }
            } else {
                twinTriggerService.runTrigger(twinTriggerEntity, twinEntity, srcStatusEntity, dstStatusEntity);
            }
        }
    }

    public void loadTrigger(TwinStatusTriggerEntity src) throws ServiceException {
        loadTriggers(Collections.singleton(src));
    }

    public void loadTriggers(Collection<TwinStatusTriggerEntity> srcCollection) throws ServiceException {
        twinTriggerService.load(srcCollection,
                TwinStatusTriggerEntity::getId,
                TwinStatusTriggerEntity::getTwinTriggerId,
                TwinStatusTriggerEntity::getTwinTrigger,
                TwinStatusTriggerEntity::setTwinTrigger);
    }

    public void loadStatus(TwinStatusTriggerEntity src) throws ServiceException {
        loadStatuses(Collections.singleton(src));
    }

    public void loadStatuses(Collection<TwinStatusTriggerEntity> srcCollection) throws ServiceException {
        twinStatusService.load(srcCollection,
                TwinStatusTriggerEntity::getId,
                TwinStatusTriggerEntity::getTwinStatusId,
                TwinStatusTriggerEntity::getTwinStatus,
                TwinStatusTriggerEntity::setTwinStatus);
    }
}
