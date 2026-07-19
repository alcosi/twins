package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryEraserExportService extends EntityExportService<TwinFactoryEraserEntity> {
    private final FactoryEraserService factoryEraserService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Set<UUID> eraserIds) throws ServiceException {
        return exportCollectionToSql(factoryEraserService.findEntitiesSafe(eraserIds).getList());
    }

    public String exportCollectionToSql(Collection<TwinFactoryEraserEntity> erasers) throws ServiceException {
        if (CollectionUtils.isEmpty(erasers)) return "";

        var sqlParts = new StringList();

        // Load and export ConditionSets
        factoryEraserService.loadConditionSet(erasers);
        sqlParts.addNotBlank(conditionSetExportService
                .exportCollectionToSql(CollectionUtils.collect(erasers, TwinFactoryEraserEntity::getConditionSet)));

        // Export Erasers
        sqlParts.addNotBlank(buildInsertsSorted(erasers, TwinFactoryEraserEntity::getId));
        return String.join("\n", sqlParts);
    }
}
