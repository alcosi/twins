package org.twins.core.service.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkValidatorExportService extends EntityExportService<LinkValidatorEntity> {
    private final LinkValidatorService linkValidatorService;

    @Override
    public String exportCollectionToSql(Collection<LinkValidatorEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return "";
        }
        // no i18n fields on link_validator — entities only
        return buildUpsertsSorted(entities, LinkValidatorEntity::getId);
    }

    public String exportToSql(Collection<UUID> linkValidatorIds) throws ServiceException {
        return exportCollectionToSql(linkValidatorService.findEntitiesSafe(linkValidatorIds).getCollection());
    }
}
