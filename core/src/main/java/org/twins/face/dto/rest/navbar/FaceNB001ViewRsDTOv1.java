package org.twins.face.dto.rest.navbar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceNB001ViewRsV1")
public class FaceNB001ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - navbar details")
    public FaceNB001DTOv1 navbar;
}
