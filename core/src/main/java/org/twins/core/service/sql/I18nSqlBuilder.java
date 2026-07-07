package org.twins.core.service.sql;

import lombok.RequiredArgsConstructor;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class I18nSqlBuilder {
    private final SqlBuilder sqlBuilder;

    public String buildI18nInsert(I18nEntity i18n, List<I18nTranslationEntity> translations) {
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
