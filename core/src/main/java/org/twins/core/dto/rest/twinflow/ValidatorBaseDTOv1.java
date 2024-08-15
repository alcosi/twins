package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.HashMap;

@Data
@Accessors(chain = true)
@Schema(name = "ValidatorBaseV1")
public class ValidatorBaseDTOv1 {

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "validator featurer id")
    public Integer validatorFeaturerId;

    @Schema(description = "featurer params")
    public HashMap<String, String> validatorParams;

    @Schema(description = "invert")
    public Boolean invert;

    @Schema(description = "active")
    public Boolean active;
}
