package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryTriggerExportService extends EntityExportService {
    private final FactoryTriggerService factoryTriggerService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Set<UUID> triggerIds) throws ServiceException {
        return exportToSql(factoryTriggerService.findEntitiesSafe(triggerIds).getCollection());
    }

    public String exportToSql(Collection<TwinFactoryTriggerEntity> triggers) throws ServiceException {
        if (triggers.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // Load conditionSets for triggers
        factoryTriggerService.loadConditionSets(triggers);
        Set<TwinFactoryConditionSetEntity> conditionSets = new HashSet<>();
        for (TwinFactoryTriggerEntity trigger : triggers) {
            if (trigger.getTwinFactoryConditionSet() != null) {
                conditionSets.add(trigger.getTwinFactoryConditionSet());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSets.isEmpty()) {
            sqlParts.addNotBlank(conditionSetExportService.exportToSql(conditionSets));
        }

        // Export Triggers
        sqlParts.addNotBlank(sqlBuilder.buildInserts(triggers));

        return String.join("\n", sqlParts);
    }
}
