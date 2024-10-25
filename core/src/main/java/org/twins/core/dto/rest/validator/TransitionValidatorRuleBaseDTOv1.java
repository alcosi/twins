package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TransitionValidatorRuleBaseV1")
public class TransitionValidatorRuleBaseDTOv1 extends ValidatorRuleBaseDTOv2 {

    @Schema(description = "twinflow transition id")
    private UUID twinflowTransitionId;

}
