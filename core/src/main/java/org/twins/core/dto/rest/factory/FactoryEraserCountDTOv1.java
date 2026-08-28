package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.enums.factory.FactoryEraserAction;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryEraserCountV1")
public class FactoryEraserCountDTOv1 extends CountDTOv1 {
    @Schema(description = "factory id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "factory")
    public UUID factoryId;

    @Schema(description = "input twin class id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "factory condition set id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert flag")
    public Boolean factoryConditionSetInvert;

    @Schema(description = "active flag")
    public Boolean active;

    @Schema(description = "eraser action")
    public FactoryEraserAction action;
}
