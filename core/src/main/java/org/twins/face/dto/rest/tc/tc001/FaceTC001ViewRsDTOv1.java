package org.twins.face.dto.rest.tc.tc001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceTC001ViewRsV1")
public class FaceTC001ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - face twin create")
    public FaceTC001DTOv1 faceTwinCreate;
}
