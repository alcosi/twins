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
    @Schema(description = "translation in current locale")
    public String translationInCurrentLocale;

    @Schema(description = "map (locale : translate)")
    public Map<Locale, String> translations;
}
