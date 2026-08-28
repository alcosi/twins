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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.factory.TwinFactoryEraserRepository;
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
public class FactoryEraserService extends EntitySecureFindServiceImpl<TwinFactoryEraserEntity> {
    @Getter
    private final TwinFactoryEraserRepository repository;
    @Lazy
    private final FactoryService factoryService;
    private final TwinClassService twinClassService;
    @Lazy
    private final FactoryConditionSetService factoryConditionSetService;

    @Override
    public CrudRepository<TwinFactoryEraserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryEraserEntity, UUID> entityGetIdFunction() {
        return TwinFactoryEraserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryEraserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        // return checkDomainAccessDenied(entity.getTwinFactory().getDomainId(), entity.logNormal(), readPermissionCheckMode);
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryEraserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryId");
        if (entity.getInputTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty inputTwinClassId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    loadTwinFactory(entity);
                if (entity.getInputTwinClass() == null || !entity.getInputTwinClass().getId().equals(entity.getInputTwinClassId()))
                    loadInputTwinClass(entity);
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEraserEntity createEraser(TwinFactoryEraserEntity createEntity) throws ServiceException {
        return saveSafe(createEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEraserEntity updateEraser(TwinFactoryEraserEntity updateEntity) throws ServiceException {
        TwinFactoryEraserEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getInputTwinClassId, TwinFactoryEraserEntity::setInputTwinClassId,
                TwinFactoryEraserEntity.Fields.inputTwinClassId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getTwinFactoryConditionSetId, TwinFactoryEraserEntity::setTwinFactoryConditionSetId,
                TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getTwinFactoryConditionInvert, TwinFactoryEraserEntity::setTwinFactoryConditionInvert,
                TwinFactoryEraserEntity.Fields.twinFactoryConditionInvert, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getActive, TwinFactoryEraserEntity::setActive,
                TwinFactoryEraserEntity.Fields.active, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getDescription, TwinFactoryEraserEntity::setDescription,
                TwinFactoryEraserEntity.Fields.description, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, TwinFactoryEraserEntity::getEraserAction, TwinFactoryEraserEntity::setEraserAction,
                TwinFactoryEraserEntity.Fields.eraserAction, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deleteEraser(UUID id) throws ServiceException {
        deleteSafe(id);
    }

    public List<TwinFactoryEraserEntity> findByTwinFactoryIdIn(Collection<UUID> factoryIds) {
        return repository.findByTwinFactoryIdIn(factoryIds);
    }

    public void loadFactoryErasers(TwinFactoryEntity factory) {
        loadFactoryErasers(Collections.singletonList(factory));
    }

    public void loadFactoryErasers(Collection<TwinFactoryEntity> factories) {
        loadKit(
                factories,
                TwinFactoryEntity::getId,
                TwinFactoryEntity::getTwinFactoryEraserKit,
                TwinFactoryEntity::setTwinFactoryEraserKit,
                repository::findByTwinFactoryIdIn,
                TwinFactoryEraserEntity::getId,
                TwinFactoryEraserEntity::getTwinFactoryId,
                TwinFactoryEraserEntity::setTwinFactory);
    }

    public void loadConditionSet(TwinFactoryEraserEntity eraser) throws ServiceException {
        loadConditionSet(Collections.singleton(eraser));
    }

    public void loadConditionSet(Collection<TwinFactoryEraserEntity> erasers) throws ServiceException {
        factoryConditionSetService.load(erasers,
                TwinFactoryEraserEntity::getTwinFactoryConditionSetId,
                TwinFactoryEraserEntity::getTwinFactoryConditionSet,
                TwinFactoryEraserEntity::setTwinFactoryConditionSet);
    }

    public void loadTwinFactory(TwinFactoryEraserEntity eraser) throws ServiceException {
        loadTwinFactory(Collections.singleton(eraser));
    }

    public void loadTwinFactory(Collection<TwinFactoryEraserEntity> erasers) throws ServiceException {
        factoryService.load(erasers,
                TwinFactoryEraserEntity::getTwinFactoryId,
                TwinFactoryEraserEntity::getTwinFactory,
                TwinFactoryEraserEntity::setTwinFactory);
    }

    public void loadInputTwinClass(TwinFactoryEraserEntity eraser) throws ServiceException {
        loadInputTwinClass(Collections.singleton(eraser));
    }

    public void loadInputTwinClass(Collection<TwinFactoryEraserEntity> erasers) throws ServiceException {
        twinClassService.load(erasers,
                TwinFactoryEraserEntity::getInputTwinClassId,
                TwinFactoryEraserEntity::getInputTwinClass,
                TwinFactoryEraserEntity::setInputTwinClass);
    }
}
