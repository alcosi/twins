package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineSaveV1")
public class FactoryPipelineSaveDTOv1 {
    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID inputTwinClassId;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean factoryConditionSetInvert;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "output status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID outputStatusId;

    @Schema(description = "next factory id", example = DTOExamples.FACTORY_ID)
    public UUID nextFactoryId;

    @Schema(description = "template twin id", example = DTOExamples.TWIN_ID)
    public UUID templateTwinId;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}
