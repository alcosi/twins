package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierRepository;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.HashMap;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class FactoryMultiplierService extends EntitySecureFindServiceImpl<TwinFactoryMultiplierEntity> {
    @Getter
    private final TwinFactoryMultiplierRepository repository;
    private final TwinClassService twinClassService;
    private final TwinFactoryService twinFactoryService;

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
        return checkDomainAccessDenied(entity.getTwinFactory().getDomainId(), entity.logNormal(), readPermissionCheckMode);
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
                    entity.setInputTwinClass(twinClassService.findEntitySafe(entity.getInputTwinClassId()));
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
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

    public void duplicateMultipliersForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory, KitGrouped<TwinFactoryMultiplierEntity, UUID, UUID> multipliersKit) throws ServiceException {
        List<TwinFactoryMultiplierEntity> multipliers = multipliersKit.getGrouped(fromFactory.getId());
        if (CollectionUtils.isEmpty(multipliers)) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryMultiplierEntity>();
        for (TwinFactoryMultiplierEntity originalMultiplier : multipliers) {
            TwinFactoryMultiplierEntity duplicateMultiplier = new TwinFactoryMultiplierEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setInputTwinClassId(originalMultiplier.getInputTwinClassId())
                    .setMultiplierFeaturerId(originalMultiplier.getMultiplierFeaturerId())
                    .setMultiplierParams(originalMultiplier.getMultiplierParams());
            entitiesForSave.add(duplicateMultiplier);
        }
        saveSafe(entitiesForSave);
    }

}
