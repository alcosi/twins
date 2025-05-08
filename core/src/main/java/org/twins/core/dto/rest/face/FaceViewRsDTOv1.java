package org.twins.core.dto.rest.face;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceViewRsV1")
public class FaceViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - face details")
    public FaceDTOv1 face;
}
