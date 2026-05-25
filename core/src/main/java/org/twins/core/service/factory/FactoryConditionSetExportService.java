package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryConditionSetExportService {
    private final FactoryConditionService factoryConditionService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Collection<TwinFactoryConditionSetEntity> conditionSets) throws ServiceException {
        if (conditionSets.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // ConditionSets
        String conditionSetsSql = sqlBuilder.buildInserts(conditionSets);
        if (!conditionSetsSql.isEmpty()) {
            sqlParts.add(conditionSetsSql);
        }

        // Collect ConditionSet IDs and load Conditions
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryConditionSetEntity conditionSet : conditionSets) {
            conditionSetIds.add(conditionSet.getId());
        }

        List<TwinFactoryConditionEntity> conditions = factoryConditionService.findByTwinFactoryConditionSetIdIn(conditionSetIds);
        if (!conditions.isEmpty()) {
            String conditionsSql = sqlBuilder.buildInserts(conditions);
            if (!conditionsSql.isEmpty()) {
                sqlParts.add(conditionsSql);
            }
        }

        return String.join("\n", sqlParts);
    }
}
