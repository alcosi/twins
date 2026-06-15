package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryMultiplierExportService extends EntityExportService<TwinFactoryMultiplierEntity> {
    private final FactoryMultiplierService factoryMultiplierService;
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;
    private final FactoryMultiplierFilterExportService multiplierFilterExportService;

    public String exportToSql(Set<UUID> multiplierIds) throws ServiceException {
        return exportCollectionToSql(factoryMultiplierService.findEntitiesSafe(multiplierIds).getList());
    }

    public String exportCollectionToSql(Collection<TwinFactoryMultiplierEntity> multipliers) throws ServiceException {
        if (multipliers.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // Export Multipliers
        sqlParts.addNotBlank(sqlBuilder.buildInserts(multipliers));

        // Load and export MultiplierFilters
        factoryMultiplierFilterService.loadFactoryMultiplierFilters(multipliers);
        exportChildrenKit(
                true,
                multipliers,
                TwinFactoryMultiplierEntity::getTwinFactoryMultiplierFilterKit,
                multiplierFilterExportService::exportCollectionToSql,
                sqlParts);

        return String.join("\n", sqlParts);
    }

}
