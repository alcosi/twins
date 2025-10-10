package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.enums.factory.FactoryEraserAction;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryEraserV1")
public class FactoryEraserDTOv1 {
    @Schema(description = "id", example = DTOExamples.FACTORY_ID)
    public UUID id;

    @Schema(description = "factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "factory")
    public UUID factoryId;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean factoryConditionSetInvert;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "action", example = DTOExamples.ERASER_ACTION)
    public FactoryEraserAction action;
}


