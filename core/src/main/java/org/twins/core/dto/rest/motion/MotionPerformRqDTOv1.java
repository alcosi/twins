package org.twins.core.dto.rest.motion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "MotionPerformRqV1")
public class MotionPerformRqDTOv1 extends Request {
    @Schema(description = "Target twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema
    public String comment;

    @Schema(description = "some extra data to perform motion")
    public MotionContextDTOv1 context;
}
