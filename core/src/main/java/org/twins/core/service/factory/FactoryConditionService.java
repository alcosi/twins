package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.factory.TwinFactoryConditionRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryConditionService extends EntitySecureFindServiceImpl<TwinFactoryConditionEntity> {

    private final TwinFactoryConditionRepository repository;
    private final FeaturerService featurerService;

    @Override
    public Function<TwinFactoryConditionEntity, UUID> entityGetIdFunction() {
        return TwinFactoryConditionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryConditionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryConditionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinFactoryConditionSetId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinFactoryConditionSetId");
        if (entity.getConditionerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty сonditionerFeaturer");
        return true;
    }

    @Override
    public CrudRepository<TwinFactoryConditionEntity, UUID> entityRepository() {
        return repository;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinFactoryConditionEntity> createFactoryConditions(
            List<TwinFactoryConditionEntity> conditionCreates
    ) throws ServiceException {
        if (CollectionUtils.isEmpty(conditionCreates)) {
            return Collections.emptyList();
        }
        for (TwinFactoryConditionEntity twinFactoryConditionEntity : conditionCreates) {
            validateEntityAndThrow(twinFactoryConditionEntity, EntitySmartService.EntityValidateMode.beforeSave);
        }
        return StreamSupport.stream(
                entityRepository().saveAll(conditionCreates).spliterator(), false
        ).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinFactoryConditionEntity> updateFactoryConditions(
            List<TwinFactoryConditionEntity> conditionUpdates
    ) throws ServiceException {
        if (CollectionUtils.isEmpty(conditionUpdates)) {
            return Collections.emptyList();
        }

        Kit<TwinFactoryConditionEntity, UUID> dbFactoryConditionKit = findEntitiesSafe(
                conditionUpdates.stream()
                        .map(TwinFactoryConditionEntity::getId)
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinFactoryConditionEntity> changes = new ChangesHelperMulti<>();

        for (TwinFactoryConditionEntity twinFactoryConditionEntity : conditionUpdates) {
            ChangesHelper changesHelper = new ChangesHelper();
            TwinFactoryConditionEntity dbFactoryConditionEntity = dbFactoryConditionKit.get(twinFactoryConditionEntity.getId());

            updateEntityFieldByEntity(twinFactoryConditionEntity, dbFactoryConditionEntity,
                    TwinFactoryConditionEntity::getTwinFactoryConditionSetId, TwinFactoryConditionEntity::setTwinFactoryConditionSetId,
                    TwinFactoryConditionEntity.Fields.twinFactoryConditionSetId, changesHelper);

            updateEntityFieldByEntity(twinFactoryConditionEntity, dbFactoryConditionEntity,
                    TwinFactoryConditionEntity::getConditionerFeaturerId, TwinFactoryConditionEntity::setConditionerFeaturerId,
                    TwinFactoryConditionEntity.Fields.conditionerFeaturerId, changesHelper);

            updateEntityFieldByEntity(twinFactoryConditionEntity, dbFactoryConditionEntity,
                    TwinFactoryConditionEntity::getDescription, TwinFactoryConditionEntity::setDescription,
                    TwinFactoryConditionEntity.Fields.description, changesHelper);

            updateEntityFieldByEntity(twinFactoryConditionEntity, dbFactoryConditionEntity,
                    TwinFactoryConditionEntity::getActive, TwinFactoryConditionEntity::setActive,
                    TwinFactoryConditionEntity.Fields.active, changesHelper);

            updateEntityFieldByEntity(twinFactoryConditionEntity, dbFactoryConditionEntity,
                    TwinFactoryConditionEntity::getInvert, TwinFactoryConditionEntity::setInvert,
                    TwinFactoryConditionEntity.Fields.invert, changesHelper);

            updateConditionerParams(dbFactoryConditionEntity, twinFactoryConditionEntity.getConditionerParams(), changesHelper);

            changes.add(dbFactoryConditionEntity, changesHelper);
        }
        updateSafe(changes);
        return dbFactoryConditionKit.getList();
    }

    public void updateConditionerParams(TwinFactoryConditionEntity dbEntity, HashMap<String, String> newConditionerParams, ChangesHelper changesHelper) {
        if (!MapUtils.areEqual(dbEntity.getConditionerParams(), newConditionerParams)) {
            changesHelper.add(TwinFactoryConditionEntity.Fields.conditionerParams, dbEntity.getConditionerParams(), newConditionerParams);
            dbEntity
                    .setConditionerParams(newConditionerParams);
        }
    }

    public void loadConditioner(TwinFactoryConditionEntity src) {
        loadConditioners(Collections.singleton(src));
    }

    public void loadConditioners(Collection<TwinFactoryConditionEntity> srcCollection) {
        featurerService.loadFeaturers(srcCollection,
                TwinFactoryConditionEntity::getId,
                TwinFactoryConditionEntity::getConditionerFeaturerId,
                TwinFactoryConditionEntity::getConditionerFeaturer,
                TwinFactoryConditionEntity::setConditionerFeaturer);
    }
}
