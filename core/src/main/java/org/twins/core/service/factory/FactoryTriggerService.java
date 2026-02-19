package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dao.trigger.TwinFactoryTriggerRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.ArrayList;
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
public class FactoryTriggerService extends EntitySecureFindServiceImpl<TwinFactoryTriggerEntity> {
    private final TwinFactoryTriggerRepository repository;
    private final TwinFactoryService twinFactoryService;
    private final AuthService authService;

    @Override
    public CrudRepository<TwinFactoryTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryTriggerEntity, UUID> entityGetIdFunction() {
        return TwinFactoryTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return entity.getTwinFactory().getDomainId() != null && !entity.getTwinFactory().getDomainId().equals(apiUser.getDomainId());
    }

    @Override
    public boolean validateEntity(TwinFactoryTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinFactoryId is not specified");
        }
        if (entity.getInputTwinClassId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " inputTwinClassId is not specified");
        }
        if (entity.getTwinTriggerId() == null) {
            return logErrorAndReturnFalse(entity.logDetailed() + " twinTriggerId is not specified");
        }
        if (entity.getTwinFactory() == null) {
            entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
        }
        if (twinFactoryService.isEntityReadDenied(entity.getTwinFactory(), EntitySmartService.ReadPermissionCheckMode.none)) {
            return logErrorAndReturnFalse(entity.logDetailed() + " factory domain check failed");
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinFactoryTriggerEntity> createFactoryTriggers(Collection<TwinFactoryTriggerEntity> factoryTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(factoryTriggers)) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(saveSafe(factoryTriggers).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinFactoryTriggerEntity> updateFactoryTriggers(Collection<TwinFactoryTriggerEntity> factoryTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(factoryTriggers)) {
            return Collections.emptyList();
        }
        ChangesHelperMulti<TwinFactoryTriggerEntity> changes = new ChangesHelperMulti<>();
        List<TwinFactoryTriggerEntity> allEntities = new ArrayList<>(factoryTriggers.size());

        for (TwinFactoryTriggerEntity trigger : factoryTriggers) {
            TwinFactoryTriggerEntity entity = findEntitySafe(trigger.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValue(trigger.getTwinFactoryId(), entity,
                    TwinFactoryTriggerEntity::getTwinFactoryId, TwinFactoryTriggerEntity::setTwinFactoryId,
                    TwinFactoryTriggerEntity.Fields.twinFactoryId, changesHelper);
            updateEntityFieldByValue(trigger.getInputTwinClassId(), entity,
                    TwinFactoryTriggerEntity::getInputTwinClassId, TwinFactoryTriggerEntity::setInputTwinClassId,
                    TwinFactoryTriggerEntity.Fields.inputTwinClassId, changesHelper);
            updateEntityFieldByValue(trigger.getTwinFactoryConditionSetId(), entity,
                    TwinFactoryTriggerEntity::getTwinFactoryConditionSetId, TwinFactoryTriggerEntity::setTwinFactoryConditionSetId,
                    TwinFactoryTriggerEntity.Fields.twinFactoryConditionSetId, changesHelper);
            updateEntityFieldByValue(trigger.getTwinFactoryConditionInvert(), entity,
                    TwinFactoryTriggerEntity::getTwinFactoryConditionInvert, TwinFactoryTriggerEntity::setTwinFactoryConditionInvert,
                    TwinFactoryTriggerEntity.Fields.twinFactoryConditionInvert, changesHelper);
            updateEntityFieldByValue(trigger.getActive(), entity,
                    TwinFactoryTriggerEntity::getActive, TwinFactoryTriggerEntity::setActive,
                    TwinFactoryTriggerEntity.Fields.active, changesHelper);
            updateEntityFieldByValue(trigger.getDescription(), entity,
                    TwinFactoryTriggerEntity::getDescription, TwinFactoryTriggerEntity::setDescription,
                    TwinFactoryTriggerEntity.Fields.description, changesHelper);
            updateEntityFieldByValue(trigger.getTwinTriggerId(), entity,
                    TwinFactoryTriggerEntity::getTwinTriggerId, TwinFactoryTriggerEntity::setTwinTriggerId,
                    TwinFactoryTriggerEntity.Fields.twinTriggerId, changesHelper);
            updateEntityFieldByValue(trigger.getAsync(), entity,
                    TwinFactoryTriggerEntity::getAsync, TwinFactoryTriggerEntity::setAsync,
                    TwinFactoryTriggerEntity.Fields.async, changesHelper);
            changes.add(entity, changesHelper);
        }

        updateSafe(changes);
        return allEntities;
    }
}
