package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSaveV1")
public class TwinValidatorSaveDTOv1 {
    @Schema(description = "grouped set of twin validators id", example = DTOExamples.UUID_ID)
    public UUID twinValidatorSetId;

    @Schema(description = "validator featurer id", example = DTOExamples.FEATURER_ID)
    public Integer validatorFeaturerId;

    @Schema(description = "validator params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public Map<String, String> validatorParams;

    @Schema(description = "invert", example = DTOExamples.COUNT)
    public Boolean invert;

    @Schema(description = "active", example = DTOExamples.COUNT)
    public Boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "order", example = DTOExamples.COUNT)
    public Integer order;
}
