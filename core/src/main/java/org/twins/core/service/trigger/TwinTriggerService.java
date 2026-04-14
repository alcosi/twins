package org.twins.core.service.trigger;

import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.trigger.TwinTriggerCreate;
import org.twins.core.domain.trigger.TwinTriggerUpdate;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.featurer.trigger.TwinTrigger;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.cambium.common.util.UuidUtils;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.kit.Kit;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class TwinTriggerService extends EntitySecureFindServiceImpl<TwinTriggerEntity> {
    @Getter
    private final TwinTriggerRepository repository;
    private final AuthService authService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final TwinService twinService;
    private final TwinClassService twinClassService;
    @Lazy
    private final TwinTriggerTaskService twinTriggerTaskService;

    @Override
    public CrudRepository<TwinTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinTriggerEntity, UUID> entityGetIdFunction() {
        return TwinTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinTriggerFeaturerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinTriggerFeaturerId is not specified");
        }

        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }

    @Transactional(rollbackFor = Throwable.class)
    @LogExecutionTime(logIfTookMoreThenMs = 2000)
    public List<TwinTriggerEntity> createTriggers(List<TwinTriggerCreate> triggers) throws ServiceException {
        if (CollectionUtils.isEmpty(triggers)) {
            return Collections.emptyList();
        }

        ApiUser apiUser = authService.getApiUser();
        List<TwinTriggerEntity> triggersToSave = new ArrayList<>();

        for (TwinTriggerCreate trigger : triggers) {
            HashMap<String, String> triggerParams = trigger.getTriggerParams() != null
                    ? new HashMap<>(trigger.getTriggerParams())
                    : new HashMap<>();

            if (trigger.getTriggerFeaturerId() != null) {
                featurerService.checkValid(trigger.getTriggerFeaturerId(), triggerParams, TwinTrigger.class);
                featurerService.prepareForStore(trigger.getTriggerFeaturerId(), triggerParams);
            } else {
                throw new ServiceException(ErrorCodeCommon.FEATURER_IS_NULL);
            }

            TwinTriggerEntity triggerEntity = new TwinTriggerEntity()
                    .setDomainId(apiUser.getDomainId())
                    .setTwinTriggerFeaturerId(trigger.getTriggerFeaturerId())
                    .setTwinTriggerParam(triggerParams)
                    .setName(trigger.getName())
                    .setDescription(trigger.getDescription())
                    .setActive(trigger.getActive() != null ? trigger.getActive() : true);

            triggersToSave.add(triggerEntity);
        }

        return StreamSupport.stream(saveSafe(triggersToSave).spliterator(), false).toList();
    }

    public void loadTwinTriggerFeaturer(TwinTriggerEntity entity) {
        loadTwinTriggerFeaturer(List.of(entity));
    }

    public void loadTwinTriggerFeaturer(Collection<TwinTriggerEntity> entities) {
        featurerService.loadFeaturers(entities,
                TwinTriggerEntity::getId,
                TwinTriggerEntity::getTwinTriggerFeaturerId,
                TwinTriggerEntity::getTwinTriggerFeaturer,
                TwinTriggerEntity::setTwinTriggerFeaturer);
    }

    public void loadJobTwinClass(TwinTriggerEntity entity) throws ServiceException {
        loadJobTwinClass(List.of(entity));
    }

    public void loadJobTwinClass(Collection<TwinTriggerEntity> entities) throws ServiceException {
        twinClassService.load(entities,
                TwinTriggerEntity::getId,
                TwinTriggerEntity::getJobTwinClassId,
                TwinTriggerEntity::getJobTwinClass,
                TwinTriggerEntity::setJobTwinClass);
    }

    @Transactional(rollbackFor = Throwable.class)
    @LogExecutionTime(logIfTookMoreThenMs = 2000)
    public List<TwinTriggerEntity> updateTriggers(List<TwinTriggerUpdate> triggers) throws ServiceException {
        if (triggers == null || triggers.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<TwinTriggerEntity> changes = new ChangesHelperMulti<>();
        List<TwinTriggerEntity> allEntities = new ArrayList<>(triggers.size());

        Kit<TwinTriggerEntity, UUID> entitiesKit = findEntitiesSafe(triggers.stream().map(TwinTriggerUpdate::getId).toList());

        for (TwinTriggerUpdate trigger : triggers) {
            TwinTriggerEntity entity = entitiesKit.get(trigger.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByValue(trigger.getName(), entity,
                    TwinTriggerEntity::getName, TwinTriggerEntity::setName,
                    TwinTriggerEntity.Fields.name, changesHelper);
            updateEntityFieldByValue(trigger.getDescription(), entity,
                    TwinTriggerEntity::getDescription, TwinTriggerEntity::setDescription,
                    TwinTriggerEntity.Fields.description, changesHelper);
            updateEntityFieldByValue(trigger.getActive(), entity,
                    TwinTriggerEntity::getActive, TwinTriggerEntity::setActive,
                    TwinTriggerEntity.Fields.active, changesHelper);
            updateFieldTwinTriggerFeaturerId(entity, trigger.getTriggerFeaturerId(),
                    trigger.getTriggerParams() != null ? new HashMap<>(trigger.getTriggerParams()) : new HashMap<>(), changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    public void updateFieldTwinTriggerFeaturerId(TwinTriggerEntity dbTwinTriggerEntity, Integer newFeaturerId,
                                                 HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (newFeaturerParams.isEmpty())
                return; // nothing was changed
            else
                newFeaturerId = dbTwinTriggerEntity.getTwinTriggerFeaturerId(); // only params were changed
        }
        if (changesHelper.isChanged(TwinTriggerEntity.Fields.twinTriggerFeaturerId,
                dbTwinTriggerEntity.getTwinTriggerFeaturerId(), newFeaturerId)) {
            featurerService.checkValid(newFeaturerId, newFeaturerParams, TwinTrigger.class);
            dbTwinTriggerEntity.setTwinTriggerFeaturerId(newFeaturerId);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!newFeaturerParams.equals(dbTwinTriggerEntity.getTwinTriggerParam())) {
            changesHelper.add(TwinTriggerEntity.Fields.twinTriggerParam,
                    dbTwinTriggerEntity.getTwinTriggerParam(), newFeaturerParams);
            dbTwinTriggerEntity.setTwinTriggerParam(newFeaturerParams);
        }
    }

    /**
     * Runs trigger with optional job twin creation.
     * If trigger has jobTwinClassId set, creates a job twin and passes its ID in parameters.
     *
     * @param twinTriggerEntity the trigger to run
     * @param twinEntity        the twin that triggered the event
     * @param srcTwinStatus     source twin status
     * @param dstTwinStatus     destination twin status
     * @param twinTriggerTaskId optional task ID - if provided, execution is async; if null, creates task for sync execution
     */
    @Transactional(rollbackFor = Throwable.class)
    public void runTrigger(TwinTriggerEntity twinTriggerEntity, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID twinTriggerTaskId) throws ServiceException {
        // Create task for sync execution tracking if no taskId provided
        if (twinTriggerTaskId == null) {
            TwinTriggerTaskEntity syncTask = twinTriggerTaskService.addSyncTask(
                    twinEntity.getId(),
                    twinTriggerEntity.getId(),
                    srcTwinStatus != null ? srcTwinStatus.getId() : null
            );
            twinTriggerTaskId = syncTask.getId();
        }

        // Create job twin if jobTwinClassId is set
        UUID jobTwinId = null;
        UUID jobTwinClassId = twinTriggerEntity.getJobTwinClassId();
        if (jobTwinClassId != null) {
            TwinClassEntity twinClass = twinClassService.findEntitySafe(jobTwinClassId);
            TwinEntity jobTwinEntity = new TwinEntity()
                    .setId(twinTriggerTaskId == null ? UuidUtils.generate() : twinTriggerTaskId)
                    .setName(twinTriggerEntity.getName() == null ? "job" : twinTriggerEntity.getName())
                    .setTwinClassId(jobTwinClassId)
                    .setTwinClass(twinClass);

            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(jobTwinEntity);
            twinCreate.setCanTriggerAfterOperationFactory(false);
            twinCreate.setLauncher(TwinOperation.Launcher.trigger);
            twinCreate.setSketchMode(false);

            jobTwinId = twinService.createTwin(twinCreate).getCreatedTwin().getId();
            log.info("Created job twin[{}] for trigger[{}]", jobTwinId, twinTriggerEntity.getId());
        }

        // Get and run the featurer
        TwinTrigger trigger = featurerService.getFeaturer(twinTriggerEntity.getTwinTriggerFeaturerId(), TwinTrigger.class);
        Properties triggerProperties = featurerService.extractProperties(twinTriggerEntity.getTwinTriggerFeaturerId(), twinTriggerEntity.getTwinTriggerParam());
        trigger.run(triggerProperties, twinEntity, srcTwinStatus, dstTwinStatus, jobTwinId);
    }
}
