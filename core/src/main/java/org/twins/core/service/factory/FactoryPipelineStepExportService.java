package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
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
        if (CollectionUtils.isEmpty(steps)) return "";
        var sqlParts = new StringList();

        // Load and export ConditionSets
        factoryPipelineStepService.loadConditionSets(steps);
        sqlParts.addNotBlank(conditionSetExportService.exportToSql(
                CollectionUtils.collect(steps, TwinFactoryPipelineStepEntity::getTwinFactoryConditionSet)));

        // Export Pipeline Steps
        sqlParts.addNotBlank(sqlBuilder.buildInserts(steps));

        return String.join("\n", sqlParts);
    }
}
