package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryExportService extends EntityExportService<TwinFactoryEntity> {
    private final FactoryExecutionService twinFactoryService;

    private final FactoryBranchExportService branchExportService;
    private final FactoryMultiplierExportService multiplierExportService;
    private final FactoryPipelineExportService pipelineExportService;
    private final FactoryEraserExportService eraserExportService;
    private final FactoryTriggerExportService triggerExportService;

    @Override
    public String exportCollectionToSql(Collection<TwinFactoryEntity> factories) throws ServiceException {
        return exportToSql(factories, true, true, true, true, true, true);
    }

    public String exportToSql(UUID factoryId, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includePipelineSteps, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        return exportToSql(Collections.singleton(factoryId), includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers);
    }

    public String exportToSql(Set<UUID> twinFactoryIds, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includePipelineSteps, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        var factories = twinFactoryService.findEntitiesSafe(twinFactoryIds);
        return exportToSql(factories.getCollection(), includeBranches, includeMultipliers, includePipelines, includePipelineSteps, includeErasers, includeTriggers);
    }

    public String exportToSql(Collection<TwinFactoryEntity> factories, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includePipelineSteps, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        if (CollectionUtils.isEmpty(factories)) return "";

        var sqlParts = new StringList();

        // Collect I18n IDs from factories
        Set<UUID> i18nIds = i18nService.collectI18nIds(factories,
                TwinFactoryEntity::getNameI18NId,
                TwinFactoryEntity::getDescriptionI18NId);

        // I18n for factories
        i18nExportService.addExportSafe(i18nIds, sqlParts);

        sqlParts.addNotBlank(sqlBuilder.buildInserts(factories));

        twinFactoryService.loadFactoryElements(factories);

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


}
