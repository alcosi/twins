package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TransitionTriggerUpdateRqV1")
public class TransitionTriggerUpdateRqDTOv1 extends Request {
    @Schema(description = "transition triggers")
    public List<TransitionTriggerUpdateDTOv1> transitionTriggers;
}
