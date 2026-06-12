package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryEraserExportService extends EntityExportService {
    private final FactoryEraserService factoryEraserService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Set<UUID> eraserIds) throws ServiceException {
        return exportToSql(factoryEraserService.findEntitiesSafe(eraserIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryEraserEntity> erasers) throws ServiceException {
        if (erasers.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // Load conditionSets for erasers
        factoryEraserService.loadConditionSets(erasers);
        Set<TwinFactoryConditionSetEntity> conditionSets = new HashSet<>();
        for (TwinFactoryEraserEntity eraser : erasers) {
            if (eraser.getConditionSet() != null) {
                conditionSets.add(eraser.getConditionSet());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSets.isEmpty()) {
            sqlParts.addNotBlank(conditionSetExportService.exportToSql(conditionSets));
        }

        // Export Erasers
        sqlParts.addNotBlank(sqlBuilder.buildInserts(erasers));

        return String.join("\n", sqlParts);
    }
}
