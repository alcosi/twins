package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.dto.I18nDTOv1;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusV2")
public class TwinStatusDTOv2 extends TwinStatusDTOv1 {
    @Schema(description = "translation of names")
    public I18nDTOv1 translationName;

    @Schema(description = "translation of descriptions")
    public I18nDTOv1 translationDescription;
}
