package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformRsV1")
public class TwinTransitionPerformRsDTOv2 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "major/minor result")
    public TwinTransitionPerformResultDTO result;
}
