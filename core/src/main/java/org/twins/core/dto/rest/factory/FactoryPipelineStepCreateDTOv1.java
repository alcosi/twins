package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineStepCreateV1")
public class FactoryPipelineStepCreateDTOv1 {

    @Schema(description = "factory pipeline id", example = DTOExamples.FACTORY_PIPELINE_ID)
    public UUID factoryPipelineId;

    @NotNull
    @Schema(description = "order", example = DTOExamples.COUNT)
    public Integer order;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean factoryConditionSetInvert;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @NotNull
    @Schema(description = "filler featurer id", example = DTOExamples.FEATURER_ID)
    public Integer fillerFeaturerId;

    @Schema(description = "filler params", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> fillerParams;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "is optional", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean optional;
}
