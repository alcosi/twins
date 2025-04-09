package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Locale;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldI18nV1")
public class TwinFieldI18nDTOv1 {

    @Schema(description = "locale", example = DTOExamples.LOCALE)
    public Locale locale;

    @Schema(description = "translation", example = DTOExamples.TRANSLATION)
    public String translation;
}
