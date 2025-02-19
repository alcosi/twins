package org.twins.core.service.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierRepository;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class FactoryMultiplierService extends EntitySecureFindServiceImpl<TwinFactoryMultiplierEntity> {
    @Getter
    private final TwinFactoryMultiplierRepository repository;
    private final TwinClassService twinClassService;
    private final AuthService authService;
    private final FeaturerService featurerService;
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
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getTwinFactory().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryMultiplierEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getInputTwinClass() == null || !entity.getInputTwinClass().getId().equals(entity.getInputTwinClassId()))
                    entity.setInputTwinClass(twinClassService.findEntitySafe(entity.getInputTwinClassId()));
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
                if (entity.getMultiplierFeaturer() == null || !(entity.getMultiplierFeaturer().getId() == (entity.getMultiplierFeaturerId())))
                    entity.setMultiplierFeaturer(featurerService.checkValid(entity.getMultiplierFeaturerId(), entity.getMultiplierParams(), Multiplier.class));
        }
        return true;
    }

    public TwinFactoryMultiplierEntity createFactoryMultiplier(TwinFactoryMultiplierEntity entity) throws ServiceException {
        entity.setId(UUID.randomUUID());
        validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
        return repository.save(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryMultiplierEntity updateFactoryMultiplier(TwinFactoryMultiplierEntity multiplierUpdate) throws ServiceException {
        TwinFactoryMultiplierEntity dbMultiplierEntity = findEntitySafe(multiplierUpdate.getId());
        multiplierUpdate.setId(dbMultiplierEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateInputTwinClassId(dbMultiplierEntity, multiplierUpdate.getInputTwinClassId(), changesHelper);
        updateMultiplierFeaturerId(dbMultiplierEntity, multiplierUpdate.getMultiplierFeaturerId(), changesHelper);
        updateMultiplierParams(dbMultiplierEntity, multiplierUpdate.getMultiplierParams(), changesHelper);
        updateActive(dbMultiplierEntity, multiplierUpdate.getActive(), changesHelper);
        updateDescription(dbMultiplierEntity, multiplierUpdate.getDescription(), changesHelper);

        if (changesHelper.hasChanges()) {
            validateEntity(dbMultiplierEntity, EntitySmartService.EntityValidateMode.beforeSave);
            dbMultiplierEntity = entitySmartService.saveAndLogChanges(dbMultiplierEntity, repository, changesHelper);
        }
        return dbMultiplierEntity;
    }

    private void updateInputTwinClassId(TwinFactoryMultiplierEntity dbMultiplierEntity, UUID newInputTwinClassId,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryMultiplierEntity.Fields.inputTwinClassId, dbMultiplierEntity.getInputTwinClassId(), newInputTwinClassId))
            return;
        dbMultiplierEntity.setInputTwinClassId(newInputTwinClassId);
    }

    private void updateMultiplierFeaturerId(TwinFactoryMultiplierEntity dbMultiplierEntity, Integer newMultiplierFeaturerId,
                                            ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryMultiplierEntity.Fields.multiplierFeaturerId, dbMultiplierEntity.getMultiplierFeaturerId(), newMultiplierFeaturerId))
            return;
        dbMultiplierEntity.setMultiplierFeaturerId(newMultiplierFeaturerId);
    }

    private void updateMultiplierParams(TwinFactoryMultiplierEntity dbMultiplierEntity, HashMap<String, String> newMultiplierParams,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryMultiplierEntity.Fields.multiplierParams, dbMultiplierEntity.getMultiplierParams(), newMultiplierParams))
            return;
        dbMultiplierEntity.setMultiplierParams(newMultiplierParams);
    }

    private void updateActive(TwinFactoryMultiplierEntity dbMultiplierEntity, boolean newActive,
                              ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryMultiplierEntity.Fields.active, dbMultiplierEntity.getActive(), newActive))
            return;
        dbMultiplierEntity.setActive(newActive);
    }

    private void updateDescription(TwinFactoryMultiplierEntity dbMultiplierEntity, String newDescription,
                                   ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryMultiplierEntity.Fields.description, dbMultiplierEntity.getDescription(), newDescription))
            return;
        dbMultiplierEntity.setDescription(newDescription);
    }
}
