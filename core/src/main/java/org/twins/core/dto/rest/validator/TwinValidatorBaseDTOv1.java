package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorBaseV1")
public class TwinValidatorBaseDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "grouped set of twin validators id")
    public UUID twinValidatorSetId;;

    @Schema(description = "validator featurer id")
    public Integer validatorFeaturerId;

    @Schema(description = "featurer params")
    public HashMap<String, String> validatorParams;

    @Schema(description = "invert")
    public Boolean invert;

    @Schema(description = "active")
    public Boolean active;

    @Schema(description = "description")
    public String description;

    @Schema(description = "order")
    public Integer order;

}
