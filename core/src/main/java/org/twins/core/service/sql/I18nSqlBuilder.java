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
        return buildI18n(i18n, translations, false);
    }

    /**
     * Upsert variant: emits {@code INSERT ... ON CONFLICT DO UPDATE} for both {@code i18n} (PK: id)
     * and {@code i18n_translation} (composite PK: i18n_id, locale), so re-importing refreshes
     * existing translations instead of leaving stale rows.
     */
    public String buildI18nUpsert(I18nEntity i18n, List<I18nTranslationEntity> translations) {
        return buildI18n(i18n, translations, true);
    }

    private String buildI18n(I18nEntity i18n, List<I18nTranslationEntity> translations, boolean upsert) {
        List<String> parts = new ArrayList<>();

        String i18nSql = upsert ? sqlBuilder.buildUpsert(i18n) : sqlBuilder.buildInsert(i18n);
        if (!i18nSql.isEmpty()) {
            parts.add(i18nSql);
        }

        if (translations != null && !translations.isEmpty()) {
            for (I18nTranslationEntity translation : translations) {
                String transSql = upsert ? sqlBuilder.buildUpsert(translation) : sqlBuilder.buildInsert(translation);
                if (!transSql.isEmpty()) {
                    parts.add(transSql);
                }
            }
        }

        String result = String.join("\n", parts);
        return result.isEmpty() ? "" : result + "\n";
    }
}
