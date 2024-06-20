package org.twins.core.dto.rest.twinstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusV2")
public class TwinStatusDTOv2 extends TwinStatusDTOv1 {
    @Schema(description = "translation of names")
    public I18nDTOv1 translationName;

    @Schema(description = "translation of descriptions")
    public I18nDTOv1 translationDescription;
}
