package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.*;
import org.twins.core.service.EntityExportService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
        return exportToSql(factories, true, true, true, true, true, true, true, false, false);
    }

    public String exportToSql(UUID factoryId,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers) throws ServiceException {
        return exportToSql(factoryId, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, false, false);
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
        return exportToSql(factoryId, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory, false);
    }

    public String exportToSql(UUID factoryId,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers,
                              boolean cascadeFactory,
                              boolean clearElements) throws ServiceException {
        return exportToSql(Collections.singleton(factoryId), includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory, clearElements);
    }

    public String exportToSql(Set<UUID> twinFactoryIds,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers) throws ServiceException {
        return exportToSql(twinFactoryIds, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, false, false);
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
        return exportToSql(twinFactoryIds, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory, false);
    }

    public String exportToSql(Set<UUID> twinFactoryIds,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers,
                              boolean cascadeFactory,
                              boolean clearElements) throws ServiceException {
        var factories = factoryService.findEntitiesSafe(twinFactoryIds);
        return exportToSql(factories.getCollection(), includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory, clearElements);
    }

    public String exportToSql(Collection<TwinFactoryEntity> factories,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers) throws ServiceException {
        return exportToSql(factories, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, false, false);
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
        return exportToSql(factories, includeConditionSets, includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers, cascadeFactory, false);
    }

    public String exportToSql(Collection<TwinFactoryEntity> factories,
                              boolean includeConditionSets,
                              boolean includeBranches,
                              boolean includeMultipliers,
                              boolean includePipelines,
                              boolean includePipelineSteps,
                              boolean includeErasers,
                              boolean includeTriggers,
                              boolean cascadeFactory,
                              boolean clearElements) throws ServiceException {
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

        // clearElements: drop orphan factory elements (present in target DB but not in this export)
        // BEFORE the upserts so the target DB converges to exactly the exported state. The factory
        // row itself is never deleted (external FKs from twinflow_transition). Cascade FKs purge
        // pipeline steps / conditions / multiplier filters; twin_factory_condition_set is cleared
        // only when all its RESTRICT referencers are also in clear scope (see appendClearElementsSql).
        if (clearElements) {
            appendClearElementsSql(sqlParts, factories,
                    includeConditionSets, includeBranches, includeMultipliers,
                    includePipelines, includeErasers, includeTriggers);
        }

        // Collect I18n IDs from factories
        Set<UUID> i18nIds = i18nService.collectI18nIds(factories,
                TwinFactoryEntity::getNameI18NId,
                TwinFactoryEntity::getDescriptionI18NId);

        // I18n for factories
        i18nExportService.addExportSafe(i18nIds, sqlParts);

        sqlParts.addNotBlank(buildUpsertsSorted(factories, TwinFactoryEntity::getId));

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
     * Emits the clearElements DELETE block for the given factories. For every include-* category
     * that is enabled, deletes that factory's rows of the corresponding table. The factory row
     * itself is NOT deleted (external FKs). Cascade FKs handle pipeline_step / condition /
     * multiplier_filter removal, so they need no separate DELETE here.
     * <p>
     * {@code twin_factory_condition_set} is referenced RESTRICT by twin_factory_pipeline /
     * branch / eraser / multiplier, so it is only safe to delete when ALL of those referencers are
     * also being cleared; otherwise its DELETE is skipped with a SQL comment and a warning, and its
     * definition is still refreshed by the upsert.
     */
    private void appendClearElementsSql(StringList sqlParts,
                                        Collection<TwinFactoryEntity> factories,
                                        boolean includeConditionSets,
                                        boolean includeBranches,
                                        boolean includeMultipliers,
                                        boolean includePipelines,
                                        boolean includeErasers,
                                        boolean includeTriggers) {
        if (CollectionUtils.isEmpty(factories)) return;
        Set<UUID> factoryIds = factories.stream().map(TwinFactoryEntity::getId).collect(Collectors.toSet());

        StringList deletes = new StringList();
        if (includePipelines)   deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryPipelineEntity.class,   "twin_factory_id", factoryIds));
        if (includeBranches)    deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryBranchEntity.class,    "twin_factory_id", factoryIds));
        if (includeErasers)     deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryEraserEntity.class,     "twin_factory_id", factoryIds));
        if (includeMultipliers) deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryMultiplierEntity.class, "twin_factory_id", factoryIds));
        if (includeTriggers)    deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryTriggerEntity.class,    "twin_factory_id", factoryIds));

        // condition_set is RESTRICT-referenced by pipeline / branch / eraser / multiplier. Only safe
        // to delete once all of those referencers are in clear scope.
        boolean canClearConditionSets = includeConditionSets
                && includePipelines && includeBranches && includeErasers && includeMultipliers;
        if (includeConditionSets && !canClearConditionSets) {
            String missing = joinMissingClearReferencers(includePipelines, includeBranches, includeErasers, includeMultipliers);
            log.warn("clearElements: twin_factory_condition_set cleanup skipped for factories {} "
                    + "(RESTRICT referencers outside clear scope: {}); definition is refreshed via upsert",
                    factoryIds, missing);
            deletes.add("-- clearElements: twin_factory_condition_set cleanup SKIPPED (RESTRICT referencers outside clear scope: "
                    + missing + "); definition refreshed via upsert");
        } else if (canClearConditionSets) {
            deletes.addNotBlank(sqlBuilder.buildDeleteByColumn(TwinFactoryConditionSetEntity.class, "twin_factory_id", factoryIds));
        }

        if (!deletes.isEmpty()) {
            sqlParts.add("-- clearElements: factories = " + factoryIds);
            sqlParts.addAll(deletes);
        }
    }

    private String joinMissingClearReferencers(boolean includePipelines,
                                               boolean includeBranches,
                                               boolean includeErasers,
                                               boolean includeMultipliers) {
        List<String> missing = new ArrayList<>();
        if (!includePipelines)   missing.add("twin_factory_pipeline");
        if (!includeBranches)    missing.add("twin_factory_branch");
        if (!includeErasers)     missing.add("twin_factory_eraser");
        if (!includeMultipliers) missing.add("twin_factory_multiplier");
        return String.join(", ", missing);
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
