package org.twins.core.service.twinclassfield;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.service.EntityExportService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinClassFieldValidatorExportService extends EntityExportService<TwinClassFieldValidatorEntity> {
    private final TwinClassFieldValidatorService twinClassFieldValidatorService;

    @Override
    public String exportCollectionToSql(Collection<TwinClassFieldValidatorEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return "";
        }
        Set<UUID> i18nIds = i18nService.collectI18nIds(entities,
                TwinClassFieldValidatorEntity::getBeValidationErrorI18nId,
                e -> null);
        List<String> sqlParts = new ArrayList<>();
        if (!i18nIds.isEmpty()) {
            sqlParts.add(i18nExportService.exportToSql(i18nIds));
        }
        sqlParts.add(buildUpsertsSorted(entities, TwinClassFieldValidatorEntity::getId));
        return String.join("\n", sqlParts);
    }

    public String exportToSql(Collection<UUID> twinClassFieldValidatorIds) throws ServiceException {
        return exportCollectionToSql(twinClassFieldValidatorService.findEntitiesSafe(twinClassFieldValidatorIds).getCollection());
    }
}
