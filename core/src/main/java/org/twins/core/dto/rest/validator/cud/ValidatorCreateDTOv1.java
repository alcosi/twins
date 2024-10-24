package org.twins.core.dto.rest.validator.cud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.validator.TwinValidatorBaseDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorCreateV1")
public class ValidatorCreateDTOv1 extends TwinValidatorBaseDTOv1 {
}
