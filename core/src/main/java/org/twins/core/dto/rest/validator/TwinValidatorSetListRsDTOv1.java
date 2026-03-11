package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorSetListRsV1")
public class TwinValidatorSetListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "twin validator set list")
    public List<TwinValidatorSetDTOv1> validatorSets;

}
