package org.twins.face.dto.rest.twidget.tw006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceTW006ViewRsV1")
public class FaceTW006ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - widget details")
    public FaceTW006DTOv1 widget;
}
