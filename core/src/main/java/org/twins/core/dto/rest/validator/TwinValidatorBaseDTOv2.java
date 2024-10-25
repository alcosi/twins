package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorBaseV2")
public class TwinValidatorBaseDTOv2  extends TwinValidatorBaseDTOv1{
    @Schema(description = "grouping set of twin validator")
    public TwinValidatorSetBaseDTOv1 twinValidatorSet;
}
