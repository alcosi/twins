package org.twins.face.dto.rest.bc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceBC001ViewRsV1")
public class FaceBC001ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - widget details")
    public FaceBC001DTOv1 breadCrumbs;
}