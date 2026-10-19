package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorCreateV1")
public class TwinValidatorCreateDTOv1 {

    @NotNull
    @Schema(description = "twin validator set id this validator belongs to")
    public UUID twinValidatorSetId;

    @NotNull
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

    @NotNull
    @Schema(description = "order")
    public Integer order;

}
