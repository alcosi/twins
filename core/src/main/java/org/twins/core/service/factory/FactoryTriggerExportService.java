package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
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
        if (CollectionUtils.isEmpty(triggers)) return "";

        var sqlParts = new StringList();

        // Load and export ConditionSets
        factoryTriggerService.loadConditionSets(triggers);
        sqlParts.addNotBlank(conditionSetExportService.exportToSql(
                CollectionUtils.collect(triggers, TwinFactoryTriggerEntity::getTwinFactoryConditionSet)));

        // Export Triggers
        sqlParts.addNotBlank(sqlBuilder.buildInserts(triggers));

        return String.join("\n", sqlParts);
    }
}
