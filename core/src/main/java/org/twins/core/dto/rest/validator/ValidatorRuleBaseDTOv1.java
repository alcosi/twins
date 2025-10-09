package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

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
    @RelatedObject(type = TwinDTOv2.class, name = "twinValidatorSet")
    public UUID twinValidatorSetId;;

    @Schema(description = "Twin validator list")
    public List<TwinValidatorBaseDTOv1> twinValidators;

    @Schema(description = "grouping set of twin validator")
    public TwinValidatorSetBaseDTOv1 twinValidatorSet;
}


