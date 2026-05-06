package org.twins.core.service.sql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class I18nSqlBuilder {
    private final SqlBuilder sqlBuilder;

    public String buildI18nInsert(I18nEntity i18n, List<I18nTranslationEntity> translations) {
        // Force initialization of lazy fields by touching them
        i18n.getId();
        i18n.getDomainId();
        i18n.getName();
        i18n.getKey();
        i18n.getType(); // Enum with @Convert - needs initialization

        List<String> parts = new ArrayList<>();

        String i18nSql = sqlBuilder.buildInsert(i18n);
        if (!i18nSql.isEmpty()) {
            parts.add(i18nSql);
        }

        if (translations != null && !translations.isEmpty()) {
            for (I18nTranslationEntity translation : translations) {
                String transSql = sqlBuilder.buildInsert(translation);
                if (!transSql.isEmpty()) {
                    parts.add(transSql);
                }
            }
        }

        String result = String.join("\n", parts);
        return result.isEmpty() ? "" : result + "\n";
    }
}
