package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryTriggerExportService {
    private final FactoryTriggerService factoryTriggerService;
    private final FactoryConditionSetService factoryConditionSetService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Set<UUID> triggerIds) throws ServiceException {
        return exportToSql(factoryTriggerService.findEntitiesSafe(triggerIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryTriggerEntity> triggers) throws ServiceException {
        if (triggers.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // Collect ConditionSet IDs
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryTriggerEntity trigger : triggers) {
            if (trigger.getTwinFactoryConditionSetId() != null) {
                conditionSetIds.add(trigger.getTwinFactoryConditionSetId());
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

        // Export Triggers
        String triggersSql = sqlBuilder.buildInserts(triggers);
        if (!triggersSql.isEmpty()) {
            sqlParts.add(triggersSql);
        }

        return String.join("\n", sqlParts);
    }
}
