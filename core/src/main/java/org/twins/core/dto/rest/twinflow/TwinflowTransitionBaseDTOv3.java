package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.validator.TransitionValidatorRuleBaseDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinflowTransitionBaseV3")
public class TwinflowTransitionBaseDTOv3 extends TwinflowTransitionBaseDTOv2 {

    @Schema(description = "validators")
    public List<TransitionValidatorRuleBaseDTOv1> validatorRules;

    @Schema(description = "triggers")
    public List<TriggerDTOv1> triggers;
}
