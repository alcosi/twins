package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.service.EntityExportService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryConditionSetExportService extends EntityExportService {
    private final FactoryConditionService factoryConditionService;

    public String exportToSql(Collection<TwinFactoryConditionSetEntity> conditionSets) throws ServiceException {
        if (conditionSets.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // ConditionSets
        sqlParts.addNotBlank(sqlBuilder.buildInserts(conditionSets));

        // Collect ConditionSet IDs and load Conditions
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryConditionSetEntity conditionSet : conditionSets) {
            conditionSetIds.add(conditionSet.getId());
        }

        List<TwinFactoryConditionEntity> conditions = factoryConditionService.findByTwinFactoryConditionSetIdIn(conditionSetIds);
        if (!conditions.isEmpty()) {
            sqlParts.addNotBlank(sqlBuilder.buildInserts(conditions));
        }

        return String.join("\n", sqlParts);
    }
}
