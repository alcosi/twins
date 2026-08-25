package org.twins.core.service.twinstatus;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinStatusExportService extends EntityExportService<TwinStatusEntity> {
    private final TwinStatusService twinStatusService;

    @Override
    public String exportCollectionToSql(Collection<TwinStatusEntity> statuses) throws ServiceException {
        if (CollectionUtils.isEmpty(statuses)) {
            return "";
        }
        Set<UUID> i18nIds = i18nService.collectI18nIds(statuses,
                TwinStatusEntity::getNameI18nId,
                TwinStatusEntity::getDescriptionI18nId);
        var sqlParts = new StringList();
        sqlParts.addNotBlank(i18nExportService.exportToSql(i18nIds));
        sqlParts.addNotBlank(buildUpsertsSorted(statuses, TwinStatusEntity::getId));
        return String.join("\n", sqlParts);
    }

    public String exportToSql(Collection<UUID> statusIds) throws ServiceException {
        return exportCollectionToSql(twinStatusService.findEntitiesSafe(statusIds).getCollection());
    }
}
