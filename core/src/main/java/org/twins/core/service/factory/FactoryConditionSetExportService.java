package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FactoryConditionSetExportService extends EntityExportService<TwinFactoryConditionSetEntity> {
    private final FactoryConditionService factoryConditionService;
    private final FactoryConditionExportService factoryConditionExportService;

    public String exportCollectionToSql(Collection<TwinFactoryConditionSetEntity> conditionSets) throws ServiceException {
        if (CollectionUtils.isEmpty(conditionSets)) return "";
        var sqlParts = new StringList();

        // ConditionSets
        sqlParts.addNotBlank(sqlBuilder.buildInserts(conditionSets));

        // Conditions
        factoryConditionService.loadConditions(conditionSets);
        exportChildrenKit(
                true,
                conditionSets,
                TwinFactoryConditionSetEntity::getTwinFactoryConditionKit,
                factoryConditionExportService::exportCollectionToSql,
                sqlParts);

        return String.join("\n", sqlParts);
    }
}
