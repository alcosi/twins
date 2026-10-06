package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorUpdateV1")
public class TwinValidatorUpdateDTOv1 {

    @NotNull
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "twin validator set id this validator belongs to")
    public UUID twinValidatorSetId;

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
