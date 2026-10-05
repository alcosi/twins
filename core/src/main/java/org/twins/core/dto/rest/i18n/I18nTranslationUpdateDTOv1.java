package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Locale;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "I18nTranslationUpdateV1")
public class I18nTranslationUpdateDTOv1 {
    @Schema(name = "i18n id")
    public UUID i18nId;

    @Schema(description = "locale", example = DTOExamples.LOCALE)
    public Locale locale;

    @Schema(description = "translation", example = DTOExamples.TRANSLATION)
    public String translation;
}
