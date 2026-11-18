package org.twins.face.dto.rest.widget.wt009;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FaceWT009ViewRsV1")
public class FaceWT009ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - widget details")
    public FaceWT009DTOv1 widget;
}
