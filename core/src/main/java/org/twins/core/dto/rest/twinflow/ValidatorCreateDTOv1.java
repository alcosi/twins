package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorCreateV1")
public class ValidatorCreateDTOv1 extends ValidatorBaseDTOv1 {
}
