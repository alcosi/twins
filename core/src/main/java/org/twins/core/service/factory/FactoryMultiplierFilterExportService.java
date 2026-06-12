package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FactoryMultiplierFilterExportService extends EntityExportService {
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Collection<TwinFactoryMultiplierFilterEntity> filters) throws ServiceException {
        if (filters.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // Load conditionSets for filters
        factoryMultiplierFilterService.loadConditionSets(filters);
        Set<TwinFactoryConditionSetEntity> conditionSets = new HashSet<>();
        for (TwinFactoryMultiplierFilterEntity filter : filters) {
            if (filter.getConditionSet() != null) {
                conditionSets.add(filter.getConditionSet());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSets.isEmpty()) {
            sqlParts.addNotBlank(conditionSetExportService.exportToSql(conditionSets));
        }

        // Export MultiplierFilters
        sqlParts.addNotBlank(sqlBuilder.buildInserts(filters));

        return String.join("\n", sqlParts);
    }
}
