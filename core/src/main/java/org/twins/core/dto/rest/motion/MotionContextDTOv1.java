package org.twins.core.dto.rest.motion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name =  "MotionContextV1")
public class MotionContextDTOv1 {
    @Schema(description = "data")
    public Map<String, String> data; //for future use
}
