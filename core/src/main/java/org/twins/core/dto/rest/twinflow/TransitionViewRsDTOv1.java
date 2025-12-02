package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TransitionViewRsV1")
public class TransitionViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "transition details")
    public TwinflowTransitionBaseDTOv2 transition;
}
