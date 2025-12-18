package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerRepository;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionTriggerService extends EntitySecureFindServiceImpl<TwinflowTransitionTriggerEntity> {
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    private final FeaturerService featurerService;

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
        if (entity.getTransitionTriggerFeaturerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " empty transitionTriggerFeaturerId");
        }

        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getTransitionTriggerFeaturer() == null || !entity.getTransitionTriggerFeaturerId().equals(entity.getTransitionTriggerFeaturer().getId())) {
                    entity.setTransitionTriggerFeaturer(featurerService.checkValid(entity.getTransitionTriggerFeaturerId(), entity.getTransitionTriggerParams(), TransitionTrigger.class));
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
        updateTransitionTriggerFeaturer(dbEntity, updateEntity.getTransitionTriggerFeaturerId(), updateEntity.getTransitionTriggerParams(), changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinflowTransitionTriggerEntity::getOrder,
                TwinflowTransitionTriggerEntity::setOrder, TwinflowTransitionTriggerEntity.Fields.order, changesHelper);
        updateActive(dbEntity, updateEntity, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    private void updateTransitionTriggerFeaturer(TwinflowTransitionTriggerEntity dbEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbEntity.getTransitionTriggerFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.transitionTriggerFeaturerId, dbEntity.getTransitionTriggerFeaturerId(), newFeaturerId)) {
            FeaturerEntity newTransitionTriggerFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, TransitionTrigger.class);
            dbEntity
                    .setTransitionTriggerFeaturerId(newTransitionTriggerFeaturer.getId())
                    .setTransitionTriggerFeaturer(newTransitionTriggerFeaturer);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbEntity.getTransitionTriggerParams(), newFeaturerParams)) {
            changesHelper.add(TwinflowTransitionTriggerEntity.Fields.transitionTriggerParams, dbEntity.getTransitionTriggerParams(), newFeaturerParams);
            dbEntity.setTransitionTriggerParams(newFeaturerParams);
        }
    }

    private void updateActive(TwinflowTransitionTriggerEntity dbEntity, TwinflowTransitionTriggerEntity updateEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.isActive, dbEntity.getIsActive(), updateEntity.getIsActive()))
            return;
        dbEntity.setIsActive(updateEntity.getIsActive());
    }

    public void loadTrigger(TwinflowTransitionTriggerEntity src) {
        loadTriggers(Collections.singleton(src));
    }

    public void loadTriggers(Collection<TwinflowTransitionTriggerEntity> srcCollection) {
        featurerService.loadFeaturers(srcCollection,
                TwinflowTransitionTriggerEntity::getId,
                TwinflowTransitionTriggerEntity::getTransitionTriggerFeaturerId,
                TwinflowTransitionTriggerEntity::getTransitionTriggerFeaturer,
                TwinflowTransitionTriggerEntity::setTransitionTriggerFeaturer);
    }
}
