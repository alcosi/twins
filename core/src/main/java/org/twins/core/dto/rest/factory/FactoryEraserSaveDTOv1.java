package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.factory.FactoryEraserAction;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryEraserSaveV1")
public class FactoryEraserSaveDTOv1 {
    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID inputTwinClassId;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID twinFactoryConditionSetId;

    @Schema(description = "factory condition invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean twinFactoryConditionInvert;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "action", example = DTOExamples.ERASER_ACTION)
    public FactoryEraserAction action;
}


