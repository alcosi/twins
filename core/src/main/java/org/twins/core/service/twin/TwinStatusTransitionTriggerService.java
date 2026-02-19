package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class TwinStatusTransitionTriggerService extends EntitySecureFindServiceImpl<TwinStatusTransitionTriggerEntity> {
    private final TwinStatusTransitionTriggerRepository repository;

    @Override
    public CrudRepository<TwinStatusTransitionTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinStatusTransitionTriggerEntity, UUID> entityGetIdFunction() {
        return TwinStatusTransitionTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatusTransitionTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatusTransitionTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinStatusId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinStatusId is not specified");
        }
        if (entity.getType() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " type is not specified");
        }
        if (entity.getTwinTriggerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinTriggerId is not specified");
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinStatusTransitionTriggerEntity> createStatusTransitionTriggers(Collection<TwinStatusTransitionTriggerEntity> statusTransitionTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(statusTransitionTriggers)) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(saveSafe(statusTransitionTriggers).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinStatusTransitionTriggerEntity> updateStatusTransitionTriggers(Collection<TwinStatusTransitionTriggerEntity> statusTransitionTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(statusTransitionTriggers)) {
            return Collections.emptyList();
        }
        org.cambium.common.util.ChangesHelperMulti<TwinStatusTransitionTriggerEntity> changes = new org.cambium.common.util.ChangesHelperMulti<>();
        List<TwinStatusTransitionTriggerEntity> allEntities = new java.util.ArrayList<>(statusTransitionTriggers.size());

        for (TwinStatusTransitionTriggerEntity trigger : statusTransitionTriggers) {
            TwinStatusTransitionTriggerEntity entity = findEntitySafe(trigger.getId());
            allEntities.add(entity);

            org.cambium.common.util.ChangesHelper changesHelper = new org.cambium.common.util.ChangesHelper();
            updateEntityFieldByValue(trigger.getTwinStatusId(), entity,
                    TwinStatusTransitionTriggerEntity::getTwinStatusId, TwinStatusTransitionTriggerEntity::setTwinStatusId,
                    TwinStatusTransitionTriggerEntity.Fields.twinStatusId, changesHelper);
            updateEntityFieldByValue(trigger.getType(), entity,
                    TwinStatusTransitionTriggerEntity::getType, TwinStatusTransitionTriggerEntity::setType,
                    TwinStatusTransitionTriggerEntity.Fields.type, changesHelper);
            updateEntityFieldByValue(trigger.getOrder(), entity,
                    TwinStatusTransitionTriggerEntity::getOrder, TwinStatusTransitionTriggerEntity::setOrder,
                    TwinStatusTransitionTriggerEntity.Fields.order, changesHelper);
            updateEntityFieldByValue(trigger.getTwinTriggerId(), entity,
                    TwinStatusTransitionTriggerEntity::getTwinTriggerId, TwinStatusTransitionTriggerEntity::setTwinTriggerId,
                    TwinStatusTransitionTriggerEntity.Fields.twinTriggerId, changesHelper);
            updateEntityFieldByValue(trigger.getAsync(), entity,
                    TwinStatusTransitionTriggerEntity::getAsync, TwinStatusTransitionTriggerEntity::setAsync,
                    TwinStatusTransitionTriggerEntity.Fields.async, changesHelper);
            updateEntityFieldByValue(trigger.getActive(), entity,
                    TwinStatusTransitionTriggerEntity::getActive, TwinStatusTransitionTriggerEntity::setActive,
                    TwinStatusTransitionTriggerEntity.Fields.active, changesHelper);
            changes.add(entity, changesHelper);
        }

        updateSafe(changes);
        return allEntities;
    }
}
