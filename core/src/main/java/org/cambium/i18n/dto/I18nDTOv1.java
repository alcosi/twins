package org.cambium.i18n.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "I18nV1")
public class I18nDTOv1 {
    @Schema(description = "translate")
    public String translationInCurrentLocale;

    @Schema(description = "map (locale : translate)")
    public Map<Locale, String> translations;

    public static I18nDTOv1 createI18n(String name) {
        return new I18nDTOv1().setTranslationInCurrentLocale(name);
    }

    public static I18nDTOv1 empty() {
        return new I18nDTOv1().setTranslationInCurrentLocale("");
    }
}
