package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSetCountV1")
public class TwinValidatorSetCountDTOv1 extends CountDTOv1 {
    @Schema(description = "flag to invert the validator set result")
    public Boolean invert;
}
