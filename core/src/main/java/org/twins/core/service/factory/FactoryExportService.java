package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.service.EntityExportService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryExportService extends EntityExportService {
    private final TwinFactoryService twinFactoryService;

    private final FactoryBranchExportService branchExportService;
    private final FactoryMultiplierExportService multiplierExportService;
    private final FactoryPipelineExportService pipelineExportService;
    private final FactoryEraserExportService eraserExportService;
    private final FactoryTriggerExportService triggerExportService;

    public String exportToSql(UUID factoryId, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includePipelineSteps, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        return exportToSql(Collections.singleton(factoryId), includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers);
    }

    @SneakyThrows
    public String exportToSql(Set<UUID> factoryIds, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includePipelineSteps, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        var factoriesKit = twinFactoryService.findEntitiesSafe(factoryIds);

        if (factoriesKit.isEmpty()) {
            return "";
        }

        List<TwinFactoryEntity> factories = factoriesKit.getList();
        var sqlParts = new StringList();

        // Collect I18n IDs from factories
        Set<UUID> i18nIds = i18nService.collectI18nIds(factories,
                TwinFactoryEntity::getNameI18NId,
                TwinFactoryEntity::getDescriptionI18NId);

        // I18n for factories
        i18nExportService.addExportSafe(i18nIds, sqlParts);

        sqlParts.addNotBlank(sqlBuilder.buildInserts(factories));

        // Load all factory elements using optimized load methods
        twinFactoryService.loadFactoryElements(factories);

        exportChildren(
                includeBranches,
                factories,
                factory -> factory.getTwinFactoryBranchKit().getCollection(),
                branchExportService::exportToSql,
                sqlParts);

        exportChildren(
                includeMultipliers,
                factories,
                factory -> factory.getTwinFactoryMultiplierKit().getCollection(),
                multiplierExportService::exportToSql,
                sqlParts);

        exportChildren(
                includeErasers,
                factories,
                factory -> factory.getTwinFactoryEraserKit().getCollection(),
                eraserExportService::exportToSql,
                sqlParts);

        exportChildren(
                includeTriggers,
                factories,
                factory -> factory.getTwinFactoryTriggerKit().getCollection(),
                triggerExportService::exportToSql,
                sqlParts);

        // Pipelines
        if (includePipelines) {
            List<TwinFactoryPipelineEntity> pipelines = new ArrayList<>();
            for (TwinFactoryEntity factory : factories) {
                pipelines.addAll(factory.getTwinFactoryPipelineKit().getList());
            }
            if (!pipelines.isEmpty()) {
                sqlParts.addNotBlank(pipelineExportService.exportToSql(pipelines, includePipelineSteps));
            }
        }

        return String.join("\n", sqlParts);
    }
}
