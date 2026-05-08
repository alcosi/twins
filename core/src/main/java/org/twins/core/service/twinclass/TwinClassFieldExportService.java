package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TwinClassFieldExportService {
    private final SqlBuilder sqlBuilder;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;

    public String exportToSql(Collection<TwinClassFieldEntity> fields) throws ServiceException {
        if (fields.isEmpty()) {
            return "";
        }

        Set<UUID> i18nIds = i18nService.collectI18nIds(fields,
                TwinClassFieldEntity::getNameI18nId,
                TwinClassFieldEntity::getDescriptionI18nId);

        List<String> sqlParts = new ArrayList<>();

        if (!i18nIds.isEmpty()) {
            String i18nSql = i18nExportService.exportToSql(i18nIds);
            if (!i18nSql.isEmpty()) {
                sqlParts.add(i18nSql);
            }
        }

        String fieldsSql = sqlBuilder.buildInserts(fields);
        if (!fieldsSql.isEmpty()) {
            sqlParts.add(fieldsSql);
        }

        return String.join("\n", sqlParts);
    }
}
