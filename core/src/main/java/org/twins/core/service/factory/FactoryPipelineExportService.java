package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryPipelineExportService {
    private final FactoryPipelineService factoryPipelineService;
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetService factoryConditionSetService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Set<UUID> pipelineIds) throws ServiceException {
        return exportToSql(factoryPipelineService.findEntitiesSafe(pipelineIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryPipelineEntity> pipelines) throws ServiceException {
        if (pipelines.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // Collect Pipeline IDs and load Steps
        Set<UUID> pipelineIds = new HashSet<>();
        for (TwinFactoryPipelineEntity pipeline : pipelines) {
            pipelineIds.add(pipeline.getId());
        }

        List<TwinFactoryPipelineStepEntity> steps = factoryPipelineStepService.findByTwinFactoryPipelineIdIn(pipelineIds);

        // Collect ConditionSet IDs from both pipelines and steps
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryPipelineEntity pipeline : pipelines) {
            if (pipeline.getTwinFactoryConditionSetId() != null) {
                conditionSetIds.add(pipeline.getTwinFactoryConditionSetId());
            }
        }
        for (TwinFactoryPipelineStepEntity step : steps) {
            if (step.getTwinFactoryConditionSetId() != null) {
                conditionSetIds.add(step.getTwinFactoryConditionSetId());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSetIds.isEmpty()) {
            var conditionSetKit = factoryConditionSetService.findEntitiesSafe(conditionSetIds);
            if (!conditionSetKit.getList().isEmpty()) {
                String conditionSetSql = conditionSetExportService.exportToSql(conditionSetKit.getList());
                if (!conditionSetSql.isEmpty()) {
                    sqlParts.add(conditionSetSql);
                }
            }
        }

        // Export Pipelines
        String pipelinesSql = sqlBuilder.buildInserts(pipelines);
        if (!pipelinesSql.isEmpty()) {
            sqlParts.add(pipelinesSql);
        }

        // Export PipelineSteps
        if (!steps.isEmpty()) {
            String stepsSql = sqlBuilder.buildInserts(steps);
            if (!stepsSql.isEmpty()) {
                sqlParts.add(stepsSql);
            }
        }

        return String.join("\n", sqlParts);
    }
}
