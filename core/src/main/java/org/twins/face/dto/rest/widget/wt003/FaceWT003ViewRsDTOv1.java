package org.twins.face.dto.rest.widget.wt003;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceWT003ViewRsV1")
public class FaceWT003ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - widget details")
    public FaceWT003DTOv1 widget;
}
