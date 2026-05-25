package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryMultiplierExportService {
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;
    private final FactoryConditionSetService factoryConditionSetService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Collection<TwinFactoryMultiplierEntity> multipliers) throws ServiceException {
        if (multipliers.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // Collect Multiplier IDs and load Filters
        Set<UUID> multiplierIds = new HashSet<>();
        for (TwinFactoryMultiplierEntity multiplier : multipliers) {
            multiplierIds.add(multiplier.getId());
        }

        List<TwinFactoryMultiplierFilterEntity> filters = factoryMultiplierFilterService.findByTwinFactoryMultiplierIdIn(multiplierIds);

        // Collect ConditionSet IDs from filters
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryMultiplierFilterEntity filter : filters) {
            if (filter.getTwinFactoryConditionSetId() != null) {
                conditionSetIds.add(filter.getTwinFactoryConditionSetId());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSetIds.isEmpty()) {
            List<UUID> conditionSetIdList = new ArrayList<>(conditionSetIds);
            var conditionSetKit = factoryConditionSetService.findEntitiesSafe(conditionSetIdList);
            if (!conditionSetKit.getList().isEmpty()) {
                String conditionSetSql = conditionSetExportService.exportToSql(conditionSetKit.getList());
                if (!conditionSetSql.isEmpty()) {
                    sqlParts.add(conditionSetSql);
                }
            }
        }

        // Export Multipliers
        String multipliersSql = sqlBuilder.buildInserts(multipliers);
        if (!multipliersSql.isEmpty()) {
            sqlParts.add(multipliersSql);
        }

        // Export MultiplierFilters
        if (!filters.isEmpty()) {
            String filtersSql = sqlBuilder.buildInserts(filters);
            if (!filtersSql.isEmpty()) {
                sqlParts.add(filtersSql);
            }
        }

        return String.join("\n", sqlParts);
    }
}
