package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nTranslationUpdateRqV1")
public class I18nTranslationUpdateRqDTOv1 extends Request {
    @Schema(description = "i18n translation update")
    public I18nTranslationUpdateDTOv1 i18nTranslation;
}
