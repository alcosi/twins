package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;

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
    @RelatedObject(type = TwinflowTransitionBaseDTOv1.class, name = "twinflowTransition")
    public UUID twinflowTransitionId;

    @Schema(description = "an id of transition trigger featurer", example = DTOExamples.INTEGER)
    public Integer transitionTriggerFeaturerId;

    @Schema(description = "featurer params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public HashMap<String, String> transitionTriggerParams;
}


