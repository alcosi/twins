package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryPipelineExportService {
    private final FactoryPipelineService factoryPipelineService;
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final FactoryPipelineStepExportService pipelineStepExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Set<UUID> pipelineIds, boolean includePipelineSteps) throws ServiceException {
        return exportToSql(factoryPipelineService.findEntitiesSafe(pipelineIds).getList(), includePipelineSteps);
    }

    public String exportToSql(Collection<TwinFactoryPipelineEntity> pipelines, boolean includePipelineSteps) throws ServiceException {
        if (pipelines.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

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
            String conditionSetSql = conditionSetExportService.exportToSql(conditionSets);
            if (!conditionSetSql.isEmpty()) {
                sqlParts.add(conditionSetSql);
            }
        }

        // Export Pipelines
        String pipelinesSql = sqlBuilder.buildInserts(pipelines);
        if (!pipelinesSql.isEmpty()) {
            sqlParts.add(pipelinesSql);
        }

        // Export PipelineSteps (if enabled) - step service will handle its own conditionSets
        if (includePipelineSteps) {
            Set<UUID> pipelineIds = new HashSet<>();
            for (TwinFactoryPipelineEntity pipeline : pipelines) {
                pipelineIds.add(pipeline.getId());
            }

            var steps = factoryPipelineStepService.findByTwinFactoryPipelineIdIn(pipelineIds);
            if (!steps.isEmpty()) {
                String stepsSql = pipelineStepExportService.exportToSql(steps);
                if (!stepsSql.isEmpty()) {
                    sqlParts.add(stepsSql);
                }
            }
        }

        return String.join("\n", sqlParts);
    }
}
