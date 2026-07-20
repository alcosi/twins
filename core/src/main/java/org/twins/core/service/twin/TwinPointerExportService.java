package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinPointerExportService extends EntityExportService<TwinPointerEntity> {
    private final TwinPointerService twinPointerService;

    /**
     * twin_pointer has NO i18n fields (no nameI18nId / descriptionI18nId), so the export is just the
     * entity INSERTs — i18nExportService is intentionally NOT used here.
     */
    @Override
    public String exportCollectionToSql(Collection<TwinPointerEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) return "";
        return buildUpsertsSorted(entities, TwinPointerEntity::getId);
    }

    /**
     * Collection overload matching the api_starter.md export controller template
     * ({@code exportService.exportToSql(entities.getCollection())}).
     */
    public String exportToSql(Collection<UUID> twinPointerIds) throws ServiceException {
        return exportCollectionToSql(twinPointerService.findEntitiesSafe(twinPointerIds).getCollection());
    }
}
