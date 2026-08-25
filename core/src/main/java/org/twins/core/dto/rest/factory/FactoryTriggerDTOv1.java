package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryTriggerV1")
public class FactoryTriggerDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "factory id")
    @RelatedObject(type = FactoryDTOv1.class, name = "factory")
    public UUID factoryId;

    @Schema(description = "input twin class id")
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "twin trigger id")
    @RelatedObject(type = TwinTriggerDTOv1.class, name = "twinTrigger")
    public UUID twinTriggerId;

    @Schema(description = "condition set id")
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "twinFactoryConditionSet")
    public UUID twinFactoryConditionSetId;

    @Schema(description = "condition invert")
    public Boolean twinFactoryConditionInvert;

    @Schema(description = "is active")
    public Boolean active;

    @Schema(description = "is async")
    public Boolean async;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}
