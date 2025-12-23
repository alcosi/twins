package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryPipelineService extends EntitySecureFindServiceImpl<TwinFactoryPipelineEntity> {
    @Getter
    private final TwinFactoryPipelineRepository repository;
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinFactoryService twinFactoryService;
    private final FactoryConditionSetService factoryConditionSetService;
    private final TwinStatusService twinStatusService;
    private final TwinService twinService;

    @Override
    public CrudRepository<TwinFactoryPipelineEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryPipelineEntity, UUID> entityGetIdFunction() {
        return TwinFactoryPipelineEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryPipelineEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getTwinFactory().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryPipelineEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryId");
        if (entity.getInputTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty inputTwinClassId");


        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getInputTwinClass() == null || !entity.getInputTwinClass().getId().equals(entity.getInputTwinClassId()))
                    entity.setInputTwinClass(twinClassService.findEntitySafe(entity.getInputTwinClassId()));
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
                if (entity.getNextTwinFactoryId() != null && (entity.getNextTwinFactory() == null || !entity.getNextTwinFactory().getId().equals(entity.getNextTwinFactoryId())))
                    entity.setNextTwinFactory(twinFactoryService.findEntitySafe(entity.getNextTwinFactoryId()));
                if (entity.getTwinFactoryConditionSetId() != null && (entity.getConditionSet() == null || !entity.getConditionSet().getId().equals(entity.getTwinFactoryConditionSetId())))
                    entity.setConditionSet(factoryConditionSetService.findEntitySafe(entity.getTwinFactoryConditionSetId()));
                if (entity.getOutputTwinStatusId() != null && (entity.getOutputTwinStatus() == null || !entity.getOutputTwinStatus().getId().equals(entity.getOutputTwinStatusId())))
                    entity.setOutputTwinStatus(twinStatusService.findEntitySafe(entity.getOutputTwinStatusId()));
                if (entity.getTemplateTwinId() != null && (entity.getTemplateTwin() == null || !entity.getTemplateTwin().getId().equals(entity.getTemplateTwinId())))
                    entity.setTemplateTwin(twinService.findEntitySafe(entity.getTemplateTwinId()));
                if (entity.getNextTwinFactoryLimitScope() == null)
                    entity.setNextTwinFactoryLimitScope(true);
        }
        return true;
    }

    public TwinFactoryPipelineEntity createFactoryPipeline(TwinFactoryPipelineEntity entity) throws ServiceException {
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryPipelineEntity updateFactoryPipeline(TwinFactoryPipelineEntity entity) throws ServiceException {
        TwinFactoryPipelineEntity dbEntity = findEntitySafe(entity.getId());
        entity.setId(dbEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getInputTwinClassId,
                TwinFactoryPipelineEntity::setInputTwinClassId, TwinFactoryPipelineEntity.Fields.inputTwinClassId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getTwinFactoryConditionSetId,
                TwinFactoryPipelineEntity::setTwinFactoryConditionSetId, TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getTwinFactoryConditionInvert,
                TwinFactoryPipelineEntity::setTwinFactoryConditionInvert, TwinFactoryPipelineEntity.Fields.twinFactoryConditionInvert, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getActive,
                TwinFactoryPipelineEntity::setActive, TwinFactoryPipelineEntity.Fields.active, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getOutputTwinStatusId,
                TwinFactoryPipelineEntity::setOutputTwinStatusId, TwinFactoryPipelineEntity.Fields.outputTwinStatusId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getNextTwinFactoryId,
                TwinFactoryPipelineEntity::setNextTwinFactoryId, TwinFactoryPipelineEntity.Fields.nextTwinFactoryId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getTemplateTwinId,
                TwinFactoryPipelineEntity::setTemplateTwinId, TwinFactoryPipelineEntity.Fields.templateTwinId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineEntity::getDescription,
                TwinFactoryPipelineEntity::setDescription, TwinFactoryPipelineEntity.Fields.description, changesHelper);

        return updateSafe(dbEntity, changesHelper);
    }
}
