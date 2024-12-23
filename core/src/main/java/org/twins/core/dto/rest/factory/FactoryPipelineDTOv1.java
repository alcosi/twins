package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineV1")
public class FactoryPipelineDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_ID)
    public UUID id;

    @Schema(description = "factory id", example = DTOExamples.FACTORY_ID)
    public UUID factoryId;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID inputTwinClassId;

    @Schema(description = "factory id", example = DTOExamples.FACTORY_ID)
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean factoryConditionSetInvert;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "output twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID outputTwinStatusId;

    @Schema(description = "next factory id", example = DTOExamples.FACTORY_ID)
    public UUID nextFactoryId;

    @Schema(description = "next factory limit scope", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean nextFactoryLimitScope;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "count pipeline steps", example = DTOExamples.COUNT)
    public Integer pipelineStepsCount;
}
