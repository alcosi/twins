package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.EntityExportService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinClassFieldExportService extends EntityExportService<TwinClassFieldEntity> {
    public String exportCollectionToSql(Collection<TwinClassFieldEntity> fields) throws ServiceException {
        if (CollectionUtils.isEmpty(fields)) {
            return "";
        }
        Set<UUID> i18nIds = i18nService.collectI18nIds(fields,
                TwinClassFieldEntity::getNameI18nId,
                TwinClassFieldEntity::getDescriptionI18nId);
        var sqlParts = new StringList();
        sqlParts.addNotBlank(i18nExportService.exportToSql(i18nIds));
        sqlParts.addNotBlank(sqlBuilder.buildInserts(fields));

        return String.join("\n", sqlParts);
    }
}
