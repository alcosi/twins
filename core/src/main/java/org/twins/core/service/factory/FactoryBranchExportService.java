package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryBranchExportService {
    private final FactoryBranchService factoryBranchService;
    private final FactoryConditionSetExportService conditionSetExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(Set<UUID> branchIds) throws ServiceException {
        return exportToSql(factoryBranchService.findEntitiesSafe(branchIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryBranchEntity> branches) throws ServiceException {
        if (branches.isEmpty()) {
            return "";
        }

        List<String> sqlParts = new ArrayList<>();

        // Load conditionSets for branches
        factoryBranchService.loadConditionSets(branches);
        Set<TwinFactoryConditionSetEntity> conditionSets = new HashSet<>();
        for (TwinFactoryBranchEntity branch : branches) {
            if (branch.getConditionSet() != null) {
                conditionSets.add(branch.getConditionSet());
            }
        }

        // Export ConditionSets and Conditions
        if (!conditionSets.isEmpty()) {
            String conditionSetSql = conditionSetExportService.exportToSql(conditionSets);
            if (!conditionSetSql.isEmpty()) {
                sqlParts.add(conditionSetSql);
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
