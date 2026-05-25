package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryBranchExportService {
    private final FactoryConditionSetService factoryConditionSetService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Collection<TwinFactoryBranchEntity> branches) throws ServiceException {
        if (branches.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // Collect ConditionSet IDs
        Set<UUID> conditionSetIds = new HashSet<>();
        for (TwinFactoryBranchEntity branch : branches) {
            if (branch.getTwinFactoryConditionSetId() != null) {
                conditionSetIds.add(branch.getTwinFactoryConditionSetId());
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

        // Export Branches
        String branchesSql = sqlBuilder.buildInserts(branches);
        if (!branchesSql.isEmpty()) {
            sqlParts.add(branchesSql);
        }

        return String.join("\n", sqlParts);
    }
}
