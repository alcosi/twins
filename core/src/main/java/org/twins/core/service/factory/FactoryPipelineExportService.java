package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.HashSet;
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

    public String exportToSql(Collection<TwinFactoryPipelineEntity> pipelines, boolean includePipelineSteps) throws ServiceException {
        if (pipelines.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // Load conditionSets for pipelines
        factoryPipelineService.loadConditionSets(pipelines);
        Set<TwinFactoryConditionSetEntity> conditionSets = new HashSet<>();
        for (TwinFactoryPipelineEntity pipeline : pipelines) {
            if (pipeline.getConditionSet() != null) {
                conditionSets.add(pipeline.getConditionSet());
            }
        }

        // Export ConditionSets and Conditions (for pipelines only)
        if (!conditionSets.isEmpty()) {
            sqlParts.addNotBlank(conditionSetExportService.exportToSql(conditionSets));
        }

        // Export Pipelines
        sqlParts.addNotBlank(sqlBuilder.buildInserts(pipelines));

        // Export PipelineSteps (if enabled) - step service will handle its own conditionSets
        if (includePipelineSteps) {
            Set<UUID> pipelineIds = new HashSet<>();
            for (TwinFactoryPipelineEntity pipeline : pipelines) {
                pipelineIds.add(pipeline.getId());
            }

            var steps = factoryPipelineStepService.findByTwinFactoryPipelineIdIn(pipelineIds);
            if (!steps.isEmpty()) {
                sqlParts.addNotBlank(pipelineStepExportService.exportToSql(steps));
            }
        }

        return String.join("\n", sqlParts);
    }
}
