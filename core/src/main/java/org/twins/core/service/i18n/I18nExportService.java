package org.twins.core.service.i18n;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.service.sql.I18nSqlBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class I18nExportService {
    private final I18nService i18nService;
    private final I18nSqlBuilder i18nSqlBuilder;

    public String exportToSql(Set<UUID> i18nIds) throws ServiceException {
        if (CollectionUtils.isEmpty(i18nIds)) return "";
        List<I18nEntity> i18nEntities = i18nService.findEntitiesSafe(i18nIds).getList();
        i18nService.loadTranslations(i18nEntities);

        // Sort by id so the export is deterministic (diff-able). Input is a HashSet/Set-derived Kit
        // whose order is not guaranteed.
        i18nEntities.sort(Comparator.comparing(
                I18nEntity::getId, Comparator.nullsFirst(Comparator.naturalOrder())));

        StringBuilder result = new StringBuilder();
        for (I18nEntity i18n : i18nEntities) {
            // Sort translations by (i18nId, locale tag). I18nTranslationEntity has no single id —
            // its PK is (i18n_id, locale). toLanguageTag() is stable across JVMs (toString() is not).
            List<I18nTranslationEntity> translations = i18n.getTranslationsKit() != null
                    ? new ArrayList<>(i18n.getTranslationsKit().getList())
                    : Collections.emptyList();
            translations.sort(Comparator
                    .comparing(I18nTranslationEntity::getI18nId, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(t -> t.getLocale() != null ? t.getLocale().toLanguageTag() : null,
                            Comparator.nullsFirst(Comparator.naturalOrder())));
            String i18nSql = i18nSqlBuilder.buildI18nUpsert(i18n, translations);
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
