package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryEraserExportService {
    private final FactoryEraserService factoryEraserService;
    private final FactoryConditionSetService factoryConditionSetService;
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

        // Collect ConditionSet IDs
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryEraserEntity eraser : erasers) {
            if (eraser.getTwinFactoryConditionSetId() != null) {
                conditionSetIds.add(eraser.getTwinFactoryConditionSetId());
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

        // Export Erasers
        String erasersSql = sqlBuilder.buildInserts(erasers);
        if (!erasersSql.isEmpty()) {
            sqlParts.add(erasersSql);
        }

        return String.join("\n", sqlParts);
    }
}
