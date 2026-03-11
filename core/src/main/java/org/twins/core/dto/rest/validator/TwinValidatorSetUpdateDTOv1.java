package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinValidatorSetUpdateV1")
public class TwinValidatorSetUpdateDTOv1 extends TwinValidatorSetSaveDTOv1 {

    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

}
