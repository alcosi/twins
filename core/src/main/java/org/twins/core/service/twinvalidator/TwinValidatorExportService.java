package org.twins.core.service.twinvalidator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinValidatorExportService extends EntityExportService<TwinValidatorEntity> {
    private final TwinValidatorService twinValidatorService;

    /**
     * twin_validator has NO i18n fields (no nameI18nId / descriptionI18nId), so the export is just the
     * entity UPSERTs — i18nExportService is intentionally NOT used here. The parent twin_validator_set
     * is a FK dependency expected to already exist in the target database.
     */
    @Override
    public String exportCollectionToSql(Collection<TwinValidatorEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return "";
        }
        return buildUpsertsSorted(entities, TwinValidatorEntity::getId);
    }

    /**
     * Collection overload matching the export controller template
     * ({@code exportService.exportToSql(request.getTwinValidatorIds())}).
     */
    public String exportToSql(Collection<UUID> twinValidatorIds) throws ServiceException {
        return exportCollectionToSql(twinValidatorService.findEntitiesSafe(twinValidatorIds).getCollection());
    }
}
