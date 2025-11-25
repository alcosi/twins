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
@Schema(name = "I18nV1")
public class I18nDTOv1 {
    @Schema(description = "i18n id", example = DTOExamples.I18N_ID)
    public UUID i18nId;

    @Schema(description = "key", example = DTOExamples.I18N_KEY)
    public String key;

    @Schema(description = "key", example = DTOExamples.I18N_NAME)
    public String name;

    @Schema(description = "translations", example = DTOExamples.I18N_ID)
    public Map<Locale, String> translations;
}


