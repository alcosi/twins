package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;

@Data
@Accessors(chain = true)
public class I18nSnapshot {
    private Locale locale;
    private String translation;

    public static I18nSnapshot of(Locale locale, String translation) {
        return new I18nSnapshot()
                .setLocale(locale)
                .setTranslation(translation);
    }

    public static void extractTemplateVars(HashMap<String, String> vars, I18nSnapshot snapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "locale", snapshot != null && snapshot.getLocale() != null
                ? snapshot.getLocale().toString()
                : "");
        vars.put(prefix + "translation", snapshot != null && snapshot.getTranslation() != null
                ? snapshot.getTranslation()
                : "");
    }
}
