package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.validator.ValidatorDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorUpdateV1")
public class ValidatorUpdateDTOv1 extends ValidatorDTOv1 {
}
