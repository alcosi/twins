package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "I18nTranslationUpdateV1")
public class I18nTranslationUpdateDTOv1 extends I18nTranslationSaveDTOv1 {
    @Schema(name = "i18n id")
    public UUID i18nId;
}
