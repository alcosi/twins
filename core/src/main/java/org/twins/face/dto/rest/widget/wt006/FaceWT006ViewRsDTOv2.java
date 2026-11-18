package org.twins.face.dto.rest.widget.wt006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceWT006ViewRsV2")
public class FaceWT006ViewRsDTOv2 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - widget details")
    public FaceWT006DTOv2 widget;
}
