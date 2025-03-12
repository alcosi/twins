package org.twins.core.dto.rest.face;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceViewV1")
public class FaceViewDTOv1 extends FaceBasicDTOv1 {
    @Schema(description = "name", example = DTOExamples.FACE_COMPONENT)
    public String name;

    @Schema(description = "description", example = DTOExamples.FACE_CONFIG_ID)
    public String description;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "createdByUserId")
    public UUID createdByUserId;
}
