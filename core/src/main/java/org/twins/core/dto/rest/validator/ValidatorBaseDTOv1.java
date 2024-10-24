package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorBaseV1")
public class ValidatorBaseDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "grouped set of twin validators id")
    public UUID twinValidatorSetId;;

    @Schema(description = "Twin validator list")
    public List<TwinValidatorBaseDTOv1> twinValidators;

}
