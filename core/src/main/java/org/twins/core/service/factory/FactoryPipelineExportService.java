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
public class FactoryPipelineExportService extends EntityExportService {
    private final FactoryPipelineService factoryPipelineService;
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final FactoryPipelineStepExportService pipelineStepExportService;

    public String exportToSql(Set<UUID> pipelineIds, boolean includePipelineSteps) throws ServiceException {
        return exportToSql(factoryPipelineService.findEntitiesSafe(pipelineIds).getCollection(), includePipelineSteps);
    }

    public String exportToSql(Collection<TwinFactoryPipelineEntity> pipelines) throws ServiceException {
        return exportToSql(pipelines, true);
    }

    public String exportToSql(Collection<TwinFactoryPipelineEntity> pipelines, boolean includePipelineSteps) throws ServiceException {
        if (CollectionUtils.isEmpty(pipelines)) return "";
        var sqlParts = new StringList();

        // Load and export ConditionSets
        factoryPipelineService.loadConditionSets(pipelines);
        sqlParts.addNotBlank(conditionSetExportService.exportToSql(
                CollectionUtils.collect(pipelines, TwinFactoryPipelineEntity::getConditionSet)));

        // Export Pipelines
        sqlParts.addNotBlank(sqlBuilder.buildInserts(pipelines));
        if (includePipelineSteps) {
            factoryPipelineStepService.loadFactoryPipelineSteps(pipelines);
            exportChildrenKit(
                    true,
                    pipelines,
                    TwinFactoryPipelineEntity::getTwinFactoryPipelineStepKit,
                    pipelineStepExportService::exportToSql,
                    sqlParts);
        }

        return String.join("\n", sqlParts);
    }
}
