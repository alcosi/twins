package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorUpdateV1")
public class ValidatorUpdateDTOv1 extends ValidatorDTOv1 {
}
