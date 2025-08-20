package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TransitionTriggerSaveV1")
public class TransitionTriggerSaveDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "active")
    public Boolean active;

    @Schema(description = "twinflow transition", example = DTOExamples.TWINFLOW_TRANSITION_ID)
    public UUID twinflowTransitionId;

    @Schema(description = "[optional] an id of transition trigger featurer", example = DTOExamples.INTEGER)
    public Integer transitionTriggerFeaturerId;

    @Schema(description = "[optional] head hunter featurer params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public HashMap<String, String> transitionTriggerParams;
}
