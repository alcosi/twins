package org.twins.core.service.i18n;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.service.sql.I18nSqlBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class I18nExportService {
    private final I18nService i18nService;
    private final I18nSqlBuilder i18nSqlBuilder;

    public String exportToSql(Set<UUID> i18nIds) throws ServiceException {
        List<I18nEntity> i18nEntities = i18nService.findEntitiesSafe(i18nIds).getList();
        i18nService.loadTranslations(i18nEntities);

        StringBuilder result = new StringBuilder();
        for (I18nEntity i18n : i18nEntities) {
            String i18nSql = i18nSqlBuilder.buildI18nInsert(i18n,
                    i18n.getTranslationsKit() != null ? i18n.getTranslationsKit().getList() : Collections.emptyList());
            if (!i18nSql.isEmpty()) {
                if (!result.isEmpty()) result.append("\n");
                result.append(i18nSql);
            }
        }
        return result.toString();
    }

    public void addExportSafe(Set<UUID> i18nIds, StringList sqlParts) throws ServiceException {
        if (!i18nIds.isEmpty()) {
            sqlParts.addNotBlank(exportToSql(i18nIds));
        }
    }
}
