package org.twins.core.service.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.service.auth.AuthService;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class FactoryPipelineStepService extends EntitySecureFindServiceImpl<TwinFactoryPipelineStepEntity> {
    @Getter
    private final TwinFactoryPipelineStepRepository repository;
    private final AuthService authService;
    private final FactoryPipelineService factoryPipelineService;
    private final FeaturerService featurerService;
    private final FactoryConditionSetService factoryConditionSetService;

    @Override
    public CrudRepository<TwinFactoryPipelineStepEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryPipelineStepEntity, UUID> entityGetIdFunction() {
        return TwinFactoryPipelineStepEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryPipelineStepEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getTwinFactoryPipeline().getTwinFactory().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryPipelineStepEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryPipelineId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryPipelineId");
        if (entity.getFillerFeaturerId() == 0)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty fillerFeaturerId");
        if (entity.getOrder() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty Order");
        }

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinFactoryPipeline() == null || !entity.getTwinFactoryPipeline().getId().equals(entity.getTwinFactoryPipelineId()))
                    entity.setTwinFactoryPipeline(factoryPipelineService.findEntitySafe(entity.getTwinFactoryPipelineId()));
                if (entity.getFillerFeaturer() == null || !(entity.getFillerFeaturer().getId() == (entity.getFillerFeaturerId())))
                    entity.setFillerFeaturer(featurerService.checkValid(entity.getFillerFeaturerId(), entity.getFillerParams(), Filler.class));
                if (entity.getTwinFactoryConditionSet() == null || !entity.getTwinFactoryConditionSet().getId().equals(entity.getTwinFactoryConditionSetId()))
                    entity.setTwinFactoryConditionSet(factoryConditionSetService.findEntitySafe(entity.getTwinFactoryConditionSetId()));
        }
        return true;
    }

    public TwinFactoryPipelineStepEntity createFactoryPipelineStep(TwinFactoryPipelineStepEntity entity) throws ServiceException {
        return saveSafe(entity);
    }

    public TwinFactoryPipelineStepEntity updateFactoryPipelineStep(TwinFactoryPipelineStepEntity entity) throws ServiceException {
        TwinFactoryPipelineStepEntity dbEntity = new TwinFactoryPipelineStepEntity();
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityField(entity, dbEntity, TwinFactoryPipelineStepEntity::getTwinFactoryPipelineId,
                TwinFactoryPipelineStepEntity::setTwinFactoryPipelineId, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, changesHelper);
        updateEntityField(entity, dbEntity, TwinFactoryPipelineStepEntity::getOrder,
                TwinFactoryPipelineStepEntity::setOrder, TwinFactoryPipelineStepEntity.Fields.order, changesHelper);
        updateEntityField(entity, dbEntity, TwinFactoryPipelineStepEntity::getTwinFactoryConditionInvert,
                TwinFactoryPipelineStepEntity::setTwinFactoryConditionInvert, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionInvert, changesHelper);
        updateEntityField(entity, dbEntity, TwinFactoryPipelineStepEntity::getActive,
                TwinFactoryPipelineStepEntity::setActive, TwinFactoryPipelineStepEntity.Fields.active, changesHelper);
        updateEntityField(entity, dbEntity, TwinFactoryPipelineStepEntity::getDescription,
                TwinFactoryPipelineStepEntity::setDescription, TwinFactoryPipelineStepEntity.Fields.description, changesHelper);
        updateEntityField(entity, dbEntity, TwinFactoryPipelineStepEntity::getOptional,
                TwinFactoryPipelineStepEntity::setOptional, TwinFactoryPipelineStepEntity.Fields.optional, changesHelper);

        updateFillerFeaturerId(dbEntity, entity.getFillerFeaturerId(), entity.getFillerParams(), changesHelper);

        return updateSafe(dbEntity,changesHelper);
    }

    public void updateFillerFeaturerId(TwinFactoryPipelineStepEntity dbEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        FeaturerEntity newFillerFeaturer = null;
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbEntity.getFillerFeaturerId(); // only params where changed
        }
        if (!MapUtils.areEqual(dbEntity.getFillerParams(), newFeaturerParams)) {
            newFillerFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, Filler.class);
            changesHelper.add(TwinFactoryMultiplierEntity.Fields.multiplierParams, dbEntity.getFillerParams(), newFeaturerParams);
            dbEntity
                    .setFillerParams(newFeaturerParams);
        }
        if (changesHelper.isChanged(TwinFactoryMultiplierEntity.Fields.multiplierFeaturerId, dbEntity.getFillerFeaturerId(), newFeaturerId)) {
            if (newFillerFeaturer == null)
                newFillerFeaturer = featurerService.getFeaturerEntity(newFeaturerId);
            dbEntity
                    .setFillerFeaturerId(newFillerFeaturer.getId())
                    .setFillerFeaturer(newFillerFeaturer);
        }
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
