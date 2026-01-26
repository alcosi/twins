package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierFilterV1")
public class FactoryMultiplierFilterDTOv1 {
    @Schema(description = "id", example = DTOExamples.FACTORY_ID)
    public UUID id;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "multiplier id", example = DTOExamples.MULTIPLIER_ID)
    @RelatedObject(type = FactoryMultiplierDTOv1.class, name = "multiplier")
    public UUID multiplierId;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public boolean factoryConditionSetInvert;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}


