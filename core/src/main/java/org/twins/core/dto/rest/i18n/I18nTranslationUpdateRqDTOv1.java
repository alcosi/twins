package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nUpdateRqV1")
public class I18nTranslationUpdateRqDTOv1 extends Request {
    @Schema(description = "i18n translations update")
    public List<I18nTranslationUpdateDTOv1> translations;
}
