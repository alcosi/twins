package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nTranslationSaveRsV1")
public class I18nTranslationSaveRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - i18n translations")
    public List<I18nTranslationDTOv1> i18nTranslations;
}
