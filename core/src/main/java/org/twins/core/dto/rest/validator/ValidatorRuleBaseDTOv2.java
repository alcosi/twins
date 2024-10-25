package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorRuleBaseV2")
public class ValidatorRuleBaseDTOv2 extends ValidatorRuleBaseDTOv1 {

    @Schema(description = "grouping set of twin validator")
    public TwinValidatorSetBaseDTOv1 twinValidatorSet;

}
