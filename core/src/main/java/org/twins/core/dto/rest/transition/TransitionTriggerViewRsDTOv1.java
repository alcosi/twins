package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TransitionTriggerViewRsV1")
public class TransitionTriggerViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "trigger")
    public TransitionTriggerDTOv1 trigger;
}
