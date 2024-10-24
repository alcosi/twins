package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorBaseV2")
public class ValidatorBaseDTOv2 extends ValidatorBaseDTOv1 {

    @Schema(description = "grouping set of twin validator")
    public TwinValidatorSetBaseDTOv1 twinValidatorSet;

}
