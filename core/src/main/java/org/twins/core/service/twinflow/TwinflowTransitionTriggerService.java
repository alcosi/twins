package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerRepository;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerRepository;
import org.twins.core.service.trigger.TwinTriggerService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionTriggerService extends EntitySecureFindServiceImpl<TwinflowTransitionTriggerEntity> {
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    private final TwinTriggerService twinTriggerService;

    @Override
    public CrudRepository<TwinflowTransitionTriggerEntity, UUID> entityRepository() {
        return twinflowTransitionTriggerRepository;
    }

    @Override
    public Function<TwinflowTransitionTriggerEntity, UUID> entityGetIdFunction() {
        return TwinflowTransitionTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinflowTransitionId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " empty twinflowTransitionId");
        }
        if (entity.getTwinTriggerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " empty twinTriggerId");
        }

        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getTwinTrigger() == null || !entity.getTwinTriggerId().equals(entity.getTwinTrigger().getId())) {
                    entity.setTwinTrigger(twinTriggerService.findEntitySafe(entity.getTwinTriggerId()));
                }
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowTransitionTriggerEntity createTransitionTrigger(TwinflowTransitionTriggerEntity transitionTrigger) throws ServiceException {
        return saveSafe(transitionTrigger.setIsActive(true));
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowTransitionTriggerEntity updateTransitionTrigger(TwinflowTransitionTriggerEntity updateEntity) throws ServiceException {
        TwinflowTransitionTriggerEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinflowTransitionTriggerEntity::getTwinflowTransitionId,
                TwinflowTransitionTriggerEntity::setTwinflowTransitionId, TwinflowTransitionTriggerEntity.Fields.twinflowTransitionId, changesHelper);
        updateTwinTrigger(dbEntity, updateEntity.getTwinTriggerId(), changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinflowTransitionTriggerEntity::getOrder,
                TwinflowTransitionTriggerEntity::setOrder, TwinflowTransitionTriggerEntity.Fields.order, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinflowTransitionTriggerEntity::getAsync,
                TwinflowTransitionTriggerEntity::setAsync, TwinflowTransitionTriggerEntity.Fields.async, changesHelper);
        updateActive(dbEntity, updateEntity, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    private void updateTwinTrigger(TwinflowTransitionTriggerEntity dbEntity, UUID newTwinTriggerId, ChangesHelper changesHelper) throws ServiceException {
        if (newTwinTriggerId == null) {
            return;
        }
        if (changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.twinTriggerId, dbEntity.getTwinTriggerId(), newTwinTriggerId)) {
            TwinTriggerEntity newTwinTrigger = twinTriggerService.findEntitySafe(newTwinTriggerId);
            dbEntity
                    .setTwinTriggerId(newTwinTrigger.getId())
                    .setTwinTrigger(newTwinTrigger);
        }
    }

    private void updateActive(TwinflowTransitionTriggerEntity dbEntity, TwinflowTransitionTriggerEntity updateEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.isActive, dbEntity.getIsActive(), updateEntity.getIsActive()))
            return;
        dbEntity.setIsActive(updateEntity.getIsActive());
    }

    public void loadTrigger(TwinflowTransitionTriggerEntity src) throws ServiceException {
        loadTriggers(Collections.singleton(src));
    }

    public void loadTriggers(Collection<TwinflowTransitionTriggerEntity> srcCollection) throws ServiceException {
        for (TwinflowTransitionTriggerEntity entity : srcCollection) {
            if (entity.getTwinTriggerId() != null && entity.getTwinTrigger() == null) {
                entity.setTwinTrigger(twinTriggerService.findEntitySafe(entity.getTwinTriggerId()));
            }
        }
    }
}
