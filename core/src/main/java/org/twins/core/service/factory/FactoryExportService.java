package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.service.EntityExportService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryExportService extends EntityExportService<TwinFactoryEntity> {
    private final FactoryService factoryService;

    private final FactoryBranchExportService branchExportService;
    private final FactoryMultiplierExportService multiplierExportService;
    private final FactoryPipelineExportService pipelineExportService;
    private final FactoryEraserExportService eraserExportService;
    private final FactoryTriggerExportService triggerExportService;
    private final FactoryConditionSetExportService factoryConditionSetExportService;
    private final FactoryConditionSetService factoryConditionSetService;

    @Override
    public String exportCollectionToSql(Collection<TwinFactoryEntity> factories) throws ServiceException {
        return exportToSql(factories, true, true, true, true, true, true, true, false);
    }

    public String exportToSql(UUID factoryId,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers) throws ServiceException {
        return exportToSql(factoryId, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, false);
    }

    public String exportToSql(UUID factoryId,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers,
                              boolean cascadeFactory) throws ServiceException {
        return exportToSql(Collections.singleton(factoryId), includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory);
    }

    public String exportToSql(Set<UUID> twinFactoryIds,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers) throws ServiceException {
        return exportToSql(twinFactoryIds, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, false);
    }

    public String exportToSql(Set<UUID> twinFactoryIds,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers,
                              boolean cascadeFactory) throws ServiceException {
        var factories = factoryService.findEntitiesSafe(twinFactoryIds);
        return exportToSql(factories.getCollection(), includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory);
    }

    public String exportToSql(Collection<TwinFactoryEntity> factories,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers) throws ServiceException {
        return exportToSql(factories, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, false);
    }

    public String exportToSql(Collection<TwinFactoryEntity> factories,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers,
                              boolean cascadeFactory) throws ServiceException {
        if (CollectionUtils.isEmpty(factories)) return "";

        // Expand the transitive closure of factories reachable via chaining links
        // (pipeline.nextTwinFactoryId / pipeline.afterCommitTwinFactoryId / branch.nextTwinFactoryId)
        // before exporting. The single batch below then covers the whole graph, which also keeps
        // i18n de-duplication correct (a single addExportSafe call on the full closure).
        if (cascadeFactory) {
            factories = expandFactoryCascade(factories);
            if (CollectionUtils.isEmpty(factories)) return "";
        }

        var sqlParts = new StringList();

        // Collect I18n IDs from factories
        Set<UUID> i18nIds = i18nService.collectI18nIds(factories,
                TwinFactoryEntity::getNameI18NId,
                TwinFactoryEntity::getDescriptionI18NId);

        // I18n for factories
        i18nExportService.addExportSafe(i18nIds, sqlParts);

        sqlParts.addNotBlank(buildInsertsSorted(factories, TwinFactoryEntity::getId));

        factoryService.loadFactoryElements(factories);
        factoryConditionSetService.loadFactoryConditionSets(factories);

        exportChildrenKit(
                includeConditionSets,
                factories,
                TwinFactoryEntity::getTwinFactoryConditionSetKit,
                factoryConditionSetExportService::exportCollectionToSql,
                sqlParts);

        exportChildrenKit(
                includeBranches,
                factories,
                TwinFactoryEntity::getTwinFactoryBranchKit,
                branchExportService::exportCollectionToSql,
                sqlParts);

        exportChildrenKit(
                includeMultipliers,
                factories,
                TwinFactoryEntity::getTwinFactoryMultiplierKit,
                multiplierExportService::exportCollectionToSql,
                sqlParts);

        exportChildrenKit(
                includeErasers,
                factories,
                TwinFactoryEntity::getTwinFactoryEraserKit,
                eraserExportService::exportCollectionToSql,
                sqlParts);

        exportChildrenKit(
                includeTriggers,
                factories,
                TwinFactoryEntity::getTwinFactoryTriggerKit,
                triggerExportService::exportCollectionToSql,
                sqlParts);

        exportChildrenKit(
                includePipelines,
                factories,
                TwinFactoryEntity::getTwinFactoryPipelineKit,
                list -> pipelineExportService.exportCollectionToSql(list, includePipelineSteps),
                sqlParts);

        return String.join("\n", sqlParts);
    }

    /**
     * Builds the transitive closure of factories reachable from the seed via chaining links:
     * {@code pipeline.nextTwinFactoryId}, {@code pipeline.afterCommitTwinFactoryId} and
     * {@code branch.nextTwinFactoryId}.
     * <p>
     * Cycle-safe: a monotonically growing {@code visited} map guarantees termination and ensures
     * every factory is loaded and emitted exactly once (self-loops and arbitrary cycles handled).
     * Broken (missing) and cross-domain references abort via {@link FactoryService#findEntitiesSafe},
     * surfacing a clear error rather than silently dropping part of the graph. Idempotent w.r.t.
     * already loaded element kits, so seed elements prefetched by the caller are not fetched twice.
     */
    private List<TwinFactoryEntity> expandFactoryCascade(Collection<TwinFactoryEntity> seed) throws ServiceException {
        LinkedHashMap<UUID, TwinFactoryEntity> visited = new LinkedHashMap<>();
        for (TwinFactoryEntity factory : seed) {
            visited.put(factory.getId(), factory);
        }
        List<TwinFactoryEntity> pending = new ArrayList<>(visited.values());
        while (!pending.isEmpty()) {
            List<TwinFactoryEntity> level = pending;
            pending = new ArrayList<>();
            factoryService.loadFactoryElements(level);
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
            Kit<TwinFactoryEntity, UUID> loaded = factoryService.findEntitiesSafe(newIds);
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
