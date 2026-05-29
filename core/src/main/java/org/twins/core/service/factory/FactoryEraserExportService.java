package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryEraserExportService {
    private final FactoryEraserService factoryEraserService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Set<UUID> eraserIds) throws ServiceException {
        return exportToSql(factoryEraserService.findEntitiesSafe(eraserIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryEraserEntity> erasers) throws ServiceException {
        if (erasers.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

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
            String conditionSetSql = conditionSetExportService.exportToSql(conditionSets);
            if (!conditionSetSql.isEmpty()) {
                sqlParts.add(conditionSetSql);
            }
        }

        // Export Erasers
        String erasersSql = sqlBuilder.buildInserts(erasers);
        if (!erasersSql.isEmpty()) {
            sqlParts.add(erasersSql);
        }

        return String.join("\n", sqlParts);
    }
}
