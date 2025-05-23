package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.validator.cud.TransitionValidatorRuleCudDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TransitionUpdateRqV1")
public class TransitionUpdateRqDTOv1 extends TransitionSaveRqDTOv1 {
    @Schema(description = "validator rules cud operations")
    public TransitionValidatorRuleCudDTOv1 validatorRules;
}
