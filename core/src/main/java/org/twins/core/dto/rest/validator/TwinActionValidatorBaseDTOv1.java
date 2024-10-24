package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinActionValidatorBaseV1")
public class TwinActionValidatorBaseDTOv1 extends ValidatorBaseDTOv1 {

    @Schema(description = "twinflow transition id")
    private UUID twinflowTransitionId;

}
