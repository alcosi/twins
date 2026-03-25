package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.trigger.TwinTriggerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionTriggerService extends EntitySecureFindServiceImpl<TwinflowTransitionTriggerEntity> {
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    private final TwinTriggerService twinTriggerService;
    private final AuthService authService;

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
        ApiUser apiUser = authService.getApiUser();
        loadTrigger(entity);
        if (entity.getTwinTrigger().getDomainId() != null) {
            return !entity.getTwinTrigger().getDomainId().equals(apiUser.getDomainId());
        }
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
    public List<TwinflowTransitionTriggerEntity> createTransitionTriggers(Collection<TwinflowTransitionTriggerEntity> transitionTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(transitionTriggers)) {
            return Collections.emptyList();
        }
        for (TwinflowTransitionTriggerEntity trigger : transitionTriggers) {
            trigger.setActive(true);
        }
        return IterableUtils.toList(saveSafe(transitionTriggers));
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinflowTransitionTriggerEntity> updateTransitionTriggers(Collection<TwinflowTransitionTriggerEntity> transitionTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(transitionTriggers)) {
            return Collections.emptyList();
        }
        ChangesHelperMulti<TwinflowTransitionTriggerEntity> changes = new ChangesHelperMulti<>();
        List<TwinflowTransitionTriggerEntity> allEntities = new ArrayList<>(transitionTriggers.size());

        for (TwinflowTransitionTriggerEntity trigger : transitionTriggers) {
            TwinflowTransitionTriggerEntity entity = findEntitySafe(trigger.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByEntity(trigger, entity, TwinflowTransitionTriggerEntity::getTwinflowTransitionId,
                    TwinflowTransitionTriggerEntity::setTwinflowTransitionId, TwinflowTransitionTriggerEntity.Fields.twinflowTransitionId, changesHelper);
            updateTwinTrigger(entity, trigger.getTwinTriggerId(), changesHelper);
            updateEntityFieldByEntity(trigger, entity, TwinflowTransitionTriggerEntity::getOrder,
                    TwinflowTransitionTriggerEntity::setOrder, TwinflowTransitionTriggerEntity.Fields.order, changesHelper);
            updateEntityFieldByEntity(trigger, entity, TwinflowTransitionTriggerEntity::getAsync,
                    TwinflowTransitionTriggerEntity::setAsync, TwinflowTransitionTriggerEntity.Fields.async, changesHelper);
            updateEntityFieldByEntity(trigger, entity, TwinflowTransitionTriggerEntity::getActive,
                    TwinflowTransitionTriggerEntity::setActive, TwinflowTransitionTriggerEntity.Fields.active, changesHelper);
            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        return allEntities;
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

    public void loadTrigger(TwinflowTransitionTriggerEntity src) throws ServiceException {
        loadTriggers(Collections.singleton(src));
    }

    public void loadTriggers(Collection<TwinflowTransitionTriggerEntity> srcCollection) throws ServiceException {
        twinTriggerService.load(srcCollection,
                TwinflowTransitionTriggerEntity::getId,
                TwinflowTransitionTriggerEntity::getTwinTriggerId,
                TwinflowTransitionTriggerEntity::getTwinTrigger,
                TwinflowTransitionTriggerEntity::setTwinTrigger);
    }
}
