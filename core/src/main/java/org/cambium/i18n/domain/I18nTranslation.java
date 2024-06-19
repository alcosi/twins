package org.cambium.i18n.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
@Accessors(chain = true)
public class I18nTranslation {

    private String translationInCurrentLocale;
    private Map<Locale, String> translations;

    public static Map<Locale, String> createAndGetTranslations(Locale locale, String translation) {
        if (locale == null || translation == null)
            return Collections.emptyMap();
        I18nTranslation i18nTranslation = new I18nTranslation();
        i18nTranslation.translations = new HashMap<>();
        i18nTranslation.translations.put(locale, translation);
        return i18nTranslation.getTranslations();
    }
}
