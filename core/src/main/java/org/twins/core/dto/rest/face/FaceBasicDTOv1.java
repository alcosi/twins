package org.twins.core.dto.rest.face;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FacePointerV1")
public class FaceBasicDTOv1 {
    @Schema(description = "component", example = DTOExamples.FACE_COMPONENT)
    public String component;

    @Schema(description = "config id", example = DTOExamples.FACE_CONFIG_ID)
    public UUID configId;
}
