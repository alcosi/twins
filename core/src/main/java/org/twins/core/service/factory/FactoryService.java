package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.dao.twinflow.TwinflowTransitionRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.cambium.common.util.RowUtils.mapUuidInt;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class FactoryService extends EntitySecureFindServiceImpl<TwinFactoryEntity> {
    @Getter
    private final TwinFactoryRepository repository;
    private final AuthService authService;
    private final I18nService i18nService;
    private final UserService userService;
    private final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;
    private final TwinFactoryMultiplierFilterRepository twinFactoryMultiplierFilterRepository;
    private final TwinFactoryPipelineRepository twinFactoryPipelineRepository;
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;
    private final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    private final TwinFactoryEraserRepository twinFactoryEraserRepository;
    private final TwinflowTransitionRepository twinflowTransitionRepository;
    private final TwinflowFactoryRepository twinflowFactoryRepository;
    @Lazy
    private final FactoryMultiplierService factoryMultiplierService;
    @Lazy
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;
    @Lazy
    private final FactoryPipelineService factoryPipelineService;
    @Lazy
    private final FactoryPipelineStepService factoryPipelineStepService;
    @Lazy
    private final FactoryBranchService factoryBranchService;
    @Lazy
    private final FactoryEraserService factoryEraserService;
    @Lazy
    private final FactoryTriggerService factoryTriggerService;

    @Override
    public CrudRepository<TwinFactoryEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryEntity, UUID> entityGetIdFunction() {
        return TwinFactoryEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity,EntitySmartService.ReadPermissionCheckMode.none);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEntity createFactory(TwinFactoryEntity factory, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        factory
                .setDomainId(apiUser.getDomainId())
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_FACTORY_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_FACTORY_DESCRIPTION, descriptionI18n).getId())
                .setCreatedByUserId(apiUser.getUserId())
                .setCreatedByUser(apiUser.getUser())
                .setCreatedAt(Timestamp.from(Instant.now()));
        validateEntityAndThrow(factory, EntitySmartService.EntityValidateMode.beforeSave);
        return repository.save(factory);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEntity updateFactory(TwinFactoryEntity factoryEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinFactoryEntity dbEntity = findEntitySafe(factoryEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateFactoryKey(factoryEntity, dbEntity, changesHelper);
        updateFactoryName(nameI18n, dbEntity, changesHelper);
        updateFactoryDescription(descriptionI18n, dbEntity, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    private void updateFactoryKey(TwinFactoryEntity factoryEntity, TwinFactoryEntity dbEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryEntity.Fields.key, dbEntity.getKey(), factoryEntity.getKey()))
            return;
        dbEntity.setKey(factoryEntity.getKey());
    }

    private void updateFactoryName(I18nEntity nameI18n, TwinFactoryEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbEntity.getNameI18NId() != null)
            nameI18n.setId(dbEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWIN_FACTORY_NAME, nameI18n);
        if (changesHelper.isChanged(TwinFactoryEntity.Fields.nameI18NId, dbEntity.getNameI18NId(), nameI18n.getId()))
            dbEntity.setNameI18NId(nameI18n.getId());
    }

    private void updateFactoryDescription(I18nEntity descriptionI18n, TwinFactoryEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWIN_FACTORY_DESCRIPTION, descriptionI18n);
        if (changesHelper.isChanged(TwinFactoryEntity.Fields.descriptionI18NId, dbEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbEntity.setDescriptionI18NId(descriptionI18n.getId());
    }

    public boolean isKeyFree(String key, UUID domainId) {
        return !repository.existsByKeyAndDomainId(key, domainId);
    }

    public void countFactoryUsages(TwinFactoryEntity twinFactory) {
        countFactoryUsages(Collections.singletonList(twinFactory));
    }

    public void countFactoryUsages(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryUsagesCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> twinflowTransitionCounts = mapUuidInt(twinflowTransitionRepository.countByInbuiltTwinFactoryIds(needLoad.getIdSet()));
        Map<UUID, Integer> twinflowFactoryCounts = mapUuidInt(twinflowFactoryRepository.countByAfterTransitionPerformFactoryIds(needLoad.getIdSet()));
        Map<UUID, Integer> twinFactoryBranchCounts = mapUuidInt(twinFactoryBranchRepository.countByNextTwinFactoryIds(needLoad.getIdSet()));
        Map<UUID, Integer> pipelineNextTwinFactoryCounts = mapUuidInt(twinFactoryPipelineRepository.countByNextTwinFactoryIds(needLoad.getIdSet()));
        Map<UUID, Integer> pipelineAfterCommitTwinFactoryCounts = mapUuidInt(twinFactoryPipelineRepository.countByAfterCommitTwinFactoryIds(needLoad.getIdSet()));

        needLoad.getCollection().forEach(twinFactory -> {
            int twinflowCount = twinflowTransitionCounts.getOrDefault(twinFactory.getId(), 0) + twinflowFactoryCounts.getOrDefault(twinFactory.getId(), 0);
            int twinFactoryBranchCount = twinFactoryBranchCounts.getOrDefault(twinFactory.getId(), 0);
            int twinFactoryPipelineCount = pipelineNextTwinFactoryCounts.getOrDefault(twinFactory.getId(), 0) + pipelineAfterCommitTwinFactoryCounts.getOrDefault(twinFactory.getId(), 0);
            twinFactory.setFactoryUsagesCount(twinflowCount + twinFactoryBranchCount + twinFactoryPipelineCount);
        });
    }

    public void countFactoryPipelines(TwinFactoryEntity twinFactory) {
        countFactoryPipelines(Collections.singletonList(twinFactory));
    }

    public void countFactoryPipelines(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryPipelinesCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryPipelines = mapUuidInt(twinFactoryPipelineRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryPipelinesCount(factoryPipelines.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countFactoryPipelineSteps(TwinFactoryPipelineEntity twinFactoryPipeline) {
        countFactoryPipelineSteps(Collections.singletonList(twinFactoryPipeline));
    }

    public void countFactoryPipelineSteps(Collection<TwinFactoryPipelineEntity> twinFactoryPipelines) {
        Kit<TwinFactoryPipelineEntity, UUID> needLoad = new Kit<>(TwinFactoryPipelineEntity::getId);
        for (TwinFactoryPipelineEntity twinFactoryPipeline : twinFactoryPipelines) {
            if (twinFactoryPipeline.getPipelineStepsCount() == null)
                needLoad.add(twinFactoryPipeline);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryPipelineSteps = mapUuidInt(twinFactoryPipelineStepRepository.countByFactoryPipelineIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactoryPipeline -> twinFactoryPipeline.setPipelineStepsCount(factoryPipelineSteps.getOrDefault(twinFactoryPipeline.getId(), 0)));
    }

    public void countFactoryMultipliers(TwinFactoryEntity twinFactory) {
        countFactoryMultipliers(Collections.singletonList(twinFactory));
    }

    public void countFactoryMultipliers(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryMultipliersCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryMultipliers = mapUuidInt(twinFactoryMultiplierRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryMultipliersCount(factoryMultipliers.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countFactoryBranches(TwinFactoryEntity twinFactory) {
        countFactoryBranches(Collections.singletonList(twinFactory));
    }

    public void countFactoryBranches(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryBranchesCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryBranches = mapUuidInt(twinFactoryBranchRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryBranchesCount(factoryBranches.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countFactoryErasers(TwinFactoryEntity twinFactory) {
        countFactoryErasers(Collections.singletonList(twinFactory));
    }

    public void countFactoryErasers(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryErasersCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryErasers = mapUuidInt(twinFactoryEraserRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryErasersCount(factoryErasers.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countConditionSetInFactoryPipelineUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryPipelineUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryPipelineUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryPipelineUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryPipelineRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryPipelineUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryPipelineStepUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryPipelineStepUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryPipelineStepUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryPipelineStepUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryPipelineStepRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryPipelineStepUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryMultiplierFilterUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryMultiplierFilterUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryMultiplierFilterUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryMultiplierFilterUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryMultiplierFilterRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryMultiplierFilterUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryBranchUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryBranchUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryBranchUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryBranchUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryBranchRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryBranchUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryEraserUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryEraserUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryEraserUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryEraserUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryEraserRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryEraserUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countFactoryMultiplierFilters(TwinFactoryMultiplierEntity multiplier) {
        countFactoryMultiplierFilters(Collections.singletonList(multiplier));
    }

    public void countFactoryMultiplierFilters(Collection<TwinFactoryMultiplierEntity> multiplierList) {
        Kit<TwinFactoryMultiplierEntity, UUID> needLoad = new Kit<>(TwinFactoryMultiplierEntity::getId);
        for (TwinFactoryMultiplierEntity multiplier : multiplierList) {
            if (multiplier.getFactoryMultiplierFiltersCount() == null)
                needLoad.add(multiplier);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> mulitplierFilterMap = mapUuidInt(twinFactoryMultiplierFilterRepository.countByMultiplierIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(multiplierFilter -> multiplierFilter.setFactoryMultiplierFiltersCount(mulitplierFilterMap.getOrDefault(multiplierFilter.getId(), 0)));
    }

    public void loadCreatedByUser(TwinFactoryEntity entity) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(entity));
    }

    public void loadCreatedByUser(Collection<TwinFactoryEntity> entities) throws ServiceException {
        userService.load(entities,
                TwinFactoryEntity::getCreatedByUserId,
                TwinFactoryEntity::getCreatedByUser,
                TwinFactoryEntity::setCreatedByUser);
    }

    public void loadFactoryElements(TwinFactoryEntity factory) {
        loadFactoryElements(Collections.singletonList(factory));
    }

    public void loadFactoryElements(Collection<TwinFactoryEntity> factories) {
        factoryMultiplierService.loadFactoryMultipliers(factories);
        var multipliers = new ArrayList<TwinFactoryMultiplierEntity>();
        for (TwinFactoryEntity factory : factories) {
            multipliers.addAll(factory.getTwinFactoryMultiplierKit().getCollection());
        }
        factoryMultiplierFilterService.loadFactoryMultiplierFilters(multipliers);
        factoryPipelineService.loadFactoryPipelines(factories);
        var pipelines = new ArrayList<TwinFactoryPipelineEntity>();
        for (TwinFactoryEntity factory : factories) {
            pipelines.addAll(factory.getTwinFactoryPipelineKit().getCollection());
        }
        factoryPipelineStepService.loadFactoryPipelineSteps(pipelines);
        factoryBranchService.loadFactoryBranches(factories);
        factoryEraserService.loadFactoryErasers(factories);
        factoryTriggerService.loadFactoryTriggers(factories);
    }
}
