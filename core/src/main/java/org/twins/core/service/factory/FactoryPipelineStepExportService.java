package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryPipelineStepExportService {
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Set<UUID> stepIds) throws ServiceException {
        return exportToSql(factoryPipelineStepService.findEntitiesSafe(stepIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryPipelineStepEntity> steps) throws ServiceException {
        if (steps.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // Load conditionSets for steps
        factoryPipelineStepService.loadConditionSets(steps);
        Set<TwinFactoryConditionSetEntity> conditionSets = new HashSet<>();
        for (TwinFactoryPipelineStepEntity step : steps) {
            if (step.getTwinFactoryConditionSet() != null) {
                conditionSets.add(step.getTwinFactoryConditionSet());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSets.isEmpty()) {
            String conditionSetSql = conditionSetExportService.exportToSql(conditionSets);
            if (!conditionSetSql.isEmpty()) {
                sqlParts.add(conditionSetSql);
            }
        }

        // Export Pipeline Steps
        String stepsSql = sqlBuilder.buildInserts(steps);
        if (!stepsSql.isEmpty()) {
            sqlParts.add(stepsSql);
        }

        return String.join("\n", sqlParts);
    }
}
