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
public class FactoryPipelineStepExportService extends EntityExportService<TwinFactoryPipelineStepEntity> {
    private final FactoryPipelineStepService factoryPipelineStepService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Set<UUID> stepIds) throws ServiceException {
        return exportCollectionToSql(factoryPipelineStepService.findEntitiesSafe(stepIds).getCollection());
    }

    public String exportCollectionToSql(Collection<TwinFactoryPipelineStepEntity> steps) throws ServiceException {
        if (CollectionUtils.isEmpty(steps)) return "";
        var sqlParts = new StringList();

        // Load and export ConditionSets
        factoryPipelineStepService.loadConditionSet(steps);
        sqlParts.addNotBlank(conditionSetExportService.exportCollectionToSql(
                CollectionUtils.collect(steps, TwinFactoryPipelineStepEntity::getTwinFactoryConditionSet)));

        // Export Pipeline Steps
        sqlParts.addNotBlank(sqlBuilder.buildInserts(steps));

        return String.join("\n", sqlParts);
    }
}
