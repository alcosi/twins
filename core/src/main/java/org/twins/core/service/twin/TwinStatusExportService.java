package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinStatusExportService {
    private final SqlBuilder sqlBuilder;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;

    public String exportToSql(Collection<TwinStatusEntity> statuses) throws ServiceException {
        if (statuses.isEmpty()) {
            return "";
        }

        Set<UUID> i18nIds = i18nService.collectI18nIds(statuses,
                TwinStatusEntity::getNameI18nId,
                TwinStatusEntity::getDescriptionI18nId);

        var sqlParts = new StringList();

        if (!i18nIds.isEmpty()) {
            sqlParts.addNotBlank(i18nExportService.exportToSql(i18nIds));
        }

        sqlParts.addNotBlank(sqlBuilder.buildInserts(statuses));

        return String.join("\n", sqlParts);
    }
}
