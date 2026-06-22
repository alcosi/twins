package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FactoryMultiplierFilterExportService extends EntityExportService<TwinFactoryMultiplierFilterEntity> {
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportCollectionToSql(Collection<TwinFactoryMultiplierFilterEntity> filters) throws ServiceException {
        if (CollectionUtils.isEmpty(filters)) return "";
        var sqlParts = new StringList();
        // Load and export ConditionSets
        factoryMultiplierFilterService.loadConditionSets(filters);
        sqlParts.addNotBlank(conditionSetExportService.exportCollectionToSql(
                CollectionUtils.collect(filters, TwinFactoryMultiplierFilterEntity::getConditionSet)));
        // Export MultiplierFilters
        sqlParts.addNotBlank(sqlBuilder.buildInserts(filters));
        return String.join("\n", sqlParts);
    }
}
