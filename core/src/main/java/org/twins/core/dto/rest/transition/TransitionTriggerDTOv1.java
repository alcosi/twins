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
@Schema(name = "TransitionTriggerV1")
public class TransitionTriggerDTOv1 {
    @Schema(description = "id", example = DTOExamples.TRIGGER_ID)
    public UUID id;

    @Schema(description = "twinflow transition Id", example = DTOExamples.TWINFLOW_ID)
    @RelatedObject(type = TwinflowTransitionBaseDTOv1.class, name = "twinflowTransition")
    public UUID twinflowTransitionId;

    @Schema(description = "order", example = DTOExamples.INTEGER)
    public Integer order;

    @Schema(description = "[optional] an id of transition trigger featurer", example = DTOExamples.INTEGER)
    public Integer transitionTriggerFeaturerId;

    @Schema(description = "[optional] head hunter featurer params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public HashMap<String, String> transitionTriggerParams;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public boolean active;
}


