package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.domain.transition.TransitionContextBatch;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionTriggerService extends EntitySecureFindServiceImpl<TwinflowTransitionTriggerEntity> {
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    private final TwinflowTransitionTriggerTaskRepository twinflowTransitionTriggerTaskRepository;
    private final FeaturerService featurerService;
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
        return saveSafe(transitionTrigger.setActive(true));
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
        if (!changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.isActive, dbEntity.isActive(), updateEntity.isActive()))
            return;
        dbEntity.setActive(updateEntity.isActive());
    }

    @Transactional
    public void runTriggers(TransitionContextBatch transitionContextBatch) throws ServiceException {
        loadTriggers(transitionContextBatch.getAll().stream().map(TransitionContext::getTransitionEntity).toList());
        for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
            TwinflowTransitionEntity transitionEntity = transitionContext.getTransitionEntity();
            //todo run status input/output triggers
            List<TwinflowTransitionTriggerTaskEntity> twinflowTransitionTriggerTaskList = new ArrayList<>();
            for (TwinEntity targetTwin : transitionContext.getTargetTwinList().values()) {
                for (TwinflowTransitionTriggerEntity triggerEntity : transitionEntity.getTriggersKit()) {
                    if (!triggerEntity.isActive()) {
                        log.info("{} will not be triggered, since it is inactive", triggerEntity.logDetailed());
                        continue;
                    }
                    log.info("{} will be triggered", triggerEntity.logDetailed());
                    //if async run it by TransitionTriggerTask (async)
                    if (triggerEntity.isAsync()) {
                        twinflowTransitionTriggerTaskList.add(new TwinflowTransitionTriggerTaskEntity()
                                .setTwinflowTransitionTriggerId(triggerEntity.getId())
                                .setTwinId(targetTwin.getId())
                                .setSrcTwinStatus(transitionEntity.getSrcTwinStatus())
                        );
                    } else {
                        TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
                        transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), targetTwin, transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
                    }
                }
            }
            addTasks(twinflowTransitionTriggerTaskList);
        }
    }

    public Kit<TwinflowTransitionTriggerEntity, UUID> loadTriggers(TwinflowTransitionEntity transition) {
        if (transition.getTriggersKit() != null)
            return transition.getTriggersKit();
        List<TwinflowTransitionTriggerEntity> triggers = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transition.getId());
        transition.setTriggersKit(new Kit<>(triggers, TwinflowTransitionTriggerEntity::getId));
        return transition.getTriggersKit();
    }

    public void loadTriggers(Collection<TwinflowTransitionEntity> transitions) {
        Map<UUID, TwinflowTransitionEntity> needLoad = new HashMap<>();
        for (TwinflowTransitionEntity transition : transitions)
            if (transition.getTriggersKit() == null)
                needLoad.put(transition.getId(), transition);
        if (needLoad.isEmpty()) return;
        KitGrouped<TwinflowTransitionTriggerEntity, UUID, UUID> triggersKit = new KitGrouped<>(
                twinflowTransitionTriggerRepository.findAllByTwinflowTransitionIdInOrderByOrder(needLoad.keySet()), TwinflowTransitionTriggerEntity::getId, TwinflowTransitionTriggerEntity::getTwinflowTransitionId);
        for (Map.Entry<UUID, TwinflowTransitionEntity> entry : needLoad.entrySet())
            entry.getValue().setTriggersKit(new Kit<>(triggersKit.getGrouped(entry.getKey()), TwinflowTransitionTriggerEntity::getId));
    }

    public void addTasks(Collection<TwinflowTransitionTriggerTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<TwinflowTransitionTriggerTaskEntity> twinflowTransitionTriggerTaskList = new ArrayList<>();
        for (var task : tasks) {
            task
                    .setRequestId(apiUser.getRequestId()) //we have uniq index on twinId + requestId to avoid conflict runs
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId())
                    .setBusinessAccountId(apiUser.getBusinessAccountId());
            if (task.getStatusId() == null)
                task.setStatusId(TwinflowTransitionTriggerStatus.NEED_START);
            twinflowTransitionTriggerTaskList.add(task);
        }
        entitySmartService.saveAllAndLog(twinflowTransitionTriggerTaskList, twinflowTransitionTriggerTaskRepository);
    }
}
