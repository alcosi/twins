package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nTranslationSearchRqV1")
public class I18nTranslationSearchRqDTOv1 extends Request {
    @Schema(description = "i18n basic search")
    public I18nTranslationSearchDTOv1 search;

}
