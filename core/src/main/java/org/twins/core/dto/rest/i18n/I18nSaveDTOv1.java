package org.twins.core.dto.rest.i18n;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Locale;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "I18nSaveV1")
public class I18nSaveDTOv1 {
    @Schema(description = "translation in current locale", example = DTOExamples.TRANSLATION)
    public String translationInCurrentLocale;

    @Schema(description = "map (locale : translate)", example = DTOExamples.TRANSLATION_MAP)
    public Map<Locale, String> translations;

    @JsonIgnore
    public I18nType i18nType;
}
