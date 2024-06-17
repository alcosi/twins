package org.cambium.i18n.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "I18nSaveV1")
public class I18nSaveDTOv1 {
    @Schema //todo description schema
    public String translationInCurrentLocale;
    @Schema //todo description schema
    public Map<Locale, String> translations;
}
