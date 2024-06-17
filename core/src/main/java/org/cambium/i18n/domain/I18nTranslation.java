package org.cambium.i18n.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.Map;

@Data
@Accessors(chain = true)
public class I18nTranslation {
    private String translate;
    private Map<Locale, String> translations;

    public static I18nTranslation createTranslate(String translate) {
        return new I18nTranslation().setTranslate(translate);
    }

    public static I18nTranslation empty(){
        return new I18nTranslation().setTranslate("");
    }
}
