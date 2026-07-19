package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryPipelineExportService extends EntityExportService<TwinFactoryPipelineEntity> {
    private final FactoryPipelineService factoryPipelineService;
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final FactoryPipelineStepExportService pipelineStepExportService;

    public String exportCollectionToSql(Set<UUID> pipelineIds, boolean includePipelineSteps) throws ServiceException {
        return exportCollectionToSql(factoryPipelineService.findEntitiesSafe(pipelineIds).getCollection(), includePipelineSteps);
    }

    public String exportCollectionToSql(Collection<TwinFactoryPipelineEntity> pipelines) throws ServiceException {
        return exportCollectionToSql(pipelines, true);
    }

    public String exportCollectionToSql(Collection<TwinFactoryPipelineEntity> pipelines, boolean includePipelineSteps) throws ServiceException {
        if (CollectionUtils.isEmpty(pipelines)) return "";
        var sqlParts = new StringList();

        // Load and export ConditionSets
        factoryPipelineService.loadConditionSet(pipelines);
        sqlParts.addNotBlank(conditionSetExportService.exportCollectionToSql(
                CollectionUtils.collect(pipelines, TwinFactoryPipelineEntity::getConditionSet)));

        // Export Pipelines
        sqlParts.addNotBlank(buildUpsertsSorted(pipelines, TwinFactoryPipelineEntity::getId));
        if (includePipelineSteps) {
            factoryPipelineStepService.loadFactoryPipelineSteps(pipelines);
            exportChildrenKit(
                    true,
                    pipelines,
                    TwinFactoryPipelineEntity::getTwinFactoryPipelineStepKit,
                    pipelineStepExportService::exportCollectionToSql,
                    sqlParts);
        }

        return String.join("\n", sqlParts);
    }
}
