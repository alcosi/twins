package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryPipelineStepExportService extends EntityExportService {
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Set<UUID> stepIds) throws ServiceException {
        return exportToSql(factoryPipelineStepService.findEntitiesSafe(stepIds).getCollection());
    }

    public String exportToSql(Collection<TwinFactoryPipelineStepEntity> steps) throws ServiceException {
        if (steps.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

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
            sqlParts.addNotBlank(conditionSetExportService.exportToSql(conditionSets));
        }

        // Export Pipeline Steps
        sqlParts.addNotBlank(sqlBuilder.buildInserts(steps));

        return String.join("\n", sqlParts);
    }
}
