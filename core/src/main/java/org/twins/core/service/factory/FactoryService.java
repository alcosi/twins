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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.factoryprocessor.FactoryProcessor;
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
    private final FactoryConditionSetService factoryConditionSetService;
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
        if (factory.getFactoryProcessorFeaturerId() != null) {
            validateAndPrepareFeaturer(factory.getFactoryProcessorFeaturerId(), factory.getFactoryProcessorParams(), FactoryProcessor.class);
        } else {
            factory
                    .setFactoryProcessorFeaturerId(FeaturerTwins.ID_5401)
                    .setFactoryProcessorParams(null);
        }
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
        updateFactoryProcessorFeaturerId(dbEntity, factoryEntity.getFactoryProcessorFeaturerId(), factoryEntity.getFactoryProcessorParams(), changesHelper);
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

    public void updateFactoryProcessorFeaturerId(TwinFactoryEntity dbEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        updateEntityFeaturerField(dbEntity, newFeaturerId, newFeaturerParams,
                TwinFactoryEntity::getFactoryProcessorFeaturerId, TwinFactoryEntity::setFactoryProcessorFeaturerId,
                TwinFactoryEntity::getFactoryProcessorParams, TwinFactoryEntity::setFactoryProcessorParams,
                TwinFactoryEntity.Fields.factoryProcessorFeaturerId, TwinFactoryEntity.Fields.factoryProcessorParams,
                FactoryProcessor.class, changesHelper);
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

    public void loadCreatedByUser(TwinFactoryEntity entity) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(entity));
    }

    public void loadCreatedByUser(Collection<TwinFactoryEntity> entities) throws ServiceException {
        userService.load(entities,
                TwinFactoryEntity::getCreatedByUserId,
                TwinFactoryEntity::getCreatedByUser,
                TwinFactoryEntity::setCreatedByUser);
    }

    public void loadFactoryElements(TwinFactoryEntity factory) throws ServiceException {
        loadFactoryElements(Collections.singletonList(factory));
    }

    public void loadFactoryElements(Collection<TwinFactoryEntity> factories) throws ServiceException {
        factoryConditionSetService.loadFactoryConditionSets(factories);
        factoryMultiplierService.loadFactoryMultipliers(factories);
        var multipliers = new ArrayList<TwinFactoryMultiplierEntity>();
        for (var factory : factories) {
            multipliers.addAll(factory.getTwinFactoryMultiplierKit().getCollection());
        }
        factoryMultiplierFilterService.loadFactoryMultiplierFilters(multipliers);
        var elementsWithConditionSets = new ArrayList<ContainsFactoryConditionSet>();
        for (var multiplier : multipliers) {
            elementsWithConditionSets.addAll(multiplier.getTwinFactoryMultiplierFilterKit().getCollection());
        }
        factoryPipelineService.loadFactoryPipelines(factories);
        var pipelines = new ArrayList<TwinFactoryPipelineEntity>();
        for (var factory : factories) {
            pipelines.addAll(factory.getTwinFactoryPipelineKit().getCollection());
        }
        factoryPipelineStepService.loadFactoryPipelineSteps(pipelines);
        for (var pipeline : pipelines) {
            elementsWithConditionSets.addAll(pipeline.getTwinFactoryPipelineStepKit().getCollection());
        }
        factoryBranchService.loadFactoryBranches(factories);
        factoryEraserService.loadFactoryErasers(factories);
        factoryTriggerService.loadFactoryTriggers(factories);
        for (var factory : factories) {
            elementsWithConditionSets.addAll(factory.getTwinFactoryPipelineKit().getCollection());
            elementsWithConditionSets.addAll(factory.getTwinFactoryBranchKit().getCollection());
            elementsWithConditionSets.addAll(factory.getTwinFactoryEraserKit().getCollection());
            elementsWithConditionSets.addAll(factory.getTwinFactoryTriggerKit().getCollection());
        }
        factoryConditionSetService.loadElementsConditionSets(elementsWithConditionSets);
    }

    public static final int FACTORY_CASCADE_HARD_CAP = 100;

    /**
     * Builds the transitive closure of factories reachable from the seed via chaining links:
     * {@code pipeline.nextTwinFactoryId}, {@code pipeline.afterCommitTwinFactoryId} and
     * {@code branch.nextTwinFactoryId}. Cycle-safe (monotonically growing visited map guarantees
     * termination). Aborts with {@link ErrorCodeTwins#FACTORY_CASCADE_LIMIT_EXCEEDED} if the
     * closure size would exceed {@code hardCap}. Cross-domain/broken refs abort via
     * {@link #findEntitiesSafe}. Idempotent w.r.t. already loaded element kits.
     */
    public List<TwinFactoryEntity> expandFactoryCascade(Collection<TwinFactoryEntity> seed, int hardCap) throws ServiceException {
        LinkedHashMap<UUID, TwinFactoryEntity> visited = new LinkedHashMap<>();
        for (TwinFactoryEntity factory : seed) {
            visited.put(factory.getId(), factory);
        }
        List<TwinFactoryEntity> pending = new ArrayList<>(visited.values());
        while (!pending.isEmpty()) {
            List<TwinFactoryEntity> level = pending;
            pending = new ArrayList<>();
            loadFactoryElements(level);
            Set<UUID> discovered = new HashSet<>();
            for (TwinFactoryEntity factory : level) {
                collectChainedFactoryIds(factory, discovered);
            }
            // cycle protection: only descend into factories not yet visited
            Set<UUID> newIds = new HashSet<>();
            for (UUID id : discovered) {
                if (id != null && !visited.containsKey(id)) {
                    newIds.add(id);
                }
            }
            if (newIds.isEmpty()) {
                continue;
            }
            if (visited.size() + newIds.size() > hardCap) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_CASCADE_LIMIT_EXCEEDED);
            }
            Kit<TwinFactoryEntity, UUID> loaded = findEntitiesSafe(newIds);
            for (TwinFactoryEntity factory : loaded.getCollection()) {
                visited.put(factory.getId(), factory);
            }
            pending = new ArrayList<>(loaded.getCollection());
        }
        return new ArrayList<>(visited.values());
    }

    private void collectChainedFactoryIds(TwinFactoryEntity factory, Set<UUID> sink) {
        Kit<TwinFactoryPipelineEntity, UUID> pipelines = factory.getTwinFactoryPipelineKit();
        if (pipelines != null) {
            for (TwinFactoryPipelineEntity pipeline : pipelines.getCollection()) {
                if (pipeline.getNextTwinFactoryId() != null) {
                    sink.add(pipeline.getNextTwinFactoryId());
                }
                if (pipeline.getAfterCommitTwinFactoryId() != null) {
                    sink.add(pipeline.getAfterCommitTwinFactoryId());
                }
            }
        }
        Kit<TwinFactoryBranchEntity, UUID> branches = factory.getTwinFactoryBranchKit();
        if (branches != null) {
            for (TwinFactoryBranchEntity branch : branches.getCollection()) {
                if (branch.getNextTwinFactoryId() != null) {
                    sink.add(branch.getNextTwinFactoryId());
                }
            }
        }
    }
}
