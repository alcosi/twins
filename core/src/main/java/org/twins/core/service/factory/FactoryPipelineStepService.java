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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepRepository;
import org.twins.core.featurer.factory.filler.Filler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class FactoryPipelineStepService extends EntitySecureFindServiceImpl<TwinFactoryPipelineStepEntity> {
    @Getter
    private final TwinFactoryPipelineStepRepository repository;
    @Lazy
    private final FactoryPipelineService factoryPipelineService;
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
        return checkDomainAccessDenied(entity.getTwinFactoryPipeline().getTwinFactory().getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinFactoryPipelineStepEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryPipelineId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryPipelineId");
        if (entity.getFillerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty fillerFeaturerId");
        if (entity.getOrder() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty order");
        }

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinFactoryPipeline() == null || !entity.getTwinFactoryPipeline().getId().equals(entity.getTwinFactoryPipelineId()))
                    entity.setTwinFactoryPipeline(factoryPipelineService.findEntitySafe(entity.getTwinFactoryPipelineId()));
                validateAndPrepareFeaturer(entity.getFillerFeaturerId(), entity.getFillerParams(), Filler.class);
                if (entity.getTwinFactoryConditionSetId() != null && (entity.getTwinFactoryConditionSet() == null || !entity.getTwinFactoryConditionSet().getId().equals(entity.getTwinFactoryConditionSetId())))
                    entity.setTwinFactoryConditionSet(factoryConditionSetService.findEntitySafe(entity.getTwinFactoryConditionSetId()));
        }
        return true;
    }

    public TwinFactoryPipelineStepEntity createFactoryPipelineStep(TwinFactoryPipelineStepEntity entity) throws ServiceException {
        return saveSafe(entity);
    }

    public TwinFactoryPipelineStepEntity updateFactoryPipelineStep(TwinFactoryPipelineStepEntity entity) throws ServiceException {
        TwinFactoryPipelineStepEntity dbEntity = findEntitySafe(entity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getTwinFactoryPipelineId,
                TwinFactoryPipelineStepEntity::setTwinFactoryPipelineId, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipelineId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getOrder,
                TwinFactoryPipelineStepEntity::setOrder, TwinFactoryPipelineStepEntity.Fields.order, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getTwinFactoryConditionSetId,
                TwinFactoryPipelineStepEntity::setTwinFactoryConditionSetId, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionSetId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getTwinFactoryConditionInvert,
                TwinFactoryPipelineStepEntity::setTwinFactoryConditionInvert, TwinFactoryPipelineStepEntity.Fields.twinFactoryConditionInvert, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getActive,
                TwinFactoryPipelineStepEntity::setActive, TwinFactoryPipelineStepEntity.Fields.active, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getDescription,
                TwinFactoryPipelineStepEntity::setDescription, TwinFactoryPipelineStepEntity.Fields.description, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, TwinFactoryPipelineStepEntity::getOptional,
                TwinFactoryPipelineStepEntity::setOptional, TwinFactoryPipelineStepEntity.Fields.optional, changesHelper);

        updateFillerFeaturerId(dbEntity, entity.getFillerFeaturerId(), entity.getFillerParams(), changesHelper);

        return updateSafe(dbEntity,changesHelper);
    }

    public void updateFillerFeaturerId(TwinFactoryPipelineStepEntity dbEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        updateEntityFeaturerField(dbEntity, newFeaturerId, newFeaturerParams,
                TwinFactoryPipelineStepEntity::getFillerFeaturerId, TwinFactoryPipelineStepEntity::setFillerFeaturerId,
                TwinFactoryPipelineStepEntity::getFillerParams, TwinFactoryPipelineStepEntity::setFillerParams,
                TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId, TwinFactoryPipelineStepEntity.Fields.fillerParams,
                Filler.class, changesHelper);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }

    public List<TwinFactoryPipelineStepEntity> findByTwinFactoryPipelineIdIn(Collection<UUID> pipelineIds) {
        return repository.findByTwinFactoryPipelineIdInOrderByOrderAsc(pipelineIds);
    }

    public void loadFactoryPipelineSteps(TwinFactoryPipelineEntity pipeline) {
        loadFactoryPipelineSteps(Collections.singletonList(pipeline));
    }

    public void loadFactoryPipelineSteps(Collection<TwinFactoryPipelineEntity> pipelines) {
        loadKit(
                pipelines,
                TwinFactoryPipelineEntity::getId,
                TwinFactoryPipelineEntity::getTwinFactoryPipelineStepKit,
                TwinFactoryPipelineEntity::setTwinFactoryPipelineStepKit,
                repository::findByTwinFactoryPipelineIdInOrderByOrderAsc,
                TwinFactoryPipelineStepEntity::getId,
                TwinFactoryPipelineStepEntity::getTwinFactoryPipelineId);
    }

    public void loadConditionSets(TwinFactoryPipelineStepEntity step) throws ServiceException {
        loadConditionSets(Collections.singleton(step));
    }

    public void loadConditionSets(Collection<TwinFactoryPipelineStepEntity> steps) throws ServiceException {
        factoryConditionSetService.load(steps,
                TwinFactoryPipelineStepEntity::getTwinFactoryConditionSetId,
                TwinFactoryPipelineStepEntity::getTwinFactoryConditionSet,
                TwinFactoryPipelineStepEntity::setTwinFactoryConditionSet);
    }
}
