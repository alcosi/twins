package org.twins.face.dto.rest.widget.wt002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceWT002ViewRsV1")
public class FaceWT002ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - widget details")
    public FaceWT002DTOv1 widget;
}
