package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.service.EntityExportService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FactoryMultiplierExportService extends EntityExportService {
    private final FactoryMultiplierService factoryMultiplierService;
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;
    private final FactoryMultiplierFilterExportService multiplierFilterExportService;

    public String exportToSql(Set<UUID> multiplierIds) throws ServiceException {
        return exportToSql(factoryMultiplierService.findEntitiesSafe(multiplierIds).getList());
    }

    public String exportToSql(Collection<TwinFactoryMultiplierEntity> multipliers) throws ServiceException {
        if (multipliers.isEmpty()) {
            return "";
        }

        var sqlParts = new StringList();

        // Export Multipliers
        sqlParts.addNotBlank(sqlBuilder.buildInserts(multipliers));

        // Load and export MultiplierFilters
        factoryMultiplierFilterService.loadFactoryMultiplierFilters(multipliers);
        List<TwinFactoryMultiplierFilterEntity> filters = new ArrayList<>();
        for (TwinFactoryMultiplierEntity multiplier : multipliers) {
            filters.addAll(multiplier.getTwinFactoryMultiplierFilterKit().getCollection());
        }
        if (!filters.isEmpty()) {
            sqlParts.addNotBlank(multiplierFilterExportService.exportToSql(filters));
        }

        return String.join("\n", sqlParts);
    }
}
