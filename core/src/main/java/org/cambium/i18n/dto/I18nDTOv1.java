package org.cambium.i18n.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.dao.I18nType;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Locale;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "I18nV1")
public class I18nDTOv1 {
    @Schema(description = "translation in current locale", example = DTOExamples.LOCALE)
    public String translationInCurrentLocale;

    @Schema(description = "map (locale : translate)", example = DTOExamples.TRANSLATION_MAP)
    public Map<Locale, String> translations;

    @JsonIgnore
    public I18nType i18nType;
}
