package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nViewRsV1")
public class I18nViewRsDTOv1 extends Response {
    @Schema(description = "i18n")
    public I18nDTOv1 i18n;
}
