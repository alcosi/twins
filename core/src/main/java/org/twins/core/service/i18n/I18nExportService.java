package org.twins.core.service.i18n;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.service.sql.I18nSqlBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class I18nExportService {
    private final I18nService i18nService;
    private final I18nSqlBuilder i18nSqlBuilder;

    public void appendI18nSql(StringBuilder sql, Set<UUID> i18nIds) throws ServiceException {
        List<I18nEntity> i18nEntities = i18nService.findEntitiesSafe(i18nIds).getList();
        i18nService.loadTranslations(i18nEntities);

        for (I18nEntity i18n : i18nEntities) {
            String i18nSql = i18nSqlBuilder.buildI18nInsert(i18n,
                    i18n.getTranslationsKit() != null ? new ArrayList<>(i18n.getTranslationsKit()) : Collections.emptyList());
            if (!i18nSql.isEmpty()) {
                if (!sql.isEmpty()) sql.append("\n");
                sql.append(i18nSql);
            }
        }
    }
}
