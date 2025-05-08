package org.twins.face.dto.rest.twidget.tw004;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceTW004ViewRsV2")
public class FaceTW004ViewRsDTOv2 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - widget details")
    public FaceTW004DTOv2 widget;
}
