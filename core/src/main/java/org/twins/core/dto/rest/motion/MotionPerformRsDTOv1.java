package org.twins.core.dto.rest.motion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultDTO;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "MotionPerformRsV1")
public class MotionPerformRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "major/minor result")
    public TwinTransitionPerformResultDTO result;
}
