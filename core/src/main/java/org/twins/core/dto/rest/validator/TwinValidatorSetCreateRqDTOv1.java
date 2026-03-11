package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorSetCreateRqV1")
public class TwinValidatorSetCreateRqDTOv1 extends Request {

    @Schema(description = "twin validator set list")
    public List<TwinValidatorSetCreateDTOv1> validatorSets;

}
