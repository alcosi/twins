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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    @Lazy
    private final FactoryService factoryService;
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
//        return checkDomainAccessDenied(entity.getTwinFactory().getDomainId(), entity.logNormal(), readPermissionCheckMode);
        return false;
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
                    loadInputTwinClass(entity);
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    loadTwinFactory(entity);
                if (entity.getNextTwinFactoryId() != null && (entity.getNextTwinFactory() == null || !entity.getNextTwinFactory().getId().equals(entity.getNextTwinFactoryId())))
                    loadNextTwinFactory(entity);
                if (entity.getTwinFactoryConditionSetId() != null && (entity.getConditionSet() == null || !entity.getConditionSet().getId().equals(entity.getTwinFactoryConditionSetId())))
                    loadConditionSet(entity);
                if (entity.getOutputTwinStatusId() != null && (entity.getOutputTwinStatus() == null || !entity.getOutputTwinStatus().getId().equals(entity.getOutputTwinStatusId())))
                    loadOutputTwinStatus(entity);
                if (entity.getTemplateTwinId() != null && (entity.getTemplateTwin() == null || !entity.getTemplateTwin().getId().equals(entity.getTemplateTwinId())))
                    loadTemplateTwin(entity);
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

    public List<TwinFactoryPipelineEntity> findByTwinFactoryIdIn(Collection<UUID> factoryIds) {
        return repository.findByTwinFactoryIdIn(factoryIds);
    }

    public void loadFactoryPipelines(TwinFactoryEntity factory) {
        loadFactoryPipelines(Collections.singletonList(factory));
    }

    public void loadFactoryPipelines(Collection<TwinFactoryEntity> factories) {
        loadKit(
                factories,
                TwinFactoryEntity::getId,
                TwinFactoryEntity::getTwinFactoryPipelineKit,
                TwinFactoryEntity::setTwinFactoryPipelineKit,
                repository::findByTwinFactoryIdIn,
                TwinFactoryPipelineEntity::getId,
                TwinFactoryPipelineEntity::getTwinFactoryId);
    }

    public void loadConditionSet(TwinFactoryPipelineEntity src) throws ServiceException {
        loadConditionSet(Collections.singletonList(src));
    }

    public void loadConditionSet(Collection<TwinFactoryPipelineEntity> srcCollection) throws ServiceException {
        factoryConditionSetService.load(srcCollection,
                TwinFactoryPipelineEntity::getTwinFactoryConditionSetId,
                TwinFactoryPipelineEntity::getConditionSet,
                TwinFactoryPipelineEntity::setConditionSet);
    }

    public void loadTwinFactory(TwinFactoryPipelineEntity src) throws ServiceException {
        loadTwinFactory(Collections.singleton(src));
    }

    public void loadTwinFactory(Collection<TwinFactoryPipelineEntity> srcCollection) throws ServiceException {
        factoryService.load(srcCollection,
                TwinFactoryPipelineEntity::getTwinFactoryId,
                TwinFactoryPipelineEntity::getTwinFactory,
                TwinFactoryPipelineEntity::setTwinFactory);
    }

    public void loadNextTwinFactory(TwinFactoryPipelineEntity src) throws ServiceException {
        loadNextTwinFactory(Collections.singleton(src));
    }

    public void loadNextTwinFactory(Collection<TwinFactoryPipelineEntity> srcCollection) throws ServiceException {
        factoryService.load(srcCollection,
                TwinFactoryPipelineEntity::getNextTwinFactoryId,
                TwinFactoryPipelineEntity::getNextTwinFactory,
                TwinFactoryPipelineEntity::setNextTwinFactory);
    }

    public void loadInputTwinClass(TwinFactoryPipelineEntity src) throws ServiceException {
        loadInputTwinClass(Collections.singleton(src));
    }

    public void loadInputTwinClass(Collection<TwinFactoryPipelineEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                TwinFactoryPipelineEntity::getInputTwinClassId,
                TwinFactoryPipelineEntity::getInputTwinClass,
                TwinFactoryPipelineEntity::setInputTwinClass);
    }

    public void loadOutputTwinStatus(TwinFactoryPipelineEntity src) throws ServiceException {
        loadOutputTwinStatus(Collections.singleton(src));
    }

    public void loadOutputTwinStatus(Collection<TwinFactoryPipelineEntity> srcCollection) throws ServiceException {
        twinStatusService.load(srcCollection,
                TwinFactoryPipelineEntity::getOutputTwinStatusId,
                TwinFactoryPipelineEntity::getOutputTwinStatus,
                TwinFactoryPipelineEntity::setOutputTwinStatus);
    }

    public void loadTemplateTwin(TwinFactoryPipelineEntity src) throws ServiceException {
        loadTemplateTwin(Collections.singleton(src));
    }

    public void loadTemplateTwin(Collection<TwinFactoryPipelineEntity> srcCollection) throws ServiceException {
        twinService.load(srcCollection,
                TwinFactoryPipelineEntity::getTemplateTwinId,
                TwinFactoryPipelineEntity::getTemplateTwin,
                TwinFactoryPipelineEntity::setTemplateTwin);
    }
}
