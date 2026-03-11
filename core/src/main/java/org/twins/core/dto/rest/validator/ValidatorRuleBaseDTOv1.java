package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorRuleBaseV1")
public class ValidatorRuleBaseDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "active")
    public Boolean active;

    @Schema(description = "grouped set of twin validators id")
    public UUID twinValidatorSetId;;

    @Schema(description = "Twin validator list")
    public List<TwinValidatorDTOv1> twinValidators;

    @Schema(description = "grouping set of twin validator")
    public TwinValidatorSetDTOv1 twinValidatorSet;
}


