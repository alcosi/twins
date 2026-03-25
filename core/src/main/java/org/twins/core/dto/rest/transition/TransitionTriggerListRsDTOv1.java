package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TransitionTriggerListRsV1")
public class TransitionTriggerListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "transition triggers")
    public List<TransitionTriggerDTOv1> transitionTriggers;
}
