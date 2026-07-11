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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierRepository;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class FactoryMultiplierService extends EntitySecureFindServiceImpl<TwinFactoryMultiplierEntity> {
    @Getter
    private final TwinFactoryMultiplierRepository repository;
    private final TwinClassService twinClassService;
    @Lazy
    private final FactoryService factoryService;

    @Override
    public CrudRepository<TwinFactoryMultiplierEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryMultiplierEntity, UUID> entityGetIdFunction() {
        return TwinFactoryMultiplierEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryMultiplierEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
//        return checkDomainAccessDenied(entity.getTwinFactory().getDomainId(), entity.logNormal(), readPermissionCheckMode);
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryMultiplierEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if(entity.getInputTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty inputTwinClassId");
        if(entity.getTwinFactoryId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryId");
        if(entity.getMultiplierFeaturerId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty multiplierFeaturerId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getInputTwinClass() == null || !entity.getInputTwinClass().getId().equals(entity.getInputTwinClassId()))
                    loadInputTwinClass(entity);
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    loadTwinFactory(entity);
                validateAndPrepareFeaturer(entity.getMultiplierFeaturerId(), entity.getMultiplierParams(), Multiplier.class);
        }
        return true;
    }

    public TwinFactoryMultiplierEntity createFactoryMultiplier(TwinFactoryMultiplierEntity entity) throws ServiceException {
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryMultiplierEntity updateFactoryMultiplier(TwinFactoryMultiplierEntity multiplierUpdate) throws ServiceException {
        TwinFactoryMultiplierEntity dbMultiplierEntity = findEntitySafe(multiplierUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(multiplierUpdate, dbMultiplierEntity, TwinFactoryMultiplierEntity::getInputTwinClassId,
                TwinFactoryMultiplierEntity::setInputTwinClassId, TwinFactoryMultiplierEntity.Fields.inputTwinClassId, changesHelper);
        updateEntityFieldByEntity(multiplierUpdate, dbMultiplierEntity, TwinFactoryMultiplierEntity::getDescription,
                TwinFactoryMultiplierEntity::setDescription, TwinFactoryMultiplierEntity.Fields.description, changesHelper);
        updateEntityFieldByEntity(multiplierUpdate, dbMultiplierEntity, TwinFactoryMultiplierEntity::getActive,
                TwinFactoryMultiplierEntity::setActive, TwinFactoryMultiplierEntity.Fields.active, changesHelper);

        updateMultiplierFeaturerId(dbMultiplierEntity, multiplierUpdate.getMultiplierFeaturerId(), multiplierUpdate.getMultiplierParams(), changesHelper);

        return updateSafe(dbMultiplierEntity, changesHelper);
    }


    public void updateMultiplierFeaturerId(TwinFactoryMultiplierEntity dbMultiplierEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        updateEntityFeaturerField(dbMultiplierEntity, newFeaturerId, newFeaturerParams,
                TwinFactoryMultiplierEntity::getMultiplierFeaturerId, TwinFactoryMultiplierEntity::setMultiplierFeaturerId,
                TwinFactoryMultiplierEntity::getMultiplierParams, TwinFactoryMultiplierEntity::setMultiplierParams,
                TwinFactoryMultiplierEntity.Fields.multiplierFeaturerId, TwinFactoryMultiplierEntity.Fields.multiplierParams,
                Multiplier.class, changesHelper);
    }

    public List<TwinFactoryMultiplierEntity> findByTwinFactoryIdIn(Collection<UUID> factoryIds) {
        return repository.findByTwinFactoryIdIn(factoryIds);
    }

    public void loadFactoryMultipliers(TwinFactoryEntity factory) {
        loadFactoryMultipliers(Collections.singletonList(factory));
    }

    public void loadFactoryMultipliers(Collection<TwinFactoryEntity> factories) {
        loadKit(
                factories,
                TwinFactoryEntity::getId,
                TwinFactoryEntity::getTwinFactoryMultiplierKit,
                TwinFactoryEntity::setTwinFactoryMultiplierKit,
                repository::findByTwinFactoryIdIn,
                TwinFactoryMultiplierEntity::getId,
                TwinFactoryMultiplierEntity::getTwinFactoryId);
    }

    public void loadTwinFactory(TwinFactoryMultiplierEntity multiplier) throws ServiceException {
        loadTwinFactory(Collections.singleton(multiplier));
    }

    public void loadTwinFactory(Collection<TwinFactoryMultiplierEntity> multipliers) throws ServiceException {
        factoryService.load(multipliers,
                TwinFactoryMultiplierEntity::getTwinFactoryId,
                TwinFactoryMultiplierEntity::getTwinFactory,
                TwinFactoryMultiplierEntity::setTwinFactory);
    }

    public void loadInputTwinClass(TwinFactoryMultiplierEntity multiplier) throws ServiceException {
        loadInputTwinClass(Collections.singleton(multiplier));
    }

    public void loadInputTwinClass(Collection<TwinFactoryMultiplierEntity> multipliers) throws ServiceException {
        twinClassService.load(multipliers,
                TwinFactoryMultiplierEntity::getInputTwinClassId,
                TwinFactoryMultiplierEntity::getInputTwinClass,
                TwinFactoryMultiplierEntity::setInputTwinClass);
    }
}
