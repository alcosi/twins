package org.twins.face.dto.rest.widget.wt005;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceWT005ViewRsV1")
public class FaceWT005ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - widget details")
    public FaceWT005DTOv1 widget;
}
