package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TransitionTriggerCreateRqV1")
public class TransitionTriggerCreateRqDTOv1 extends Request {
    @Schema(description = "transition trigger")
    public TransitionTriggerCreateDTOv1 trigger;
}
