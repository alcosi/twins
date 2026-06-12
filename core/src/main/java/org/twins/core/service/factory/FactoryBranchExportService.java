package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryBranchExportService extends EntityExportService {
    private final FactoryBranchService factoryBranchService;
    private final FactoryConditionSetExportService conditionSetExportService;

    public String exportToSql(Set<UUID> branchIds) throws ServiceException {
        return exportToSql(factoryBranchService.findEntitiesSafe(branchIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryBranchEntity> branches) throws ServiceException {
        if (CollectionUtils.isEmpty(branches)) return "";
        var sqlParts = new StringList();
        // Load and export ConditionSets
        factoryBranchService.loadConditionSets(branches);
        sqlParts.addNotBlank(conditionSetExportService.exportToSql(
                CollectionUtils.collect(branches, TwinFactoryBranchEntity::getConditionSet)));
        // Export Branches
        sqlParts.addNotBlank(sqlBuilder.buildInserts(branches));
        return String.join("\n", sqlParts);
    }
}
