package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TransitionTriggerUpdateV1")
public class TransitionTriggerUpdateDTOv1 {
    @NotNull
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "active")
    public Boolean active;

    @Schema(description = "async")
    public Boolean async;

    @Schema(description = "twinflow transition", example = DTOExamples.TWINFLOW_TRANSITION_ID)
    public UUID twinflowTransitionId;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    public UUID twinTriggerId;
}
