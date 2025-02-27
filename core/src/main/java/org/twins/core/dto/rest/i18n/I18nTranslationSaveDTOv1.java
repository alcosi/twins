package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "I18nTranslationSaveV1")
public class I18nTranslationSaveDTOv1 {

    @Schema(description = "map (locale : translate)", example = DTOExamples.TRANSLATION_MAP)
    private Map<Locale, String> translations;
}
