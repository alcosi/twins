package org.twins.face.dto.rest.tc.tc002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceTC002ViewRsV1")
public class FaceTC002ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "resilt - face twin create")
    public FaceTC002DTOv1 faceTwinCreate;
}
